package uk.ac.ox.cs.pdq.test.fol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

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
				terms[j] = Variable.create("x" + j);
			}
			Predicate predicate = new Predicate("r" + i, terms.length);
			formulae.add(Atom.create(predicate, terms));
		}
		assertEquals(n, formulae.size());
		for (int i = 0; i < n; i++) {
			Term[] terms = new Term[arities[i]];
			for (int j = 0, l = terms.length; j < l; j++) {
				terms[j] = Variable.create("x" + j);
			}
			Predicate predicate = new Predicate("r" + i, terms.length);
			assertTrue(formulae.contains(Atom.create(predicate, terms)));
		}
	}

	/**
	 * Test predicate formula valid.
	 */
	@Test public void testPredicateFormulaValid() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p = Atom.create( s, t);
		Assert.assertEquals("Atom must have name signature " + s, s, p.getPredicate());
		Assert.assertArrayEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	/**
	 * Test predicate formula zero arity.
	 */
	@Test public void testPredicateFormulaZeroArity() {
		Predicate s = new Predicate("s", 0);
		Term[] t = new Term[0];
		Atom p = Atom.create( s, t);
		Assert.assertEquals("Atom must have name signature " + s, s, p.getPredicate());
		Assert.assertArrayEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	/**
	 * Test predicate formula valid relation1.
	 */
	@Test
	public void testPredicateFormulaValidRelation1() {
		Relation r = Relation.create("s",
				new Attribute[]{Attribute.create(String.class, "a")});
		Term[] t = new Term[]{Variable.create("x")};
		Atom p = Atom.create(r, t);
		Assert.assertEquals("Atom must have name signature " + r, r, p.getPredicate());
		Assert.assertArrayEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	/**
	 * Test predicate formula valid relation2.
	 */
	@Test
	public void testPredicateFormulaValidRelation2() {
		Relation r = Relation.create("s",
				new Attribute[]{Attribute.create(String.class, "a")});
		Term[] t = new Term[]{TypedConstant.create("x")};
		Atom p = Atom.create(r, t);
		Assert.assertEquals("Atom must have name signature " + r, r, p.getPredicate());
		Assert.assertArrayEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	/**
	 * Test equality.
	 */
	@Test public void testEquality() {
		Relation r = Relation.create("s",
				new Attribute[]{Attribute.create(String.class, "a")});
		Term[] t = new Term[]{TypedConstant.create("x")};
		Atom p1 = Atom.create(r, t);
		Atom p2 = Atom.create(r, t);
		Assert.assertTrue("PredicateFormula p1 and p2 must be the same", p1.equals(p2));
	}

	/**
	 * Test equality wrong arity.
	 */
	@Test public void testEqualityWrongArity() {
		Relation r1 = Relation.create("r", new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")});
		Relation r2 = Relation.create("r", new Attribute[]{Attribute.create(String.class, "a")});
		Term[] t1 = new Term[]{TypedConstant.create("x"), TypedConstant.create("y")};
		Term[] t2 = new Term[]{TypedConstant.create("x")};
		Atom p1 = Atom.create(r1, t1);
		Atom p2 = Atom.create(r2, t2);
		Assert.assertFalse("PredicateFormula p1 and p2 have different arities", p1.equals(p2));
	}

	/**
	 * Test equality wrong name.
	 */
	@Test public void testEqualityWrongName() {
		Relation r1 = Relation.create("r1", new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")});
		Relation r2 = Relation.create("r2", new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")});
		Term[] t1 = new Term[]{TypedConstant.create("x"), TypedConstant.create("y")};
		Term[] t2 = new Term[]{TypedConstant.create("x"), TypedConstant.create("y")};
		Atom p1 = Atom.create(r1, t1);
		Atom p2 = Atom.create(r2, t2);
		Assert.assertFalse("PredicateFormula p1 and p2 have different names", p1.equals(p2));
	}

	/**
	 * Test hash duplicates.
	 */
	@Test public void testHashDuplicates() {
		Set<Atom> set = new LinkedHashSet<>();
		Relation r = Relation.create("r", new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")});
		TypedConstant c = TypedConstant.create("c");
		UntypedConstant s = UntypedConstant.create("s");
		Variable v = Variable.create("v");
		set.add(Atom.create(r, new Term[]{c, v}));
		set.add(Atom.create(r, new Term[]{c, c}));
		set.add(Atom.create(r, new Term[]{s, v}));
		set.add(Atom.create(r, new Term[]{s, c}));
		set.add(Atom.create(r, new Term[]{c, v}));
		set.add(Atom.create(r, new Term[]{c, c}));
		set.add(Atom.create(r, new Term[]{s, v}));
		set.add(Atom.create(r, new Term[]{s, c}));
		Assert.assertEquals("PredicateFormula set must have 4 elements", 4, set.size());
	}

	/**
	 * Test hash no duplicates.
	 */
	@Test public void testHashNoDuplicates() {
		Set<Atom> set = new LinkedHashSet<>();
		Relation r = Relation.create("r", new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")});
		TypedConstant c = TypedConstant.create("c");
		UntypedConstant s = UntypedConstant.create("s");
		Variable v = Variable.create("v");
		set.add(Atom.create(r, new Term[]{c, v}));
		set.add(Atom.create(r, new Term[]{c, c}));
		set.add(Atom.create(r, new Term[]{s, v}));
		set.add(Atom.create(r, new Term[]{s, c}));
		set.add(Atom.create(r, new Term[]{v, v}));
		set.add(Atom.create(r, new Term[]{c, s}));
		set.add(Atom.create(r, new Term[]{s, s}));
		set.add(Atom.create(r, new Term[]{v, s}));
		Assert.assertEquals("PredicateFormula set must have 8 elements", 8, set.size());
	}

	/**
	 * Test get term count.
	 */
	@Test public void testGetTermCount() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom must have " + t.length +  " terms ", t.length, p.getPredicate().getArity());
	}

	/**
	 * Test get term.
	 */
	@Test public void testGetTerm() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		for (int i = 0, l = t.length; i < l; i++) {
			Assert.assertEquals("Atom's " + i + "th term must be " +
					t[i], t[i], p.getTerm(i));
		}
	}

	/**
	 * Test get term out of range1.
	 */
	@Test (expected=IndexOutOfBoundsException.class)
	public void testGetTermOutOfRange1() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Atom.create(s, t).getTerm(-1);
	}

	/**
	 * Test get term out of range2.
	 */
	@Test (expected=IndexOutOfBoundsException.class)
	public void testGetTermOutOfRange2() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Atom.create(s, t).getTerm(5);
	}

	/**
	 * Test get terms.
	 */
	@Test public void testGetTerms() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p = Atom.create(s, t);
		Assert.assertArrayEquals("Atom terms must match term list of constructor",t, p.getTerms());
	}

	/**
	 * Test get selected terms.
	 */
	@Test public void testGetSelectedTerms() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom terms subset must match",
				Sets.newLinkedHashSet(),
				p.getTerms(Lists.<Integer>newArrayList()));
		Assert.assertEquals("Atom terms subset must match",
				Sets.newHashSet(Variable.create("x1")),
				p.getTerms(Lists.<Integer>newArrayList(0)));
		Assert.assertEquals("Atom terms subset must match",
				Sets.newHashSet(Variable.create("x3"), UntypedConstant.create("x4"), TypedConstant.create("x5")),
				p.getTerms(Lists.<Integer>newArrayList(2, 3, 4)));
	}

	/**
	 * Test get selected terms outof range1.
	 */
	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetSelectedTermsOutofRange1() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom.create(s, t).getTerms(Lists.<Integer>newArrayList(0, 2, -1));
	}

	/**
	 * Test get selected terms outof range2.
	 */
	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetSelectedTermsOutofRange2() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom.create(s, t).getTerms(Lists.<Integer>newArrayList(2, 1, 5));
	}

	/**
	 * Test get constants.
	 */
	@Test public void testGetConstants() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom terms subset must match",
				new Term[]{UntypedConstant.create("x4"), TypedConstant.create("x5")},
				Utility.getTypedAndUntypedConstants(p, new Integer[]{3, 4}));
	}

	/**
	 * Test get constants invalid.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testGetConstantsInvalid() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Utility.getTypedAndUntypedConstants(Atom.create(s, t),new Integer[]{0, 1});
	}

	/**
	 * Test get variable.
	 */
	@Test public void testGetVariable() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom variables subset must match",
				Lists.<Term>newArrayList(Variable.create("x1"), Variable.create("x2"), Variable.create("x4")),
				p.getVariables());
	}

	/**
	 * Test get all constants.
	 */
	@Test public void testGetAllConstants() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom variables subset must match",
				Sets.newHashSet(UntypedConstant.create("x3"), TypedConstant.create("x5")),
				Utility.getTypedAndUntypedConstants(p));
	}

	/**
	 * Gets the schema constants.
	 *
	 * @return the schema constants
	 */
	@Test public void getSchemaConstants() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), TypedConstant.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom variables subset must match",
				Lists.newArrayList(TypedConstant.create("x2"), TypedConstant.create("x5")), Utility.getTypedConstants(p));
	}

	/**
	 * Test get predicates.
	 */
	@Test public void testGetPredicates() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), TypedConstant.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom lists must match", Lists.newArrayList(p), p.getAtoms());
	}

	/**
	 * Test get atoms.
	 */
	@Test public void testGetAtoms() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), TypedConstant.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")
		};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom lists must match", Lists.newArrayList(p), p.getAtoms());
	}

	/**
	 * Test get term positions.
	 */
	@Test public void testGetTermPositions() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x1"), TypedConstant.create("x4")
		};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom x1 term positions must match", Lists.newArrayList(0, 3), Utility.getTermPositions(p,Variable.create("x1")));
		Assert.assertEquals("Atom x2 term positions must match", Lists.newArrayList(1), Utility.getTermPositions(p,Variable.create("x2")));
		Assert.assertEquals("Atom x3 term positions must match", Lists.newArrayList(2), Utility.getTermPositions(p,UntypedConstant.create("x3")));
		Assert.assertEquals("Atom x4 term positions must match", Lists.newArrayList(4), Utility.getTermPositions(p,TypedConstant.create("x4")));
	}

	/**
	 * Gets the term positions not found.
	 *
	 * @return the term positions not found
	 */
	@Test public void getTermPositionsNotFound() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x1"), TypedConstant.create("x4")
		};
		Assert.assertTrue("Atom term positions list must be empty", Utility.getTermPositions(Atom.create(s, t),Variable.create("x5")).isEmpty());

	}

	/**
	 * Test is fact.
	 */
	@Test public void testIsFact() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{
				TypedConstant.create("x1"), TypedConstant.create("x2"), TypedConstant.create("x3"),
				TypedConstant.create("x1"), TypedConstant.create("x4")
		};
		Atom p = Atom.create(s, t);
		Assert.assertTrue("Fact terms must contain schema constants only", p.isFact());
	}

	/**
	 * Test is not fact.
	 */
	@Test public void testIsNotFact() {
		Predicate s = new Predicate("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"), Variable.create("x1"), TypedConstant.create("x4")};
		Atom p = Atom.create(s, t);
		Assert.assertFalse("Fact terms must contain schema constants only", p.isFact());
	}
}
