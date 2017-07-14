package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration.BinaryConfigurationTypes;
import uk.ac.ox.cs.pdq.planner.util.PlanUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/** TOCOMMENT: WHAT NOTES??
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

//	/**
//	 * Creates a top-down physical plan from a DAG plan.
//	 *
//	 * @param config BinaryConfiguration<S>
//	 * @return a relational expression equivalent to the given linear plan
//	 */
//	public static DAGPlan toDAGPlan(BinaryConfiguration config) {
//		return toDAGPlan(config, config.getInput(), config.getLeft(), config.getRight(), config.getType());
//	}

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
	private static RelationalTerm toDAGPlan(DAGChaseConfiguration config, Collection<? extends Term> inputs, DAGChaseConfiguration left, DAGChaseConfiguration right, BinaryConfigurationTypes type) {
		RelationalTerm lOp = left.getPlan();
		RelationalTerm rOp = right.getPlan();
		RelationalTerm operator = null;

		switch(type) {
		case PCOMPOSE:
			operator = new DependentJoinTerm(lOp, rOp);
			break;
		case MERGE:
			operator = new JoinTerm(lOp, rOp);
			break;
		case JCOMPOSE:
			operator = new DependentJoinTerm(lOp, rOp);
			break;
		case GENCOMPOSE:
			operator = new DependentJoinTerm(lOp, rOp);
			break;
		default:
			throw new java.lang.IllegalArgumentException();
		}
		
		plan.addChild(right.getPlan());
		return operator;
	}

	/**
	 * Creates a top-down physical plan from a DAG plan.
	 *
	 * @param config DAGConfiguration<S>
	 * @return a relational expression equivalent to the given linear plan
	 */
	public static RelationalTerm toDAGPlan(DAGChaseConfiguration config) {
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
	private static RelationalTerm toDAGPlan(ApplyRule config) {
		Relation relation = config.getRelation();
		AccessMethod binding = config.getBindingPositions();
		Collection<Atom> facts = config.getFacts();
		if (facts.isEmpty()) {
			//planRelation is a copy of the relation without the extra attribute in the schema, needed for chasing
			Relation planRelation = new Relation(relation.getName(), relation.getAttributes().subList(0, relation.getAttributes().size()-1)){};
			planRelation.setMetadata(relation.getMetadata());
			return AccessTerm.create(planRelation, binding);
		}
		RelationalTerm op = null;
		for (Atom fact: facts) {
			//planRelation is a copy of the relation without the extra attribute in the schema, needed for chasing
			Relation planRelation = new Relation(relation.getName(), relation.getAttributes().subList(0, relation.getAttributes().size()-1)){};
			planRelation.setMetadata(relation.getMetadata());
			planRelation.setAccessMethods(relation.getAccessMethods());
			RelationalTerm access = AccessTerm.create(planRelation, binding);//, fact.getTerms()
			Condition selectPredicates = PlanUtils.createSelectPredicates(fact.getTerms());
			if (selectPredicates != null) 
				access = SelectionTerm.create(selectPredicates, access);
			access = ProjectionTerm.create(access, renameAllVariables(access.getColumns(), fact.getTerms()));
			if (op == null) {
				op = access;
			} else {
				op = new JoinTerm(op, access);
			}
		}
		return op;
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
