package uk.ac.ox.cs.pdq.runtime.exec;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

// TODO: Auto-generated Javadoc
/**
 * Translate logical plans to physical relation plans.
 * 
 * @author Julien Leblay
 */
public class PlanTranslator {
	
	
	/**
	 * Translate a logical plan to a bottom-up physical plan.
	 *
	 * @param logOp the logical operator
	 * @return a physical that corresponds exactly to the given logical plan.
	 * In particular, not further optimization is applied to the resulting plan.
	 * It uses hash join as default for equijoins and NestedLoopJoin for 
	 * joins with arbitrary predicates.
	 */
	public static TupleIterator translate(RelationalTerm logOp) {
		Preconditions.checkArgument(logOp != null);
		return null;
	}

}
