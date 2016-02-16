package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.wrappers.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Scan;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
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
	Attribute a = new Attribute(Integer.class, "a"), 
			b = new Attribute(String.class, "b"), 
			c = new Attribute(String.class, "c");
	
	/** The output columns. */
	List<Attribute> outputColumns = Lists.newArrayList(a, b, c);
	
	/** The output type. */
	TupleType outputType = TupleType.DefaultFactory.create(
			Integer.class,
			String.class,
			String.class);
	
	/** The relation. */
	InMemoryTableWrapper relation = new InMemoryTableWrapper("test", outputColumns);
	
	/** The mt. */
	AccessMethod mt = new AccessMethod("mt", Types.LIMITED, Lists.newArrayList(2, 1));
	
	/** The filter1. */
	Predicate filter1 = new ConstantEqualityPredicate(0, new TypedConstant<>(2));
	
	/** The filter2. */
	Predicate filter2 = new ConstantEqualityPredicate(1, new TypedConstant<>("x"));
	
	/** The filter3. */
	Predicate filter3 = new ConjunctivePredicate<>(Lists.newArrayList(filter1, filter2));
	
	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
		this.relation.load(Lists.newArrayList(
				outputType.createTuple(1, "x", "one"), 
				outputType.createTuple(2, "x", "two"), 
				outputType.createTuple(3, "x", "three")));
		this.relation.addAccessMethod(mt);

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
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		Scan scan = new Scan(relation, filter3);
		Scan copy = scan.deepCopy();
		Assert.assertEquals("Scan iterators deep copy relation must be equals to itself", this.relation, copy.getRelation());
		Assert.assertEquals("Scan iterators deep copy relation must be equals to itself", this.filter3, copy.getFilter());
		Assert.assertEquals("Scan iterator columns must match that of initialization", scan.getColumns(), copy.getColumns());
		Assert.assertEquals("Scan iterator type must match that of child", scan.getType(), copy.getType());
		Assert.assertEquals("Scan iterator inputs must match that of child", scan.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("Scan iterator input type must match that of child", scan.getInputType(), copy.getInputType());
		scan.open();
		copy.open();
		Assert.assertEquals("Scan next item must match", scan.next(), copy.next());
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