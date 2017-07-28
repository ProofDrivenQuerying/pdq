package uk.ac.ox.cs.pdq.test.cost.estimators;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.WhiteBoxCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;

// TODO: Auto-generated Javadoc
/**
 * Tests the WhiteBox cost estimator.
 *
 * @author Efthymia Tsamoura
 */
public class TotalWhiteBoxEstimatorTest extends CostEstimatorTest{

	/** The event bus. */
	private EventBus eventBus = new EventBus();

	/** The shema path. */
	private static String SHEMA_PATH = "test/cost/";

	/** The plan path. */
	private static String PLAN_PATH = "test/cost/blackbox/";
	
	/** The catalog. */
	private static String CATALOG = "test/cost/erspi/catalog/catalog.properties";

	/** The schemata. */
	String schemata = "schema_bio.xml";

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
			"plan_bio_11.xml",
			"plan_bio_12.xml",
			"plan_bio_13.xml",
			"plan_bio_14.xml",
			"plan_bio_15.xml"
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

		for(int i = 0; i < this.plans.length; ++i) {
			String s = this.schemata;
			String f = this.plans[i];

			try(FileInputStream sis = new FileInputStream(SHEMA_PATH + s)) {
				Schema schema = new SchemaReader().read(sis);
				if (schema == null) 
					throw new IllegalStateException("Schema must be provided.");
				
				Entry<RelationalTerm, Cost> plan = this.obtainPlan(PLAN_PATH + f, schema);
				Catalog catalog = new SimpleCatalog(schema, CATALOG);
				CardinalityEstimator card = new NaiveCardinalityEstimator(catalog);
				WhiteBoxCostEstimator costEstimator = null;
				costEstimator = new WhiteBoxCostEstimator(new StatisticsCollector(false, this.eventBus), card, catalog);
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