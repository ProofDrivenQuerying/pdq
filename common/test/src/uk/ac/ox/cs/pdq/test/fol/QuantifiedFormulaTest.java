package uk.ac.ox.cs.pdq.test.fol;

import java.util.List;

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

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
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
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5")
				);
		Atom p = new Atom( s, t);
		QuantifiedFormula n = new QuantifiedFormula(LogicalSymbols.UNIVERSAL, Lists.newArrayList(new Variable("x1")), p);
		Assert.assertEquals("Universal subformulation must match that of construction ", p, n.getChildren().get(0));
	}
	
	/**
	 * Test equals universal.
	 */
	@Test public void testEqualsUniversal() {
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
		List<Variable> v1 = Lists.newArrayList(new Variable("x1"));
		List<Variable> v2 = Lists.newArrayList(new Variable("x1"));
		QuantifiedFormula n1 = new QuantifiedFormula(LogicalSymbols.UNIVERSAL,v1, p1);
		QuantifiedFormula n2 = new QuantifiedFormula(LogicalSymbols.UNIVERSAL,v2, p2);
		Assert.assertTrue("Universal subformulation must match that of construction ", n1.equals(n2));
	}

	/**
	 * Test not equals universal.
	 */
	@Test public void testNotEqualsUniversal() {
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
		Negation n1 = Negation.of(p1);
		Negation n2 = Negation.of(p2);
		Assert.assertFalse("Universal subformulation must match that of construction ", n1.equals(n2));
	}

	/**
	 * Test existential.
	 */
	@Test public void testExistential() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new Variable("x3"),
				new UntypedConstant("x4"), 
				new TypedConstant<>("x5")
				);
		Atom p = new Atom( s, t);
		QuantifiedFormula n = new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, Lists.newArrayList(new Variable("x1")), p);
		Assert.assertEquals("Universal subformulation must match that of construction ", p, n.getChildren().get(0));
	}
	
	/**
	 * Test equals existential.
	 */
	@Test public void testEqualsExistential() {
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
		List<Variable> v1 = Lists.newArrayList(new Variable("x1"));
		List<Variable> v2 = Lists.newArrayList(new Variable("x1"));
		QuantifiedFormula n1 = new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, v1, p1);
		QuantifiedFormula n2 = new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, v2, p2);
		Assert.assertTrue("Universal subformulation must match that of construction ", n1.equals(n2));
	}

	/**
	 * Test not equals existential.
	 */
	@Test public void testNotEqualsExistential() {
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
		List<Variable> v1 = Lists.newArrayList(new Variable("x1"));
		List<Variable> v2 = Lists.newArrayList(new Variable("x1"));
		QuantifiedFormula n1 = new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, v1, p1);
		QuantifiedFormula n2 = new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, v2, p2);
		Assert.assertFalse("Universal subformulation must match that of construction ", n1.equals(n2));
	}

}
