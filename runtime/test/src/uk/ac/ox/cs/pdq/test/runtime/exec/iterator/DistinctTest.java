package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Distinct;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;

// TODO: Auto-generated Javadoc
/**
 * The Class DistinctTest.
 *
 * @author Julien LEBLAY
 */
public class DistinctTest extends UnaryIteratorTest {
	
	/** The iterator. */
	Distinct iterator;

	/** The empty child. */
	@Mock TupleIterator emptyChild;
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.runtime.exec.iterator.TupleIteratorTest#setup()
	 */
	@Before public void setup() {
		super.setup();
        MockitoAnnotations.initMocks(this);
        when(child.getColumns()).thenReturn(outputColumns);
		when(child.getType()).thenReturn(outputType);
		when(child.getInputColumns()).thenReturn(inputColumns);
		when(child.getInputType()).thenReturn(inputType);
		when(child.deepCopy()).thenReturn(child);
		when(child.hasNext()).thenReturn(true, true, true, true, true, true, false);
		when(child.next()).thenReturn(
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("one", 2, "str", 6), 
				outputType.createTuple("one", 2, "str", 6), 
				outputType.createTuple("two", 2, "str", 6));
		when(emptyChild.hasNext()).thenReturn(false);
		this.iterator = new Distinct(child);
	}
	
	/**
	 * Inits the count.
	 */
	@Test public void initCount() {
		this.iterator = new Distinct(child);
		Assert.assertEquals("Distinct child must match that of initialization", this.child, this.iterator.getChild());
		Assert.assertEquals("Distinct input columns must match that of initialization", this.inputColumns, this.iterator.getInputColumns());
		Assert.assertEquals("Distinct input type must match that of initialization", this.inputType, this.iterator.getInputType());
		Assert.assertEquals("Distinct output columns must match that of initialization", this.outputColumns, this.iterator.getColumns());
		Assert.assertEquals("Distinct output type must match that of initialization", this.outputType, this.iterator.getType());
	}
	
	/**
	 * Inits the distinct null child.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initDistinctNullChild() {
		new Distinct(null);
	}
	
	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		this.iterator = new Distinct(child);
		Distinct copy = this.iterator.deepCopy();
		Assert.assertEquals("Distinct iterators deep copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("Distinct iterator columns must match that of initialization", this.iterator.getColumns(), copy.getColumns());
		Assert.assertEquals("Distinct iterator type must match that of child", this.iterator.getType(), copy.getType());
		Assert.assertEquals("Distinct iterator inputs must match that of child", this.iterator.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("Distinct iterator input type must match that of child", this.iterator.getInputType(), copy.getInputType());
	}

	/**
	 * Checks for next for not empty.
	 */
	@Test public void hasNextForNotEmpty() {
		this.iterator = new Distinct(child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Checks for next for empty.
	 */
	@Test public void hasNextForEmpty() {
		this.iterator = new Distinct(emptyChild);
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Next for not empty.
	 */
	@Test public void nextForNotEmpty() {
		this.iterator = new Distinct(child);
		this.iterator.open();
		Assert.assertEquals(outputType.createTuple("one", 1, "str", 6), this.iterator.next());
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
		Assert.assertEquals(outputType.createTuple("two", 2, "str", 6), this.iterator.next());
	}

	/**
	 * Next for empty.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void nextForEmpty() {
		this.iterator = new Distinct(emptyChild);
		this.iterator.open();
		this.iterator.next();
	}

	/**
	 * Next not empty too many.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void nextNotEmptyTooMany() {
		this.iterator = new Distinct(child);
		this.iterator.open();
		for (int i = 0, l = 4; i < l; i++) {
			this.iterator.next();
		}
	}

	/**
	 * Reset not empty.
	 */
	@Test public void resetNotEmpty() {
		this.iterator = new Distinct(child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 1, "str", 6), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("two", 2, "str", 6), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		when(child.hasNext()).thenReturn(true, true, true, true, true, true, false);
		when(child.next()).thenReturn(
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("one", 2, "str", 6), 
				outputType.createTuple("one", 2, "str", 6), 
				outputType.createTuple("two", 2, "str", 6));
		this.iterator.reset();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 1, "str", 6), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("two", 2, "str", 6), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Reset empty.
	 */
	@Test public void resetEmpty() {
		this.iterator = new Distinct(emptyChild);
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Bind null.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindNull() {
		this.iterator = new Distinct(child);
		this.iterator.open();
		this.iterator.bind(null);
	}
	
	/**
	 * Bind on unopened.
	 */
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		this.iterator = new Distinct(child);
		this.iterator.bind(Tuple.EmptyTuple);
	}
	
	/**
	 * Bind illegal type.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindIllegalType() {
		this.iterator = new Distinct(child);
		this.iterator.open();
		this.iterator.bind(outputType.createTuple("four", 4));
	}
	
	/**
	 * Bind empty tuple.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindEmptyTuple() {
		this.iterator = new Distinct(child);
		this.iterator.open();
		this.iterator.bind(Tuple.EmptyTuple);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.runtime.exec.iterator.TupleIteratorTest#getIterator()
	 */
	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
}
