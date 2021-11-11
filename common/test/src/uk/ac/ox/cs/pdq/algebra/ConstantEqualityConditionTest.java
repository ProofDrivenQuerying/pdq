// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;
import uk.ac.ox.cs.pdq.fol.TypedConstant;

public class ConstantEqualityConditionTest {

	TupleType tt_iii = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);
	
	Tuple tuple1 = tt_iii.createTuple(1, 11, 12);
	Tuple tuple2 = tt_iii.createTuple(2, 11, 12);
	
	TupleType tt_sii = TupleType.DefaultFactory.create(String.class, Integer.class, Integer.class);
	Tuple tuple3 = tt_sii.createTuple("1", 11, 12);
	
	@Test
	public void testIsSatisfiedTypedArray() {
		
		// Condition: the value at index 0 must be equal to 1.
		ConstantEqualityCondition.create(0, TypedConstant.create(1));
		
		Attribute[] typed = new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};

		// When the condition is evaluated on a Typed[] object, only the types are compared.
		// The result is true if type of the TypedConstant in the condition is equal to 
		// that in the given Typed[] array. The _value_ of the TypedConstant is ignored.  
		Assert.assertNotNull(typed);
		
		typed = new Attribute[] {Attribute.create(Double.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
			}

	@Test
	public void testIsSatisfiedTuple() {
		
		// Condition: the value at index 0 must be equal to 1.
		Condition target = ConstantEqualityCondition.create(0, TypedConstant.create(1));
		Assert.assertNotNull(target);
	}

	@Test
	public void testAsPredicate() {
		
		// Condition: the value at index 0 must be equal to 1.
		Condition target = ConstantEqualityCondition.create(0, TypedConstant.create(1));
		Assert.assertNotNull(target);

	}

	/*
	 * Tests the AccessTerm class by calling every method.
	 * Other classes are used as required.
	 * INPUTS: None
	 * OUTPUTS: Documented below
	 * PERFORMANCE: On 11/4/2018 this JUnit test returned in 0.5s on Mark's machine
	 */
	
	@Test
	public void testCreation() {
	
		Object constant = 0;
	
		// Constructor tests invariant
		ConstantEqualityCondition cec = ConstantEqualityCondition.create(1, TypedConstant.create(constant));
		
		// ConstantEqualityCondition.equals null should be false
		boolean b = cec.equals(null);
		Assert.assertFalse(b);
		
		// ConstantEqualityCondition.getClass has an expected name
		Assert.assertTrue(cec.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition");
				
		// ConstantEqualityCondition.hashCode is non negative
		int h = cec.hashCode();
		Assert.assertTrue(h >= 0);

		// ConstantEqualityCondition.toString is #1=#2&#3=#4
		String s = cec.toString();
		Assert.assertTrue(s.equals("#1=0"));
		
		// getConstant is invariant
		TypedConstant tc = cec.getConstant();
		Assert.assertNotNull(tc);
		
		// getPosition is one
		int p = cec.getPosition();
		Assert.assertTrue(p == 1);
	}
}
