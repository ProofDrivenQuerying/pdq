// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.algebra;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

// Tests the AccessMethod class, and if we created it using the cache or not.
// @author Gabor
public class AccessMethodTest {

	public AccessMethodTest() {
	}

	// Makes sure assertions are enabled.
	@Before
	public void setup() {
		PdqTest.reInitalize(this);
	}

	// Test number of relations.
	@Test
	public void testCreation() {
		AccessMethodDescriptor am1 = AccessMethodDescriptor.create("am1", new Integer[] { 0 });
		AccessMethodDescriptor am2 = AccessMethodDescriptor.create("am2", new Integer[] { 0 });
		AccessMethodDescriptor am3 = AccessMethodDescriptor.create("am1", new Integer[] { 0 });

		// Test that 2 AccessMethods with the same name will be the same
		if (am1 != am3) {
			Assert.fail("AccessMethod cache does not provide same reference");
		}
		System.out.println("java version = " + System.getProperty("java.version"));
		// Test that 2 Access methods with different names will not be the same
		if (am1 == am2) {
			Assert.fail("AccessMethod cache provides same reference when it shouldn't");
		}
		// Test that the name is as we would expect
		Assert.assertEquals("am1", am1.getName());
		if (am1 != am3) { // it have to be two different reference
			Assert.fail("AccessMethod cache does not provide same reference");
		}
		if (am1 == am2) { // it have to be the same reference
			Assert.fail("AccessMethod cache provides same reference when it shouldn't");
		}

		Assert.assertEquals("am1", am1.getName());
	}
}
