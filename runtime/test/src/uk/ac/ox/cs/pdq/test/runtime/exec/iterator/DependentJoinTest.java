package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.DependentJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.EmptyIterator;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Scan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Access;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class BindJoinTest.
 */
public class DependentJoinTest extends NaryIteratorTest {

	/** The iterator. */
	DependentJoin iterator;
	
	/** The in34. */
	List<Typed> out12, out21, in21, out34, in34;
	
	/** The free. */
	AccessMethod free ;
	
	/** The mt1. */
	AccessMethod mt1;
	
	/** The mt2. */
	AccessMethod mt2;
	
	/** The mt3. */
	AccessMethod mt3;
	
	/** The rel1. */
	InMemoryTableWrapper rel1;
	
	/** The rel2. */
	InMemoryTableWrapper rel2;
	
	/** The expected34. */
	Set<Tuple> expected12, expected21, expected34;

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.runtime.exec.iterator.TupleIteratorTest#setup()
	 */
	@Before public void setup() {
		super.setup();
        MockitoAnnotations.initMocks(this);
		d = Attribute.create(String.class, "d");
		out12 = Lists.newArrayList(a, b, c, d, c, e);
		out21 = Lists.newArrayList(c, e, a, b, c, d);
		in21 = Lists.newArrayList(c);
		out34 = Lists.newArrayList(c, e, a, b, c, d); 
		in34 = Lists.newArrayList(e, a, b);
        // Using mocks for test the bind join is tricky because of the complex
        // uses of hasNext and next methods. Reverting to memory scans below.
        free = AccessMethod.create(new Integer[]{});
        mt1 = AccessMethod.create("m1", new Integer[]{1});
        mt2 = AccessMethod.create("m2", new Integer[]{2});
        mt3 = AccessMethod.create("m3", new Integer[]{1,2});
        rel1 = new InMemoryTableWrapper("R1", 
        		new Attribute[]{(Attribute) a, (Attribute) b, (Attribute) c, (Attribute) d},
	    		new AccessMethod[]{free, mt3});
        rel1.load(Lists.newArrayList(
        		child1Type.createTuple("A", 10, 1, "D"),
        		child1Type.createTuple("A", 10, 2, "D"),
        		child1Type.createTuple("A", 20, 1, "D"),
        		child1Type.createTuple("B", 20, 1, "D")));
        rel2 = new InMemoryTableWrapper("R2", 
        		new Attribute[]{(Attribute) c, (Attribute) e},
	    		new AccessMethod[]{mt1, mt2});
        
        rel2.load(Lists.newArrayList(
        		child2Type.createTuple(1, "A"),
        		child2Type.createTuple(2, "B"),
        		child2Type.createTuple(1, "B"),
        		child2Type.createTuple(1, "C")));
        child1 = new Scan(rel1);
		child2 = new Access(rel2, mt1);
		child3 = new Access(rel2, mt2);
		child4 = new Access(rel1, mt3);

        this.iterator = new DependentJoin(child1, child2);

        this.expected12 = Sets.newSet(
    			TupleType.DefaultFactory.createFromTyped(out12).createTuple("A", 10, 1, "D", 1, "A"),
    			TupleType.DefaultFactory.createFromTyped(out12).createTuple("A", 10, 1, "D", 1, "B"),
    			TupleType.DefaultFactory.createFromTyped(out12).createTuple("A", 10, 1, "D", 1, "C"),
    			TupleType.DefaultFactory.createFromTyped(out12).createTuple("A", 10, 2, "D", 2, "B"),
    			TupleType.DefaultFactory.createFromTyped(out12).createTuple("A", 20, 1, "D", 1, "B"),
    			TupleType.DefaultFactory.createFromTyped(out12).createTuple("A", 20, 1, "D", 1, "C"),
    			TupleType.DefaultFactory.createFromTyped(out12).createTuple("A", 20, 1, "D", 1, "A"),
    			TupleType.DefaultFactory.createFromTyped(out12).createTuple("B", 20, 1, "D", 1, "A"),
    			TupleType.DefaultFactory.createFromTyped(out12).createTuple("B", 20, 1, "D", 1, "B"),
    			TupleType.DefaultFactory.createFromTyped(out12).createTuple("B", 20, 1, "D", 1, "C"));
    	this.expected21 = Sets.newSet(
    			TupleType.DefaultFactory.createFromTyped(out21).createTuple(1, "A", "A", 10, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out21).createTuple(1, "B", "A", 10, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out21).createTuple(1, "C", "A", 10, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out21).createTuple(2, "B", "A", 10, 2, "D"),
    			TupleType.DefaultFactory.createFromTyped(out21).createTuple(1, "B", "A", 20, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out21).createTuple(1, "C", "A", 20, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out21).createTuple(1, "A", "A", 20, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out21).createTuple(1, "A", "B", 20, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out21).createTuple(1, "B", "B", 20, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out21).createTuple(1, "C", "B", 20, 1, "D"));
    	this.expected34 = Sets.newSet(
    			TupleType.DefaultFactory.createFromTyped(out34).createTuple(1, "A", "A", 10, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out34).createTuple(1, "B", "A", 10, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out34).createTuple(1, "C", "A", 10, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out34).createTuple(2, "B", "A", 10, 2, "D"),
    			TupleType.DefaultFactory.createFromTyped(out34).createTuple(1, "B", "A", 20, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out34).createTuple(1, "C", "A", 20, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out34).createTuple(1, "A", "A", 20, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out34).createTuple(1, "A", "B", 20, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out34).createTuple(1, "B", "B", 20, 1, "D"),
    			TupleType.DefaultFactory.createFromTyped(out34).createTuple(1, "C", "B", 20, 1, "D"));

	}
	
