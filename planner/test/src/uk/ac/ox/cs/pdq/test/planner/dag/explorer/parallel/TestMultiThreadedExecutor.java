package uk.ac.ox.cs.pdq.test.planner.dag.explorer.parallel;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.FollowUpHandling;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.SynchronizedEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGExplorerUtilities;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.MultiThreadedContext;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.MultiThreadedExecutor;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ClosedValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Tests the MultiThreadedExecutor class by using it mostly with Mock objects
 * and a simple example schema.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestMultiThreadedExecutor extends PdqTest {

	int parallelThreads = 20;
	boolean twoWay = false;

	boolean printPlans = false;
	private LogicalDatabaseInstance connection;

	/**
	 * Tests binary configurations when the MultiThreadedExecutor's twoWay mode
	 * parameter is true.
	 * 
	 * @see TestMultiThreadedExecutor.test1CreateBinaryConfigurations()
	 */
	@Test
	public void test1CreateBinaryConfigurations_TwoWay() {
		twoWay = true;
		test1CreateBinaryConfigurations();
	}

	/**
	 * Tests binary configurations when the MultiThreadedExecutor's twoWay mode
	 * parameter is false.
	 * 
	 * @see TestMultiThreadedExecutor.test1CreateBinaryConfigurations()
	 */
	@Test
	public void test1CreateBinaryConfigurations_SingleWay() {
		twoWay = false;
		test1CreateBinaryConfigurations();
	}

	/**
	 * Uses test scenario6 as input. Creates initial configurations and asserts the
	 * amount created. Also asserts the number of equivalence classes created in the
	 * process. After 3 rounds of creating configurations there should be
	 * 
	 * <li>160 equivalence classes in case twoWay mode is true,</li>
	 * <li>64 equivalence classes in case twoWay mode is false,</li>
	 * <li>96 new configurations in case twoWay mode is true,</li>
	 * <li>24 new configurations in case twoWay mode is false,</li>
	 * 
	 */
	public void test1CreateBinaryConfigurations() {
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
		try {
			ExternalDatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Derby);
			connection = new LogicalDatabaseInstance(new MultiInstanceFactCache(), dm, 1);
			connection.initialiseDatabaseForSchema(accessibleSchema);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);
		when(parameters.getMaxDepth()).thenReturn(ts.getSchema().getNumberOfRelations());

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));
		when(costEstimator.clone()).thenReturn(costEstimator);

		// Mock success domination
		SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
		when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
				.thenReturn(false);
		when(successDominance.clone()).thenReturn(successDominance);

		// Mock domination
		Dominance dominance = Mockito.mock(Dominance.class);
		when(dominance.isDominated(Mockito.any(Configuration.class), Mockito.any(Configuration.class))).thenReturn(false);
		when(dominance.clone()).thenReturn(dominance);

		// Create validators
		List<Validator> validators = new ArrayList<>();
		validators.add(new DefaultValidator());

		// Create a multitheaded executor
		MultiThreadedContext mtcontext = null;
		try {
			mtcontext = new MultiThreadedContext(parallelThreads, chaser, connection, costEstimator, successDominance, new Dominance[] { dominance }, validators);
		} catch (Exception e1) {
			e1.printStackTrace();
			Assert.fail();
		}
		MultiThreadedExecutor executor = new MultiThreadedExecutor(mtcontext);

		try {
			List<DAGChaseConfiguration> configurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(parameters, ts.getQuery(), accessibleQuery, accessibleSchema,
					chaser, connection);
			Set<String> predicateNames = new HashSet<>();
			for (DAGChaseConfiguration c : configurations) {
				for (ApplyRule app : c.getApplyRules()) {
					String predicateName = app.getRelation().getName();
					Assert.assertTrue(!predicateNames.contains(predicateName));
					predicateNames.add(predicateName);
				}
			}
			Assert.assertEquals(4, predicateNames.size());

			Queue<DAGChaseConfiguration> leftSideConfigurations = new ConcurrentLinkedQueue<>();
			DAGEquivalenceClasses equivalenceClasses = new SynchronizedEquivalenceClasses();
			leftSideConfigurations.addAll(configurations);
			for (DAGChaseConfiguration initialConfiguration : configurations) {
				int size = equivalenceClasses.size();
				equivalenceClasses.addEntry(initialConfiguration);
				Assert.assertEquals(size + 1, equivalenceClasses.size());
			}

			Collection<DAGChaseConfiguration> newConfigurations = null;
			try {
				newConfigurations = executor.createBinaryConfigurations(2, leftSideConfigurations, equivalenceClasses.getConfigurations(), new Dependency[] {}, null,
						equivalenceClasses, twoWay, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
				leftSideConfigurations.clear();
				leftSideConfigurations.addAll(newConfigurations);
				for (DAGChaseConfiguration configuration : newConfigurations) {
					int size = equivalenceClasses.size();
					equivalenceClasses.addEntry(configuration);
					Assert.assertEquals(size + 1, equivalenceClasses.size());
				}
				Assert.assertEquals(12, newConfigurations.size());
				Assert.assertEquals(16, equivalenceClasses.size());

				newConfigurations = executor.createBinaryConfigurations(3, leftSideConfigurations, equivalenceClasses.getConfigurations(), new Dependency[] {}, null,
						equivalenceClasses, twoWay, Long.MAX_VALUE, TimeUnit.MILLISECONDS);

				int excpected = 24;
				if (twoWay)
					excpected *= 2;
				Assert.assertEquals(excpected, newConfigurations.size());
				leftSideConfigurations.clear();
				leftSideConfigurations.addAll(newConfigurations);
				for (DAGChaseConfiguration configuration : newConfigurations) {
					int size = equivalenceClasses.size();
					equivalenceClasses.addEntry(configuration);
					Assert.assertEquals(size + 1, equivalenceClasses.size());
				}
				if (twoWay)
					Assert.assertEquals(64, equivalenceClasses.size());
				else
					Assert.assertEquals(40, equivalenceClasses.size());
				newConfigurations = executor.createBinaryConfigurations(4, leftSideConfigurations, equivalenceClasses.getConfigurations(), new Dependency[] {}, null,
						equivalenceClasses, twoWay, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
				for (DAGChaseConfiguration configuration : newConfigurations) {
					int size = equivalenceClasses.size();
					equivalenceClasses.addEntry(configuration);
					Assert.assertEquals(size + 1, equivalenceClasses.size());
				}
				if (twoWay)
					Assert.assertEquals(160, equivalenceClasses.size());
				else
					Assert.assertEquals(64, equivalenceClasses.size());

				if (twoWay)
					Assert.assertEquals(96, newConfigurations.size());
				else
					Assert.assertEquals(24, newConfigurations.size());
			} catch (PlannerException | LimitReachedException e) {
				e.printStackTrace();
				Assert.fail();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			if (connection != null) {
				try {
					connection.dropDatabase();
					connection.shutdown();
				} catch (DatabaseException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		}
	}

	@Test
	public void test2CreateBinaryConfigurations() {
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
		try {
			ExternalDatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Derby);
			connection = new LogicalDatabaseInstance(new MultiInstanceFactCache(), dm, 1);
			connection.initialiseDatabaseForSchema(accessibleSchema);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(ts.getSchema().getNumberOfRelations());
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));
		when(costEstimator.clone()).thenReturn(costEstimator);

		// Mock success domination
		SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
		when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
				.thenReturn(false);
		// SuccessDominance successDominance = new
		// SuccessDominanceFactory(SuccessDominanceTypes.CLOSED).getInstance();
		when(successDominance.clone()).thenReturn(successDominance);

		// Mock domination
		Dominance dominance = Mockito.mock(Dominance.class);
		when(dominance.isDominated(Mockito.any(Configuration.class), Mockito.any(Configuration.class))).thenReturn(false);

		// Create validators
		List<Validator> validators = new ArrayList<>();
		validators.add(new DefaultValidator());

		// Create a multitheaded executor
		MultiThreadedContext mtcontext = null;
		try {
			mtcontext = new MultiThreadedContext(parallelThreads, chaser, connection, costEstimator, successDominance, new Dominance[] { dominance }, validators);
		} catch (Exception e1) {
			e1.printStackTrace();
			Assert.fail();
		}
		MultiThreadedExecutor executor = new MultiThreadedExecutor(mtcontext);

		try {
			List<DAGChaseConfiguration> configurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(parameters, ts.getQuery(), accessibleQuery, accessibleSchema, chaser,
					connection);
			Set<String> predicateNames = new HashSet<>();
			for (DAGChaseConfiguration c : configurations) {
				for (ApplyRule app : c.getApplyRules()) {
					String predicateName = app.getRelation().getName();
					if (!predicateName.equals("R1")) // R1 has two access methods, so it will not be unique.
						Assert.assertTrue(!predicateNames.contains(predicateName));
					predicateNames.add(predicateName);
				}
			}
			Assert.assertEquals(3, predicateNames.size());

			Queue<DAGChaseConfiguration> leftSideConfigurations = new ConcurrentLinkedQueue<>();
			DAGEquivalenceClasses equivalenceClasses = new SynchronizedEquivalenceClasses();
			leftSideConfigurations.addAll(configurations);
			for (DAGChaseConfiguration initialConfiguration : configurations) {
				equivalenceClasses.addEntry(initialConfiguration);
			}

			Collection<DAGChaseConfiguration> newConfigurations = null;
			try {

				// round 1
				newConfigurations = executor.createBinaryConfigurations(2, leftSideConfigurations, equivalenceClasses.getConfigurations(), new Dependency[] {}, null,
						equivalenceClasses, twoWay, Long.MAX_VALUE, TimeUnit.MILLISECONDS);

				Assert.assertEquals(10, newConfigurations.size());

				// round 2
				leftSideConfigurations.clear();
				leftSideConfigurations.addAll(newConfigurations);
				for (DAGChaseConfiguration configuration : newConfigurations) {
					equivalenceClasses.addEntry(configuration);
				}
				newConfigurations = executor.createBinaryConfigurations(3, leftSideConfigurations, equivalenceClasses.getConfigurations(), new Dependency[] {}, null,
						equivalenceClasses, twoWay, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
				int excpected = 12;
				if (twoWay)
					excpected *= 2;

				Assert.assertEquals(excpected, newConfigurations.size());

				// round 3
				leftSideConfigurations.clear();
				leftSideConfigurations.addAll(newConfigurations);
				for (DAGChaseConfiguration configuration : newConfigurations) {
					equivalenceClasses.addEntry(configuration);
				}
				newConfigurations = executor.createBinaryConfigurations(4, leftSideConfigurations, equivalenceClasses.getConfigurations(), new Dependency[] {}, null,
						equivalenceClasses, twoWay, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
				Assert.assertEquals(0, newConfigurations.size());
			} catch (PlannerException | LimitReachedException e) {
				e.printStackTrace();
				Assert.fail();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			if (connection != null) {
				try {
					connection.dropDatabase();
					connection.shutdown();
				} catch (DatabaseException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		}
	}

	/**
	 * Creates a schema out of 4,8 or 12 relations where each four relation forms a
	 * busy sub-plan. Current assertions are set to validate the results of a chase
	 * execution of 8 relations.
	 */
	@Test
	public void test4LargeBushyPlanExploration() {
		final int NUMBER_OF_RELATIONS = 8; // can be 4, 8 or 12. With 8 relations, and using the ClosedValidator it should
											// be successful in 3-4sec. with 12 it takes way too long.
		// Create the relations
		Relation[] relations = new Relation[NUMBER_OF_RELATIONS + 1];
		for (int i = 0; i < relations.length - 1; i++) {
			if (i % 2 == 0)
				relations[i] = Relation.create("R" + i, new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s },
						new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
			else
				relations[i] = Relation.create("R" + i, new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s },
						new AccessMethod[] { AccessMethod.create(new Integer[] { 2, 3 }) });
		}
		relations[relations.length - 1] = Relation.create("Accessible", new Attribute[] { this.a_s });
		// Create query
		// R0(x,y,z,w) R1(_,_,z,w) R2(x,y,z',w') R3(_,_,z',w')
		Atom[] atoms = new Atom[relations.length - 1];
		List<Variable> head = new ArrayList<>();
		for (int z = 0; z < (relations.length - 1) / 4; z++) {
			Variable x = Variable.create("x" + z);
			head.add(x);
			Variable y = Variable.create("y" + z);
			head.add(y);
			Variable v = Variable.create("z" + z);
			Variable w = Variable.create("w" + z);
			atoms[4 * z + 0] = Atom.create(relations[4 * z + 0], new Term[] { x, y, v, w });
			atoms[4 * z + 1] = Atom.create(relations[4 * z + 1], new Term[] { Variable.create("x" + z + "b"), Variable.create("y" + z + "b"), v, w });
			atoms[4 * z + 2] = Atom.create(relations[4 * z + 2], new Term[] { x, y, Variable.create("z" + z + "c"), Variable.create("w" + z + "c") });
			atoms[4 * z + 3] = Atom.create(relations[4 * z + 3],
					new Term[] { Variable.create("x" + z + "d"), Variable.create("y" + z + "d"), Variable.create("z" + z + "c"), Variable.create("w" + z + "c") });
		}
		ConjunctiveQuery query = ConjunctiveQuery.create(head.toArray(new Variable[head.size()]), (Conjunction) Conjunction.of(atoms));

		// Create schema
		Schema schema = new Schema(relations);

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query);
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
		try {
			ExternalDatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Derby);
			connection = new LogicalDatabaseInstance(new MultiInstanceFactCache(), dm, 1);
			connection.initialiseDatabaseForSchema(accessibleSchema);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));
		when(costEstimator.clone()).thenReturn(costEstimator);

		// Mock success domination
		SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
		when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
				.thenReturn(false);
		when(successDominance.clone()).thenReturn(successDominance);
		// Mock domination
		Dominance dominance = Mockito.mock(Dominance.class);
		when(dominance.isDominated(Mockito.any(Configuration.class), Mockito.any(Configuration.class))).thenReturn(false);

		// Create validators
		List<Validator> validators = new ArrayList<>();
		// validators.add(new DefaultValidator());
		validators.add(new ClosedValidator());

		try {

			// Create a multitheaded executor
			MultiThreadedContext mtcontext = null;
			try {
				mtcontext = new MultiThreadedContext(parallelThreads, chaser, connection, costEstimator, successDominance, new Dominance[] { dominance }, validators);
			} catch (Exception e1) {
				e1.printStackTrace();
				Assert.fail();
			}

			MultiThreadedExecutor executor = new MultiThreadedExecutor(mtcontext);

			List<DAGChaseConfiguration> configurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(parameters, query, accessibleQuery, accessibleSchema, chaser,
					connection);
			Queue<DAGChaseConfiguration> leftSideConfigurations = new ConcurrentLinkedQueue<>();
			DAGEquivalenceClasses equivalenceClasses = new SynchronizedEquivalenceClasses();
			leftSideConfigurations.addAll(configurations);
			for (DAGChaseConfiguration initialConfiguration : configurations) {
				equivalenceClasses.addEntry(initialConfiguration);
			}

			Collection<DAGChaseConfiguration> newConfigurations = null;

			// round 1
			newConfigurations = executor.createBinaryConfigurations(2, leftSideConfigurations, equivalenceClasses.getConfigurations(), new Dependency[] {}, null,
					equivalenceClasses, twoWay, Long.MAX_VALUE, TimeUnit.MILLISECONDS);

			Assert.assertEquals(16, newConfigurations.size());

			// round 2 .. 7
			for (int round = 2; round < 8; round++) {
				leftSideConfigurations.addAll(newConfigurations);
				for (DAGChaseConfiguration configuration : newConfigurations) {
					equivalenceClasses.addEntry(configuration);
				}
				newConfigurations = executor.createBinaryConfigurations(round + 1, leftSideConfigurations, equivalenceClasses.getConfigurations(), new Dependency[] {}, null,
						equivalenceClasses, twoWay, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
				// System.out.println("Round: #" + round + " found configs: " +
				// newConfigurations.size());
			}
			Assert.assertEquals(120, newConfigurations.size());

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			if (connection != null) {
				try {
					connection.dropDatabase();
					connection.shutdown();
				} catch (DatabaseException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		}
	}
	@After
	public void tearDown() throws Exception {
		if (connection!=null) {
			connection.dropDatabase();
			connection.shutdown();
		}
	}

}
