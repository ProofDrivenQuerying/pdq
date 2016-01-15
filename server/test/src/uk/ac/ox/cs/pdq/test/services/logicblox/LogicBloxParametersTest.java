package uk.ac.ox.cs.pdq.test.services.logicblox;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Julien Leblay
 */
public class LogicBloxParametersTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void getDefaultFilename() {}
	
	@Test public void getSupportedVersion() {}
	
	@Test public void setSupportedVersion() {}
	
	@Test public void getOptimizationMode() {}
	
	@Test public void setOptimizationMode1() {}
	
	@Test public void setOptimizationMode2() {}
}