	/**
	 * Inits the null children1.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren1() {
        new DependentJoin(child4, null);
	}
	
	/**
	 * Inits the null children2.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren2() {
        new DependentJoin(null, child4);
	}

	/**
	 * Inits the two children12.
	 */
	@Test public void initTwoChildren12() {
		Condition natural = ConjunctiveCondition.create(new SimpleCondition[]{AttributeEqualityCondition.create(2, 4)});
		this.iterator = new DependentJoin(child1, child2);
		Assert.assertEquals("BindJoin iterator columns must match that of the concatenation of its children", out12, this.iterator.getColumns());
		Assert.assertEquals("BindJoin iterator type must match that of the concatenation of its children", TupleType.DefaultFactory.createFromTyped(out12), this.iterator.getType());
		Assert.assertEquals("BindJoin iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("BindJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("BindJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2), this.iterator.getChildren());
		Assert.assertEquals("BindJoin iterator Condition must match that of natural join", natural, this.iterator.getCondition());
	}

	/**
	 * Inits the two children21.
	 */
	@Test public void initTwoChildren21() {
		Condition natural = ConjunctiveCondition.create(new SimpleCondition[]{AttributeEqualityCondition.create(0, 4)});
		this.iterator = new DependentJoin(child2, child1);
		Assert.assertEquals("BindJoin iterator columns must match that of the concatenation of its children", out21, this.iterator.getColumns());
		Assert.assertEquals("BindJoin iterator type must match that of the concatenation of its children", TupleType.DefaultFactory.createFromTyped(out21), this.iterator.getType());
		Assert.assertEquals("BindJoin iterator input columns must match that of the concatenation of its children", in21, this.iterator.getInputColumns());
		Assert.assertEquals("BindJoin iterator input type must match that of the concatenation of its children", TupleType.DefaultFactory.createFromTyped(in21), this.iterator.getInputType());
		Assert.assertEquals("BindJoin iterator children must match that of initialization", Lists.newArrayList(child2, child1), this.iterator.getChildren());
		Assert.assertEquals("BindJoin iterator Condition must match that of natural join", natural, this.iterator.getCondition());
	}

	/**
	 * Inits the two children34.
	 */
	@Test public void initTwoChildren34() {
		Condition natural = ConjunctiveCondition.create(new SimpleCondition[]{AttributeEqualityCondition.create(0, 4)});
		this.iterator = new DependentJoin(child3, child4);
		Assert.assertEquals("BindJoin iterator columns must match that of the concatenation of its children", out34, this.iterator.getColumns());
		Assert.assertEquals("BindJoin iterator type must match that of the concatenation of its children", TupleType.DefaultFactory.createFromTyped(out34), this.iterator.getType());
		Assert.assertEquals("BindJoin iterator input columns must match that of the concatenation of its children", in34, this.iterator.getInputColumns());
		Assert.assertEquals("BindJoin iterator input type must match that of the concatenation of its children", TupleType.DefaultFactory.createFromTyped(in34), this.iterator.getInputType());
		Assert.assertEquals("BindJoin iterator children must match that of initialization", Lists.newArrayList(child3, child4), this.iterator.getChildren());
		Assert.assertEquals("BindJoin iterator Condition must match that of natural join", natural, this.iterator.getCondition());
	}

	/**
	 * Inits the with inconsistent Condition.
	 */
	@Test(expected=AssertionError.class)
	public void initWithInconsistentCondition() {
		SimpleCondition c1Toc2 = AttributeEqualityCondition.create(2, 4);
		SimpleCondition c1ToOutOfBounds = AttributeEqualityCondition.create(2, 10);
		Condition conjunct = ConjunctiveCondition.create(new SimpleCondition[]{c1Toc2, c1ToOutOfBounds});
		new DependentJoin(conjunct, child1, child2);
	}

	/**
	 * Inits the with inconsistent Condition2.
	 */
	@Test(expected=AssertionError.class)
	public void initWithInconsistentCondition2() {
		SimpleCondition c1Toc2 = AttributeEqualityCondition.create(2, 4);
		SimpleCondition c1ToOutOfBounds = AttributeEqualityCondition.create(2, -1);
		Condition conjunct = ConjunctiveCondition.create(new SimpleCondition[]{c1Toc2, c1ToOutOfBounds});
		new DependentJoin(conjunct, child1, child2);
	}

	/**
	 * Iterate12.
	 */
	@Test public void iterate12() {
		this.iterator = new DependentJoin(child1, child2);
		this.iterator.open();
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 10; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected12, observed);
	}
	
	/**
	 * Next12 too many.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void next12TooMany() {
		this.iterator = new DependentJoin(child1, child2);
		this.iterator.open();
		for (int i = 0, l = 12; i < l; i++) {
			this.iterator.next();
		}
	}

	/**
	 * Reset12.
	 */
	@Test public void reset12() {
		this.iterator = new DependentJoin(child1, child2);
		this.iterator.open();
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 10; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected12, observed);
		
		this.iterator.reset();
		observed.clear();
		for (int i = 0, l = 10; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected12, observed);
	}

