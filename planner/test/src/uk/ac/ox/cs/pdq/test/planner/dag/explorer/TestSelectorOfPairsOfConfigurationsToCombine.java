package uk.ac.ox.cs.pdq.test.planner.dag.explorer;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
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
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.PlanPrinter;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.FollowUpHandling;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGExplorerUtilities;
import uk.ac.ox.cs.pdq.planner.dag.explorer.SelectorOfPairsOfConfigurationsToCombine;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestSelectorOfPairsOfConfigurationsToCombine {

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
		test1GetNextPairOfConfigurationsToCompose();
	}

	public void test1GetNextPairOfConfigurationsToCompose() {
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
			
			List<Validator> validators = new ArrayList<>(); 
			validators.add(new DefaultValidator());
			List<DAGChaseConfiguration> left = configurations;
			List<DAGChaseConfiguration> right = configurations;

			Pair<DAGChaseConfiguration, DAGChaseConfiguration> pair = null;
			SelectorOfPairsOfConfigurationsToCombine<AccessibleChaseInstance> selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right, validators);
			List<DAGChaseConfiguration> right2 = new ArrayList<>();
			while ((pair = selector.getNextPairOfConfigurationsToCompose(2)) != null) {
				BinaryConfiguration configuration = new BinaryConfiguration(
						pair.getLeft(),
						pair.getRight());
				right2.add(configuration);
			}
			// AB, AC, AD
			// BA, BC, BD
			// CA, CB, CD
			// DA, DB, DC
			Assert.assertEquals(12,right2.size());
			
			selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right2, validators);
			List<DAGChaseConfiguration> right3 = new ArrayList<>();
			while ((pair = selector.getNextPairOfConfigurationsToCompose(3)) != null) {
				BinaryConfiguration configuration = new BinaryConfiguration(
						pair.getLeft(),
						pair.getRight());
				right3.add(configuration);
			}
			// AB, AC, AD
			// BA, BC, BD   + A   =       BC A,BD A,CB A,CD A,DB A,DC A    * 4 = 6*4 =24 combination. plus inverse for all = 48
			//							  A BC,A BD,A CB,A CD,A DB A DC	
			// CA, CB, CD   + B   =       AC B,AD B,CA B,CD B,DA B,DC B
			// DA, DB, DC
			
			Assert.assertEquals(48,right3.size());
		
			selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right3, validators);
			List<DAGChaseConfiguration> right4 = new ArrayList<>();
			while ((pair = selector.getNextPairOfConfigurationsToCompose(4)) != null) {
				BinaryConfiguration configuration = new BinaryConfiguration(
						pair.getLeft(),
						pair.getRight());
				right4.add(configuration);
				try {
					if (printPlans) PlanPrinter.openPngPlan(configuration.getPlan());
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			//
			//BC A,BD A,CB A,CD A,DB A,DC A    + B    =CD A B, DC A B -> and inverse  B CD A, B DC A = 4 combinations with B, 12 combinations with b c and d.
			//A BC,A BD,A CB,A CD,A DB A DC	   we have 8 rows, 8 * 12 = 96.
			Assert.assertEquals(96,right4.size());
			
			selector = new SelectorOfPairsOfConfigurationsToCombine<>(right2, right2, validators);
			List<DAGChaseConfiguration> right4b = new ArrayList<>();
			while ((pair = selector.getNextPairOfConfigurationsToCompose(4)) != null) {
				BinaryConfiguration configuration = new BinaryConfiguration(
						pair.getLeft(),
						pair.getRight());
				right4b.add(configuration);
				try {
					if (printPlans) PlanPrinter.openPngPlan(configuration.getPlan());
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			// AB, AC, AD   + AB, AC, AD
			// BA, BC, BD     BA, BC, BD  
			// 4*3*2*1 = 24 combinations
			Assert.assertEquals(24,right4b.size());

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}


	@Test
	public void test2GetNextPairOfConfigurationsToCompose() {
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

		// Mock the cost estimator
		CostEstimator costEstimator = Mockito.mock(CostEstimator.class);
		when(costEstimator.cost(Mockito.any(RelationalTerm.class))).thenReturn(new DoubleCost(1.0));

		// Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(Integer.MAX_VALUE);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);

		try {
			List<DAGChaseConfiguration> configurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(parameters, query, accessibleQuery, accessibleSchema, chaser, connection);
			
			Set<String> predicateNames = new HashSet<>();
			for (DAGChaseConfiguration c: configurations) {
				for (ApplyRule app: c.getApplyRules()) {
					String predicateName = app.getRelation().getName() + "_" + app.getRule().getAccessMethod().getName();
					Assert.assertTrue(!predicateNames.contains(predicateName));
					predicateNames.add(predicateName);
				}
			}
			Assert.assertEquals(4,predicateNames.size());

			List<Validator> validators = new ArrayList<>(); 
			validators.add(new DefaultValidator());

			List<DAGChaseConfiguration> left = configurations;
			List<DAGChaseConfiguration> right = new ArrayList<>();
			right.addAll(configurations);

			Pair<DAGChaseConfiguration, DAGChaseConfiguration> pair = null;
			SelectorOfPairsOfConfigurationsToCombine<AccessibleChaseInstance> selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right, validators);
			int depth = 2;
			final int CORRECT_NUMBER_OF_PLANS[] = new int[] {10,24,0}; 
			do {
				Collection<DAGChaseConfiguration> last = new LinkedHashSet<>();
				while ((pair = selector.getNextPairOfConfigurationsToCompose(depth)) != null) {
					BinaryConfiguration configuration = new BinaryConfiguration(
							pair.getLeft(),
							pair.getRight());
					last.add(configuration);
				}
				System.out.println("last size: " + last.size());
				Assert.assertEquals(CORRECT_NUMBER_OF_PLANS[depth-2], last.size());
				if (last.size()==0)
					break;
				left.clear();
				left.addAll(last);
				last.clear();
				selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right, validators);
				depth++;
			}while(true);

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
