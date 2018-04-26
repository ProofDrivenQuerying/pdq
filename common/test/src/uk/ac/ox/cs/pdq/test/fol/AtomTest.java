package uk.ac.ox.cs.pdq.test.fol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

public class AtomTest {

	private Random random = new Random();
	
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	// Create a linked hash set with terms of random arity, variables and predicates
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
			Predicate predicate = Predicate.create("r" + i, terms.length);
			formulae.add(Atom.create(predicate, terms));
		}
		Assert.assertEquals(n, formulae.size());
		for (int i = 0; i < n; i++) {
			Term[] terms = new Term[arities[i]];
			for (int j = 0, l = terms.length; j < l; j++) {
				terms[j] = Variable.create("x" + j);
			}
			Predicate predicate = Predicate.create("r" + i, terms.length);
			Assert.assertTrue(formulae.contains(Atom.create(predicate, terms)));
		}
	}

	// Creates a predicate, terms of variables and un/typed constants, then creates an atom from that
	@Test public void testPredicateFormulaValid() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p = Atom.create( s, t);
		Assert.assertEquals("Atom must have name signature " + s, s, p.getPredicate());
		Assert.assertArrayEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	// Creates an atom with minimal predicate and term
	@Test public void testPredicateFormulaZeroArity() {
		Predicate s = Predicate.create("s", 0);
		Term[] t = new Term[0];
		Atom p = Atom.create( s, t);
		Assert.assertEquals("Atom must have name signature " + s, s, p.getPredicate());
		Assert.assertArrayEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	// Creates an atom from relation and term of Variable
	@Test
	public void testPredicateFormulaValidRelation1() {
		Relation r = Relation.create("s",
				new Attribute[]{Attribute.create(String.class, "a")});
		Term[] t = new Term[]{Variable.create("x")};
		Atom p = Atom.create(r, t);
		Assert.assertNotEquals("Atom must not have name signature " + r, r, p.getPredicate());
		Assert.assertArrayEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	// Creates an atom from relation and term of TypedConstant, x
	@Test
	public void testPredicateFormulaValidRelation2() {
		Relation r = Relation.create("s",
				new Attribute[]{Attribute.create(String.class, "a")});
		Term[] t = new Term[]{TypedConstant.create("x")};
		Atom p = Atom.create(r, t);
		Assert.assertNotEquals("Atom must not have name signature " + r, r, p.getPredicate());
		Assert.assertArrayEquals("Atom must have name terms " + t, t, p.getTerms());
	}

	// Creates 2 atoms from relation and term of TypedConstant, x
	@Test public void testEquality() {
		Relation r = Relation.create("s",
				new Attribute[]{Attribute.create(String.class, "a")});
		Term[] t = new Term[]{TypedConstant.create("x")};
		Atom p1 = Atom.create(r, t);
		Atom p2 = Atom.create(r, t);
		Assert.assertTrue("PredicateFormula p1 and p2 must be the same", p1.equals(p2));
	}

	// Creates 2 atoms from 2 relations, r, and 2 terms of TypedConstant, x, y and x
	@Test public void testEqualityWrongArity() {
		Relation r1 = Relation.create("r", new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")});
		Relation r2 = Relation.create("r", new Attribute[]{Attribute.create(String.class, "a")});
		Term[] t1 = new Term[]{TypedConstant.create("x"), TypedConstant.create("y")};
		Term[] t2 = new Term[]{TypedConstant.create("x")};
		Atom p1 = Atom.create(r1, t1);
		Atom p2 = Atom.create(r2, t2);
		Assert.assertFalse("PredicateFormula p1 and p2 have different arities", p1.equals(p2));
	}

	//  Creates 2 atoms from 2 relations, r1, r2, and 2 terms of TypedConstant, x, y and x, y
	@Test public void testEqualityWrongName() {
		Relation r1 = Relation.create("r1", new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")});
		Relation r2 = Relation.create("r2", new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")});
		Term[] t1 = new Term[]{TypedConstant.create("x"), TypedConstant.create("y")};
		Term[] t2 = new Term[]{TypedConstant.create("x"), TypedConstant.create("y")};
		Atom p1 = Atom.create(r1, t1);
		Atom p2 = Atom.create(r2, t2);
		Assert.assertFalse("PredicateFormula p1 and p2 have different names", p1.equals(p2));
	}

	// Creates a set of atoms from typed and untyped constants and variables with duplicates
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

	// Creates a set of atoms from typed and untyped constants and variables with no duplicates
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

	// Creates an atom from predicate and 5 terms and checks length
	@Test public void testGetTermCount() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom must have " + t.length +  " terms ", t.length, p.getPredicate().getArity());
	}

	// Creates an atom from predicate and 5 terms and checks them all
	@Test public void testGetTerm() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		for (int i = 0, l = t.length; i < l; i++) {
			Assert.assertEquals("Atom's " + i + "th term must be " +
					t[i], t[i], p.getTerm(i));
		}
	}

	// Creates an atom from predicate and 5 terms then causes an OutOfBounds exception (low)
	@Test (expected=IndexOutOfBoundsException.class)
	public void testGetTermOutOfRange1() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Atom.create(s, t).getTerm(-1);
	}

	// Creates an atom from predicate and 5 terms then causes an OutOfBounds exception (high)
	@Test (expected=IndexOutOfBoundsException.class)
	public void testGetTermOutOfRange2() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Atom.create(s, t).getTerm(5);
	}

	// Creates an atom from predicate and 5 terms then compares with getTerms
	@Test public void testGetTerms() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p = Atom.create(s, t);
		Assert.assertArrayEquals("Atom terms must match term list of constructor",t, p.getTerms());
	}

	// Creates an atom from predicate and 5 terms then compares atom term subsets
	@Test public void testGetSelectedTerms() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom terms subset must match",
				new LinkedHashSet<>(),
				p.getTerms(new ArrayList<Integer>()));
		Assert.assertEquals("Atom terms subset must match",
				Sets.newHashSet(Variable.create("x1")),
				p.getTerms(Lists.<Integer>newArrayList(0)));
		Assert.assertEquals("Atom terms subset must match",
				Sets.newHashSet(Variable.create("x3"), UntypedConstant.create("x4"), TypedConstant.create("x5")),
				p.getTerms(Lists.<Integer>newArrayList(2, 3, 4)));
	}

	// Creates an atom from predicate and 5 terms then causes an OutOfBounds exception (low)
	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetSelectedTermsOutofRange1() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom.create(s, t).getTerms(Lists.<Integer>newArrayList(0, 2, -1));
	}

	// Creates an atom from predicate and 5 terms then causes an OutOfBounds exception (high)
	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetSelectedTermsOutofRange2() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")
		};
		Atom.create(s, t).getTerms(Lists.<Integer>newArrayList(2, 1, 5));
	}

	// Creates an atom from predicate and 5 terms then checks atom terms subsets
	@Test public void testGetConstants() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		List<Constant> list = new ArrayList<>();
		list.add(UntypedConstant.create("x4"));
		list.add(TypedConstant.create("x5"));
		Assert.assertEquals("Atom terms subset must match",
				list,
				Utility.getTypedAndUntypedConstants(p, new Integer[]{3, 4}));
	}

	// Creates an atom from predicate and 5 terms then calls Utility.getTypedAndUntypedConstants
	@Test(expected=IllegalArgumentException.class)
	public void testGetConstantsInvalid() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), Variable.create("x3"),
				UntypedConstant.create("x4"), TypedConstant.create("x5")};
		Utility.getTypedAndUntypedConstants(Atom.create(s, t),new Integer[]{0, 1});
	}

	// Creates an atom from predicate and 5 terms then compares variables subset
	@Test public void testGetVariable() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		Assert.assertArrayEquals("Atom variables subset must match",
				new Variable[]{Variable.create("x1"), Variable.create("x2"), Variable.create("x4")},
				p.getVariables());
	}

	// Creates an atom from predicate and 5 terms then compares constants subset
	@Test public void testGetAllConstants() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		Set<Constant> list = new HashSet<>();
		list.add(UntypedConstant.create("x3"));
		list.add(TypedConstant.create("x5"));
		
		Assert.assertEquals("Atom variables subset must match",
				list,
				Utility.getTypedAndUntypedConstants(p));
	}

	// Creates an atom from predicate and 5 terms then compares typed constants subset
	@Test public void getSchemaConstants() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), TypedConstant.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		Assert.assertEquals("Atom variables subset must match",
				Lists.newArrayList(TypedConstant.create("x2"), TypedConstant.create("x5")), 
				Utility.getTypedConstants(p));
	}

	// Creates an atom from predicate and 5 terms then compares atom lists
	@Test public void testGetPredicates() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), TypedConstant.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x4"), TypedConstant.create("x5")};
		Atom p = Atom.create(s, t);
		Assert.assertArrayEquals("Atom lists must match", new Atom[]{p}, p.getAtoms());
	}

	// Creates an atom from predicate and 5 terms then checks atom term positions
	@Test public void testGetTermPositions() {
		Predicate s = Predicate.create("s", 5);
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

	// Creates an atom from predicate and 5 terms then checks atom term positions list is empty
	@Test public void getTermPositionsNotFound() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"),
				Variable.create("x1"), TypedConstant.create("x4")
		};
		Assert.assertTrue("Atom term positions list must be empty", Utility.getTermPositions(Atom.create(s, t),Variable.create("x5")).isEmpty());

	}

	// Creates an atom from predicate and 5 constant terms then checks atom is ground
	@Test public void testIsFact() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{
				TypedConstant.create("x1"), TypedConstant.create("x2"), TypedConstant.create("x3"),
				TypedConstant.create("x1"), TypedConstant.create("x4")
		};
		Atom p = Atom.create(s, t);
		Assert.assertTrue("Fact terms must contain schema constants only", p.isGround());
	}

	// Creates an atom from predicate and 5 constant and variable terms then checks atom is not ground
	@Test public void testIsNotFact() {
		Predicate s = Predicate.create("s", 5);
		Term[] t = new Term[]{Variable.create("x1"), Variable.create("x2"), UntypedConstant.create("x3"), Variable.create("x1"), TypedConstant.create("x4")};
		Atom p = Atom.create(s, t);
		Assert.assertFalse("Fact terms must contain schema constants only", p.isGround());
	}
}
