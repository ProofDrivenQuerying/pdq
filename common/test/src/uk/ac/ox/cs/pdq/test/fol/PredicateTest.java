package uk.ac.ox.cs.pdq.test.fol;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * A predicate's signature, associate a symbol with an arity..
 *
 * @author Julien Leblay
 */
public class PredicateTest {

	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	/**
	 * Test signature valid.
	 */
	@Test public void testSignatureValid() {
		Predicate s = new Predicate("s", 1);
		Assert.assertEquals("Predicate must have name 's'", "s", s.getName());
		Assert.assertEquals("Predicate must have arity 1", 1, s.getArity());
	}

	/**
	 * Test signature zero arity.
	 */
	@Test public void testSignatureZeroArity() {
		Predicate s = new Predicate("s", 0);
		Assert.assertEquals("Predicate must have name 's'", "s", s.getName());
		Assert.assertEquals("Predicate must have arity 0", 0, s.getArity());
	}

	/**
	 * Test signature empty name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testSignatureEmptyName() {
		new Predicate("", 0);
	}

	/**
	 * Test signature null name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testSignatureNullName() {
		new Predicate(null, 0);
	}

	/**
	 * Test negative arity.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testNegativeArity() {
		new Predicate("s", -1);
	}

	/**
	 * Test equality.
	 */
	@Test public void testEquality() {
		Predicate s1 = new Predicate("s", 5);
		Predicate s2 = new Predicate("s", 5);
		Assert.assertTrue("Signatures s1 and s2 must be the same", s1.equals(s2));
	}

	/**
	 * Test equality wrong arity.
	 */
	@Test public void testEqualityWrongArity() {
		Predicate s1 = new Predicate("s", 5);
		Predicate s2 = new Predicate("s", 1);
		Assert.assertFalse("Signatures s1 and s2 have different arities", s1.equals(s2));
	}

	/**
	 * Test equality wrong name.
	 */
	@Test public void testEqualityWrongName() {
		Predicate s1 = new Predicate("s", 5);
		Predicate s2 = new Predicate("s", 1);
		Assert.assertNotEquals("Signatures s1 and s2 have different arities", s1.equals(s2));
	}

	/**
	 * Test hash duplicates.
	 */
	@Test public void testHashDuplicates() {
		Set<Predicate> set = new LinkedHashSet<>();
		set.add(new Predicate("s", 0));
		set.add(new Predicate("s", 1));
		set.add(new Predicate("s", 2));
		set.add(new Predicate("s", 4));
		set.add(new Predicate("s", 0));
		set.add(new Predicate("s", 1));
		set.add(new Predicate("s", 2));
		set.add(new Predicate("s", 4));
		Assert.assertEquals("Predicate set must have 4 elements", 4, set.size());
	}

	/**
	 * Test hash no duplicates.
	 */
	@Test public void testHashNoDuplicates() {
		Set<Predicate> set = new LinkedHashSet<>();
		set.add(new Predicate("s", 0));
		set.add(new Predicate("s", 1));
		set.add(new Predicate("s", 2));
		set.add(new Predicate("s", 4));
		set.add(new Predicate("r", 0));
		set.add(new Predicate("r", 1));
		set.add(new Predicate("r", 2));
		set.add(new Predicate("r", 4));
		Assert.assertEquals("Predicate set must have 8 elements", 8, set.size());
	}
}
