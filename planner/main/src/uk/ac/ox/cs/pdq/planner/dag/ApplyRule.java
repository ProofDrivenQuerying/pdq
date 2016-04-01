package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Instances of unary DAG configurations.
 * They are of the form ApplyRule(R,\vec{b}), where R is an accessibility axiom corresponding to method mt on relation R, 
 * and \vec{b} is a binding of the universally quantified variables
 * of R to chase constants or schema constants. The input constants are all those chase constants in \vec{b} where the
 * corresponding variable of R occurs within the R atoms of R at an input position of method mt. The outputs facts
 * of the configuration are any inferred accessible facts produced
 * be applying R with binding \vec{b}, as well as all facts that are consequences from these under the copy of the integrity
 * constraints. Calculating these output facts requires a consequence closure procedure.
 *  
 *  
 * @author Efthymia Tsamoura
 *
 */
public class ApplyRule extends DAGChaseConfiguration {

	/**  An accessibility axiom. */
	private final AccessibilityAxiom rule;
	
	/** The facts of this configuration. These must share the same constants for the input positions of the accessibility axiom */
	private final Set<Atom> facts;

	/**  The string representation of this configuration. */
	private String toString;
	
	/**
	 * Instantiates a new apply rule.
	 *
	 * @param state 		The state of this configuration.
	 * @param rule 		Input accessibility axiom
	 * @param facts 		Input facts. These must share the same constants for the input positions of the input accessibility axiom
	 */
	public ApplyRule(
			AccessibleChaseState state,
			AccessibilityAxiom rule,
			Set<Atom> facts
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
		DAGPlan plan = DAGPlanGenerator.toDAGPlan(this);
		this.setPlan(plan);
		Preconditions.checkState(this.getInput().containsAll(this.getPlan().getInputs()));
		Preconditions.checkState(this.getPlan().getInputs().containsAll(this.getPlan().getInputs()));
	}

	/**
	 * Gets the rule.
	 *
	 * @return AccessibilityAxiom
	 */
	public AccessibilityAxiom getRule() {
		return this.rule;
	}


	/**
	 * Gets the facts.
	 *
	 * @return 		the facts of this configuration
	 */
	public Collection<Atom> getFacts() {
		return this.facts;
	}

	/**
	 * Gets the binding positions.
	 *
	 * @return 		the access method
	 */
	public AccessMethod getBindingPositions() {
		return this.rule.getAccessMethod();
	}

	/**
	 * Gets the relation.
	 *
	 * @return 		the relation of this configuration
	 */
	public Relation getRelation() {
		return this.rule.getBaseRelation();
	}
	
	/**
	 * Generates the initial chase facts of this configuration.
	 *
	 * @param chaser the chaser
	 * @param query the query
	 * @param accessibleSchema the accessible schema
	 */
	public void generate(Chaser chaser, Query<?> query, AccessibleSchema accessibleSchema) {
		this.getState().generate(accessibleSchema, this.rule, this.facts);
		chaser.reasonUntilTermination(this.getState(), accessibleSchema.getInferredAccessibilityAxioms());
	}
	
	/**
	 * To string.
	 *
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
	 * Clone.
	 *
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
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getApplyRules()
	 */
	public Collection<ApplyRule> getApplyRules() {
		return Sets.newHashSet(this);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getApplyRulesList()
	 */
	public List<ApplyRule> getApplyRulesList() {
		return Lists.newArrayList(this);
	}
	
}
