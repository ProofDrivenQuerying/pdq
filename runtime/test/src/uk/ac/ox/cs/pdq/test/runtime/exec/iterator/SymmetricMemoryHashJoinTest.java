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
import uk.ac.ox.cs.pdq.runtime.exec.iterator.SymmetricMemoryHashJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

/**
 * 
 * @author Julien LEBLAY
 *
 */
public class SymmetricMemoryHashJoinTest extends NaryIteratorTest {

	SymmetricMemoryHashJoin iterator;
	// Non-mock
	TupleIterator nonMock1, nonMock2, nonMock4;

	
	@Before public void setup() {
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
        
        // Using mocks for test the hash join is tricky because of the complex
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
        nonMock4 = new MemoryScan(child4Columns, Lists.newArrayList(
        		child4Type.createTuple(1, "A"),
        		child4Type.createTuple(2, "B"),
        		child4Type.createTuple(1, "B"),
        		child4Type.createTuple(2, "D")));

        this.iterator = new SymmetricMemoryHashJoin(child1, child2);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren1() {
        new SymmetricMemoryHashJoin(child1, null);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren2() {
        new SymmetricMemoryHashJoin(null, child1);
	}

	@Test public void initSemiJoin() {
		Predicate aEqualsE = new AttributeEqualityPredicate(0, 4);
		Predicate predicate = new ConjunctivePredicate<>(Lists.newArrayList(aEqualsE));
		this.iterator = new SymmetricMemoryHashJoin(predicate, child1, child3);
		Assert.assertEquals("SymmetricMemoryHashJoin iterator columns must match that of the concatenation of its children", child1And3, this.iterator.getColumns());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator type must match that of the concatenation of its children", child1And3Type, this.iterator.getType());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator input columns must match that of the concatenation of its children", child1And3Input, this.iterator.getInputColumns());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator input type must match that of the concatenation of its children", child1And3InputType, this.iterator.getInputType());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator children must match that of initialization", Lists.newArrayList(child1, child3), this.iterator.getChildren());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator predicate must match that of initialization", predicate , this.iterator.getPredicate());
	}

