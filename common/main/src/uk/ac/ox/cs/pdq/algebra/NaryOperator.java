package uk.ac.ox.cs.pdq.algebra;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * NaryOperator defines a top-class for all operators with multiple children.
 *
 * @author Julien Leblay
 */
public abstract class NaryOperator extends RelationalOperator {

	/** The children. */
	protected List<RelationalOperator> children;

	/** The columns. */
	protected List<Term> columns;

	/** The input terms . */
	protected final List<Term> inputTerms;

	/**
	 * Instantiates a new operator.
	 *
	 * @param children
	 *            the children
	 */
	public NaryOperator(RelationalOperator... children) {
		this(Lists.newArrayList(children));
	}

	/**
	 * Instantiates a new operator.
	 *
	 * @param children
	 *            the children
	 */
	public NaryOperator(Collection<RelationalOperator> children) {
		this(inferInputType(children), inferType(children), children);
	}

	/**
	 * Instantiates a new operator.
	 *
	 * @param inputType TupleType
	 * @param outputOverride TupleType
	 * @param children the children
	 */
	public NaryOperator(TupleType inputType, TupleType outputOverride, RelationalOperator... children) {
		this(inputType, outputOverride, Lists.newArrayList(children));
	}

	/**
	 * Instantiates a new operator.
	 *
	 * @param inputType TupleType
	 * @param outputOverride TupleType
	 * @param children the children
	 */
	public NaryOperator(TupleType inputType, TupleType outputOverride, Collection<RelationalOperator> children) {
		this(inferInputTerms(children), outputOverride, children);
	}

	/**
	 * Instantiates a new operator.
	 *
	 * @param inputTerms List<Term>
	 * @param typeOverride TupleType
	 * @param children            the children
	 */
	public NaryOperator(List<Term> inputTerms, TupleType typeOverride, Collection<RelationalOperator> children) {
		super(inferType(inputTerms, children), typeOverride);
		Preconditions.checkArgument(!children.isEmpty(), "Attempting to instantiate a plan operator with an empty list of children.");
//		if (children != null && children.size() > 0) {
			this.children = ImmutableList.copyOf(children);
			this.columns = ImmutableList.copyOf(this.updateColumns());
//		} else {
//			this.children = ImmutableList.copyOf(new LinkedList<RelationalOperator>());
//			this.columns = ImmutableList.copyOf(new LinkedList<Term>());
//		}
		this.inputTerms = inputTerms;
	}

	/**
	 * Infer the tuple type of the given collection of children.
	 *
	 * @param children the children
	 * @return TupleType
	 */
	protected static TupleType inferType(Collection<RelationalOperator> children) {
		Preconditions.checkArgument(children != null);
		List<Type> result = new LinkedList<>();
		for (RelationalOperator child: children) {
			for (int i = 0, l = child.getType().size(); i < l; i++) {
				result.add(child.getType().getType(i));
			}
		}
		return TupleType.DefaultFactory.create(result.toArray(new Type[result.size()]));
	}

	/**
	 * TOCOMMENT what does each one of the following three methods do?
	 * Infer the tuple type of the given collection of children.
	 *
	 * @param terms List<Term>
	 * @param children the children
	 * @return TupleType
	 */
	protected static TupleType inferType(List<Term> terms, Collection<RelationalOperator> children) {
		Preconditions.checkNotNull(terms != null);
		Preconditions.checkNotNull(children);
		List<Type> result = new LinkedList<>();
		for (Term term: terms) {
			for (RelationalOperator child: children) {
				int position = child.getColumns().indexOf(term);
				if (position >= 0) {
					result.add(child.getType().getType(position));
					break;
				}
			}
		}
		if (result.size() != terms.size()) {
			Preconditions.checkState(result.size() == terms.size());
		}
		return TupleType.DefaultFactory.create(result.toArray(new Type[result.size()]));
	}

