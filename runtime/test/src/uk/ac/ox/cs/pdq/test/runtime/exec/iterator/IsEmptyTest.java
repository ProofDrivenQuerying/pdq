package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.IsEmpty;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

// TODO: Auto-generated Javadoc
/**
 * The Class IsEmptyTest.
 *
 * @author Julien LEBLAY
 */
public class IsEmptyTest extends UnaryIteratorTest {

	/** The iterator. */
	IsEmpty iterator;
	
	/** The is empty column. */
	Attribute isEmptyColumn = new Attribute(Boolean.class, IsEmpty.class.getSimpleName());
	
	/** The boolean type. */
	TupleType booleanType = TupleType.DefaultFactory.create(Boolean.class);
	
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
		when(child.hasNext()).thenReturn(true, true, true, false);
		when(child.next()).thenReturn(
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("one", 2, "str", 6), 
				outputType.createTuple("two", 2, "str", 6));
		when(emptyChild.hasNext()).thenReturn(false);
		this.iterator = new IsEmpty(child);
	}
	
	/**
	 * Inits the is empty.
	 */
	@Test public void initIsEmpty() {
		this.iterator = new IsEmpty(child);
		Assert.assertEquals("IsEmpty child must match that of initialization", this.child, this.iterator.getChild());
		Assert.assertEquals("IsEmpty input columns must match that of initialization", this.inputColumns, this.iterator.getInputColumns());
		Assert.assertEquals("IsEmpty input type must match that of initialization", this.inputType, this.iterator.getInputType());
		Assert.assertEquals("IsEmpty output columns must match that of initialization", Lists.newArrayList(this.isEmptyColumn), this.iterator.getColumns());
		Assert.assertEquals("IsEmpty output type must match that of initialization", booleanType, this.iterator.getType());
	}
	
	/**
	 * Inits the is empty null child.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initIsEmptyNullChild() {
		new IsEmpty(null);
	}
	
	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		this.iterator = new IsEmpty(child);
		IsEmpty copy = this.iterator.deepCopy();
		Assert.assertEquals("IsEmpty iterators deep copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("IsEmpty iterator columns must match that of initialization", this.iterator.getColumns(), copy.getColumns());
		Assert.assertEquals("IsEmpty iterator type must match that of child", this.iterator.getType(), copy.getType());
		Assert.assertEquals("IsEmpty iterator inputs must match that of child", this.iterator.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("IsEmpty iterator input type must match that of child", this.iterator.getInputType(), copy.getInputType());
		this.iterator.open();
		copy.open();
		Assert.assertEquals("IsEmpty next item must match", this.iterator.next(), copy.next());
	}

	/**
	 * Checks for next for not empty.
	 */
	@Test public void hasNextForNotEmpty() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Checks for next for empty.
	 */
	@Test public void hasNextForEmpty() {
		this.iterator = new IsEmpty(emptyChild);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Next for not empty.
	 */
	@Test public void nextForNotEmpty() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		Assert.assertEquals(booleanType.createTuple(false), this.iterator.next());
	}

	/**
	 * Next for empty.
	 */
	@Test public void nextForEmpty() {
		this.iterator = new IsEmpty(emptyChild);
		this.iterator.open();
		Assert.assertEquals(booleanType.createTuple(true), this.iterator.next());
	}

	/**
	 * Next no more than one result not empty.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void nextNoMoreThanOneResultNotEmpty() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		this.iterator.next();
		this.iterator.next();
	}

	/**
	 * Next no more than one result empty.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void nextNoMoreThanOneResultEmpty() {
		this.iterator = new IsEmpty(emptyChild);
		this.iterator.open();
		this.iterator.next();
		this.iterator.next();
	}

	/**
	 * Reset not empty.
	 */
	@Test public void resetNotEmpty() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(booleanType.createTuple(false), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(booleanType.createTuple(false), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Reset empty.
	 */
	@Test public void resetEmpty() {
		this.iterator = new IsEmpty(emptyChild);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(booleanType.createTuple(true), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(booleanType.createTuple(true), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Bind null.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindNull() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		this.iterator.bind(null);
	}
	
	/**
	 * Bind on unopened.
	 */
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		this.iterator = new IsEmpty(child);
		this.iterator.bind(Tuple.EmptyTuple);
	}
	
	/**
	 * Bind illegal type.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindIllegalType() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		this.iterator.bind(outputType.createTuple("four", 4));
	}
	
	/**
	 * Bind empty tuple.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindEmptyTuple() {
		this.iterator = new IsEmpty(child);
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
