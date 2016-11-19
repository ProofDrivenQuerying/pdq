package uk.ac.ox.cs.pdq.algebra;

/**
 * Distinct unary operator removes duplicates in a result set.
 *
 * @author Julien Leblay
 * TOCOMMENT:  THIS CLASS SHOULD DIE!
 */
public class Distinct extends UnaryOperator {

	/**
	 * Instantiates a new distinct operator that has a relational operator as a child.
	 *
	 * @param child LogicalOperator
	 */
	public Distinct(RelationalOperator child) {
		super(child);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#deepCopy()
	 */
	@Override
	public Distinct deepCopy() throws RelationalOperatorException {
		return new Distinct(this.child.deepCopy());
	}
}
