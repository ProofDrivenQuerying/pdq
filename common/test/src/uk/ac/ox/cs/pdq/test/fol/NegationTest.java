package uk.ac.ox.cs.pdq.test.fol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.collect.Lists;

/**
 * @author Julien Leblay
 *
 * @param <T>
 */
public final class NegationTest {

	@Test public void testOf() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("x5")
				);
		Predicate p = new Predicate( s, t);
		Negation<Predicate> n = Negation.of(p);
		Assert.assertEquals("Negation subformulation must match that of construction ", p, n.getChild());
	}

	@Test public void testEquals() {
		Signature s1 = new Signature("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("x5")
				);
		Predicate p1 = new Predicate(s1, t1);
		Signature s2 = new Signature("s", 5);
		List<Term> t2 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("x5")
				);
		Predicate p2 = new Predicate(s2, t2);
		Negation<Predicate> n1 = Negation.of(p1);
		Negation<Predicate> n2 = Negation.of(p2);
		Assert.assertTrue("Negation subformulation must match that of construction ", n1.equals(n2));
	}

	@Test public void testNotEquals() {
		Signature s1 = new Signature("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("x5")
				);
		Predicate p1 = new Predicate(s1, t1);
		Signature s2 = new Signature("s", 5);
		List<Term> t2 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("y")
				);
		Predicate p2 = new Predicate(s2, t2);
		Negation<Predicate> n1 = Negation.of(p1);
		Negation<Predicate> n2 = Negation.of(p2);
		Assert.assertFalse("Negation subformulation must match that of construction ", n1.equals(n2));
	}

	@Test public void testGround() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Skolem("x3"),
				new Variable("x4"), 
				new TypedConstant<>("x5")
				);
		List<Term> g = Lists.<Term>newArrayList(
				new TypedConstant<>("c1"), 
				new TypedConstant<>("c2"), 
				new Skolem("x3"),
				new TypedConstant<>("c4"), 
				new TypedConstant<>("x5")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Predicate p = new Predicate(s, t);
		Negation<Predicate> n = Negation.of(p);
		Assert.assertEquals("Grounded negation must comply to mapping ", g, n.ground(m).getTerms());
	}
}
