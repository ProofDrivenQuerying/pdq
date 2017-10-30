package uk.ac.ox.cs.pdq.test.planner.dag.explorer;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.PlanPrinter;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.FollowUpHandling;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGExplorerUtilities;
import uk.ac.ox.cs.pdq.planner.dag.explorer.SelectorOfPairsOfConfigurationsToCombine;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * Tests the SelectorOfPairsOfConfigurationsToCombine class, by creating an
 * AccessibleSchema, an accessibleQuery and a chaser to create an
 * InitialApplyRuleConfigurations. We will test the selector on these to make
 * sure it generates the correct pairs of configurations to combine.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestSelectorOfPairsOfConfigurationsToCombine extends PdqTest {

	boolean printPlans = false;

	/**
	 * Uses testScenario6 as input.
	 * 
	 * Creates an initial apply rule configuration, and then tests the SelectorOfPairsOfConfigurationsToCombine class using these configurations.
	 * The expected results are:
	 * <li> 12 binary combinations.</li> 
	 * <li> 48 combinations of 3 rules.</li> 
	 * <li> 96 combinations can be made combining the previous plus 1.</li> 
	 * <li> 24 combinations can be created combining pairs.</li> 
	 */
	@Test
	public void test1GetNextPairOfConfigurationsToCompose() {
		GlobalCounterProvider.getNext("CannonicalName");
		TestScenario ts = getScenario6();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(ts.getQuery());
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
		DatabaseConnection connection = null;
		try {
			connection = new DatabaseConnection(DatabaseParameters.Derby, accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		try {
			
			List<DAGChaseConfiguration> configurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(parameters, ts.getQuery(), accessibleQuery, accessibleSchema, chaser,
					connection);
			Set<String> predicateNames = new HashSet<>();
			for (DAGChaseConfiguration c : configurations) {
				for (ApplyRule app : c.getApplyRules()) {
					String predicateName = app.getRelation().getName();
					Assert.assertTrue(!predicateNames.contains(predicateName));
					predicateNames.add(predicateName);
				}
			}
			Assert.assertEquals(4, predicateNames.size());

			List<Validator> validators = new ArrayList<>();
			validators.add(new DefaultValidator());
			List<DAGChaseConfiguration> left = configurations;
			List<DAGChaseConfiguration> right = configurations;

			Pair<DAGChaseConfiguration, DAGChaseConfiguration> pair = null;
			SelectorOfPairsOfConfigurationsToCombine<AccessibleChaseInstance> selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right, validators);
			List<DAGChaseConfiguration> right2 = new ArrayList<>();
			while ((pair = selector.getNextPairOfConfigurationsToCompose(2)) != null) {
				BinaryConfiguration configuration = new BinaryConfiguration(pair.getLeft(), pair.getRight());
				right2.add(configuration);
			}
			// AB, AC, AD
			// BA, BC, BD
			// CA, CB, CD
			// DA, DB, DC
			Assert.assertEquals(12, right2.size());

			selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right2, validators);
			List<DAGChaseConfiguration> right3 = new ArrayList<>();
			while ((pair = selector.getNextPairOfConfigurationsToCompose(3)) != null) {
				BinaryConfiguration configuration = new BinaryConfiguration(pair.getLeft(), pair.getRight());
				right3.add(configuration);
			}
			// AB, AC, AD
			// BA, BC, BD + A = BC A,BD A,CB A,CD A,DB A,DC A * 4 = 6*4 =24 combination.
			// plus inverse for all = 48
			// A BC,A BD,A CB,A CD,A DB A DC
			// CA, CB, CD + B = AC B,AD B,CA B,CD B,DA B,DC B
			// DA, DB, DC

			Assert.assertEquals(48, right3.size());

			selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right3, validators);
			List<DAGChaseConfiguration> right4 = new ArrayList<>();
			while ((pair = selector.getNextPairOfConfigurationsToCompose(4)) != null) {
				BinaryConfiguration configuration = new BinaryConfiguration(pair.getLeft(), pair.getRight());
				right4.add(configuration);
				try {
					if (printPlans)
						PlanPrinter.openPngPlan(configuration.getPlan());
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}

			}
			//
			// BC A,BD A,CB A,CD A,DB A,DC A + B =CD A B, DC A B -> and inverse B CD A, B DC
			// A = 4 combinations with B, 12 combinations with b c and d.
			// A BC,A BD,A CB,A CD,A DB A DC we have 8 rows, 8 * 12 = 96.
			Assert.assertEquals(96, right4.size());

			selector = new SelectorOfPairsOfConfigurationsToCombine<>(right2, right2, validators);
			List<DAGChaseConfiguration> right4b = new ArrayList<>();
			while ((pair = selector.getNextPairOfConfigurationsToCompose(4)) != null) {
				BinaryConfiguration configuration = new BinaryConfiguration(pair.getLeft(), pair.getRight());
				right4b.add(configuration);
				try {
					if (printPlans)
						PlanPrinter.openPngPlan(configuration.getPlan());
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}

			}
			// AB, AC, AD + AB, AC, AD
			// BA, BC, BD BA, BC, BD
			// 4*3*2*1 = 24 combinations
			Assert.assertEquals(24, right4b.size());

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Uses test scenario3 as input.
	 * Asserts the number of combinations. On depth 2 there should be 10 combinations, 24 on depth=3 and none on depth=4.
	 */
	@Test
	public void test2GetNextPairOfConfigurationsToCompose() {
		TestScenario ts = getScenario3();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(ts.getQuery());
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
		DatabaseConnection connection = null;
		try {
			connection = new DatabaseConnection(DatabaseParameters.Derby, accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(Integer.MAX_VALUE);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		try {
			List<DAGChaseConfiguration> configurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(parameters, ts.getQuery(), accessibleQuery, accessibleSchema, chaser,
					connection);

			Set<String> predicateNames = new HashSet<>();
			for (DAGChaseConfiguration c : configurations) {
				for (ApplyRule app : c.getApplyRules()) {
					String predicateName = app.getRelation().getName() + "_" + app.getRule().getAccessMethod().getName();
					Assert.assertTrue(!predicateNames.contains(predicateName));
					predicateNames.add(predicateName);
				}
			}
			Assert.assertEquals(4, predicateNames.size());

			List<Validator> validators = new ArrayList<>();
			validators.add(new DefaultValidator());

			List<DAGChaseConfiguration> left = configurations;
			List<DAGChaseConfiguration> right = new ArrayList<>();
			right.addAll(configurations);

			Pair<DAGChaseConfiguration, DAGChaseConfiguration> pair = null;
			SelectorOfPairsOfConfigurationsToCombine<AccessibleChaseInstance> selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right, validators);
			int depth = 2;
			final int CORRECT_NUMBER_OF_PLANS[] = new int[] { 10, 24, 0 };
			do {
				Collection<DAGChaseConfiguration> last = new LinkedHashSet<>();
				while ((pair = selector.getNextPairOfConfigurationsToCompose(depth)) != null) {
					BinaryConfiguration configuration = new BinaryConfiguration(pair.getLeft(), pair.getRight());
					last.add(configuration);
				}
				Assert.assertEquals(CORRECT_NUMBER_OF_PLANS[depth - 2], last.size());
				if (last.size() == 0)
					break;
				left.clear();
				left.addAll(last);
				last.clear();
				selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right, validators);
				depth++;
			} while (true);

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
