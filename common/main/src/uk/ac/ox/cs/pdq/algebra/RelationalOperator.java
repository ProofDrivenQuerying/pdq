package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.EstimateProvider;
import uk.ac.ox.cs.pdq.rewrite.Rewritable;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.util.Operator;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


/**
 * RelationalOperator defines a top-class for all logical relational operators.
 *
 * @author Julien Leblay
 */
public abstract class RelationalOperator implements Rewritable, Operator {

	/**
	 * ??? Not sure what this is
	 * The Enum SharingType.
	 */
	public static enum SharingType { 
 /** The nothing. */
 NOTHING, 
 /** The cache. */
 CACHE, 
 /** The blocking. */
 BLOCKING }

	/** The operator's type. */
	protected final TupleType outputType;

	/** The operator's input type. */
	protected final TupleType inputType;

	/** The operator's dataguide. */
	protected EstimateProvider<RelationalOperator> metadata;

	/**
	 * Checks if is closed.
	 *
	 * @return false if the operator has some unfulfilled inputs, true otherwise.
	 */
	public abstract boolean isClosed();

	/**
	 * Instantiates a new operator.
	 * @param input TupleType
	 * @param output TupleType
	 */
	protected RelationalOperator(TupleType input, TupleType output) {
		this.inputType = input;
		this.outputType = output;
	}

	/**
	 * Instantiates a new operator.
	 *
	 * @param output TupleType
	 */
	public RelationalOperator(TupleType output) {
		this(TupleType.EmptyTupleType, output);
	}

	/**
	 * A deep clone copy of the operator.
	 *
	 * @return a deep copy of the operator.
	 * @throws RelationalOperatorException the relational operator exception
	 */
	public abstract RelationalOperator deepCopy() throws RelationalOperatorException;

	/**
	 * Gets the output term at position i in this operator.
	 *
	 * @param i the index of the column to return.
	 * @return the column at index i.
	 */
	public abstract Term getColumn(int i);

	/**
	 * Gets the output terms of this operator.
	 *
	 * @return the columns
	 */
	public abstract List<Term> getColumns();

	/**
	 * Gets the input term list.
	 *
	 * @return the list of input terms
	 */
	public abstract List<Term> getInputTerms();

	/**
	 * Gets the output tuple type.
	 *
	 * @return the tuple Type of this operator
	 */
	public TupleType getType() {
		return this.outputType;
	}

	/**
	 * Gets the input tuple type.
	 *
	 * @return the input tuple Type of this operator
	 */
	public TupleType getInputType() {
		return this.inputType;
	}

	/**
	 * Guard-method to ensure sub-classes are used with valid states.
	 *
	 * @param o the operator
	 * @return the input type of the given operator.
	 */
	protected static TupleType outputType(RelationalOperator o) {
		Preconditions.checkNotNull(o);
		return o.getType();
	}

	/**
	 * Guard-method to ensure sub-classes are used with valid states.
	 *
	 * @param o the operator
	 * @return the input terms of the given operator.
	 */
	protected static List<Term> outputTerms(RelationalOperator o) {
		Preconditions.checkNotNull(o);
		return o.getColumns();
	}

	/**
	 * Guard-method to ensure sub-classes are used with valid states.
	 *
	 * @param o the operator
	 * @return the input type of the given operator.
	 */
	protected static TupleType inputType(RelationalOperator o) {
		Preconditions.checkNotNull(o);
		return o.getInputType();
	}

	/**
	 * Guard-method to ensure sub-classes are use with valid states.
	 *
	 * @param o the operator
	 * @return the input terms of the given operator.
	 */
	protected static List<Term> inputTerms(RelationalOperator o) {
		Preconditions.checkNotNull(o);
		return o.getInputTerms();
	}

	/**
	 * Gets the columns display.
	 *
	 * @return a list of human readable column headers.
	 */
	public List<String> getColumnsDisplay() {
		List<String> result = new ArrayList<>();
		for (Term t: this.getColumns()) {
			result.add(t.toString());
		}
		return result;
	}

