package uk.ac.ox.cs.pdq.util;

import java.util.List;

import uk.ac.ox.cs.pdq.fol.Term;


/**
 * Top-level interface for any operator accepting input and output types.
 * This can be fined-grained operator such as relational algebra operators,
 * or more coarse views on specific plan operators.
 * 
 * @author Julien Leblay
 */
public interface Operator extends Costable {

	/** */
	public static enum SortOrder { ASC, DESC, UNSORTED }
	
//	/**
//	 * @return a list of pair of column/sortorder, specifying how the output
//	 * of this operator is sorted.
//	 */
//	List<Pair<Term, SortOrder>> getSortOrder();
	
	/**
	 * @return TupleType
	 */
	TupleType getInputType();

	/**
	 * @return Type
	 */
	TupleType getType();

	/**
	 * @return TupleType
	 */
	List<Term> getColumns();
}
