package uk.ac.ox.cs.pdq.test.planner.linear.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderDependentCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
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
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Tests the LinearGeneric explorer class.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestLinearKchase {

	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
	protected Attribute InstanceID = Attribute.create(Integer.class, "InstanceID");

	@Mock
	protected SimpleCatalog catalog;

	@Before
	public void setup() {
		Utility.assertsEnabled();
		MockitoAnnotations.initMocks(this);
		GlobalCounterProvider.resetCounters();
		uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
		uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
		uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
	}

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

	public void test1ExplorationSteps(int chaseInterval) {
		// Create the relations
		Relation[] relations = new Relation[4];
		relations[0] = Relation.create("R0", new Attribute[] { this.a, this.b, this.c, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a, this.b, this.c, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] { 0 }) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a, this.b, this.c, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] { 1 }) });
		relations[3] = Relation.create("Accessible", new Attribute[] { this.a, this.InstanceID });
		// Create query
		Atom[] atoms = new Atom[3];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		atoms[0] = Atom.create(relations[0], new Term[] { x, Variable.create("y1"), Variable.create("z1") });
		atoms[1] = Atom.create(relations[1], new Term[] { x, y, Variable.create("z2") });
		atoms[2] = Atom.create(relations[2], new Term[] { Variable.create("x1"), y, z });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y, z }, (Conjunction) Conjunction.of(atoms));

		// Create schema
		Schema schema = new Schema(relations);
		schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(5)));

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query, query.getSubstitutionOfFreeVariablesToCanonicalConstants());

		// Create database connection
		DatabaseConnection databaseConnection = null;
		try {
			databaseConnection = new DatabaseConnection(new DatabaseParameters(), accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);

		// Mock the cost estimator
		// CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		// when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new
		// DoubleCost(1.0));
		when(this.catalog.getCardinality(relations[0])).thenReturn(10);
		when(this.catalog.getCardinality(relations[1])).thenReturn(10000);
		when(this.catalog.getCardinality(relations[2])).thenReturn(100);
		when(this.catalog.getCardinality(relations[3])).thenReturn(10000);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(relations[0], AccessMethod.create(new Integer[] {}))).thenReturn(10);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(relations[1], AccessMethod.create(new Integer[] { 0 }))).thenReturn(100);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(relations[2], AccessMethod.create(new Integer[] {}))).thenReturn(10);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(relations[3], AccessMethod.create(new Integer[] { 0 }))).thenReturn(100);

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

			explorer = new LinearKChase(new EventBus(), false, query, accessibleQuery, accessibleSchema, chaser, databaseConnection, costEstimator, costPropagator, nodeFactory,
					parameters.getMaxDepth(), chaseInterval);

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
