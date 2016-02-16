package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.wrappers.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TopDownAccess;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class TopDownAccessTest.
 *
 * @author Julien LEBLAY
 */
public class TopDownAccessTest {

	/** The d. */
	Attribute a = new Attribute(Integer.class, "a"), 
			b = new Attribute(String.class, "b"), 
			c = new Attribute(String.class, "c"), 
			d = new Attribute(Integer.class, "d");
	
	/** The output columns. */
	List<Typed> outputColumns;
	
	/** The input columns. */
	List<Typed> inputColumns;
	
	/** The output type. */
	TupleType outputType;
	
	/** The input type. */
	TupleType inputType;
	
	/** The iterator. */
	TopDownAccess iterator;
	
	/** The relation. */
	InMemoryTableWrapper relation;
	
	/** The unrelated. */
	AccessMethod mt1, mt2, mt3, free, unrelated;
	
	/** The binding1. */
	Tuple binding1;
	
	/** The binding2. */
	Tuple binding2;
	
	/** The binding3. */
	Tuple binding3;
	
	/** The binding4. */
	Tuple binding4;
	
	/** The binding5. */
	Tuple binding5;
	
	/** The static1. */
	Map<Integer, TypedConstant<?>> static1 = new LinkedHashMap<>();
	
	/** The static2. */
	Map<Integer, TypedConstant<?>> static2 = new LinkedHashMap<>();
	
	/** The static3. */
	Map<Integer, TypedConstant<?>> static3 = new LinkedHashMap<>();
	
	/** The static4. */
	Map<Integer, TypedConstant<?>> static4 = new LinkedHashMap<>();
	
	/** The static5. */
	Map<Integer, TypedConstant<?>> static5 = new LinkedHashMap<>();
	
	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
		this.outputColumns = Lists.<Typed>newArrayList(a, b, c, d);
		this.outputType = TupleType.DefaultFactory.create(Integer.class, String.class, String.class, Integer.class);
		this.inputColumns = Lists.<Typed>newArrayList(b, a);
		this.inputType = TupleType.DefaultFactory.create(Integer.class, String.class);

		this.relation = new InMemoryTableWrapper("test", Lists.newArrayList(a, b, c, d));
		
		this.free = new AccessMethod("mt1", Types.FREE, Lists.<Integer>newArrayList());
		this.mt1 = new AccessMethod("mt2", Types.LIMITED, Lists.newArrayList(1));
		this.mt2 = new AccessMethod("mt3", Types.LIMITED, Lists.newArrayList(2, 1));
		this.mt3 = new AccessMethod("mt4", Types.BOOLEAN, Lists.newArrayList(2, 3, 1, 4));
		this.unrelated = new AccessMethod("unrelated", Types.LIMITED, Lists.newArrayList(3));
		
		this.relation.load(Lists.newArrayList(
				outputType.createTuple(1, "x", "one", 2), 
				outputType.createTuple(2, "x", "two", 4), 
				outputType.createTuple(3, "x", "one", 8),
				outputType.createTuple(3, "x", "four", 32),
				outputType.createTuple(3, "y", "one", 256)));
		this.relation.addAccessMethod(mt1);
		this.relation.addAccessMethod(mt2);
		this.relation.addAccessMethod(mt3);
		this.relation.addAccessMethod(free);
		
		this.binding1 = TupleType.DefaultFactory.create(Integer.class).createTuple(3);
		this.binding2 = TupleType.DefaultFactory.create(String.class, Integer.class).createTuple("x", 3);
		this.binding3 = TupleType.DefaultFactory.create(String.class, String.class, Integer.class, Integer.class).createTuple("y", "one", 3, 256);
		this.binding4 = TupleType.DefaultFactory.create(Integer.class).createTuple(2);
		this.binding5 = TupleType.DefaultFactory.create(Integer.class).createTuple(5);
		
		this.static1.put(0, new TypedConstant<>(3));
		this.static2.put(1, new TypedConstant<>("x"));
		this.static2.put(0, new TypedConstant<>(3));
		this.static3.put(1, new TypedConstant<>("x"));
		this.static3.put(3, new TypedConstant<>("one"));
		this.static3.put(0, new TypedConstant<>(3));
		this.static3.put(3, new TypedConstant<>(256));
		this.static4.put(0, new TypedConstant<>("x"));
		this.static5.put(1, new TypedConstant<>("x"));

