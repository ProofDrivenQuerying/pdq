package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Configuration post pruning. Removes redundant access commands and follow-up joins
 *
 * @author Efthymia Tsamoura
 */
public class ConfigurationPostPruning {

	protected final Query<?> query;

	protected final AccessibleSchema accessibleSchema;

	protected final Chaser chaser;

	protected final CostEstimator<DAGPlan> costEstimator;
	
	/** A successful configuration*/
	private final DAGChaseConfiguration configuration;

	/** Facts that match the query */
	private final Collection<Predicate> queryFacts;
	/**
	 * True if the input configuration is pruned
	 */
	private Boolean isPruned = false;

	/**
	 *
	 * @param configuration
	 * 		Input configuration
	 * @param queryFacts
	 * 		Facts that match the query. 
	 * An exception is thrown if the input facts are not accessible or inferred accessible ones
	 *
	 */
	public ConfigurationPostPruning(
			Query<?> query,
			AccessibleSchema accessibleSchema,
			Chaser chaser,
			CostEstimator<DAGPlan> costEstimator,
			DAGChaseConfiguration configuration, 
			Collection<Predicate> queryFacts) {
		Preconditions.checkNotNull(query);
		Preconditions.checkNotNull(accessibleSchema);
		Preconditions.checkNotNull(chaser);
		Preconditions.checkNotNull(costEstimator);
		Preconditions.checkNotNull(configuration);
		Preconditions.checkArgument(configuration.isClosed());
		Preconditions.checkArgument(configuration.isSuccessful(query));
		Preconditions.checkNotNull(queryFacts);
		Collection<Predicate> qF = new LinkedHashSet<>();
		for(Predicate queryFact: queryFacts) {
			if(queryFact.getSignature() instanceof InferredAccessibleRelation) {
				qF.add(queryFact);
			} else if(!(queryFact.getSignature() instanceof AccessibleRelation)) {
				throw new java.lang.IllegalArgumentException();
			}
		}
		this.query = query;
		this.accessibleSchema = accessibleSchema;
		this.chaser = chaser;
		this.costEstimator = costEstimator;
		this.configuration = configuration;
		this.queryFacts = qF;
	}


	/**
	 * Prunes the input configuration
	 * @return the post-pruned configuration
	 * @throws PlannerException
	 */
	public DAGChaseConfiguration prune() throws PlannerException {
		Collection<Predicate> infAccFacts
		= this.getAccessibilityAxioms(this.queryFacts, this.configuration.getState());
		DAGChaseConfiguration output = this.pruneRecursive(this.configuration, infAccFacts);
		if(this.isPruned) {
			return output;
		}
		return null;
	}


	/**
	 * 
	 * @param configuration
	 * @param queryFacts
	 * @return
	 * 		a configuration that performs only the accesses required to match the input query facts
	 * @throws PlannerException
	 */
	private DAGChaseConfiguration pruneRecursive(DAGChaseConfiguration configuration, Collection<Predicate> queryFacts) throws PlannerException {
		
		/** If the configuration is an ApplyRule one, then find the facts that after chasing lead to the derivation of the input query facts  */
		if(configuration instanceof ApplyRule) {
			Set<Predicate> firings = this.getFiringsThatExposeFacts(((ApplyRule) configuration), this.accessibleSchema, queryFacts);
			if(!firings.equals(((ApplyRule) configuration).getFacts())) {
				this.isPruned = true;
				return this.prune(((ApplyRule) configuration), firings, this.chaser, this.query, this.accessibleSchema);
			}
			return configuration;
		}
		/** Otherwise, prune first the right subconfiguration, and then, the right subconfiguration*/
		DAGChaseConfiguration c1 = ((BinaryConfiguration)configuration).getLeft();
		DAGChaseConfiguration c2 = ((BinaryConfiguration)configuration).getRight();

		Set<Predicate> atoms1 = Sets.newLinkedHashSet(c1.getState().getDerivedInferred());
		Set<Predicate> atoms2 = Sets.newLinkedHashSet(c2.getState().getDerivedInferred());
		if(atoms1.containsAll(atoms2)) {
			this.isPruned = true;
			return this.pruneRecursive(c1, queryFacts);
		}
		DAGChaseConfiguration ret = null;
		DAGChaseConfiguration r = this.pruneRecursive(c2, queryFacts);
		Collection<Constant> input2 = r.getInput();
		Collection<Set<Predicate>> sets = ConfigurationUtility.getMinimalSetThatExposesConstants(c1, input2, this.accessibleSchema);
		for(Set<Predicate> set:sets) {
			DAGChaseConfiguration l = this.pruneRecursive(c1, set);
			DAGChaseConfiguration output = new BinaryConfiguration(
					l, r
					);
			this.costEstimator.cost(configuration.getPlan());
			((BinaryConfiguration)configuration).chase(this.chaser, this.query, this.accessibleSchema.getInferredAccessibilityAxioms());
			if(ret == null || ret.getPlan().getCost().greaterThan(output.getPlan().getCost())) {
				ret = output;
			}
		}
		return ret;
	}
	
	/**
	 * 
	 * @param input
	 * @param state
	 * @return the accessed facts that lead to the derivation of the input facts
	 */
	protected Collection<Predicate> getAccessibilityAxioms(Collection<Predicate> input, AccessibleChaseState state) {
		Collection<Predicate> facts = new LinkedHashSet<>();
		for(Predicate fact:input) {
			Pair<Constraint, Collection<Predicate>> pair = state.getProvenance(fact);
			if(pair!= null) {
				if(pair.getLeft() instanceof AccessibilityAxiom) {
					facts.add(fact);
				}
				else {
					facts.addAll(this.getAccessibilityAxioms(pair.getRight(), state));
				}
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
		}
		return facts;
	}


	/**
	 * 
	 * @return true if the input configuration is post-pruned
	 */
	public Boolean getIsPruned() {
		return this.isPruned;
	}
	
	/**
	 * @param facts
	 * @return
	 * 		a configuration that comprises only the facts derived using only the input facts.
	 * 		The input facts must be a subset of this configuration's facts
	 */
	private DAGChaseConfiguration prune(ApplyRule applyRule, Set<Predicate> facts, Chaser chaser, Query<?> query, AccessibleSchema accessibleSchema) {
		ApplyRule configuration = new ApplyRule(
				applyRule.getState(),
				applyRule.getRule(),
				facts);
		configuration.generate(chaser, query, accessibleSchema);
		return configuration;
	}
	
	/**
	 * @param input
	 * @return the facts that lead to the derivation of the input facts
	 */
	private Set<Predicate> getFiringsThatExposeFacts(ApplyRule applyRule, AccessibleSchema accessibleSchema, Collection<Predicate> input) {
		Set<Predicate> ret = new LinkedHashSet<>();
		Relation baseRelation = applyRule.getRelation();
		InferredAccessibleRelation infAccRelation = accessibleSchema.getInferredAccessibleRelation(baseRelation);
		for(Predicate fact:applyRule.getFacts()) {
			for(Predicate inputFact:input) {
				if(inputFact.getSignature() instanceof InferredAccessibleRelation &&
						inputFact.getSignature().equals(infAccRelation) &&
						inputFact.getTerms().equals(fact.getTerms())) {
					ret.add(fact);
					break;
				}
			}
		}
		return ret;
	}
	
}
