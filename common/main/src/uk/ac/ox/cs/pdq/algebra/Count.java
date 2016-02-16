package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Count counts the number of tuples in a result set and returns it.
 *
 * @author Julien Leblay
 */
public class Count extends UnaryOperator {

	/**
	 * Instantiates a new operator.
	 *
	 * @param child LogicalOperator
	 */
	public Count(RelationalOperator child) {
		super(inputType(child), inputTerms(child),
			TupleType.DefaultFactory.create(Integer.class),
			Lists.<Term>newArrayList(new Variable("Count(" + child.getColumns() + ")")),
			child);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.relational.logical.LogicalOperator#deepCopy()
	 */
	@Override
	public Count deepCopy() throws RelationalOperatorException {
		return new Count(this.child.deepCopy());
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.relational.logical.LogicalOperator#getColumnsDisplay()
	 */
	@Override
	public List<String> getColumnsDisplay() {
		List<String> result = new ArrayList<>();
		StringBuilder builder = new StringBuilder("COUNT(");
		String sep = "";
		for (String s: super.getColumnsDisplay()) {
			builder.append(sep).append(s);
			sep = ",";
		}
		builder.append(')');
		result.add(builder.toString());
		return result;
	}
}
