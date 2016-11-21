package uk.ac.ox.cs.pdq.test.fol;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
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
//		Predicate s1 = new Predicate("r", 5);
//		List<Term> t1 = Lists.<Term>newArrayList(
//				new Variable("x1"), 
//				new Variable("x2"), 
//				new Variable("x3"),
//				new UntypedConstant("x4"), 
//				new TypedConstant<>("x5"));
//		Atom p1 = new Atom(s1, t1);
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
		//ConjunctiveQuery q = new ConjunctiveQuery("r", t1, Conjunction.of(p2, p3));
		ConjunctiveQuery q = new ConjunctiveQuery(Lists.<Variable>newArrayList(new Variable("x1"), new Variable("x3")), 
				(Conjunction) Conjunction.of(p2, p3));
		Assert.assertEquals("ConjunctiveQuery atoms must match that of atom list", Lists.newArrayList(p2, p3), q.getAtoms());
		Assert.assertEquals("ConjunctiveQuery body must match that of construction", Conjunction.of(p2, p3), q.getChildren().get(0));
//		Assert.assertEquals("ConjunctiveQuery head must match that of construction", p1, q.getHead());
		Assert.assertEquals("ConjunctiveQuery bound variables must match that of construction", Lists.newArrayList(), q.getBoundVariables());
		Assert.assertEquals("ConjunctiveQuery free variables must match that of construction", 
				Lists.<Variable>newArrayList(new Variable("x1"), new Variable("x3")), 
				q.getFreeVariables());
		Assert.assertEquals("ConjunctiveQuery predicates must match that of construction", Lists.newArrayList(p2, p3), q.getAtoms());
		Assert.assertEquals("ConjunctiveQuery sub-formulas must match that of construction", Conjunction.of(p2, p3), q.getChildren().get(0));
		Assert.assertEquals("ConjunctiveQuery terms must match that of construction", Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), 
				new Variable("x1"),
				new Variable("x3"), 
				new UntypedConstant("x4")
				), 
				q.getTerms());
	}

	/**
	 * Test equals.
	 */
	@Test public void testEquals() {
//		Predicate s1 = new Predicate("r", 5);
//		List<Term> t1 = Lists.<Term>newArrayList(
//				new Variable("x1"), 
//				new Variable("x2"), 
//				new Variable("x3"),
//				new UntypedConstant("x4"), 
//				new TypedConstant<>("x5"));
//		Atom p1 = new Atom(s1, t1);
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
		ConjunctiveQuery q1 = new ConjunctiveQuery(Lists.<Variable>newArrayList(new Variable("x1"), new Variable("x3")), 
				(Conjunction) Conjunction.of(p2, p3));
		ConjunctiveQuery q2 = new ConjunctiveQuery(Lists.<Variable>newArrayList(new Variable("x1"), new Variable("x3")), 
				(Conjunction) Conjunction.of(p2, p3));
		Assert.assertTrue("Conjunctive queries must be equal ", q1.equals(q2));
	}

}
