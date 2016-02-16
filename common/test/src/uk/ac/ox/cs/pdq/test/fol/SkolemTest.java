package uk.ac.ox.cs.pdq.test.fol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.util.Utility;


// TODO: Auto-generated Javadoc
/**
 * The Class SkolemTest.
 *
 * @author Julien Leblay
 */
public final class SkolemTest {
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	/**
	 * Test no two similary generated skolems.
	 */
	@Test public void testNoTwoSimilaryGeneratedSkolems() {
		Assert.assertNotEquals(
				"Two Skolem generated consecutively must not be equals",
				Skolem.getFreshConstant(),
				Skolem.getFreshConstant());
	}

	/**
	 * Test generated skolems similar after reset.
	 */
	@Test public void testGeneratedSkolemsSimilarAfterReset() {
		Skolem.resetCounter();
		Skolem s1 = Skolem.getFreshConstant();
		Skolem.resetCounter();
		Skolem s2 = Skolem.getFreshConstant();
		Assert.assertEquals(
				"Two Skolem generated right after a reset must be equal",
				s1, s2);
	}


	/**
	 * Test skolem is skolem.
	 */
	@Test public void testSkolemIsSkolem() {
		Assert.assertTrue("Skolem.isSkolem must be always true", new Skolem("v").isSkolem());
	}

	/**
	 * Test skolem is not variable.
	 */
	@Test public void testSkolemIsNotVariable() {
		Assert.assertFalse("Skolem.isVariable must be always false", new Skolem("v").isVariable());
	}

	/**
	 * Test skolem valid.
	 */
	@Test public void testSkolemValid() {
		Skolem v = new Skolem("v");
		Assert.assertEquals("Skolem must have name 'v'", "v", v.getName());
	}

	/**
	 * Test skolem empty name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testSkolemEmptyName() {
		new Skolem("");
	}

	/**
	 * Test skolem null name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testSkolemNullName() {
		new Skolem(null);
	}

	/**
	 * Test hash code.
	 */
	@Test public void testHashCode() {
		int n = 100;
		HashSet<Skolem> terms = new HashSet<>();
		for (int i = 0; i < n; i++) {
			terms.add(new Skolem("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(new Skolem("x" + i)));
		}
		for (int i = 0; i < n; i++) {
			terms.add(new Skolem("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(new Skolem("x" + i)));
		}
	}

	/**
	 * Test equals.
	 */
	@Test public void testEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertEquals(new Skolem("x" + i), new Skolem("x" + i));
		}
	}

	/**
	 * Test not equals.
	 */
	@Test public void testNotEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertNotEquals(new Skolem("x" + i), new Skolem("y" + i));
		}
	}
}
