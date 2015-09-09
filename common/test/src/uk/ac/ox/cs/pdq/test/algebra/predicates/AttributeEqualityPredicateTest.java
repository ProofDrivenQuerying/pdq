package uk.ac.ox.cs.pdq.test.algebra.predicates;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.db.DatabasePredicate;
import uk.ac.ox.cs.pdq.db.EntityRelation;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 *
 * @author Julien Leblay
 */
public class AttributeEqualityPredicateTest {
	
	AttributeEqualityPredicate pred;
	Tuple t1, t2, t3, t4, t5;
	EntityRelation R = new EntityRelation("R"), S = new EntityRelation("S");
	DatabasePredicate r = R.createAtoms(), s = S.createAtoms();
	
	@Before
	public void setup() {
		t1 = TupleType.DefaultFactory.create(
				Integer.class, 
				Integer.class).createTuple(1, 2);
		t2 = TupleType.DefaultFactory.create(
				Integer.class, 
				Integer.class, 
				String.class, 
				String.class).createTuple(
						0, 0, "A", "A");
		t3 = TupleType.DefaultFactory.create(
				String.class, 
				Integer.class, 
				Integer.class, 
				String.class).createTuple(
						"A", 1, null, "1");
		t4 = TupleType.DefaultFactory.create(R, R, S).createTuple(r, r, s);
		t5 = TupleType.DefaultFactory.create().createTuple();
	}
	
	@Test public void initAttributeEqualityPredicateTest() {
		pred = new AttributeEqualityPredicate(0, 0) ;
		Assert.assertEquals("Initial parameters are found in getters", 0, pred.getPosition());
		Assert.assertEquals("Initial parameters are found in getters", 0, pred.getOther());
		
		pred = new AttributeEqualityPredicate(0, 10) ;
		Assert.assertEquals("Initial parameters are found in getters", 0, pred.getPosition());
		Assert.assertEquals("Initial parameters are found in getters", 10, pred.getOther());
		
		pred = new AttributeEqualityPredicate(10, 0) ;
		Assert.assertEquals("Initial parameters are found in getters", 10, pred.getPosition());
		Assert.assertEquals("Initial parameters are found in getters", 0, pred.getOther());
	}
	
	
	@Test(expected=AssertionError.class)
	public void initAttributeEqualityPredicateNegativeParameters() {
		new AttributeEqualityPredicate(0, -1) ;
	}

	@Test public void isSatisfiedSamePositions() {
		pred = new AttributeEqualityPredicate(0, 0) ;
		Assert.assertTrue("Equality on oneself is always satisfied", pred.isSatisfied(t1));
		Assert.assertTrue("Equality on oneself is always satisfied", pred.isSatisfied(t2));
		Assert.assertTrue("Equality on oneself is always satisfied", pred.isSatisfied(t3));
		Assert.assertTrue("Equality on oneself is always satisfied", pred.isSatisfied(t4));
		
		pred = new AttributeEqualityPredicate(2, 2) ;
		Assert.assertTrue("Equality on oneself is always satisfied", pred.isSatisfied(t3));
	}

	@Test public void isSatisfiedDistinctPositions() {
		pred = new AttributeEqualityPredicate(0, 1) ;
		Assert.assertTrue("Equality on integers ", pred.isSatisfied(t2));
		
		pred = new AttributeEqualityPredicate(2, 3) ;
		Assert.assertTrue("Equality on string ", pred.isSatisfied(t2));
		
		pred = new AttributeEqualityPredicate(0, 1) ;
		Assert.assertTrue("Equality on entity relations ", pred.isSatisfied(t4));
	}

	@Test public void isNotSatisfied() {
		pred = new AttributeEqualityPredicate(0, 1) ;
		Assert.assertFalse("Non-equality on integers ", pred.isSatisfied(t1));
		
		pred = new AttributeEqualityPredicate(0, 3) ;
		Assert.assertFalse("Non-equality on strings ", pred.isSatisfied(t3));
		
		pred = new AttributeEqualityPredicate(0, 2) ;
		Assert.assertFalse("Non-equality on entity relations ", pred.isSatisfied(t4));
		
		pred = new AttributeEqualityPredicate(1, 2) ;
		Assert.assertFalse("Non-equality across integers and string ", pred.isSatisfied(t2));
		
		pred = new AttributeEqualityPredicate(1, 3) ;
		Assert.assertFalse("Non-equality across similar integers and string ", pred.isSatisfied(t3));
	}

	@Test(expected=AssertionError.class)
	public void isSatisfiedWithIllegalArgument() {
		pred = new AttributeEqualityPredicate(0, 10);
		pred.isSatisfied(t1);
	}

	@Test(expected=AssertionError.class)
	public void isSatisfiedWithIllegalEmptyTupleArgument() {
		pred = new AttributeEqualityPredicate(0, 0);
		pred.isSatisfied(t5);
	}

	@Test public void equals() {
		pred = new AttributeEqualityPredicate(0, 1) ;
		Assert.assertEquals("Equality on attribute equality predicates ", pred, new AttributeEqualityPredicate(0, 1));
		Assert.assertNotEquals("Non-equality on attribute equality predicates ", pred, new AttributeEqualityPredicate(0, 2));
	}

	@Test public void testHashCode() {
		Set<AttributeEqualityPredicate> s = new LinkedHashSet<>();
		s.add(new AttributeEqualityPredicate(0, 1));
		Assert.assertEquals("Size of singleton set should be one ", 1, s.size());
		
		s.add(new AttributeEqualityPredicate(0, 1));
		Assert.assertEquals("Adding same predicate should not increase set size ", 1, s.size());
		
		s.add(new AttributeEqualityPredicate(0, 2));
		Assert.assertEquals("Adding different predicate should increase set size ", 2, s.size());
	}
}
