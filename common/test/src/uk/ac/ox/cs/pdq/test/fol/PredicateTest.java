package uk.ac.ox.cs.pdq.test.fol;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.util.Utility;

// @author Julien Leblay
public class PredicateTest {

	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	// Creates predicate then checks for name and arity of one
	@Test public void testSignatureValid() {
		Predicate s = Predicate.create("s", 1);
		Assert.assertEquals("Predicate must have name 's'", "s", s.getName());
		Assert.assertEquals("Predicate must have arity 1", 1, s.getArity());
	}

	// Creates predicate then checks for name and arity of zero
	@Test public void testSignatureZeroArity() {
		Predicate s = Predicate.create("s", 0);
		Assert.assertEquals("Predicate must have name 's'", "s", s.getName());
		Assert.assertEquals("Predicate must have arity 0", 0, s.getArity());
	}

	// Creates predicate with no name and expects IllegalArgumentException
	@Test(expected=IllegalArgumentException.class)
	public void testSignatureEmptyName() {
		Predicate.create("", 0);
	}

	
	// Creates predicate with null name and expects IllegalArgumentException
	@Test(expected=IllegalArgumentException.class)
	public void testSignatureNullName() {
		Predicate.create(null, 0);
	}

	// Creates predicate with negative arity and expects IllegalArgumentException
	@Test(expected=IllegalArgumentException.class)
	public void testNegativeArity() {
		Predicate.create("s", -1);
	}

	// Creates 2 predicates and checks for equality
	@Test public void testEquality() {
		Predicate s1 = Predicate.create("s", 5);
		Predicate s2 = Predicate.create("s", 5);
		Assert.assertTrue("Signatures s1 and s2 must be the same", s1.equals(s2));
	}

	// Creates 2 predicates with different aritys and checks for inequality
	@Test public void testEqualityWrongArity() {
		Predicate s1 = Predicate.create("s", 5);
		Predicate s2 = Predicate.create("s", 1);
		Assert.assertFalse("Signatures s1 and s2 have different arities", s1.equals(s2));
	}

	// Creates 2 predicates with different aritys and checks for inequality
	@Test public void testEqualityWrongName() {
		Predicate s1 = Predicate.create("s1", 5);
		Predicate s2 = Predicate.create("s2", 5);
		Assert.assertFalse("Signatures s1 and s2 have different arities", s1.equals(s2));
	}

	// Creates a set of 8 predicates with 4 uniques, then tests for set size
	@Test public void testHashDuplicates() {
		Set<Predicate> set = new LinkedHashSet<>();
		set.add(Predicate.create("s", 0));
		set.add(Predicate.create("s", 1));
		set.add(Predicate.create("s", 2));
		set.add(Predicate.create("s", 4));
		set.add(Predicate.create("s", 0));
		set.add(Predicate.create("s", 1));
		set.add(Predicate.create("s", 2));
		set.add(Predicate.create("s", 4));
		Assert.assertEquals("Predicate set must have 4 elements", 4, set.size());
	}

	// Creates a set of 8 unique predicates, then tests for set size
	@Test public void testHashNoDuplicates() {
		Set<Predicate> set = new LinkedHashSet<>();
		set.add(Predicate.create("s", 0));
		set.add(Predicate.create("s", 1));
		set.add(Predicate.create("s", 2));
		set.add(Predicate.create("s", 4));
		set.add(Predicate.create("r", 0));
		set.add(Predicate.create("r", 1));
		set.add(Predicate.create("r", 2));
		set.add(Predicate.create("r", 4));
		Assert.assertEquals("Predicate set must have 8 elements", 8, set.size());
	}
}
