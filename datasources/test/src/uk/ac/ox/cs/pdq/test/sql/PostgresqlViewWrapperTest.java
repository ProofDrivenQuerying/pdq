package uk.ac.ox.cs.pdq.test.sql;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Julien Leblay
 */
@Ignore
public final class PostgresqlViewWrapperTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void addAccessMethod() {
	}
}
