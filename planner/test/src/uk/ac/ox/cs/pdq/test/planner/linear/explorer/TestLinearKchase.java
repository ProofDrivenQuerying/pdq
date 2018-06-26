package uk.ac.ox.cs.pdq.test.planner.linear.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseParameters;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderDependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearKChase;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Tests the LinearKChase explorer class.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestLinearKchase extends PdqTest {

	@Mock
	protected SimpleCatalog catalog;
	private LogicalDatabaseInstance connection;

	/**
	 * the following three test case executes the same chasing but with different
	 * "chase interval" value. Results should be the same, so the assertions in the
	 * test1ExplorationSteps function should make sure we have the right plan each
	 * time. More details about the input and output can be read at the
	 * test1ExplorationSteps comment and also at the PdqTest.getScenario1()
	 * functions. this case K (or chase interval) = 10.
	 */
	@Test
	public void test1ExplorationStepsA_k10() {
		GlobalCounterProvider.resetCounters();
		GlobalCounterProvider.getNext("CannonicalName");
		test1ExplorationSteps(10);
	}

	/**
	 * same as above, but K=2.
	 */
	@Test
	public void test1ExplorationStepsB_k2() {
		GlobalCounterProvider.resetCounters();
		GlobalCounterProvider.getNext("CannonicalName");
		test1ExplorationSteps(2);
	}

	/**
	 * same as above, but K=1. This case should be basically the same as a normal
	 * linear chase since we will do a full chase step at each iteration, so this
	 * setting is not commonly used, however it should not break, crash or go to
	 * infinite loops, and to make sore this nice reliable fixed state is permanent
	 * I added this test.
	 */
	@Test
	public void test1ExplorationStepsB_k1() {
		GlobalCounterProvider.resetCounters();
		GlobalCounterProvider.getNext("CannonicalName");
		test1ExplorationSteps(1);
	}

	/**
	 * Uses test Scenario1 and asserts the best plan to be correct and looking
	 * something like this:
	 * 
	 * <pre>
	 * DependentJoin{[(#4=#7)]
	 * 		DependentJoin{[(#0=#3)]
	 * 			Rename{[c1,c2,c3]
	 * 				Access{R0.mt_0[]}
	 * 			},
	 * 			Rename{[c1,c4,c5]
	 * 				Access{R1.mt_1[#0=a]}
	 * 			}
	 * 		},
	 * 		Rename{[c6,c4,c7]
	 * 			Access{R2.mt_2[#1=b]}
	 * 		}
	 * 	}
	 * </pre>
	 * 
	 * @param chaseInterval
	 */
	public void test1ExplorationSteps(int chaseInterval) {
		TestScenario ts = getScenario1();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(ts.getQuery());
		ConjunctiveQuery query = ts.getQuery();
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(query);
		Map<Variable, Constant> substitutionFiltered = new HashMap<>(); 
		substitutionFiltered.putAll(substitution);
		for(Variable variable:query.getBoundVariables()) 
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(query,substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(query,substitutionFiltered);
		ExplorationSetUp.getCanonicalSubstitution().put(accessibleQuery,substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(accessibleQuery,substitutionFiltered);

		// Create database connection
		DatabaseManager databaseConnection = null;
		try {
			databaseConnection = createConnection(DatabaseParameters.Postgres, accessibleSchema);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser();

		// Mock the cost estimator
		when(this.catalog.getCardinality(ts.getSchema().getRelations()[0])).thenReturn(10);
		when(this.catalog.getCardinality(ts.getSchema().getRelations()[1])).thenReturn(10000);
		when(this.catalog.getCardinality(ts.getSchema().getRelations()[2])).thenReturn(100);
		when(this.catalog.getCardinality(ts.getSchema().getRelations()[3])).thenReturn(10000);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(ts.getSchema().getRelations()[0], AccessMethodDescriptor.create(new Integer[] {}))).thenReturn(10);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(ts.getSchema().getRelations()[1], AccessMethodDescriptor.create(new Integer[] { 0 }))).thenReturn(100);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(ts.getSchema().getRelations()[2], AccessMethodDescriptor.create(new Integer[] {}))).thenReturn(10);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(ts.getSchema().getRelations()[3], AccessMethodDescriptor.create(new Integer[] { 0 }))).thenReturn(100);

		CardinalityEstimator card = new NaiveCardinalityEstimator(this.catalog);
		// TextBookCostEstimator costEstimator = null;
		TextBookCostEstimator costEstimator = new TextBookCostEstimator(card);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);

		// Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);

		// Create linear explorer
		LinearKChase explorer = null;
		try {

			// OrderDependentCostEstimator sad = ;
			OrderDependentCostPropagator costPropagator = new OrderDependentCostPropagator(costEstimator);

			explorer = new LinearKChase(new EventBus(), ts.getQuery(), accessibleQuery, accessibleSchema, chaser, databaseConnection, costEstimator, costPropagator,
					nodeFactory, parameters.getMaxDepth(), chaseInterval);

			explorer.explore();

			// checking the plan
			RelationalTerm plan = explorer.getBestPlan();
			Assert.assertNotNull(plan);
			Assert.assertTrue(plan instanceof DependentJoinTerm);
			Assert.assertEquals(0, plan.getInputAttributes().length);
			Assert.assertEquals(9, plan.getOutputAttributes().length);
			Assert.assertEquals(2, plan.getChildren().length);
			Assert.assertTrue(plan.getChildren()[0] instanceof DependentJoinTerm);
			Assert.assertTrue(plan.getChildren()[1] instanceof RenameTerm);
			AssertHasAccessTermChild(plan.getChildren()[1]);
			RelationalTerm subPlan = plan.getChildren()[0];
			Assert.assertTrue(subPlan instanceof DependentJoinTerm);
			Assert.assertEquals(2, subPlan.getChildren().length);

			Assert.assertTrue(subPlan.getChildren()[0] instanceof RenameTerm);
			Assert.assertTrue(subPlan.getChildren()[1] instanceof RenameTerm);
			AssertHasAccessTermChild(subPlan.getChildren()[0]);
			AssertHasAccessTermChild(subPlan.getChildren()[1]);

		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	@After
	public void tearDown() {
		if (connection!=null) {
			try {
				connection.dropDatabase();
				connection.shutdown();
			} catch (DatabaseException e) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}
	
	private DatabaseManager createConnection(DatabaseParameters params, Schema s) {
		try {
			connection = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(params),1);
			connection.initialiseDatabaseForSchema(s);
			return connection;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
