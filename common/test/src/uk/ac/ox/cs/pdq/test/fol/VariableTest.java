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

// TODO: Auto-generated Javadoc
/**
 * The Class VariableTest.
 */
public class VariableTest {
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	/**
	 * Test variable is variable.
	 */
	@Test public void testVariableIsVariable() {
		Assert.assertTrue("Variable.isVariable must be always true", Variable.create("v").isVariable());
	}

	/**
	 * Test variable is not skolem.
	 */
	@Test public void testVariableIsNotSkolem() {
		Assert.assertFalse("Variable.isSkolem must be always false", Variable.create("v").isUntypedConstant());
	}

	/**
	 * Test variable valid.
	 */
	@Test public void testVariableValid() {
		Variable v = Variable.create("v");
		Assert.assertEquals("Variable must have name 'v'", "v", v.getSymbol());
	}

	/**
	 * Test variable empty name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testVariableEmptyName() {
		Variable.create("");
	}

	/**
	 * Test variable null name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testVariableNullName() {
		Variable.create(null);
	}

	/**
	 * Test hash code.
	 */
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

	/**
	 * Test equals.
	 */
	@Test public void testEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertEquals(Variable.create("x" + i), Variable.create("x" + i));
		}
	}

	/**
	 * Test not equals.
	 */
	@Test public void testNotEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertNotEquals(Variable.create("x" + i), Variable.create("y" + i));
		}
	}
}
