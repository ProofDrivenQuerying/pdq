package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
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
		Term[] t1 = new Term[]{
				Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")
		};
		Predicate s2 = new Predicate("s", 2);
		Term[] t2 = new Term[]{TypedConstant.create("x5"), Variable.create("x1")};
		Atom p1 = Atom.create(s1, t1);
		Atom p2 = Atom.create(s2, t2);
		List<Term> g = Lists.<Term>newArrayList(
				TypedConstant.create("c1"), TypedConstant.create("c2"),
				UntypedConstant.create("x3"), TypedConstant.create("c4"),
				TypedConstant.create("x5")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(Variable.create("x1"), TypedConstant.create("c1"));
		m.put(Variable.create("x2"), TypedConstant.create("c2"));
		m.put(Variable.create("x4"), TypedConstant.create("c4"));
		Formula i = Conjunction.of(p1, p2);
		Assert.assertEquals("Grounded conjunction must comply to mapping ", g, Utility.applySubstitution(i, m).getTerms());
	}

	/**
	 * Test ground.
	 */
	@Test 
	public void testGround2() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")
		};
		Term[] g = new Term[]{
				TypedConstant.create("c1"), TypedConstant.create("c2"), UntypedConstant.create("x3"),
				TypedConstant.create("c4"), TypedConstant.create("x5")
		};
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(Variable.create("x1"), TypedConstant.create("c1"));
		m.put(Variable.create("x2"), TypedConstant.create("c2"));
		m.put(Variable.create("x4"), TypedConstant.create("c4"));
		Atom p = Atom.create(s, t);
		Assert.assertArrayEquals("Grounded negation must comply to mapping ",g, Utility.applySubstitution(p, m).getTerms());
	}

	/**
	 * Test ground.
	 */
	@Test 
	public void testGround3() {
		Predicate s1 = new Predicate("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				UntypedConstant.create("x3"),
				Variable.create("x4"), 
				TypedConstant.create("x5")
		};
		Predicate s2 = new Predicate("s", 2);
		Term[] t2 =new Term[]{
				TypedConstant.create("x5"), 
				Variable.create("x1")};
		Atom p1 = Atom.create(s1, t1);
		Atom p2 = Atom.create(s2, t2);
		Term[] g = new Term[]{
				TypedConstant.create("c1"), 
				TypedConstant.create("c2"),
				UntypedConstant.create("x3"), 
				TypedConstant.create("c4"),
				TypedConstant.create("x5")
		};
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(Variable.create("x1"), TypedConstant.create("c1"));
		m.put(Variable.create("x2"), TypedConstant.create("c2"));
		m.put(Variable.create("x4"), TypedConstant.create("c4"));
		Formula i = Disjunction.of(p1, p2);
		Assert.assertArrayEquals("Grounded disjunction must comply to mapping ", g, Utility.applySubstitution(i, m).getTerms());
	}

	/**
	 * Test ground.
	 */
	@Test 
	public void testGround4() {
		Predicate s1 = new Predicate("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				UntypedConstant.create("x3"),
				Variable.create("x4"), 
				TypedConstant.create("x5")
		};
		Predicate s2 = new Predicate("s", 2);
		Term[] t2 = new Term[]{
				TypedConstant.create("x5"), 
				Variable.create("x1")};
		Atom p1 = Atom.create(s1, t1);
		Atom p2 = Atom.create(s2, t2);
		Term[] g = new Term[]{
				TypedConstant.create("c1"), 
				TypedConstant.create("c2"),
				UntypedConstant.create("x3"), 
				TypedConstant.create("c4"),
				TypedConstant.create("x5")
		};
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(Variable.create("x1"), TypedConstant.create("c1"));
		m.put(Variable.create("x2"), TypedConstant.create("c2"));
		m.put(Variable.create("x4"), TypedConstant.create("c4"));
		Formula i = Implication.of(p1, p2);
		Assert.assertArrayEquals("Grounded negation must comply to mapping ", g, Utility.applySubstitution(i, m).getTerms());
	}


	//	/**
	//	 * Test ground universal.
	//	 */
	//	@Test public void testGroundUniversal() {
	//		Predicate s = new Predicate("s", 5);
	//		List<Term> t = Lists.<Term>newArrayList(
	//				Variable.create("x1"), 
	//				Variable.create("x2"), 
	//				UntypedConstant.create("x3"),
	//				Variable.create("x4"), 
	//				TypedConstant.create("x5")
	//				);
	//		List<Term> g = Lists.<Term>newArrayList(
	//				Variable.create("x1"), 
	//				TypedConstant.create("c2"), 
	//				UntypedConstant.create("x3"),
	//				TypedConstant.create("c4"), 
	//				TypedConstant.create("x5")
	//				);
	//		Map<Variable, Constant> m = new LinkedHashMap<>();
	//		m.put(Variable.create("x2"), TypedConstant.create("c2"));
	//		m.put(Variable.create("x4"), TypedConstant.create("c4"));
	//		Atom p = Atom.create(s, t);
	//		List<Variable> v = Lists.newArrayList(Variable.create("x1"));
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
	//				Variable.create("x1"), 
	//				Variable.create("x2"), 
	//				UntypedConstant.create("x3"),
	//				Variable.create("x4"), 
	//				TypedConstant.create("x5")
	//				);
	//		List<Term> g = Lists.<Term>newArrayList(
	//				Variable.create("x1"), 
	//				TypedConstant.create("c2"), 
	//				UntypedConstant.create("x3"),
	//				TypedConstant.create("c4"), 
	//				TypedConstant.create("x5")
	//				);
	//		Map<Variable, Constant> m = new LinkedHashMap<>();
	//		m.put(Variable.create("x1"), TypedConstant.create("c1"));
	//		m.put(Variable.create("x2"), TypedConstant.create("c2"));
	//		m.put(Variable.create("x4"), TypedConstant.create("c4"));
	//		Atom p = Atom.create(s, t);
	//		List<Variable> v = Lists.newArrayList(Variable.create("x1"));
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
	//				Variable.create("x1"), 
	//				Variable.create("x2"), 
	//				UntypedConstant.create("x3"),
	//				Variable.create("x4"), 
	//				TypedConstant.create("x5")
	//				);
	//		List<Term> g = Lists.<Term>newArrayList(
	//				TypedConstant.create("c1"), 
	//				TypedConstant.create("c2"), 
	//				UntypedConstant.create("x3"),
	//				TypedConstant.create("c4"), 
	//				TypedConstant.create("x5")
	//				);
	//		Map<Variable, Constant> m = new LinkedHashMap<>();
	//		m.put(Variable.create("x1"), TypedConstant.create("c1"));
	//		m.put(Variable.create("x2"), TypedConstant.create("c2"));
	//		m.put(Variable.create("x4"), TypedConstant.create("c4"));
	//		Atom p = Atom.create(s, t);
	//		List<Variable> v = Lists.newArrayList(Variable.create("x1"));
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
	//				Variable.create("x1"), 
	//				Variable.create("x2"), 
	//				UntypedConstant.create("x3"),
	//				Variable.create("x4"), 
	//				TypedConstant.create("x5")
	//				);
	//		List<Term> g = Lists.<Term>newArrayList(
	//				Variable.create("x1"), 
	//				TypedConstant.create("c2"), 
	//				UntypedConstant.create("x3"),
	//				TypedConstant.create("c4"), 
	//				TypedConstant.create("x5")
	//				);
	//		Map<Variable, Constant> m = new LinkedHashMap<>();
	//		m.put(Variable.create("x2"), TypedConstant.create("c2"));
	//		m.put(Variable.create("x4"), TypedConstant.create("c4"));
	//		Atom p = Atom.create(s, t);
	//		List<Variable> v = Lists.newArrayList(Variable.create("x1"));
	//		QuantifiedFormula n = new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, v, p);
	//		Assert.assertEquals("Grounded universal must comply to mapping ", g, n.ground(m).getTerms());
	//	}

	/**
	 * Test ground.
	 */
	@Test public void testGround() {
		Predicate s1 = new Predicate("s", 5);
		Term[] t1 = new Term[]{
				Variable.create("x1"), 
				Variable.create("x2"), 
				UntypedConstant.create("x3"),
				Variable.create("x4"), 
				TypedConstant.create("x5")};
		Predicate s2 = new Predicate("s", 2);
		Term[] t2 = new Term[]{TypedConstant.create("x5"), Variable.create("x1")};
		Atom p1 = Atom.create(s1, t1);
		Atom p2 = Atom.create(s2, t2);
		List<Term> g = Lists.<Term>newArrayList(
				TypedConstant.create("c1"), 
				TypedConstant.create("c2"),
				UntypedConstant.create("x3"), 
				TypedConstant.create("c4"),
				TypedConstant.create("x5"));
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(Variable.create("x1"), TypedConstant.create("c1"));
		m.put(Variable.create("x2"), TypedConstant.create("c2"));
		m.put(Variable.create("x4"), TypedConstant.create("c4"));
		Conjunction i = (Conjunction) Conjunction.of(p1, p2);
		Assert.assertEquals("Grounded conjunction must comply to mapping ", g, Utility.applySubstitution(i, m).getTerms());
	}
}
