package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.Collections;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.EmptyIterator;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class EmptyIteratorTest.
 *
 * @author Julien LEBLAY
 */
public class EmptyIteratorTest {

	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
	}
	
	/**
	 * Inits the empty.
	 */
	@Test public void initEmpty() {
		EmptyIterator empty = new EmptyIterator();
		Assert.assertEquals("Empty input columns must match that of initialization", Collections.EMPTY_LIST, empty.getInputColumns());
		Assert.assertEquals("Empty input type must match that of initialization", TupleType.EmptyTupleType, empty.getInputType());
		Assert.assertEquals("Empty output columns must match that of initialization", Collections.EMPTY_LIST, empty.getColumns());
		Assert.assertEquals("Empty output type must match that of initialization", TupleType.EmptyTupleType, empty.getType());
	}
	
	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		EmptyIterator empty = new EmptyIterator();
		EmptyIterator copy = empty.deepCopy();
		Assert.assertEquals("Empty iterator columns must match that of initialization", empty.getColumns(), copy.getColumns());
		Assert.assertEquals("Empty iterator type must match that of child", empty.getType(), copy.getType());
		Assert.assertEquals("Empty iterator inputs must match that of child", empty.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("Empty iterator input type must match that of child", empty.getInputType(), copy.getInputType());
		empty.open();
		copy.open();
		Assert.assertEquals("Empty next item must match", empty.hasNext(), copy.hasNext());
	}

	/**
	 * Checks for next.
	 */
	@Test public void hasNext() {
		EmptyIterator empty = new EmptyIterator();
		empty.open();
		Assert.assertFalse(empty.hasNext());
	}


	/**
	 * Next.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void next() {
		EmptyIterator empty = new EmptyIterator();
		empty.open();
		empty.next();
	}

	/**
	 * Reset filtered1.
	 */
	@Test public void resetFiltered1() {
		EmptyIterator empty = new EmptyIterator();
		empty.open();
		empty.reset();
	}

	/**
	 * Bind null.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindNull() {
		EmptyIterator empty = new EmptyIterator();
		empty.open();
		empty.bind(null);
	}
	
	/**
	 * Bind on unopened.
	 */
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		EmptyIterator empty = new EmptyIterator();
		empty.bind(Tuple.EmptyTuple);
	}
	
	/**
	 * Bind on interrupted.
	 */
	@Test(expected=IllegalStateException.class)
	public void bindOnInterrupted() {
		EmptyIterator empty = new EmptyIterator();
		empty.open();
		empty.interrupt();
		empty.bind(Tuple.EmptyTuple);
	}
	
	/**
	 * Bind on closed.
	 */
	@Test(expected=IllegalStateException.class)
	public void bindOnClosed() {
		EmptyIterator empty = new EmptyIterator();
		empty.open();
		empty.close();
		empty.bind(Tuple.EmptyTuple);
	}
	
	/**
	 * Bind illegal type.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindIllegalType() {
		EmptyIterator empty = new EmptyIterator();
		empty.open();
		empty.bind(TupleType.DefaultFactory.create(String.class, Integer.class).createTuple("four", 4));
	}
}