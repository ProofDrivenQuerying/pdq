package uk.ac.ox.cs.pdq.test.planner.linear.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
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
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearGeneric;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.util.PlanTree;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestLinearGeneric {

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
		
		//Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		
		//TODO assert that the accessible schema is fine
		
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
		
		//Mock the cost estimator 
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));
				
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(Integer.MAX_VALUE);
		
		//Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);
				
		//Create linear explorer
		LinearGeneric explorer = null;
		try {
				explorer = new LinearGeneric(
					new EventBus(), 
					false,
					query, 
					accessibleQuery,
					accessibleSchema, 
					chaser, 
					databaseConnection, 
					costEstimator,
					nodeFactory,
					parameters.getMaxDepth());
				
				PlanTree<SearchNode> planTree = null;
				planTree = explorer.getPlanTree();
				SearchNode root = planTree.getRoot();
				LinearChaseConfiguration configuration0 = root.getConfiguration();
				Assert.assertEquals(1,configuration0.getCandidates().size());
				
				//Call the explorer for first time
				explorer._explore();
				Assert.assertEquals(0,configuration0.getCandidates().size());
				LinearChaseConfiguration configuration1 = planTree.getVertex(root.getId()+1).getConfiguration();
				Assert.assertEquals(1,configuration1.getCandidates().size());
				Assert.assertEquals(1,configuration1.getFacts().size());
				Atom a = configuration1.getFacts().iterator().next();
				Assert.assertEquals("R0",a.getPredicate().getName());
				Assert.assertArrayEquals(new Term[] {UntypedConstant.create("c1"),UntypedConstant.create("c2"),UntypedConstant.create("c3")}, a.getTerms());
				
				
				//Call the explorer for second time
				explorer._explore();
				LinearChaseConfiguration configuration2 = planTree.getVertex(root.getId()+2).getConfiguration();
				Assert.assertEquals(1,configuration2.getCandidates().size());
				Assert.assertEquals(1,configuration2.getFacts().size());
				a = configuration2.getFacts().iterator().next();
				Assert.assertEquals("R1",a.getPredicate().getName());
				Assert.assertArrayEquals(new Term[] {UntypedConstant.create("c1"),UntypedConstant.create("c4"),UntypedConstant.create("c5")}, a.getTerms());
				
				//Call the explorer for third time
				explorer._explore();
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
		} catch (LimitReachedException e) {
			e.printStackTrace();
		}
	}
	
	private static void AssertHasAccessTermChild(RelationalTerm relationalTerm) {
		Assert.assertNotNull(relationalTerm);
		Assert.assertNotNull(relationalTerm.getChildren());
		Assert.assertEquals(1, relationalTerm.getChildren().length);
		Assert.assertTrue(relationalTerm.getChild(0) instanceof AccessTerm);
	}

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
		
		//Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		
		//TODO assert that the accessible schema is fine
		
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
		
		//Mock the cost estimator 
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));
				
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(Integer.MAX_VALUE);
		
		//Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);
				
		//Create linear explorer
		LinearGeneric explorer = null;
		try {
				explorer = new LinearGeneric(
					new EventBus(), 
					false,
					query, 
					accessibleQuery,
					accessibleSchema, 
					chaser, 
					databaseConnection, 
					costEstimator,
					nodeFactory,
					parameters.getMaxDepth());
				
				PlanTree<SearchNode> planTree = null;
				planTree = explorer.getPlanTree();
				SearchNode root = planTree.getRoot();
				LinearChaseConfiguration configuration0 = root.getConfiguration();
				Assert.assertEquals(0,configuration0.getCandidates().size());
				
				//Call the explorer for first time
				explorer._explore();
				Assert.assertNull(planTree.getVertex(1));
				Assert.assertNull(explorer.getBestPlan());
				  
		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
		} catch (LimitReachedException e) {
			e.printStackTrace();
		}
	}
	
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
		
		//TODO assert that the accessible schema is fine
		
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
		
		//Mock the cost estimator 
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));
				
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(Integer.MAX_VALUE);
		
		//Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);
				
		//Create linear explorer
		LinearGeneric explorer = null;
		try {
				explorer = new LinearGeneric(
					new EventBus(), 
					false,
					query, 
					accessibleQuery,
					accessibleSchema, 
					chaser, 
					databaseConnection, 
					costEstimator,
					nodeFactory,
					parameters.getMaxDepth());
				
				explorer.explore();
				List<Entry<RelationalTerm, Cost>> exploredPlans = explorer.getExploredPlans();
				Assert.assertNotNull(exploredPlans);
				Assert.assertFalse(exploredPlans.isEmpty());
				Assert.assertEquals(4, exploredPlans.size());
		} catch (PlannerException | SQLException e) {
			e.printStackTrace();
		} catch (LimitReachedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private DatabaseParameters getMySqlConfig() {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setConnectionUrl("jdbc:mysql://localhost/");
		dbParam.setDatabaseDriver("com.mysql.jdbc.Driver");
		dbParam.setDatabaseName("test_get_triggers");
		dbParam.setDatabaseUser("root");
		dbParam.setDatabasePassword("root");
		return dbParam; 
	}
	
	private DatabaseParameters getPostgresConfig() {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setConnectionUrl("jdbc:postgresql://localhost/");
		dbParam.setDatabaseDriver("org.postgresql.Driver");
		dbParam.setDatabaseName("test_get_triggers");
		dbParam.setDatabaseUser("postgres");
		dbParam.setDatabasePassword("root");
		return dbParam; 
	}
	@Test 
	public void test1ExplorationThreeRelationsDerby() {
		List<Entry<RelationalTerm, Cost>> exploredPlans = findExploredPlans(3,new DatabaseParameters());
		Assert.assertEquals(68, exploredPlans.size());
	}
	@Test 
	public void test1ExplorationThreeRelationsMySql() {
		List<Entry<RelationalTerm, Cost>> exploredPlans = findExploredPlans(3,getMySqlConfig());
		Assert.assertEquals(68, exploredPlans.size());
	}
	@Test 
	public void test1ExplorationThreeRelationsPostgres() {
		List<Entry<RelationalTerm, Cost>> exploredPlans = findExploredPlans(3,getPostgresConfig());
		Assert.assertEquals(68, exploredPlans.size());
	}
	//@Test
	public void test1ExplorationFiveRelationsDerby() {
		List<Entry<RelationalTerm, Cost>> exploredPlans = findExploredPlans(5,new DatabaseParameters());
		Assert.assertEquals(6, exploredPlans.size());
	}
	//@Test 
	public void test1ExplorationFiveRelationsMySql() {
		List<Entry<RelationalTerm, Cost>> exploredPlans = findExploredPlans(5,getMySqlConfig());
		Assert.assertEquals(6, exploredPlans.size());
	}
	//@Test 
	public void test1ExplorationFiveRelationsPostgres() {
		List<Entry<RelationalTerm, Cost>> exploredPlans = findExploredPlans(5,getPostgresConfig());
		Assert.assertEquals(6, exploredPlans.size());
	}
	
	public List<Entry<RelationalTerm, Cost>> findExploredPlans(int numberOfRelations, DatabaseParameters dbParams) {
		//Create the relations
		Relation[] relations = new Relation[(int) (numberOfRelations + Math.pow(2.0, numberOfRelations))+1];
		for(int index = 0; index < numberOfRelations; ++index) 
			relations[index] = Relation.create("R" + index, new Attribute[]{this.a, this.b, this.c, this.d, this.InstanceID}, 
					new AccessMethod[]{AccessMethod.create(new Integer[0])});
		relations[numberOfRelations] = Relation.create("Accessible", new Attribute[]{this.a,this.InstanceID});

		//Create a conjunctive query that joins all relations in the first three positions 
		Random random = new Random();
		Atom[] atoms = new Atom[numberOfRelations];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		for(int index = 0; index < numberOfRelations; ++index)
			atoms[index] = Atom.create(relations[index], new Term[]{x,y,z,Variable.create("v" + random.nextInt())});
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[]{x,y,z}, (Conjunction) Conjunction.of(atoms));
		
		//Create all views and update the relations with the newly create views
		Set<Atom> setOfAtoms = new LinkedHashSet<>();
		for(int index = 0; index < numberOfRelations; ++index)
			setOfAtoms.add(atoms[index]);
		Set<Set<Atom>> powerSet = Sets.powerSet(setOfAtoms);
		int powersetIndex = 0;
		int dependencyIndex = 0;
		int viewIndex = numberOfRelations+1;
		Dependency[] dependencies = new Dependency[(powerSet.size()-1)*2];
		for(Set<Atom> set:powerSet) {
			View view = new View("V" + powersetIndex++, new Attribute[]{this.a, this.b, this.c,this.InstanceID}, new AccessMethod[]{AccessMethod.create(new Integer[0])});
			relations[viewIndex++] = view;
			int index = 0;
			Atom[] head = new Atom[set.size()];
			Iterator<Atom> iterator = set.iterator();
			while(iterator.hasNext())
				head[index++] = iterator.next();
			if (index!=0) {
				LinearGuarded viewToRelationDependency = LinearGuarded.create(Atom.create(view, new Term[]{x,y,z}), head);
				view.setViewToRelationDependency(viewToRelationDependency);
				dependencies[dependencyIndex++] = view.getViewToRelationDependency();
				dependencies[dependencyIndex++] = view.getRelationToViewDependency();
			}
		}
		
		//Create schema
		Schema schema = new Schema(relations, dependencies);
		
		//Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		
		//TODO verify that the accessible schema is fine
		
		//Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query, query.getSubstitutionOfFreeVariablesToCanonicalConstants());
	
		//Create database connection
		DatabaseConnection databaseConnection = null;
		try {
			databaseConnection = new DatabaseConnection(dbParams, accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Create the chaser 
		RestrictedChaser chaser = new RestrictedChaser(null);
		
		//Mock the cost estimator 
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));
				
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(numberOfRelations);
		
		//Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);
				
		//Create linear explorer
		LinearGeneric explorer = null;
		try {
				explorer = new LinearGeneric(
					new EventBus(), 
					false,
					query, 
					accessibleQuery,
					accessibleSchema, 
					chaser, 
					databaseConnection, 
					costEstimator,
					nodeFactory,
					parameters.getMaxDepth());
				explorer.explore();
		} catch (Throwable e) {
			// exception expected after further exploration fails.
		}
		return explorer.getExploredPlans();
	}
	
}
