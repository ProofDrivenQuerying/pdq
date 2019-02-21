package uk.ac.ox.cs.pdq.planner.dag;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.plancreation.PlanCreationUtility;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.util.Utility;

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
			AccessibleChaseInstance state,
			AccessibilityAxiom rule,
			Set<Atom> facts
			) {		
		super(state, ApplyRule.getInputConstants(rule, facts), Utility.getUntypedConstants(facts), 1);
		Preconditions.checkNotNull(rule);
		Preconditions.checkNotNull(facts);
		this.rule = rule;
		this.facts = facts;
		this.plan = PlanCreationUtility.createSingleAccessPlan(this.rule.getBaseRelation(), this.rule.getAccessMethod(), this.facts);
	}

	/**
	 *
	 * @return AccessibilityAxiom
	 */
	public AccessibilityAxiom getRule() {
		return this.rule;
	}

	/**
	 *
	 * @return 		the facts of this configuration
	 */
	public Collection<Atom> getFacts() {
		return this.facts;
	}

	/**
	 *s
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
	public void generate(Chaser chaser, ConjunctiveQuery query, Dependency[] dependencies) {
		this.getState().generate(this.rule, this.facts);
		chaser.reasonUntilTermination(this.getState(), dependencies);
	}
	
	/**
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
				new LinkedHashSet<>(this.facts));
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
	
	/**
	 * Creates the initial configurations.
	 *
	 * @return a list of ApplyRule configurations based on the facts derived after chasing the input schema with the canonical database of the query
	 * @throws SQLException 
	 */
	public static List<DAGChaseConfiguration> createInitialApplyRuleConfigurations(
			PlannerParameters parameters,
			ConjunctiveQuery query, 
			AccessibleSchema accessibleSchema, 
			Chaser chaser, 
			DatabaseManager connection
			) throws SQLException {
		AccessibleDatabaseChaseInstance state = null;
		state = new AccessibleDatabaseChaseInstance(query, accessibleSchema, connection, false);
		chaser.reasonUntilTermination(state, accessibleSchema.getOriginalDependencies());
	
		List<DAGChaseConfiguration> collection = new ArrayList<>();
		Collection<Pair<AccessibilityAxiom,Collection<Atom>>> groupsOfFacts = state.groupFactsByAccessMethods(accessibleSchema.getAccessibilityAxioms());
		for (Pair<AccessibilityAxiom, Collection<Atom>> groupOfFacts: groupsOfFacts) {
			ApplyRule applyRule = null;
			Collection<Collection<Atom>> groupForGivenAccessMethod = new LinkedHashSet<>();
			switch (parameters.getFollowUpHandling()) {
			case MINIMAL:
				for (Atom p: groupOfFacts.getRight()) {
					groupForGivenAccessMethod.add(Sets.newHashSet(p));
				}
				break;
			default:
				groupForGivenAccessMethod.add(groupOfFacts.getRight());
				break;
			}
			for (Collection<Atom> atoms:groupForGivenAccessMethod) {
				AccessibleChaseInstance newState = (uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance) 
						new AccessibleDatabaseChaseInstance(atoms, connection, false);
				applyRule = new ApplyRule(
						newState,
						groupOfFacts.getLeft(),
						Sets.newHashSet(atoms)
						);
				applyRule.generate(chaser, query, accessibleSchema.getInferredAccessibilityAxioms());
				collection.add(applyRule);
			}
		}
		return collection;
	}

	/**
	 * Given a set of facts F=F1.... on the same relation R,
	 * and an accessibility axiom on R, corresponding to performing a particular access method mt
	 * on R; find the constants that lie within the input positions of each Fi for mt 
	 *  
	 * @param rule the accessibility axiom for some method being fired
	 * @param facts the facts
	 * @return the constants of the input facts that correspond to the input positions of the method
	 */
	public static Collection<Constant> getInputConstants(AccessibilityAxiom rule, Set<Atom> facts) {
		Collection<Constant> inputs = new LinkedHashSet<>();
		for(Atom fact:facts) {
			List<Constant> constants = fact.getTypedAndUntypedConstants(rule.getAccessMethod().getInputs());
			for(Constant constant:constants) {
				// only untyped constants can be inputs.
				if(constant.isUntypedConstant()) {
					inputs.add(constant);
				}
			}
		}
		return inputs;
	}
	
}
