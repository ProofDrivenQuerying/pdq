package uk.ac.ox.cs.pdq.test.planner.linear.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderDependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearKChase;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;
import uk.ac.ox.cs.pdq.util.PdqTest;

/**
 * Tests the LinearKChase explorer class.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestLinearKchase extends PdqTest {

	@Mock
	protected SimpleCatalog catalog;

	// the following three test case executes the same chasing but with different
	// "chase interval" value. Results should be the same, so the assertions in the
	// test1ExplorationSteps function should make sure we have the right plan each
	// time.
	@Test
	public void test1ExplorationStepsA_k10() {
		GlobalCounterProvider.resetCounters();
		GlobalCounterProvider.getNext("CannonicalName");
		test1ExplorationSteps(10);
	}

	@Test
	public void test1ExplorationStepsB_k2() {
		GlobalCounterProvider.resetCounters();
		GlobalCounterProvider.getNext("CannonicalName");
		test1ExplorationSteps(2);
	}

	@Test
	public void test1ExplorationStepsB_k1() {
		GlobalCounterProvider.resetCounters();
		GlobalCounterProvider.getNext("CannonicalName");
		test1ExplorationSteps(1);
	}

	/**
	 * Uses test Scenario1 and asserts the best plan to be correct.
	 * 
	 * @param chaseInterval
	 */
	public void test1ExplorationSteps(int chaseInterval) {
		TestScenario ts = getScenario1();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(ts.getQuery(), ts.getQuery().getSubstitutionOfFreeVariablesToCanonicalConstants());

		// Create database connection
		DatabaseConnection databaseConnection = null;
		try {
			databaseConnection = new DatabaseConnection(DatabaseParameters.Derby, accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);

		// Mock the cost estimator
		// CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		// when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new
		// DoubleCost(1.0));
		when(this.catalog.getCardinality(ts.getSchema().getRelations()[0])).thenReturn(10);
		when(this.catalog.getCardinality(ts.getSchema().getRelations()[1])).thenReturn(10000);
		when(this.catalog.getCardinality(ts.getSchema().getRelations()[2])).thenReturn(100);
		when(this.catalog.getCardinality(ts.getSchema().getRelations()[3])).thenReturn(10000);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(ts.getSchema().getRelations()[0], AccessMethod.create(new Integer[] {}))).thenReturn(10);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(ts.getSchema().getRelations()[1], AccessMethod.create(new Integer[] { 0 }))).thenReturn(100);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(ts.getSchema().getRelations()[2], AccessMethod.create(new Integer[] {}))).thenReturn(10);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(ts.getSchema().getRelations()[3], AccessMethod.create(new Integer[] { 0 }))).thenReturn(100);

		CardinalityEstimator card = new NaiveCardinalityEstimator(this.catalog);
		// TextBookCostEstimator costEstimator = null;
		TextBookCostEstimator costEstimator = new TextBookCostEstimator(null, card);

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

			explorer = new LinearKChase(new EventBus(), false, ts.getQuery(), accessibleQuery, accessibleSchema, chaser, databaseConnection, costEstimator, costPropagator,
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

	private static void AssertHasAccessTermChild(RelationalTerm relationalTerm) {
		Assert.assertNotNull(relationalTerm);
		Assert.assertNotNull(relationalTerm.getChildren());
		Assert.assertEquals(1, relationalTerm.getChildren().length);
		Assert.assertTrue(relationalTerm.getChild(0) instanceof AccessTerm);
	}
}
