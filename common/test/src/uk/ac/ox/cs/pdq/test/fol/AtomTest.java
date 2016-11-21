package uk.ac.ox.cs.pdq.test.fol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * The Class PredicateFormulaTest.
 */
public class AtomTest {

	/** The random. */
	private Random random = new Random();
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	/**
	 * Test hash code.
	 */
	@Test
	public void testHashCode() {
		int n = 100, m = 10;
		Set<Atom> formulae = new LinkedHashSet<>();
		int[] arities = new int[n];
		for (int i = 0; i < n; i++) {
			arities[i] = this.random.nextInt(m);
			Term[] terms = new Term[arities[i]];
			for (int j = 0, l = terms.length; j < l; j++) {
				terms[j] = new Variable("x" + j);
			}
			Predicate predicate = new Predicate("r" + i, terms.length);
			formulae.add(new Atom(predicate, Lists.newArrayList(terms)));
		}
		assertEquals(n, formulae.size());
		for (int i = 0; i < n; i++) {
			Term[] terms = new Term[arities[i]];
			for (int j = 0, l = terms.length; j < l; j++) {
				terms[j] = new Variable("x" + j);
			}
			Predicate predicate = new Predicate("r" + i, terms.length);
			assertTrue(formulae.contains(new Atom(predicate, Lists.newArrayList(terms))));
		}
	}

