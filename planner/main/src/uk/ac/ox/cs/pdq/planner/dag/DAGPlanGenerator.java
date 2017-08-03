package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Set;

import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration.BinaryConfigurationTypes;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;

//TODO 
/** TOCOMMENT: WHAT NOTES??
/**
 * Implement algorithms 1 and 2 from the dependent join notes.
 *
 * @author Julien Leblay
 *
 */
public class DAGPlanGenerator {
	
	/**
	 *
	 * @param left DAGConfiguration<S>
	 * @param right DAGConfiguration<S>
	 * @param type BinaryConfigurationTypes
	 * @return DAGPlan
	 */
	public static RelationalTerm toDAGPlan(DAGChaseConfiguration left, DAGChaseConfiguration right, BinaryConfigurationTypes type) {
		Set<Constant> inputs = Sets.newLinkedHashSet(right.getInput());
		inputs.removeAll(left.getOutput());
		inputs.addAll(left.getInput());
		
		RelationalTerm lOp = left.getPlan();
		RelationalTerm rOp = right.getPlan();
		RelationalTerm operator = null;

		switch(type) {
		case PCOMPOSE:
			operator = DependentJoinTerm.create(lOp, rOp);
			break;
		case MERGE:
			operator = JoinTerm.create(lOp, rOp);
			break;
		case JCOMPOSE:
			operator = DependentJoinTerm.create(lOp, rOp);
			break;
		case GENCOMPOSE:
			operator = DependentJoinTerm.create(lOp, rOp);
			break;
		default:
			throw new java.lang.IllegalArgumentException();
		}
		return operator;
	}

	/**
	 * Creates a top-down physical plan from a DAG plan.
	 *
	 * @param config DAGConfiguration<S>
	 * @return a relational expression equivalent to the given linear plan
	 */
	public static RelationalTerm toDAGPlan(DAGChaseConfiguration config) {
		if (config instanceof ApplyRule) 
			return PlanCreationUtility.createSingleAccessPlan(((ApplyRule) config).getRelation(), ((ApplyRule) config).getRule().getAccessMethod(), ((ApplyRule) config).getFacts());
		else if (config instanceof BinaryConfiguration) 
			return toDAGPlan((BinaryConfiguration) config);
		throw new IllegalStateException("DAGConfiguration type " + config + " not supported.");
	}
}
