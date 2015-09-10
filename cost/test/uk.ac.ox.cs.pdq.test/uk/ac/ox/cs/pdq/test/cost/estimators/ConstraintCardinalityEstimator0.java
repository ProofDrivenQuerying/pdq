package uk.ac.ox.cs.pdq.test.cost.estimators;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Ignore;

import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.cost.statistics.estimators.ConstraintCardinalityEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.plan.NormalisedPlan;
import uk.ac.ox.cs.pdq.reasoning.HomomorphismException;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.HomomorphismDetectorTypes;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
@Ignore
public abstract class ConstraintCardinalityEstimator0 {

	protected String driver = null;
	protected String url = "jdbc:mysql://localhost/";
	protected String database = "pdq_chase";
	protected String username = "root";
	protected String password ="root";
	
	protected Schema schema;
	protected Catalog catalog = new SimpleCatalog(this.schema, "test/catalog.properties");
	protected HomomorphismDetector detector;
	protected NormalisedPlan plan;
	protected ConstraintCardinalityEstimator estimator;
	
	@Before
	public void setup() {
		this.plan = this.loadPlan();
		
		try {
			this.detector = 
					new HomomorphismManagerFactory().getInstance(
							this.schema, 
							null, 
							HomomorphismDetectorTypes.DATABASE,
							this.driver,
							this.url,
							this.database,
							this.username,			
							this.password);
			((DBHomomorphismManager) this.detector).consolidateBaseTables(this.plan.getTables());
		} catch (HomomorphismException | SQLException e) {
			e.printStackTrace();
		}
		
		this.estimator = new ConstraintCardinalityEstimator(this.schema, (DBHomomorphismManager) this.detector);
	}	
		
	protected abstract NormalisedPlan loadPlan();	
}
