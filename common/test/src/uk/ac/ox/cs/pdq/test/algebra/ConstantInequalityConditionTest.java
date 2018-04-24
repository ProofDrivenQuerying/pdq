package uk.ac.ox.cs.pdq.test.algebra;

import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConstantInequalityCondition;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

public class ConstantInequalityConditionTest {

	TupleType tt_iii = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);
	
	Tuple tuple1 = tt_iii.createTuple(1, 11, 12);
	Tuple tuple2 = tt_iii.createTuple(2, 11, 12);
	Tuple tuple0 = tt_iii.createTuple(0, 11, 12);
	
	TupleType tt_sii = TupleType.DefaultFactory.create(String.class, Integer.class, Integer.class);
	Tuple tuple3 = tt_sii.createTuple("1", 11, 12);
	
	@Test
	public void testIsSatisfiedTypedArray() {
		
		// Condition: the value at index 0 must be equal to 1.
		Condition target = ConstantInequalityCondition.create(0, TypedConstant.create(1));
		
		Typed[] typed = new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};

		// When the condition is evaluated on a Typed[] object, only the types are compared.
		// The result is true if type of the TypedConstant in the condition is equal to 
		// that in the given Typed[] array. The _value_ of the TypedConstant is ignored.  
		Assert.assertTrue(target.isSatisfied(typed));
		
		typed = new Attribute[] {Attribute.create(Double.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
		
		Assert.assertFalse(target.isSatisfied(typed));
	}

	@Test
	public void testIsSatisfiedTuple() {
		
		// Condition: the value at index 0 must be less than 2.
		Condition target = ConstantInequalityCondition.create(0, TypedConstant.create(2));
		
		Assert.assertTrue(target.isSatisfied(tuple1));
		Assert.assertFalse(target.isSatisfied(tuple2));

		// Condition: the value at index 0 must be less than 3.
		target = ConstantInequalityCondition.create(0, TypedConstant.create(3));
		
		Assert.assertTrue(target.isSatisfied(tuple1));
		Assert.assertTrue(target.isSatisfied(tuple2));

		// Condition: the value at index 0 must be less than 1.
		target = ConstantInequalityCondition.create(0, TypedConstant.create(1));
		
		Assert.assertFalse(target.isSatisfied(tuple1));
		Assert.assertFalse(target.isSatisfied(tuple2));

		// Condition: the value at index 0 must be greater than 2.
		target = ConstantInequalityCondition.create(0, TypedConstant.create(2), false);
		
		Assert.assertFalse(target.isSatisfied(tuple1));
		Assert.assertFalse(target.isSatisfied(tuple2));
		
		// Condition: the value at index 0 must be greater than 1.
		target = ConstantInequalityCondition.create(0, TypedConstant.create(1), false);
		
		Assert.assertFalse(target.isSatisfied(tuple1));
		Assert.assertTrue(target.isSatisfied(tuple2));
		
		// Condition: the value at index 0 must be greater than 0.
		target = ConstantInequalityCondition.create(0, TypedConstant.create(0), false);
		
		Assert.assertTrue(target.isSatisfied(tuple1));
		Assert.assertTrue(target.isSatisfied(tuple2));
	}

	@Test
	public void testAsPredicate() {
		
		// Condition: the value at index 0 must be equal to 1.
		Condition target = ConstantInequalityCondition.create(0, TypedConstant.create(1));

		Predicate<Tuple> result = target.asPredicate();
		
		// 1 is not less then 1
		Assert.assertFalse(result.test(tuple1));
		// 2 is not less then 1:
		Assert.assertFalse(result.test(tuple2));
		// 0 is less then 1:
		Assert.assertTrue(result.test(tuple0));
	}
}
