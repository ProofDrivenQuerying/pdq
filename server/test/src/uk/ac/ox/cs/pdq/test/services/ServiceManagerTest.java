package uk.ac.ox.cs.pdq.test.services;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Julien Leblay
 */
public class ServiceManagerTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void start() {}
	
	@Test public void status() {}
	
	@Test public void stop1() {}

	@Test public void stop2() {}
	
	@Test public void run() {}

	@Test public void getName() {}
}