	/**
	 * Iterate21 unbound.
	 */
	@Test(expected=IllegalStateException.class) 
	public void iterate21Unbound() {
		this.iterator = new DependentJoin(child2, child1);
		this.iterator.open();
		this.iterator.next();  
	}

	/**
	 * Iterate21 illegal binding.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void iterate21IllegalBinding() {
		this.iterator = new DependentJoin(child2, child1);
		this.iterator.open();
		this.iterator.bind(TupleType.DefaultFactory.create(String.class).createTuple("A"));
	}

	/**
	 * Iterate21.
	 */
	@Test public void iterate21() {
		this.iterator = new DependentJoin(child2, child1);
		this.iterator.open();
		Set<Tuple> observed = new LinkedHashSet<>();
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(1));
		for (int i = 0, l = 9; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(2));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(3));
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected21, observed);
	}
	
	/**
	 * Next21 too many.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void next21TooMany() {
		this.iterator = new DependentJoin(child2, child1);
		this.iterator.open();
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(3));
		this.iterator.next();
	}

	/**
	 * Reset21.
	 */
	@Test public void reset21() {
		this.iterator = new DependentJoin(child2, child1);
		this.iterator.open();
		Set<Tuple> observed = new LinkedHashSet<>();
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(1));
		for (int i = 0, l = 9; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(2));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(3));
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected21, observed);
		
		this.iterator.reset();
		observed.clear();
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(1));
		for (int i = 0, l = 9; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(2));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(3));
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected21, observed);
	}

	/**
	 * Iterate34 unbound.
	 */
	@Test(expected=IllegalStateException.class) 
	public void iterate34Unbound() {
		this.iterator = new DependentJoin(child3, child4);
		this.iterator.open();
		this.iterator.next();  
	}

	/**
	 * Iterate34 illegal binding.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void iterate34IllegalBinding() {
		this.iterator = new DependentJoin(child3, child4);
		this.iterator.open();
		this.iterator.bind(TupleType.DefaultFactory.create(String.class).createTuple("X"));
	}

	/**
	 * Iterate34.
	 */
	@Test public void iterate34() {
		this.iterator = new DependentJoin(child3, child4);
		this.iterator.open();
		Set<Tuple> observed = new LinkedHashSet<>();
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "A", 10));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "B", 10));
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("B", "A", 10));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "A", 10));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "A", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("B", "A", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "A", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "B", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "C", 20));
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("B", "B", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "B", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "C", 20));
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected34, observed);
	}
	
	/**
	 * Next34 too many.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void next34TooMany() {
		this.iterator = new DependentJoin(child3, child4);
		this.iterator.open();
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("X", "X", 20));
		this.iterator.next();
	}

	/**
	 * Reset34.
	 */
	@Test public void reset34() {
		this.iterator = new DependentJoin(child3, child4);
		this.iterator.open();
		Set<Tuple> observed = new LinkedHashSet<>();
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "A", 10));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "B", 10));
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("B", "A", 10));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "A", 10));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "A", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("B", "A", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "A", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "B", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "C", 20));
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("B", "B", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "B", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "C", 20));
		Assert.assertFalse(this.iterator.hasNext());
		
		this.iterator.reset();
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "A", 10));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "B", 10));
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("B", "A", 10));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "A", 10));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "A", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("B", "A", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "A", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "B", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("A", "C", 20));
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("B", "B", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "B", 20));
		Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("C", "C", 20));
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Iterate natural with empty child.
	 */
	@Test public void iterateNaturalWithEmptyChild() {
		this.iterator = new DependentJoin(child1, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
	}

	/**
	 * Next natural with empty child too many.
	 */
	@Test(expected=NoSuchElementException.class) 
	public void nextNaturalWithEmptyChildTooMany() {
		this.iterator = new DependentJoin(child1, new EmptyIterator());
		this.iterator.open();
		this.iterator.next(); 
	}

	/**
	 * Reset natural with empty child.
	 */
	@Test public void resetNaturalWithEmptyChild() {
		this.iterator = new DependentJoin(child1, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
		
		this.iterator.reset();
		Assert.assertFalse(this.iterator.hasNext());
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.runtime.exec.iterator.TupleIteratorTest#getIterator()
	 */
	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
}