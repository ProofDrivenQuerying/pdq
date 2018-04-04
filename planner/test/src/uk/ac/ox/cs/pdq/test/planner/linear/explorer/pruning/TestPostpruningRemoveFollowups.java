package uk.ac.ox.cs.pdq.test.planner.linear.explorer.pruning;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearGeneric;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.linear.explorer.pruning.PostPruningRemoveFollowUps;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * Tests the postpruning class.
 * 
 * @author Efthymia Tsamoura
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPostpruningRemoveFollowups {

	protected Attribute a = Attribute.create(String.class, "a");
	protected Attribute b = Attribute.create(String.class, "b");
	protected Attribute c = Attribute.create(String.class, "c");
	protected Attribute d = Attribute.create(String.class, "d");
	private LogicalDatabaseInstance connection;

	@Before
	public void setup() {
		uk.ac.ox.cs.pdq.util.Utility.assertsEnabled();
		MockitoAnnotations.initMocks(this);
		GlobalCounterProvider.resetCounters();
		uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
		uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
		uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
	}

	@Test
	public void notImplementedTestCases() {
		// The query is Q(x,y) = R0(x,y) R1(y,z) R2(z,w)
		// We have free access on R0
		// dependent access on R1 on the first position
		// and dependent access on R2 on the first position again.
		// We also have a dependency R0(x,y) R1(y,z) -> R3(x,y,z)
		// and a relation R3 with free access
		// Suppose that we found the plan that performs accesses in the following order
		// R0(x,y) R1(y,z) R3(x,y,z) R2(z,w)
		// postpruning should trash the access on R3

		// The query is Q(x,y) = R0(x,y) R1(y,z) R2(z,w)
		// We have free access on R0
		// dependent access on R1 on the first position
		// and dependent access on R2 on the first position again.
		// We also have a dependency R0(x,y) R1(y,z) -> R3(x,y,z) R2(z,w)
		// and a relation R3 with free access
		// Suppose that we found the plan that performs accesses in the following order
		// R0(x,y) R1(y,z) R3(x,y,z) R2(z,w)
		// postpruning should trash the access on R3 and on R2
		// @Test
		/**
		 * The query is Q(x,y) = R0(x,y) R1(y,z) We also have the dependencies R0(x,y)->
		 * R2(x,y), R1(y,z) -> R3(y,z) Every relation has a free access. Suppose that we
		 * found the plan that performs accesses in the following order R2(x,y) R3(y,z)
		 * R0(x,y) R1(y,z) Postpruning should return R0(x,y) R1(y,z)
		 */
		//
		// -------For post pruning-------
		// The query is Q(x,y) = \exists y R0(x,y) R1(y,z)
		// We also have the dependencies
		// R0(x,y) -> R3(x,y)
		// R1(y,z) -> R4(y,z)
		// R3(x,y), R4(y,z) -> R0(x,w) R1(w,z)
		// Every relation has a free access
		// Suppose that we found the plan that performs accesses in the following order
		// R3(x,y) R4(y,z) R0(x,y) R1(y,z)
		// Postpruning should return
		// R3(x,y) R4(y,z)
		//Assert.fail("Missing test cases");
	}

	@Test
	public void testPruningSamePlanReturning() {
		// -------For post pruning-------
		// The query is Q(x,y) = R0(x,y) R1(y,z)
		// We also have the dependencies
		// R0(x,y) -> R3(x,y)
		// R1(y,z) -> R4(y,z)
		// Relations R3 and R4 have free access
		// Relation R0 requires input on the first position
		// Relation R1 requires input on the second position
		// Suppose that we found the plan that performs accesses in the following order
		// R3(x,y) R4(y,z) R0(x,y) R1(y,z)
		// Postpruning should return the same plan

		DatabaseParameters dbParams = DatabaseParameters.Postgres;
		int numberOfRelations = 2;

		// Create the relations
		Relation[] relations = new Relation[(int) (numberOfRelations + Math.pow(2.0, numberOfRelations)) + 1];
		for (int index = 0; index < numberOfRelations; ++index)
			relations[index] = Relation.create("R" + index, new Attribute[] { this.a, this.b, this.c, this.d },
					new AccessMethod[] { AccessMethod.create(new Integer[0]) });
		relations[numberOfRelations] = Relation.create("Accessible", new Attribute[] { this.a });

		// Create a conjunctive query that joins all relations in the first three
		// positions
		Random random = new Random();
		Atom[] atoms = new Atom[numberOfRelations];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		ArrayList<Variable> freeVariables = new ArrayList<Variable>();
		freeVariables.add(x);
		freeVariables.add(y);
		freeVariables.add(z);
		for (int index = 0; index < numberOfRelations; ++index) {
			Variable v = Variable.create("v" + random.nextInt());
			atoms[index] = Atom.create(relations[index], new Term[] { x, y, z, v });
			freeVariables.add(v);

		}
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] {x,y,z}, atoms);

		// Create all views and update the relations with the newly create views
		Set<Atom> setOfAtoms = new LinkedHashSet<>();
		for (int index = 0; index < numberOfRelations; ++index)
			setOfAtoms.add(atoms[index]);
		Set<Set<Atom>> powerSet = Sets.powerSet(setOfAtoms);
		int powersetIndex = 0;
		int dependencyIndex = 0;
		int viewIndex = numberOfRelations + 1;
		Dependency[] dependencies = new Dependency[(powerSet.size() - 1) * 2];
		for (Set<Atom> set : powerSet) {
			View view = new View("V" + powersetIndex++, new Attribute[] { this.a, this.b, this.c }, new AccessMethod[] { AccessMethod.create(new Integer[0]) });
			relations[viewIndex++] = view;
			int index = 0;
			Atom[] head = new Atom[set.size()];
			Iterator<Atom> iterator = set.iterator();
			while (iterator.hasNext())
				head[index++] = iterator.next();
			if (index != 0) {
				LinearGuarded viewToRelationDependency = LinearGuarded.create(Atom.create(view, new Term[] { x, y, z }), head);
				view.setViewToRelationDependency(viewToRelationDependency);
				dependencies[dependencyIndex++] = view.getViewToRelationDependency();
				dependencies[dependencyIndex++] = view.getRelationToViewDependency();
			}
		}

		// Create schema
		Schema schema = new Schema(relations, dependencies);

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);

		// Create accessible query
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query);
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
			databaseConnection = createConnection(dbParams, accessibleSchema);
		} catch (Exception e) {
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
		when(parameters.getMaxDepth()).thenReturn(numberOfRelations);

		// Create nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);

		// Create linear explorer
		LinearGeneric explorer = null;
		try {
			explorer = new LinearGeneric(new EventBus(), false, query, accessibleQuery, accessibleSchema, chaser, databaseConnection, costEstimator, nodeFactory, 4);
			explorer.explore();
		} catch (Throwable e) {
			// exception expected after further exploration fails.
			e.printStackTrace();
		}
		PostPruningRemoveFollowUps postpruning = new PostPruningRemoveFollowUps(nodeFactory, accessibleSchema, chaser, query);
		try {
			int failed = 0;
			int successful = 0;
			List<SearchNode> blockedNodes = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				List<Integer> bestPath = null;
				SearchNode bestNode = null;
				//explorer.getPlanTree().getVertex(2)
				for (int rootId = explorer.getPlanTree().getRoot().getId(); rootId < 20; rootId++) {
					SearchNode currentNode = explorer.getPlanTree().getVertex(rootId);
					if (!blockedNodes.contains(currentNode)) {
						if (currentNode!= null && currentNode.getStatus() == NodeStatus.SUCCESSFUL) {
							bestPath = currentNode.getBestPathFromRoot();
							bestNode = currentNode;
							break;
						}
					}
				}
				Assert.assertNotNull(bestPath);
				List<Match> matches = explorer.getPlanTree().getPath(bestPath).get(bestPath.size() - 1).matchesQuery(accessibleQuery);
				Map<Variable, Constant> mapping = matches.get(0).getMapping();
				
				for (Variable v:accessibleQuery.getFreeVariables()) {
					mapping.put(v, substitution.get(v));
				}
				Atom[] factsInQueryMatch = uk.ac.ox.cs.pdq.reasoning.chase.Utility
						.applySubstitution(accessibleQuery, matches.get(0).getMapping())
						.getAtoms();
				if (postpruning.pruneSearchNodePath(bestNode, explorer.getPlanTree().getPath(bestPath), factsInQueryMatch)) {
					Assert.assertEquals(bestNode.getBestPlanFromRoot(), postpruning.getPlan());
					successful++;
				} else {
					failed++;
				}
				blockedNodes.add(bestNode);
			}

			Assert.assertEquals(2, successful);
			Assert.assertEquals(8, failed);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

	}
	@After
	public void tearDown() {
		if (connection!=null) {
			try {
				connection.dropDatabase();
				connection.shutdown();
			} catch (DatabaseException e) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}
	
	private DatabaseManager createConnection(DatabaseParameters params, Schema s) {
		try {
			connection = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(params),1);
			connection.initialiseDatabaseForSchema(s);
			return connection;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