	/**
	 * Test predicate formula valid.
	 */
	@Test public void testPredicateFormulaValid() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		Atom p = new Atom( s, t);
		Assert.assertEquals("Atom must have name signature " + s, s, p.getPredicate());
		Assert.assertEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	/**
	 * Test predicate formula zero arity.
	 */
	@Test public void testPredicateFormulaZeroArity() {
		Predicate s = new Predicate("s", 0);
		List<Term> t = Lists.newArrayList();
		Atom p = new Atom( s, t);
		Assert.assertEquals("Atom must have name signature " + s, s, p.getPredicate());
		Assert.assertEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	/**
	 * Test predicate formula arity mistmatch1.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testPredicateFormulaArityMistmatch1() {
		new Atom(
				new Predicate("s", 0),
				Lists.newArrayList(new Variable("x")));
	}

	/**
	 * Test predicate formula arity mistmatch2.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testPredicateFormulaArityMistmatch2() {
		new Atom(
				new Predicate("s", 1),
				Lists.<Term>newArrayList());
	}

	/**
	 * Test predicate formula valid relation1.
	 */
	@Test
	public void testPredicateFormulaValidRelation1() {
		Relation r = new Relation("s",
				Lists.newArrayList(new Attribute(String.class, "a"))) {};
		List<Term> t = Lists.<Term>newArrayList(new Variable("x"));
		Atom p = new Atom(r, t);
		Assert.assertEquals("Atom must have name signature " + r, r, p.getPredicate());
		Assert.assertEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	/**
	 * Test predicate formula valid relation2.
	 */
	@Test
	public void testPredicateFormulaValidRelation2() {
		Relation r = new Relation("s",
				Lists.newArrayList(new Attribute(String.class, "a"))) {};
		List<Term> t = Lists.<Term>newArrayList(new TypedConstant<String>("x"));
		Atom p = new Atom(r, t);
		Assert.assertEquals("Atom must have name signature " + r, r, p.getPredicate());
		Assert.assertEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	/**
	 * Test equality.
	 */
	@Test public void testEquality() {
		Relation r = new Relation("s",
				Lists.newArrayList(new Attribute(String.class, "a"))) {};
		List<Term> t = Lists.<Term>newArrayList(new TypedConstant<String>("x"));
		Atom p1 = new Atom(r, t);
		Atom p2 = new Atom(r, t);
		Assert.assertTrue("PredicateFormula p1 and p2 must be the same", p1.equals(p2));
	}

	/**
	 * Test equality wrong arity.
	 */
	@Test public void testEqualityWrongArity() {
		Relation r1 = new Relation("r", Lists.newArrayList(
				new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
		Relation r2 = new Relation("r", Lists.newArrayList(
				new Attribute(String.class, "a"))) {};
		List<Term> t1 = Lists.<Term>newArrayList(new TypedConstant<String>("x"), new TypedConstant<String>("y"));
		List<Term> t2 = Lists.<Term>newArrayList(new TypedConstant<String>("x"));
		Atom p1 = new Atom(r1, t1);
		Atom p2 = new Atom(r2, t2);
		Assert.assertFalse("PredicateFormula p1 and p2 have different arities", p1.equals(p2));
	}

	/**
	 * Test equality wrong name.
	 */
	@Test public void testEqualityWrongName() {
		Relation r1 = new Relation("r1", Lists.newArrayList(
				new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
		Relation r2 = new Relation("r2", Lists.newArrayList(
				new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
		List<Term> t1 = Lists.<Term>newArrayList(new TypedConstant<String>("x"), new TypedConstant<String>("y"));
		List<Term> t2 = Lists.<Term>newArrayList(new TypedConstant<String>("x"), new TypedConstant<String>("y"));
		Atom p1 = new Atom(r1, t1);
		Atom p2 = new Atom(r2, t2);
		Assert.assertFalse("PredicateFormula p1 and p2 have different names", p1.equals(p2));
	}

	/**
	 * Test hash duplicates.
	 */
	@Test public void testHashDuplicates() {
		Set<Atom> set = new LinkedHashSet<>();
		Relation r = new Relation("r", Lists.newArrayList(
				new Attribute(String.class, "a1"), 
				new Attribute(String.class, "a2"))) {};
		TypedConstant<String> c = new TypedConstant<>("c");
		UntypedConstant s = new UntypedConstant("s");
		Variable v = new Variable("v");
		set.add(new Atom(r, Lists.newArrayList(c, v)));
		set.add(new Atom(r, Lists.newArrayList(c, c)));
		set.add(new Atom(r, Lists.newArrayList(s, v)));
		set.add(new Atom(r, Lists.newArrayList(s, c)));
		set.add(new Atom(r, Lists.newArrayList(c, v)));
		set.add(new Atom(r, Lists.newArrayList(c, c)));
		set.add(new Atom(r, Lists.newArrayList(s, v)));
		set.add(new Atom(r, Lists.newArrayList(s, c)));
		Assert.assertEquals("PredicateFormula set must have 4 elements", 4, set.size());
	}

	/**
	 * Test hash no duplicates.
	 */
	@Test public void testHashNoDuplicates() {
		Set<Atom> set = new LinkedHashSet<>();
		Relation r = new Relation("r", Lists.newArrayList(
				new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
		TypedConstant<String> c = new TypedConstant<>("c");
		UntypedConstant s = new UntypedConstant("s");
		Variable v = new Variable("v");
		set.add(new Atom(r, Lists.newArrayList(c, v)));
		set.add(new Atom(r, Lists.newArrayList(c, c)));
		set.add(new Atom(r, Lists.newArrayList(s, v)));
		set.add(new Atom(r, Lists.newArrayList(s, c)));
		set.add(new Atom(r, Lists.newArrayList(v, v)));
		set.add(new Atom(r, Lists.newArrayList(c, s)));
		set.add(new Atom(r, Lists.newArrayList(s, s)));
		set.add(new Atom(r, Lists.newArrayList(v, s)));
		Assert.assertEquals("PredicateFormula set must have 8 elements", 8, set.size());
	}

	/**
	 * Test get term count.
	 */
	@Test public void testGetTermCount() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		Atom p = new Atom(s, t);
		Assert.assertEquals("Atom must have " + t.size() +  " terms ",
				t.size(), p.getPredicate().getArity());
	}

	/**
	 * Test get term.
	 */
	@Test public void testGetTerm() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		Atom p = new Atom(s, t);
		for (int i = 0, l = t.size(); i < l; i++) {
			Assert.assertEquals("Atom's " + i + "th term must be " +
					t.get(i), t.get(i), p.getTerm(i));
		}
	}

	/**
	 * Test get term out of range1.
	 */
	@Test (expected=IndexOutOfBoundsException.class)
	public void testGetTermOutOfRange1() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		new Atom(s, t).getTerm(-1);
	}

	/**
	 * Test get term out of range2.
	 */
	@Test (expected=IndexOutOfBoundsException.class)
	public void testGetTermOutOfRange2() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		new Atom(s, t).getTerm(5);
	}

	/**
	 * Test get terms.
	 */
	@Test public void testGetTerms() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		Atom p = new Atom(s, t);
		Assert.assertEquals("Atom terms must match term list of constructor",
				t, p.getTerms());
	}

	/**
	 * Test get selected terms.
	 */
	@Test public void testGetSelectedTerms() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		Atom p = new Atom(s, t);
		Assert.assertEquals("Atom terms subset must match",
				Sets.newLinkedHashSet(),
				p.getTerms(Lists.<Integer>newArrayList()));
		Assert.assertEquals("Atom terms subset must match",
				Sets.newHashSet(new Variable("x1")),
				p.getTerms(Lists.<Integer>newArrayList(0)));
		Assert.assertEquals("Atom terms subset must match",
				Sets.newHashSet(new Variable("x3"), new UntypedConstant("x4"), new TypedConstant<>("x5")),
				p.getTerms(Lists.<Integer>newArrayList(2, 3, 4)));
	}

	/**
	 * Test get selected terms outof range1.
	 */
	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetSelectedTermsOutofRange1() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		new Atom(s, t).getTerms(Lists.<Integer>newArrayList(0, 2, -1));
	}

	/**
	 * Test get selected terms outof range2.
	 */
	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetSelectedTermsOutofRange2() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5")
				);
		new Atom(s, t).getTerms(Lists.<Integer>newArrayList(2, 1, 5));
	}

	/**
	 * Test get constants.
	 */
	@Test public void testGetConstants() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5"));
		Atom p = new Atom(s, t);
		Assert.assertEquals("Atom terms subset must match",
				Lists.newArrayList(new UntypedConstant("x4"), new TypedConstant<>("x5")),
				Utility.getTypedAndUntypedConstants(p, Lists.newArrayList(3, 4)));
	}

	/**
	 * Test get constants invalid.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testGetConstantsInvalid() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new UntypedConstant("x4"), new TypedConstant<>("x5"));
		Utility.getTypedAndUntypedConstants(new Atom(s, t),(Lists.newArrayList(0, 1)));
	}

	/**
	 * Test get variable.
	 */
	@Test public void testGetVariable() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new UntypedConstant("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Atom p = new Atom(s, t);
		Assert.assertEquals("Atom variables subset must match",
				Lists.<Term>newArrayList(new Variable("x1"), new Variable("x2"), new Variable("x4")),
				p.getVariables());
	}

	/**
	 * Test get all constants.
	 */
	@Test public void testGetAllConstants() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new UntypedConstant("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Atom p = new Atom(s, t);
		Assert.assertEquals("Atom variables subset must match",
				Sets.newHashSet(new UntypedConstant("x3"), new TypedConstant<>("x5")),
				Utility.getTypedAndUntypedConstants(p));
	}

	/**
	 * Gets the schema constants.
	 *
	 * @return the schema constants
	 */
	@Test public void getSchemaConstants() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new TypedConstant<>("x2"), new UntypedConstant("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Atom p = new Atom(s, t);
		Assert.assertEquals("Atom variables subset must match",
				Sets.newHashSet(new TypedConstant<>("x2"), new TypedConstant<>("x5")),
				Utility.getTypedConstants(p));
	}

	/**
	 * Test get predicates.
	 */
	@Test public void testGetPredicates() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new TypedConstant<>("x2"), new UntypedConstant("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Atom p = new Atom(s, t);
		Assert.assertEquals("Atom lists must match",
				Lists.newArrayList(p), p.getAtoms());
	}

	/**
	 * Test get atoms.
	 */
	@Test public void testGetAtoms() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new TypedConstant<>("x2"), new UntypedConstant("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Atom p = new Atom(s, t);
		Assert.assertEquals("Atom lists must match",
				Lists.newArrayList(p), p.getAtoms());
	}

	/**
	 * Test get term positions.
	 */
	@Test public void testGetTermPositions() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new UntypedConstant("x3"),
				new Variable("x1"), new TypedConstant<>("x4")
				);
		Atom p = new Atom(s, t);
		Assert.assertEquals("Atom x1 term positions must match", Lists.newArrayList(0, 3), p.getTermPositions(new Variable("x1")));
		Assert.assertEquals("Atom x2 term positions must match", Lists.newArrayList(1), p.getTermPositions(new Variable("x2")));
		Assert.assertEquals("Atom x3 term positions must match", Lists.newArrayList(2), p.getTermPositions(new UntypedConstant("x3")));
		Assert.assertEquals("Atom x4 term positions must match", Lists.newArrayList(4), p.getTermPositions(new TypedConstant<>("x4")));
	}

	/**
	 * Gets the term positions not found.
	 *
	 * @return the term positions not found
	 */
	@Test public void getTermPositionsNotFound() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new UntypedConstant("x3"),
				new Variable("x1"), new TypedConstant<>("x4")
				);
		Assert.assertTrue("Atom term positions list must be empty",
				new Atom(s, t).getTermPositions(new Variable("x5"))
				.isEmpty());

	}

	/**
	 * Test is fact.
	 */
	@Test public void testIsFact() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new TypedConstant<>("x1"), new TypedConstant<>("x2"), new TypedConstant<>("x3"),
				new TypedConstant<>("x1"), new TypedConstant<>("x4")
				);
		Atom p = new Atom(s, t);
		Assert.assertTrue("Fact terms must contain schema constants only", p.isFact());
	}

	/**
	 * Test is not fact.
	 */
	@Test public void testIsNotFact() {
		Predicate s = new Predicate("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new UntypedConstant("x3"),
				new Variable("x1"), new TypedConstant<>("x4")
				);
		Atom p = new Atom(s, t);
		Assert.assertFalse("Fact terms must contain schema constants only", p.isFact());
	}
}
