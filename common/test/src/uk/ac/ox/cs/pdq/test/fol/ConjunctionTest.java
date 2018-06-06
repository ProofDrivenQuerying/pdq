package uk.ac.ox.cs.pdq.test.fol;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

// @Author Julien Leblay

public class ConjunctionTest {
	
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	// Creates a conjunction from 2 atoms with 1 predicate and 5 or 2 terms each, then compares atoms
	@Test public void testOf() {
		Predicate s1 = Predicate.create("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Predicate s2 = Predicate.create("s", 2);
		Term[] t2 = new Term[]{TypedConstant.create("x5"), Variable.create("x1")};
		Atom p1 = Atom.create(s1, t1);
		Atom p2 = Atom.create(s2, t2);
		Formula i = Conjunction.create(p1, p2);
		Assert.assertArrayEquals("Conjunction atoms must match that of construction",
				new Atom[]{p1, p2}, i.getAtoms());
	}

	// Creates 2 conjunctions from 2 atoms with 1 predicates and 5 terms each, then checks for equality
	@Test public void testEquals() {
		Predicate s1 = Predicate.create("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p1 = Atom.create(s1, t1);
		Predicate s2 = Predicate.create("s", 5);
		Term[] t2 = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p2 = Atom.create(s2, t2);
		Formula i1 = Conjunction.create(p1, p2);
		Formula i2 = Conjunction.create(p1, p2);
		Assert.assertTrue("Conjunctions must match be equal ", i1.equals(i2));
	}

	// Creates 2 conjunctions from 3 atoms with 1 predicate and 5 terms each, then checks for equality
	@Test public void testNotEquals() {
		Predicate s1 = Predicate.create("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p1 = Atom.create(s1, t1);
		Predicate s2 = Predicate.create("s", 5);
		Term[] t2 = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("y1")
		};
		Atom p2 = Atom.create(s2, t2);
		Predicate s3 = Predicate.create("s", 5);
		Term[] t3 = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("y2")
		};
		Atom p3 = Atom.create(s3, t3);
		Formula i1 = Conjunction.create(p1, p2);
		Formula i2 = Conjunction.create(p1, p3);
		Assert.assertFalse("Conjunctions must match be equal ", i1.equals(i2));
	}
}
