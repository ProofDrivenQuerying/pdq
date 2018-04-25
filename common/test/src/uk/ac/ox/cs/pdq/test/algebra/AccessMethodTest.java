
package uk.ac.ox.cs.pdq.test.algebra;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the AccessMethod class, and if we created it using the cache or not.
 * @author Gabor
 *
 */
public class AccessMethodTest extends PdqTest {

	public AccessMethodTest() {
	}
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		PdqTest.reInitalize(this);
	}

	/**
	 * Test number of relations.
	 */
	@Test
	public void testCreation() {
		Relation relation = Relation.create("test",new Attribute[] {a,b,c});
		AccessMethod am1 = AccessMethod.create("am", new Integer[] {0});
		Relation relationB = Relation.create("test1",new Attribute[] {a,b,c});
		
		AccessMethod am2 = AccessMethod.create("am2", new Integer[] {0});
		AccessMethod am3 = AccessMethod.create("am", new Integer[] {0});
		
		// Test that 2 AccessMethods with the same name will be the same
		if (am1 != am3) {
			Assert.fail("AccessMethod cache does not provide same reference");
		}
		
		// Test that 2 Access methods with different names will not be the same
		if (am1 == am2) {
			Assert.fail("AccessMethod cache provides same reference when it shouldn't");
		}
		
		// Test that the name is as we would expect
		Assert.assertEquals("am",am1.getName());
	}
}
