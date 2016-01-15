package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.MemoryScan;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

/**
 * 
 * @author Julien LEBLAY
 */
public class MemoryScanTest {

	Attribute a, b, c;
	List<Typed> outputColumns;
	TupleType outputType;
	Collection<Tuple> data;
	Predicate filter1;
	Predicate filter2;
	Predicate filter3;
	
	@Before public void setup() {
		Utility.assertsEnabled();
		a = new Attribute(Integer.class, "a");
		b = new Attribute(String.class, "b");
		c = new Attribute(String.class, "c");
		this.outputColumns = Lists.<Typed>newArrayList(a, b, c);
		this.outputType = TupleType.DefaultFactory.create(
				Integer.class,
				String.class,
				String.class);
		this.data = Lists.newArrayList(
				outputType.createTuple(1, "x", "one"), 
				outputType.createTuple(2, "x", "two"), 
				outputType.createTuple(3, "x", "three"));
		this.filter1 = new ConstantEqualityPredicate(0, new TypedConstant<>(2));
		this.filter2 = new ConstantEqualityPredicate(1, new TypedConstant<>("x"));
		this.filter3 = new ConjunctivePredicate<>(Lists.newArrayList(filter1, filter2));

        MockitoAnnotations.initMocks(this);
	}
	
	@Test public void initMemoryScan() {
		MemoryScan scan = new MemoryScan(outputColumns, data);
		Assert.assertNull(scan.getFilter());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initMemoryScanNullData() {
		MemoryScan scan = new MemoryScan(outputColumns, null);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initMemoryScanDataTypeMismatch() {
		TupleType badType = TupleType.DefaultFactory.create(
				Integer.class,
				String.class);
		MemoryScan scan = new MemoryScan(outputColumns, Lists.newArrayList(
				badType.createTuple(1, "x"), 
				badType.createTuple(2, "x"), 
				badType.createTuple(3, "x")));
	}
	
	@Test public void initMemoryScanFiltered1() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter1);
		Assert.assertEquals(filter1, scan.getFilter());
	}
	
	@Test public void initMemoryScanFiltered2() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter2);
		Assert.assertEquals(filter2, scan.getFilter());
	}
	
	@Test public void initMemoryScanFiltered3() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter3);
		Assert.assertEquals(filter3, scan.getFilter());
	}

	@Test public void deepCopy() throws RelationalOperatorException {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter3);
		MemoryScan copy = scan.deepCopy();
		Assert.assertEquals("MemoryScan iterators deep copy relation must be equals to itself", this.filter3, copy.getFilter());
		Assert.assertEquals("MemoryScan iterator columns must match that of initialization", scan.getColumns(), copy.getColumns());
		Assert.assertEquals("MemoryScan iterator type must match that of child", scan.getType(), copy.getType());
		Assert.assertEquals("MemoryScan iterator inputs must match that of child", scan.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("MemoryScan iterator input type must match that of child", scan.getInputType(), copy.getInputType());
		scan.open();
		copy.open();
		Assert.assertEquals("MemoryScan next item must match", scan.next(), copy.next());
	}

	@Test public void hasNext() {
		MemoryScan scan = new MemoryScan(outputColumns, data);
		scan.open();
		Assert.assertTrue(scan.hasNext());
		scan.next();
		Assert.assertTrue(scan.hasNext());
		scan.next();
		Assert.assertTrue(scan.hasNext());
		scan.next();
		Assert.assertFalse(scan.hasNext());
		scan.reset();
	}

	@Test public void hasNextFiltered1() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter1);
		scan.open();
		Assert.assertTrue(scan.hasNext());
		scan.next();
		Assert.assertFalse(scan.hasNext());
		scan.reset();
	}

	@Test public void hasNextFiltered2() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter2);
		scan.open();
		Assert.assertTrue(scan.hasNext());
		scan.next();
		Assert.assertTrue(scan.hasNext());
		scan.next();
		Assert.assertTrue(scan.hasNext());
		scan.next();
		Assert.assertFalse(scan.hasNext());
		scan.reset();
	}

	@Test public void hasNextFiltered3() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter3);
		scan.open();
		Assert.assertTrue(scan.hasNext());
		scan.next();
		Assert.assertFalse(scan.hasNext());
		scan.reset();
	}

	@Test public void next() {
		MemoryScan scan = new MemoryScan(outputColumns, data);
		scan.open();
		Assert.assertEquals(outputType.createTuple(1, "x", "one"), scan.next()); 
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "three"), scan.next());
	}

	@Test public void nextFiltered1() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter1);
		scan.open();
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
	}

	@Test public void nextFiltered2() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter2);
		scan.open();
		Assert.assertEquals(outputType.createTuple(1, "x", "one"), scan.next()); 
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "three"), scan.next());
	}

	@Test public void nextFiltered3() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter3);
		scan.open();
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
	}

	@Test public void reset() {
		MemoryScan scan = new MemoryScan(outputColumns, data);
		scan.open();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(1, "x", "one"), scan.next()); 
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "three"), scan.next());
		Assert.assertFalse(scan.hasNext());
		scan.reset();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(1, "x", "one"), scan.next()); 
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "three"), scan.next());
		Assert.assertFalse(scan.hasNext());
	}

	@Test public void resetFiltered1() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter1);
		scan.open();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertFalse(scan.hasNext());
		scan.reset();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertFalse(scan.hasNext());
	}

	@Test public void resetFiltered2() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter2);
		scan.open();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(1, "x", "one"), scan.next()); 
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "three"), scan.next());
		Assert.assertFalse(scan.hasNext());
		scan.reset();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(1, "x", "one"), scan.next()); 
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "three"), scan.next());
		Assert.assertFalse(scan.hasNext());
	}

	@Test public void resetFiltered3() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter3);
		scan.open();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertFalse(scan.hasNext());
		scan.reset();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertFalse(scan.hasNext());
	}

	@Test(expected=NullPointerException.class)
	public void bindNull() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter3);
		scan.open();
		scan.bind(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter3);
		scan.bind(Tuple.EmptyTuple);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bindIllegalType() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter3);
		scan.open();
		scan.bind(outputType.createTuple("four", 4));
	}
	
	@Test public void bindEmptyTuple() {
		MemoryScan scan = new MemoryScan(outputColumns, data, filter3);
		scan.open();
		scan.bind(Tuple.EmptyTuple);
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertFalse(scan.hasNext());
	}
}