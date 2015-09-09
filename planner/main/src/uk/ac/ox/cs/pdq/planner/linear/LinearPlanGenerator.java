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
	 * @param configuration LinearConfiguration
	 * @param parentPlan LinearPlan
	 * @param cf ControlFlows
	 * @return LinearPlan
	 */
	public static LinearPlan createLinearPlan(LinearChaseConfiguration configuration, LinearPlan parentPlan) {
		return create(configuration, parentPlan, inferOutputChaseConstants(configuration));
	}

	/**
	 * @param nodes List<T>
	 * @return LinearPlan
	 */
	public static<T extends SearchNode> LinearPlan createLinearPlan(List<T> nodes) {
		LinearPlan parentPlan = null;
		for (T node: nodes) {
			parentPlan = node.getConfiguration().createPlan(parentPlan);
		}
		return parentPlan;
	}

	/**
	 * @param configuration LinearConfiguration
	 * @param predecessor LinearPlan
	 * @param toProject List<Term>
	 * @return LinearPlan
	 */
	private static LinearPlan create(LinearConfiguration configuration,
			LinearPlan predecessor,
			List<Term> toProject) {
		Preconditions.checkArgument(configuration.getExposedCandidates() != null);
		RelationalOperator op1 = null;
		AccessOperator access = null;
		RelationalOperator predAlias = null;
		if (predecessor != null) {
			predAlias = new SubPlanAlias(predecessor);
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
		if (predecessor != null) {
			if (access instanceof DependentAccess) {
				op1 = new DependentJoin(predAlias, op1);
			} else {
				op1 = new Join(predAlias, op1);
			}
		}
		LinearPlan lp = new LinearPlan(op1, access);
		if (predecessor != null) {
			lp.addPrefix(predecessor);
			predecessor.addSuffix(lp);
		}
		return lp;
	}


	/**
	 * @param configuration LinearConfiguration
	 * @return List<Term>
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
	 * @param candidate Candidate
	 * @param toProject List<? extends Term>
	 * @return LinkedHashMap<Integer,Term>
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
