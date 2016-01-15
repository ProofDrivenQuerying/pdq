package uk.ac.ox.cs.pdq.test.services;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Julien Leblay
 */
public class BootstrapTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void staticGetVersion() {}
	
	@Test public void staticActionConverterConvert() {}
	
	@Test public void staticCommaSeparatedSplitterSplit() {}
}
