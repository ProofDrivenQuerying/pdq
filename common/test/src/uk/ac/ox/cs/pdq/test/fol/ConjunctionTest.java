package uk.ac.ox.cs.pdq.test.fol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

/**
 * @author Julien Leblay
 *
 * @param <T>
 */
public class ConjunctionTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	@Test public void testOf() {
		Signature s1 = new Signature("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		Signature s2 = new Signature("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), new Variable("x1"));
		Predicate p1 = new Predicate(s1, t1);
		Predicate p2 = new Predicate(s2, t2);
		Conjunction<Predicate> i = Conjunction.of(p1, p2);
		Assert.assertEquals("Conjunction atoms must match that of construction",
				Lists.newArrayList(p1, p2), i.getPredicates());
	}

	@Test public void testEquals() {
		Signature s1 = new Signature("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		Predicate p1 = new Predicate(s1, t1);
		Signature s2 = new Signature("s", 5);
		List<Term> t2 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		Predicate p2 = new Predicate(s2, t2);
		Conjunction<Predicate> i1 = Conjunction.of(p1, p2);
		Conjunction i2 = Conjunction.builder().and(p1, p2).build();
		Assert.assertTrue("Conjunctions must match be equal ", i1.equals(i2));
	}

	@Test public void testNotEquals() {
		Signature s1 = new Signature("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		Predicate p1 = new Predicate(s1, t1);
		Signature s2 = new Signature("s", 5);
		List<Term> t2 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("y1")
				);
		Predicate p2 = new Predicate(s2, t2);
		Signature s3 = new Signature("s", 5);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("y2")
				);
		Predicate p3 = new Predicate(s3, t3);
		Conjunction<Predicate> i1 = Conjunction.of(p1, p2);
		Conjunction<Predicate> i2 = Conjunction.of(p1, p3);
		Assert.assertFalse("Conjunctions must match be equal ", i1.equals(i2));
	}

	@Test public void testGround() {
		Signature s1 = new Signature("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Skolem("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Signature s2 = new Signature("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), new Variable("x1"));
		Predicate p1 = new Predicate(s1, t1);
		Predicate p2 = new Predicate(s2, t2);
		List<Term> g = Lists.<Term>newArrayList(
				new TypedConstant<>("c1"), new TypedConstant<>("c2"),
				new Skolem("x3"), new TypedConstant<>("c4"),
				new TypedConstant<>("x5"), new TypedConstant<>("x5"),
				new TypedConstant<>("c1")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Conjunction<Predicate> i = Conjunction.of(p1, p2);
		Assert.assertEquals("Grounded conjunction must comply to mapping ", g, i.ground(m).getTerms());
	}
}
