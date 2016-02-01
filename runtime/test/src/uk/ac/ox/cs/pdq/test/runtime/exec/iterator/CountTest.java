package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Count;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

/**
 * 
 * @author Julien LEBLAY
 */
public class CountTest extends UnaryIteratorTest {

	Count iterator;
	Attribute countColumn = new Attribute(Integer.class, Count.class.getSimpleName());
	TupleType intType = TupleType.DefaultFactory.create(Integer.class);
	@Mock TupleIterator emptyChild;
	
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
		this.iterator = new Count(child);
	}
	
	@Test public void initCount() {
		this.iterator = new Count(child);
		Assert.assertEquals("Count child must match that of initialization", this.child, this.iterator.getChild());
		Assert.assertEquals("Count input columns must match that of initialization", this.inputColumns, this.iterator.getInputColumns());
		Assert.assertEquals("Count input type must match that of initialization", this.inputType, this.iterator.getInputType());
		Assert.assertEquals("Count output columns must match that of initialization", Lists.newArrayList(this.countColumn), this.iterator.getColumns());
		Assert.assertEquals("Count output type must match that of initialization", intType, this.iterator.getType());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initCountNullChild() {
		new Count(null);
	}
	
	@Test public void deepCopy() throws RelationalOperatorException {
		this.iterator = new Count(child);
		Count copy = this.iterator.deepCopy();
		Assert.assertEquals("Count iterators deep copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("Count iterator columns must match that of initialization", this.iterator.getColumns(), copy.getColumns());
		Assert.assertEquals("Count iterator type must match that of child", this.iterator.getType(), copy.getType());
		Assert.assertEquals("Count iterator inputs must match that of child", this.iterator.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("Count iterator input type must match that of child", this.iterator.getInputType(), copy.getInputType());
	}

	@Test public void hasNextForNotEmpty() {
		this.iterator = new Count(child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void hasNextForEmpty() {
		this.iterator = new Count(emptyChild);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void nextForNotEmpty() {
		this.iterator = new Count(child);
		this.iterator.open();
		Assert.assertEquals(intType.createTuple(3), this.iterator.next());
	}

	@Test public void nextForEmpty() {
		this.iterator = new Count(emptyChild);
		this.iterator.open();
		Assert.assertEquals(intType.createTuple(0), this.iterator.next());
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextNoMoreThanOneResultNotEmpty() {
		this.iterator = new Count(child);
		this.iterator.open();
		this.iterator.next();
		this.iterator.next();
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextNoMoreThanOneResultEmpty() {
		this.iterator = new Count(emptyChild);
		this.iterator.open();
		this.iterator.next();
		this.iterator.next();
	}

	@Test public void resetNotEmpty() {
		this.iterator = new Count(child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(intType.createTuple(3), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		when(child.hasNext()).thenReturn(true, true, true, false);
		this.iterator.reset();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(intType.createTuple(3), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void resetEmpty() {
		this.iterator = new Count(emptyChild);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(intType.createTuple(0), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(intType.createTuple(0), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test(expected=IllegalArgumentException.class)
	public void bindNull() {
		this.iterator = new Count(child);
		this.iterator.open();
		this.iterator.bind(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		this.iterator = new Count(child);
		this.iterator.bind(Tuple.EmptyTuple);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bindIllegalType() {
		this.iterator = new Count(child);
		this.iterator.open();
		this.iterator.bind(outputType.createTuple("four", 4));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bindEmptyTuple() {
		this.iterator = new Count(child);
		this.iterator.open();
		this.iterator.bind(Tuple.EmptyTuple);
	}

	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
}
