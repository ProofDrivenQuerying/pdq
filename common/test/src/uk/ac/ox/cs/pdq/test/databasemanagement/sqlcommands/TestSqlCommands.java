package uk.ac.ox.cs.pdq.test.databasemanagement.sqlcommands;

import org.junit.Test;

import org.junit.Assert;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.BasicSelect;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/** This class has a test case for each command object in the sqlcommands package.
 * @author Gabor
 *
 */
public class TestSqlCommands extends PdqTest {

	@Test
	public void testBasicSelect() {
		BasicSelect bs = new BasicSelect(R); 
		Assert.assertNotNull(bs.getFormula());
	}
	
	@Test
	public void testBulkInsert() {
		Assert.assertNotNull(null);
	}
	@Test
	public void testCommand() {
		Assert.assertNotNull(null);
	}
	@Test
	public void testCreateDatabase() {
		Assert.assertNotNull(null);
	}
	@Test
	public void testCreateTable() {
		Assert.assertNotNull(null);
	}
	@Test
	public void testDelete() {
		Assert.assertNotNull(null);
	}
	@Test
	public void testDifferenceQuery() {
		Assert.assertNotNull(null);
	}
	@Test
	public void testDropDatabase() {
		Assert.assertNotNull(null);
	}
	@Test
	public void testInsert() {
		Assert.assertNotNull(null);
	}
}
