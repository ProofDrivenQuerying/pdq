package uk.ac.ox.cs.pdq.algebra;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.base.Preconditions;

/**
 * Superclass to all unary logical operators.
 *
 * @author Julien Leblay
 */
public abstract class UnaryOperator extends RelationalOperator {

	/** The child of the operator. */
	protected RelationalOperator child;

	/** The ??? (output) columns. */
	protected final List<Term> columns;

	/** The input terms . */
	protected final List<Term> inputTerms;

	/** The position of the input term among the outputs. */
	protected List<Integer> inputMapping;

	/**
	 * Instantiates a new operator.
	 *
	 * @param inputType TupleType
	 * @param inputTerms List<Term>
	 * @param outputOverride TupleType
	 * @param columns List<Term>
	 * @param child LogicalOperator
	 */
	protected UnaryOperator(
			TupleType inputType, List<Term> inputTerms,
			TupleType outputOverride, List<Term> columns, RelationalOperator child) {
		super(inputType, outputOverride);
		Preconditions.checkArgument(inputType.size() == inputTerms.size());
		this.child = child;
		this.columns = columns;
		this.inputTerms = inputTerms;
	}

	/**
	 * Instantiates a new operator.
	 *
	 * @param typeOverride TupleType
	 * @param columns List<Term>
	 * @param child LogicalOperator
	 */
	public UnaryOperator(TupleType typeOverride, List<Term> columns, RelationalOperator child) {
		this(inputType(child), inputTerms(child), typeOverride, columns, child);
	}

	/**
	 * Instantiates a new operator.
	 *
	 * @param typeOverride TupleType
	 * @param child LogicalOperator
	 */
	public UnaryOperator(TupleType typeOverride, RelationalOperator child) {
		this(typeOverride, outputTerms(child), child);
	}

	/**
	 * Instantiates a new operator.
	 *
	 * @param child LogicalOperator
	 */
	public UnaryOperator(RelationalOperator child) {
		this(outputType(child), child);
	}

	/**
	 * Gets the child of the operator.
	 *
	 * @return the unique child of this operator
	 */
	public RelationalOperator getChild() {
		return this.child;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#getColumn(int)
	 */
	@Override
	public Term getColumn(int i) {
		Preconditions.checkArgument(0 <= i && i < this.columns.size()) ;
		return this.getColumns().get(i);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#getColumns()
	 */
	@Override
	public List<Term> getColumns() {
		return this.columns;
	}

	/**
	 * Gets the input terms.
	 *
	 * @return List<Term>
	 */
	@Override
	public List<Term> getInputTerms() {
		return this.inputTerms;
	}

	/**
	 * ???
	 * Gets the depth.
	 *
	 * @return Integer
	 */
	@Override
	public Integer getDepth() {
		Integer childDepth = this.child.getDepth();
		assert childDepth >= 0;
		return this.child.getDepth() + 1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName()).append('(');
		result.append(this.child.toString());
		result.append(')');
		return result.toString();
	}

	/**
	 * Two unary operators are equal if the their child, their input and output terms are the same
	 * ("same" is implemented with the corresponding equals() methods)
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		return super.equals(o)
				&& this.getClass().isInstance(o)
				&& (this.child == null ? ((UnaryOperator) o).child == null : this.child.equals(((UnaryOperator) o).child))
				&& this.columns.equals(((UnaryOperator) o).columns)
				&& this.inputTerms.equals(((UnaryOperator) o).inputTerms)
				;

	}

	@Override
	public int hashCode() {
		return Objects.hash(this.outputType, this.metadata, this.inputType,
				this.child, this.columns, this.inputTerms);
	}

	/**
	 * Checks if the operator is closed.
	 *
	 * @return boolean
	 */
	@Override
	public boolean isClosed() {
		return this.child.isClosed();
	}

	/**
	 * ???
	 * Checks if is quasi leaf.
	 *
	 * @return boolean
	 */
	@Override
	public boolean isQuasiLeaf() {
		return this.child.isQuasiLeaf();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#isLeftDeep()
	 */
	@Override
	public boolean isLeftDeep() {
		return this.child.isLeftDeep();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#isRightDeep()
	 */
	@Override
	public boolean isRightDeep() {
		return this.child.isRightDeep();
	}
}