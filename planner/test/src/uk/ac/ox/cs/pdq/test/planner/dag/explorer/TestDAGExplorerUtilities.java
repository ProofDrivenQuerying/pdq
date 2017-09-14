package uk.ac.ox.cs.pdq.test.planner.dag.explorer;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGExplorerUtilities;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Tests the DAGExplorerUtilities.createInitialApplyRuleConfigurations function over a simple case.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestDAGExplorerUtilities {

	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
	protected Attribute InstanceID = Attribute.create(Integer.class, "InstanceID");
	
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
		test1CreateInitialApplyRuleConfigurations();
	}
	
	public void test1CreateInitialApplyRuleConfigurations() {
		//Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[]{this.a, this.b, this.c, this.d, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{}),AccessMethod.create(new Integer[]{0})});
		relations[1] = Relation.create("R1", new Attribute[]{this.a, this.b, this.c, this.d, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{0}), AccessMethod.create(new Integer[]{2,3})});
		
		relations[2] = Relation.create("R2", new Attribute[]{this.a, this.b, this.c, this.d, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{})});
		relations[3] = Relation.create("R3", new Attribute[]{this.a, this.b, this.c, this.d, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{2,3})});
		relations[4] = Relation.create("Accessible", new Attribute[]{this.a,this.InstanceID});
		//Create query
		//R0('constant1',y,z,w) R1('constant2',_,z,w) R2(x,y,z',w') R3(_,_,z',w')
		Atom[] atoms = new Atom[4];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		Variable w = Variable.create("w");
		atoms[0] = Atom.create(relations[0], new Term[]{TypedConstant.create(1),y,z,w});
		atoms[1] = Atom.create(relations[1], new Term[]{TypedConstant.create(2), Variable.create("y2"),z,w});
		atoms[2] = Atom.create(relations[2], new Term[]{x,y,Variable.create("z3"), Variable.create("w3")});
		atoms[3] = Atom.create(relations[3], new Term[]{Variable.create("x4"), Variable.create("y4"),Variable.create("z3"), Variable.create("w3")});
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[]{x,y}, (Conjunction) Conjunction.of(atoms));
		
		//Create schema
		Schema schema = new Schema(relations);
		schema.addConstants(Arrays.asList(new TypedConstant[] {TypedConstant.create(1),TypedConstant.create(2)} ));
		
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
			Assert.fail();
		}
		
		//Create the chaser 
		RestrictedChaser chaser = new RestrictedChaser(null);
				
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getFollowUpHandling()).thenReturn(FollowUpHandling.MINIMAL);
		
		try {
			List<DAGChaseConfiguration> configurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(parameters, query, accessibleQuery, accessibleSchema, chaser, connection);
			Set<DAGChaseConfiguration> uniqueConfigs = new HashSet<>();
			int index = 0;
			String predicateNames[] = new String[] {"R0","R0","R1","R1","R2","R3",}; 
			for (DAGChaseConfiguration conf: configurations) {
				System.out.println(conf);
				uniqueConfigs.add(conf);
				Assert.assertTrue(conf.getApplyRules().toString().contains(predicateNames[index]));
				index++;
			}
			Assert.assertEquals(6, uniqueConfigs.size());
			Assert.assertEquals(6, configurations.size());
			
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		
		
	}
}