        MockitoAnnotations.initMocks(this);
	}
	
	/**
	 * Inits the with inconsistent access method.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initWithInconsistentAccessMethod() {
		new TopDownAccess(relation, unrelated);
	}
	
	/**
	 * Inits the access free access.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initAccessFreeAccess() {
		this.iterator = new TopDownAccess(relation, free);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(free, this.iterator.getAccessMethod());
		Assert.assertEquals(new HashMap<>(), this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access limited access1.
	 */
	@Test public void initAccessLimitedAccess1() {
		this.iterator = new TopDownAccess(relation, mt1);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt1, this.iterator.getAccessMethod());
		Assert.assertEquals(new HashMap<>(), this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access limited access2.
	 */
	@Test public void initAccessLimitedAccess2() {
		this.iterator = new TopDownAccess(relation, mt2);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt2, this.iterator.getAccessMethod());
		Assert.assertEquals(new HashMap<>(), this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access boolean access.
	 */
	@Test public void initAccessBooleanAccess() {
		this.iterator = new TopDownAccess(relation, mt3);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt3, this.iterator.getAccessMethod());
		Assert.assertEquals(new HashMap<>(), this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access limited access1 with static input.
	 */
	@Test public void initAccessLimitedAccess1WithStaticInput() {
		this.iterator = new TopDownAccess(relation, mt1, static1);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt1, this.iterator.getAccessMethod());
		Assert.assertEquals(static1, this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access limited access1 with inconsistent type static input.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initAccessLimitedAccess1WithInconsistentTypeStaticInput() {
		this.iterator = new TopDownAccess(relation, mt1, static4);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt1, this.iterator.getAccessMethod());
		Assert.assertEquals(static4, this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access limited access1 with inconsistent entry static input.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initAccessLimitedAccess1WithInconsistentEntryStaticInput() {
		this.iterator = new TopDownAccess(relation, mt1, static5);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt1, this.iterator.getAccessMethod());
		Assert.assertEquals(static5, this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access limited access2 with partial static input.
	 */
	@Test public void initAccessLimitedAccess2WithPartialStaticInput() {
		this.iterator = new TopDownAccess(relation, mt2, static1);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt2, this.iterator.getAccessMethod());
		Assert.assertEquals(static1, this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access limited access2 with full static input.
	 */
	@Test public void initAccessLimitedAccess2WithFullStaticInput() {
		this.iterator = new TopDownAccess(relation, mt2, static2);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt2, this.iterator.getAccessMethod());
		Assert.assertEquals(static2, this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access boolean access with partial static input.
	 */
	@Test public void initAccessBooleanAccessWithPartialStaticInput() {
		this.iterator = new TopDownAccess(relation, mt3, static1);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt3, this.iterator.getAccessMethod());
		Assert.assertEquals(static1, this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access boolean access with full static input.
	 */
	@Test public void initAccessBooleanAccessWithFullStaticInput() {
		this.iterator = new TopDownAccess(relation, mt3, static3);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt3, this.iterator.getAccessMethod());
		Assert.assertEquals(static3, this.iterator.getStaticInputs());
	}
	
	/**
	 * Inits the access null relation.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initAccessNullRelation() {
		new TopDownAccess(null, mt1);
	}

	/**
	 * Inits the access null access method.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initAccessNullAccessMethod() {
		new TopDownAccess(relation, null);
	}

	/**
	 * Inits the access null child.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initAccessNullChild() {
		new TopDownAccess(relation, mt1, null);
	}

	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		this.iterator = new TopDownAccess(relation, mt2, static1);
		TopDownAccess copy = this.iterator.deepCopy();
		Assert.assertEquals("TopDownAccess iterators deep copy relation must be equal to itself", this.relation, copy.getRelation());
		Assert.assertEquals("TopDownAccess iterators deep copy access method must be equal to itself", this.mt2, copy.getAccessMethod());
		Assert.assertEquals("TopDownAccess iterators deep copy static inputs must be equal to itself", this.static1, copy.getStaticInputs());
		Assert.assertEquals("TopDownAccess iterator columns must match that of initialization", this.iterator.getColumns(), copy.getColumns());
		Assert.assertEquals("TopDownAccess iterator type must match that of child", this.iterator.getType(), copy.getType());
		Assert.assertEquals("TopDownAccess iterator inputs must match that of child", this.iterator.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("TopDownAccess iterator input type must match that of child", this.iterator.getInputType(), copy.getInputType());
	}
	
	/**
	 * Bind inconsistent empty input.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void bindInconsistentEmptyInput() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.open();
		this.iterator.bind(Tuple.EmptyTuple);
	}		
	
	/**
	 * Inits the inconsistent input type.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initInconsistentInputType() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.open();
		this.iterator.bind(TupleType.DefaultFactory.create(String.class).createTuple("str"));
	}		
	
	/**
	 * Bind inconsistent input arity.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void bindInconsistentInputArity() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.open();
		this.iterator.bind(TupleType.DefaultFactory.create(String.class, Integer.class).createTuple("str", 1));
	}		

	/**
	 * Checks for next limited1.
	 */
	@Test public void hasNextLimited1() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.open();
		this.iterator.bind(binding1);
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());

		this.iterator.bind(binding1);
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());

		this.iterator.bind(TupleType.DefaultFactory.create(Integer.class).createTuple(2));
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Checks for next limited1 with static input.
	 */
	@Test public void hasNextLimited1WithStaticInput() {
		this.iterator = new TopDownAccess(relation, mt1, static1);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Next limited1 with static input.
	 */
	@Test public void nextLimited1WithStaticInput() {
		this.iterator = new TopDownAccess(relation, mt1, static1);
		this.iterator.open();
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
	}

	/**
	 * Reset limited1 with static input.
	 */
	@Test public void resetLimited1WithStaticInput() {
		this.iterator = new TopDownAccess(relation, mt1, static1);
		this.iterator.open();
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
		this.iterator.reset();
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
	}

	/**
	 * Checks for next limited2.
	 */
	@Test public void hasNextLimited2() {
		this.iterator = new TopDownAccess(relation, mt2);
		this.iterator.open();
		this.iterator.bind(binding2);
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); 

		this.iterator.bind(binding2);
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); 
	}

	/**
	 * Checks for next limited2 with partial static input.
	 */
	@Test public void hasNextLimited2WithPartialStaticInput() {
		this.iterator = new TopDownAccess(relation, mt2, static5);
		this.iterator.open();
		this.iterator.bind(binding1); // (x, 3) => 1 tuple
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); 

		this.iterator.bind(binding4); // (x, 2) => 1 tuple
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); 

		this.iterator.bind(binding5); // (x, 5) => 0 tuple
		Assert.assertFalse(this.iterator.hasNext()); 
	}

	/**
	 * Next limited2 with partial static input.
	 */
	@Test public void nextLimited2WithPartialStaticInput() {
		this.iterator = new TopDownAccess(relation, mt2, static5);
		this.iterator.open();
		this.iterator.bind(binding1); // (x, 3) => 1 tuple
		Assert.assertTrue(this.iterator.hasNext()); 
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext()); 
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext()); 

		this.iterator.bind(binding4); // (x, 2) => 1 tuple
		Assert.assertTrue(this.iterator.hasNext()); 
		Assert.assertEquals(outputType.createTuple(2, "x", "two", 4), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext()); 

		this.iterator.bind(binding5); // (x, 5) => 0 tuple
		Assert.assertFalse(this.iterator.hasNext()); 
	}

	/**
	 * Reset limited2 with partial static input.
	 */
	@Test public void resetLimited2WithPartialStaticInput() {
		this.iterator = new TopDownAccess(relation, mt2, static5);
		this.iterator.open();
		this.iterator.bind(binding1); // (x, 3) => 2 tuples
		Assert.assertTrue(this.iterator.hasNext()); 
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext()); 
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext()); 
		this.iterator.reset();  // (x, 3) => 2 tuples
		Assert.assertTrue(this.iterator.hasNext()); 
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext()); 
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext()); 

		this.iterator.bind(binding4); // (x, 2) => 1 tuple
		Assert.assertTrue(this.iterator.hasNext()); 
		Assert.assertEquals(outputType.createTuple(2, "x", "two", 4), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext()); 
		this.iterator.reset();  // (x, 2) => 1 tuple
		Assert.assertTrue(this.iterator.hasNext()); 
		Assert.assertEquals(outputType.createTuple(2, "x", "two", 4), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext()); 

		this.iterator.bind(binding5); // (x, 5) => 0 tuple
		Assert.assertFalse(this.iterator.hasNext());
		
	}

	/**
	 * Checks for next boolean.
	 */
	@Test public void hasNextBoolean() {
		this.iterator = new TopDownAccess(relation, mt3);
		this.iterator.open();
		this.iterator.bind(binding3);
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); 

		this.iterator.bind(binding3);
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); 
	}

	/**
	 * Next limited1.
	 */
	@Test public void nextLimited1() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.open();
		this.iterator.bind(binding1);
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());

		this.iterator.bind(binding1);
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
	}

	/**
	 * Next limited2.
	 */
	@Test public void nextLimited2() {
		this.iterator = new TopDownAccess(relation, mt2);
		this.iterator.open();
		this.iterator.bind(binding2);
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());

		this.iterator.bind(binding2);
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
	}

	/**
	 * Next boolean.
	 */
	@Test public void nextBoolean() {
		this.iterator = new TopDownAccess(relation, mt3);
		this.iterator.open();
		this.iterator.bind(binding3);
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());

		this.iterator.bind(binding3);
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
	}

	/**
	 * Next boolean limit reached.
	 */
	@Test (expected=NoSuchElementException.class)
	public void nextBooleanLimitReached() {
		this.iterator = new TopDownAccess(relation, mt3);
		this.iterator.open();
		this.iterator.bind(binding3);
		this.iterator.next();
		this.iterator.next();
	}

	/**
	 * Reset limited1.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void resetLimited1() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.open();
		this.iterator.bind(binding1);
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();

		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.next();
	}

	/**
	 * Reset limited2.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void resetLimited2() {
		this.iterator = new TopDownAccess(relation, mt2);
		this.iterator.open();
		this.iterator.bind(binding2);
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();

		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.next();
	}

	/**
	 * Reset boolean.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void resetBoolean() {
		this.iterator = new TopDownAccess(relation, mt3);
		this.iterator.open();
		this.iterator.bind(binding3);
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();

		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.next();
	}
	
	/**
	 * Iterate over unbound access.
	 */
	@Test(expected=IllegalStateException.class) 
	public void iterateOverUnboundAccess() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.open();
		this.iterator.next();
	}		

	/**
	 * Bind null.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindNull() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.open();
		this.iterator.bind(null);
	}
	
	/**
	 * Bind on unopened.
	 */
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.bind(Tuple.EmptyTuple);
	}
	
	/**
	 * Bind on interrupted.
	 */
	@Test(expected=IllegalStateException.class)
	public void bindOnInterrupted() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.bind(Tuple.EmptyTuple);
	}
	
	/**
	 * Bind inconsistent type.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindInconsistentType() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.open();
		this.iterator.bind(outputType.createTuple("four", 4));
	}
	
	/**
	 * Bind empty tuple.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void bindEmptyTuple() {
		this.iterator = new TopDownAccess(relation, mt1);
		this.iterator.open();
		this.iterator.bind(Tuple.EmptyTuple);
	}

	/**
	 * Bind with full static input1.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void bindWithFullStaticInput1() {
		this.iterator = new TopDownAccess(relation, mt1, static1);
		this.iterator.open();
		this.iterator.bind(binding1);
	}

	/**
	 * Bind with full static input2.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void bindWithFullStaticInput2() {
		this.iterator = new TopDownAccess(relation, mt1, static2);
		this.iterator.open();
		this.iterator.bind(binding2);
	}

	/**
	 * Bind with full static input3.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void bindWithFullStaticInput3() {
		this.iterator = new TopDownAccess(relation, mt1, static3);
		this.iterator.open();
		this.iterator.bind(binding3);
	}
}