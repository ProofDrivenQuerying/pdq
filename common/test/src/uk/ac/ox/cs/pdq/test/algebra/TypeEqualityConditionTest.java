package uk.ac.ox.cs.pdq.test.algebra;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Predicate;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.TypeEqualityCondition;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import uk.ac.ox.cs.pdq.db.Attribute;


public class TypeEqualityConditionTest {

	@Test
	public void testIsSatisfiedTypedArray() {
		
		// Condition: types in indices 0 and 2 must be equal.
		Condition target = TypeEqualityCondition.create(0, 2);
		
		Typed[] typed = new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
		
		Assert.assertFalse(target.isSatisfied(typed));
		
		typed = new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Double.class, "b"), Attribute.create(Integer.class, "c")};
		
		Assert.assertTrue(target.isSatisfied(typed));
		
		// Condition: types in indices 0 and 1 must be equal.
		target = TypeEqualityCondition.create(0, 1);
		
		typed = new Attribute[] {Attribute.create(String.class, "a"),
				Attribute.create(String.class, "b"), Attribute.create(Integer.class, "c")};
		
		Assert.assertTrue(target.isSatisfied(typed));
		
		typed = new Attribute[] {Attribute.create(String.class, "a"),
				Attribute.create(Double.class, "b"), Attribute.create(Integer.class, "c")};
		
		Assert.assertFalse(target.isSatisfied(typed));
	}

	@Test
	public void testIsSatisfiedTuple() {
		
		// Condition: types in indices 0 and 2 must be equal.
		Condition target = TypeEqualityCondition.create(0, 2);

		TupleType tt_iis = TupleType.DefaultFactory.create(Integer.class, Integer.class, String.class);
		Tuple tuple = tt_iis.createTuple(10, 11, "12");
		
		Assert.assertFalse(target.isSatisfied(tuple));
		
		TupleType tt_isi = TupleType.DefaultFactory.create(Integer.class, String.class, Integer.class);
		tuple = tt_isi.createTuple(10, "11", 12);
		
		Assert.assertTrue(target.isSatisfied(tuple));
	}

	@Test
	public void testAsPredicate() {
		
		// Condition: types in indices 1 and 2 must be equal.
		Condition target = TypeEqualityCondition.create(1, 2);
		
		Predicate<Tuple> result = target.asPredicate();
		
		TupleType tt_iis = TupleType.DefaultFactory.create(Integer.class, Integer.class, String.class);
		Tuple tuple = tt_iis.createTuple(10, 11, "12");

		Assert.assertFalse(result.test(tuple));
		
		TupleType tt_iss = TupleType.DefaultFactory.create(Integer.class, String.class, String.class);
		tuple = tt_iss.createTuple(10, "11", "12");
		
		Assert.assertTrue(result.test(tuple));
	}
}
