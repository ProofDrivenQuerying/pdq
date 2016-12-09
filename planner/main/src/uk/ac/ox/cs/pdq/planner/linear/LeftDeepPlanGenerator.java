package uk.ac.ox.cs.pdq.planner.linear;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.DependentJoin;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.Scan;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.plan.PlanUtils;
import uk.ac.ox.cs.pdq.plan.SubPlanAlias;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Transforms linear chase configurations to left-deep plans. 
 *
 * @author Efthymia Tsamoura
 */
public class LeftDeepPlanGenerator {

	/**
	 * Creates a linear plan by appending the access and middlewares commands of the input configuration to the input parent plan.
	 *
	 * @param configuration the configuration
	 * @param parent the parent
	 * @return the left deep plan
	 */
	public static LeftDeepPlan createLeftDeepPlan(LinearChaseConfiguration configuration, LeftDeepPlan parent) {
		return createLeftDeepPlan(configuration, parent, inferOutputChaseConstants(configuration));
	}

	/**
	 * Creates a linear plan using the subplans of the input sequence of nodes.
	 *
	 * @param <T> the generic type
	 * @param nodes List<T>
	 * @return the left deep plan
	 */
	public static<T extends SearchNode> LeftDeepPlan createLeftDeepPlan(List<T> nodes) {
		LeftDeepPlan parentPlan = null;
		for (T node: nodes) {
			parentPlan = LeftDeepPlanGenerator.createLeftDeepPlan(node.getConfiguration(), parentPlan);
			
		}
		return parentPlan;
	}

	/**
	 * Creates a linear plan by appending the access and middlewares commands of the input configuration to the input parent plan.
	 * The top level operator is a projection that projects the terms toProject.
	 * 
	 * The newly created access and middleware command are created as follows:
	 * For an exposed fact f, If f has been exposed by an input-free accessibility axiom (access method), 
	 * then create an input-free access else create a dependent access operator.
	 * If f has schema constants in output positions or repeated constants, then these schema constants map to filtering predicates.
	 * Finally, project the variables that correspond to output chase constants. 
	 *
	 * @param c the c
	 * @param parent 		The input parent plan. This is the plan of the parent configuration of c, i.e., the configuration that is augmented with the exposed facts of c.
	 * @param toProject 		Terms to project in the resulting plan
	 * @return the left deep plan
	 */
	private static LeftDeepPlan createLeftDeepPlan(LinearConfiguration c,
			LeftDeepPlan parent,
			List<Term> toProject) {
		Preconditions.checkArgument(c.getExposedCandidates() != null);
		RelationalOperator op1 = null;
		AccessOperator access = null;
		RelationalOperator predAlias = null;
		if (parent != null) {
			predAlias = new SubPlanAlias(parent);
		}

		//Iterate over each exposed fact
		for (Candidate candidate: c.getExposedCandidates()) {
			if (access == null) {
				//If this fact has been exposed by an input-free accessibility axiom (access method), then create an input-free access
				//else create a dependent access operator
				if (candidate.getAccessMethod().getType() == Types.FREE) {
					Relation planRelation = new Relation(candidate.getRelation().getName(), candidate.getRelation().getAttributes().subList(0, candidate.getRelation().getAttributes().size()-1)){};
					planRelation.setMetadata(candidate.getRelation().getMetadata());
					planRelation.setAccessMethods(candidate.getRelation().getAccessMethods());
					access =  new Scan(planRelation);
				} else {
					//planRelation is a copy of the relation without the extra attribute in the schema, needed for chasing
					Relation planRelation = new Relation(candidate.getRelation().getName(), candidate.getRelation().getAttributes().subList(0, candidate.getRelation().getAttributes().size()-1)){};
					planRelation.setMetadata(candidate.getRelation().getMetadata());
					access = new DependentAccess(planRelation,candidate.getAccessMethod(), candidate.getFact().getTerms());
				}
			}
			RelationalOperator op2 = (RelationalOperator) access;
			//Find if this fact has schema constants in output positions or repeated constants
			//If yes, then these schema constants map to filtering predicates
			uk.ac.ox.cs.pdq.algebra.predicates.Predicate selectPredicates = PlanUtils.createSelectPredicates(candidate.getFact().getTerms());
			if (selectPredicates != null) {
				op2 = new Selection(selectPredicates, op2);
			}
			//Project the variables that correspond to output chase constants 
			LinkedHashMap<Integer, Term> renaming = getOutputMap(candidate, toProject);
			if (renaming.size() != op2.getColumns().size()
					|| !renaming.values().containsAll(op2.getColumns())
					|| !op2.getColumns().containsAll(renaming.values())) {
				op2 = new Projection(op2, renaming);
			}

			if (op1 == null) {
				op1 = op2;
			} else {
				op1 = new Join(op1, op2);
			}
		}
		if (parent != null) {
			if (access instanceof DependentAccess) {
				op1 = new DependentJoin(predAlias, op1);
			} else {
				op1 = new Join(predAlias, op1);
			}
		}
		LeftDeepPlan lp = new LeftDeepPlan(op1, access);
		if (parent != null) {
			lp.addPrefix(parent);
			parent.addSuffix(lp);
		}
		return lp;
	}


	/**
	 * Infer output chase constants.
	 *
	 * @param configuration the configuration
	 * @return the output constants of the input configuration
	 */
	public static List<Term> inferOutputChaseConstants(LinearChaseConfiguration configuration) {
		Collection<Term> result = new LinkedHashSet<>();
		for (Atom fact: configuration.getOutputFacts()) {
			for (Term t: fact.getTerms()) {
				if (t.isVariable() || t.isUntypedConstant()) {
					result.add(t);
				}
			}
		}
		return Lists.newArrayList(result);
	}

	/**
	 * Gets the output map.
	 *
	 * @param candidate the candidate
	 * @param toProject the to project
	 * @return 		a map of positions to terms of a candidate fact
	 */
	private static LinkedHashMap<Integer, Term> getOutputMap(Candidate candidate, List<? extends Term> toProject) {
		LinkedHashMap<Integer, Term> ret = new LinkedHashMap();
		for (Map.Entry<Integer, Term> entry: candidate.getOutputChaseConstants().entrySet()) {
			if (toProject.contains(entry.getValue())) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}
}
