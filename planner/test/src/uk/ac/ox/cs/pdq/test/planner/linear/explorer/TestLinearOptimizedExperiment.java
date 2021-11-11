// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.planner.linear.explorer;

import com.google.common.eventbus.EventBus;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CountNumberOfAccessedRelationsCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.exceptions.LimitReachedException;
import uk.ac.ox.cs.pdq.fol.*;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleQuery;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.equivalence.linear.LinearEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.cost.CostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderIndependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearOptimized;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.plantree.PlanTree;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.ChaseConfiguration;
import uk.ac.ox.cs.pdq.reasoning.chase.ParallelChaser;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.PdqTest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * Tests the LinearOptimized explorer class (this class was originally called
 * LinearOptimizedExperiment). Uses the same 3 scenario as the other two linear
 * chaser test does, and has a test case to validate the functionality of the
 * new linear optimized algorithm.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestLinearOptimizedExperiment extends PdqTest {

	private LogicalDatabaseInstance connection;

	/**
	 * Tests the explorer with the Scenario1 input schema and query. Asserts the
	 * best plan found should be something like:
	 * 
	 * <pre>
	 * DependentJoin { [(#4=#7)]
	 * 		DependentJoin{ [(#0=#3)]
	 * 			Rename{[c1,c2,c3]
	 * 				Access{R0.mt_0[]}
	 * 			},
	 * 			Rename{[c1,c4,c5]
	 * 				Access{R1.mt_1[#0=a]}
	 * 			}
	 * 		},
	 * 		Rename{[c6,c4,c7]
	 * 				Access{R2.mt_2[#1=b]}
	 * 		}
	 * }
	 * </pre>
	 */
	@Test
	@SuppressWarnings("rawtypes")
	public void test1ExplorationSteps() {
		GlobalCounterProvider.getNext("CannonicalName");
		// Create the relations
		TestScenario ts = getScenario1();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());

		assertAccessibleSchema(accessibleSchema, ts.getSchema(), 3);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = AccessibleQuery.createAccessibleQuery(ts.getQuery());
		Map<Variable, Constant> substitution = ChaseConfiguration
				.generateSubstitutionToCanonicalVariables(ts.getQuery());
		Map<Variable, Constant> substitutionFiltered = new HashMap<>();
		substitutionFiltered.putAll(substitution);
		for (Variable variable : ts.getQuery().getBoundVariables())
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(ts.getQuery(), substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(ts.getQuery(), substitutionFiltered);
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
		ParallelChaser chaser = new ParallelChaser();

		// Create the cost estimator
		OrderIndependentCostEstimator costEstimator = new CountNumberOfAccessedRelationsCostEstimator();

		// Create the cost propagator
		CostPropagator costPropagatpor = new OrderIndependentCostPropagator(costEstimator);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);

		// Create linear explorer
		LinearOptimized explorer = null;
		try {
			explorer = new LinearOptimized(new EventBus(), ts.getQuery(), accessibleSchema, chaser, databaseConnection,
					costEstimator, costPropagatpor, parameters.getMaxDepth(), 1);

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
			Assert.assertArrayEquals(new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"),
					UntypedConstant.create("c3") }, a.getTerms());

			// Call the explorer for second time
			explorer.performSingleExplorationStep();
			LinearChaseConfiguration configuration2 = planTree.getVertex(root.getId() + 2).getConfiguration();
			Assert.assertEquals(1, configuration2.getCandidates().size());
			Assert.assertEquals(1, configuration2.getFacts().size());
			a = configuration2.getFacts().iterator().next();
			Assert.assertEquals("R1", a.getPredicate().getName());
			Assert.assertArrayEquals(new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c4"),
					UntypedConstant.create("c5") }, a.getTerms());

			// Call the explorer for third time
			explorer.performSingleExplorationStep();
			LinearChaseConfiguration configuration3 = planTree.getVertex(root.getId() + 3).getConfiguration();
			Assert.assertEquals(0, configuration3.getCandidates().size());
			Assert.assertEquals(1, configuration3.getFacts().size());
			a = configuration3.getFacts().iterator().next();
			Assert.assertEquals("R2", a.getPredicate().getName());
			Assert.assertArrayEquals(new Term[] { UntypedConstant.create("c6"), UntypedConstant.create("c4"),
					UntypedConstant.create("c7") }, a.getTerms());

			// checking the plan
			RelationalTerm plan = explorer.getBestPlan();
			Assert.assertNotNull(plan);
			Assert.assertTrue(plan.getChild(0) instanceof DependentJoinTerm);
			Assert.assertEquals(0, plan.getChild(0).getInputAttributes().length);
			Assert.assertEquals(9, plan.getChild(0).getOutputAttributes().length);
			Assert.assertEquals(2, plan.getChild(0).getChildren().length);
			Assert.assertTrue(plan.getChild(0).getChildren()[0] instanceof DependentJoinTerm);
			Assert.assertTrue(plan.getChild(0).getChildren()[1] instanceof RenameTerm);
			AssertHasAccessTermChild(plan.getChild(0).getChildren()[1]);
			RelationalTerm subPlan = plan.getChild(0).getChildren()[0];
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
	 * Tests with scenario2, asserts that we have no valid plan.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void test2ExplorationSteps() {
		TestScenario ts = getScenario2();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());

		assertAccessibleSchema(accessibleSchema, ts.getSchema(), 3);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = AccessibleQuery.createAccessibleQuery(ts.getQuery());
		Map<Variable, Constant> substitution = ChaseConfiguration
				.generateSubstitutionToCanonicalVariables(ts.getQuery());
		Map<Variable, Constant> substitutionFiltered = new HashMap<>();
		substitutionFiltered.putAll(substitution);
		for (Variable variable : ts.getQuery().getBoundVariables())
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(ts.getQuery(), substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(ts.getQuery(), substitutionFiltered);
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
		ParallelChaser chaser = new ParallelChaser();

		// Create the cost estimator
		OrderIndependentCostEstimator costEstimator = new CountNumberOfAccessedRelationsCostEstimator();

		// Create the cost propagator
		CostPropagator costPropagatpor = new OrderIndependentCostPropagator(costEstimator);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);

		// Create linear explorer
		LinearOptimized explorer = null;
		try {
			explorer = new LinearOptimized(new EventBus(), ts.getQuery(), accessibleSchema, chaser, databaseConnection,
					costEstimator, costPropagatpor, parameters.getMaxDepth(), 1);

			PlanTree<SearchNode> planTree = null;
			planTree = explorer.getPlanTree();
			SearchNode root = planTree.getRoot();
			LinearChaseConfiguration configuration0 = root.getConfiguration();
			Assert.assertEquals(0, configuration0.getCandidates().size());

			// Call the explorer
			explorer.explore();

			RelationalTerm dominatingPlan = root.getDominatingPlan();
			Cost dominatingPlansCost = root.getCostOfDominatingPlan();
			Assert.assertEquals(dominatingPlansCost, root.getCostOfDominatingPlan());
			for (int i = 0; planTree.getVertex(root.getId() + i) != null; i++) {
				SearchNode node = planTree.getVertex(root.getId() + i);
				Assert.assertEquals(dominatingPlansCost, node.getCostOfDominatingPlan());
				Assert.assertEquals(dominatingPlan, node.getDominatingPlan());
			}
			Assert.assertNull(dominatingPlan); // no valid plan in this example.
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
	 * Tests with PdqTest.scenario3, asserts that all plans are dominated by the
	 * best plan.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void test3ExplorationSteps() {
		TestScenario ts = getScenario3();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());

		assertAccessibleSchema(accessibleSchema, ts.getSchema(), 4);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = AccessibleQuery.createAccessibleQuery(ts.getQuery());
		Map<Variable, Constant> substitution = ChaseConfiguration
				.generateSubstitutionToCanonicalVariables(ts.getQuery());
		Map<Variable, Constant> substitutionFiltered = new HashMap<>();
		substitutionFiltered.putAll(substitution);
		for (Variable variable : ts.getQuery().getBoundVariables())
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(ts.getQuery(), substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(ts.getQuery(), substitutionFiltered);
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
		ParallelChaser chaser = new ParallelChaser();

		// Create the cost estimator
		OrderIndependentCostEstimator costEstimator = new CountNumberOfAccessedRelationsCostEstimator();

		// Create the cost propagator
		CostPropagator costPropagatpor = new OrderIndependentCostPropagator(costEstimator);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		Mockito.reset(parameters);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);

		// Create linear explorer
		LinearOptimized explorer = null;
		try {
			explorer = new LinearOptimized(new EventBus(), ts.getQuery(), accessibleSchema, chaser, databaseConnection,
					costEstimator, costPropagatpor, parameters.getMaxDepth(), 1);

			explorer.explore();
			PlanTree<SearchNode> planTree = null;
			planTree = explorer.getPlanTree();
			SearchNode root = planTree.getRoot();

			RelationalTerm dominatingPlan = null;
			Cost dominatingPlansCost = null;

			Assert.assertEquals(dominatingPlansCost, root.getCostOfDominatingPlan());
			for (int i = root.getId(); planTree.getVertex(root.getId() + i) != null; i++) {
				SearchNode node = planTree.getVertex(root.getId() + i);
				if (node.getDominatingPlan() != null) {
					if (dominatingPlan != null) {
						Assert.assertEquals(dominatingPlan, node.getDominatingPlan());
						Assert.assertEquals(dominatingPlansCost, node.getCostOfDominatingPlan());
					} else {
						dominatingPlan = node.getDominatingPlan(); // there should be only one successful plan.
						dominatingPlansCost = node.getCostOfDominatingPlan();
					}
				}
			}
			Assert.assertNotNull(dominatingPlan);

		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Checks most of the properties of the accessible schema. Throws assertion
	 * error in case there is a change in the schema configuration.
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
			} else if (axiom.getBoundVariables().length == 3
					&& axiom.getBoundVariables()[0].equals(Variable.create("a"))) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("a"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[2], Variable.create("c"));
				abc++;
			} else if (axiom.getBoundVariables().length == 3
					&& axiom.getBoundVariables()[0].equals(Variable.create("b"))) {
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
		Assert.assertEquals(9, accessibleSchema.getRelations().length);
		Dependency[] infAccAxioms = accessibleSchema.getInferredAccessibilityAxioms();
		Assert.assertNotNull(infAccAxioms);
		Assert.assertEquals(0, infAccAxioms.length);
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

	/**
	 * <pre>
	 * Let the query Q= R(x) wedge U(x)
	 * 
	 * 
	 * 
	 *  Constraints Sigma= R(x) —> S(x), S(x) —> R(x), T(x) —> U(x), U(x) —> T(x)
	 *  So constraint copy Sigma’= InfaccR(x) —> InfaccS(x), InfaccS(x) —> InfaccR(x)
	 *  InfaccT(x)—> InfaccU(x), InfaccU(x) —> InfaccT(x)
	 *  
	 *  Free access on R, S, T, U
	 *  Initial configuration after chasing canonicaldb of
	 *  Q is n0= {R(x0), S(x0), T(x0), U(x0)}; obbviously n0  is its
	 *  own representative
	
	 *  Candidates in n0 R(x0), S(x0), T(x0), U(x0)
	 *  
	 *  
	 *  1) Explore candidate R(x0) in n0
	 *  
	 *  
	 *  create child node n1 initially with InfaccR(x0), 
	 *  
	 *  we haven’t already exposed R(x0) in an equivalent node,
	 *  so chase to get 
	 *  InfaccR(x0), InfaccS(x0)
	 *  
	 *  check for equivalent node after chasing, but there is none,
	 *  so n1 is its own representative.
	 *  
	 *  Current tree: n0 with child n1
	 *  
	 *  2) Explore candidate R(x0) in n0
	 *  
	 *  created child node n2 initially adding on InfaccS(x0)
	 *  
	 *  again, we have not already exposed R(x0) in an equivalent
	 *  node, so chase to get InfaccS(x0), InfaccR(x0). n2 is equivalent
	 *  to n1, so set n1 to be the representative of n2
	 *  
	 *  Current tree: n0 with children n1 and n2, with n1 and n2 equivalent
	 *  
	 *  3) Explore candidate T(x0) in n1
	 *  
	 *  create child node n3 initially adding InfaccT(x0)
	 *  we have not already exposed T(x0) in n1, so chase
	 *  to get InfaccU(x0). 
	 *  n3 is not equivalent to any existing node, so set n3 to be its
	 *  own representative
	 *  
	 *  Current tree: n0 with children n1 and n2; n1 has child n3
	 *  
	 *  4) Explore candidate T(x0) in n2
	 *  
	 *  Create a new node n4
	 *  We have already exposed T(x0) in n1, so re-use the configuration of
	 *  n3 to get the configuration of n4, set representative of n4 to n3
	 *  
	 *  Current tree:no with children n1 and n2 that are equivalent;
	 *  n1 has child n3, n2 has child n4; n3 and n4 are also equivalent
	 * </pre>
	 * 
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void testNewOptimization() {
		// Relations
		Relation R = Relation.create("R", new Attribute[] { this.a }, new AccessMethodDescriptor[] { method0 });
		Relation S = Relation.create("S", new Attribute[] { this.a }, new AccessMethodDescriptor[] { method0 });
		Relation T = Relation.create("T", new Attribute[] { this.a }, new AccessMethodDescriptor[] { method0 });
		Relation U = Relation.create("U", new Attribute[] { this.a }, new AccessMethodDescriptor[] { method0 });

		// Constraints Sigma= R(x) —> S(x),
		TGD d1 = TGD.create(new Atom[] { Atom.create(R, new Term[] { x }) },
				new Atom[] { Atom.create(S, new Term[] { x }) });
		// S(x) —> R(x)
		TGD d2 = TGD.create(new Atom[] { Atom.create(S, new Term[] { x }) },
				new Atom[] { Atom.create(R, new Term[] { x }) });
		// T(x) —> U(x)
		TGD d3 = TGD.create(new Atom[] { Atom.create(T, new Term[] { x }) },
				new Atom[] { Atom.create(U, new Term[] { x }) });
		// U(x) —> T(x)
		TGD d4 = TGD.create(new Atom[] { Atom.create(U, new Term[] { x }) },
				new Atom[] { Atom.create(T, new Term[] { x }) });

		Schema s = new Schema(new Relation[] { R, S, T, U }, new Dependency[] { d1, d2, d3, d4 });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] {},
				new Atom[] { Atom.create(R, new Term[] { x }), Atom.create(U, new Term[] { x }) });
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(s);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = AccessibleQuery.createAccessibleQuery(cq);
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(cq);
		Map<Variable, Constant> substitutionFiltered = new HashMap<>();
		substitutionFiltered.putAll(substitution);
		for (Variable variable : cq.getBoundVariables())
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(cq, substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(cq, substitutionFiltered);
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
		ParallelChaser chaser = new ParallelChaser();

		// Create the cost estimator
		OrderIndependentCostEstimator costEstimator = new CountNumberOfAccessedRelationsCostEstimator();

		// Create the cost propagator
		CostPropagator costPropagatpor = new OrderIndependentCostPropagator(costEstimator);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		Mockito.reset(parameters);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);

		// Create linear explorer
		LinearOptimized explorer = null;
		try {
			explorer = new LinearOptimized(new EventBus(), cq, accessibleSchema, chaser, databaseConnection,
					costEstimator, costPropagatpor, parameters.getMaxDepth(), 1);
			SearchNode root = explorer.getPlanTree().getRoot();
			Candidate candidateForN1 = root.getConfiguration().getCandidates().get(0);
			Set<Candidate> candidates = root.getConfiguration().getSimilarCandidates(candidateForN1);

			// 2 steps of exploration:
			// create n1
			explorer.explorationStep(root, candidateForN1, candidates);
			// create n2
			explorer.explorationStep(root, candidateForN1, candidates);

			// Checking results:
			PlanTree<SearchNode> planTree = explorer.getPlanTree();
			LinearEquivalenceClasses classes = explorer.getLinearEquivalenceClasses();
			// there should be two representative at this stage. One for n0[n0], and one for
			// n1[n1,n2]
			Assert.assertEquals(2, classes.getRepresentatives().size());
			// root has no equal node.
			Assert.assertEquals(1, classes.getEquivalenceClass(planTree.getRoot()).size());
			// nodes 1 and 2 should be in the same classes
			Assert.assertEquals(2, classes.getEquivalenceClass(planTree.getVertex(1)).size());
			Assert.assertEquals(2,
					classes.getEquivalenceClass(classes.searchRepresentative(planTree.getVertex(2))).size());
			Assert.assertEquals(classes.searchRepresentative(planTree.getVertex(1)),
					classes.searchRepresentative(planTree.getVertex(2)));

			Candidate candidateForN3 = planTree.getVertex(1).getConfiguration().getCandidates().get(1);
			Set<Candidate> candidatesN3 = root.getConfiguration().getSimilarCandidates(candidateForN3);
			Candidate candidateForN4 = planTree.getVertex(2).getConfiguration().getCandidates().get(1);
			Set<Candidate> candidatesN4 = root.getConfiguration().getSimilarCandidates(candidateForN3);
			// 2 more steps of exploration:
			// create n3
			explorer.explorationStep(planTree.getVertex(1), candidateForN3, candidatesN3);
			// create n4 -- Since n3 is a successful node already,
			// therefore a new node was created throwing off the node numbering by one. this
			// means the N4 node's verted ID will be 5.
			explorer.explorationStep(planTree.getVertex(2), candidateForN4, candidatesN4);

			// assert equality classes
			// there should be three representative at this stage:
			// n0[n0],n1[n1,n2],n3[n3,n4]
			Assert.assertEquals(3, classes.getRepresentatives().size());
			// root has no equal node.
			Assert.assertEquals(1, classes.getEquivalenceClass(planTree.getRoot()).size());
			// nodes 1 and 2 should be in the same classes
			Assert.assertEquals(2, classes.getEquivalenceClass(planTree.getVertex(1)).size());
			Assert.assertEquals(2,
					classes.getEquivalenceClass(classes.searchRepresentative(planTree.getVertex(2))).size());
			Assert.assertEquals(classes.searchRepresentative(planTree.getVertex(1)),
					classes.searchRepresentative(planTree.getVertex(2)));

			// making sure n3 and n4 are equal
			int N3 = 3;
			int N4 = 5;// the vertex ID is off by one node that was created internally when we created
						// n3.
			Assert.assertEquals(2, classes.getEquivalenceClass(planTree.getVertex(N3)).size());
			Assert.assertEquals(2,
					classes.getEquivalenceClass(classes.searchRepresentative(planTree.getVertex(N4))).size());
			Assert.assertEquals(classes.searchRepresentative(planTree.getVertex(N3)),
					classes.searchRepresentative(planTree.getVertex(N4)));

		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}
