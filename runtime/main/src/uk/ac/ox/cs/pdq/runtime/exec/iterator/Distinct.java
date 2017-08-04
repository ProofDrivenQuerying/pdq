package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;

// TODO: Auto-generated Javadoc
/**
 * Distinct removes duplicates in a result set.
 * 
 * @author Julien Leblay
 */
public class Distinct extends TupleIterator {
	
	/**  The sole child of the operator. */
	protected final TupleIterator child;

	/**  The iteratorCache. */
	protected Set<Tuple> cache = new LinkedHashSet<>();
	
	/**  The next result to return. */
	protected Tuple nextResult = null;
	
	/**
	 * Instantiates a new operator.
	 * 
	 * @param child TupleIterator
	 */
	public Distinct(TupleIterator child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Assert.assertNotNull(child);
		this.child = child;
	}
	
	@Override
	public TupleIterator[] getChildren() {
		return new TupleIterator[]{this.child};
	}

	@Override
	public TupleIterator getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#open()
	 */
	@Override
	public void open() {
		Assert.assertTrue(this.open == null || this.open);
		this.child.open();
		this.open = true;
		this.nextTuple();
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#reset()
	 */
	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.child.reset();
		this.cache.clear();
		this.nextTuple();
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#interrupt()
	 */
	@Override
	public void interrupt() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.nextResult = null;
		this.interrupted = true;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		return !this.interrupted && this.nextResult != null;
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		if (this.nextResult == null) {
			throw new NoSuchElementException("End of operator reached.");
		}
		Tuple result = this.nextResult;
		this.nextTuple();
		return result;
	}

	/**
	 * Prepares the next tuple to return. If the end of the operator is reached,
	 * nextTuple shall be null.
	 */
	private void nextTuple() {
		this.nextResult = null;
		do {
			if (this.child.hasNext()) {
				this.nextResult = this.child.next();
				
			} else {
				this.nextResult = null;
			}
		} while (this.cache.contains(this.nextResult));
		if (this.nextResult != null) {
			this.cache.add(this.nextResult);
		}
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#bind(uk.ac.ox.cs.pdq.util.Tuple)
	 */
	@Override
	public void bind(Tuple t) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(t != null);
		Assert.assertTrue(RuntimeUtilities.typeOfAttributesEqualsTupleType(t.getType(), this.inputAttributes));
		this.child.bind(t);
	}
}
