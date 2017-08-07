package uk.ac.ox.cs.pdq.cost.io.jaxb;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.util.Utility;

public class CostIOManagerTest {

	/**
	 * Makes sure assertions are enabled.
	 */
	@Before
	public void setup() {
		Utility.assertsEnabled();
	}

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

	private File createCostFile() throws JAXBException, FileNotFoundException {
		File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\cost\\io\\jaxb\\schema.xml");
		Schema schema = IOManager.importSchema(schemaFile);
		Cost c = new DoubleCost(2.555);
		File out = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\cost\\io\\jaxb\\ProjectionTermWithCost.xml");
		RelationalTerm plan = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
		CostIOManager.writeRelationalTermAndCost(out, plan, c);
		return out;
	}

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
