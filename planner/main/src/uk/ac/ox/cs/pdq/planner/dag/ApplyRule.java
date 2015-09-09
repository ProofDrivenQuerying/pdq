package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * An ApplyRule configuration. Corresponds to an access
 * @author Efthymia Tsamoura
 *
 */
public class ApplyRule extends DAGChaseConfiguration {

	/** An accessibility axiom  */
	private final AccessibilityAxiom rule;
	
	/** The facts of this configuration. These must share the same constants for the input positions of the accessibility axiom */
	private final Set<Predicate> facts;

	/** The string representation of this configuration*/
	private String toString;
	
	/**
	 * 
	 * @param accessibleSchema
	 * @param query
	 * 		The input query
	 * @param chaser
	 * 		Chase reasoner
	 * @param state
	 * 		The state of this configuration.
	 * @param dominance
	 * 		Perform dominance checks
	 * @param successDominance
	 * 		Performs success dominance checks
	 * @param costEstimator
	 * 		Estimates the configuration's plan
	 * @param rule
	 * 		Input accessibility axiom
	 * @param facts
	 * 		Input facts. These must share the same constants for the input positions of the input accessibility axiom
	 */
	public ApplyRule(
			AccessibleSchema accessibleSchema,
			Query<?> query,
			Chaser chaser,
			AccessibleChaseState state,
			Dominance[] dominance,
			SuccessDominance successDominance,
			CostEstimator<DAGPlan> costEstimator,
			AccessibilityAxiom rule,
			Set<Predicate> facts
			) {		
		super(accessibleSchema, query, chaser, state, null,
				PlannerUtility.getInputConstants(rule, facts),
				Utility.getConstants(facts),
				ConfigurationUtility.arrayCopy(dominance),
				successDominance.clone(),
				1,
				0,
				costEstimator);
		Preconditions.checkNotNull(rule);
		Preconditions.checkNotNull(facts);
		this.rule = rule;
		this.facts = facts;
		DAGPlan plan = PlanGenerator.toPlan(this);
		this.setPlan(plan);
		this.getCostEstimator().cost(plan);
		Preconditions.checkState(this.getInput().containsAll(this.getPlan().getInputs()));
		Preconditions.checkState(this.getPlan().getInputs().containsAll(this.getPlan().getInputs()));
	}

	/**
	 * @param facts
	 * @return
	 * 		a configuration that comprises only the facts derived using only the input facts.
	 * 		The input facts must be a subset of this configuration's facts
	 */
	public DAGChaseConfiguration prune(Set<Predicate> facts) {
		ApplyRule configuration = new ApplyRule(
				this.getAccessibleSchema(),
				this.getQuery(),
				this.getChaser(),
				this.getState(),
				ConfigurationUtility.arrayCopy(this.getDominanceDetectors()),
				this.getSuccessDominanceDetector().clone(),
				this.getCostEstimator(),
				this.getRule(),
				facts);
		configuration.generate();
		return configuration;
	}

	/**
	 * @return AccessibilityAxiom
	 */
	public AccessibilityAxiom getRule() {
		return this.rule;
	}


	/**
	 * 
	 * @return
	 * 		the facts of this configuration
	 */
	public Collection<Predicate> getFacts() {
		return this.facts;
	}

	/**
	 * 
	 * @return
	 * 		the access method
	 */
	public AccessMethod getBindingPositions() {
		return this.rule.getAccessMethod();
	}

	/**
	 * @return 
	 * 		the relation of this configuration
	 */
	public Relation getRelation() {
		return this.rule.getBaseRelation();
	}

	/**
	 * Generates the initial state of this configuration
	 */
	public void generate() {
		this.getState().generate(this.getAccessibleSchema(), this.rule, this.facts);
		this.getChaser().reasonUntilTermination(this.getState(), this.getQuery(), this.getAccessibleSchema().getInferredAccessibilityAxioms());
	}


	/**
	 * @param input
	 * @return the facts that lead to the derivation of the input facts
	 */
	public Set<Predicate> getFiringsThatExposeFacts(Collection<Predicate> input) {
		Set<Predicate> ret = new LinkedHashSet<>();
		Relation baseRelation = this.rule.getBaseRelation();
		InferredAccessibleRelation infAccRelation = this.getAccessibleSchema().getInferredAccessibleRelation(baseRelation);
		for(Predicate fact:this.facts) {
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

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			this.toString = "APPLYRULE" + "(" + this.rule.getBaseRelation().getName() +
					"(" + Joiner.on(",").join(this.rule.getAccessMethod().getInputs()) + ")" +
					"{" + Joiner.on(",").join(this.facts) + "}" + ")";
		}
		return this.toString;
	}

	/**
	 * @return ApplyRule<S>
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#clone()
	 */
	@Override
	public ApplyRule clone() {
		return new ApplyRule(
				this.getAccessibleSchema(),
				this.getQuery(),
				this.getChaser(),
				this.getState().clone(),
				this.getDominanceDetectors(),
				this.getSuccessDominanceDetector(),
				this.getCostEstimator(),
				this.getRule(),
				Sets.newHashSet(this.facts));
	}
}
