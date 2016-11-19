package uk.ac.ox.cs.pdq.test.fol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * A conjunctive query.
 *
 * @author Julien Leblay
 * @author Julien Leblay
 */
public class ConjunctiveQueryTest {
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	/**
	 * Test conjunctive query construction1.
	 */
	@Test public void testConjunctiveQueryConstruction1() {
		Predicate s1 = new Predicate("r", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5"));
		Atom p1 = new Atom(s1, t1);
		Predicate s2 = new Predicate("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Atom p2 = new Atom(s2, t2);
		Predicate s3 = new Predicate("t", 3);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x3"), 
				new UntypedConstant("x4"),
				new TypedConstant<>("x5"));
		Atom p3 = new Atom(s3, t3);
		ConjunctiveQuery q = new ConjunctiveQuery("r", t1, Conjunction.of(p2, p3));
		Assert.assertEquals("ConjunctiveQuery atoms must match that of atom list", Lists.newArrayList(p1, p2, p3), q.getAtoms());
		Assert.assertEquals("ConjunctiveQuery body must match that of construction", Conjunction.of(p2, p3), q.getBody());
		Assert.assertEquals("ConjunctiveQuery head must match that of construction", p1, q.getHead());
		Assert.assertEquals("ConjunctiveQuery bound variables must match that of construction", Lists.newArrayList(), q.getBound());
		Assert.assertEquals("ConjunctiveQuery free variables must match that of construction", t1, q.getFree());
		Assert.assertEquals("ConjunctiveQuery predicates must match that of construction", Lists.newArrayList(p1, p2, p3), q.getAtoms());
		Assert.assertEquals("ConjunctiveQuery sub-formulas must match that of construction", Lists.newArrayList(p2, p3), q.getChildren());
		Assert.assertEquals("ConjunctiveQuery terms must match that of construction", t1, q.getTerms());
	}

	/**
	 * Test conjunctive query construction2.
	 */
	@Test public void testConjunctiveQueryConstruction2() {
		Predicate s1 = new Predicate("r", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5"));
		Atom p1 = new Atom(s1, t1);
		Predicate s2 = new Predicate("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Atom p2 = new Atom(s2, t2);
		Predicate s3 = new Predicate("t", 3);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5"));
		Atom p3 = new Atom(s3, t3);
		ConjunctiveQuery q = new ConjunctiveQuery(p1, Conjunction.of(p2, p3));
		Assert.assertEquals("ConjunctiveQuery atoms must match that of atom list", Lists.newArrayList(p1, p2, p3), q.getAtoms());
		Assert.assertEquals("ConjunctiveQuery body must match that of construction", Conjunction.of(p2, p3), q.getBody());
		Assert.assertEquals("ConjunctiveQuery head must match that of construction", p1, q.getHead());
		Assert.assertEquals("ConjunctiveQuery bound variables must match that of construction", Lists.newArrayList(), q.getBound());
		Assert.assertEquals("ConjunctiveQuery free variables must match that of construction", t1, q.getFree());
		Assert.assertEquals("ConjunctiveQuery predicates must match that of construction", Lists.newArrayList(p1, p2, p3), q.getAtoms());
		Assert.assertEquals("ConjunctiveQuery sub-formulas must match that of construction", Lists.newArrayList(p2, p3), q.getChildren());
		Assert.assertEquals("ConjunctiveQuery terms must match that of construction", t1, q.getTerms());
	}

	/**
	 * Test equals.
	 */
	@Test public void testEquals() {
		Predicate s1 = new Predicate("r", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5"));
		Atom p1 = new Atom(s1, t1);
		Predicate s2 = new Predicate("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Atom p2 = new Atom(s2, t2);
		Predicate s3 = new Predicate("t", 3);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x3"), 
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5"));
		Atom p3 = new Atom(s3, t3);
		ConjunctiveQuery q1 = new ConjunctiveQuery(p1, Conjunction.of(p2, p3));
		ConjunctiveQuery q2 = new ConjunctiveQuery("r", t1, Conjunction.of(p2, p3));
		Assert.assertTrue("Conjunctive queries must be equal ", q1.equals(q2));
	}

	/**
	 * Test not equals.
	 */
	@Test public void testNotEquals() {
		Predicate s1 = new Predicate("r", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5"));
		Atom p1 = new Atom(s1, t1);
		Predicate s2 = new Predicate("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"));
		Atom p2 = new Atom(s2, t2);
		Predicate s3 = new Predicate("t", 3);
		List<Term> t3 = Lists.<Term>newArrayList(
				new Variable("x3"), 
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5"));
		Atom p3 = new Atom(s3, t3);
		ConjunctiveQuery q1 = new ConjunctiveQuery(p1, Conjunction.of(p2, p3));
		ConjunctiveQuery q2 = new ConjunctiveQuery("q", t1, Conjunction.of(p2, p3));
		Assert.assertFalse("Conjunctive queries must not be equal ", q1.equals(q2));
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
				new TypedConstant<>("x5"));
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
				new TypedConstant<>("c1"));
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Conjunction i = Conjunction.of(p1, p2);
		Assert.assertEquals("Grounded conjunction must comply to mapping ", g, i.ground(m).getTerms());
	}

	/**
	 * Test get canonical.
	 */
	@Test public void testGetCanonical() {
	}

	/**
	 * Test get important subqueries.
	 */
	@Test public void testGetImportantSubqueries() {
	}

	/**
	 * Test canonical mapping.
	 */
	@Test public void testCanonicalMapping() {
	}

	/**
	 * Test is boolean.
	 */
	@Test public void testIsBoolean() {
	}

	/**
	 * Test get constants.
	 */
	@Test public void testGetConstants() {
	}

	/**
	 * Test get free to canonical.
	 */
	@Test public void testGetFreeToCanonical() {
	}

	/**
	 * Test hash code.
	 */
	@Test public void testHashCode() {
	}
}
