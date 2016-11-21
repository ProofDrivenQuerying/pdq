package uk.ac.ox.cs.pdq.test.fol;

import java.util.List;

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

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class ConjunctionTest.
 *
 * @author Julien Leblay
 */
public class ConjunctionTest {
	
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
		Predicate s1 = new Predicate("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		Predicate s2 = new Predicate("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), new Variable("x1"));
		Atom p1 = new Atom(s1, t1);
		Atom p2 = new Atom(s2, t2);
		Formula i = Conjunction.of(p1, p2);
		Assert.assertEquals("Conjunction atoms must match that of construction",
				Lists.newArrayList(p1, p2), i.getAtoms());
	}

	/**
	 * Test equals.
	 */
	@Test public void testEquals() {
		Predicate s1 = new Predicate("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		Atom p1 = new Atom(s1, t1);
		Predicate s2 = new Predicate("s", 5);
		List<Term> t2 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		Atom p2 = new Atom(s2, t2);
		Formula i1 = Conjunction.of(p1, p2);
		Formula i2 = Conjunction.of(p1, p2);
		Assert.assertTrue("Conjunctions must match be equal ", i1.equals(i2));
	}

	/**
	 * Test not equals.
	 */
	@Test public void testNotEquals() {
		Predicate s1 = new Predicate("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		Atom p1 = new Atom(s1, t1);
		Predicate s2 = new Predicate("s", 5);
		List<Term> t2 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("y1")
				);
		Atom p2 = new Atom(s2, t2);
		Predicate s3 = new Predicate("s", 5);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("y2")
				);
		Atom p3 = new Atom(s3, t3);
		Formula i1 = Conjunction.of(p1, p2);
		Formula i2 = Conjunction.of(p1, p3);
		Assert.assertFalse("Conjunctions must match be equal ", i1.equals(i2));
	}
}
