package uk.ac.ox.cs.pdq.test.fol;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class NegationTest.
 *
 * @author Julien Leblay
 */
public final class NegationTest {

	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	/**
	 * Test of.
	 */
	@Test public void testOf() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p = Atom.create( s, t);
		Negation n = Negation.of(p);
		Assert.assertEquals("Negation subformulation must match that of construction ", p, n.getChild(0));
	}

	/**
	 * Test equals.
	 */
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
		Formula n1 = Negation.of(p1);
		Formula n2 = Negation.of(p2);
		Assert.assertTrue("Negation subformulation must match that of construction ", n1.equals(n2));
	}

	/**
	 * Test not equals.
	 */
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
				TypedConstant.create("y")
		};
		Atom p2 = Atom.create(s2, t2);
		Formula n1 = Negation.of(p1);
		Formula n2 = Negation.of(p2);
		Assert.assertFalse("Negation subformulation must match that of construction ", n1.equals(n2));
	}
}
