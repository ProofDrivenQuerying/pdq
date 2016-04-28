package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 * IsEmpty unary operator used for boolean queries. Return true if the database satisfies
 * the body of the query, false otherwise.
 *
 * @author Julien LEBLAY
 */
public class IsEmpty extends UnaryOperator {

	/**
	 * Instantiates a new operator.
	 *
	 * @param child LogicalOperator
	 */
	public IsEmpty(RelationalOperator child) {
		super(inputType(child), inputTerms(child),
				TupleType.DefaultFactory.create(Boolean.class),
				Lists.<Term>newArrayList(new Variable("IsEmpty(" + child.getColumns() + ")")),
				child);
	}

	/*
	 * (non-Javadoc)
	 * @see  uk.ac.ox.cs.pdq.algebra.RelationalOperator#deepCopy()
	 */
	@Override
	public IsEmpty deepCopy() throws RelationalOperatorException {
		return new IsEmpty(this.child.deepCopy());
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#getColumnsDisplay()
	 */
	@Override
	public List<String> getColumnsDisplay() {
		List<String> result = new ArrayList<>();
		result.add("IS_EMPTY");
		return result;
	}
}
