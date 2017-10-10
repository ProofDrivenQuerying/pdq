package uk.ac.ox.cs.pdq.test.fol;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * The Class QuantifiedFormulaTest.
 */
public class QuantifiedFormulaTest {
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	/**
	 * Test universal.
	 */
	@Test public void testUniversal() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p = Atom.create( s, t);
		QuantifiedFormula n = QuantifiedFormula.create(LogicalSymbols.UNIVERSAL, new Variable[]{Variable.create("x1")}, p);
		Assert.assertEquals("Universal subformulation must match that of construction ", p, n.getChild(0));
	}
	
	/**
	 * Test equals universal.
	 */
	@Test public void testEqualsUniversal() {
		Predicate s1 = Predicate.create("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p1 = Atom.create(s1, t1);
		Predicate s2 = Predicate.create("s", 5);
		Term[] t2 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p2 = Atom.create(s2, t2);
		Variable[] v1 = new Variable[]{Variable.create("x1")};
		Variable[] v2 = new Variable[]{Variable.create("x1")};
		QuantifiedFormula n1 = QuantifiedFormula.create(LogicalSymbols.UNIVERSAL,v1, p1);
		QuantifiedFormula n2 = QuantifiedFormula.create(LogicalSymbols.UNIVERSAL,v2, p2);
		Assert.assertTrue("Universal subformulation must match that of construction ", n1.equals(n2));
	}

	/**
	 * Test not equals universal.
	 */
	@Test public void testNotEqualsUniversal() {
		Predicate s1 = Predicate.create("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p1 = Atom.create(s1, t1);
		Predicate s2 = Predicate.create("s", 5);
		Term[] t2 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("y")
		};
		Atom p2 = Atom.create(s2, t2);
		Negation n1 = Negation.of(p1);
		Negation n2 = Negation.of(p2);
		Assert.assertFalse("Universal subformulation must match that of construction ", n1.equals(n2));
	}

	/**
	 * Test existential.
	 */
	@Test public void testExistential() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p = Atom.create( s, t);
		QuantifiedFormula n = QuantifiedFormula.create(LogicalSymbols.EXISTENTIAL, new Variable[]{Variable.create("x1")}, p);
		Assert.assertEquals("Universal subformulation must match that of construction ", p, n.getChild(0));
	}
	
	/**
	 * Test equals existential.
	 */
	@Test public void testEqualsExistential() {
		Predicate s1 = Predicate.create("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p1 = Atom.create(s1, t1);
		Predicate s2 = Predicate.create("s", 5);
		Term[] t2 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p2 = Atom.create(s2, t2);
		Variable[] v1 = new Variable[]{Variable.create("x1")};
		Variable[] v2 = new Variable[]{Variable.create("x1")};
		QuantifiedFormula n1 = QuantifiedFormula.create(LogicalSymbols.EXISTENTIAL, v1, p1);
		QuantifiedFormula n2 = QuantifiedFormula.create(LogicalSymbols.EXISTENTIAL, v2, p2);
		Assert.assertTrue("Universal subformulation must match that of construction ", n1.equals(n2));
	}

	/**
	 * Test not equals existential.
	 */
	@Test public void testNotEqualsExistential() {
		Predicate s1 = Predicate.create("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("x5")
		};
		Atom p1 = Atom.create(s1, t1);
		Predicate s2 = Predicate.create("s", 5);
		Term[] t2 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				Variable.create("x3"),
				UntypedConstant.create("x4"), 
				TypedConstant.create("y")
		};
		Atom p2 = Atom.create(s2, t2);
		Variable[] v1 = new Variable[]{Variable.create("x1")};
		Variable[] v2 = new Variable[]{Variable.create("x1")};
		QuantifiedFormula n1 = QuantifiedFormula.create(LogicalSymbols.EXISTENTIAL, v1, p1);
		QuantifiedFormula n2 = QuantifiedFormula.create(LogicalSymbols.EXISTENTIAL, v2, p2);
		Assert.assertFalse("Universal subformulation must match that of construction ", n1.equals(n2));
	}

}
