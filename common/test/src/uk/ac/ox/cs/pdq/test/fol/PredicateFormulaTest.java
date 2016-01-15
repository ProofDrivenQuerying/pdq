package uk.ac.ox.cs.pdq.test.fol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PredicateFormulaTest {

	private Random random = new Random();
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	@Test
	public void testHashCode() {
		int n = 100, m = 10;
		Set<Predicate> formulae = new LinkedHashSet<>();
		int[] arities = new int[n];
		for (int i = 0; i < n; i++) {
			arities[i] = this.random.nextInt(m);
			Term[] terms = new Term[arities[i]];
			for (int j = 0, l = terms.length; j < l; j++) {
				terms[j] = new Variable("x" + j);
			}
			Signature signature = new Signature("r" + i, terms.length);
			formulae.add(new Predicate(signature, Lists.newArrayList(terms)));
		}
		assertEquals(n, formulae.size());
		for (int i = 0; i < n; i++) {
			Term[] terms = new Term[arities[i]];
			for (int j = 0, l = terms.length; j < l; j++) {
				terms[j] = new Variable("x" + j);
			}
			Signature signature = new Signature("r" + i, terms.length);
			assertTrue(formulae.contains(new Predicate(signature, Lists.newArrayList(terms))));
		}
	}

	@Test public void testPredicateFormulaValid() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		Predicate p = new Predicate( s, t);
		Assert.assertEquals("Predicate must have name signature " + s, s, p.getSignature());
		Assert.assertEquals("Predicate must have name terms " + t, t, p.getTerms());
	}

	@Test public void testPredicateFormulaZeroArity() {
		Signature s = new Signature("s", 0);
		List<Term> t = Lists.newArrayList();
		Predicate p = new Predicate( s, t);
		Assert.assertEquals("Predicate must have name signature " + s, s, p.getSignature());
		Assert.assertEquals("Predicate must have name terms " + t, t, p.getTerms());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPredicateFormulaArityMistmatch1() {
		new Predicate(
				new Signature("s", 0),
				Lists.newArrayList(new Variable("x")));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPredicateFormulaArityMistmatch2() {
		new Predicate(
				new Signature("s", 1),
				Lists.<Term>newArrayList());
	}

	@Test
	public void testPredicateFormulaValidRelation1() {
		Relation r = new Relation("s",
				Lists.newArrayList(new Attribute(String.class, "a"))) {};
		List<Term> t = Lists.<Term>newArrayList(new Variable("x"));
		Predicate p = new Predicate(r, t);
		Assert.assertEquals("Predicate must have name signature " + r, r, p.getSignature());
		Assert.assertEquals("Predicate must have name terms " + t, t, p.getTerms());
	}

	@Test
	public void testPredicateFormulaValidRelation2() {
		Relation r = new Relation("s",
				Lists.newArrayList(new Attribute(String.class, "a"))) {};
		List<Term> t = Lists.<Term>newArrayList(new TypedConstant<String>("x"));
		Predicate p = new Predicate(r, t);
		Assert.assertEquals("Predicate must have name signature " + r, r, p.getSignature());
		Assert.assertEquals("Predicate must have name terms " + t, t, p.getTerms());
	}

	@Test public void testEquality() {
		Relation r = new Relation("s",
				Lists.newArrayList(new Attribute(String.class, "a"))) {};
		List<Term> t = Lists.<Term>newArrayList(new TypedConstant<String>("x"));
		Predicate p1 = new Predicate(r, t);
		Predicate p2 = new Predicate(r, t);
		Assert.assertTrue("PredicateFormula p1 and p2 must be the same", p1.equals(p2));
	}

	@Test public void testEqualityWrongArity() {
		Relation r1 = new Relation("r", Lists.newArrayList(
				new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
		Relation r2 = new Relation("r", Lists.newArrayList(
				new Attribute(String.class, "a"))) {};
		List<Term> t1 = Lists.<Term>newArrayList(new TypedConstant<String>("x"), new TypedConstant<String>("y"));
		List<Term> t2 = Lists.<Term>newArrayList(new TypedConstant<String>("x"));
		Predicate p1 = new Predicate(r1, t1);
		Predicate p2 = new Predicate(r2, t2);
		Assert.assertFalse("PredicateFormula p1 and p2 have different arities", p1.equals(p2));
	}

	@Test public void testEqualityWrongName() {
		Relation r1 = new Relation("r1", Lists.newArrayList(
				new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
		Relation r2 = new Relation("r2", Lists.newArrayList(
				new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
		List<Term> t1 = Lists.<Term>newArrayList(new TypedConstant<String>("x"), new TypedConstant<String>("y"));
		List<Term> t2 = Lists.<Term>newArrayList(new TypedConstant<String>("x"), new TypedConstant<String>("y"));
		Predicate p1 = new Predicate(r1, t1);
		Predicate p2 = new Predicate(r2, t2);
		Assert.assertFalse("PredicateFormula p1 and p2 have different names", p1.equals(p2));
	}

	@Test public void testHashDuplicates() {
		Set<Predicate> set = new LinkedHashSet<>();
		Relation r = new Relation("r", Lists.newArrayList(
				new Attribute(String.class, "a1"), 
				new Attribute(String.class, "a2"))) {};
		TypedConstant<String> c = new TypedConstant<>("c");
		Skolem s = new Skolem("s");
		Variable v = new Variable("v");
		set.add(new Predicate(r, Lists.newArrayList(c, v)));
		set.add(new Predicate(r, Lists.newArrayList(c, c)));
		set.add(new Predicate(r, Lists.newArrayList(s, v)));
		set.add(new Predicate(r, Lists.newArrayList(s, c)));
		set.add(new Predicate(r, Lists.newArrayList(c, v)));
		set.add(new Predicate(r, Lists.newArrayList(c, c)));
		set.add(new Predicate(r, Lists.newArrayList(s, v)));
		set.add(new Predicate(r, Lists.newArrayList(s, c)));
		Assert.assertEquals("PredicateFormula set must have 4 elements", 4, set.size());
	}

	@Test public void testHashNoDuplicates() {
		Set<Predicate> set = new LinkedHashSet<>();
		Relation r = new Relation("r", Lists.newArrayList(
				new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
		TypedConstant<String> c = new TypedConstant<>("c");
		Skolem s = new Skolem("s");
		Variable v = new Variable("v");
		set.add(new Predicate(r, Lists.newArrayList(c, v)));
		set.add(new Predicate(r, Lists.newArrayList(c, c)));
		set.add(new Predicate(r, Lists.newArrayList(s, v)));
		set.add(new Predicate(r, Lists.newArrayList(s, c)));
		set.add(new Predicate(r, Lists.newArrayList(v, v)));
		set.add(new Predicate(r, Lists.newArrayList(c, s)));
		set.add(new Predicate(r, Lists.newArrayList(s, s)));
		set.add(new Predicate(r, Lists.newArrayList(v, s)));
		Assert.assertEquals("PredicateFormula set must have 8 elements", 8, set.size());
	}

	@Test public void testGetTermCount() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Predicate must have " + t.size() +  " terms ",
				t.size(), p.getTermsCount());
	}

	@Test public void testGetTerm() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		Predicate p = new Predicate(s, t);
		for (int i = 0, l = t.size(); i < l; i++) {
			Assert.assertEquals("Predicate's " + i + "th term must be " +
					t.get(i), t.get(i), p.getTerm(i));
		}
	}

	@Test (expected=IndexOutOfBoundsException.class)
	public void testGetTermOutOfRange1() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		new Predicate(s, t).getTerm(-1);
	}

	@Test (expected=IndexOutOfBoundsException.class)
	public void testGetTermOutOfRange2() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		new Predicate(s, t).getTerm(5);
	}

	@Test public void testGetTerms() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Predicate terms must match term list of constructor",
				t, p.getTerms());
	}

	@Test public void testGetSelectedTerms() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Predicate terms subset must match",
				Sets.newLinkedHashSet(),
				p.getTerms(Lists.<Integer>newArrayList()));
		Assert.assertEquals("Predicate terms subset must match",
				Sets.newHashSet(new Variable("x1")),
				p.getTerms(Lists.<Integer>newArrayList(0)));
		Assert.assertEquals("Predicate terms subset must match",
				Sets.newHashSet(new Variable("x3"), new Skolem("x4"), new TypedConstant<>("x5")),
				p.getTerms(Lists.<Integer>newArrayList(2, 3, 4)));
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetSelectedTermsOutofRange1() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		new Predicate(s, t).getTerms(Lists.<Integer>newArrayList(0, 2, -1));
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetSelectedTermsOutofRange2() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5")
				);
		new Predicate(s, t).getTerms(Lists.<Integer>newArrayList(2, 1, 5));
	}

	@Test public void testGetConstants() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5"));
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Predicate terms subset must match",
				Lists.newArrayList(new Skolem("x4"), new TypedConstant<>("x5")),
				p.getConstants(Lists.newArrayList(3, 4)));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetConstantsInvalid() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Variable("x3"),
				new Skolem("x4"), new TypedConstant<>("x5"));
		new Predicate(s, t).getConstants(Lists.newArrayList(0, 1));
	}

	@Test public void testGetVariable() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Skolem("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Predicate variables subset must match",
				Lists.<Term>newArrayList(new Variable("x1"), new Variable("x2"), new Variable("x4")),
				p.getVariables());
	}

	@Test public void testGetAllConstants() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Skolem("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Predicate variables subset must match",
				Sets.newHashSet(new Skolem("x3"), new TypedConstant<>("x5")),
				p.getConstants());
	}

	@Test public void getSchemaConstants() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new TypedConstant<>("x2"), new Skolem("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Predicate variables subset must match",
				Sets.newHashSet(new TypedConstant<>("x2"), new TypedConstant<>("x5")),
				p.getSchemaConstants());
	}

	@Test public void testGetPredicates() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new TypedConstant<>("x2"), new Skolem("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Predicate lists must match",
				Lists.newArrayList(p), p.getPredicates());
	}

	@Test public void testGetAtoms() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new TypedConstant<>("x2"), new Skolem("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Predicate lists must match",
				Lists.newArrayList(p), p.getPredicates());
	}

	@Test public void testGround() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Skolem("x3"),
				new Variable("x4"), new TypedConstant<>("x5")
				);
		List<Term> g = Lists.<Term>newArrayList(
				new TypedConstant<>("c1"), new TypedConstant<>("c2"), new Skolem("x3"),
				new TypedConstant<>("c4"), new TypedConstant<>("x5")
				);
		Map<Variable, Constant> m = new LinkedHashMap<>();
		m.put(new Variable("x1"), new TypedConstant<>("c1"));
		m.put(new Variable("x2"), new TypedConstant<>("c2"));
		m.put(new Variable("x4"), new TypedConstant<>("c4"));
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Grounded negation must comply to mapping ",g, p.ground(m).getTerms());
	}

	@Test public void testGetTermPositions() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Skolem("x3"),
				new Variable("x1"), new TypedConstant<>("x4")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertEquals("Predicate x1 term positions must match", Lists.newArrayList(0, 3), p.getTermPositions(new Variable("x1")));
		Assert.assertEquals("Predicate x2 term positions must match", Lists.newArrayList(1), p.getTermPositions(new Variable("x2")));
		Assert.assertEquals("Predicate x3 term positions must match", Lists.newArrayList(2), p.getTermPositions(new Skolem("x3")));
		Assert.assertEquals("Predicate x4 term positions must match", Lists.newArrayList(4), p.getTermPositions(new TypedConstant<>("x4")));
	}

	@Test public void getTermPositionsNotFound() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Skolem("x3"),
				new Variable("x1"), new TypedConstant<>("x4")
				);
		Assert.assertTrue("Predicate term positions list must be empty",
				new Predicate(s, t).getTermPositions(new Variable("x5"))
				.isEmpty());

	}

	@Test public void testIsFact() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new TypedConstant<>("x1"), new TypedConstant<>("x2"), new TypedConstant<>("x3"),
				new TypedConstant<>("x1"), new TypedConstant<>("x4")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertTrue("Fact terms must contain schema constants only", p.isFact());
	}

	@Test public void testIsNotFact() {
		Signature s = new Signature("s", 5);
		List<Term> t = Lists.<Term>newArrayList(
				new Variable("x1"), new Variable("x2"), new Skolem("x3"),
				new Variable("x1"), new TypedConstant<>("x4")
				);
		Predicate p = new Predicate(s, t);
		Assert.assertFalse("Fact terms must contain schema constants only", p.isFact());
	}
}
