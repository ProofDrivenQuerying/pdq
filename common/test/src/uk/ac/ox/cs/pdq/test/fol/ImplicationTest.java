package uk.ac.ox.cs.pdq.test.fol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.collect.Lists;

/**
 * An implication
 * @author Julien Leblay
 *
 * @param <T>
 */
public class ImplicationTest {

	@Test public void testOf() {
		Signature s1 = new Signature("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("x5")
				);
		Signature s2 = new Signature("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Predicate p1 = new Predicate(s1, t1);
		Predicate p2 = new Predicate(s2, t2);
		Implication<Predicate, Predicate> i = Implication.of(p1, p2);
		Assert.assertEquals("Implication body must match that of construction ", p1, i.getBody());
		Assert.assertEquals("Implication head must match that of construction ", p2, i.getHead());
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
		Implication<Predicate, Predicate> i1 = Implication.of(p1, p2);
		Implication<Predicate, Predicate> i2 = Implication.of(Pair.of(p1, p2));
		Assert.assertTrue("Implications must match be equal ", i1.equals(i2));
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
				new TypedConstant<>("y1")
				);
		Predicate p2 = new Predicate(s2, t2);
		Signature s3 = new Signature("s", 5);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("y2")
				);
		Predicate p3 = new Predicate(s3, t3);
		Implication<Predicate, Predicate> i1 = Implication.of(p1, p2);
		Implication<Predicate, Predicate> i2 = Implication.of(p1, p3);
		Assert.assertFalse("Implications must match be equal ", i1.equals(i2));
	}

	@Test public void testGround() {
		Signature s1 = new Signature("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Skolem("x3"),
				new Variable("x4"), 
				new TypedConstant<>("x5")
				);
		Signature s2 = new Signature("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Predicate p1 = new Predicate(s1, t1);
		Predicate p2 = new Predicate(s2, t2);
		List<Term> g = Lists.<Term>newArrayList(
				new TypedConstant<>("c1"), 
				new TypedConstant<>("c2"),
				new Skolem("x3"), 
				new TypedConstant<>("c4"),
				new TypedConstant<>("x5"), 
				new TypedConstant<>("x5"),
				new TypedConstant<>("c1")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Implication<Predicate, Predicate> i = Implication.of(p1, p2);
		Assert.assertEquals("Grounded negation must comply to mapping ", g, i.ground(m).getTerms());
	}
}
