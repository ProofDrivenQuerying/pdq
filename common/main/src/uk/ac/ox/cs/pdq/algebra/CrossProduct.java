package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * CrossProduct implements a n-ary cartesian product.
 *
 * @author Julien Leblay
 */
public class CrossProduct extends NaryOperator {

	/**
	 * Instantiates a new cross product.
	 *
	 * @param children
	 *            the children
	 */
	public CrossProduct(RelationalOperator... children) {
		super(children);
	}

	/**
	 * Instantiates a new cross product.
	 *
	 * @param children
	 *            the children
	 */
	public CrossProduct(List<RelationalOperator> children) {
		super(children);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#deepCopy()
	 */
	@Override
	public CrossProduct deepCopy() throws RelationalOperatorException {
		List<RelationalOperator> clones = new ArrayList<>();
		for (RelationalOperator child: this.children) {
			clones.add(child.deepCopy());
		}
		return new CrossProduct(clones);
	}
}