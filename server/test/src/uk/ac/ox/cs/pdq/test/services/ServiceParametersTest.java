package uk.ac.ox.cs.pdq.test.services;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Julien LEBLAY
 */
public class ServiceParametersTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void getDefaultFilename() {}

	@Test public void getServiceName() {}
	
	@Test public void setServiceName() {}
	
	@Test public void getPort() {}
	
	@Test public void setPort1() {}
	
	@Test public void setPort2() {}
}
