package uk.ac.ox.cs.pdq.test.planner.dag.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
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
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.PlanPrinter;
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
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Tests the DAG generic class. Makes sure we have all possible plans explored.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestDAGGeneric {

	protected Attribute alpha = Attribute.create(Integer.class, "alpha");
	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
	protected Attribute InstanceID = Attribute.create(Integer.class, "InstanceID");
	private final boolean printPlans = false;
	
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
	public void test1() {
		GlobalCounterProvider.resetCounters();
		GlobalCounterProvider.getNext("CannonicalName");
		test1ExplorationSteps();
	}

	public void test1ExplorationSteps() { 
		// Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[] { this.a, this.b, this.c, this.d, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a, this.b, this.c, this.d, this.InstanceID },
				new AccessMethod[] { AccessMethod.create(new Integer[] { 2, 3 }) });

		relations[2] = Relation.create("R2", new Attribute[] { this.a, this.b, this.c, this.d, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[3] = Relation.create("R3", new Attribute[] { this.a, this.b, this.c, this.d, this.InstanceID },
				new AccessMethod[] { AccessMethod.create(new Integer[] { 2, 3 }) });
		relations[4] = Relation.create("Accessible", new Attribute[] { this.a, this.InstanceID });
		// Create query
		// R0(x,y,z,w) R1(_,_,z,w) R2(x,y,z',w') R3(_,_,z',w')
		Atom[] atoms = new Atom[4];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		Variable w = Variable.create("w");
		atoms[0] = Atom.create(relations[0], new Term[] { x, y, z, w });
		atoms[1] = Atom.create(relations[1], new Term[] { Variable.create("x2"), Variable.create("y2"), z, w });
		atoms[2] = Atom.create(relations[2], new Term[] { x, y, Variable.create("z3"), Variable.create("w3") });
		atoms[3] = Atom.create(relations[3], new Term[] { Variable.create("x4"), Variable.create("y4"), Variable.create("z3"), Variable.create("w3") });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y }, (Conjunction) Conjunction.of(atoms));

		// Create schema
		Schema schema = new Schema(relations);

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		assertAccessibleSchema(accessibleSchema, schema, 4);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query, query.getSubstitutionOfFreeVariablesToCanonicalConstants());

		// Create database connection
		DatabaseConnection connection = null;
		try {
			DatabaseParameters mySqlDbParam = new DatabaseParameters();
			mySqlDbParam.setConnectionUrl("jdbc:mysql://localhost/");
			mySqlDbParam.setDatabaseDriver("com.mysql.jdbc.Driver");
			mySqlDbParam.setDatabaseName("test_get_triggers");
			mySqlDbParam.setDatabaseUser("root");
			mySqlDbParam.setDatabasePassword("root");
			DatabaseParameters postgresDbParam = new DatabaseParameters();
			postgresDbParam.setConnectionUrl("jdbc:postgresql://localhost/");
			postgresDbParam.setDatabaseDriver("org.postgresql.Driver");
			postgresDbParam.setDatabaseName("test_get_triggers");
			postgresDbParam.setDatabaseUser("postgres");
			postgresDbParam.setDatabasePassword("root");
			
			connection = new DatabaseConnection(mySqlDbParam, accessibleSchema);
		} catch (SQLException e) {
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

		//Mock success domination
		SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
		when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
				.thenReturn(false);
		when(successDominance.clone()).thenReturn(successDominance);

		//Create validators
		List<Validator> validators = new ArrayList<>();
		validators.add(new DefaultValidator());

		try {
			DAGGeneric explorer = new DAGGeneric(new EventBus(), false, parameters, query, accessibleQuery, accessibleSchema, chaser, connection, costEstimator, successDominance,
					null, validators, 4);
			explorer.explore();
			explorer.getExploredPlans();
			List<Entry<RelationalTerm, Cost>> exploredPlans = explorer.getExploredPlans();
			Assert.assertNotNull(exploredPlans);
			Assert.assertFalse(exploredPlans.isEmpty());
			Assert.assertEquals(30, exploredPlans.size());
			boolean topIsAlwaysDependentJoin = true;
			for (Entry<RelationalTerm, Cost> plan: exploredPlans) {
				try {
					if (printPlans) PlanPrinter.openPngPlan(plan.getKey());
				} catch(Throwable t) {
					t.printStackTrace();
				}
				if (!(plan.getKey() instanceof DependentJoinTerm)) {
					topIsAlwaysDependentJoin = false;
				}
				int dependentJoints = countDependentJoinsInPlan(plan.getKey());
				Assert.assertTrue(dependentJoints >= 1); // each plan must contain at least one dependent join term.
			}
			
			// left deep and right deep plans have dependent join on top. This makes sure at least one of them is not like that.
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
	
	private int countDependentJoinsInPlan(RelationalTerm key) {
		int ret = 0;
		if (key instanceof DependentJoinTerm) {
			ret++;
		}
		for (RelationalTerm child:key.getChildren()) {
			ret += countDependentJoinsInPlan(child);
		}
		return ret;
	}

	private void assertAccessibleSchema(AccessibleSchema accessibleSchema, Schema schema, int numberOfAxioms) {
		Assert.assertNotNull(accessibleSchema);

		// constants
		Assert.assertEquals(0, accessibleSchema.getConstants().size());
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
		Assert.assertEquals(2, abcd);
		Assert.assertEquals(0, anythingElse);
		Assert.assertEquals(2, cdab);

		Assert.assertNotNull(accessibleSchema.getRelations());
		Assert.assertEquals(10, accessibleSchema.getRelations().length);
		Dependency[] infAccAxioms = accessibleSchema.getInferredAccessibilityAxioms();
		Assert.assertNotNull(infAccAxioms);
		Assert.assertEquals(0, infAccAxioms.length);
	}

	@Test
	public void test3ExplorationSteps() {
		// Create the relations
		Relation[] relations = new Relation[4];
		relations[0] = Relation.create("R0", new Attribute[] { this.a, this.b, this.c, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a, this.b, this.c, this.InstanceID },
				new AccessMethod[] { AccessMethod.create(new Integer[] { 0 }), AccessMethod.create(new Integer[] { 2 }) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a, this.b, this.c, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] { 1 }) });
		relations[3] = Relation.create("Accessible", new Attribute[] { this.a, this.InstanceID });

		// Create query
		Atom[] atoms = new Atom[3];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		atoms[0] = Atom.create(relations[0], new Term[] { x, Variable.create("y1"), Variable.create("z1") });
		atoms[1] = Atom.create(relations[1], new Term[] { x, y, TypedConstant.create(5) });
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
			DatabaseParameters mySqlDbParam = new DatabaseParameters();
			mySqlDbParam.setConnectionUrl("jdbc:mysql://localhost/");
			mySqlDbParam.setDatabaseDriver("com.mysql.jdbc.Driver");
			mySqlDbParam.setDatabaseName("test_get_triggers");
			mySqlDbParam.setDatabaseUser("root");
			mySqlDbParam.setDatabasePassword("root");
			
			databaseConnection = new DatabaseConnection(mySqlDbParam, accessibleSchema);
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
		when(parameters.getMaxDepth()).thenReturn(3);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		// Create DAGGeneric
		DAGGeneric explorer = null;
		try {
			//Mock success domination
			SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
			when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
					.thenReturn(false);
			when(successDominance.clone()).thenReturn(successDominance);

			//Create validators
			List<Validator> validators = new ArrayList<>();
			validators.add(new DefaultValidator());
			
			explorer = new DAGGeneric(new EventBus(), false, parameters, query, accessibleQuery, accessibleSchema, chaser, databaseConnection, costEstimator, successDominance, null,
					validators, 3);

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
	 *  Creates a schema out of 4,8 or 12 relations where each four relation forms a busy sub-plan. Current assertions are set to validate the results of a chase execution of 8 relations.  
	 */
	@Test
	public void test4LargeBushyPlanExploration() {
		final int NUMBER_OF_RELATIONS = 8; // can be 4, 8 or 12. With 8 relations, and using the ClosedValidator it should be successful in 3-4sec. with 12 it takes way too long.
		// Create the relations
		Relation[] relations = new Relation[NUMBER_OF_RELATIONS+1];
		for (int i = 0; i < relations.length-1; i++) {
			if (i%2 == 0)
				relations[i] = Relation.create("R"+i, new Attribute[] { this.a, this.b, this.c, this.d, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
			else 
				relations[i] = Relation.create("R"+i, new Attribute[] { this.a, this.b, this.c, this.d, this.InstanceID },
						new AccessMethod[] { AccessMethod.create(new Integer[] { 2, 3 }) });
		}
		relations[relations.length-1] = Relation.create("Accessible", new Attribute[] { this.a, this.InstanceID });
		// Create query
		// R0(x,y,z,w) R1(_,_,z,w) R2(x,y,z',w') R3(_,_,z',w')
		Atom[] atoms = new Atom[relations.length-1];
		List<Variable> head = new ArrayList<>();
		for (int z= 0; z<(relations.length-1)/4; z++) {
			Variable x = Variable.create("x"+z);
			head.add(x);
			Variable y = Variable.create("y"+z);
			head.add(y);
			Variable v = Variable.create("z"+z);
			Variable w = Variable.create("w"+z);
			atoms[4*z + 0] = Atom.create(relations[4*z + 0], new Term[] { x, y, v, w });
			atoms[4*z + 1] = Atom.create(relations[4*z + 1], new Term[] { Variable.create("x"+z+"b"), Variable.create("y"+z+"b"), v, w });
			atoms[4*z + 2] = Atom.create(relations[4*z + 2], new Term[] { x, y, Variable.create("z"+z+"c"), Variable.create("w"+z+"c") });
			atoms[4*z + 3] = Atom.create(relations[4*z + 3], new Term[] { Variable.create("x"+z+"d"), Variable.create("y"+z+"d"), Variable.create("z"+z+"c"), Variable.create("w"+z+"c") });
		}
		ConjunctiveQuery query = ConjunctiveQuery.create(head.toArray(new Variable[head.size()]), (Conjunction) Conjunction.of(atoms));

		// Create schema
		Schema schema = new Schema(relations);

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query, query.getSubstitutionOfFreeVariablesToCanonicalConstants());

		// Create database connection
		DatabaseConnection connection = null;
		try {
			DatabaseParameters postgresDbParam = new DatabaseParameters();
			postgresDbParam.setConnectionUrl("jdbc:postgresql://localhost/");
			postgresDbParam.setDatabaseDriver("org.postgresql.Driver");
			postgresDbParam.setDatabaseName("test_get_triggers");
			postgresDbParam.setDatabaseUser("postgres");
			postgresDbParam.setDatabasePassword("root");
			
			connection = new DatabaseConnection(postgresDbParam, accessibleSchema);
		} catch (SQLException e) {
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

		//Mock success domination
		SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
		when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
				.thenReturn(false);

		//Create validators
		List<Validator> validators = new ArrayList<>();
		//validators.add(new DefaultValidator());
		validators.add(new ClosedValidator());

		try {
			DAGGeneric explorer = new DAGGeneric(new EventBus(), false, parameters, query, accessibleQuery, accessibleSchema, chaser, connection, costEstimator, successDominance,
					null, validators, relations.length);
			explorer.explore();
			explorer.getExploredPlans();
			List<Entry<RelationalTerm, Cost>> exploredPlans = explorer.getExploredPlans();
			Assert.assertNotNull(exploredPlans);
			Assert.assertFalse(exploredPlans.isEmpty());
			Assert.assertEquals(120, exploredPlans.size());
			boolean topIsAlwaysDependentJoin = true;
			for (Entry<RelationalTerm, Cost> plan: exploredPlans) {
				try {
					if (printPlans) PlanPrinter.openPngPlan(plan.getKey());
				} catch(Throwable t) {
					t.printStackTrace();
				}
				if (!(plan.getKey() instanceof DependentJoinTerm)) {
					topIsAlwaysDependentJoin = false;
				}
				int dependentJoints = countDependentJoinsInPlan(plan.getKey());
				Assert.assertTrue(dependentJoints >= 1); // each plan must contain at least one dependent join term.
			}
			
			// left deep and right deep plans have dependent join on top. This makes sure at least one of them is not like that.
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
	 *  The query is Q(x,z) = \exists y R0(x,y) R1(y,z) 
	 *  We also have the dependencies 
	 *  R0(x,y) -> R2(x,y) 
	 *  R1(y,z) -> R3(y,z) 
	 *  R2(x,y), R3(y,z) -> R0(x,w) R1(w,z) 
	 *  Every relation has a free access. Suppose that we found the plan that performs accesses in the following order R2(x,y) R3(y,z) R0(x,y) R1(y,z) 
	 *  Postpruning should return R2(x,y) R3(y,z)
	 *  
	 *  We should find at least the following plans 
	 *  R0(x,y) R1(y,z) 
	 *  R3(x,y) R0(x,y) R1(y,z) 
	 *  R4(y,z) R0(x,y) R1(y,z) 
	 *  R3(x,y) R4(y,z) 
	 *  R4(y,z) R3(x,y) 
	 *  R4(y,z) R3(x,y) R0(x,y) R1(y,z)
 	 */
	@Test
	public void test5() {
		// Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[] { this.a, this.b, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a, this.b, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a, this.b, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[3] = Relation.create("R3", new Attribute[] { this.a, this.b, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[4] = Relation.create("Accessible", new Attribute[] { this.a, this.InstanceID });
		// Create query
		Atom[] atoms = new Atom[2];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		Variable w = Variable.create("w");
		atoms[0] = Atom.create(relations[0], new Term[] { x, y });
		atoms[1] = Atom.create(relations[1], new Term[] { y, z });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, z }, (Conjunction) Conjunction.of(atoms));

		Dependency dependency1 = TGD.create(new Atom[] { Atom.create(relations[0], new Term[] { x, y })},
				new Atom[] { Atom.create(relations[2], new Term[] { x, y })});
		Dependency dependency2 = TGD.create(new Atom[] { Atom.create(relations[1], new Term[] { y, z })},
				new Atom[] { Atom.create(relations[3], new Term[] { y, z })});
		//R2(x,y), R3(y,z) -> R0(x,w) R1(w,z)
		Dependency dependency3 = TGD.create(new Atom[] { Atom.create(relations[2], new Term[] { x, y }), Atom.create(relations[3], new Term[] { y, z })},
				new Atom[] { Atom.create(relations[0], new Term[] { x, w }), Atom.create(relations[1], new Term[] { w, z })});
		// Create schema
		Schema schema = new Schema(relations, new Dependency[] { dependency1, dependency2, dependency3 });

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query, query.getSubstitutionOfFreeVariablesToCanonicalConstants());

		// Create database connection
		DatabaseConnection databaseConnection = null;
		try {
			DatabaseParameters mySqlDbParam = new DatabaseParameters();
			mySqlDbParam.setConnectionUrl("jdbc:mysql://localhost/");
			mySqlDbParam.setDatabaseDriver("com.mysql.jdbc.Driver");
			mySqlDbParam.setDatabaseName("test_get_triggers");
			mySqlDbParam.setDatabaseUser("root");
			mySqlDbParam.setDatabasePassword("root");			
			databaseConnection = new DatabaseConnection(mySqlDbParam, accessibleSchema);
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
		when(parameters.getMaxDepth()).thenReturn(3);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		// Create DAGGeneric
		DAGGeneric explorer = null;
		try {
			//Mock success domination
			SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
			when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
					.thenReturn(false);
			when(successDominance.clone()).thenReturn(successDominance);

			//Create validators
			List<Validator> validators = new ArrayList<>();
			validators.add(new DefaultValidator());
			
			explorer = new DAGGeneric(new EventBus(), false, parameters, query, accessibleQuery, accessibleSchema, chaser, databaseConnection, costEstimator, successDominance, null,
					validators, 3);

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
