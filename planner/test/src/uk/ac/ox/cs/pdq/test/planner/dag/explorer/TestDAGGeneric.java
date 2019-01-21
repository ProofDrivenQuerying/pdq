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

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
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
import uk.ac.ox.cs.pdq.planner.PlannerParameters.FollowUpHandling;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleQuery;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGGenericSimple;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultPairValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.PairValidator;
import uk.ac.ox.cs.pdq.planner.dominance.CostDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Tests the DAGGenericSimple class. Makes sure we have all possible plans explored.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestDAGGeneric extends PdqTest {
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
		ConjunctiveQuery accessibleQuery = AccessibleQuery.createAccessibleQuery(ts.getQuery());
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
			databaseConnection = new InternalDatabaseManager();
			databaseConnection.initialiseDatabaseForSchema(accessibleSchema);
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
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		// Create DAGGenericSimple
		DAGGenericSimple explorer = null;
		try {
			// Mock success domination
			CostDominance successDominance = Mockito.mock(CostDominance.class);
			when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
					.thenReturn(false);
			when(successDominance.clone()).thenReturn(successDominance);

			// Create validators
			List<PairValidator> validators = new ArrayList<>();
			validators.add(new DefaultPairValidator());

			explorer = new DAGGenericSimple(new EventBus(), parameters, ts.getQuery(), accessibleSchema, chaser, databaseConnection, costEstimator,
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
		relations[0] = Relation.create("R0", new Attribute[] { this.a_s, this.b_s }, new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a_s, this.b_s }, new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a_s, this.b_s }, new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });
		relations[3] = Relation.create("R3", new Attribute[] { this.a_s, this.b_s }, new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });
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
		ConjunctiveQuery accessibleQuery = AccessibleQuery.createAccessibleQuery(query);
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
			databaseConnection = new InternalDatabaseManager();
			databaseConnection.initialiseDatabaseForSchema(accessibleSchema);
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
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		// Create DAGGenericSimple
		DAGGenericSimple explorer = null;
		try {
			// Mock success domination
			CostDominance successDominance = Mockito.mock(CostDominance.class);
			when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
					.thenReturn(false);
			when(successDominance.clone()).thenReturn(successDominance);

			// Create validators
			List<PairValidator> validators = new ArrayList<>();
			validators.add(new DefaultPairValidator());

			explorer = new DAGGenericSimple(new EventBus(), parameters, query, accessibleSchema, chaser, databaseConnection, costEstimator, successDominance,
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

}
