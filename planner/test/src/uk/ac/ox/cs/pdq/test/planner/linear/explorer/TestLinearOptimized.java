package uk.ac.ox.cs.pdq.test.planner.linear.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.cost.estimators.CountNumberOfAccessedRelationsCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
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
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.cost.CostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderIndependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearOptimized;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.util.PlanTree;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Tests the TestLinearOptimized explorer class.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestLinearOptimized {

	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
	protected Attribute InstanceID = Attribute.create(Integer.class, "InstanceID");
	
	@Test 
	public void test1ExplorationStepsA() {
		GlobalCounterProvider.resetCounters();
		GlobalCounterProvider.getNext("CannonicalName");
		test1ExplorationSteps();
	}
	@Test 
	public void test1ExplorationStepsB() {
		GlobalCounterProvider.resetCounters();
		GlobalCounterProvider.getNext("CannonicalName");
		test1ExplorationSteps();
	}
	@SuppressWarnings("rawtypes")
	public void test1ExplorationSteps() {
		//Create the relations
		Relation[] relations = new Relation[4];
		relations[0] = Relation.create("R0", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{})});
		relations[1] = Relation.create("R1", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{0})});
		relations[2] = Relation.create("R2", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{1})});
		relations[3] = Relation.create("Accessible", new Attribute[]{this.a,this.InstanceID});
		//Create query
		Atom[] atoms = new Atom[3];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		atoms[0] = Atom.create(relations[0], new Term[]{x,Variable.create("y1"),Variable.create("z1")});
		atoms[1] = Atom.create(relations[1], new Term[]{x,y,Variable.create("z2")});
		atoms[2] = Atom.create(relations[2], new Term[]{Variable.create("x1"),y,z});
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[]{x,y,z}, (Conjunction) Conjunction.of(atoms));
		
		//Create schema
		Schema schema = new Schema(relations);
		schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(5)));

		//Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		
		assertAccessibleSchema(accessibleSchema, schema,3);
		
		//Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query, query.getSubstitutionOfFreeVariablesToCanonicalConstants());
	
		//Create database connection
		DatabaseConnection databaseConnection = null;
		try {
			databaseConnection = new DatabaseConnection(new DatabaseParameters(), accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Create the chaser 
		RestrictedChaser chaser = new RestrictedChaser(null);
		
		//Create the cost estimator 
		OrderIndependentCostEstimator costEstimator = new CountNumberOfAccessedRelationsCostEstimator(null);
		
		//Create the cost propagator
		CostPropagator costPropagatpor = new OrderIndependentCostPropagator(costEstimator);
				
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);
		
		//Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);
				
		//Create linear explorer
		LinearOptimized explorer = null;
		try {
				explorer = new LinearOptimized(
					new EventBus(), 
					false,
					query, 
					accessibleQuery,
					accessibleSchema, 
					chaser, 
					databaseConnection, 
					costEstimator,
					costPropagatpor,
					nodeFactory,
					parameters.getMaxDepth(),
					1,
					null,
					false);
				
				PlanTree<SearchNode> planTree = null;
				planTree = explorer.getPlanTree();
				SearchNode root = planTree.getRoot();
				LinearChaseConfiguration configuration0 = root.getConfiguration();
				Assert.assertEquals(1,configuration0.getCandidates().size());
				
				//Call the explorer for first time
				explorer.performSingleExplorationStep();
				Assert.assertEquals(0,configuration0.getCandidates().size());
				LinearChaseConfiguration configuration1 = planTree.getVertex(root.getId()+1).getConfiguration();
				Assert.assertEquals(1,configuration1.getCandidates().size());
				Assert.assertEquals(1,configuration1.getFacts().size());
				Atom a = configuration1.getFacts().iterator().next();
				Assert.assertEquals("R0",a.getPredicate().getName());
				Assert.assertArrayEquals(new Term[] {UntypedConstant.create("c1"),UntypedConstant.create("c2"),UntypedConstant.create("c3")}, a.getTerms());
				
				//Call the explorer for second time
				explorer.performSingleExplorationStep();
				LinearChaseConfiguration configuration2 = planTree.getVertex(root.getId()+2).getConfiguration();
				Assert.assertEquals(1,configuration2.getCandidates().size());
				Assert.assertEquals(1,configuration2.getFacts().size());
				a = configuration2.getFacts().iterator().next();
				Assert.assertEquals("R1",a.getPredicate().getName());
				Assert.assertArrayEquals(new Term[] {UntypedConstant.create("c1"),UntypedConstant.create("c4"),UntypedConstant.create("c5")}, a.getTerms());
				
				//Call the explorer for third time
				explorer.performSingleExplorationStep();
				LinearChaseConfiguration configuration3 = planTree.getVertex(root.getId()+3).getConfiguration();
				Assert.assertEquals(0,configuration3.getCandidates().size());
				Assert.assertEquals(1,configuration3.getFacts().size());
				a = configuration3.getFacts().iterator().next();
				Assert.assertEquals("R2",a.getPredicate().getName());
				Assert.assertArrayEquals(new Term[] {UntypedConstant.create("c6"),UntypedConstant.create("c4"),UntypedConstant.create("c7")}, a.getTerms());

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

	@SuppressWarnings("rawtypes")
	@Test 
	public void test2ExplorationSteps() {
		//Create the relations
		Relation[] relations = new Relation[4];
		relations[0] = Relation.create("R0", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{0})});
		relations[1] = Relation.create("R1", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{0})});
		relations[2] = Relation.create("R2", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{1})});
		relations[3] = Relation.create("Accessible", new Attribute[]{this.a,this.InstanceID});

		//Create query
		Atom[] atoms = new Atom[3];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		atoms[0] = Atom.create(relations[0], new Term[]{x,Variable.create("y1"),Variable.create("z1")});
		atoms[1] = Atom.create(relations[1], new Term[]{x,y,Variable.create("z2")});
		atoms[2] = Atom.create(relations[2], new Term[]{Variable.create("x1"),y,z});
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[]{x,y,z}, (Conjunction) Conjunction.of(atoms));
		
		//Create schema
		Schema schema = new Schema(relations);
		schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(5)));

		//Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		
		assertAccessibleSchema(accessibleSchema, schema,3);
		
		//Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query, query.getSubstitutionOfFreeVariablesToCanonicalConstants());
	
		//Create database connection
		DatabaseConnection databaseConnection = null;
		try {
			databaseConnection = new DatabaseConnection(new DatabaseParameters(), accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Create the chaser 
		RestrictedChaser chaser = new RestrictedChaser(null);
		
		//Create the cost estimator 
		OrderIndependentCostEstimator costEstimator = new CountNumberOfAccessedRelationsCostEstimator(null);
		
		//Create the cost propagator
		CostPropagator costPropagatpor = new OrderIndependentCostPropagator(costEstimator);
				
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);
		
		//Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);
				
		//Create linear explorer
		LinearOptimized explorer = null;
		try {
			explorer = new LinearOptimized(
					new EventBus(), 
					false,
					query, 
					accessibleQuery,
					accessibleSchema, 
					chaser, 
					databaseConnection, 
					costEstimator,
					costPropagatpor,
					nodeFactory,
					parameters.getMaxDepth(),
					1,
					null,
					false);
				
				PlanTree<SearchNode> planTree = null;
				planTree = explorer.getPlanTree();
				SearchNode root = planTree.getRoot();
				LinearChaseConfiguration configuration0 = root.getConfiguration();
				Assert.assertEquals(0,configuration0.getCandidates().size());
				
				//Call the explorer
				explorer.explore();
				
				//TODO Assert that at the end of exploration we found a single plan
				//This is done by checking that only one path in the plan tree leads to a match
				//TODO Assert also that every path other than the successful one is dominated by the path that leads to a query match  
				//Domination is checked by iterating over all nodes in the plan tree that are not in the successful path
				//and checking that getDominatingPlan() and getCostOfDominatingPlan() 
				//return the only successful plan we found and its cost
		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Test 
	public void test3ExplorationSteps() {
		//Create the relations
		Relation[] relations = new Relation[4];
		relations[0] = Relation.create("R0", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{})});
		relations[1] = Relation.create("R1", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{0}),AccessMethod.create(new Integer[]{2})});
		relations[2] = Relation.create("R2", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{1})});
		relations[3] = Relation.create("Accessible", new Attribute[]{this.a,this.InstanceID});

		//Create query
		Atom[] atoms = new Atom[3];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		atoms[0] = Atom.create(relations[0], new Term[]{x,Variable.create("y1"),Variable.create("z1")});
		atoms[1] = Atom.create(relations[1], new Term[]{x,y,TypedConstant.create(5)});
		atoms[2] = Atom.create(relations[2], new Term[]{Variable.create("x1"),y,z});
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[]{x,y,z}, (Conjunction) Conjunction.of(atoms));
		
		//Create schema
		Schema schema = new Schema(relations);
		schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(5)));
		
		//Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		
		assertAccessibleSchema(accessibleSchema, schema,4);
		
		
		//Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query, query.getSubstitutionOfFreeVariablesToCanonicalConstants());
	
		//Create database connection
		DatabaseConnection databaseConnection = null;
		try {
			databaseConnection = new DatabaseConnection(new DatabaseParameters(), accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		
		//Create the chaser 
		RestrictedChaser chaser = new RestrictedChaser(null);
		
		//Create the cost estimator 
		OrderIndependentCostEstimator costEstimator = new CountNumberOfAccessedRelationsCostEstimator(null);
		
		//Create the cost propagator
		CostPropagator costPropagatpor = new OrderIndependentCostPropagator(costEstimator);
				
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);
		
		//Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);
				
		//Create linear explorer
		LinearOptimized explorer = null;
		try {
			explorer = new LinearOptimized(
					new EventBus(), 
					false,
					query, 
					accessibleQuery,
					accessibleSchema, 
					chaser, 
					databaseConnection, 
					costEstimator,
					costPropagatpor,
					nodeFactory,
					parameters.getMaxDepth(),
					1,
					null,
					false);
				
				explorer.explore();
				//TODO Assert that at the end of exploration we found a single plan
				//This is done by checking that only one path in the plan tree leads to a match
				//TODO Assert also that every path other than the successful one is dominated by the path that leads to a query match  
				//Domination is checked by iterating over all nodes in the plan tree that are not in the successful path
				//and checking that getDominatingPlan() and getCostOfDominatingPlan() 
				//return the only successful plan we found and its cost
		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	private void assertAccessibleSchema(AccessibleSchema accessibleSchema, Schema schema, int numberOfAxioms) {
		Assert.assertNotNull(accessibleSchema);
		
		// constants
		Assert.assertEquals(1,accessibleSchema.getConstants().size());
		Assert.assertEquals(TypedConstant.create(5),accessibleSchema.getConstant("5"));
		// accessibility axioms
		Assert.assertNotNull(accessibleSchema.getAccessibilityAxioms());
		Assert.assertEquals(numberOfAxioms,accessibleSchema.getAccessibilityAxioms().length);
		int abc=0;
		int bc=0;
		int bac=0;
		for (AccessibilityAxiom axiom:accessibleSchema.getAccessibilityAxioms()) {
			if (axiom.getBoundVariables().length==2) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("c"));
				bc++;
			} else
			if (axiom.getBoundVariables().length==3 && axiom.getBoundVariables()[0].equals(Variable.create("a"))) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("a"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[2], Variable.create("c"));
				abc++;
			} else
			if (axiom.getBoundVariables().length==3 && axiom.getBoundVariables()[0].equals(Variable.create("b"))) {
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

}
