package uk.ac.ox.cs.pdq.planner.linear;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.DependentJoin;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.Operators;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.Scan;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.algebra.SubPlanAlias;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.LinearPlan;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Creates linear plans
 *
 * @author Efthymia Tsamoura
 */
public class LinearPlanGenerator {

	/**
	 * Creates a linear plan by appending the access and middlewares commands of the input configuration to the input parent plan
	 * @param configuration 
	 * @param parent 
	 * @return 
	 */
	public static LinearPlan createLinearPlan(LinearChaseConfiguration configuration, LinearPlan parent) {
		return create(configuration, parent, inferOutputChaseConstants(configuration));
	}

	/**
	 * Creates a linear plan using the subplans of the input sequence of nodes
	 * @param nodes List<T>
	 * @return 
	 */
	public static<T extends SearchNode> LinearPlan createLinearPlan(List<T> nodes) {
		LinearPlan parentPlan = null;
		for (T node: nodes) {
			parentPlan = LinearPlanGenerator.createLinearPlan(node.getConfiguration(), parentPlan);
			
		}
		return parentPlan;
	}

	/**
	 * Creates a linear plan by appending the access and middlewares commands of the input configuration to the input parent plan.
	 * The top level operator is a projection that projects the input terms
	 * @param configuration 
	 * @param parent 
	 * @param toProject 
	 * 		Terms to project in the resulting plan
	 * @return 
	 */
	private static LinearPlan create(LinearConfiguration configuration,
			LinearPlan parent,
			List<Term> toProject) {
		Preconditions.checkArgument(configuration.getExposedCandidates() != null);
		RelationalOperator op1 = null;
		AccessOperator access = null;
		RelationalOperator predAlias = null;
		if (parent != null) {
			predAlias = new SubPlanAlias(parent);
		}

		for (Candidate candidate: configuration.getExposedCandidates()) {
			if (access == null) {
				if (candidate.getBinding().getType() == Types.FREE) {
					access = new Scan(candidate.getRelation());
				} else {
					access = new DependentAccess(candidate.getRelation(), candidate.getBinding(), candidate.getFact().getTerms());
				}
			}
			RelationalOperator op2 = (RelationalOperator) access;
			uk.ac.ox.cs.pdq.algebra.predicates.Predicate selectPredicates = Operators.createSelectPredicates(candidate.getFact().getTerms());
			if (selectPredicates != null) {
				op2 = new Selection(selectPredicates, op2);
			}
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
		LinearPlan lp = new LinearPlan(op1);
		if (parent != null) {
			lp.addPrefix(parent);
			parent.addSuffix(lp);
		}
		return lp;
	}


	/**
	 * @param configuration 
	 * @return the output constants of the input configuration
	 */
	public static List<Term> inferOutputChaseConstants(LinearChaseConfiguration configuration) {
		Collection<Term> result = new LinkedHashSet();
		for (Predicate fact: configuration.getOutputFacts()) {
			for (Term t: fact.getTerms()) {
				if (t.isVariable() || t.isSkolem()) {
					result.add(t);
				}
			}
		}
		return Lists.newArrayList(result);
	}

	/**
	 * @param candidate 
	 * @param toProject 
	 * @return
	 * 		a map of positions to terms of a candidate fact
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
