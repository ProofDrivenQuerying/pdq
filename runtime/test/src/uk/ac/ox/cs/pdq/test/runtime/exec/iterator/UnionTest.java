package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Union;

/**
 * 
 * @author Julien LEBLAY
 */
public class UnionTest extends NaryIteratorTest {

	Union iterator;
	
	@Before public void setup() {
        MockitoAnnotations.initMocks(this);
        
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
        
        when(child2.hasNext()).thenReturn(true, true, true, true, false);
        when(child2.next()).thenReturn(
        		child2Type.createTuple(1, "A"),
        		child2Type.createTuple(2, "A"),
        		child2Type.createTuple(1, "A"),
        		child2Type.createTuple(1, "B"));
        when(child4.hasNext()).thenReturn(true, true, true, true, false);
        when(child4.next()).thenReturn(
        		child2Type.createTuple(1, "A"),
        		child2Type.createTuple(2, "B"),
        		child2Type.createTuple(1, "B"),
        		child2Type.createTuple(3, "B"));
        
        this.iterator = new Union(child2, child4);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initInconsistentChildren() {
        new Union(child2, child3, child4);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren1() {
        new Union(null, child3, child4);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren2() {
        new Union(child3, null, child4);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren3() {
        new Union(child3, child4, null);
	}

	@Test public void hasNextOneChild() {
		this.iterator = new Union(child2);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); 
	}

	@Test public void nextOneChild() {
		this.iterator = new Union(child2);
		this.iterator.open();
		Assert.assertEquals(child2Type.createTuple(1, "A"), this.iterator.next());
		Assert.assertEquals(child2Type.createTuple(2, "A"), this.iterator.next());
		Assert.assertEquals(child2Type.createTuple(1, "B"), this.iterator.next());
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextOneChildTooMany() {
		this.iterator = new Union(child2);
		this.iterator.open();
		this.iterator.next();
		this.iterator.next();
		this.iterator.next();
		this.iterator.next();
	}

	@Ignore public void resetOneChild() {
		this.iterator = new Union(child2);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(1, "A"), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(2, "A"), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(1, "B"), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();
        when(child2.hasNext()).thenReturn(true, true, true, true, false);
        when(child2.next()).thenReturn(
        		child2Type.createTuple(1, "A"),
        		child2Type.createTuple(2, "A"),
        		child2Type.createTuple(1, "A"),
        		child2Type.createTuple(1, "B"));
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(1, "A"), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(2, "A"), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(2, "B"), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test public void hasNextTwoChildren() {
		this.iterator = new Union(child2, child4);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); 
	}

	@Test public void nextTwoChildren() {
		this.iterator = new Union(child2, child4);
		this.iterator.open();
		Assert.assertEquals(child2Type.createTuple(1, "A"), this.iterator.next());
		Assert.assertEquals(child2Type.createTuple(2, "A"), this.iterator.next());
		Assert.assertEquals(child2Type.createTuple(1, "B"), this.iterator.next());
		Assert.assertEquals(child2Type.createTuple(2, "B"), this.iterator.next());
		Assert.assertEquals(child2Type.createTuple(3, "B"), this.iterator.next());
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextTwoChildrenTooMany() {
		this.iterator = new Union(child2, child4);
		this.iterator.open();
		this.iterator.next();
		this.iterator.next();
		this.iterator.next();
		this.iterator.next();
		this.iterator.next();
		this.iterator.next();
	}

	@Ignore public void resetTwoChildren() {
		this.iterator = new Union(child2, child4);
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(1, "A"), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(2, "A"), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(1, "B"), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();
        when(child2.hasNext()).thenReturn(true, true, true, true, false);
        when(child2.next()).thenReturn(
        		child2Type.createTuple(1, "A"),
        		child2Type.createTuple(2, "A"),
        		child2Type.createTuple(1, "A"),
        		child2Type.createTuple(1, "B"));
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(1, "A"), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(2, "A"), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(child2Type.createTuple(2, "B"), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}
	
	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
	
}
