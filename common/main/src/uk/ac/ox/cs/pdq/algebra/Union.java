package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Union the results of its children.
 *
 * @author Julien Leblay
 */
public class Union extends NaryOperator {

	/**
	 * Instantiates a new operator.
	 *
	 * @param children the children
	 */
	public Union(RelationalOperator... children) {
		super(children);
	}

	/**
	 * Instantiates a new operator.
	 *
	 * @param children the children
	 */
	public Union(List<RelationalOperator> children) {
		super(children);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.relational.logical.NaryOperator#deepCopy()
	 */
	@Override
	public Union deepCopy() throws RelationalOperatorException {
		List<RelationalOperator> clones = new ArrayList<>();
		for (RelationalOperator child: this.children) {
			clones.add(child.deepCopy());
		}
		return new Union(clones);
	}
}
