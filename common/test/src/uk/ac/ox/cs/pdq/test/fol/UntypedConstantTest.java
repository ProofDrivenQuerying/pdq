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

import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.test.util.PdqTest;


// @author Julien Leblay
public final class UntypedConstantTest {
	
	@Before 
	public void setup() {
		PdqTest.assertsEnabled();
	}

	// Assert that 2 consecutively generated constants are not the same
	@Test public void testNoTwoSimilaryGeneratedUntypedConstants() {
		Assert.assertNotEquals(
				"Two UntypedConstant generated consecutively must not be equals",
				UntypedConstant.getFreshConstant(),
				UntypedConstant.getFreshConstant());
	}


	// Assert that UntypedConstant.isUntypedConstant is true
	@Test public void testUntypedConstantIsUntypedConstant() {
		Assert.assertTrue("UntypedConstant.isUntypedConstant must be always true", UntypedConstant.create("v").isUntypedConstant());
	}

	// Assert that UntypedConstant.isVariable is false
	@Test public void testUntypedConstantIsNotVariable() {
		Assert.assertFalse("UntypedConstant.isVariable must be always false", UntypedConstant.create("v").isVariable());
	}

	// Assert that UntypedConstant.getSymbol is name
	@Test public void testUntypedConstantValid() {
		UntypedConstant v = UntypedConstant.create("v");
		Assert.assertEquals("UntypedConstant must have name 'v'", "v", v.getSymbol());
	}

	// Creates an untyped constant with an empty name and expects an IllegalArgumentsException
	@Test(expected=IllegalArgumentException.class)
	public void testUntypedConstantEmptyName() {
		UntypedConstant.create("");
	}

	// Creates an untyped constant with a null name and expects an IllegalArgumentsException
	@Test(expected=IllegalArgumentException.class)
	public void testUntypedConstantNullName() {
		UntypedConstant.create(null);
	}

	// Creates a hash set of uniquely named untyped constants and check that they are there
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

	// Assert that untyped constants with the same name are the same
	@Test public void testEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertEquals(UntypedConstant.create("x" + i), UntypedConstant.create("x" + i));
		}
	}

	// Assert that untyped constants with different names are different
	@Test public void testNotEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertNotEquals(UntypedConstant.create("x" + i), UntypedConstant.create("y" + i));
		}
	}
}
