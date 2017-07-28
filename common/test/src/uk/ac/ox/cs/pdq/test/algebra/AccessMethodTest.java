package uk.ac.ox.cs.pdq.test.algebra;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.util.Utility;

public class AccessMethodTest {

	public AccessMethodTest() {
	}
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	/**
	 * Test number of relations.
	 */
	@Test
	public void testCreation() {
		AccessMethod am1 = AccessMethod.create("test",new Integer[] {0});
		AccessMethod am2 = AccessMethod.create("test1",new Integer[] {0});
		AccessMethod am3= AccessMethod.create("test",new Integer[] {0});
		
		if (am1 != am3) { // ATTENTIONAL! it have to be the same reference
			Assert.fail("AccessMethod cache does not provide same reference");
		}
		if (am1 == am2) { // ATTENTIONAL! it have to be the same reference
			Assert.fail("AccessMethod cache provides same reference when it shouldn't");
		}
		
		Assert.assertEquals("test",am1.getName());
	}
}
