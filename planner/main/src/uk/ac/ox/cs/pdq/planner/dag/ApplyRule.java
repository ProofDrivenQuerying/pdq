package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
	 * @param state
	 * 		The state of this configuration.
	 * @param rule
	 * 		Input accessibility axiom
	 * @param facts
	 * 		Input facts. These must share the same constants for the input positions of the input accessibility axiom
	 */
	public ApplyRule(
			AccessibleChaseState state,
			AccessibilityAxiom rule,
			Set<Predicate> facts
			) {		
		super(state, 
				PlannerUtility.getInputConstants(rule, facts),
				Utility.getConstants(facts),
				1,
				0);
		Preconditions.checkNotNull(rule);
		Preconditions.checkNotNull(facts);
		this.rule = rule;
		this.facts = facts;
		DAGPlan plan = PlanGenerator.toPlan(this);
		this.setPlan(plan);
		Preconditions.checkState(this.getInput().containsAll(this.getPlan().getInputs()));
		Preconditions.checkState(this.getPlan().getInputs().containsAll(this.getPlan().getInputs()));
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
	public void generate(Chaser chaser, Query<?> query, AccessibleSchema accessibleSchema) {
		this.getState().generate(accessibleSchema, this.rule, this.facts);
		chaser.reasonUntilTermination(this.getState(), query, accessibleSchema.getInferredAccessibilityAxioms());
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
				this.getState().clone(),
				this.getRule(),
				Sets.newHashSet(this.facts));
	}
	
	public Collection<ApplyRule> getApplyRules() {
		return Sets.newHashSet(this);
	}

	public List<ApplyRule> getApplyRulesList() {
		return Lists.newArrayList(this);
	}
	
}
