package uk.ac.ox.cs.pdq.test.runtime.util;

import org.junit.Before;
import org.junit.Ignore;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Prints tuple to the given print stream, if provided, log.info otherwise.
 * 
 * @author Julien Leblay
 */
@Ignore
public class TuplePrinterTest implements EventHandler {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
}
