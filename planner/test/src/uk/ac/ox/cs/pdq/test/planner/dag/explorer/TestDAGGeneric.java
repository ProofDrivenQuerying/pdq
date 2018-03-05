package uk.ac.ox.cs.pdq.test.planner.dag.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.PlanPrinter;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.FollowUpHandling;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGGeneric;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ClosedValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Tests the DAG generic class. Makes sure we have all possible plans explored.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestDAGGeneric extends PdqTest {

	private final boolean printPlans = false;

	/**
	 * Uses scenario5 as input, checks the accessible schema before exploration, and
	 * the explored plans afterwards. In this test we should have 30 plans similar
	 * to this:
	 * 
	 * <pre>
	 * Plan=Join{[(#0=#8&#1=#9)]
	 * 		DependentJoin{[(#2=#6&#3=#7)]
	 * 			Rename{[c1,c2,c7,c8]Access{R2.mt_3[]}},
	 * 			Rename{[c9,c10,c7,c8]Access{R3.mt_4[#2=c,#3=d]}}},
	 * 		DependentJoin{[(#2=#6&#3=#7)]
	 * 			Rename{[c1,c2,c3,c4]Access{R0.mt_0[]}},
	 * 			Rename{[c5,c6,c3,c4]Access{R1.mt_2[#2=c,#3=d]}}}}
	 * Cost=1.0
	 * </pre>
	 * 
	 * Asserts that we have busy plans too not only left-deep.
	 */
	@Test
	public void test1ExplorationSteps() {
		GlobalCounterProvider.getNext("CannonicalName");
		TestScenario ts = getScenario5();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());

		assertAccessibleSchema(accessibleSchema, ts.getSchema(), 5);

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
		DatabaseManager connection = null;
		try {
			ExternalDatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
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

		// Mock success domination
		SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
		when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
				.thenReturn(false);
		when(successDominance.clone()).thenReturn(successDominance);

		// Create validators
		List<Validator> validators = new ArrayList<>();
		validators.add(new DefaultValidator());

		try {
			DAGGeneric explorer = new DAGGeneric(new EventBus(), false, parameters, ts.getQuery(), accessibleQuery, accessibleSchema, chaser, connection, costEstimator,
					successDominance, null, validators, 4);
			explorer.explore();
			explorer.getExploredPlans();
			List<Entry<RelationalTerm, Cost>> exploredPlans = explorer.getExploredPlans();
			Assert.assertNotNull(exploredPlans);
			Assert.assertFalse(exploredPlans.isEmpty());
			Assert.assertEquals(30, exploredPlans.size());
			boolean topIsAlwaysDependentJoin = true;
			for (Entry<RelationalTerm, Cost> plan : exploredPlans) {
				try {
					if (printPlans)
						PlanPrinter.openPngPlan(plan.getKey());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				if (!(plan.getKey() instanceof DependentJoinTerm)) {
					topIsAlwaysDependentJoin = false;
				}
				int dependentJoints = countDependentJoinsInPlan(plan.getKey());
				Assert.assertTrue(dependentJoints >= 1); // each plan must contain at least one dependent join term.
			}

			// left deep and right deep plans have dependent join on top. This makes sure at
			// least one of them is not like that.
			Assert.assertFalse(topIsAlwaysDependentJoin);

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (PlannerException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Uses scenario3 as input. Asserts that we found 8 plans, similar to this:
	 * 
	 * <pre>
	 *  
	 * DependentJoin{[(#4=#7)]
	 *  	DependentJoin{[(#0=#3)]
	 *   		Rename{[c0,c1,c2]
	 *   			Access{R0.mt_0[]}},
	 *   		Select{[(#2=5)]
	 *   			Rename{[c0,c3,5]
	 *   				Access{R1.mt_1[#0=a]}}}},
	 *   	Rename{[c4,c3,c5]
	 *   		Access{R2.mt_3[#1=b]}}}
	 * cost = 1.0
	 * </pre>
	 */
	@Test
	public void test3ExplorationSteps() {
		TestScenario ts = getScenario3();

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
			ExternalDatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
			databaseConnection = new LogicalDatabaseInstance(new MultiInstanceFactCache(), dm, 1);
			databaseConnection.initialiseDatabaseForSchema(accessibleSchema);
		} catch (Exception e) {
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
		when(parameters.getMaxDepth()).thenReturn(3);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		// Create DAGGeneric
		DAGGeneric explorer = null;
		try {
			// Mock success domination
			SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
			when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
					.thenReturn(false);
			when(successDominance.clone()).thenReturn(successDominance);

			// Create validators
			List<Validator> validators = new ArrayList<>();
			validators.add(new DefaultValidator());

			explorer = new DAGGeneric(new EventBus(), false, parameters, ts.getQuery(), accessibleQuery, accessibleSchema, chaser, databaseConnection, costEstimator,
					successDominance, null, validators, 3);

			explorer.explore();
			List<Entry<RelationalTerm, Cost>> exploredPlans = explorer.getExploredPlans();
			Assert.assertNotNull(exploredPlans);
			Assert.assertFalse(exploredPlans.isEmpty());
			Assert.assertEquals(8, exploredPlans.size());
		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Creates a schema out of 1,2 or 3 group of 4 relations where each four
	 * relation forms a busy sub-plan. Current assertions are set to validate the
	 * results of a chase execution of 8 relations. Number of relations can be
	 * changed in the first line of the function for testing purposes. The case of 4
	 * relations is havily tested, the case of 12 relations is too slow for normal
	 * unit tests, so we use only the 8 relation mode for now.
	 * <pre>
	 * Expected plans are similar to this:
	 * 
	 * Project{[c0,c1,c10,c11]
	 * 		Join{[(#0=#24&#1=#25&#8=#16&#9=#17)]
	 * 			Join{[]DependentJoin{[(#2=#6&#3=#7)]
	 * 				Rename{[c0,c1,c2,c3]
	 * 					Access{R0.mt_0[]}},
	 * 				Rename{[c4,c5,c2,c3]
	 * 					Access{R1.mt_1[#2=c,#3=d]}}},
	 * 			DependentJoin{[(#2=#6&#3=#7)]
	 * 				Rename{[c10,c11,c16,c17]
	 * 					Access{R6.mt_6[]}},
	 * 				Rename{[c18,c19,c16,c17]
	 * 					Access{R7.mt_7[#2=c,#3=d]}}}},
	 * 		Join{[]
	 * 			DependentJoin{[(#2=#6&#3=#7)]
	 * 				Rename{[c10,c11,c12,c13]
	 * 					Access{R4.mt_4[]}},
	 * 				Rename{[c14,c15,c12,c13]
	 * 					Access{R5.mt_5[#2=c,#3=d]}}},
	 * 			DependentJoin{[(#2=#6&#3=#7)]
	 * 				Rename{[c0,c1,c6,c7]
	 * 					Access{R2.mt_2[]}},
	 * 				Rename{[c8,c9,c6,c7]
	 * 					Access{R3.mt_3[#2=c,#3=d]}}}}}}
	 * </pre>
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
		ConjunctiveQuery query = ConjunctiveQuery.create(head.toArray(new Variable[head.size()]), atoms);

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
		DatabaseManager connection = null;
		try {
			ExternalDatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
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

		// Mock success domination
		SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
		when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
				.thenReturn(false);

		// Create validators
		List<Validator> validators = new ArrayList<>();
		// validators.add(new DefaultValidator());
		validators.add(new ClosedValidator());

		try {
			DAGGeneric explorer = new DAGGeneric(new EventBus(), false, parameters, query, accessibleQuery, accessibleSchema, chaser, connection, costEstimator, successDominance,
					null, validators, relations.length);
			explorer.explore();
			explorer.getExploredPlans();
			List<Entry<RelationalTerm, Cost>> exploredPlans = explorer.getExploredPlans();
			Assert.assertNotNull(explorer.getBestPlan());
			Assert.assertEquals(0,(int)explorer.getBestPlan().getNumberOfInputAttributes());
			Assert.assertNotNull(exploredPlans);
			Assert.assertFalse(exploredPlans.isEmpty());
			Assert.assertEquals(120, exploredPlans.size());
			boolean topIsAlwaysDependentJoin = true;
			for (Entry<RelationalTerm, Cost> plan : exploredPlans) {
				try {
					if (printPlans)
						PlanPrinter.openPngPlan(plan.getKey());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				if (!(plan.getKey() instanceof DependentJoinTerm)) {
					topIsAlwaysDependentJoin = false;
				}
				int dependentJoints = countDependentJoinsInPlan(plan.getKey());
				Assert.assertTrue(dependentJoints >= 1); // each plan must contain at least one dependent join term.
			}

			// left deep and right deep plans have dependent join on top. This makes sure at
			// least one of them is not like that.
			Assert.assertFalse(topIsAlwaysDependentJoin);

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (PlannerException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
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
		// Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[] { this.a_s, this.b_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a_s, this.b_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a_s, this.b_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[3] = Relation.create("R3", new Attribute[] { this.a_s, this.b_s }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[4] = Relation.create("Accessible", new Attribute[] { this.a_s });
		// Create query
		Atom[] atoms = new Atom[2];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		Variable w = Variable.create("w");
		atoms[0] = Atom.create(relations[0], new Term[] { x, y });
		atoms[1] = Atom.create(relations[1], new Term[] { y, z });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, z }, atoms);

		Dependency dependency1 = TGD.create(new Atom[] { Atom.create(relations[0], new Term[] { x, y }) }, new Atom[] { Atom.create(relations[2], new Term[] { x, y }) });
		Dependency dependency2 = TGD.create(new Atom[] { Atom.create(relations[1], new Term[] { y, z }) }, new Atom[] { Atom.create(relations[3], new Term[] { y, z }) });
		// R2(x,y), R3(y,z) -> R0(x,w) R1(w,z)
		Dependency dependency3 = TGD.create(new Atom[] { Atom.create(relations[2], new Term[] { x, y }), Atom.create(relations[3], new Term[] { y, z }) },
				new Atom[] { Atom.create(relations[0], new Term[] { x, w }), Atom.create(relations[1], new Term[] { w, z }) });
		// Create schema
		Schema schema = new Schema(relations, new Dependency[] { dependency1, dependency2, dependency3 });

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
		DatabaseManager databaseConnection = null;
		try {
			ExternalDatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
			databaseConnection = new LogicalDatabaseInstance(new MultiInstanceFactCache(), dm, 1);
			databaseConnection.initialiseDatabaseForSchema(accessibleSchema);
		} catch (Exception e) {
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
		when(parameters.getMaxDepth()).thenReturn(3);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		// Create DAGGeneric
		DAGGeneric explorer = null;
		try {
			// Mock success domination
			SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
			when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
					.thenReturn(false);
			when(successDominance.clone()).thenReturn(successDominance);

			// Create validators
			List<Validator> validators = new ArrayList<>();
			validators.add(new DefaultValidator());

			explorer = new DAGGeneric(new EventBus(), false, parameters, query, accessibleQuery, accessibleSchema, chaser, databaseConnection, costEstimator, successDominance,
					null, validators, 3);

			explorer.explore();
			List<Entry<RelationalTerm, Cost>> exploredPlans = explorer.getExploredPlans();
			Assert.assertNotNull(exploredPlans);
			Assert.assertFalse(exploredPlans.isEmpty());
			Assert.assertEquals(8, exploredPlans.size());
		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	// utility function to do asserts on the accessible schema
	private void assertAccessibleSchema(AccessibleSchema accessibleSchema, Schema schema, int numberOfAxioms) {
		Assert.assertNotNull(accessibleSchema);

		// accessibility axioms
		Assert.assertNotNull(accessibleSchema.getAccessibilityAxioms());
		Assert.assertEquals(numberOfAxioms, accessibleSchema.getAccessibilityAxioms().length);
		int abcd = 0;
		int anythingElse = 0;
		int cdab = 0;
		for (AccessibilityAxiom axiom : accessibleSchema.getAccessibilityAxioms()) {
			if (axiom.getBoundVariables().length == 4 && axiom.getBoundVariables()[0].equals(Variable.create("a"))) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("a"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[2], Variable.create("c"));
				Assert.assertEquals(axiom.getBoundVariables()[3], Variable.create("d"));
				abcd++;
			} else if (axiom.getBoundVariables().length == 4 && axiom.getBoundVariables()[0].equals(Variable.create("c"))) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("c"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("d"));
				Assert.assertEquals(axiom.getBoundVariables()[2], Variable.create("a"));
				Assert.assertEquals(axiom.getBoundVariables()[3], Variable.create("b"));
				cdab++;
			} else {
				anythingElse++;
			}
		}
		Assert.assertEquals(3, abcd);
		Assert.assertEquals(0, anythingElse);
		Assert.assertEquals(2, cdab);

		Assert.assertNotNull(accessibleSchema.getRelations());
		Assert.assertEquals(10, accessibleSchema.getRelations().length);
		Dependency[] infAccAxioms = accessibleSchema.getInferredAccessibilityAxioms();
		Assert.assertNotNull(infAccAxioms);
		Assert.assertEquals(0, infAccAxioms.length);
	}

}
