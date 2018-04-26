package uk.ac.ox.cs.pdq.test.fol;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ChaseConstantGenerator;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

// @Author Mark Ridler

public class ChaseConstantGeneratorTest {
	
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	// Creates a dependency from 2 atoms with predicate and terms
	@Test public void testCreation() {
		
		String s = ChaseConstantGenerator.getName();
		Assert.assertTrue(s.equals("c0"));
		
		Predicate pred1 = Predicate.create("pred1", 1);
		Predicate pred2 = Predicate.create("pred1", 2);
		Term[] term1 = {Variable.create("x")};
		Term[] term2 = {Variable.create("y"), Variable.create("z")};
		
		Atom[] atom1 = {Atom.create(pred1, term1)};
		Atom[] atom2 = {Atom.create(pred2, term2)};
		
		Dependency dependency = Dependency.create(atom1, atom2);
		HashMap<Variable, Constant> map = new HashMap<>();
		map.put(Variable.create("x"), TypedConstant.create(0));
		Variable existentialVariable = Variable.create("e");
		
		String s2 = ChaseConstantGenerator.getTriggerWitness(dependency, map, existentialVariable);
		Assert.assertTrue(s2.equals("k1"));
	}
}
