package uk.ac.ox.cs.pdq.test.fol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

public class VariableTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	@Test public void testVariableIsVariable() {
		Assert.assertTrue("Variable.isVariable must be always true", new Variable("v").isVariable());
	}

	@Test public void testVariableIsNotSkolem() {
		Assert.assertFalse("Variable.isSkolem must be always false", new Variable("v").isSkolem());
	}

	@Test public void testVariableValid() {
		Variable v = new Variable("v");
		Assert.assertEquals("Variable must have name 'v'", "v", v.getName());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testVariableEmptyName() {
		new Variable("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testVariableNullName() {
		new Variable(null);
	}

	@Test public void testHashCode() {
		int n = 100;
		HashSet<Variable> terms = new HashSet<>();
		for (int i = 0; i < n; i++) {
			terms.add(new Variable("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(new Variable("x" + i)));
		}
		for (int i = 0; i < n; i++) {
			terms.add(new Variable("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(new Variable("x" + i)));
		}
	}

	@Test public void testEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertEquals(new Variable("x" + i), new Variable("x" + i));
		}
	}

	@Test public void testNotEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertNotEquals(new Variable("x" + i), new Variable("y" + i));
		}
	}
}
