package uk.ac.ox.cs.pdq.test.services.logicblox;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Julien Leblay
 */
public class ContextTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void getWorkspace() {}

	@Test public void getSchema() {}

	@Test public void addCandidate() {}
	
	@Test public void commit1() {}
	
	@Test public void commit2() {}
	
	@Test public void staticWorkspaceGetName() {}
}
