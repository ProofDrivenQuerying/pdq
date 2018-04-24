
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
		Relation relation = new Relation("test",new Attribute[] {a,b,c});
		AccessMethod am1 = method_0.getMethod(relation);
// AccessMethods are not cached any more.
//		Relation relationB = new Relation("test1",new Attribute[] {a,b,c});
		
//		AccessMethod am2 = method_0.getMethod(relationB);
//		AccessMethod am3= method_0.getMethod(relation);
		
		// Test that 2 AccessMethods with the same name will be the same
//		if (am1 != am3) {
//			Assert.fail("AccessMethod cache does not provide same reference");
//		}
//		
//		// Test that 2 Access methods with different names will not be the same
//		if (am1 == am2) {
//			Assert.fail("AccessMethod cache provides same reference when it shouldn't");
//		}
		
		// Test that the name is as we would expect
		Assert.assertEquals("test",am1.getName());
	}
}
