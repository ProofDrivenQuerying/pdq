package uk.ac.ox.cs.pdq.test.fol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * An implication.
 *
 * @author Julien Leblay
 */
public class ImplicationTest {
	
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
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5")
				);
		Predicate s2 = new Predicate("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Atom p1 = new Atom(s1, t1);
		Atom p2 = new Atom(s2, t2);
		Formula i = Implication.of(p1, p2);
		Assert.assertEquals("Implication body must match that of construction ", p1, i.getBody());
		Assert.assertEquals("Implication head must match that of construction ", p2, i.getHead());
	}

	/**
	 * Test equals.
	 */
	@Test public void testEquals() {
		Predicate s1 = new Predicate("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5")
				);
		Atom p1 = new Atom(s1, t1);
		Predicate s2 = new Predicate("s", 5);
		List<Term> t2 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5")
				);
		Atom p2 = new Atom(s2, t2);
		Formula i1 = Implication.of(p1, p2);
		Formula i2 = Implication.of(Pair.of(p1, p2));
		Assert.assertTrue("Implications must match be equal ", i1.equals(i2));
	}

	/**
	 * Test not equals.
	 */
	@Test public void testNotEquals() {
		Predicate s1 = new Predicate("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5")
				);
		Atom p1 = new Atom(s1, t1);
		Predicate s2 = new Predicate("s", 5);
		List<Term> t2 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("y1")
				);
		Atom p2 = new Atom(s2, t2);
		Predicate s3 = new Predicate("s", 5);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("y2")
				);
		Atom p3 = new Atom(s3, t3);
		Formula i1 = Implication.of(p1, p2);
		Formula i2 = Implication.of(p1, p3);
		Assert.assertFalse("Implications must match be equal ", i1.equals(i2));
	}

	/**
	 * Test ground.
	 */
	@Test public void testGround() {
		Predicate s1 = new Predicate("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new UntypedConstant("x3"),
				new Variable("x4"), 
				new TypedConstant<>("x5")
				);
		Predicate s2 = new Predicate("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Atom p1 = new Atom(s1, t1);
		Atom p2 = new Atom(s2, t2);
		List<Term> g = Lists.<Term>newArrayList(
				new TypedConstant<>("c1"), 
				new TypedConstant<>("c2"),
				new UntypedConstant("x3"), 
				new TypedConstant<>("c4"),
				new TypedConstant<>("x5"), 
				new TypedConstant<>("x5"),
				new TypedConstant<>("c1")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Formula i = Implication.of(p1, p2);
		Assert.assertEquals("Grounded negation must comply to mapping ", g, i.ground(m).getTerms());
	}
}
