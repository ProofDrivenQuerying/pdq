package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.wrappers.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.BottomUpAccess;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class BottomUpAccessTest.
 *
 * @author Julien LEBLAY
 */
public class BottomUpAccessTest extends UnaryIteratorTest {

	/** The open iterator. */
	BottomUpAccess iterator, openIterator;
	
	/** The relation. */
	InMemoryTableWrapper relation;
	
	/** The unrelated. */
	AccessMethod mt1, mt2, mt3, free, unrelated;
	
	/** The child5. */
	@Mock TupleIterator child1, child2, child3, child4, child5;
	
	/** The child5 input type. */
	TupleType child1Type, child2Type, child3Type, child5Type, child5InputType;
	
	/** The d. */
	Attribute a, b, c, d;
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.runtime.exec.iterator.TupleIteratorTest#setup()
	 */
	@Before public void setup() {
		super.setup();
		a = new Attribute(Integer.class, "a");
		b = new Attribute(String.class, "b");
		c = new Attribute(String.class, "c");
		d = new Attribute(Integer.class, "d");
		this.outputColumns = Lists.<Typed>newArrayList(a, b, c, d);
		this.outputType = TupleType.DefaultFactory.create(Integer.class, String.class, String.class, Integer.class);
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
		this.relation.addAccessMethod(free);
		this.relation.addAccessMethod(mt1);
		this.relation.addAccessMethod(mt2);
		this.relation.addAccessMethod(mt3);

        MockitoAnnotations.initMocks(this);
		child1Type = TupleType.DefaultFactory.create(Integer.class);
		when(child1.getColumns()).thenReturn(Lists.<Typed>newArrayList(a));
		when(child1.getType()).thenReturn(child1Type);
		when(child1.deepCopy()).thenReturn(child1);
		when(child1.hasNext()).thenReturn(
				true, true, true, true, true, false,
				true, true, true, true, true, false);
		when(child1.next()).thenReturn(
				// First round
				child1Type.createTuple(3), 
				child1Type.createTuple(2), 
				child1Type.createTuple(-1), 
				child1Type.createTuple(1), 
				child1Type.createTuple(2),
				// Second round
				child1Type.createTuple(3), 
				child1Type.createTuple(2), 
				child1Type.createTuple(-1), 
				child1Type.createTuple(1), 
				child1Type.createTuple(2));
		when(child2.getColumns()).thenReturn(Lists.<Typed>newArrayList(b, a));
		child2Type = TupleType.DefaultFactory.create(String.class, Integer.class);
		when(child2.getType()).thenReturn(child2Type);
		when(child2.hasNext()).thenReturn(
				true, true, true, true, true, true, true, false,
				true, true, true, true, true, true, true, false);
		when(child2.next()).thenReturn(
				// First round
				child2Type.createTuple("x", 3), 
				child2Type.createTuple("x", 2), 
				child2Type.createTuple("x", 1), 
				child2Type.createTuple("y", 3), 
				child2Type.createTuple("y", 2), 
				child2Type.createTuple("y", 1),
				child2Type.createTuple("x", 2),
				// Second round
				child2Type.createTuple("x", 3), 
				child2Type.createTuple("x", 2), 
				child2Type.createTuple("x", 1), 
				child2Type.createTuple("y", 3), 
				child2Type.createTuple("y", 2), 
				child2Type.createTuple("y", 1),
				child2Type.createTuple("x", 2));
		// Child 3
		when(child3.getColumns()).thenReturn(Lists.<Typed>newArrayList(b, c, a, d));
		child3Type = TupleType.DefaultFactory.create(
				String.class, String.class, Integer.class, Integer.class);
		when(child3.getType()).thenReturn(child3Type);
		when(child3.hasNext()).thenReturn(
				true, true, true, true, false,
				true, true, true, true, false);
		when(child3.next()).thenReturn(
				// First round
				child3Type.createTuple("x", "one", 1, 2), 
				child3Type.createTuple("x", "two", 1, 2), 
				child3Type.createTuple("x", "one", 1, 2), 
				child3Type.createTuple("y", "one", 3, 256),
				// Second round
				child3Type.createTuple("x", "one", 1, 2), 
				child3Type.createTuple("x", "two", 1, 2), 
				child3Type.createTuple("x", "one", 1, 2), 
				child3Type.createTuple("y", "one", 3, 256));
		// Child 4
		when(child4.getColumns()).thenReturn(Collections.EMPTY_LIST);
		when(child4.getType()).thenReturn(TupleType.EmptyTupleType);
		when(child4.next()).thenReturn(Tuple.EmptyTuple);
		// Child 5
		when(child5.getColumns()).thenReturn(Lists.<Typed>newArrayList(b, c, a, d));
		child5Type = TupleType.DefaultFactory.create(
				String.class, String.class, Integer.class, Integer.class);
		child5InputType = TupleType.DefaultFactory.create(
				Integer.class, String.class);
		when(child5.getType()).thenReturn(child5Type);
		when(child5.getInputType()).thenReturn(child5InputType);
		when(child5.hasNext()).thenReturn(
				true, true, true, true, false,
				true, true, true, true, false);
		when(child5.next()).thenReturn(
				// First round
				child5Type.createTuple("x", "one", 1, 2), 
				child5Type.createTuple("x", "two", 1, 2), 
				child5Type.createTuple("x", "one", 1, 2), 
				child5Type.createTuple("y", "one", 3, 256),
				// Second round
				child5Type.createTuple("x", "one", 1, 2), 
				child5Type.createTuple("x", "two", 1, 2), 
				child5Type.createTuple("x", "one", 1, 2), 
				child5Type.createTuple("y", "one", 3, 256));

		this.iterator = new BottomUpAccess(relation, mt1, child1);
	}
	
