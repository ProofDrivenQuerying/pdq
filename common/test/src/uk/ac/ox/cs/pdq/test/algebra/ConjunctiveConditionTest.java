// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.algebra;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the ConjunctiveCondition class.
 * @author Mark Ridler
 *
 */
public class ConjunctiveConditionTest {

	public ConjunctiveConditionTest() {
	}
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		PdqTest.reInitalize(this);
	}

	/*
	 * Tests the AccessTerm class by calling every method.
	 * Other classes are used as required.
	 * INPUTS: None
	 * OUTPUTS: Documented below
	 * PERFORMANCE: On 10/4/2018 this JUnit test returned in 0.5s on Mark's machine
	 */
	
	@Test
	public void testCreation() {
	
		ConstantEqualityCondition aec1 = ConstantEqualityCondition.create(1, TypedConstant.create((Object) 2));
		ConstantEqualityCondition aec2 = ConstantEqualityCondition.create(3, TypedConstant.create((Object) 4));
	    SimpleCondition[] sc = new SimpleCondition[] {aec1, aec2};
		
		// Constructor tests invariant
		ConjunctiveCondition cc = ConjunctiveCondition.create(sc);
		
		// ConjunctiveCondition.equals null should be false
		boolean b = cc.equals(null);
		Assert.assertFalse(b);
		
		// ConjunctiveCondition.getClass has an expected name
		Assert.assertTrue(cc.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition");
				
		// ConjunctiveCondition.hashCode is non negative
		int h = cc.hashCode();
		Assert.assertTrue(h >= 0);

		// ConjunctiveCondition.toString is #1=#2&#3=#4
		String s = cc.toString();
		Assert.assertTrue(s.equals("(#1=2&#3=4)"));
		
		// Number of conjuncts is 2
		int noc = cc.getNumberOfConjuncts();
		Assert.assertTrue(noc == 2);		

		// Simple conditions are 2 as per the input
		SimpleCondition[] sc2 = cc.getSimpleConditions();
		Assert.assertNotNull(sc2);
		Assert.assertTrue(sc2.length == 2);
		Assert.assertArrayEquals(sc, sc2);
	}
}
