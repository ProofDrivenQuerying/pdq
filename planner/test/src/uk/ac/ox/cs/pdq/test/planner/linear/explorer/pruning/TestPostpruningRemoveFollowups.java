package uk.ac.ox.cs.pdq.test.planner.linear.explorer.pruning;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.CountNumberOfAccessedRelationsCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderIndependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearGeneric;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearKChase;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearOptimized;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.linear.explorer.pruning.PostPruningRemoveFollowUps;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlanTree;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Tests the postpruning class.
 * 
 * @author Gabor
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPostpruningRemoveFollowups extends PdqTest {

	protected Attribute a = Attribute.create(String.class, "a");
	protected Attribute b = Attribute.create(String.class, "b");

	private LogicalDatabaseInstance connection;

	@Before
	public void setup() {
		uk.ac.ox.cs.pdq.util.Utility.assertsEnabled();
		MockitoAnnotations.initMocks(this);
		GlobalCounterProvider.resetCounters();
		uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
		uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
		uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
	}

	/**
	 * Uses Scenario1 from the PdqTest as input. This input has no successful
	 * configurations that can be pruned, so we should receive a false pruning
	 * result.
	 * </pre>
	 * 
	 */
	@Test
	public void testPruningNoChange() {
		GlobalCounterProvider.getNext("CannonicalName");
		TestScenario scenario1 = getScenario1();
		ConjunctiveQuery query = scenario1.getQuery();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(scenario1.getSchema());

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(scenario1.getQuery());
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(query);
		Map<Variable, Constant> substitutionFiltered = new HashMap<>();
		substitutionFiltered.putAll(substitution);
		for (Variable variable : query.getBoundVariables())
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(query, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(query, substitutionFiltered);
		ExplorationSetUp.getCanonicalSubstitution().put(accessibleQuery, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(accessibleQuery, substitutionFiltered);

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

		// Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);

		// Create linear explorer
		LinearGeneric explorer = null;
		try {
			explorer = new LinearGeneric(new EventBus(), scenario1.getQuery(), accessibleQuery, accessibleSchema,
					chaser, databaseConnection, costEstimator, nodeFactory, parameters.getMaxDepth());
			PostPruningRemoveFollowUps postpruning = new PostPruningRemoveFollowUps(nodeFactory, accessibleSchema,
					chaser, query);

			PlanTree<SearchNode> planTree = null;
			planTree = explorer.getPlanTree();
			SearchNode root = planTree.getRoot();
			LinearChaseConfiguration configuration0 = root.getConfiguration();
			Assert.assertEquals(1, configuration0.getCandidates().size());

			// Call the explorer
			while (explorer.getBestPlan() == null)
				explorer.performSingleExplorationStep();
			SearchNode bestNode = explorer.getBestNode();

			// find query matches
			List<Match> matches = explorer.getBestNode().getConfiguration().getState().getMatches(accessibleQuery,
					new HashMap<>());
			
			Atom[] factsInQueryMatch = uk.ac.ox.cs.pdq.reasoning.chase.Utility
					.applySubstitution(accessibleQuery, matches.get(0).getMapping()).getAtoms();

			// attempt pruning.
			if (postpruning.pruneSearchNodePath(bestNode,
					explorer.getPlanTree().getPath(bestNode.getBestPathFromRoot()), factsInQueryMatch)) {
				Assert.fail("Post pruning should have been un-successful, there is nothing to prune.");
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
	 * 
	 * The query is Q(x,y) = R(x,y) We also have the dependencies R(x,y) -> S(x,y).
	 * 
	 * <pre>
	 * Relations R and S have free accesses. 
	 * Suppose that we found the plan that performs accesses in the following order: 
	 * 			R(x,y) S(x,y) 
	 * postpruning should create a new plan: R(x,y)
	 * 
	 * Uses LinearGeneric explorer
	 * </pre>
	 */
	@Test
	public void testPruningSimplestPruning() {

		Relation R = Relation.create("R", new Attribute[] { this.a, this.b },
				new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });
		Relation S = Relation.create("S", new Attribute[] { this.a, this.b },
				new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });

		// Create a conjunctive query that joins all relations in the first three
		// positions
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Atom atom[] = new Atom[1];
		atom[0] = Atom.create(R, new Term[] { x, y });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y }, atom);

		Dependency[] dependencies = new Dependency[] { TGD.create(new Atom[] { Atom.create(R, new Term[] { x, y }) },
				new Atom[] { Atom.create(S, new Term[] { x, y }) }) };
		// Create schema
		Schema schema = new Schema(new Relation[] { R, S }, dependencies);

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query);
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(query);
		Map<Variable, Constant> substitutionFiltered = new HashMap<>();
		substitutionFiltered.putAll(substitution);
		for (Variable variable : query.getBoundVariables())
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(query, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(query, substitutionFiltered);
		ExplorationSetUp.getCanonicalSubstitution().put(accessibleQuery, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(accessibleQuery, substitutionFiltered);
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
		when(parameters.getMaxDepth()).thenReturn(2);

		// Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);

		// Create linear explorer
		LinearGeneric explorer = null;
		PostPruningRemoveFollowUps postpruning = new PostPruningRemoveFollowUps(nodeFactory, accessibleSchema, chaser,
				query);

		try {
			// create explorer
			explorer = new LinearGeneric(new EventBus(), query, accessibleQuery, accessibleSchema, chaser,
					databaseConnection, costEstimator, nodeFactory, 4);

			// first exploration step should create a plan: Rename{[c0,c1]Access{S.mt_1[]}}
			SearchNode newNode = explorer._performSingleExplorationStep();
			// Second exploration step should create a plan with 2 accesses:
			// Join{[(#0=#2&#1=#3)]Rename{[c0,c1]Access{S.mt_1[]}},Rename{[c0,c1]Access{R.mt_0[]}}}
			newNode = explorer._performSingleExplorationStep();

			// get query matches
			List<Match> matches = explorer.getBestNode().getConfiguration().getState().getMatches(accessibleQuery,
					new HashMap<>());
			Atom[] factsInQueryMatch = uk.ac.ox.cs.pdq.reasoning.chase.Utility
					.applySubstitution(accessibleQuery, matches.get(0).getMapping()).getAtoms();

			// attempt pruning the plan with the two accesses.
			if (postpruning.pruneSearchNodePath(newNode, explorer.getPlanTree().getPath(newNode.getPathFromRoot()),
					factsInQueryMatch)) {
				System.out.println("Successfully pruned node: " + newNode + " path: "
						+ Arrays.asList(newNode.getBestPathFromRoot()));
			} else {
				System.out.println("newNode: " + newNode + " path: " + Arrays.asList(newNode.getBestPathFromRoot()));
				Assert.fail("Prune should have been successful here.");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * 
	 * The query is Q(x,y) = R(x,y) We also have the dependencies R(x,y) -> S(x,y).
	 * 
	 * <pre>
	 * Relations R and S have free accesses. 
	 * Suppose that we found the plan that performs accesses in the following order: 
	 * 			R(x,y) S(x,y) 
	 * postpruning should create a new plan: R(x,y)
	 * 
	 * uses linear optimised explorer
	 * </pre>
	 */
	@Test
	public void testPruningSimplestPruningLO() {

		Relation R = Relation.create("R", new Attribute[] { this.a, this.b },
				new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });
		Relation S = Relation.create("S", new Attribute[] { this.a, this.b },
				new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });

		// Create a conjunctive query that joins all relations in the first three
		// positions
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Atom atom[] = new Atom[1];
		atom[0] = Atom.create(R, new Term[] { x, y });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y }, atom);

		Dependency[] dependencies = new Dependency[] { TGD.create(new Atom[] { Atom.create(R, new Term[] { x, y }) },
				new Atom[] { Atom.create(S, new Term[] { x, y }) }) };
		// Create schema
		Schema schema = new Schema(new Relation[] { R, S }, dependencies);

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query);
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(query);
		Map<Variable, Constant> substitutionFiltered = new HashMap<>();
		substitutionFiltered.putAll(substitution);
		for (Variable variable : query.getBoundVariables())
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(query, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(query, substitutionFiltered);
		ExplorationSetUp.getCanonicalSubstitution().put(accessibleQuery, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(accessibleQuery, substitutionFiltered);
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
		//Create the cost estimator 
		OrderIndependentCostEstimator costEstimator = new CountNumberOfAccessedRelationsCostEstimator();

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(2);

		// Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);

		// Create linear explorer
		LinearOptimized explorer = null;
		PostPruningRemoveFollowUps postpruning = new PostPruningRemoveFollowUps(nodeFactory, accessibleSchema, chaser,
				query);

		try {
			//Create the cost Propagator
			OrderIndependentCostPropagator costPropagator = new OrderIndependentCostPropagator(costEstimator);
			// create explorer
			explorer = new LinearOptimized(new EventBus(), query, accessibleQuery, accessibleSchema, chaser,
					databaseConnection, costEstimator,costPropagator, nodeFactory,4,1, postpruning,false);

			// first exploration step should create a plan: Rename{[c0,c1]Access{S.mt_1[]}}
			SearchNode newNode = explorer._performSingleExplorationStep();
			// Second exploration step should create a plan with 2 accesses:
			// Join{[(#0=#2&#1=#3)]Rename{[c0,c1]Access{S.mt_1[]}},Rename{[c0,c1]Access{R.mt_0[]}}}
			newNode = explorer._performSingleExplorationStep();

			// get query matches
			List<Match> matches = newNode.getConfiguration().getState().getMatches(accessibleQuery,
					new HashMap<>());
			Atom[] factsInQueryMatch = uk.ac.ox.cs.pdq.reasoning.chase.Utility
					.applySubstitution(accessibleQuery, matches.get(0).getMapping()).getAtoms();

			// attempt pruning the plan with the two accesses.
			if (postpruning.pruneSearchNodePath(newNode, explorer.getPlanTree().getPath(newNode.getPathFromRoot()),
					factsInQueryMatch)) {
				System.out.println("Successfully pruned node: " + newNode + " path: "
						+ Arrays.asList(newNode.getBestPathFromRoot()));
			} else {
				System.out.println("newNode: " + newNode + " path: " + Arrays.asList(newNode.getBestPathFromRoot()));
				Assert.fail("Prune should have been successful here.");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * 
	 * The query is Q(x,y) = R(x,y) We also have the dependencies R(x,y) -> S(x,y).
	 * 
	 * <pre>
	 * Relations R and S have free accesses. 
	 * Suppose that we found the plan that performs accesses in the following order: 
	 * 			R(x,y) S(x,y) 
	 * postpruning should create a new plan: R(x,y)
	 * 
	 * uses linear KChase explorer
	 * </pre>
	 */
	@Test
	public void testPruningSimplestPruningKChase() {

		Relation R = Relation.create("R", new Attribute[] { this.a, this.b },
				new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });
		Relation S = Relation.create("S", new Attribute[] { this.a, this.b },
				new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });

		// Create a conjunctive query that joins all relations in the first three
		// positions
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Atom atom[] = new Atom[1];
		atom[0] = Atom.create(R, new Term[] { x, y });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y }, atom);

		Dependency[] dependencies = new Dependency[] { TGD.create(new Atom[] { Atom.create(R, new Term[] { x, y }) },
				new Atom[] { Atom.create(S, new Term[] { x, y }) }) };
		// Create schema
		Schema schema = new Schema(new Relation[] { R, S }, dependencies);

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query);
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(query);
		Map<Variable, Constant> substitutionFiltered = new HashMap<>();
		substitutionFiltered.putAll(substitution);
		for (Variable variable : query.getBoundVariables())
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(query, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(query, substitutionFiltered);
		ExplorationSetUp.getCanonicalSubstitution().put(accessibleQuery, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(accessibleQuery, substitutionFiltered);
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
		//Create the cost estimator 
		OrderIndependentCostEstimator costEstimator = new CountNumberOfAccessedRelationsCostEstimator();

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(2);

		// Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);

		// Create linear explorer
		LinearKChase explorer = null;
		PostPruningRemoveFollowUps postpruning = new PostPruningRemoveFollowUps(nodeFactory, accessibleSchema, chaser,
				query);

		try {
			//Create the cost Propagator
			OrderIndependentCostPropagator costPropagator = new OrderIndependentCostPropagator(costEstimator);
			// create explorer
			explorer = new LinearKChase(new EventBus(), query, accessibleQuery, accessibleSchema, chaser,
					databaseConnection, costEstimator,costPropagator, nodeFactory,4,1);

			// first exploration step should create a plan: Rename{[c0,c1]Access{S.mt_1[]}}
			SearchNode newNode = explorer._performSingleExplorationStep();
			// Second exploration step should create a plan with 2 accesses:
			// Join{[(#0=#2&#1=#3)]Rename{[c0,c1]Access{S.mt_1[]}},Rename{[c0,c1]Access{R.mt_0[]}}}
			newNode = explorer._performSingleExplorationStep();

			// get query matches
			List<Match> matches = newNode.getConfiguration().getState().getMatches(accessibleQuery,
					new HashMap<>());
			Atom[] factsInQueryMatch = uk.ac.ox.cs.pdq.reasoning.chase.Utility
					.applySubstitution(accessibleQuery, matches.get(0).getMapping()).getAtoms();

			// attempt pruning the plan with the two accesses.
			if (postpruning.pruneSearchNodePath(newNode, explorer.getPlanTree().getPath(newNode.getPathFromRoot()),
					factsInQueryMatch)) {
				System.out.println("Successfully pruned node: " + newNode + " path: "
						+ Arrays.asList(newNode.getBestPathFromRoot()));
			} else {
				System.out.println("newNode: " + newNode + " path: " + Arrays.asList(newNode.getBestPathFromRoot()));
				Assert.fail("Prune should have been successful here.");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * <pre>
	 * The query is Q(x,y) = R0(x,y) R1(y,z) R2(z,w) 
	 * We have free access on R0 dependent access on R1 on the first position and dependent access on R2 on
	 * the first position again. We also have a dependency:
	 * 
	 * R0(x,y) R1(y,z) -> R3(x,y,z) 
	 * and a relation R3 with free access. Suppose that we found the plan
	 * that performs accesses in the following order:
	 * R0(x,y) R1(y,z) R3(x,y,z) R2(z,w) postpruning should eliminate the access on R3
	 * </pre>
	 */
	@Test
	public void testPruning() {
		Relation[] relations = new Relation[4];
		relations[0] = Relation.create("R0", new Attribute[] { this.a_s, this.b_s },
				new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a_s, this.b_s },
				new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] { 0 }) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a_s, this.b_s },
				new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] { 0 }) });
		relations[3] = Relation.create("R3", new Attribute[] { this.a_s, this.b_s, this.c_s },
				new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });
		// Create query
		Atom[] atoms = new Atom[3];
		atoms[2] = Atom.create(relations[0], new Term[] { x, y });
		atoms[1] = Atom.create(relations[1], new Term[] { y, z });
		atoms[0] = Atom.create(relations[2], new Term[] { z, w });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, z }, atoms);
		// dependency R0(x,y) R1(y,z) -> R3(x,y,z)
		Dependency dependency1 = TGD.create(
				new Atom[] { Atom.create(relations[0], new Term[] { x, y }),
						Atom.create(relations[1], new Term[] { y, z }) },
				new Atom[] { Atom.create(relations[3], new Term[] { x, y, z }) });
		// Create schema
		Schema schema = new Schema(relations, new Dependency[] { dependency1 });

		TestScenario ts = new TestScenario();
		ts.setSchema(schema);
		ts.setQuery(query);

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query);
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(query);
		Map<Variable, Constant> substitutionFiltered = new HashMap<>();
		substitutionFiltered.putAll(substitution);
		for (Variable variable : query.getBoundVariables())
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(query, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(query, substitutionFiltered);
		ExplorationSetUp.getCanonicalSubstitution().put(accessibleQuery, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(accessibleQuery, substitutionFiltered);

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

		// Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);

		// Create linear explorer
		LinearGeneric explorer = null;
		try {
			explorer = new LinearGeneric(new EventBus(), query, accessibleQuery, accessibleSchema, chaser,
					databaseConnection, costEstimator, nodeFactory, parameters.getMaxDepth());
			PostPruningRemoveFollowUps postpruning = new PostPruningRemoveFollowUps(nodeFactory, accessibleSchema,
					chaser, query);

			PlanTree<SearchNode> planTree = null;
			planTree = explorer.getPlanTree();
			SearchNode root = planTree.getRoot();
			LinearChaseConfiguration configuration0 = root.getConfiguration();
			Assert.assertEquals(2, configuration0.getCandidates().size());

			// Call the explorer one step at a time and mark all configuration nodes
			// generated.
			List<SearchNode> nodes = new ArrayList<>();
			while (explorer.getBestPlan() == null) {
				nodes.add(explorer._performSingleExplorationStep());
			}
			nodes.add(explorer._performSingleExplorationStep());

			// get query matches
			SearchNode bestNode = explorer.getBestNode();
			List<Match> matches = explorer.getBestNode().getConfiguration().getState().getMatches(accessibleQuery,
					new HashMap<>());
//			
//			List<Match> matches = explorer.getPlanTree().getPath(bestNode.getBestPathFromRoot())
//					.get(bestNode.getBestPathFromRoot().size() - 1).getConfiguration().getState()
//					.getMatches(accessibleQuery, new HashMap<>());

			Atom[] factsInQueryMatch = uk.ac.ox.cs.pdq.reasoning.chase.Utility
					.applySubstitution(accessibleQuery, matches.get(0).getMapping()).getAtoms();

			// Attempt pruning the best node, we know it cannot be pruned.
			if (postpruning.pruneSearchNodePath(explorer.getPlanTree().getRoot(),
					explorer.getPlanTree().getPath(bestNode.getBestPathFromRoot()), factsInQueryMatch)) {
				Assert.fail("Post pruning should have been successful, there is nothing to prune.");
			}

			// go over all nodes and find the plan with 4 accesses:
			for (SearchNode node : nodes) {
				if (node == null || node.getStatus() != NodeStatus.SUCCESSFUL) {
					// only successful nodes can be pruned.
					continue;
				}
				if (node.getBestPlanFromRoot().getAccesses().size() == 4) {
					// in case we have 4 accesses in the plan postpruning should eliminate the
					// access for R3.
					if (!postpruning.pruneSearchNodePath(explorer.getPlanTree().getRoot(),
							explorer.getPlanTree().getPath(node.getBestPathFromRoot()), factsInQueryMatch)) {
						Assert.fail("Post pruning should have been successful for configuration: "
								+ node.getBestPlanFromRoot());
					}
				} else {
					// when the plan has less than 3 accesses we know the pruning should not be successful.
					if (postpruning.pruneSearchNodePath(explorer.getPlanTree().getRoot(),
							explorer.getPlanTree().getPath(node.getBestPathFromRoot()), factsInQueryMatch)) {
						Assert.fail("Post pruning should have been UN-successful for configuration: "
								+ node.getBestPlanFromRoot());
					}
				}
			}
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
