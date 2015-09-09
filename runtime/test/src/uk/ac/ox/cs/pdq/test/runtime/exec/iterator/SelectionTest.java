package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;


/**
 * 
 * @author Julien LEBLAY
 */
public class SelectionTest extends UnaryIteratorTest {

	Selection iterator;
	
	Predicate filter1 = new ConstantEqualityPredicate(0, new TypedConstant<>("one"));
	Predicate filter2 = new ConstantEqualityPredicate(1, new TypedConstant<>(2));
	Predicate filter3 = new ConjunctivePredicate<>(Lists.newArrayList(filter1, filter2));
	Predicate filter4 = new ConstantEqualityPredicate(3, new TypedConstant<>("unrelated"));
	
	@Before public void setup() {
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
				outputType.createTuple("two", 2, "str", 6)
				);
		this.iterator = new Selection(filter3, child);
	}
	
	@Test public void initSelection() {
		this.iterator = new Selection(filter3, child);
		Assert.assertEquals("Selection filter must match that of initialization", this.filter3, this.iterator.getPredicate());
		Assert.assertEquals("Selection child must match that of initialization", this.child, this.iterator.getChild());
		Assert.assertEquals("Selection input columns must match that of initialization", this.inputColumns, this.iterator.getInputColumns());
		Assert.assertEquals("Selection input type must match that of initialization", this.inputType, this.iterator.getInputType());
		Assert.assertEquals("Selection output columns must match that of initialization", this.outputColumns, this.iterator.getColumns());
		Assert.assertEquals("Selection output type must match that of initialization", this.outputType, this.iterator.getType());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initSelectionNullChild() {
		new Selection(filter1, null);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initSelectionNullFilter() {
		new Selection(null, child);
	}

	@Test public void deepCopy() throws RelationalOperatorException {
		this.iterator = new Selection(filter3, child);
		Selection copy = this.iterator.deepCopy();
		Assert.assertEquals("Selection iterators deep copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("Selection iterators deep copy filter must be equals to itself", this.filter3, copy.getPredicate());
		Assert.assertEquals("Selection iterator columns must match that of initialization", this.iterator.getColumns(), copy.getColumns());
		Assert.assertEquals("Selection iterator type must match that of child", this.iterator.getType(), copy.getType());
		Assert.assertEquals("Selection iterator inputs must match that of child", this.iterator.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("Selection iterator input type must match that of child", this.iterator.getInputType(), copy.getInputType());
	}

	@Test public void hasNextFiltered1() {
		this.iterator = new Selection(filter1, child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void hasNextFiltered2() {
		this.iterator = new Selection(filter2, child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void hasNextFiltered3() {
		this.iterator = new Selection(filter3, child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void hasNextFilteredEmptyResult() {
		this.iterator = new Selection(filter4, child);
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void nextFiltered1() {
		this.iterator = new Selection(filter1, child);
		this.iterator.open();
		Assert.assertEquals(outputType.createTuple("one", 1, "str", 6), this.iterator.next());
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
	}

	@Test public void nextFiltered2() {
		this.iterator = new Selection(filter2, child);
		this.iterator.open();
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
		Assert.assertEquals(outputType.createTuple("two", 2, "str", 6), this.iterator.next());
	}

	@Test public void nextFiltered3() {
		this.iterator = new Selection(filter3, child);
		this.iterator.open();
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextFilteredEmptyResult() {
		this.iterator = new Selection(filter4, child);
		this.iterator.open();
		this.iterator.next();
	}

	@Test public void resetFiltered1() {
		this.iterator = new Selection(filter1, child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 1, "str", 6), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();
		when(child.hasNext()).thenReturn(true, true, true, false);
		when(child.next()).thenReturn(
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("one", 2, "str", 6), 
				outputType.createTuple("two", 2, "str", 6)
				);
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 1, "str", 6), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void resetFiltered2() {
		this.iterator = new Selection(filter2, child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("two", 2, "str", 6), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		when(child.hasNext()).thenReturn(true, true, true, false);
		when(child.next()).thenReturn(
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("one", 2, "str", 6), 
				outputType.createTuple("two", 2, "str", 6)
				);
		this.iterator.reset();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("two", 2, "str", 6), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void resetFiltered3() {
		this.iterator = new Selection(filter3, child);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		when(child.hasNext()).thenReturn(true, true, true, false);
		when(child.next()).thenReturn(
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("one", 2, "str", 6), 
				outputType.createTuple("two", 2, "str", 6)
				);
		this.iterator.reset();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple("one", 2, "str", 6), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test(expected=IllegalArgumentException.class)
	public void bindNull() {
		this.iterator = new Selection(filter3, child);
		this.iterator.open();
		this.iterator.bind(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		this.iterator = new Selection(filter3, child);
		this.iterator.bind(Tuple.EmptyTuple);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bindIllegalType() {
		this.iterator = new Selection(filter3, child);
		this.iterator.open();
		this.iterator.bind(outputType.createTuple("four", 4));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bindEmptyTuple() {
		this.iterator = new Selection(filter3, child);
		this.iterator.open();
		this.iterator.bind(Tuple.EmptyTuple);
	}

	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
}
