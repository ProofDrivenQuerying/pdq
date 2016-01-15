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
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.IsEmpty;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 * 
 * @author Julien LEBLAY
 */
public class IsEmptyTest extends UnaryIteratorTest {

	IsEmpty iterator;
	Attribute isEmptyColumn = new Attribute(Boolean.class, IsEmpty.class.getSimpleName());
	TupleType booleanType = TupleType.DefaultFactory.create(Boolean.class);
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
		this.iterator = new IsEmpty(child);
	}
	
	@Test public void initIsEmpty() {
		this.iterator = new IsEmpty(child);
		Assert.assertEquals("IsEmpty child must match that of initialization", this.child, this.iterator.getChild());
		Assert.assertEquals("IsEmpty input columns must match that of initialization", this.inputColumns, this.iterator.getInputColumns());
		Assert.assertEquals("IsEmpty input type must match that of initialization", this.inputType, this.iterator.getInputType());
		Assert.assertEquals("IsEmpty output columns must match that of initialization", Lists.newArrayList(this.isEmptyColumn), this.iterator.getColumns());
		Assert.assertEquals("IsEmpty output type must match that of initialization", booleanType, this.iterator.getType());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initIsEmptyNullChild() {
		new IsEmpty(null);
	}
	
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

	@Test public void hasNextForNotEmpty() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void hasNextForEmpty() {
		this.iterator = new IsEmpty(emptyChild);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void nextForNotEmpty() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		Assert.assertEquals(booleanType.createTuple(false), this.iterator.next());
	}

	@Test public void nextForEmpty() {
		this.iterator = new IsEmpty(emptyChild);
		this.iterator.open();
		Assert.assertEquals(booleanType.createTuple(true), this.iterator.next());
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextNoMoreThanOneResultNotEmpty() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		this.iterator.next();
		this.iterator.next();
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextNoMoreThanOneResultEmpty() {
		this.iterator = new IsEmpty(emptyChild);
		this.iterator.open();
		this.iterator.next();
		this.iterator.next();
	}

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

	@Test(expected=IllegalArgumentException.class)
	public void bindNull() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		this.iterator.bind(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		this.iterator = new IsEmpty(child);
		this.iterator.bind(Tuple.EmptyTuple);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bindIllegalType() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		this.iterator.bind(outputType.createTuple("four", 4));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bindEmptyTuple() {
		this.iterator = new IsEmpty(child);
		this.iterator.open();
		this.iterator.bind(Tuple.EmptyTuple);
	}

	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
}
