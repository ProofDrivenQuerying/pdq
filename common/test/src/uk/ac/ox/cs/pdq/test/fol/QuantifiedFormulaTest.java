package uk.ac.ox.cs.pdq.test.fol;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

public class QuantifiedFormulaTest {
	
	@Before 
	public void setup() {
		PdqTest.assertsEnabled();
	}
	
	// Creates a quantified formula from atom with predicate and 5 terms, then checks children
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
	
	// Creates 2 quantified formulae from 2 atoms with predicate and 5 terms, then checks for equality
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

	// Creates a quantified formula from atom with predicate and 5 terms, then checks children
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
	
	// Creates 2 quantified formulae from 2 atoms with predicate and 5 terms each, then checks for equality
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

	// Creates 2 quantified formulae from 2 atoms with predicate and 5 terms each, then checks for inequality
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
