package uk.ac.ox.cs.pdq.test.planner.dag.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

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
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
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
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestMultiThreadedExecutor {

	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
	protected Attribute InstanceID = Attribute.create(Integer.class, "InstanceID");
	
	boolean printPlans=false;

	@Test 
	public void test1() {
		GlobalCounterProvider.resetCounters();
		GlobalCounterProvider.getNext("CannonicalName");
		test1CreateBinaryConfigurations();
	}

	public void test1CreateBinaryConfigurations() {
		//Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[]{this.a, this.b, this.c, this.d, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{})});
		relations[1] = Relation.create("R1", new Attribute[]{this.a, this.b, this.c, this.d, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{2,3})});

		relations[2] = Relation.create("R2", new Attribute[]{this.a, this.b, this.c, this.d, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{})});
		relations[3] = Relation.create("R3", new Attribute[]{this.a, this.b, this.c, this.d, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{2,3})});
		relations[4] = Relation.create("Accessible", new Attribute[]{this.a,this.InstanceID});
		//Create query
		//R0(x,y,z,w) R1(_,_,z,w) R2(x,y,z',w') R3(_,_,z',w')
		Atom[] atoms = new Atom[4];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		Variable w = Variable.create("w");
		atoms[0] = Atom.create(relations[0], new Term[]{x,y,z,w});
		atoms[1] = Atom.create(relations[1], new Term[]{Variable.create("x2"), Variable.create("y2"),z,w});
		atoms[2] = Atom.create(relations[2], new Term[]{x,y,Variable.create("z3"), Variable.create("w3")});
		atoms[3] = Atom.create(relations[3], new Term[]{Variable.create("x4"), Variable.create("y4"),Variable.create("z3"), Variable.create("w3")});
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[]{x,y}, (Conjunction) Conjunction.of(atoms));

		//Create schema
		Schema schema = new Schema(relations);

		//Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		//Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query, query.getSubstitutionOfFreeVariablesToCanonicalConstants());

		//Create database connection
		DatabaseConnection connection = null;
		try {
			connection = new DatabaseConnection(new DatabaseParameters(), accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//Create the chaser 
		RestrictedChaser chaser = new RestrictedChaser(null);

		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);
		when(parameters.getMaxDepth()).thenReturn(relations.length);

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));

		//Mock success domination
		SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
		when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
				.thenReturn(false);
		
		//Mock domination
		Dominance dominance = Mockito.mock(Dominance.class);
		when(dominance.isDominated(Mockito.any(Configuration.class), Mockito.any(Configuration.class))).thenReturn(false);

		//Create validators
		List<Validator> validators = new ArrayList<>();
		validators.add(new DefaultValidator());
		
		//Create a multitheaded executor
		int parallelThreads = 20;
		MultiThreadedContext mtcontext = null;
		try {
			mtcontext = new MultiThreadedContext(parallelThreads,
					chaser,
					connection,
					costEstimator,
					successDominance,
					new Dominance[]{dominance},
					validators);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MultiThreadedExecutor executor = new MultiThreadedExecutor(mtcontext);
		
		try {
			List<DAGChaseConfiguration> configurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(parameters, query, accessibleQuery, accessibleSchema, chaser, connection);
			Set<String> predicateNames = new HashSet<>();
			for (DAGChaseConfiguration c: configurations) {
				for (ApplyRule app: c.getApplyRules()) {
					String predicateName = app.getRelation().getName();
					Assert.assertTrue(!predicateNames.contains(predicateName));
					predicateNames.add(predicateName);
				}
			}
			Assert.assertEquals(4,predicateNames.size());
			
			Queue<DAGChaseConfiguration> leftSideConfigurations = new ConcurrentLinkedQueue<>();
			DAGEquivalenceClasses equivalenceClasses = new SynchronizedEquivalenceClasses();
			leftSideConfigurations.addAll(configurations);
			for(DAGChaseConfiguration initialConfiguration: configurations) {
				equivalenceClasses.addEntry(initialConfiguration);
			}
			
			Collection<DAGChaseConfiguration> newConfigurations = null;
			try {
				newConfigurations = executor.createBinaryConfigurations(
						2, 
						leftSideConfigurations, 
						equivalenceClasses.getConfigurations(), 
						new Dependency[]{}, 
						null, 
						equivalenceClasses, 
						true, 
						Long.MAX_VALUE, 
						TimeUnit.MILLISECONDS);
			} catch (PlannerException | LimitReachedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			leftSideConfigurations.clear();
			leftSideConfigurations.addAll(newConfigurations);
			
			//TODO call the executor two more times, one with depth 3 and one with 4
			//TODO the left hand side configurations should be the outputs of the previous step
			//TODO at each step assert that we have the right number of equivalence classes
			//TODO at each step assert that we get the right number of output configurations 
			
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}


	@Test
	public void test2CreateBinaryConfigurations() {
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
		DatabaseConnection connection = null;
		try {
			connection = new DatabaseConnection(new DatabaseParameters(), accessibleSchema);
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}

		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(relations.length);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));

		//Mock success domination
		SuccessDominance successDominance = Mockito.mock(SuccessDominance.class);
		when(successDominance.isDominated(Mockito.any(RelationalTerm.class), Mockito.any(Cost.class), Mockito.any(RelationalTerm.class), Mockito.any(Cost.class)))
				.thenReturn(false);
		
		//Mock domination
		Dominance dominance = Mockito.mock(Dominance.class);
		when(dominance.isDominated(Mockito.any(Configuration.class), Mockito.any(Configuration.class))).thenReturn(false);

		//Create validators
		List<Validator> validators = new ArrayList<>();
		validators.add(new DefaultValidator());
		
		//Create a multitheaded executor
		int parallelThreads = 20;
		MultiThreadedContext mtcontext = null;
		try {
			mtcontext = new MultiThreadedContext(parallelThreads,
					chaser,
					connection,
					costEstimator,
					successDominance,
					new Dominance[]{dominance},
					validators);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MultiThreadedExecutor executor = new MultiThreadedExecutor(mtcontext);
		
		try {
			List<DAGChaseConfiguration> configurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(parameters, query, accessibleQuery, accessibleSchema, chaser, connection);
			Set<String> predicateNames = new HashSet<>();
			for (DAGChaseConfiguration c: configurations) {
				for (ApplyRule app: c.getApplyRules()) {
					String predicateName = app.getRelation().getName();
					Assert.assertTrue(!predicateNames.contains(predicateName));
					predicateNames.add(predicateName);
				}
			}
			Assert.assertEquals(4,predicateNames.size());
			
			Queue<DAGChaseConfiguration> leftSideConfigurations = new ConcurrentLinkedQueue<>();
			DAGEquivalenceClasses equivalenceClasses = new SynchronizedEquivalenceClasses();
			leftSideConfigurations.addAll(configurations);
			for(DAGChaseConfiguration initialConfiguration: configurations) {
				equivalenceClasses.addEntry(initialConfiguration);
			}
			
			Collection<DAGChaseConfiguration> newConfigurations = null;
			try {
				newConfigurations = executor.createBinaryConfigurations(
						2, 
						leftSideConfigurations, 
						equivalenceClasses.getConfigurations(), 
						new Dependency[]{}, 
						null, 
						equivalenceClasses, 
						true, 
						Long.MAX_VALUE, 
						TimeUnit.MILLISECONDS);
			} catch (PlannerException | LimitReachedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			leftSideConfigurations.clear();
			leftSideConfigurations.addAll(newConfigurations);
			
			//TODO call the executor one more times, with depth 3
			//TODO the left hand side configurations should be the outputs of the previous step
			//TODO at each step assert that we have the right number of equivalence classes
			//TODO at each step assert that we get the right number of output configurations 
			
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
