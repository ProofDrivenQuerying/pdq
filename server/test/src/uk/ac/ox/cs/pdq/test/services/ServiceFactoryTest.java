package uk.ac.ox.cs.pdq.test.services;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 *  
 * @author Julien Leblay
 *
 */
public class ServiceFactoryTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void staticRegister() {}
	
	@Test public void staticCreate() {}
}
