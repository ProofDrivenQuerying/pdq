package uk.ac.ox.cs.pdq.algebra;


/**
 * Distinct removes duplicates in a result set.
 *
 * @author Julien Leblay
 */
public class Distinct extends UnaryOperator {

	/**
	 * Instantiates a new operator
	 * @param child LogicalOperator
	 */
	public Distinct(RelationalOperator child) {
		super(child);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.relational.logical.LogicalOperator#deepCopy()
	 */
	@Override
	public Distinct deepCopy() throws RelationalOperatorException {
		return new Distinct(this.child.deepCopy());
	}
}
