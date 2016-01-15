package uk.ac.ox.cs.pdq.test.services.logicblox;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Julien Leblay
 *
 */
public class SemanticOptimizationServiceTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void getContextRepository() {}

	@Test public void resolve() {}
	
	@Test public void stop() {}

	@Test public void status() {}

	@Test public void getName() {}

	@Test public void run() {}

	@Test public void synchronizeDefaultServiceCall() {}
		
	@Test public void optimizeDefaultServiceCall() {}

	@Test public void callDefaultServiceCall() {}
}
