package uk.ac.ox.cs.pdq.test.planner.linear.explorer.equivalence;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.cost.estimators.CountNumberOfAccessedRelationsCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderIndependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearOptimized;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.equivalence.LinearEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlanTree;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 *  tests the linear EquivalenceClasses object.
 * @author gabor
 *
 */
public class TestLinearEquivalenceClasses extends PdqTest {

	public TestLinearEquivalenceClasses() {
	}
	
	@Test
	public void testClassCreation() throws PlannerException, SQLException, LimitReachedException {
		//Create the relations
		TestScenario ts = getScenario1();
		//Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());
		
		//Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(ts.getQuery());
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(ts.getQuery());
		Map<Variable, Constant> substitutionFiltered = new HashMap<>(); 
		substitutionFiltered.putAll(substitution);
		for(Variable variable:ts.getQuery().getBoundVariables()) 
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(ts.getQuery(),substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(ts.getQuery(),substitutionFiltered);
		ExplorationSetUp.getCanonicalSubstitution().put(accessibleQuery,substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(accessibleQuery,substitutionFiltered);

		//Create database connection
		DatabaseManager databaseConnection = null;
		try {
			databaseConnection = createConnection(accessibleSchema);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
		
		//Create the chaser 
		RestrictedChaser chaser = new RestrictedChaser();
		
		//Create the cost estimator 
		OrderIndependentCostEstimator costEstimator = new CountNumberOfAccessedRelationsCostEstimator();
		
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);
		
		//Create linear explorer
		LinearOptimized explorer = new LinearOptimized(
					new EventBus(), 
					ts.getQuery(), 
					accessibleSchema, 
					chaser, 
					databaseConnection, 
					costEstimator,
					new OrderIndependentCostPropagator(costEstimator),
					parameters.getMaxDepth(),
					1);
				
		PlanTree<SearchNode> planTree = null;
		planTree = explorer.getPlanTree();
		SearchNode root = planTree.getRoot();
		LinearChaseConfiguration configuration0 = root.getConfiguration();
		Assert.assertEquals(1,configuration0.getCandidates().size());
		
		//Call the explorer for first time
		explorer.performSingleExplorationStep();
		LinearEquivalenceClasses eq = new LinearEquivalenceClasses();
		// adding first
		for (SearchNode node:explorer.getPlanTree().vertexSet()) {
			ChaseConfiguration representative = eq.add(node.getConfiguration());
			Assert.assertEquals(representative,node.getConfiguration());
		}
		// chacking back
		for (SearchNode node:explorer.getPlanTree().vertexSet()) {
			ChaseConfiguration representative = eq.searchRepresentative(node.getConfiguration());
			Assert.assertEquals(representative,node.getConfiguration());
		}
	}
	
	private DatabaseManager createConnection(Schema s) {
		try {
			InternalDatabaseManager connection = new InternalDatabaseManager();
			connection.initialiseDatabaseForSchema(s);
			return connection;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
