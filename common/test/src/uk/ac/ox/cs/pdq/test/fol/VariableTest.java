// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.fol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

public class VariableTest {
	
	@Before 
	public void setup() {
		PdqTest.assertsEnabled();
	}

	// Assert that Variable.isVariable is true
	@Test public void testVariableIsVariable() {
		Assert.assertTrue("Variable.isVariable must be always true", Variable.create("v").isVariable());
	}

	// Assert that Variable.isUntypedConstant is false
	@Test public void testVariableIsNotSkolem() {
		Assert.assertFalse("Variable.isSkolem must be always false", Variable.create("v").isUntypedConstant());
	}

	// Assert that Variable.getSymbol is name
	@Test public void testVariableValid() {
		Variable v = Variable.create("v");
		Assert.assertEquals("Variable must have name 'v'", "v", v.getSymbol());
	}

	// Creates a variable with empty name then expects an IllegalArgumentsException
	@Test(expected=IllegalArgumentException.class)
	public void testVariableEmptyName() {
		Variable.create("");
	}

	// Creates a variable with null name then expects an IllegalArgumentsException
	@Test(expected=IllegalArgumentException.class)
	public void testVariableNullName() {
		Variable.create(null);
	}

	// Creates a has set on uniquely named variables then checks they are there
	@Test public void testHashCode() {
		int n = 100;
		HashSet<Variable> terms = new HashSet<>();
		for (int i = 0; i < n; i++) {
			terms.add(Variable.create("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(Variable.create("x" + i)));
		}
		for (int i = 0; i < n; i++) {
			terms.add(Variable.create("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(Variable.create("x" + i)));
		}
	}

	// Assert that 2 variables with the same name are the same
	@Test public void testEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertEquals(Variable.create("x" + i), Variable.create("x" + i));
		}
	}

	// Assert that 2 variables with different names are different
	@Test public void testNotEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertNotEquals(Variable.create("x" + i), Variable.create("y" + i));
		}
	}
}
