package uk.ac.ox.cs.pdq.test.cost.estimators;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.cost.estimators.TotalERSPICostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.eventbus.EventBus;

/**
 * Tests the WhiteBox cost estimator
 * @author Efthymia Tsamoura
 *
 */
public class TotalERSPICostEstimatorTest extends CostEstimatorTest{

	private EventBus eventBus = new EventBus();
	
	private static String SHEMA_PATH = "test/cost/";
	private static String PLAN_PATH = "test/cost/erspi/";
	private static String CATALOG = "test/cost/erspi/catalog/catalog.properties";
	
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
	boolean canonicalNames = true;
	String driver = null;
	String url = "jdbc:mysql://localhost/";
	String database = "pdq_chase";
	String username = "root";
	String password ="root";

	@Before
	public void prepare() {

	}

	@Test
	public void test() {

		for(int i = 0; i < this.schemata.length; ++i) {
			String s = this.schemata[i];
			String f = this.plans[i];

			try(FileInputStream sis = new FileInputStream(SHEMA_PATH + s)) {

				Schema schema = new SchemaReader().read(sis);
				if (schema == null) {
					throw new IllegalStateException("Schema must be provided.");
				}
				Plan plan = this.obtainPlan(PLAN_PATH + f, schema);

				Catalog catalog = new SimpleCatalog(schema, CATALOG);
				
				TotalERSPICostEstimator costEstimator = null;
				if(plan instanceof DAGPlan) {
					costEstimator = new TotalERSPICostEstimator<DAGPlan>(new StatisticsCollector(false, this.eventBus), catalog);
				}
				else {
					costEstimator = new TotalERSPICostEstimator<LeftDeepPlan>(new StatisticsCollector(false, this.eventBus), catalog);
				}
			
				Assert.assertEquals(plan.getCost(), costEstimator.estimateCost(plan));

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