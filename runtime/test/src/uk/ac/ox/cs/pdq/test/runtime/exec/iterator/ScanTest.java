package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Scan;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class ScanTest.
 *
 * @author Julien LEBLAY
 */
public class ScanTest {

	/** The c. */
	Attribute a = Attribute.create(Integer.class, "a"), 
			b = Attribute.create(String.class, "b"), 
			c = Attribute.create(String.class, "c");
	
	/** The output columns. */
	Attribute[] outputColumns = new Attribute[]{a, b, c};
	
	/** The output type. */
	TupleType outputType = TupleType.DefaultFactory.create(
			Integer.class,
			String.class,
			String.class);
	
	/** The mt. */
	AccessMethod mt = AccessMethod.create("mt", new Integer[]{2, 1});
	
	/** The relation. */
	InMemoryTableWrapper relation = new InMemoryTableWrapper("test", outputColumns, new AccessMethod[]{mt});
	

	
	/** The filter1. */
	SimpleCondition filter1 = ConstantEqualityCondition.create(0, TypedConstant.create(2));
	
	/** The filter2. */
	SimpleCondition filter2 = ConstantEqualityCondition.create(1, TypedConstant.create("x"));
	
	/** The filter3. */
	Condition filter3 = ConjunctiveCondition.create(new SimpleCondition[]{filter1, filter2});
	
	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
		this.relation.load(Lists.newArrayList(
				outputType.createTuple(1, "x", "one"), 
				outputType.createTuple(2, "x", "two"), 
				outputType.createTuple(3, "x", "three")));

        MockitoAnnotations.initMocks(this);
	}
	
	/**
	 * Inits the scan.
	 */
	@Test public void initScan() {
		Scan scan = new Scan(relation);
		Assert.assertEquals(relation, scan.getRelation());
		Assert.assertNull(scan.getFilter());
	}
	
	/**
	 * Inits the scan filtered1.
	 */
	@Test public void initScanFiltered1() {
		Scan scan = new Scan(relation, filter1);
		Assert.assertEquals(relation, scan.getRelation());
		Assert.assertEquals(filter1, scan.getFilter());
	}
	
	/**
	 * Inits the scan filtered2.
	 */
	@Test public void initScanFiltered2() {
		Scan scan = new Scan(relation, filter2);
		Assert.assertEquals(relation, scan.getRelation());
		Assert.assertEquals(filter2, scan.getFilter());
	}
	
	/**
	 * Inits the scan filtered3.
	 */
	@Test public void initScanFiltered3() {
		Scan scan = new Scan(relation, filter3);
		Assert.assertEquals(relation, scan.getRelation());
		Assert.assertEquals(filter3, scan.getFilter());
	}

	/**
	 * Checks for next.
	 */
	@Test public void hasNext() {
		Scan scan = new Scan(relation);
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

	/**
	 * Checks for next filtered1.
	 */
	@Test public void hasNextFiltered1() {
		Scan scan = new Scan(relation, filter1);
		scan.open();
		Assert.assertTrue(scan.hasNext());
		scan.next();
		Assert.assertFalse(scan.hasNext());
		scan.reset();
	}

	/**
	 * Checks for next filtered2.
	 */
	@Test public void hasNextFiltered2() {
		Scan scan = new Scan(relation, filter2);
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

	/**
	 * Checks for next filtered3.
	 */
	@Test public void hasNextFiltered3() {
		Scan scan = new Scan(relation, filter3);
		scan.open();
		Assert.assertTrue(scan.hasNext());
		scan.next();
		Assert.assertFalse(scan.hasNext());
		scan.reset();
	}

	/**
	 * Next.
	 */
	@Test public void next() {
		Scan scan = new Scan(relation);
		scan.open();
		Assert.assertEquals(outputType.createTuple(1, "x", "one"), scan.next()); 
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "three"), scan.next());
	}

	/**
	 * Next filtered1.
	 */
	@Test public void nextFiltered1() {
		Scan scan = new Scan(relation, filter1);
		scan.open();
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
	}

	/**
	 * Next filtered2.
	 */
	@Test public void nextFiltered2() {
		Scan scan = new Scan(relation, filter2);
		scan.open();
		Assert.assertEquals(outputType.createTuple(1, "x", "one"), scan.next()); 
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "three"), scan.next());
	}

	/**
	 * Next filtered3.
	 */
	@Test public void nextFiltered3() {
		Scan scan = new Scan(relation, filter3);
		scan.open();
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
	}

	/**
	 * Reset.
	 */
	@Test public void reset() {
		Scan scan = new Scan(relation);
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

	/**
	 * Reset filtered1.
	 */
	@Test public void resetFiltered1() {
		Scan scan = new Scan(relation, filter1);
		scan.open();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertFalse(scan.hasNext());
		scan.reset();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertFalse(scan.hasNext());
	}

	/**
	 * Reset filtered2.
	 */
	@Test public void resetFiltered2() {
		Scan scan = new Scan(relation, filter2);
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

	/**
	 * Reset filtered3.
	 */
	@Test public void resetFiltered3() {
		Scan scan = new Scan(relation, filter3);
		scan.open();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertFalse(scan.hasNext());
		scan.reset();
		
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertFalse(scan.hasNext());
	}

	/**
	 * Bind null.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindNull() {
		Scan scan = new Scan(relation, filter3);
		scan.open();
		scan.bind(null);
	}
	
	/**
	 * Bind on unopened.
	 */
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		Scan scan = new Scan(relation, filter3);
		scan.bind(Tuple.EmptyTuple);
	}
	
	/**
	 * Bind illegal type.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindIllegalType() {
		Scan scan = new Scan(relation, filter3);
		scan.open();
		scan.bind(outputType.createTuple("four", 4));
	}
	
	/**
	 * Bind empty tuple.
	 */
	@Test public void bindEmptyTuple() {
		Scan scan = new Scan(relation, filter3);
		scan.open();
		scan.bind(Tuple.EmptyTuple);
		Assert.assertTrue(scan.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two"), scan.next());
		Assert.assertFalse(scan.hasNext());
	}
}