// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.planner.dag.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.FollowUpHandling;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleQuery;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.ChaseConfiguration;
import uk.ac.ox.cs.pdq.reasoning.chase.ParallelChaser;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * Tests the DAGExplorerUtilities.createInitialApplyRuleConfigurations function
 * over a simple case, declared as Scenario4 in PdqTest.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestApplyRule extends PdqTest {

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
		ConjunctiveQuery accessibleQuery = AccessibleQuery.createAccessibleQuery(ts.getQuery());
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(ts.getQuery());
		Map<Variable, Constant> substitutionFiltered = new HashMap<>(); 
		substitutionFiltered.putAll(substitution);
		for(Variable variable:ts.getQuery().getBoundVariables()) 
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(ts.getQuery(),substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(ts.getQuery(),substitutionFiltered);
		ExplorationSetUp.getCanonicalSubstitution().put(accessibleQuery,substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(accessibleQuery,substitutionFiltered);

		// Create database connection
		DatabaseManager connection = null;
		try {
			connection = new InternalDatabaseManager();
			connection.initialiseDatabaseForSchema(accessibleSchema);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		ParallelChaser chaser = new ParallelChaser();

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		try {
			List<DAGChaseConfiguration> configurations = ApplyRule.createInitialApplyRuleConfigurations(parameters, ts.getQuery(), accessibleSchema,
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
		} finally {
			try {
				connection.dropDatabase();
				connection.shutdown();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}

	}

}
