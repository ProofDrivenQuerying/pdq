// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.fol;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.cs.pdq.util.PdqTest;

// @author Julien Leblay
public class ImplicationTest {

	@Before 
	public void setup() {
		PdqTest.assertsEnabled();
	}

	// Creates implication from 2 atoms with predicate and 5 or 2 terms each, then checks children
	@Test public void testOf() {
		Predicate s1 = Predicate.create("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Predicate s2 = Predicate.create("s", 2);
		Term[] t2 = new Term[]{
				TypedConstant.create("x5"), 
				Variable.create("x1")};
		Atom p1 = Atom.create(s1, t1);
		Atom p2 = Atom.create(s2, t2);
		Implication i = Implication.of(p1, p2);
		Assert.assertEquals("Implication body must match that of construction ", p1, i.getChild(0));
		Assert.assertEquals("Implication head must match that of construction ", p2, i.getChild(1));
	}

	// Creates 2 implications from 2 atoms with predicate and 5 terms each, then checks for equality
	@Test public void testEquals() {
		Predicate s1 = Predicate.create("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p1 = Atom.create(s1, t1);
		Predicate s2 = Predicate.create("s", 5);
		Term[] t2 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p2 = Atom.create(s2, t2);
		Formula i1 = Implication.of(p1, p2);
		Formula i2 = Implication.of(p1, p2);
		Assert.assertTrue("Implications must match be equal ", i1.equals(i2));
	}

	// Creates 2 implications from 3 atoms with predicate and 5 terms each, then checks for equality
	@Test public void testNotEquals() {
		Predicate s1 = Predicate.create("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p1 = Atom.create(s1, t1);
		Predicate s2 = Predicate.create("s", 5);
		Term[] t2 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("y1")
		};
		Atom p2 = Atom.create(s2, t2);
		Predicate s3 = Predicate.create("s", 5);
		Term[] t3 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("y2")
		};
		Atom p3 = Atom.create(s3, t3);
		Formula i1 = Implication.of(p1, p2);
		Formula i2 = Implication.of(p1, p3);
		Assert.assertFalse("Implications must match be equal ", i1.equals(i2));
	}
}
