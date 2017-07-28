package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.EmptyIterator;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.MemoryScan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.NestedLoopJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class NestedLoopJoinTest.
 *
 * @author Julien LEBLAY
 */
public class NestedLoopJoinTest extends NaryIteratorTest {

	/** The iterator. */
	NestedLoopJoin iterator;
	
	/** The non mock4. */
	// Non-mock
	TupleIterator nonMock1, nonMock2, nonMock3, nonMock4;

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.runtime.exec.iterator.TupleIteratorTest#setup()
	 */
	@Before public void setup() {
        super.setup();
		MockitoAnnotations.initMocks(this);
        
        when(child1.getColumns()).thenReturn(child1Columns);
        when(child1.getInputColumns()).thenReturn(child1InputColumns);
        when(child1.getType()).thenReturn(child1Type);
        when(child1.getInputType()).thenReturn(child1InputType);
        when(child2.getColumns()).thenReturn(child2Columns);
        when(child2.getInputColumns()).thenReturn(child2InputColumns);
        when(child2.getType()).thenReturn(child2Type);
        when(child2.getInputType()).thenReturn(child2InputType);
        when(child3.getColumns()).thenReturn(child3Columns);
        when(child3.getInputColumns()).thenReturn(child3InputColumns);
        when(child3.getType()).thenReturn(child3Type);
        when(child3.getInputType()).thenReturn(child3InputType);
        when(child4.getColumns()).thenReturn(child4Columns);
        when(child4.getInputColumns()).thenReturn(child4InputColumns);
        when(child4.getType()).thenReturn(child4Type);
        when(child4.getInputType()).thenReturn(child4InputType);

        // Using all-mocks for this test is tricky because of the complex
        // uses of hasNext and next methods. Reverting to memory scans below.
        nonMock1 = new MemoryScan(child1Columns, Lists.newArrayList(
        		child1Type.createTuple("A", 10, 1, "D"),
        		child1Type.createTuple("A", 10, 2, "D"),
        		child1Type.createTuple("A", 20, 1, "D"),
        		child1Type.createTuple("B", 20, 1, "D")));
        nonMock2 = new MemoryScan(child2Columns, Lists.newArrayList(
        		child2Type.createTuple(1, "A"),
        		child2Type.createTuple(2, "B"),
        		child2Type.createTuple(1, "B"),
        		child2Type.createTuple(1, "C")));
        nonMock3 = new MemoryScan(child3Columns, Lists.newArrayList(
        		child3Type.createTuple("B"),
        		child3Type.createTuple("B"),
        		child3Type.createTuple("C"),
        		child3Type.createTuple("D")));
        nonMock4 = new MemoryScan(child4Columns, Lists.newArrayList(
        		child4Type.createTuple(1, "A"),
        		child4Type.createTuple(2, "B"),
        		child4Type.createTuple(1, "B"),
        		child4Type.createTuple(2, "D")));
        
        this.iterator = new NestedLoopJoin(child1, child2, child3, child4);
	}
	
	/**
	 * Inits the null children1.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren1() {
        new NestedLoopJoin(child4, child3, null);
	}
	
	/**
	 * Inits the null children2.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren2() {
        new NestedLoopJoin(child3, null, child4);
	}

	/**
	 * Inits the one child.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initOneChild() {
		new NestedLoopJoin(child2);
	}

	/**
	 * Inits the two children.
	 */
	@Test public void initTwoChildren() {
		Condition bEqualsC = AttributeEqualityCondition.create(1, 4);
		this.iterator = new NestedLoopJoin(bEqualsC, child1, child2);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To2, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", child1To2Input, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", child1To2InputType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator Condition must match that of initialization", bEqualsC , this.iterator.getCondition());
	}

