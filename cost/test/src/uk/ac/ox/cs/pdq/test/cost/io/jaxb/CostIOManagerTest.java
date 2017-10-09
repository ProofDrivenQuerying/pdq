package uk.ac.ox.cs.pdq.test.cost.io.jaxb;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the CostIOManager's functions by creating a new cost file and then
 * attempts to parse it back. Other inherited functions of the IOManager like
 * importSchema is not tested in this class.
 * 
 * @author Gabor
 *
 */
public class CostIOManagerTest extends PdqTest {

	/**
	 * Attempt to write to the file system a cost file that we created manually.
	 * Asserts the existence of the new file and if it contains data.
	 */
	@Test
	public void testWriteCost() {
		try {
			File cost = createCostFile();
			Assert.assertTrue(cost.exists());
			Assert.assertTrue(cost.length() > 430);
			cost.delete();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	private File createCostFile() throws JAXBException, IOException {
		Cost c = new DoubleCost(2.555);
		File out = File.createTempFile("ProjectionTermWithCost","xml");
		RelationalTerm plan = AccessTerm.create(testSchema1.getRelations()[0], this.method0);
		CostIOManager.writeRelationalTermAndCost(out, plan, c);
		return out;
	}

	/**
	 * Tests if we can read back the data that we attempted to write earlier. Also deletes the file to make sure the next write-test will work. 
	 */
	@Test
	public void testReadCost() {
		try {
			File cost = createCostFile();
			Cost c = CostIOManager.readRelationalTermCost(cost, null);
			Assert.assertEquals(2.555, ((DoubleCost) c).getCost(), 0.0001);
			cost.delete();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
