package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Logical operator representation of an scan over a statically defined
 * collection of tuples.
 *
 * @author Julien Leblay
 */
public class StaticInput extends RelationalOperator {

	/** The input table of the access. */
	private final Collection<Tuple> tuples;

	/** The columns. */
	protected List<Term> columns;

	/**
	 * Instantiates a new static input.
	 *
	 * @param constants List<TypedConstant<?>>
	 */
	public StaticInput(List<TypedConstant<?>> constants) {
		super(TupleType.DefaultFactory.createFromTyped(constants));
		this.columns = Utility.typedToTerms(constants);
		this.tuples = Lists.newArrayList(TupleType.DefaultFactory.createFromTyped(constants).createTuple(constants));
	}

	/**
	 * Instantiates a new static input.
	 *
	 * @param columns List<Attribute>
	 * @param tuples Collection<Tuple>
	 */
	public StaticInput(List<Attribute> columns, Collection<Tuple> tuples) {
		super(getConsistentType(tuples));
		this.tuples = tuples;
		this.columns = Utility.typedToTerms(columns);
	}

	/**
	 * Instantiates a new static input.
	 *
	 * @param tuples Collection<Tuple>
	 */
	public StaticInput(Collection<Tuple> tuples) {
		super(getConsistentType(tuples));
		this.tuples = tuples;
		this.columns = new ArrayList<>();
		if (tuples != null && !tuples.isEmpty()) {
			Tuple t = tuples.iterator().next();
			for (int i = 0, l = t.size(); i < l; i++) {
				this.columns.add(new Variable(String.valueOf(t.getValue(i))));
			}
		}
	}

	/**
	 * Gets the consistent type.
	 *
	 * @param tuples the tuples
	 * @return true, if all the tuples in the given collection have consistent
	 * types.
	 */
	private static TupleType getConsistentType(Collection<Tuple> tuples) {
		if (tuples == null || tuples.isEmpty()) {
			return TupleType.EmptyTupleType;
		}
		TupleType result = null;
		for (Tuple t: tuples) {
			if (result == null) {
				result = t.getType();
			} else {
				assert result.equals(t.getType()): "Input tuple collection has inconsistent tuple types.";
			}
		}
		return result;
	}

	/**
	 * Gets the tuples.
	 *
	 * @return the tuples scanned by the operator
	 */
	public Collection<Tuple> getTuples() {
		return this.tuples;
	}

	/**
	 * Gets the depth.
	 *
	 * @return Integer
	 */
	@Override
	public Integer getDepth() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#deepCopy()
	 */
	@Override
	public StaticInput deepCopy() throws RelationalOperatorException {
		return new StaticInput(this.tuples);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName()).append('(');
		result.append(this.tuples).append(')');
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#getColumn(int)
	 */
	@Override
	public Term getColumn(int i) {
		return this.columns.get(i);
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
		return Lists.newArrayList();
	}

	/**
	 * Equals.
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		return super.equals(o)
				&& this.getClass().isInstance(o)
				&& this.tuples.equals(((StaticInput) o).tuples)
				&& this.columns.equals(((StaticInput) o).columns);

	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.outputType, this.metadata, this.inputType,
				this.columns, this.tuples);
	}

	/**
	 * Checks if is closed.
	 *
	 * @return boolean
	 */
	@Override
	public boolean isClosed() {
		return true;
	}

	/**
	 * Checks if is quasi leaf.
	 *
	 * @return boolean
	 */
	@Override
	public boolean isQuasiLeaf() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#isLeftDeep()
	 */
	@Override
	public boolean isLeftDeep() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#isRightDeep()
	 */
	@Override
	public boolean isRightDeep() {
		return true;
	}
}
