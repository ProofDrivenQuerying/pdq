package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import uk.ac.ox.cs.pdq.runtime.exec.iterator.CrossProduct;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.EmptyIterator;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.MemoryScan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Julien LEBLAY
 */
public class CrossProductTest extends NaryIteratorTest {

	CrossProduct iterator;
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
        
        this.iterator = new CrossProduct(child1, child2, child3, child4);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren1() {
        new CrossProduct(child4, child3, null);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren2() {
        new CrossProduct(child3, null, child4);
	}

	@Test public void initTwoChildren() {
		this.iterator = new CrossProduct(child1, child2);
		Assert.assertEquals("CrossProduct iterator columns must match that of the concatenation of its children",child1To2, this.iterator.getColumns());
		Assert.assertEquals("CrossProduct iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("CrossProduct iterator input columns must match that of the concatenation of its children",child1To2Input, this.iterator.getInputColumns());
		Assert.assertEquals("CrossProduct iterator input type must match that of the concatenation of its children", child1To2InputType, this.iterator.getInputType());
		Assert.assertEquals("CrossProduct iterator children must match that of initialization", Lists.newArrayList(child1, child2), this.iterator.getChildren());
	}

	@Test public void initManyChildren() {
		this.iterator = new CrossProduct(child1, child2, child3, child4);
		Assert.assertEquals("CrossProduct iterator columns must match that of the concatenation of its children", child1To4, this.iterator.getColumns());
		Assert.assertEquals("CrossProduct iterator type must match that of the concatenation of its children", child1To4Type, this.iterator.getType());
		Assert.assertEquals("CrossProduct iterator input columns must match that of the concatenation of its children", child1To4Input, this.iterator.getInputColumns());
		Assert.assertEquals("CrossProduct iterator input type must match that of the concatenation of its children", child1To4InputType, this.iterator.getInputType());
		Assert.assertEquals("CrossProduct iterator children must match that of initialization", Lists.newArrayList(child1, child2, child3, child4), this.iterator.getChildren());
	}

	@Test public void initTwoChildrenClosed() {
		this.iterator = new CrossProduct(nonMock1, nonMock2);
		Assert.assertEquals("CrossProduct iterator columns must match that of the concatenation of its children",child1To2, this.iterator.getColumns());
		Assert.assertEquals("CrossProduct iterator type must match that of the concatenation of its children", child1To2Type, this.iterator.getType());
		Assert.assertEquals("CrossProduct iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("CrossProduct iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("CrossProduct iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2), this.iterator.getChildren());
	}

	@Test public void initManyChildrenClosed() {
		this.iterator = new CrossProduct(nonMock1, nonMock2, nonMock3, nonMock4);
		Assert.assertEquals("CrossProduct iterator columns must match that of the concatenation of its children", child1To4, this.iterator.getColumns());
		Assert.assertEquals("CrossProduct iterator type must match that of the concatenation of its children", child1To4Type, this.iterator.getType());
		Assert.assertEquals("CrossProduct iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("CrossProduct iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("CrossProduct iterator children must match that of initialization", Lists.newArrayList(nonMock1, nonMock2, nonMock3, nonMock4), this.iterator.getChildren());
	}

	@Test public void iterateTwoChildren() {
		this.iterator = new CrossProduct(nonMock1, nonMock2);
		this.iterator.open();
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 16; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected16, observed);
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextTwoChildrenTooMany() {
		this.iterator = new CrossProduct(nonMock1, nonMock2);
		this.iterator.open();
		for (int i = 0, l = 17; i < l; i++) {
			this.iterator.next(); 
		}
	}

	@Test public void resetTwoChildren() {
		this.iterator = new CrossProduct(nonMock1, nonMock2);
		this.iterator.open();	
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 16; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected16, observed);
		
		this.iterator.reset();
		observed.clear();
		for (int i = 0, l = 16; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected16, observed);
	}
	
	@Test public void iterateWithEmptyChild() {
		this.iterator = new CrossProduct(nonMock1, nonMock2, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextWithEmptyChildTooMany() {
		this.iterator = new CrossProduct(nonMock1, nonMock2, new EmptyIterator());
		this.iterator.open();
		this.iterator.next(); 
	}

	@Test public void resetWithEmptyChild() {
		this.iterator = new CrossProduct(nonMock1, nonMock2, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
		
		this.iterator.reset();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void iterateFourChildren() {
		this.iterator = new CrossProduct(nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 256; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected256, observed);
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextFourChildrenTooMany() {
		this.iterator = new CrossProduct(nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		for (int i = 0, l = 257; i < l; i++) {
			this.iterator.next();   
		}
	}

	@Test public void resetFourChildren() {
		this.iterator = new CrossProduct(nonMock1, nonMock2, nonMock3, nonMock4);
		this.iterator.open();
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 256; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected256, observed);
		
		this.iterator.reset();
		observed.clear();
		for (int i = 0, l = 256; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected256, observed);
	}
	
	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
	
	Set<Tuple> expected16 = Sets.newSet(
			child1To2Type.createTuple("A", 10, 1, "D", 1, "A"),
			child1To2Type.createTuple("A", 10, 1, "D", 2, "B"),
			child1To2Type.createTuple("A", 10, 1, "D", 1, "B"),
			child1To2Type.createTuple("A", 10, 1, "D", 1, "C"),
			child1To2Type.createTuple("A", 10, 2, "D", 1, "A"),
			child1To2Type.createTuple("A", 10, 2, "D", 2, "B"),
			child1To2Type.createTuple("A", 10, 2, "D", 1, "B"),
			child1To2Type.createTuple("A", 10, 2, "D", 1, "C"),
			child1To2Type.createTuple("A", 20, 1, "D", 1, "A"),
			child1To2Type.createTuple("A", 20, 1, "D", 2, "B"),
			child1To2Type.createTuple("A", 20, 1, "D", 1, "B"),
			child1To2Type.createTuple("A", 20, 1, "D", 1, "C"),
			child1To2Type.createTuple("B", 20, 1, "D", 1, "A"),
			child1To2Type.createTuple("B", 20, 1, "D", 2, "B"),
			child1To2Type.createTuple("B", 20, 1, "D", 1, "B"),
			child1To2Type.createTuple("B", 20, 1, "D", 1, "C")
	);	
	Set<Tuple> expected256 = Sets.newSet(
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "B", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "B", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "B", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "B", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "B", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "B", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "B", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "B", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "B", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "B", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "B", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "C", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "C", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "C", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "C", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "C", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "C", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "C", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "C", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "C", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "C", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "C", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "C", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "C", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "C", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "C", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "C", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "D", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "D", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "D", 1, "A"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "D", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "D", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "D", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "D", 1, "A"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "D", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "D", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "D", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "D", 1, "A"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "D", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "D", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "D", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "D", 1, "A"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "D", 1, "A"),

			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "B", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "B", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "B", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "B", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "B", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "B", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "B", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "B", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "B", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "B", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "B", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "C", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "C", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "C", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "C", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "C", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "C", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "C", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "C", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "C", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "C", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "C", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "C", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "C", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "C", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "C", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "C", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "D", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "D", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "D", 2, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "D", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "D", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "D", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "D", 2, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "D", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "D", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "D", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "D", 2, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "D", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "D", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "D", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "D", 2, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "D", 2, "B"),

			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "B", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "B", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "B", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "B", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "B", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "B", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "B", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "B", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "B", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "B", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "B", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "C", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "C", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "C", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "C", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "C", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "C", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "C", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "C", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "C", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "C", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "C", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "C", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "C", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "C", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "C", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "C", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "D", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "D", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "D", 1, "B"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "D", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "D", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "D", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "D", 1, "B"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "D", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "D", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "D", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "D", 1, "B"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "D", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "D", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "D", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "D", 1, "B"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "D", 1, "B"),

			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "B", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "B", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "B", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "B", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "B", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "B", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "B", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "B", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "B", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "B", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "B", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "B", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "B", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "C", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "C", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "C", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "C", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "C", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "C", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "C", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "C", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "C", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "C", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "C", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "C", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "C", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "C", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "C", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "C", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "A", "D", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 2, "B", "D", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "B", "D", 2, "D"),
			child1To4Type.createTuple("A", 10, 1, "D", 1, "C", "D", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "A", "D", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 2, "B", "D", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "B", "D", 2, "D"),
			child1To4Type.createTuple("A", 10, 2, "D", 1, "C", "D", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "A", "D", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 2, "B", "D", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "B", "D", 2, "D"),
			child1To4Type.createTuple("A", 20, 1, "D", 1, "C", "D", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "A", "D", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 2, "B", "D", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "B", "D", 2, "D"),
			child1To4Type.createTuple("B", 20, 1, "D", 1, "C", "D", 2, "D")
	);
}