package uk.ac.ox.cs.pdq.test.planner.dag.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

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
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.FollowUpHandling;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGExplorerUtilities;
import uk.ac.ox.cs.pdq.planner.dag.explorer.SelectorOfPairsOfConfigurationsToCombine;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class TestSelectorOfPairsOfConfigurationsToCombine {

	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
	protected Attribute InstanceID = Attribute.create(Integer.class, "InstanceID");

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

		//TODO assert that the accessible schema is fine

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
			//TODO assert that we got the right configurations 

			List<Validator> validators = new ArrayList<>(); 
			validators.add(new DefaultValidator());

			List<DAGChaseConfiguration> left = configurations;
			List<DAGChaseConfiguration> right = configurations;

			Pair<DAGChaseConfiguration, DAGChaseConfiguration> pair = null;
			SelectorOfPairsOfConfigurationsToCombine selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right, validators);

			while ((pair = selector.getNextPairOfConfigurationsToCompose(4)) != null) {
				//TODO assert that we got all pairs 
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			//TODO assert that we got the right configurations 

			List<Validator> validators = new ArrayList<>(); 
			validators.add(new DefaultValidator());

			List<DAGChaseConfiguration> left = configurations;
			List<DAGChaseConfiguration> right = configurations;

			Pair<DAGChaseConfiguration, DAGChaseConfiguration> pair = null;
			SelectorOfPairsOfConfigurationsToCombine selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right, validators);

			do {
				Collection<DAGChaseConfiguration> last = new LinkedHashSet<>();
				while ((pair = selector.getNextPairOfConfigurationsToCompose(4)) != null) {
					//TODO assert that we got all pairs 
					BinaryConfiguration configuration = new BinaryConfiguration(
							pair.getLeft(),
							pair.getRight());
					last.add(configuration);
				}
				
				left.clear();
				left.addAll(last);
				last.clear();
				selector = new SelectorOfPairsOfConfigurationsToCombine<>(left, right, validators);
			}while(true);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
