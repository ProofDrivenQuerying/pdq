package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Removes redundant access commands and follow-up joins from a configuration that matches the input query.
 *
 * @author Efthymia Tsamoura
 * @deprecated 
 */
public class ConfigurationPostPruning {

	/**  The accessible counterpart of the user query *. */
	protected final Query<?> accessibleQuery;

	/**  The accessible counterpart of the input schema *. */
	protected final AccessibleSchema accessibleSchema;

	/**  Implements the chase reasoning algorithm*. */
	protected final Chaser chaser;

	/**  Estimates the cost of a plan *. */
	protected final CostEstimator<DAGPlan> costEstimator;
	
	/**  Facts that match the query. */
	private final Collection<Atom> queryFacts;
	
	/**  A successful configuration. */
	private final DAGChaseConfiguration configuration;

	/**  True if the input configuration is pruned. */
	private Boolean isPruned = false;
	
	/**
	 * Sets up a configuration postpruning object .
	 *
	 * @param accessibleQuery 		The accessible counterpart of the user query
	 * @param accessibleSchema 		The accessible counterpart of the input schema
	 * @param chaser 		Implements the chase reasoning algorithm
	 * @param costEstimator 		Estimates the cost of a plan
	 * @param configuration 		A successful configuration
	 * @param queryFacts 		Facts in the input configuration that match the query
	 * 
	 * An exception is thrown if the input facts are not accessible or inferred accessible ones
	 */
	public ConfigurationPostPruning(
			Query<?> accessibleQuery,
			AccessibleSchema accessibleSchema,
			Chaser chaser,
			CostEstimator<DAGPlan> costEstimator,
			DAGChaseConfiguration configuration, 
			Collection<Atom> queryFacts) {
		Preconditions.checkNotNull(accessibleQuery);
		Preconditions.checkNotNull(accessibleSchema);
		Preconditions.checkNotNull(chaser);
		Preconditions.checkNotNull(costEstimator);
		Preconditions.checkNotNull(configuration);
		Preconditions.checkArgument(configuration.isClosed());
		Preconditions.checkArgument(configuration.isSuccessful(accessibleQuery));
		Preconditions.checkNotNull(queryFacts);
		Collection<Atom> qF = new LinkedHashSet<>();
		for(Atom queryFact: queryFacts) {
			if(queryFact.getSignature() instanceof InferredAccessibleRelation) {
				qF.add(queryFact);
			} else if(!(queryFact.getSignature() instanceof AccessibleRelation)) {
				throw new java.lang.IllegalArgumentException();
			}
		}
		this.accessibleQuery = accessibleQuery;
		this.accessibleSchema = accessibleSchema;
		this.chaser = chaser;
		this.costEstimator = costEstimator;
		this.configuration = configuration;
		this.queryFacts = qF;
	}
	
	/**
	 * Removes redundant access commands and follow-up joins from a configuration that matches the input query.
	 *
	 * @return 		the post-pruned configuration
	 * @throws PlannerException the planner exception
	 */
	public DAGChaseConfiguration prune() throws PlannerException {
		Collection<Atom> infAccFacts
		= this.getAccessibilityAxioms(this.queryFacts, this.configuration.getState());
		DAGChaseConfiguration output = this.pruneRecursive(this.configuration, infAccFacts);
		if(this.isPruned) {
			return output;
		}
		return null;
	}


