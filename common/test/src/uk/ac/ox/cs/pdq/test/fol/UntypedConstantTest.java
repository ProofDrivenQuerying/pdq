package uk.ac.ox.cs.pdq.test.fol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.util.Utility;


// TODO: Auto-generated Javadoc
/**
 * The Class UntypedConstantTest.
 *
 * @author Julien Leblay
 */
public final class UntypedConstantTest {
	
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
	@Test public void testNoTwoSimilaryGeneratedUntypedConstants() {
		Assert.assertNotEquals(
				"Two UntypedConstant generated consecutively must not be equals",
				UntypedConstant.getFreshConstant(),
				UntypedConstant.getFreshConstant());
	}

	/**
	 * Test generated skolems similar after reset.
	 */
	@Test public void testGeneratedUntypedConstantsSimilarAfterReset() {
		UntypedConstant.resetCounter();
		UntypedConstant s1 = UntypedConstant.getFreshConstant();
		UntypedConstant.resetCounter();
		UntypedConstant s2 = UntypedConstant.getFreshConstant();
		Assert.assertEquals(
				"Two UntypedConstant generated right after a reset must be equal",
				s1, s2);
	}


	/**
	 * Test skolem is skolem.
	 */
	@Test public void testUntypedConstantIsUntypedConstant() {
		Assert.assertTrue("UntypedConstant.isUntypedConstant must be always true", new UntypedConstant("v").isUntypedConstant());
	}

	/**
	 * Test skolem is not variable.
	 */
	@Test public void testUntypedConstantIsNotVariable() {
		Assert.assertFalse("UntypedConstant.isVariable must be always false", new UntypedConstant("v").isVariable());
	}

	/**
	 * Test skolem valid.
	 */
	@Test public void testUntypedConstantValid() {
		UntypedConstant v = new UntypedConstant("v");
		Assert.assertEquals("UntypedConstant must have name 'v'", "v", v.getSymbol());
	}

	/**
	 * Test skolem empty name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testUntypedConstantEmptyName() {
		new UntypedConstant("");
	}

	/**
	 * Test skolem null name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testUntypedConstantNullName() {
		new UntypedConstant(null);
	}

	/**
	 * Test hash code.
	 */
	@Test public void testHashCode() {
		int n = 100;
		HashSet<UntypedConstant> terms = new HashSet<>();
		for (int i = 0; i < n; i++) {
			terms.add(new UntypedConstant("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(new UntypedConstant("x" + i)));
		}
		for (int i = 0; i < n; i++) {
			terms.add(new UntypedConstant("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(new UntypedConstant("x" + i)));
		}
	}

	/**
	 * Test equals.
	 */
	@Test public void testEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertEquals(new UntypedConstant("x" + i), new UntypedConstant("x" + i));
		}
	}

	/**
	 * Test not equals.
	 */
	@Test public void testNotEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertNotEquals(new UntypedConstant("x" + i), new UntypedConstant("y" + i));
		}
	}
}