	/**
	 * Infer the tuple type of the given collection of children.
	 *
	 * @param children the children
	 * @return TupleType
	 */
	protected static TupleType inferInputType(Collection<RelationalOperator> children) {
		Preconditions.checkNotNull(children);
		List<Type> result = new LinkedList<>();
		for (RelationalOperator child: children) {
			for (int i = 0, l = child.getInputType().size(); i < l; i++) {
				result.add(child.getInputType().getType(i));
			}
		}
		return TupleType.DefaultFactory.create(result.toArray(new Type[result.size()]));
	}

	/**
	 * Infer the tuple type of the given collection of children.
	 *
	 * @param children the children
	 * @return List<Term>
	 */
	protected static List<Term> inferInputTerms(Collection<RelationalOperator> children) {
		Preconditions.checkNotNull(children);
		List<Term> result = new LinkedList<>();
		for (RelationalOperator child: children) {
			result.addAll(child.getInputTerms());
		}
		return result;
	}

	/**
	 * Deep copy of an operator.
	 *
	 * @return a deep copy of the operator.
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Override
	public abstract NaryOperator deepCopy() throws RelationalOperatorException;

	/**
	 * Gets the children of the operator.
	 *
	 * @return the children
	 */
	public List<RelationalOperator> getChildren() {
		return this.children;
	}

	/**
	 * Gets the column at index i.
	 *
	 * @param i the index of the column to return.
	 * @return the column at index i.
	 */
	@Override
	public Term getColumn(int i) {
		Preconditions.checkArgument(0 <= i && i < this.columns.size()) ;
		return this.columns.get(i);
	}

	/**
	 * Gets the columns.
	 *
	 * @return the columns
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
	 * Gets the type.
	 *
	 * @return the tuple Type of this operator
	 */
	@Override
	public TupleType getType() {
		return this.outputType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName()).append('(');
		if (this.children != null && !this.children.isEmpty()) {
			for (RelationalOperator child: this.children) {
				result.append(child.toString()).append(',');
			}
			result.deleteCharAt(result.length() - 1);
		}
		result.append(')');
		return result.toString();
	}

	/**
	 * Ensures the current operator's list of columns is consistent with the
	 * concatenation of lists of columns of its children.
	 * @return List<Term>
	 */
	protected List<Term> updateColumns() {
		List<Term> result = new ArrayList<>();
		for (RelationalOperator child: this.children) {
			result.addAll(child.getColumns());
		}
		return result;
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
				&& this.children.equals(((NaryOperator) o).children)
				&& this.columns.equals(((NaryOperator) o).columns)
				&& this.inputTerms.equals(((NaryOperator) o).inputTerms)
				;

	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.outputType, this.inputType, this.children,
				this.columns, this.inputTerms, this.metadata);
	}

	/**
	 * Gets the depth.
	 *
	 * @return Integer
	 */
	@Override
	public Integer getDepth() {
		Integer result = 0;
		for (RelationalOperator child: this.children) {
			Integer childDepth = child.getDepth();
			assert childDepth >= 0;
			result = Math.max(result, childDepth);
		}
		return result + 1;
	}

	/**
	 * Checks if is closed.
	 *
	 * @return boolean
	 */
	@Override
	public boolean isClosed() {
		for (RelationalOperator child: this.children) {
			if (!child.isClosed()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * TOCOMMENT
	 * Checks if is quasi leaf.
	 *
	 * @return boolean
	 */
	@Override
	public boolean isQuasiLeaf() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#isLeftDeep()
	 */
	@Override
	public boolean isLeftDeep() {
		Iterator<RelationalOperator> childrenIt = this.children.iterator();
		assert childrenIt.hasNext();
		RelationalOperator leftMost = childrenIt.next();
		boolean result = leftMost.isLeftDeep() || leftMost.isQuasiLeaf();
		while (childrenIt.hasNext() && result) {
			RelationalOperator child = childrenIt.next();
			result &= child.isQuasiLeaf();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#isRightDeep()
	 */
	@Override
	public boolean isRightDeep() {
		boolean result = true;
		for (Iterator<RelationalOperator> childrenIt = this.children.iterator();
				childrenIt.hasNext();) {
			RelationalOperator child = childrenIt.next();
			result &= childrenIt.hasNext() ? child.isQuasiLeaf() :
				(child.isRightDeep() || child.isQuasiLeaf());
		}
		return result;
	}
}
