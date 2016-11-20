package uk.ac.ox.cs.pdq.test.fol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

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
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5")
				);
		Atom p = new Atom( s, t);
		Negation n = Negation.of(p);
		Assert.assertEquals("Negation subformulation must match that of construction ", p, n.getChildren().get(0));
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
		Formula n1 = Negation.of(p1);
		Formula n2 = Negation.of(p2);
		Assert.assertTrue("Negation subformulation must match that of construction ", n1.equals(n2));
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
				new TypedConstant<>("y")
				);
		Atom p2 = new Atom(s2, t2);
		Formula n1 = Negation.of(p1);
		Formula n2 = Negation.of(p2);
		Assert.assertFalse("Negation subformulation must match that of construction ", n1.equals(n2));
	}

	/**
	 * Test ground.
	 */
	@Test public void testGround() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new UntypedConstant("x3"),
				new Variable("x4"), 
				new TypedConstant<>("x5")
				);
		List<Term> g = Lists.<Term>newArrayList(
				new TypedConstant<>("c1"), 
				new TypedConstant<>("c2"), 
				new UntypedConstant("x3"),
				new TypedConstant<>("c4"), 
				new TypedConstant<>("x5")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Atom p = new Atom(s, t);
		Formula n = Negation.of(p);
		Assert.assertEquals("Grounded negation must comply to mapping ", g, n.ground(m).getTerms());
	}
}
