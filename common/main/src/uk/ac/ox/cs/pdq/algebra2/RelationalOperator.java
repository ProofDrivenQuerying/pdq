package uk.ac.ox.cs.pdq.algebra2;

import uk.ac.ox.cs.pdq.util.Costable;

/**
 * Top-level interface for any operator supporting input and output types.
 * It can be a fined-grained operator such as a relational algebra operator,
 * or more coarse views on specific plan operators.
 * 
 * @author Julien Leblay
 */
public interface RelationalOperator extends Costable {
	
	int getArity();
}