	@Test public void initTwoChildren() {
		Predicate bEqualsC = new ConjunctivePredicate<>(new AttributeEqualityPredicate(1, 4));
		this.iterator = new SymmetricMemoryHashJoin(bEqualsC, child1, child2);
		Assert.assertEquals("SymmetricMemoryHashJoin iterator columns must match that of the concatenation of its children", child1To2, this.iterator.getColumns());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator input columns must match that of the concatenation of its children", child1To2Input, this.iterator.getInputColumns());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator input type must match that of the concatenation of its children", child1To2InputType, this.iterator.getInputType());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2), this.iterator.getChildren());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator predicate must match that of initialization", bEqualsC , this.iterator.getPredicate());
	}

	@Test public void initTwoChildrenNaturalJoin() {
		Predicate natural = new ConjunctivePredicate<>(new AttributeEqualityPredicate(2, 4));
		this.iterator = new SymmetricMemoryHashJoin(child1, child2);
		Assert.assertEquals("SymmetricMemoryHashJoin iterator columns must match that of the concatenation of its children",child1To2, this.iterator.getColumns());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator input columns must match that of the concatenation of its children",child1To2Input, this.iterator.getInputColumns());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator input type must match that of the concatenation of its children", child1To2InputType, this.iterator.getInputType());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2), this.iterator.getChildren());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator predicate must match that of natural join", natural, this.iterator.getPredicate());
	}

	@Test public void initTwoChildrenOpen() {
		Predicate bEqualsC = new ConjunctivePredicate<>(new AttributeEqualityPredicate(1, 4));
		this.iterator = new SymmetricMemoryHashJoin(bEqualsC, nonMock1, nonMock2);
		Assert.assertEquals("SymmetricMemoryHashJoin iterator columns must match that of the concatenation of its children", child1To2, this.iterator.getColumns());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2), this.iterator.getChildren());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator predicate must match that of initialization", bEqualsC , this.iterator.getPredicate());
	}

	@Test public void initTwoChildrenNaturalJoinOpen() {
		Predicate natural = new ConjunctivePredicate<>(new AttributeEqualityPredicate(2, 4));
		this.iterator = new SymmetricMemoryHashJoin(nonMock1, nonMock2);
		Assert.assertEquals("SymmetricMemoryHashJoin iterator columns must match that of the concatenation of its children",child1To2, this.iterator.getColumns());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2), this.iterator.getChildren());
		Assert.assertEquals("SymmetricMemoryHashJoin iterator predicate must match that of natural join", natural, this.iterator.getPredicate());
	}

	@Test(expected=AssertionError.class)
	public void initWithInconsistentPredicate() {
		Predicate c1Toc2 = new AttributeEqualityPredicate(2, 4);
		Predicate c1ToOutOfBounds = new AttributeEqualityPredicate(2, 10);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(c1Toc2, c1ToOutOfBounds));
		new SymmetricMemoryHashJoin(conjunct, child1, child2);
	}

	@Test(expected=AssertionError.class)
	public void initWithInconsistentPredicate2() {
		Predicate c1Toc2 = new AttributeEqualityPredicate(2, 4);
		Predicate c1ToOutOfBounds = new AttributeEqualityPredicate(2, -1);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(c1Toc2, c1ToOutOfBounds));
		new SymmetricMemoryHashJoin(conjunct, child1, child2);
	}
	
	@Test public void iterateTwoChildrenNatural() {
		this.iterator = new SymmetricMemoryHashJoin(nonMock1, nonMock2);
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
//		while (this.iterator.hasNext()) {
//			System.out.println(this.iterator.next());
//		}
		for (int i = 0, l = 10; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
	}
	
	@Test public void iterateTwoChildren() {
		Predicate aEqualsE = new ConjunctivePredicate<>(new AttributeEqualityPredicate(0, 5));
		this.iterator = new SymmetricMemoryHashJoin(aEqualsE, nonMock1, nonMock2);
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
		this.iterator = new SymmetricMemoryHashJoin(aEqualsE, nonMock1, nonMock2);
		this.iterator.open();
		for (int i = 0, l = 6; i < l; i++) {
			this.iterator.next(); 
		}
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextTwoChildrenNaturalTooMany() {
		this.iterator = new SymmetricMemoryHashJoin(nonMock1, nonMock2);
		this.iterator.open();
		for (int i = 0, l = 16; i < l; i++) {
			this.iterator.next(); 
		}
	}

	@Test public void resetTwoChildren() {
		Predicate aEqualsE = new ConjunctivePredicate<>(new AttributeEqualityPredicate(0, 5));
		this.iterator = new SymmetricMemoryHashJoin(aEqualsE, nonMock1, nonMock2);
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
		this.iterator = new SymmetricMemoryHashJoin(nonMock1, nonMock2);
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
		this.iterator = new SymmetricMemoryHashJoin(nonMock1, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextNaturalWithEmptyChildTooMany() {
		this.iterator = new SymmetricMemoryHashJoin(nonMock1, new EmptyIterator());
		this.iterator.open();
		this.iterator.next(); 
	}

	@Test public void resetNaturalWithEmptyChild() {
		this.iterator = new SymmetricMemoryHashJoin(nonMock1, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
		
		this.iterator.reset();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void iterateSemiJoin() {
		Predicate aEqualsE = new AttributeEqualityPredicate(0, 5);
		Predicate cEqualsC = new AttributeEqualityPredicate(2, 4);
		Predicate predicate = new ConjunctivePredicate<>(Lists.newArrayList(aEqualsE, cEqualsC));
		this.iterator = new SymmetricMemoryHashJoin(predicate, nonMock1, nonMock4);
		this.iterator.open();
		Set<Tuple> expected = new LinkedHashSet<>();
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("A", 20, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 1, "B"));
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 3; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextSemiJoinTooMany() {
		Predicate aEqualsE = new AttributeEqualityPredicate(0, 5);
		Predicate cEqualsC = new AttributeEqualityPredicate(2, 4);
		Predicate predicate = new ConjunctivePredicate<>(Lists.newArrayList(aEqualsE, cEqualsC));
		this.iterator = new SymmetricMemoryHashJoin(predicate, nonMock1, nonMock4);
		this.iterator.open();
		for (int i = 0, l = 4; i < l; i++) {
			this.iterator.next(); 
		}
	}

	@Test public void resetSemiJoin() {
		Predicate aEqualsE = new AttributeEqualityPredicate(0, 5);
		Predicate cEqualsC = new AttributeEqualityPredicate(2, 4);
		Predicate predicate = new ConjunctivePredicate<>(Lists.newArrayList(aEqualsE, cEqualsC));
		this.iterator = new SymmetricMemoryHashJoin(predicate, nonMock1, nonMock4);
		this.iterator.open();
		Set<Tuple> expected = new LinkedHashSet<>();
		expected.add(child1To2Type.createTuple("A", 10, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("A", 20, 1, "D", 1, "A"));
		expected.add(child1To2Type.createTuple("B", 20, 1, "D", 1, "B"));
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 3; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected, observed);

		this.iterator.reset();
		observed.clear();
		for (int i = 0, l = 3; i < l; i++) {
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
