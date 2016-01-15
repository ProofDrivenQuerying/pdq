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

import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.EmptyIterator;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.MemoryScan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.NestedLoopJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

/**
 * 
 * @author Julien LEBLAY
 */
public class NestedLoopJoinTest extends NaryIteratorTest {

	NestedLoopJoin iterator;
	// Non-mock
	TupleIterator nonMock1, nonMock2, nonMock3, nonMock4;

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
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren1() {
        new NestedLoopJoin(child4, child3, null);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren2() {
        new NestedLoopJoin(child3, null, child4);
	}

	@Test(expected=IllegalArgumentException.class) 
	public void initOneChild() {
		new NestedLoopJoin(child2);
	}

	@Test public void initTwoChildren() {
		Predicate bEqualsC = new AttributeEqualityPredicate(1, 4);
		this.iterator = new NestedLoopJoin(bEqualsC, child1, child2);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To2, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", child1To2Input, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", child1To2InputType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator predicate must match that of initialization", bEqualsC , this.iterator.getPredicate());
	}

	@Test public void initManyChildren() {
		Predicate bEqualsC = new AttributeEqualityPredicate(1, 4);
		Predicate dEqualsE = new AttributeEqualityPredicate(3, 5);
		Predicate cEqualsC = new AttributeEqualityPredicate(4, 7);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(bEqualsC, dEqualsE, cEqualsC));
		this.iterator = new NestedLoopJoin(conjunct, child1, child2, child3, child4);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To4, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To4Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", child1To4Input, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", child1To4InputType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2, child3, child4), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator predicate must match that of initialization", conjunct , this.iterator.getPredicate());
	}

	@Test public void initTwoChildrenNaturalJoin() {
		Predicate natural = new ConjunctivePredicate<>(new AttributeEqualityPredicate(2, 4));
		this.iterator = new NestedLoopJoin(child1, child2);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children",child1To2, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children",child1To2Input, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", child1To2InputType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator predicate must match that of natural join", natural, this.iterator.getPredicate());
	}

	@Test public void initManyChildrenNaturalJoin() {
		Predicate c1Toc2 = new AttributeEqualityPredicate(2, 4);
		Predicate c1Toc4 = new AttributeEqualityPredicate(2, 7);
		Predicate c2Toc3 = new AttributeEqualityPredicate(5, 6);
		Predicate c3Toc4 = new AttributeEqualityPredicate(5, 8);
		Predicate natural = new ConjunctivePredicate<>(Lists.newArrayList(c1Toc2, c1Toc4, c2Toc3, c3Toc4));
		this.iterator = new NestedLoopJoin(child1, child2, child3, child4);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To4, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To4Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", child1To4Input, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", child1To4InputType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2, child3, child4), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator predicate must match that of natural join", natural, this.iterator.getPredicate());
	}

	@Test public void initTwoChildrenClosed() {
		Predicate bEqualsC = new AttributeEqualityPredicate(1, 4);
		this.iterator = new NestedLoopJoin(bEqualsC, nonMock1, nonMock2);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To2, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator predicate must match that of initialization", bEqualsC , this.iterator.getPredicate());
	}

	@Test public void initManyChildrenClosed() {
		Predicate bEqualsC = new AttributeEqualityPredicate(1, 4);
		Predicate dEqualsE = new AttributeEqualityPredicate(3, 5);
		Predicate cEqualsC = new AttributeEqualityPredicate(4, 7);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(bEqualsC, dEqualsE, cEqualsC));
		this.iterator = new NestedLoopJoin(conjunct, nonMock1, nonMock2, nonMock3, nonMock4);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To4, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To4Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2, nonMock3, nonMock4), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator predicate must match that of initialization", conjunct , this.iterator.getPredicate());
	}

	@Test public void initTwoChildrenNaturalJoinClosed() {
		Predicate natural = new ConjunctivePredicate<>(new AttributeEqualityPredicate(2, 4));
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children",child1To2, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children",Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator predicate must match that of natural join", natural, this.iterator.getPredicate());
	}

	@Test public void initManyChildrenNaturalJoinClosed() {
		Predicate c1Toc2 = new AttributeEqualityPredicate(2, 4);
		Predicate c1Toc4 = new AttributeEqualityPredicate(2, 7);
		Predicate c2Toc3 = new AttributeEqualityPredicate(5, 6);
		Predicate c3Toc4 = new AttributeEqualityPredicate(5, 8);
		Predicate natural = new ConjunctivePredicate<>(Lists.newArrayList(c1Toc2, c1Toc4, c2Toc3, c3Toc4));
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, nonMock3, nonMock4);
		Assert.assertEquals("NestedLoopJoin iterator columns must match that of the concatenation of its children", child1To4, this.iterator.getColumns());
		Assert.assertEquals("NestedLoopJoin iterator type must match that of the concatenation of its children", child1To4Type, this.iterator.getType());
		Assert.assertEquals("NestedLoopJoin iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("NestedLoopJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("NestedLoopJoin iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2, nonMock3, nonMock4), this.iterator.getChildren());
		Assert.assertEquals("NestedLoopJoin iterator predicate must match that of natural join", natural, this.iterator.getPredicate());
	}

	@Test(expected=AssertionError.class)
	public void initWithInconsistentPredicate() {
		Predicate c1Toc2 = new AttributeEqualityPredicate(2, 4);
		Predicate c1ToOutOfBounds = new AttributeEqualityPredicate(2, 10);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(c1Toc2, c1ToOutOfBounds));
		new NestedLoopJoin(conjunct, child1, child2, child3, child4);
	}

	@Test(expected=AssertionError.class)
	public void initWithInconsistentPredicate2() {
		Predicate c1Toc2 = new AttributeEqualityPredicate(2, 4);
		Predicate c1ToOutOfBounds = new AttributeEqualityPredicate(2, -1);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(c1Toc2, c1ToOutOfBounds));
		new NestedLoopJoin(conjunct, child1, child2, child3, child4);
	}
	
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
	
	@Test public void iterateTwoChildren() {
		Predicate aEqualsE = new ConjunctivePredicate<>(new AttributeEqualityPredicate(0, 5));
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

	@Test(expected=NoSuchElementException.class) 
	public void nextTwoChildrenTooMany() {
		Predicate aEqualsE = new ConjunctivePredicate<>(new AttributeEqualityPredicate(0, 5));
		this.iterator = new NestedLoopJoin(aEqualsE, nonMock1, nonMock2);
		this.iterator.open();
		for (int i = 0, l = 6; i < l; i++) {
			this.iterator.next(); 
		}
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextTwoChildrenNaturalTooMany() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2);
		this.iterator.open();
		for (int i = 0, l = 12; i < l; i++) {
			this.iterator.next(); 
		}
	}

	@Test public void resetTwoChildren() {
		Predicate aEqualsE = new ConjunctivePredicate<>(new AttributeEqualityPredicate(0, 5));
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
	
	@Test public void iterateNaturalWithEmptyChild() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextNaturalWithEmptyChildTooMany() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, new EmptyIterator());
		this.iterator.open();
		this.iterator.next(); 
	}

	@Test public void resetNaturalWithEmptyChild() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
		
		this.iterator.reset();
		Assert.assertFalse(this.iterator.hasNext());
	}
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

	@Test public void iterateFourChildren() {
        Predicate c1toc3 = new AttributeEqualityPredicate(3, 6);
		Predicate c2toc4 = new AttributeEqualityPredicate(4, 7);
		Predicate c3toc4 = new AttributeEqualityPredicate(6, 8);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(c1toc3, c2toc4, c3toc4));
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

	@Test(expected=NoSuchElementException.class) 
	public void nextFourChildrenTooMany() {
		Predicate p1 = new AttributeEqualityPredicate(0, 5);
		Predicate p2 = new AttributeEqualityPredicate(5, 6);
		Predicate p3 = new AttributeEqualityPredicate(6, 8);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(p1, p2, p3));
		this.iterator = new NestedLoopJoin(conjunct, nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		for (int i = 0, l = 9; i < l; i++) {
			this.iterator.next(); 
		}
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextFourChildrenNaturalTooMany() {
		this.iterator = new NestedLoopJoin(nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		for (int i = 0, l = 9; i < l; i++) {
			this.iterator.next(); 
		}
	}

	@Test public void resetFourChildren() {
        Predicate c1toc3 = new AttributeEqualityPredicate(3, 6);
		Predicate c2toc4 = new AttributeEqualityPredicate(4, 7);
		Predicate c3toc4 = new AttributeEqualityPredicate(6, 8);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(c1toc3, c2toc4, c3toc4));
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
	
	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
	
}