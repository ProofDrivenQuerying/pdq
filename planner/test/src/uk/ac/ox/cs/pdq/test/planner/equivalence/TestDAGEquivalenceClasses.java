// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.planner.equivalence;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.DominanceTypes;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.DominanceFactory;
import uk.ac.ox.cs.pdq.planner.equivalence.dag.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the SynchronizedEquivalenceClasses class.
 * @author gabor
 *
 */
public class TestDAGEquivalenceClasses extends PdqTest {
	private AccessibleChaseInstance state;
	protected Relation T =  Relation.create("T", new Attribute[] { b_s, c_s, d_s }, new AccessMethodDescriptor[] { this.method0, this.method1, this.method2 });
	private Dominance[] dominance;
	
	public TestDAGEquivalenceClasses() {
		try {
			this.state = new AccessibleDatabaseChaseInstance(getFacts(), new InternalDatabaseManager(), false);
			this.dominance = new DominanceFactory(DominanceTypes.OPEN).getInstance();
			
		} catch (SQLException | DatabaseException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void test() {
		DAGEquivalenceClasses se = new DAGEquivalenceClasses();
		// create facts
		Set<Atom> factsT = new HashSet<>();
		factsT.add(Atom.create(T, new Term[] { TypedConstant.create("A" + 1),TypedConstant.create("C" + 1),TypedConstant.create("B" + 1)}));
		Set<Atom> factsS = new HashSet<>();
		factsS.add(Atom.create(S_s, new Term[] { TypedConstant.create("sA" + 1),TypedConstant.create("sC" + 1)}));
		AccessibilityAxiom ruleT = new AccessibilityAxiom(T, this.method0);
		AccessibilityAxiom ruleS = new AccessibilityAxiom(S_s, this.method1); 
		
		// create apply rules
		DAGChaseConfiguration configuration = new ApplyRule(state, ruleT, factsT);
		DAGChaseConfiguration configuration2 = new ApplyRule(state, ruleT, factsT);
		DAGChaseConfiguration configuration3 = new ApplyRule(state, ruleS, factsS);
		configuration.setCost(new DoubleCost(10));

		//Create BinaryConfigurations
		
		BinaryConfiguration testConfig = new BinaryConfiguration(configuration2,configuration);
		testConfig.setCost(new DoubleCost(15));
		BinaryConfiguration shouldBeDominated = new BinaryConfiguration(configuration3,configuration);
		BinaryConfiguration shouldNotBeDominated = new BinaryConfiguration(configuration3,configuration);
		
		// add apply rules and binary configurations
		se.addEntry(configuration);
		se.addEntry(configuration2);
		se.addEntry(testConfig);
		
		Assert.assertEquals(1, (int)se.averageClassSize());
		Assert.assertEquals(2, se.numberOfConfigurations());
		shouldBeDominated.setCost(new DoubleCost(5));
		shouldNotBeDominated.setCost(new DoubleCost(25));
		
		Assert.assertEquals(0, se.dominatedBy(dominance, testConfig).size());
		Assert.assertEquals(0, se.dominatedBy(dominance, shouldNotBeDominated).size());
		Assert.assertEquals(1, se.dominatedBy(dominance, shouldBeDominated).size());
	}
	
	private Collection<Atom> getFacts() {
		Collection<Atom> atoms = new ArrayList<>();
		for (int i = 0 ; i < 10; i++) 
			atoms.add(Atom.create(T, new Term[] { TypedConstant.create("A" + i),TypedConstant.create("C" + i),TypedConstant.create("B" + i)}));
		return atoms;
	}
	
	@Test
	public void testUpdatingRepresentatives() {
		DAGEquivalenceClasses se = new DAGEquivalenceClasses();
		// create facts
		Set<Atom> factsT = new HashSet<>();
		Set<Atom> factsT2 = new HashSet<>();
		factsT.add(Atom.create(T, new Term[] { TypedConstant.create("A" + 1),TypedConstant.create("C" + 1),TypedConstant.create("B" + 1)}));
		factsT2.add(Atom.create(T, new Term[] { TypedConstant.create("A" + 1),TypedConstant.create("C" + 1),TypedConstant.create("B" + 1)}));
		Set<Atom> factsS = new HashSet<>();
		factsS.add(Atom.create(S_s, new Term[] { TypedConstant.create("sA" + 1),TypedConstant.create("sC" + 1)}));
		AccessibilityAxiom ruleT = new AccessibilityAxiom(T, this.method0);
		AccessibilityAxiom ruleS = new AccessibilityAxiom(S_s, this.method1); 
		
		// create apply rules
		DAGChaseConfiguration configuration = new ApplyRule(state, ruleT, factsT);
		DAGChaseConfiguration configuration2 = new ApplyRule(state, ruleS, factsS);
		configuration.setCost(new DoubleCost(10));
		// add apply rules and binary configurations
		se.addEntry(configuration);
		DAGChaseConfiguration representative1 = se.getEquivalenceClasses().iterator().next().getRepresentative();
		
		//remove entries (including the representative
		se.removeEntry(configuration);
		// add new entries
		se.addEntry(configuration2);
		DAGChaseConfiguration representative2 = se.getEquivalenceClasses().iterator().next().getRepresentative();
		// check if the representative got updated
		Assert.assertFalse(representative1.equals(representative2));
	}
}