	/**
	 * Inits the many children.
	 */
	@Test public void initManyChildren() {
		Condition bEqualsC = AttributeEqualityCondition.create(1, 4);
		Condition dEqualsE = AttributeEqualityCondition.create(3, 5);
		Condition cEqualsC = AttributeEqualityCondition.create(4, 7);
		Condition conjunct = ConjunctiveCondition.create(Lists.newArrayList(bEqualsC, dEqualsE, cEqualsC));
		this.iterator = new NestedLoopJoin(conjunct, child1, child2, child3, child4);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To4, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To4Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", child1To4Input, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", child1To4InputType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2, child3, child4), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator Condition must match that of initialization", conjunct , this.iterator.getCondition());
	}

	/**
	 * Inits the two children natural join.
	 */
	@Test public void initTwoChildrenNaturalJoin() {
		Condition natural = ConjunctiveCondition.create(AttributeEqualityCondition.create(2, 4));
		this.iterator = new NestedLoopJoin(child1, child2);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children",child1To2, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children",child1To2Input, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", child1To2InputType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator Condition must match that of natural join", natural, this.iterator.getCondition());
	}

	/**
	 * Inits the many children natural join.
	 */
	@Test public void initManyChildrenNaturalJoin() {
		Condition c1Toc2 = AttributeEqualityCondition.create(2, 4);
		Condition c1Toc4 = AttributeEqualityCondition.create(2, 7);
		Condition c2Toc3 = AttributeEqualityCondition.create(5, 6);
		Condition c3Toc4 = AttributeEqualityCondition.create(5, 8);
		Condition natural = ConjunctiveCondition.create(Lists.newArrayList(c1Toc2, c1Toc4, c2Toc3, c3Toc4));
		this.iterator = new NestedLoopJoin(child1, child2, child3, child4);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To4, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To4Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", child1To4Input, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", child1To4InputType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2, child3, child4), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator Condition must match that of natural join", natural, this.iterator.getCondition());
	}

	/**
	 * Inits the two children closed.
	 */
	@Test public void initTwoChildrenClosed() {
		Condition bEqualsC = AttributeEqualityCondition.create(1, 4);
		this.iterator = new NestedLoopJoin(bEqualsC, nonMock1, nonMock2);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To2, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator Condition must match that of initialization", bEqualsC , this.iterator.getCondition());
	}

	/**
	 * Inits the many children closed.
	 */
	@Test public void initManyChildrenClosed() {
		Condition bEqualsC = AttributeEqualityCondition.create(1, 4);
		Condition dEqualsE = AttributeEqualityCondition.create(3, 5);
		Condition cEqualsC = AttributeEqualityCondition.create(4, 7);
		Condition conjunct = ConjunctiveCondition.create(Lists.newArrayList(bEqualsC, dEqualsE, cEqualsC));
		this.iterator = new NestedLoopJoin(conjunct, nonMock1, nonMock2, nonMock3, nonMock4);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To4, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To4Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2, nonMock3, nonMock4), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator Condition must match that of initialization", conjunct , this.iterator.getCondition());
	}

	/**
	 * Inits the two children natural join closed.
	 */
	@Test public void initTwoChildrenNaturalJoinClosed() {
		Condition natural = ConjunctiveCondition.create(AttributeEqualityCondition.create(2, 4));
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children",child1To2, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children",Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator Condition must match that of natural join", natural, this.iterator.getCondition());
	}

	/**
	 * Inits the many children natural join closed.
	 */
	@Test public void initManyChildrenNaturalJoinClosed() {
		Condition c1Toc2 = AttributeEqualityCondition.create(2, 4);
		Condition c1Toc4 = AttributeEqualityCondition.create(2, 7);
		Condition c2Toc3 = AttributeEqualityCondition.create(5, 6);
		Condition c3Toc4 = AttributeEqualityCondition.create(5, 8);
		Condition natural = ConjunctiveCondition.create(Lists.newArrayList(c1Toc2, c1Toc4, c2Toc3, c3Toc4));
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, nonMock3, nonMock4);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To4, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To4Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2, nonMock3, nonMock4), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator Condition must match that of natural join", natural, this.iterator.getCondition());
	}

	/**
	 * Inits the with inconsistent Condition.
	 */
	@Test(expected=AssertionError.class)
	public void initWithInconsistentCondition() {
		Condition c1Toc2 = AttributeEqualityCondition.create(2, 4);
		Condition c1ToOutOfBounds = AttributeEqualityCondition.create(2, 10);
		Condition conjunct = ConjunctiveCondition.create(Lists.newArrayList(c1Toc2, c1ToOutOfBounds));
		new NestedLoopJoin(conjunct, child1, child2, child3, child4);
	}

	/**
	 * Inits the with inconsistent Condition2.
	 */
	@Test(expected=AssertionError.class)
	public void initWithInconsistentCondition2() {
		Condition c1Toc2 = AttributeEqualityCondition.create(2, 4);
		Condition c1ToOutOfBounds = AttributeEqualityCondition.create(2, -1);
		Condition conjunct = ConjunctiveCondition.create(Lists.newArrayList(c1Toc2, c1ToOutOfBounds));
		new NestedLoopJoin(conjunct, child1, child2, child3, child4);
	}
	
	/**
	 * Iterate two children natural.
	 */
	@Test public void iterateTwoChildrenNatural() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2);
		this.iterator.open();
		Set<Tuple> expected = new LinkedHashSet<>();
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "B"));
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "C"));
		expected.add(child1To2Type.createTuple("A", 10, 2, "D", 2, "B"));
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("A", 20, 1, "D", 1, "B"));
		expected.add(child1To2Type.createTuple("A", 20, 1, "D", 1, "C"));
		expected.add(child1To2Type.createTuple("A", 20, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 1, "B"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 1, "C"));
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 10; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
	}
	
	/**
	 * Iterate two children.
	 */
	@Test public void iterateTwoChildren() {
		Condition aEqualsE = ConjunctiveCondition.create(AttributeEqualityCondition.create(0, 5));
		this.iterator = new NestedLoopJoin(aEqualsE, nonMock1, nonMock2);
		this.iterator.open();
		Set<Tuple> expected = new LinkedHashSet<>();
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("A", 10, 2, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("A", 20, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 2, "B"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 1, "B"));
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 5; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
	}

	/**
	 * Next two children too many.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void nextTwoChildrenTooMany() {
		Condition aEqualsE = ConjunctiveCondition.create(AttributeEqualityCondition.create(0, 5));
		this.iterator = new NestedLoopJoin(aEqualsE, nonMock1, nonMock2);
		this.iterator.open();
		for (int i = 0, l = 6; i < l; i++) {
			this.iterator.next(); 
		}
	}

	/**
	 * Next two children natural too many.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void nextTwoChildrenNaturalTooMany() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2);
		this.iterator.open();
		for (int i = 0, l = 12; i < l; i++) {
			this.iterator.next(); 
		}
	}

	/**
	 * Reset two children.
	 */
	@Test public void resetTwoChildren() {
		Condition aEqualsE = ConjunctiveCondition.create(AttributeEqualityCondition.create(0, 5));
		this.iterator = new NestedLoopJoin(aEqualsE, nonMock1, nonMock2);
		this.iterator.open();
		Set<Tuple> expected = new LinkedHashSet<>();
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("A", 10, 2, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("A", 20, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 2, "B"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 1, "B"));
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 5; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
		
		this.iterator.reset();
		observed.clear();
		for (int i = 0, l = 5; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
	}

	/**
	 * Reset two children natural.
	 */
	@Test public void resetTwoChildrenNatural() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2);
		this.iterator.open();
		Set<Tuple> expected = new LinkedHashSet<>();
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "B"));
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "C"));
		expected.add(child1To2Type.createTuple("A", 10, 2, "D", 2, "B"));
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("A", 20, 1, "D", 1, "B"));
		expected.add(child1To2Type.createTuple("A", 20, 1, "D", 1, "C"));
		expected.add(child1To2Type.createTuple("A", 20, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 1, "B"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 1, "C"));
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 10; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
		
		this.iterator.reset();
		observed.clear();
		for (int i = 0, l = 10; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
	}
	
	/**
	 * Iterate natural with empty child.
	 */
	@Test public void iterateNaturalWithEmptyChild() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Next natural with empty child too many.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void nextNaturalWithEmptyChildTooMany() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, new EmptyIterator());
		this.iterator.open();
		this.iterator.next(); 
	}

	/**
	 * Reset natural with empty child.
	 */
	@Test public void resetNaturalWithEmptyChild() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
		
		this.iterator.reset();
		Assert.assertFalse(this.iterator.hasNext());
	}
	
	/**
	 * Iterate four children natural.
	 */
	@Test public void iterateFourChildrenNatural() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		Set<Tuple> expected = new LinkedHashSet<>();
		expected.add(child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 1, "B"));
		expected.add(child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 2, "B"));
		expected.add(child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 1, "B"));
		expected.add(child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 1, "B"));
		expected.add(child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 1, "B"));
		expected.add(child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 2, "B"));
		expected.add(child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 1, "B"));
		expected.add(child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 1, "B"));
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 8; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
	}

	/**
	 * Iterate four children.
	 */
	@Test public void iterateFourChildren() {
        Condition c1toc3 = AttributeEqualityCondition.create(3, 6);
		Condition c2toc4 = AttributeEqualityCondition.create(4, 7);
		Condition c3toc4 = AttributeEqualityCondition.create(6, 8);
		Condition conjunct = ConjunctiveCondition.create(Lists.newArrayList(c1toc3, c2toc4, c3toc4));
		this.iterator = new NestedLoopJoin(conjunct, nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		Set<Tuple> expected = new LinkedHashSet<>();
		expected.add(child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "D", 2, "D"));
		expected.add(child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "D", 2, "D"));
		expected.add(child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "D", 2, "D"));
		expected.add(child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "D", 2, "D"));
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 4; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
	}

	/**
	 * Next four children too many.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void nextFourChildrenTooMany() {
		Condition p1 = AttributeEqualityCondition.create(0, 5);
		Condition p2 = AttributeEqualityCondition.create(5, 6);
		Condition p3 = AttributeEqualityCondition.create(6, 8);
		Condition conjunct = ConjunctiveCondition.create(Lists.newArrayList(p1, p2, p3));
		this.iterator = new NestedLoopJoin(conjunct, nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		for (int i = 0, l = 9; i < l; i++) {
			this.iterator.next(); 
		}
	}

	/**
	 * Next four children natural too many.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void nextFourChildrenNaturalTooMany() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		for (int i = 0, l = 9; i < l; i++) {
			this.iterator.next(); 
		}
	}

	/**
	 * Reset four children.
	 */
	@Test public void resetFourChildren() {
        Condition c1toc3 = AttributeEqualityCondition.create(3, 6);
		Condition c2toc4 = AttributeEqualityCondition.create(4, 7);
		Condition c3toc4 = AttributeEqualityCondition.create(6, 8);
		Condition conjunct = ConjunctiveCondition.create(Lists.newArrayList(c1toc3, c2toc4, c3toc4));
		this.iterator = new NestedLoopJoin(conjunct, nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		Set<Tuple> expected = new LinkedHashSet<>();
		expected.add(child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "D", 2, "D"));
		expected.add(child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "D", 2, "D"));
		expected.add(child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "D", 2, "D"));
		expected.add(child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "D", 2, "D"));
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 4; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
		
		this.iterator.reset();
		observed.clear();
		for (int i = 0, l = 4; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
	}

	/**
	 * Reset four children natural.
	 */
	@Test public void resetFourChildrenNatural() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		Set<Tuple> expected = new LinkedHashSet<>();
		expected.add(child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 1, "B"));
		expected.add(child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 2, "B"));
		expected.add(child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 1, "B"));
		expected.add(child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 1, "B"));
		expected.add(child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 1, "B"));
		expected.add(child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 2, "B"));
		expected.add(child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 1, "B"));
		expected.add(child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 1, "B"));
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 8; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
		
		this.iterator.reset();
		observed.clear();
		for (int i = 0, l = 8; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.runtime.exec.iterator.TupleIteratorTest#getIterator()
	 */
	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
	
}