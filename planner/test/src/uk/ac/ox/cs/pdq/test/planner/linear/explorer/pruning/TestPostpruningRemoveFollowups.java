package uk.ac.ox.cs.pdq.test.planner.linear.explorer.pruning;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.estimators.CountNumberOfAccessedRelationsCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.linear.explorer.pruning.PostPruningRemoveFollowUps;
import uk.ac.ox.cs.pdq.planner.reasoning.MatchFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.Utility;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Tests the postpruning class.
 * 
 * @author Efthymia Tsamoura
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPostpruningRemoveFollowups {

	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
	protected Attribute InstanceID = Attribute.create(Integer.class, "InstanceID");

	@Before 
	public void setup() {
		uk.ac.ox.cs.pdq.util.Utility.assertsEnabled();
        MockitoAnnotations.initMocks(this);
        GlobalCounterProvider.resetCounters();
        uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
        uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
        uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
	}

	//The query is Q(x,y) = R0(x,y) R1(y,z) R2(z,w)
	//We have free access on R0
	//dependent access on R1 on the first position
	//and dependent access on R2 on the first position again.
	//We also have a dependency R0(x,y) R1(y,z) -> R3(x,y,z)
	//and a relation R3 with free access
	//Suppose that we found the plan that performs accesses in the following order R0(x,y) R1(y,z) R3(x,y,z) R2(z,w)
	//postpruning should trash the access on R3
	@Test 
	public void test1Postpruning() {
		//Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[]{this.a, this.b, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{})});
		relations[1] = Relation.create("R1", new Attribute[]{this.a, this.b, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{0})});
		relations[2] = Relation.create("R2", new Attribute[]{this.a, this.b, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{1})});
		relations[3] = Relation.create("R3", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{})});
		relations[4] = Relation.create("Accessible", new Attribute[]{this.a,this.InstanceID});
		//Create query
		Atom[] atoms = new Atom[3];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		Variable w = Variable.create("z");
		atoms[0] = Atom.create(relations[0], new Term[]{x,y});
		atoms[1] = Atom.create(relations[1], new Term[]{y,z});
		atoms[2] = Atom.create(relations[2], new Term[]{z,w});
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[]{x,y,z}, (Conjunction) Conjunction.of(atoms));
		Atom dummyAtom = Atom.create(relations[3], new Term[]{x,y,z});
				
		Dependency dependency = TGD.create(new Atom[]{Atom.create(relations[0], new Term[]{x,y}), Atom.create(relations[1], new Term[]{y,z})}, 
				new Atom[]{Atom.create(relations[3], new Term[]{x,y,z})});
		
		//Create schema
		Schema schema = new Schema(relations, new Dependency[]{dependency});

		//Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		
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
				
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);
		
		//Create the nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);
		
		//Create the query match for the dummy plan
		Map<Variable, Constant> substitution = query.getSubstitutionToCanonicalConstants();
		Atom[] factsToExpose = new Atom[4];
		factsToExpose[0] = (Atom) Utility.applySubstitution(atoms[0], substitution);
		factsToExpose[1] = (Atom) Utility.applySubstitution(atoms[1], substitution);
		factsToExpose[2] = (Atom) Utility.applySubstitution(dummyAtom, substitution);
		factsToExpose[3] = (Atom) Utility.applySubstitution(atoms[2], substitution);
		
		Atom[] factsInQueryMatch = new Atom[3];
		factsInQueryMatch[0] = (Atom) Utility.applySubstitution(accessibleQuery.getAtom(0), substitution);
		factsInQueryMatch[1] = (Atom) Utility.applySubstitution(accessibleQuery.getAtom(1), substitution);
		factsInQueryMatch[2] = (Atom) Utility.applySubstitution(accessibleQuery.getAtom(2), substitution);
		
		//Choose the axioms that will expose the facts in the dummy plan
		//This is a very simple function assuming that we have one accessibility axiom per relation
		//Something more sofisticted should be implemented if there exist more axioms per relation 
		AccessibilityAxiom[] axiomsThatExposeFacts = new AccessibilityAxiom[4];
		AccessibilityAxiom[] accessibilityAxioms = accessibleSchema.getAccessibilityAxioms();
		for(int atomIndex = 0; atomIndex < factsToExpose.length; ++atomIndex) {
			Atom atom = factsToExpose[atomIndex];
			for(int accessibilityAxiomIndex = 0; accessibilityAxiomIndex < accessibilityAxioms.length; ++accessibilityAxiomIndex) {
				if(accessibilityAxioms[accessibilityAxiomIndex].getBaseRelation().equals(atom.getPredicate())) {
					axiomsThatExposeFacts[atomIndex] = accessibilityAxioms[accessibilityAxiomIndex];
					break;
				}
			}
		}
		List<SearchNode> searchNodePath = this.createPathOfNodesExposingInputFactsInGivenOrder(factsToExpose, axiomsThatExposeFacts, accessibleQuery, 
				accessibleSchema, chaser, databaseConnection, nodeFactory);
		
		PostPruningRemoveFollowUps postpruning = new PostPruningRemoveFollowUps(nodeFactory, accessibleSchema, chaser, query);
		try {
			searchNodePath.get(4).setStatus(NodeStatus.SUCCESSFUL);
			try {
				postpruning.pruneSearchNodePath(searchNodePath.get(0), searchNodePath, factsInQueryMatch);
			}catch(Throwable t) {
				Assert.fail();
			}
			RelationalTerm newPlan = postpruning.getPlan();
			Assert.assertFalse(findRelationInPlan("R3",newPlan));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	private boolean findRelationInPlan(String relationNameToFind, RelationalTerm plan) {
		if (plan instanceof AccessTerm) {
			if (relationNameToFind.equalsIgnoreCase(((AccessTerm)plan).getRelation().getName()))
				return true;
		} else {
			for(RelationalTerm child:plan.getChildren()) if (findRelationInPlan(relationNameToFind, child)) return true;
		}
		return false;
	}

	//The query is Q(x,y) = R0(x,y) R1(y,z) R2(z,w)
	//We have free access on R0
	//dependent access on R1 on the first position
	//and dependent access on R2 on the first position again.
	//We also have a dependency R0(x,y) R1(y,z) -> R3(x,y,z) R2(z,w)
	//and a relation R3 with free access
	//Suppose that we found the plan that performs accesses in the following order R0(x,y) R1(y,z) R3(x,y,z) R2(z,w)
	//postpruning should trash the access on R3 and on R2
	@Test
	public void test2Postpruning() {
		//Create the relations
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[]{this.a, this.b, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{})});
		relations[1] = Relation.create("R1", new Attribute[]{this.a, this.b, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{0})});
		relations[2] = Relation.create("R2", new Attribute[]{this.a, this.b, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{1})});
		relations[3] = Relation.create("R3", new Attribute[]{this.a, this.b, this.c, this.InstanceID}, 
				new AccessMethod[]{AccessMethod.create(new Integer[]{})});
		relations[4] = Relation.create("Accessible", new Attribute[]{this.a,this.InstanceID});
		//Create query
		Atom[] atoms = new Atom[3];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		Variable w = Variable.create("z");
		atoms[0] = Atom.create(relations[0], new Term[]{x,y});
		atoms[1] = Atom.create(relations[1], new Term[]{y,z});
		atoms[2] = Atom.create(relations[2], new Term[]{z,w});
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[]{x,y,z}, (Conjunction) Conjunction.of(atoms));
		Atom dummyAtom = Atom.create(relations[3], new Term[]{x,y,z});
				
		Dependency dependency = TGD.create(new Atom[]{Atom.create(relations[0], new Term[]{x,y}), Atom.create(relations[1], new Term[]{y,z})}, 
				new Atom[]{Atom.create(relations[3], new Term[]{x,y,z}), Atom.create(relations[2], new Term[]{z,w})});
		
		//Create schema
		Schema schema = new Schema(relations, new Dependency[]{dependency});

		//Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		
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
				
		//Mock the planner parameters
		PlannerParameters parameters = Mockito.mock(PlannerParameters.class);
		when(parameters.getSeed()).thenReturn(1);
		when(parameters.getMaxDepth()).thenReturn(3);
		
		//Create the nodeFactory
		NodeFactory nodeFactory = new NodeFactory(parameters, costEstimator);
		
		//Create the query match for the dummy plan
		Map<Variable, Constant> substitution = query.getSubstitutionToCanonicalConstants();
		Atom[] factsToExpose = new Atom[4];
		factsToExpose[0] = (Atom) Utility.applySubstitution(atoms[0], substitution);
		factsToExpose[1] = (Atom) Utility.applySubstitution(atoms[1], substitution);
		factsToExpose[2] = (Atom) Utility.applySubstitution(dummyAtom, substitution);
		factsToExpose[3] = (Atom) Utility.applySubstitution(atoms[2], substitution);
		
		Atom[] factsInQueryMatch = new Atom[3];
		factsInQueryMatch[0] = (Atom) Utility.applySubstitution(accessibleQuery.getAtom(0), substitution);
		factsInQueryMatch[1] = (Atom) Utility.applySubstitution(accessibleQuery.getAtom(1), substitution);
		factsInQueryMatch[2] = (Atom) Utility.applySubstitution(accessibleQuery.getAtom(2), substitution);
		
		//Choose the axioms that will expose the facts in the dummy plan
		//This is a very simple function assuming that we have one accessibility axiom per relation
		//Something more sofisticted should be implemented if there exist more axioms per relation 
		AccessibilityAxiom[] axiomsThatExposeFacts = new AccessibilityAxiom[4];
		AccessibilityAxiom[] accessibilityAxioms = accessibleSchema.getAccessibilityAxioms();
		for(int atomIndex = 0; atomIndex < factsToExpose.length; ++atomIndex) {
			Atom atom = factsToExpose[atomIndex];
			for(int accessibilityAxiomIndex = 0; accessibilityAxiomIndex < accessibilityAxioms.length; ++accessibilityAxiomIndex) {
				if(accessibilityAxioms[accessibilityAxiomIndex].getBaseRelation().equals(atom.getPredicate())) {
					axiomsThatExposeFacts[atomIndex] = accessibilityAxioms[accessibilityAxiomIndex];
					break;
				}
			}
		}
		List<SearchNode> searchNodePath = this.createPathOfNodesExposingInputFactsInGivenOrder(factsToExpose, axiomsThatExposeFacts, accessibleQuery, 
				accessibleSchema, chaser, databaseConnection, nodeFactory);
		
		PostPruningRemoveFollowUps postpruning = new PostPruningRemoveFollowUps(nodeFactory, accessibleSchema, chaser, query);
		try {
			searchNodePath.get(4).setStatus(NodeStatus.SUCCESSFUL);
			try {
				postpruning.pruneSearchNodePath(searchNodePath.get(0), searchNodePath, factsInQueryMatch);
			}catch(Throwable t) {
				Assert.fail();
			}
			RelationalTerm newPlan = postpruning.getPlan();
			Assert.assertFalse(findRelationInPlan("R3",newPlan));
			Assert.assertFalse(findRelationInPlan("R2",newPlan));
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	private List<SearchNode> createPathOfNodesExposingInputFactsInGivenOrder(
			Atom[] factsToExpose, AccessibilityAxiom[] axiomsThatExposeFacts, 
			ConjunctiveQuery query, AccessibleSchema accessibleSchema, RestrictedChaser chaser, 
			DatabaseConnection connection, NodeFactory nodeFactory) {
		try {
			List<SearchNode> searchNodes = new ArrayList<>();
			//Create the root node
			AccessibleChaseInstance state = (uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance) 
					new AccessibleDatabaseChaseInstance(query, accessibleSchema, connection, true);
			chaser.reasonUntilTermination(state, accessibleSchema.getOriginalDependencies());
			SearchNode root = nodeFactory.getInstance(state);
			searchNodes.add(root);
			SearchNode parentNode = root;
			for(int factIndex = 0; factIndex < factsToExpose.length; ++factIndex) {
				Match match = MatchFactory.createMatchForAccessibilityAxiom(axiomsThatExposeFacts[factIndex], factsToExpose[factIndex]);
				Candidate candidate = new Candidate(axiomsThatExposeFacts[factIndex], factsToExpose[factIndex], match);
				Set<Candidate> candidates = new LinkedHashSet<>();
				candidates.add(candidate);
				SearchNode freshNode = nodeFactory.getInstance(parentNode, candidates);
				freshNode.close(chaser, accessibleSchema.getInferredAccessibilityAxioms());
				parentNode = freshNode; 
				searchNodes.add(freshNode);
			}
			return searchNodes;
		} catch (SQLException | PlannerException e) {
			e.printStackTrace();
			Assert.fail();
			
		} catch (LimitReachedException e) {
			e.printStackTrace();
			Assert.fail();
		}
		return null;
	}
	
}
