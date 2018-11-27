package uk.ac.ox.cs.pdq.test.planner.linear.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseParameters;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.PlanPrinter;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.FollowUpHandling;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearGeneric;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlanTree;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Tests the LinearGeneric explorer class.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestLinearGeneric extends PdqTest {

	private boolean doPrint = false;
	private LogicalDatabaseInstance connection;

	/**
	 * Uses Scenario1 from the PdqTest as input, then attempts a couple of
	 * exploration steps checking the partial results and eventually the found plan.
	 * TestScenario1 should have a valid plan, that should look like this:
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
	 */
	@Test
	public void test1ExplorationSteps() {
		GlobalCounterProvider.getNext("CannonicalName");
		TestScenario scenario1 = getScenario1();
		ConjunctiveQuery query = scenario1.getQuery();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(scenario1.getSchema());

		assertAccessibleSchema(accessibleSchema, scenario1.getSchema(), 3);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(scenario1.getQuery());
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
			databaseConnection = createConnection(accessibleSchema);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser();

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);

		// Create linear explorer
		LinearGeneric explorer = null;
		try {
			explorer = new LinearGeneric(new EventBus(), scenario1.getQuery(), accessibleSchema, chaser, databaseConnection, costEstimator, 
					parameters.getMaxDepth());

			PlanTree<SearchNode> planTree = null;
			planTree = explorer.getPlanTree();
			SearchNode root = planTree.getRoot();
			LinearChaseConfiguration configuration0 = root.getConfiguration();
			Assert.assertEquals(1, configuration0.getCandidates().size());

			// Call the explorer for first time
			explorer.performSingleExplorationStep();
			Assert.assertEquals(0, configuration0.getCandidates().size());
			LinearChaseConfiguration configuration1 = planTree.getVertex(root.getId() + 1).getConfiguration();
			Assert.assertEquals(1, configuration1.getCandidates().size());
			Assert.assertEquals(1, configuration1.getFacts().size());
			Atom a = configuration1.getFacts().iterator().next();
			Assert.assertEquals("R0", a.getPredicate().getName());
			Assert.assertArrayEquals(new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c3") }, a.getTerms());

			// Call the explorer for second time
			explorer.performSingleExplorationStep();
			LinearChaseConfiguration configuration2 = planTree.getVertex(root.getId() + 2).getConfiguration();
			Assert.assertEquals(1, configuration2.getCandidates().size());
			Assert.assertEquals(1, configuration2.getFacts().size());
			a = configuration2.getFacts().iterator().next();
			Assert.assertEquals("R1", a.getPredicate().getName());
			Assert.assertArrayEquals(new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c4"), UntypedConstant.create("c5") }, a.getTerms());

			// Call the explorer for third time
			explorer.performSingleExplorationStep();
			LinearChaseConfiguration configuration3 = planTree.getVertex(root.getId() + 3).getConfiguration();
			Assert.assertEquals(0, configuration3.getCandidates().size());
			Assert.assertEquals(1, configuration3.getFacts().size());
			a = configuration3.getFacts().iterator().next();
			Assert.assertEquals("R2", a.getPredicate().getName());
			Assert.assertArrayEquals(new Term[] { UntypedConstant.create("c6"), UntypedConstant.create("c4"), UntypedConstant.create("c7") }, a.getTerms());

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

	/**
	 * Similar to test1, but in this case we are using testScenario2 which has no
	 * valid plan.
	 */
	@Test
	public void test2ExplorationSteps() {
		TestScenario scenario2 = getScenario2();

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(scenario2.getSchema());

		assertAccessibleSchema(accessibleSchema, scenario2.getSchema(), 3);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(scenario2.getQuery());
		ConjunctiveQuery query = scenario2.getQuery();
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
			databaseConnection = createConnection(accessibleSchema);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser();

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);

		// Create linear explorer
		LinearGeneric explorer = null;
		try {
			explorer = new LinearGeneric(new EventBus(), scenario2.getQuery(), accessibleSchema, chaser, databaseConnection, costEstimator, 
					parameters.getMaxDepth());

			PlanTree<SearchNode> planTree = null;
			planTree = explorer.getPlanTree();
			SearchNode root = planTree.getRoot();
			LinearChaseConfiguration configuration0 = root.getConfiguration();
			Assert.assertEquals(0, configuration0.getCandidates().size());

			// Call the explorer for first time
			explorer.performSingleExplorationStep();
			Assert.assertNull(planTree.getVertex(1));
			Assert.assertNull(explorer.getBestPlan());

		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Uses test Scenario3 as input, checks the found plans, makes sure it contains
	 * at least one dependentJoinTerm.
	 */
	@Test
	public void test3ExplorationSteps() {
		TestScenario scenario3 = getScenario3();

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(scenario3.getSchema());

		assertAccessibleSchema(accessibleSchema, scenario3.getSchema(), 4);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(scenario3.getQuery());
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(scenario3.getQuery());
		Map<Variable, Constant> substitutionFiltered = new HashMap<>(); 
		substitutionFiltered.putAll(substitution);
		for(Variable variable:scenario3.getQuery().getBoundVariables()) 
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(scenario3.getQuery(),substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(scenario3.getQuery(),substitutionFiltered);
		ExplorationSetUp.getCanonicalSubstitution().put(accessibleQuery,substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(accessibleQuery,substitutionFiltered);

		// Create database connection
		DatabaseManager databaseConnection = null;
		try {
			databaseConnection = createConnection(accessibleSchema);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser();

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);

		// Create linear explorer
		LinearGeneric explorer = null;
		try {
			explorer = new LinearGeneric(new EventBus(), scenario3.getQuery(), accessibleSchema, chaser, databaseConnection, costEstimator, 
					parameters.getMaxDepth());

			explorer.explore();
			List<Entry<RelationalTerm, Cost>> exploredPlans = explorer.getExploredPlans();
			Assert.assertNotNull(exploredPlans);
			Assert.assertFalse(exploredPlans.isEmpty());
			Assert.assertEquals(4, exploredPlans.size());
			for (Entry<RelationalTerm, Cost> plan : exploredPlans) {
				try {
					if (doPrint)
						PlanPrinter.openPngPlan(plan.getKey());
				} catch (Exception e) {
					e.printStackTrace();
				}
				int dependentJoints = countDependentJoinsInPlan(plan.getKey());
				Assert.assertTrue(dependentJoints >= 1); // each plan must contain at least one dependent join term.
			}
		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	/**
	 * This test utilises a dynamic table and query generation function, and checks
	 * if we get the expected number of output plans for the given number of input
	 * tables.
	 * 
	 * For three input tables we should get 244 plans.
	 */
	@Test
	public void test1ExplorationThreeRelationsPostgres() {
		List<Entry<RelationalTerm, Cost>> exploredPlans = findExploredPlans(3, DatabaseParameters.Postgres);
		Assert.assertEquals(244, exploredPlans.size());
	}

	/**
	 * Creates a scenario similar to the one in Scenario1 but with dynamic number of
	 * tables, and creates a query that can be evaluated by joining all tables
	 * together. Starts an exploration on this schema and returns the results. It
	 * does not contains any assertions.
	 */
	public List<Entry<RelationalTerm, Cost>> findExploredPlans(int numberOfRelations, DatabaseParameters dbParams) {
		// Create the relations
		
		Relation[] relations = new Relation[(int) (numberOfRelations + Math.pow(2.0, numberOfRelations)) + 1];
		for (int index = 0; index < numberOfRelations; ++index)
			relations[index] = Relation.create("R" + index, new Attribute[] { this.a_s, this.b_s, this.c_s, this.d_s },
					new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[0]) });
		relations[numberOfRelations] = Relation.create("Accessible", new Attribute[] { this.a_s });

		// Create a conjunctive query that joins all relations in the first three
		// positions
		Random random = new Random();
		Atom[] atoms = new Atom[numberOfRelations];
		for (int index = 0; index < numberOfRelations; ++index)
			atoms[index] = Atom.create(relations[index], new Term[] { x, y, z, Variable.create("v" + random.nextInt()) });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y, z }, atoms);
		ExplorationSetUp.generateAccessibleQueryAndStoreSubstitutionToCanonicalVariables(query);
		// Create all views and update the relations with the newly create views
		Set<Atom> setOfAtoms = new LinkedHashSet<>();
		for (int index = 0; index < numberOfRelations; ++index)
			setOfAtoms.add(atoms[index]);
		Set<Set<Atom>> powerSet = Sets.powerSet(setOfAtoms);
		int powersetIndex = 0;
		int dependencyIndex = 0;
		int viewIndex = numberOfRelations + 1;
		Dependency[] dependencies = new Dependency[(powerSet.size() - 1) * 2];
		for (Set<Atom> set : powerSet) {
			View view = new View("V" + powersetIndex++, new Attribute[] { this.a_s, this.b_s, this.c_s }, new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[0]) });
			relations[viewIndex++] = view;
			int index = 0;
			Atom[] head = new Atom[set.size()];
			Iterator<Atom> iterator = set.iterator();
			while (iterator.hasNext())
				head[index++] = iterator.next();
			if (index != 0) {
				LinearGuarded viewToRelationDependency = LinearGuarded.create(Atom.create(view, new Term[] { x, y, z }), head);
				view.setViewToRelationDependency(viewToRelationDependency);
				dependencies[dependencyIndex++] = view.getViewToRelationDependency();
				dependencies[dependencyIndex++] = view.getRelationToViewDependency();
			}
		}

		// Create schema
		Schema schema = new Schema(relations, dependencies);

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		// Create database connection
		DatabaseManager databaseConnection = null;
		try {
			databaseConnection = createConnection(accessibleSchema);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser();

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(numberOfRelations);

		// Create linear explorer
		LinearGeneric explorer = null;
		try {
			explorer = new LinearGeneric(new EventBus(), query, accessibleSchema, chaser, databaseConnection, costEstimator, 
					parameters.getMaxDepth());
		} catch (Throwable e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		try {
			explorer.explore();
		} catch (Throwable e) {
			// exception expected after further exploration fails.
		}
		return explorer.getExploredPlans();
	}

	/**
	 * Uses standardScenario1 as input :
	 * The query is Q(x,z) = \exists y R0(x,y) R1(y,z) We also have the dependencies
	 * R0(x,y) -> R2(x,y) R1(y,z) -> R3(y,z) R2(x,y), R3(y,z) -> R0(x,w) R1(w,z)
	 * Every relation has a free access. Suppose that we found the plan that
	 * performs accesses in the following order R2(x,y) R3(y,z) R0(x,y) R1(y,z)
	 * Postpruning should return R2(x,y) R3(y,z)
	 * 
	 * We should find at least the following plans R0(x,y) R1(y,z) R3(x,y) R0(x,y)
	 * R1(y,z) R4(y,z) R0(x,y) R1(y,z) R3(x,y) R4(y,z) R4(y,z) R3(x,y) R4(y,z)
	 * R3(x,y) R0(x,y) R1(y,z)
	 */
	@Test
	public void test5() {
		TestScenario ts = getStandardScenario1();
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
		DatabaseManager databaseConnection = null;
		try {
			databaseConnection = createConnection(accessibleSchema);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser();

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(4);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		// Create Linear Generic
		LinearGeneric explorer = null;
		try {
			explorer = new LinearGeneric(new EventBus(), ts.getQuery(), accessibleSchema, chaser, databaseConnection, costEstimator,
					parameters.getMaxDepth());
			explorer.explore();
			List<Entry<RelationalTerm, Cost>> exploredPlans = explorer.getExploredPlans();
			Assert.assertNotNull(exploredPlans);
			Assert.assertFalse(exploredPlans.isEmpty());
			Assert.assertEquals(14, exploredPlans.size());
		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Contains several asserts to make sure the accessible schema is correct.
	 */
	private void assertAccessibleSchema(AccessibleSchema accessibleSchema, Schema schema, int numberOfAxioms) {
		Assert.assertNotNull(accessibleSchema);

		// accessibility axioms
		Assert.assertNotNull(accessibleSchema.getAccessibilityAxioms());
		Assert.assertEquals(numberOfAxioms, accessibleSchema.getAccessibilityAxioms().length);
		int abc = 0;
		int bc = 0;
		int bac = 0;
		for (AccessibilityAxiom axiom : accessibleSchema.getAccessibilityAxioms()) {
			if (axiom.getBoundVariables().length == 2) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("c"));
				bc++;
			} else if (axiom.getBoundVariables().length == 3 && axiom.getBoundVariables()[0].equals(Variable.create("a"))) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("a"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[2], Variable.create("c"));
				abc++;
			} else if (axiom.getBoundVariables().length == 3 && axiom.getBoundVariables()[0].equals(Variable.create("b"))) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("a"));
				Assert.assertEquals(axiom.getBoundVariables()[2], Variable.create("c"));
				bac++;
			}
		}
		Assert.assertEquals(2, abc);
		Assert.assertEquals(0, bc);
		Assert.assertEquals(1, bac);

		Assert.assertNotNull(accessibleSchema.getRelations());
		Assert.assertEquals(8, accessibleSchema.getRelations().length);
		Dependency[] infAccAxioms = accessibleSchema.getInferredAccessibilityAxioms();
		Assert.assertNotNull(infAccAxioms);
		Assert.assertEquals(0, infAccAxioms.length);
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
	
	private DatabaseManager createConnection(Schema s) {
		try {
			connection = new InternalDatabaseManager();
			connection.initialiseDatabaseForSchema(s);
			return connection;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
