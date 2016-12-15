package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.Utility;

import com.google.common.collect.Lists;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class TestUtility {

	/**
	 * Test ground.
	 */
	@Test 
	public void testGround1() {
		Predicate s1 = new Predicate("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new UntypedConstant("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Predicate s2 = new Predicate("s", 2);
		List<Term> t2 = Lists.<Term>newArrayList(
				new TypedConstant<>("x5"), new Variable("x1"));
		Atom p1 = new Atom(s1, t1);
		Atom p2 = new Atom(s2, t2);
		List<Term> g = Lists.<Term>newArrayList(
				new TypedConstant<>("c1"), new TypedConstant<>("c2"),
				new UntypedConstant("x3"), new TypedConstant<>("c4"),
				new TypedConstant<>("x5")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Formula i = Conjunction.of(p1, p2);
		Assert.assertEquals("Grounded conjunction must comply to mapping ", g, Utility.applySubstitution(i, m).getTerms());
	}
	
	/**
	 * Test ground.
	 */
	@Test 
	public void testGround2() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new UntypedConstant("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		List<Term> g = Lists.<Term>newArrayList(
				new TypedConstant<>("c1"), new TypedConstant<>("c2"), new UntypedConstant("x3"),
				new TypedConstant<>("c4"), new TypedConstant<>("x5")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Atom p = new Atom(s, t);
		Assert.assertEquals("Grounded negation must comply to mapping ",g, Utility.applySubstitution(p, m).getTerms());
	}
	
	/**
	 * Test ground.
	 */
	@Test 
	public void testGround3() {
		Predicate s1 = new Predicate("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new UntypedConstant("x3"),
				new Variable("x4"), 
				new TypedConstant<>("x5")
				);
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
				new TypedConstant<>("x5")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Formula i = Disjunction.of(p1, p2);
		Assert.assertEquals("Grounded disjunction must comply to mapping ", g, Utility.applySubstitution(i, m).getTerms());
	}
	
	/**
	 * Test ground.
	 */
	@Test 
	public void testGround4() {
		Predicate s1 = new Predicate("s", 5);
		List<Term> t1 = Lists.<Term>newArrayList(
				new Variable("x1"), 
				new Variable("x2"), 
				new UntypedConstant("x3"),
				new Variable("x4"), 
				new TypedConstant<>("x5")
				);
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
				new TypedConstant<>("x5")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Formula i = Implication.of(p1, p2);
		Assert.assertEquals("Grounded negation must comply to mapping ", g, Utility.applySubstitution(i, m).getTerms());
	}
	

//	/**
//	 * Test ground universal.
//	 */
//	@Test public void testGroundUniversal() {
//		Predicate s = new Predicate("s", 5);
//		List<Term> t = Lists.<Term>newArrayList(
//				new Variable("x1"), 
//				new Variable("x2"), 
//				new UntypedConstant("x3"),
//				new Variable("x4"), 
//				new TypedConstant<>("x5")
//				);
//		List<Term> g = Lists.<Term>newArrayList(
//				new Variable("x1"), 
//				new TypedConstant<>("c2"), 
//				new UntypedConstant("x3"),
//				new TypedConstant<>("c4"), 
//				new TypedConstant<>("x5")
//				);
//		Map<Variable, Constant> m = new LinkedHashMap<>();
//		m.put(new Variable("x2"), new TypedConstant<>("c2"));
//		m.put(new Variable("x4"), new TypedConstant<>("c4"));
//		Atom p = new Atom(s, t);
//		List<Variable> v = Lists.newArrayList(new Variable("x1"));
//		QuantifiedFormula n = new QuantifiedFormula(LogicalSymbols.UNIVERSAL, v, p);
//		Assert.assertEquals("Grounded universal must comply to mapping ", g, n.ground(m).getTerms());
//	}
//	
//
//	/**
//	 * Test ground universal invalid.
//	 */
//	@Test(expected=IllegalArgumentException.class)
//	public void testGroundUniversalInvalid() {
//		Predicate s = new Predicate("s", 5);
//		List<Term> t = Lists.<Term>newArrayList(
//				new Variable("x1"), 
//				new Variable("x2"), 
//				new UntypedConstant("x3"),
//				new Variable("x4"), 
//				new TypedConstant<>("x5")
//				);
//		List<Term> g = Lists.<Term>newArrayList(
//				new Variable("x1"), 
//				new TypedConstant<>("c2"), 
//				new UntypedConstant("x3"),
//				new TypedConstant<>("c4"), 
//				new TypedConstant<>("x5")
//				);
//		Map<Variable, Constant> m = new LinkedHashMap<>();
//		m.put(new Variable("x1"), new TypedConstant<>("c1"));
//		m.put(new Variable("x2"), new TypedConstant<>("c2"));
//		m.put(new Variable("x4"), new TypedConstant<>("c4"));
//		Atom p = new Atom(s, t);
//		List<Variable> v = Lists.newArrayList(new Variable("x1"));
//		QuantifiedFormula n = new QuantifiedFormula(LogicalSymbols.UNIVERSAL, v, p);
//		Assert.assertEquals("Grounded universal must comply to mapping ", g, n.ground(m).getTerms());
//	}
//	
//	/**
//	 * Test ground existential invalid.
//	 */
//	@Test(expected=IllegalArgumentException.class)
//	public void testGroundExistentialInvalid() {
//		Predicate s = new Predicate("s", 5);
//		List<Term> t = Lists.<Term>newArrayList(
//				new Variable("x1"), 
//				new Variable("x2"), 
//				new UntypedConstant("x3"),
//				new Variable("x4"), 
//				new TypedConstant<>("x5")
//				);
//		List<Term> g = Lists.<Term>newArrayList(
//				new TypedConstant<>("c1"), 
//				new TypedConstant<>("c2"), 
//				new UntypedConstant("x3"),
//				new TypedConstant<>("c4"), 
//				new TypedConstant<>("x5")
//				);
//		Map<Variable, Constant> m = new LinkedHashMap<>();
//		m.put(new Variable("x1"), new TypedConstant<>("c1"));
//		m.put(new Variable("x2"), new TypedConstant<>("c2"));
//		m.put(new Variable("x4"), new TypedConstant<>("c4"));
//		Atom p = new Atom(s, t);
//		List<Variable> v = Lists.newArrayList(new Variable("x1"));
//		QuantifiedFormula n = new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, v, p);
//		Assert.assertEquals("Grounded universal must comply to mapping ", g, n.ground(m).getTerms());
//	}
//	
//
//	/**
//	 * Test ground existential.
//	 */
//	@Test public void testGroundExistential() {
//		Predicate s = new Predicate("s", 5);
//		List<Term> t = Lists.<Term>newArrayList(
//				new Variable("x1"), 
//				new Variable("x2"), 
//				new UntypedConstant("x3"),
//				new Variable("x4"), 
//				new TypedConstant<>("x5")
//				);
//		List<Term> g = Lists.<Term>newArrayList(
//				new Variable("x1"), 
//				new TypedConstant<>("c2"), 
//				new UntypedConstant("x3"),
//				new TypedConstant<>("c4"), 
//				new TypedConstant<>("x5")
//				);
//		Map<Variable, Constant> m = new LinkedHashMap<>();
//		m.put(new Variable("x2"), new TypedConstant<>("c2"));
//		m.put(new Variable("x4"), new TypedConstant<>("c4"));
//		Atom p = new Atom(s, t);
//		List<Variable> v = Lists.newArrayList(new Variable("x1"));
//		QuantifiedFormula n = new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, v, p);
//		Assert.assertEquals("Grounded universal must comply to mapping ", g, n.ground(m).getTerms());
//	}
	
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
				new TypedConstant<>("x5"));
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Conjunction i = (Conjunction) Conjunction.of(p1, p2);
		Assert.assertEquals("Grounded conjunction must comply to mapping ", g, Utility.applySubstitution(i, m).getTerms());
	}
}
