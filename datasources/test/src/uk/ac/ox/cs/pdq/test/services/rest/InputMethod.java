package uk.ac.ox.cs.pdq.test.services.rest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Julien Leblay
 */
@Ignore
public class InputMethod {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void compoundKey() {}
	
	@Test public void format() {}
	
	@Test public void getName() {}
	
	@Test public void getParameterizedName() {}

	@Test public void getType() {}

	@Test public void getDefaultValue() {}

	@Test public void getBatchDelimiter() {}

	@Test public void getBatchSize() {}
}
