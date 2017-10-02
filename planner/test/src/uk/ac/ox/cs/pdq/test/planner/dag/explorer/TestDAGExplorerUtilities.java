package uk.ac.ox.cs.pdq.test.planner.dag.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.FollowUpHandling;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGExplorerUtilities;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.PdqTest;

/**
 * Tests the DAGExplorerUtilities.createInitialApplyRuleConfigurations function
 * over a simple case, declared as Scenario4 in PdqTest.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestDAGExplorerUtilities extends PdqTest {

	/**
	 * Uses test scenario4 as input.
	 * Checks the output of the createInitialApplyRuleConfigurations function. 
	 * There should be 6 unique configurations.
	 */
	@Test
	public void test1CreateInitialApplyRuleConfigurations() {
		GlobalCounterProvider.getNext("CannonicalName");
		TestScenario ts = getScenario4();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());
		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(ts.getQuery(), ts.getQuery().getSubstitutionOfFreeVariablesToCanonicalConstants());

		// Create database connection
		DatabaseConnection connection = null;
		try {
			connection = new DatabaseConnection(DatabaseParameters.Derby, accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		try {
			List<DAGChaseConfiguration> configurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(parameters, ts.getQuery(), accessibleQuery, accessibleSchema,
					chaser, connection);
			Set<DAGChaseConfiguration> uniqueConfigs = new HashSet<>();
			int index = 0;
			String predicateNames[] = new String[] { "R0", "R0", "R1", "R1", "R2", "R3", };
			for (DAGChaseConfiguration conf : configurations) {
				System.out.println(conf);
				uniqueConfigs.add(conf);
				Assert.assertTrue(conf.getApplyRules().toString().contains(predicateNames[index]));
				index++;
			}
			Assert.assertEquals(6, uniqueConfigs.size());
			Assert.assertEquals(6, configurations.size());

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}

	}

}
