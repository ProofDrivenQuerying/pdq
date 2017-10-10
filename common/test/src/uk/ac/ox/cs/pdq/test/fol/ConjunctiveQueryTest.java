package uk.ac.ox.cs.pdq.test.fol;

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
		Predicate s2 = Predicate.create("s", 2);
		Term[] t2 = new Term[]{TypedConstant.create("x5"), Variable.create("x1")};
		Atom p2 = Atom.create(s2, t2);
		Predicate s3 = Predicate.create("t", 3);
		Term[] t3 = new Term[]{
				Variable.create("x3"), 
				UntypedConstant.create("x4"),
				TypedConstant.create("x5")};
		Atom p3 = Atom.create(s3, t3);
		ConjunctiveQuery q = ConjunctiveQuery.create(
				new Variable[]{Variable.create("x1"), Variable.create("x3")}, 
				(Conjunction) Conjunction.of(p2, p3));
		Assert.assertArrayEquals("ConjunctiveQuery atoms must match that of atom list", new Atom[]{p2, p3}, q.getAtoms());
		Assert.assertEquals("ConjunctiveQuery body must match that of construction", Conjunction.of(p2, p3), q.getChild(0));
		Assert.assertArrayEquals("ConjunctiveQuery bound variables must match that of construction", new Variable[]{}, q.getBoundVariables());
		Assert.assertArrayEquals("ConjunctiveQuery free variables must match that of construction", 
				new Variable[]{Variable.create("x1"), Variable.create("x3")}, 
				q.getFreeVariables());
		Assert.assertArrayEquals("ConjunctiveQuery predicates must match that of construction", new Atom[]{p2, p3}, q.getAtoms());
		Assert.assertEquals("ConjunctiveQuery sub-formulas must match that of construction", Conjunction.of(p2, p3), q.getChild(0));
		Assert.assertArrayEquals("ConjunctiveQuery terms must match that of construction", 
				new Term[]{TypedConstant.create("x5"), Variable.create("x1"), Variable.create("x3"), UntypedConstant.create("x4")}, 
				q.getTerms());
	}

	/**
	 * Test equals.
	 */
	@Test public void testEquals() {
		Predicate s2 = Predicate.create("s", 2);
		Term[] t2 = new Term[]{TypedConstant.create("x5"), Variable.create("x1")};
		Atom p2 = Atom.create(s2, t2);
		
		Predicate s3 = Predicate.create("t", 3);
		Term[] t3 = new Term[]{Variable.create("x3"), UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Atom p3 = Atom.create(s3, t3);
		
		Variable[] v1 = new Variable[]{Variable.create("x1"), Variable.create("x3")};
		Variable[] v2 = new Variable[]{Variable.create("x1"), Variable.create("x3")};
		Conjunction c1 = (Conjunction) Conjunction.of(p2, p3);
		Conjunction c2 = (Conjunction) Conjunction.of(p2, p3);
		ConjunctiveQuery q1 = ConjunctiveQuery.create(v1,c1);
		ConjunctiveQuery q2 = ConjunctiveQuery.create(v2,c2);
		Assert.assertTrue("Conjunctive queries must be equal ", q1.equals(q2));
	}

}
