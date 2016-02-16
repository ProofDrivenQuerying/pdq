package uk.ac.ox.cs.pdq.util;

import java.util.List;

import uk.ac.ox.cs.pdq.fol.Term;


// TODO: Auto-generated Javadoc
/**
 * Top-level interface for any operator accepting input and output types.
 * This can be fined-grained operator such as relational algebra operators,
 * or more coarse views on specific plan operators.
 * 
 * @author Julien Leblay
 */
public interface Operator extends Costable {

	/**
	 * The Enum SortOrder.
	 */
	public static enum SortOrder { 
 /** The asc. */
 ASC, 
 /** The desc. */
 DESC, 
 /** The unsorted. */
 UNSORTED }
	
//	/**
//	 * @return a list of pair of column/sortorder, specifying how the output
//	 * of this operator is sorted.
//	 */
//	List<Pair<Term, SortOrder>> getSortOrder();
	
	/**
 * Gets the input type.
 *
 * @return TupleType
 */
	TupleType getInputType();

	/**
	 * Gets the type.
	 *
	 * @return Type
	 */
	TupleType getType();

	/**
	 * Gets the columns.
	 *
	 * @return TupleType
	 */
	List<Term> getColumns();
}
