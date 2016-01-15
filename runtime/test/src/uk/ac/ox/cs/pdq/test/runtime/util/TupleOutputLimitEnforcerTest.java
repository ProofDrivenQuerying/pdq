package uk.ac.ox.cs.pdq.test.runtime.util;

import org.junit.Before;
import org.junit.Ignore;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Julien Leblay
 */
@Ignore
public class TupleOutputLimitEnforcerTest implements EventHandler {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
}
