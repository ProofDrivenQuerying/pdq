package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;

/**
 * Unions the results of its children
 * 
 * @author Julien Leblay
 */
public class Union extends NaryIterator {

	/** The iteratorCache */
	private Set<Tuple> cache = new LinkedHashSet<>();

	/** The next result to return */
	private Tuple nextTuple = null;

	/** The iterator of the current operator's children */
	private Iterator<TupleIterator> childIterator = null;

	/** The iterator of the current children */
	private TupleIterator currentChild = null;
		
	/**
	 * Instantiates a new operator.
	 * 
	 * @param children
	 *            the children
	 */
	public Union(List<TupleIterator> children) {
		this(inputColumns(firstChild(children)), children);
	}

	/**
	 * Instantiates a new union.
	 * 
	 * @param children
	 *            the children
	 */
	public Union(TupleIterator... children) {
		this(toList(children));
	}

	/**
	 * Instantiates a new operator.
	 * 
	 * @param inputs the input parameters
	 * @param children the children
	 */
	public Union(List<Typed> inputs, List<TupleIterator> children) {
		super(TupleType.DefaultFactory.createFromTyped(inputs), inputs, 
				outputType(firstChild(children)), 
				outputColumns(firstChild(children)),
				children);
		Preconditions.checkArgument(!this.children.isEmpty(), "Attempting to instantiate union operator with an empty list of children.");
		Preconditions.checkArgument(sameTypes(this.children), "Attempting to instantiate union operator with inconsistent types.");
	}
	
	private static boolean sameTypes(Collection<TupleIterator> children) {
		TupleType type = null;
		for (TupleIterator i: children) {
			if (type == null) {
				type = i.getType();
			} else if (!type.equals(i.getType())) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#deepCopy()
	 */
	@Override
	public Union deepCopy() {
		List<TupleIterator> clones = new ArrayList<>();
		for (TupleIterator child: this.children) {
			clones.add(child.deepCopy());
		}
		return new Union(clones);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#open()
	 */
	@Override
	public void open() {
		super.open();
		this.childIterator = this.children.iterator();
		this.currentChild = this.childIterator.next();
		this.nextTuple();
	}

	/**
	 * Prepares the next tuple to return. If the end of the operator is reached,
	 * nextResult shall be null.
	 */
	private void nextTuple() {
		while (this.currentChild.hasNext()) {
			this.nextTuple = this.currentChild.next();
			if (!this.cache.contains(this.nextTuple)) {
				this.cache.add(this.nextTuple);
				return;
			}
		}
		while (this.childIterator.hasNext()) {
			this.currentChild = this.childIterator.next();
			while (this.currentChild.hasNext()) {
				this.nextTuple = this.currentChild.next();
				if (!this.cache.contains(this.nextTuple)) {
					this.cache.add(this.nextTuple);
					return;
				}
			}
		}
		this.nextTuple = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		if (this.interrupted) {
			return false;
		}
		return this.nextTuple != null;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#next()
	 */
	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		if (this.nextTuple == null) {
			throw new NoSuchElementException("End of operator reached.");
		}
		Tuple result = this.nextTuple;
		this.nextTuple();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		this.childIterator = this.children.iterator();
		this.currentChild = this.childIterator.next();
		this.nextTuple();
		this.cache.clear();
	}
}