	/**
	 * Inits the with inconsistent access method.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initWithInconsistentAccessMethod() {
		new BottomUpAccess(relation, unrelated, child);
	}
	
	/**
	 * Inits the access free access.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initAccessFreeAccess() {
		this.iterator = new BottomUpAccess(relation, free, child4);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(free, this.iterator.getAccessMethod());
		Assert.assertEquals(child, this.iterator.getChild());
	}
	
	/**
	 * Inits the access limited access1.
	 */
	@Test public void initAccessLimitedAccess1() {
		this.iterator = new BottomUpAccess(relation, mt1, child1);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt1, this.iterator.getAccessMethod());
		Assert.assertEquals(child1, this.iterator.getChild());
	}
	
	/**
	 * Inits the access limited access2.
	 */
	@Test public void initAccessLimitedAccess2() {
		this.iterator = new BottomUpAccess(relation, mt2, child2);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt2, this.iterator.getAccessMethod());
		Assert.assertEquals(child2, this.iterator.getChild());
	}
	
	/**
	 * Inits the access boolean access.
	 */
	@Test public void initAccessBooleanAccess() {
		this.iterator = new BottomUpAccess(relation, mt3, child3);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt3, this.iterator.getAccessMethod());
		Assert.assertEquals(child3, this.iterator.getChild());
	}
	
	/**
	 * Inits the access open.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void initAccessOpen() {
		this.iterator = new BottomUpAccess(relation, mt1, child5);
		Assert.assertEquals(relation, this.iterator.getRelation());
		Assert.assertEquals(mt1, this.iterator.getAccessMethod());
		Assert.assertEquals(child5, this.iterator.getChild());
		Assert.assertEquals(child5InputType, this.iterator.getChild().getInputType());
	}
	
	/**
	 * Inits the access null relation.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initAccessNullRelation() {
		new BottomUpAccess(null, mt1, child1);
	}

	/**
	 * Inits the access null access method.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initAccessNullAccessMethod() {
		new BottomUpAccess(relation, null, child1);
	}

	/**
	 * Inits the access null child.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initAccessNullChild() {
		new BottomUpAccess(relation, mt1, null);
	}

	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		this.iterator = new BottomUpAccess(relation, mt1, child1);
		BottomUpAccess copy = this.iterator.deepCopy();
		Assert.assertEquals("BottomUpAccess iterators deep copy relation must be equals to itself", this.relation, copy.getRelation());
		Assert.assertEquals("BottomUpAccess iterators deep copy access method must be equals to itself", this.mt1, copy.getAccessMethod());
		Assert.assertEquals("BottomUpAccess iterators deep copy child must be equals to itself", this.child1, copy.getChild());
		Assert.assertEquals("BottomUpAccess iterator columns must match that of initialization", this.iterator.getColumns(), copy.getColumns());
		Assert.assertEquals("BottomUpAccess iterator type must match that of child", this.iterator.getType(), copy.getType());
		Assert.assertEquals("BottomUpAccess iterator inputs must match that of child", this.iterator.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("BottomUpAccess iterator input type must match that of child", this.iterator.getInputType(), copy.getInputType());
	}
	
	/**
	 * Inits the inconsistent empty input.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initInconsistentEmptyInput() {
		when(child.getType()).thenReturn(TupleType.EmptyTupleType);
		when(child.next()).thenReturn(Tuple.EmptyTuple);
		new BottomUpAccess(relation, mt1, child);
	}		
	
	/**
	 * Inits the inconsistent input type.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initInconsistentInputType() {
		when(child.getType()).thenReturn(TupleType.DefaultFactory.create(String.class));
		when(child.next()).thenReturn(TupleType.DefaultFactory.create(String.class).createTuple("str"));
		new BottomUpAccess(relation, mt1, child);
	}		
	
	/**
	 * Inits the inconsistent input arity.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initInconsistentInputArity() {
		when(child.getType()).thenReturn(TupleType.DefaultFactory.create(Integer.class, String.class));
		when(child.next()).thenReturn(TupleType.DefaultFactory.create(Integer.class, String.class).createTuple(1, "str"));
		new BottomUpAccess(relation, mt1, child);
	}		

	/**
	 * Checks for next limited1.
	 */
	@Test public void hasNextLimited1() {
		this.iterator = new BottomUpAccess(relation, mt1, child1);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Checks for next limited2.
	 */
	@Test public void hasNextLimited2() {
		this.iterator = new BottomUpAccess(relation, mt2, child2);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); 
	}

	/**
	 * Checks for next boolean.
	 */
	@Test public void hasNextBoolean() {
		this.iterator = new BottomUpAccess(relation, mt3, child3);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext()); this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); 
	}


	/**
	 * Next limited1.
	 */
	@Test public void nextLimited1() {
		this.iterator = new BottomUpAccess(relation, mt1, child1);
		this.iterator.open();
		Assert.assertEquals(outputType.createTuple(1, "x", "one", 2), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(2, "x", "two", 4), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
	}

	/**
	 * Next limited2.
	 */
	@Test public void nextLimited2() {
		this.iterator = new BottomUpAccess(relation, mt2, child2);
		this.iterator.open();
		Assert.assertEquals(outputType.createTuple(1, "x", "one", 2), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(2, "x", "two", 4), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
	}

	/**
	 * Next boolean.
	 */
	@Test public void nextBoolean() {
		this.iterator = new BottomUpAccess(relation, mt3, child3);
		this.iterator.open();
		Assert.assertEquals(outputType.createTuple(1, "x", "one", 2), this.iterator.next());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
	}

	/**
	 * Next boolean limit reached.
	 */
	@Test (expected=NoSuchElementException.class)
	public void nextBooleanLimitReached() {
		this.iterator = new BottomUpAccess(relation, mt3, child3);
		this.iterator.open();
		this.iterator.next();
		this.iterator.next();
		this.iterator.next();
	}

	/**
	 * Reset limited1.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void resetLimited1() {
		this.iterator = new BottomUpAccess(relation, mt1, child1);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(1, "x", "one", 2), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two", 4), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();

		TupleType child2Type = TupleType.DefaultFactory.create(String.class, Integer.class);
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(1, "x", "one", 2), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two", 4), this.iterator.next());
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
		this.iterator = new BottomUpAccess(relation, mt2, child2);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(1, "x", "one", 2), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two", 4), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "one", 8), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "x", "four", 32), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();

		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(1, "x", "one", 2), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(2, "x", "two", 4), this.iterator.next());
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
	 * Reset boolean.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void resetBoolean() {
		this.iterator = new BottomUpAccess(relation, mt3, child3);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(1, "x", "one", 2), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();

		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(1, "x", "one", 2), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(outputType.createTuple(3, "y", "one", 256), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.next();
	}

//	@Test(expected=NullPointerException.class)
//	public void bindNull() {
//		this.iterator = new BottomUpAccess(relation, binding3);
//		this.iterator.open();
//		this.iterator.bind(null);
//	}
//	
//	@Test(expected=IllegalStateException.class)
//	public void bindOnUnopened() {
//		this.iterator = new BottomUpAccess(relation, binding3);
//		this.iterator.bind(Tuple.EmptyTuple);
//	}
//	
//	@Test(expected=IllegalArgumentException.class)
//	public void bindIllegalType() {
//		this.iterator = new BottomUpAccess(relation, binding3);
//		this.iterator.open();
//		this.iterator.bind(outputType.createTuple("four", 4));
//	}
//	
//	@Test public void bindEmptyTuple() {
//		this.iterator = new BottomUpAccess(relation, binding3);
//		this.iterator.open();
//		this.iterator.bind(Tuple.EmptyTuple);
//		Assert.assertTrue(this.iterator.hasNext());
//		Assert.assertEquals(outputType.createTuple(2, "x", "two"), this.iterator.next());
//		Assert.assertFalse(this.iterator.hasNext());
//	}

	/* (non-Javadoc)
 * @see uk.ac.ox.cs.pdq.test.runtime.exec.iterator.TupleIteratorTest#getIterator()
 */
@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
}