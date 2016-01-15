package uk.ac.ox.cs.pdq.test.services.logicblox.rewrite;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Julien Leblay
 *
 */
public class DeskolemizerTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void rewrite() {}
	
	@Test public void map() {}
	
	@Test public void staticQueryDeskolemizerRewrite() {}
	
	@Test public void staticImplicationDeskolemizerRewrite() {}
	
	@Test public void staticConjunctionDeskolemizerRewrite() {}
	
	@Test public void staticDisjunctionDeskolemizerRewrite() {}
	
	@Test public void staticNegationDeskolemizerRewrite() {}
	
	@Test public void staticAtomDeskolemizerRewrite() {}
}
