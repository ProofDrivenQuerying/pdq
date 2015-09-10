package uk.ac.ox.cs.pdq.test.reasoning.utility;

import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Ignore;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.plan.CommandToTGDTranslator;
import uk.ac.ox.cs.pdq.plan.NormalisedPlan;
import uk.ac.ox.cs.pdq.reasoning.HomomorphismException;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.HomomorphismDetectorTypes;
import uk.ac.ox.cs.pdq.reasoning.chase.EGDChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;

import com.google.common.collect.Sets;
/**
 * 
 * @author Efthymia Tsamoura
 *
 */

public abstract class ReasonerUtilityTest0 {

	protected String driver = "com.mysql.jdbc.Driver";
	protected String url = "jdbc:mysql://localhost/";
	protected String database = "pdq_chase";
	protected String username = "root";
	protected String password ="root";

	protected Schema schema = new TestSchema().getSchema();
	protected Collection<EGD> keys = this.schema.getKeyDependencies();
	protected HomomorphismDetector detector;
	protected NormalisedPlan plan;
	protected Collection<Constraint> planConstraints;

	protected final RestrictedChaser restrictedChaser = new RestrictedChaser(null);
	protected final EGDChaser egdChaser = new EGDChaser(null);


	@Before
	public void setup() {
		this.plan = this.loadPlan();

		//Get the tgds from the input commands;
		Collection<TGD> forwardTgds = new CommandToTGDTranslator().toTGD(this.plan);
		Collection<TGD> backwardTgds = Sets.newLinkedHashSet();
		for(TGD tgd:forwardTgds) {
			backwardTgds.add(tgd.invert());
		}
		this.planConstraints = CollectionUtils.<Constraint>union(forwardTgds, backwardTgds);

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
	}

	public abstract NormalisedPlan loadPlan();	
}
