package uk.ac.ox.cs.pdq.algebra;

import java.util.List;

import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Costable;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 * Top-level interface for any operator supporting input and output types.
 * It can be a fined-grained operator such as a relational algebra operator,
 * or more coarse views on specific plan operators.
 * 
 * @author Julien Leblay
 */
public interface Operator extends Costable {

	/**
	 * Specifies how the output of this operator is sorted.
	 */
	public static enum SortOrder { 
		/** The asc. */
		ASC, 
		/** The desc. */
		DESC, 
		/** The unsorted. */
		UNSORTED }


	/**
	 * Gets the input type of the operator.
	 *
	 * @return TupleType
	 */
	TupleType getInputType();

	/**
	 * TOCOMMENT is this the output TupleType?
	 * 
	 * Gets the type of the operator.
	 *
	 * @return Type
	 */
	TupleType getType();

	/**
	 * TOCOMMENT what columns?
	 * 
	 * Gets the columns.
	 *
	 * @return TupleType
	 */
	List<Term> getColumns();
}
