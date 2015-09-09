package uk.ac.ox.cs.pdq.test.algebra.predicates;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.db.DatabasePredicate;
import uk.ac.ox.cs.pdq.db.EntityRelation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 *
 * @author Julien Leblay
 */
public class ConstantEqualityPredicateTest {
	
	ConstantEqualityPredicate pred;
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
	
	@Test public void initConstantEqualityPredicateTest() {
		pred = new ConstantEqualityPredicate(0, new TypedConstant<>(0)) ;
		Assert.assertEquals("Initial parameters are found in getters", 0, pred.getPosition());
		Assert.assertEquals("Initial parameters are found in getters", new TypedConstant<>(0), pred.getValue());
		pred = new ConstantEqualityPredicate(0, new TypedConstant<>("0")) ;
		Assert.assertEquals("Initial parameters are found in getters", 0, pred.getPosition());
		Assert.assertEquals("Initial parameters are found in getters", new TypedConstant<>("0"), pred.getValue());
		//TODO: Add test for entity relation constant types
	}
	
	
	@Test(expected=AssertionError.class)
	public void initConstantEqualityPredicateNegativeParameters() {
		new ConstantEqualityPredicate(-1, new TypedConstant<>("0")) ;
	}
	
	@Test
	public void isSatisfiedOnNull() {
	}

	@Test public void isSatisfiedDistinctPositions() {
		pred = new ConstantEqualityPredicate(0, new TypedConstant<>(1)) ;
		Assert.assertTrue("Equality on integers ", pred.isSatisfied(t1));
		pred = new ConstantEqualityPredicate(2, new TypedConstant<>("A")) ;
		Assert.assertTrue("Equality on string ", pred.isSatisfied(t2));
		pred = new ConstantEqualityPredicate(2, null) ;
		Assert.assertTrue("Equality on null value ", pred.isSatisfied(t3));
		//TODO: Add test for entity relation constant types
	}

	@Test public void isNotSatisfied() {
		pred = new ConstantEqualityPredicate(0, new TypedConstant<>(4)) ;
		Assert.assertFalse("Non-equality on integers ", pred.isSatisfied(t1));
		pred = new ConstantEqualityPredicate(0, new TypedConstant<>("B")) ;
		Assert.assertFalse("Non-equality on strings ", pred.isSatisfied(t3));
		//TODO: Add test for entity relation constant types
		pred = new ConstantEqualityPredicate(1, new TypedConstant<>("B")) ;
		Assert.assertFalse("Non-equality across integers and string ", pred.isSatisfied(t2));
		pred = new ConstantEqualityPredicate(1, new TypedConstant<>("0")) ;
		Assert.assertFalse("Non-equality across similar integers and string ", pred.isSatisfied(t3));
		pred = new ConstantEqualityPredicate(1, null) ;
		Assert.assertFalse("Non-equality on null value ", pred.isSatisfied(t3));
	}

	@Test(expected=AssertionError.class)
	public void isSatisfiedWithIllegalArgument() {
		pred = new ConstantEqualityPredicate(10, new TypedConstant<>("0"));
		pred.isSatisfied(t1);
	}

	@Test(expected=AssertionError.class)
	public void isSatisfiedWithIllegalEmptyTupleArgument() {
		pred = new ConstantEqualityPredicate(0, new TypedConstant<>("0"));
		pred.isSatisfied(t5);
	}

	@Test public void equals() {
		pred = new ConstantEqualityPredicate(0, new TypedConstant<>("0")) ;
		Assert.assertEquals("Equality on attribute equality predicates ", pred, new ConstantEqualityPredicate(0, new TypedConstant<>("0")));
		Assert.assertNotEquals("Non-equality on attribute equality predicates ", pred, new ConstantEqualityPredicate(0, new TypedConstant<>(0)));
	}

	@Test public void testHashCode() {
		Set<ConstantEqualityPredicate> s = new LinkedHashSet<>();
		s.add(new ConstantEqualityPredicate(0, new TypedConstant<>("0")));
		Assert.assertEquals("Size of singleton set should be one ", 1, s.size());
		s.add(new ConstantEqualityPredicate(0, new TypedConstant<>("0")));
		Assert.assertEquals("Adding same predicate should not increase set size ", 1, s.size());
		s.add(new ConstantEqualityPredicate(0, new TypedConstant<>(0)));
		Assert.assertEquals("Adding different predicate should increase set size ", 2, s.size());
	}
}
