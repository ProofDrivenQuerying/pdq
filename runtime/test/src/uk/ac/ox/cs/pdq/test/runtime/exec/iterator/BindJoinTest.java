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

import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.wrappers.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.BindJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.EmptyIterator;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Scan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TopDownAccess;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.collect.Lists;

/**
 */
public class BindJoinTest extends NaryIteratorTest {

	BindJoin iterator;
	List<Typed> out12, out21, in21, out34, in34;
	AccessMethod free ;
	AccessMethod mt1;
	AccessMethod mt2;
	AccessMethod mt3;
	InMemoryTableWrapper rel1;
	InMemoryTableWrapper rel2;
	Set<Tuple> expected12, expected21, expected34;

	@Before public void setup() {
        MockitoAnnotations.initMocks(this);
		d = new Attribute(String.class, "d");
		out12 = Lists.newArrayList(a, b, c, d, c, e);
		out21 = Lists.newArrayList(c, e, a, b, c, d);
		in21 = Lists.newArrayList(c);
		out34 = Lists.newArrayList(c, e, a, b, c, d); 
		in34 = Lists.newArrayList(e, a, b);
        // Using mocks for test the bind join is tricky because of the complex
        // uses of hasNext and next methods. Reverting to memory scans below.
        free = new AccessMethod();
        mt1 = new AccessMethod("m1", Types.LIMITED, Lists.newArrayList(1));
        mt2 = new AccessMethod("m2", Types.LIMITED, Lists.newArrayList(2));
        mt3 = new AccessMethod("m3", Types.LIMITED, Lists.newArrayList(1, 2));
        rel1 = new InMemoryTableWrapper("R1", 
        		Lists.newArrayList((Attribute) a, (Attribute) b, (Attribute) c, (Attribute) d),
	    		Lists.newArrayList(free, mt3));
        rel1.load(Lists.newArrayList(
        		child1Type.createTuple("A", 10, 1, "D"),
        		child1Type.createTuple("A", 10, 2, "D"),
        		child1Type.createTuple("A", 20, 1, "D"),
        		child1Type.createTuple("B", 20, 1, "D")));
        rel2 = new InMemoryTableWrapper("R2", 
        		Lists.newArrayList((Attribute) c, (Attribute) e),
        		Lists.newArrayList(mt1, mt2));
        rel2.load(Lists.newArrayList(
        		child2Type.createTuple(1, "A"),
        		child2Type.createTuple(2, "B"),
        		child2Type.createTuple(1, "B"),
        		child2Type.createTuple(1, "C")));
        child1 = new Scan(rel1);
		child2 = new TopDownAccess(rel2, mt1);
		child3 = new TopDownAccess(rel2, mt2);
		child4 = new TopDownAccess(rel1, mt3);

        this.iterator = new BindJoin(child1, child2);

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
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren1() {
        new BindJoin(child4, null);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initNullChildren2() {
        new BindJoin(null, child4);
	}

	@Test public void initTwoChildren12() {
		Predicate natural = new ConjunctivePredicate<>(new AttributeEqualityPredicate(2, 4));
		this.iterator = new BindJoin(child1, child2);
		Assert.assertEquals("BindJoin iterator columns must match that of the concatenation of its children", out12, this.iterator.getColumns());
		Assert.assertEquals("BindJoin iterator type must match that of the concatenation of its children", TupleType.DefaultFactory.createFromTyped(out12), this.iterator.getType());
		Assert.assertEquals("BindJoin iterator input columns must match that of the concatenation of its children", Collections.EMPTY_LIST, this.iterator.getInputColumns());
		Assert.assertEquals("BindJoin iterator input type must match that of the concatenation of its children", TupleType.EmptyTupleType, this.iterator.getInputType());
		Assert.assertEquals("BindJoin iterator children must match that of initialization", Lists.newArrayList(child1, child2), this.iterator.getChildren());
		Assert.assertEquals("BindJoin iterator predicate must match that of natural join", natural, this.iterator.getPredicate());
	}

	@Test public void initTwoChildren21() {
		Predicate natural = new ConjunctivePredicate<>(new AttributeEqualityPredicate(0, 4));
		this.iterator = new BindJoin(child2, child1);
		Assert.assertEquals("BindJoin iterator columns must match that of the concatenation of its children", out21, this.iterator.getColumns());
		Assert.assertEquals("BindJoin iterator type must match that of the concatenation of its children", TupleType.DefaultFactory.createFromTyped(out21), this.iterator.getType());
		Assert.assertEquals("BindJoin iterator input columns must match that of the concatenation of its children", in21, this.iterator.getInputColumns());
		Assert.assertEquals("BindJoin iterator input type must match that of the concatenation of its children", TupleType.DefaultFactory.createFromTyped(in21), this.iterator.getInputType());
		Assert.assertEquals("BindJoin iterator children must match that of initialization", Lists.newArrayList(child2, child1), this.iterator.getChildren());
		Assert.assertEquals("BindJoin iterator predicate must match that of natural join", natural, this.iterator.getPredicate());
	}

	@Test public void initTwoChildren34() {
		Predicate natural = new ConjunctivePredicate<>(new AttributeEqualityPredicate(0, 4));
		this.iterator = new BindJoin(child3, child4);
		Assert.assertEquals("BindJoin iterator columns must match that of the concatenation of its children", out34, this.iterator.getColumns());
		Assert.assertEquals("BindJoin iterator type must match that of the concatenation of its children", TupleType.DefaultFactory.createFromTyped(out34), this.iterator.getType());
		Assert.assertEquals("BindJoin iterator input columns must match that of the concatenation of its children", in34, this.iterator.getInputColumns());
		Assert.assertEquals("BindJoin iterator input type must match that of the concatenation of its children", TupleType.DefaultFactory.createFromTyped(in34), this.iterator.getInputType());
		Assert.assertEquals("BindJoin iterator children must match that of initialization", Lists.newArrayList(child3, child4), this.iterator.getChildren());
		Assert.assertEquals("BindJoin iterator predicate must match that of natural join", natural, this.iterator.getPredicate());
	}

	@Test(expected=AssertionError.class)
	public void initWithInconsistentPredicate() {
		Predicate c1Toc2 = new AttributeEqualityPredicate(2, 4);
		Predicate c1ToOutOfBounds = new AttributeEqualityPredicate(2, 10);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(c1Toc2, c1ToOutOfBounds));
		new BindJoin(conjunct, child1, child2);
	}

	@Test(expected=AssertionError.class)
	public void initWithInconsistentPredicate2() {
		Predicate c1Toc2 = new AttributeEqualityPredicate(2, 4);
		Predicate c1ToOutOfBounds = new AttributeEqualityPredicate(2, -1);
		Predicate conjunct = new ConjunctivePredicate<>(Lists.newArrayList(c1Toc2, c1ToOutOfBounds));
		new BindJoin(conjunct, child1, child2);
	}

	@Test public void iterate12() {
		this.iterator = new BindJoin(child1, child2);
		this.iterator.open();
		Set<Tuple> observed = new LinkedHashSet<>();
		for (int i = 0, l = 10; i < l; i++) {
			Assert.assertTrue(this.iterator.hasNext()); observed.add(this.iterator.next());  
		}
		Assert.assertFalse(this.iterator.hasNext());
		Assert.assertEquals(expected12, observed);
	}
	
	@Test(expected=NoSuchElementException.class) 
	public void next12TooMany() {
		this.iterator = new BindJoin(child1, child2);
		this.iterator.open();
		for (int i = 0, l = 12; i < l; i++) {
			this.iterator.next();
		}
	}

	@Test public void reset12() {
		this.iterator = new BindJoin(child1, child2);
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

	@Test(expected=IllegalStateException.class) 
	public void iterate21Unbound() {
		this.iterator = new BindJoin(child2, child1);
		this.iterator.open();
		this.iterator.next();  
	}

	@Test(expected=IllegalArgumentException.class) 
	public void iterate21IllegalBinding() {
		this.iterator = new BindJoin(child2, child1);
		this.iterator.open();
		this.iterator.bind(TupleType.DefaultFactory.create(String.class).createTuple("A"));
	}

	@Test public void iterate21() {
		this.iterator = new BindJoin(child2, child1);
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
	
	@Test(expected=NoSuchElementException.class) 
	public void next21TooMany() {
		this.iterator = new BindJoin(child2, child1);
		this.iterator.open();
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in21).createTuple(3));
		this.iterator.next();
	}

	@Test public void reset21() {
		this.iterator = new BindJoin(child2, child1);
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

	@Test(expected=IllegalStateException.class) 
	public void iterate34Unbound() {
		this.iterator = new BindJoin(child3, child4);
		this.iterator.open();
		this.iterator.next();  
	}

	@Test(expected=IllegalArgumentException.class) 
	public void iterate34IllegalBinding() {
		this.iterator = new BindJoin(child3, child4);
		this.iterator.open();
		this.iterator.bind(TupleType.DefaultFactory.create(String.class).createTuple("X"));
	}

	@Test public void iterate34() {
		this.iterator = new BindJoin(child3, child4);
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
	
	@Test(expected=NoSuchElementException.class) 
	public void next34TooMany() {
		this.iterator = new BindJoin(child3, child4);
		this.iterator.open();
		this.iterator.bind(TupleType.DefaultFactory.createFromTyped(in34).createTuple("X", "X", 20));
		this.iterator.next();
	}

	@Test public void reset34() {
		this.iterator = new BindJoin(child3, child4);
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

	@Test public void iterateNaturalWithEmptyChild() {
		this.iterator = new BindJoin(child1, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
	}

	@Test(expected=NoSuchElementException.class) 
	public void nextNaturalWithEmptyChildTooMany() {
		this.iterator = new BindJoin(child1, new EmptyIterator());
		this.iterator.open();
		this.iterator.next(); 
	}

	@Test public void resetNaturalWithEmptyChild() {
		this.iterator = new BindJoin(child1, new EmptyIterator());
		this.iterator.open();
		Assert.assertFalse(this.iterator.hasNext());
		
		this.iterator.reset();
		Assert.assertFalse(this.iterator.hasNext());
	}
	
	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
}