	/**
	 * Gets the metadata.
	 *
	 * @return true if is output of this operator is sorted.
	 */
	public EstimateProvider<RelationalOperator> getMetadata() {
		return this.metadata;
	}

	/**
	 * Sets the metadata.
	 *
	 * @param md the new metadata
	 */
	public void setMetadata(EstimateProvider<RelationalOperator> md) {
		this.metadata = md;
	}

	/**
	 * Gets the depth.
	 *
	 * @return the depth of the lowest descendant of this operator
	 */
	public abstract Integer getDepth();

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.outputType == ((RelationalOperator) o).outputType
				&& this.inputType == ((RelationalOperator) o).inputType
				&& this.metadata == ((RelationalOperator) o).metadata;

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.outputType, this.inputType, this.metadata);
	}

	/**
	 * Checks if is quasi leaf.
	 *
	 * @return true if the operator is a leaf or the ancestor of a single leaf
	 */
	public abstract boolean isQuasiLeaf();

	/**
	 * Checks if is left deep.
	 *
	 * @return true if the operator is left-deep
	 */
	public abstract boolean isLeftDeep();

	/**
	 * Checks if is right deep.
	 *
	 * @return true if the operator is right-deep
	 */
	public abstract boolean isRightDeep();

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.rewrite.Rewritable#rewrite(uk.ac.ox.cs.pdq.rewrite.Rewriter)
	 */
	@Override
	public <I extends Rewritable, O> O rewrite(Rewriter<I, O> rewriter) throws RewriterException {
		return rewriter.rewrite((I) this);
	}

	/**
	 * Gets the accesses.
	 *
	 * @param operator the operator
	 * @return the access operators that are children of the input operator
	 */
	public static Collection<AccessOperator> getAccesses(RelationalOperator operator) {
		Collection<AccessOperator> result = new LinkedHashSet<>();
		if (operator instanceof Scan) {
			result.add(((Scan) operator));
			return result;
		}
		if (operator instanceof Access) {
			result.add(((Access) operator));
			return result;
		}
		if (operator instanceof DependentAccess) {
			result.add(((DependentAccess) operator));
			return result;
		}
		if (operator instanceof NaryOperator) {
			for (RelationalOperator child: ((NaryOperator)operator).getChildren()) {
				result.addAll(getAccesses(child));
			}
			return result;
		}
		if (operator instanceof UnaryOperator) {
			result.addAll(getAccesses(((UnaryOperator)operator).getChild()));
		}
		return result;
	}
	
	/**
	 * Gets the selections.
	 *
	 * @param operator the operator
	 * @return the selections
	 */
	public static Collection<Selection> getSelections(RelationalOperator operator) {
		Collection<Selection> result = new LinkedHashSet<>();
		if (operator instanceof Selection) {
			result.add(((Selection) operator));
			return result;
		}
		if (operator instanceof NaryOperator) {
			for (RelationalOperator child: ((NaryOperator)operator).getChildren()) {
				result.addAll(getSelections(child));
			}
			return result;
		}
		if (operator instanceof UnaryOperator) {
			result.addAll(getSelections(((UnaryOperator)operator).getChild()));
		}
		return result;
	}

	/**
	 * Gets the leaves.
	 *
	 * @param operator the operator
	 * @return the leave operators rooted below the input operator
	 */
	public static List<AccessOperator> getLeaves(RelationalOperator operator) {
		Preconditions.checkNotNull(operator);
		if( operator instanceof NaryOperator) {
			List<AccessOperator> ops = new ArrayList<>();
			for (RelationalOperator child: ((NaryOperator)operator).getChildren()) {
				ops.addAll(getLeaves(child));
			}
			return ops;
		}
		if (operator instanceof AccessOperator) {
			return Lists.newArrayList((AccessOperator) operator);
		}
		if (operator instanceof UnaryOperator) {
			return getLeaves(((UnaryOperator) operator).getChild());
		}
		throw new IllegalStateException("Check " + operator);
	}
}
