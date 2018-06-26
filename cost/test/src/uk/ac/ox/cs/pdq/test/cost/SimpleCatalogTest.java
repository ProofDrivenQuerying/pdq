package uk.ac.ox.cs.pdq.test.cost;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.cost.estimators.FixedCostPerAccessCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Basic functionality test for the FixedCostPerAccessCostEstimator class and
 * for the SimpleCatalog class.
 * 
 * @author Gabor
 *
 */
public class SimpleCatalogTest extends PdqTest {

	/**
	 * Reads the test\catalog.properties file and checks if the SimpleCatalog class
	 * processed it properly. The file content should be
	 * 
	 * <pre>
	 * RE:R1 BI:access_method1							RT:13
	 * RE:R2 BI:access_method2							RT:15
	 * </pre>
	 * 
	 * Creates R1 and R2 relations with a single attribute, using access_method1 and
	 * access_method2.<br>
	 * A simple accessTerm should have the cost as the individual accesses have, so
	 * 13 and 15 accordingly.
	 */
	@Test
	public void case1() {
		AccessMethodDescriptor am1 = AccessMethodDescriptor.create("access_method1", new Integer[] { 0 });
		AccessMethodDescriptor am2 = AccessMethodDescriptor.create("access_method2", new Integer[] { 0 });
		Relation relation1 = Relation.create("R1", new Attribute[] { Attribute.create(Integer.class, "r1_attribute") }, new AccessMethodDescriptor[] { am1 });
		Relation relation2 = Relation.create("R2", new Attribute[] { Attribute.create(Integer.class, "r2_attribute") }, new AccessMethodDescriptor[] { am2 });
		Schema schema = new Schema(new Relation[] { relation1, relation2 });
		try {
			SimpleCatalog catalog = new SimpleCatalog(schema, "test//catalog.properties");
			Assert.assertNotNull(catalog);
			Assert.assertTrue(13.0 == catalog.getCost(relation1, am1));
			Assert.assertTrue(15.0 == catalog.getCost(relation2, am2));

			AccessTerm at = AccessTerm.create(relation1, am1);
			AccessTerm at1 = AccessTerm.create(relation2, am2);

			FixedCostPerAccessCostEstimator est = new FixedCostPerAccessCostEstimator(catalog);
			Assert.assertTrue(13.0 == est.cost(at).getCost());
			Assert.assertTrue(15.0 == est.cost(at1).getCost());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