	/**
	 * Prune recursive.
	 *
	 * @param configuration 		A successful configuration
	 * @param queryFacts 		Facts in the input configuration that match the query
	 * @return 		a configuration that performs only the accesses required to match the input query facts
	 * @throws PlannerException the planner exception
	 */
	private DAGChaseConfiguration pruneRecursive(DAGChaseConfiguration configuration, Collection<Atom> queryFacts) throws PlannerException {
		
		/** If the configuration is an ApplyRule one, then find the facts that after chasing lead to the derivation of the input query facts  */
		if(configuration instanceof ApplyRule) {
			Set<Atom> firings = this.getFiringsThatExposeFacts(((ApplyRule) configuration), this.accessibleSchema, queryFacts);
			if(!firings.equals(((ApplyRule) configuration).getFacts())) {
				this.isPruned = true;
				return this.prune(((ApplyRule) configuration), firings, this.chaser, this.accessibleQuery, this.accessibleSchema);
			}
			return configuration;
		}
		/** Otherwise, prune first the right subconfiguration, and then, the right subconfiguration*/
		DAGChaseConfiguration c1 = ((BinaryConfiguration)configuration).getLeft();
		DAGChaseConfiguration c2 = ((BinaryConfiguration)configuration).getRight();

		Set<Atom> atoms1 = Sets.newLinkedHashSet(c1.getState().getDerivedInferred());
		Set<Atom> atoms2 = Sets.newLinkedHashSet(c2.getState().getDerivedInferred());
		if(atoms1.containsAll(atoms2)) {
			this.isPruned = true;
			return this.pruneRecursive(c1, queryFacts);
		}
		DAGChaseConfiguration ret = null;
		DAGChaseConfiguration r = this.pruneRecursive(c2, queryFacts);
		Collection<Constant> input2 = r.getInput();
		Collection<Set<Atom>> sets = ConfigurationUtility.getMinimalSetThatExposesConstants(c1, input2, this.accessibleSchema);
		for(Set<Atom> set:sets) {
			DAGChaseConfiguration l = this.pruneRecursive(c1, set);
			DAGChaseConfiguration output = new BinaryConfiguration(
					l, r
					);
			this.costEstimator.cost(configuration.getPlan());
			((BinaryConfiguration)configuration).reasonUntilTermination(this.chaser, this.accessibleQuery, this.accessibleSchema.getInferredAccessibilityAxioms());
			if(ret == null || ret.getPlan().getCost().greaterThan(output.getPlan().getCost())) {
				ret = output;
			}
		}
		return ret;
	}
	
	/**
	 * Gets the accessibility axioms.
	 *
	 * @param input the input
	 * @param state the state
	 * @return the accessed facts that lead to the derivation of the input facts in the input chase state
	 */
	protected Collection<Atom> getAccessibilityAxioms(Collection<Atom> input, AccessibleChaseState state) {
		Collection<Atom> facts = new LinkedHashSet<>();
		for(Atom fact:input) {
			Pair<Constraint, Collection<Atom>> pair = state.getProvenance(fact);
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
	 * Gets the checks if is pruned.
	 *
	 * @return true if the configuration provided in the constructor is post-pruned
	 */
	public Boolean getIsPruned() {
		return this.isPruned;
	}
	
	/**
	 * Prune.
	 *
	 * @param applyRule the apply rule
	 * @param facts the facts
	 * @param chaser the chaser
	 * @param query the query
	 * @param accessibleSchema the accessible schema
	 * @return 		a configuration that comprises only the facts derived using only the input facts.
	 * 		The input facts must be a subset of this configuration's facts
	 */
	private DAGChaseConfiguration prune(ApplyRule applyRule, Set<Atom> facts, Chaser chaser, Query<?> query, AccessibleSchema accessibleSchema) {
		ApplyRule configuration = new ApplyRule(
				applyRule.getState(),
				applyRule.getRule(),
				facts);
		configuration.generate(chaser, query, accessibleSchema);
		return configuration;
	}
	
	/**
	 * Gets the firings that expose facts.
	 *
	 * @param applyRule the apply rule
	 * @param accessibleSchema the accessible schema
	 * @param input the input
	 * @return the facts that lead to the derivation of the input facts
	 */
	private Set<Atom> getFiringsThatExposeFacts(ApplyRule applyRule, AccessibleSchema accessibleSchema, Collection<Atom> input) {
		Set<Atom> ret = new LinkedHashSet<>();
		Relation baseRelation = applyRule.getRelation();
		InferredAccessibleRelation infAccRelation = accessibleSchema.getInferredAccessibleRelation(baseRelation);
		for(Atom fact:applyRule.getFacts()) {
			for(Atom inputFact:input) {
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
