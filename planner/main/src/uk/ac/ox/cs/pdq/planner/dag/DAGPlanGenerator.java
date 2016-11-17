package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.DependentJoin;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.PlanUtils;
import uk.ac.ox.cs.pdq.plan.TreePlan;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration.BinaryConfigurationTypes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Implement algorithms 1 and 2 from the dependent join notes.
 *
 * @author Julien Leblay
 *
 */
public class DAGPlanGenerator {

	/**
	 * To dag plan.
	 *
	 * @param left DAGConfiguration<S>
	 * @param right DAGConfiguration<S>
	 * @param type BinaryConfigurationTypes
	 * @return DAGPlan
	 */
	public static DAGPlan toDAGPlan(DAGChaseConfiguration left, DAGChaseConfiguration right, BinaryConfigurationTypes type) {
		Set<Constant> inputs = Sets.newLinkedHashSet(right.getInput());
		inputs.removeAll(left.getOutput());
		inputs.addAll(left.getInput());
		return toDAGPlan(inputs, left, right, type);
	}

	/**
	 * Creates a top-down physical plan from a DAG plan.
	 *
	 * @param config BinaryConfiguration<S>
	 * @return a relational expression equivalent to the given linear plan
	 */
	public static DAGPlan toDAGPlan(BinaryConfiguration config) {
		return toDAGPlan(config, config.getInput(), config.getLeft(), config.getRight(), config.getType());
	}

	/**
	 * Creates a top-down physical plan from a DAG plan.
	 *
	 * @param inputs Collection<? extends Term>
	 * @param left DAGConfiguration<S>
	 * @param right DAGConfiguration<S>
	 * @param type BinaryConfigurationTypes
	 * @return a relational expression equivalent to the given linear plan
	 */
	private static DAGPlan toDAGPlan(Collection<? extends Term> inputs, DAGChaseConfiguration left, DAGChaseConfiguration right, BinaryConfigurationTypes type) {
		return toDAGPlan(null, inputs, left, right, type);
	}

	/**
	 * Creates a top-down physical plan from a DAG plan.
	 *
	 * @param config DAGConfiguration<S>
	 * @param inputs Collection<? extends Term>
	 * @param left DAGConfiguration<S>
	 * @param right DAGConfiguration<S>
	 * @param type BinaryConfigurationTypes
	 * @return a relational expression equivalent to the given linear plan
	 */
	private static DAGPlan toDAGPlan(DAGChaseConfiguration config, Collection<? extends Term> inputs, DAGChaseConfiguration left, DAGChaseConfiguration right, BinaryConfigurationTypes type) {
		RelationalOperator lOp = left.getPlan().getOperator();
		RelationalOperator rOp = right.getPlan().getOperator();
		RelationalOperator operator = null;

		switch(type) {
		case PCOMPOSE:
			operator = new DependentJoin(lOp, rOp);
			break;
		case MERGE:
			operator = new Join(lOp, rOp);
			break;
		case JCOMPOSE:
			operator = new DependentJoin(lOp, rOp);
			break;
		case GENCOMPOSE:
			operator = new DependentJoin(lOp, rOp);
			break;
		default:
			throw new java.lang.IllegalArgumentException();
		}

		TreePlan plan = new TreePlan(inputs, operator);
		plan.addChild(left.getPlan());
		plan.addChild(right.getPlan());
		return plan;
	}

	/**
	 * Creates a top-down physical plan from a DAG plan.
	 *
	 * @param config DAGConfiguration<S>
	 * @return a relational expression equivalent to the given linear plan
	 */
	public static DAGPlan toDAGPlan(DAGChaseConfiguration config) {
		if (config instanceof ApplyRule) {
			return toDAGPlan((ApplyRule) config);
		} else if (config instanceof BinaryConfiguration) {
			return toDAGPlan((BinaryConfiguration) config);
		}
		throw new IllegalStateException("DAGConfiguration type " + config + " not supported.");
	}

	/**
	 * Creates a top-down physical plan from a DAG plan.
	 *
	 * @param config ApplyRule
	 * @return a relational expression equivalent to the given linear plan
	 */
	private static DAGPlan toDAGPlan(ApplyRule config) {
		Relation relation = config.getRelation();
		AccessMethod binding = config.getBindingPositions();
		Collection<Atom> facts = config.getFacts();
		if (facts.isEmpty()) {
			return new DAGPlan(new DependentAccess(relation, binding));
		}
		RelationalOperator op = null;
		for (Atom fact: facts) {
			RelationalOperator access = new DependentAccess(relation, binding, fact.getTerms());
			uk.ac.ox.cs.pdq.algebra.predicates.Predicate selectPredicates = PlanUtils.createSelectPredicates(fact.getTerms());
			if (selectPredicates != null) {
				access = new Selection(selectPredicates, access);
			}
			access = new Projection(access, renameAllVariables(access.getColumns(), fact.getTerms()));
			if (op == null) {
				op = access;
			} else {
				op = new Join(op, access);
			}
		}
		return new DAGPlan(config.getInput(), op);
	}

	/**
	 * Rename all variables.
	 *
	 * @param from List<Term>
	 * @param to List<Term>
	 * @return Map<Integer,Term>
	 */
	private static Map<Integer, Term> renameAllVariables(List<Term> from, List<Term> to) {
		Preconditions.checkArgument(from.size() == to.size());
		Map<Integer, Term> result = new LinkedHashMap<>();
		for (int i = 0, l = from.size(); i < l; i++) {
			if (to.get(i).isVariable() || to.get(i).isUntypedConstant()) {
				result.put(i, to.get(i));
			}
		}
		return result;
	}
}
