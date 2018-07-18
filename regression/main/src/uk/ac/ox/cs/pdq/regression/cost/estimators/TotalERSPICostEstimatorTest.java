package uk.ac.ox.cs.pdq.regression.cost.estimators;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.TotalNumberOfOutputTuplesPerAccessCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.regression.utils.PlannerTestUtilities;

/**
 * Tests the WhiteBox cost estimator.
 *
 * @author Efthymia Tsamoura
 */
public class TotalERSPICostEstimatorTest{
	
	/** The shema path. */
	private static String SHEMA_PATH = "test/cost/";
	
	/** The plan path. */
	private static String PLAN_PATH = "test/cost/erspi/";
	
	/** The catalog. */
	private static String CATALOG = "test/cost/erspi/catalog/catalog.properties";
	
	/** The schemata. */
	String[] schemata = {
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			};
	
	/** The plans. */
	String[] plans = {
			"plan_bio_1.xml",
			"plan_bio_2.xml",
			"plan_bio_3.xml",
			"plan_bio_4.xml",
			"plan_bio_5.xml",
			"plan_bio_6.xml",
			"plan_bio_7.xml",
			"plan_bio_8.xml",
			"plan_bio_9.xml",
			"plan_bio_10.xml",
			};
	
	/** The canonical names. */
	boolean canonicalNames = true;
	
	/** The driver. */
	String driver = null;
	
	/** The url. */
	String url = "jdbc:mysql://localhost/";
	
	/** The database. */
	String database = "pdq_chase";
	
	/** The username. */
	String username = "root";
	
	/** The password. */
	String password ="root";

	/**
	 * Prepare.
	 */
	@Before
	public void prepare() {

	}

	/**
	 * Test.
	 */
	@Test
	public void test() {
		for(int i = 0; i < this.schemata.length; ++i) {
			String s = this.schemata[i];
			String f = this.plans[i];
			try {
				Schema schema = IOManager.importSchema(new File(SHEMA_PATH + s));
				if (schema == null) 
					throw new IllegalStateException("Schema must be provided.");
				Entry<RelationalTerm, Cost> plan = PlannerTestUtilities.obtainPlan(PLAN_PATH + f, schema);
				Catalog catalog = new SimpleCatalog(schema, CATALOG);
				TotalNumberOfOutputTuplesPerAccessCostEstimator costEstimator = null;
				costEstimator = new TotalNumberOfOutputTuplesPerAccessCostEstimator(catalog);
				Assert.assertEquals(plan.getValue(), costEstimator.cost(plan.getKey()));

			} catch (FileNotFoundException e) {
				System.out.println("Cannot find input files");
			} catch (Exception e) {
				System.out.println("EXCEPTION: " + e.getClass().getSimpleName() + " " + e.getMessage());
				e.printStackTrace();
			} catch (Error e) {
				System.out.println("ERROR: " + e.getClass().getSimpleName() + " " + e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

}