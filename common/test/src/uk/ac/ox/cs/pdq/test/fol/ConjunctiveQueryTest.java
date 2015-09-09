package uk.ac.ox.cs.pdq.test.fol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.collect.Lists;

/**
 * A conjunctive query
 * @author Julien Leblay
 * @author Julien Leblay
 *
 */
public class ConjunctiveQueryTest {

	@Test public void testConjunctiveQueryConstruction1() {
		Signature s1 = new Signature("r", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("x5"));
		Predicate p1 = new Predicate(s1, t1);
		Signature s2 = new Signature("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Predicate p2 = new Predicate(s2, t2);
		Signature s3 = new Signature("t", 3);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x3"), 
				new Skolem("x4"),
				new TypedConstant<>("x5"));
		Predicate p3 = new Predicate(s3, t3);
		ConjunctiveQuery q = new ConjunctiveQuery("r", t1, Conjunction.of(p2, p3));
		Assert.assertEquals("ConjunctiveQuery atoms must match that of atom list", Lists.newArrayList(p1, p2, p3), q.getPredicates());
		Assert.assertEquals("ConjunctiveQuery body must match that of construction", Conjunction.of(p2, p3), q.getBody());
		Assert.assertEquals("ConjunctiveQuery head must match that of construction", p1, q.getHead());
		Assert.assertEquals("ConjunctiveQuery bound variables must match that of construction", Lists.newArrayList(), q.getBound());
		Assert.assertEquals("ConjunctiveQuery free variables must match that of construction", t1, q.getFree());
		Assert.assertEquals("ConjunctiveQuery predicates must match that of construction", Lists.newArrayList(p1, p2, p3), q.getPredicates());
		Assert.assertEquals("ConjunctiveQuery sub-formulas must match that of construction", Lists.newArrayList(p2, p3), q.getChildren());
		Assert.assertEquals("ConjunctiveQuery terms must match that of construction", t1, q.getTerms());
	}

	@Test public void testConjunctiveQueryConstruction2() {
		Signature s1 = new Signature("r", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("x5"));
		Predicate p1 = new Predicate(s1, t1);
		Signature s2 = new Signature("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Predicate p2 = new Predicate(s2, t2);
		Signature s3 = new Signature("t", 3);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("x5"));
		Predicate p3 = new Predicate(s3, t3);
		ConjunctiveQuery q = new ConjunctiveQuery(p1, Conjunction.of(p2, p3));
		Assert.assertEquals("ConjunctiveQuery atoms must match that of atom list", Lists.newArrayList(p1, p2, p3), q.getPredicates());
		Assert.assertEquals("ConjunctiveQuery body must match that of construction", Conjunction.of(p2, p3), q.getBody());
		Assert.assertEquals("ConjunctiveQuery head must match that of construction", p1, q.getHead());
		Assert.assertEquals("ConjunctiveQuery bound variables must match that of construction", Lists.newArrayList(), q.getBound());
		Assert.assertEquals("ConjunctiveQuery free variables must match that of construction", t1, q.getFree());
		Assert.assertEquals("ConjunctiveQuery predicates must match that of construction", Lists.newArrayList(p1, p2, p3), q.getPredicates());
		Assert.assertEquals("ConjunctiveQuery sub-formulas must match that of construction", Lists.newArrayList(p2, p3), q.getChildren());
		Assert.assertEquals("ConjunctiveQuery terms must match that of construction", t1, q.getTerms());
	}

	@Test public void testEquals() {
		Signature s1 = new Signature("r", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("x5"));
		Predicate p1 = new Predicate(s1, t1);
		Signature s2 = new Signature("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Predicate p2 = new Predicate(s2, t2);
		Signature s3 = new Signature("t", 3);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x3"), 
				new Skolem("x4"), 
				new TypedConstant<>("x5"));
		Predicate p3 = new Predicate(s3, t3);
		ConjunctiveQuery q1 = new ConjunctiveQuery(p1, Conjunction.of(p2, p3));
		ConjunctiveQuery q2 = new ConjunctiveQuery("r", t1, Conjunction.of(p2, p3));
		Assert.assertTrue("Conjunctive queries must be equal ", q1.equals(q2));
	}

	@Test public void testNotEquals() {
		Signature s1 = new Signature("r", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new Skolem("x4"), 
				new TypedConstant<>("x5"));
		Predicate p1 = new Predicate(s1, t1);
		Signature s2 = new Signature("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Predicate p2 = new Predicate(s2, t2);
		Signature s3 = new Signature("t", 3);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x3"), 
				new Skolem("x4"), 
				new TypedConstant<>("x5"));
		Predicate p3 = new Predicate(s3, t3);
		ConjunctiveQuery q1 = new ConjunctiveQuery(p1, Conjunction.of(p2, p3));
		ConjunctiveQuery q2 = new ConjunctiveQuery("q", t1, Conjunction.of(p2, p3));
		Assert.assertFalse("Conjunctive queries must not be equal ", q1.equals(q2));
	}

	@Test public void testGround() {
		Signature s1 = new Signature("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Skolem("x3"),
				new Variable("x4"), 
				new TypedConstant<>("x5"));
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
				new TypedConstant<>("c1"));
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Conjunction<Predicate> i = Conjunction.of(p1, p2);
		Assert.assertEquals("Grounded conjunction must comply to mapping ", g, i.ground(m).getTerms());
	}

	@Test public void testGetCanonical() {
	}

	@Test public void testGetImportantSubqueries() {
	}

	@Test public void testCanonicalMapping() {
	}

	@Test public void testIsBoolean() {
	}

	@Test public void testGetConstants() {
	}

	@Test public void testGetFreeToCanonical() {
	}

	@Test public void testHashCode() {
	}
}
