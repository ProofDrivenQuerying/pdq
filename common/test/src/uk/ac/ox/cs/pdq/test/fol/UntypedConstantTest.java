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
	 * Test skolem is skolem.
	 */
	@Test public void testUntypedConstantIsUntypedConstant() {
		Assert.assertTrue("UntypedConstant.isUntypedConstant must be always true", UntypedConstant.create("v").isUntypedConstant());
	}

	/**
	 * Test skolem is not variable.
	 */
	@Test public void testUntypedConstantIsNotVariable() {
		Assert.assertFalse("UntypedConstant.isVariable must be always false", UntypedConstant.create("v").isVariable());
	}

	/**
	 * Test skolem valid.
	 */
	@Test public void testUntypedConstantValid() {
		UntypedConstant v = UntypedConstant.create("v");
		Assert.assertEquals("UntypedConstant must have name 'v'", "v", v.getSymbol());
	}

	/**
	 * Test skolem empty name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testUntypedConstantEmptyName() {
		UntypedConstant.create("");
	}

	/**
	 * Test skolem null name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testUntypedConstantNullName() {
		UntypedConstant.create(null);
	}

	/**
	 * Test hash code.
	 */
	@Test public void testHashCode() {
		int n = 100;
		HashSet<UntypedConstant> terms = new HashSet<>();
		for (int i = 0; i < n; i++) {
			terms.add(UntypedConstant.create("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(UntypedConstant.create("x" + i)));
		}
		for (int i = 0; i < n; i++) {
			terms.add(UntypedConstant.create("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(UntypedConstant.create("x" + i)));
		}
	}

	/**
	 * Test equals.
	 */
	@Test public void testEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertEquals(UntypedConstant.create("x" + i), UntypedConstant.create("x" + i));
		}
	}

	/**
	 * Test not equals.
	 */
	@Test public void testNotEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertNotEquals(UntypedConstant.create("x" + i), UntypedConstant.create("y" + i));
		}
	}
}
