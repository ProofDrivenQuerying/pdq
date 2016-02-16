package uk.ac.ox.cs.pdq.test.fol;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * A predicate's signature, associate a symbol with an arity..
 *
 * @author Julien Leblay
 */
public class SignatureTest {

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
		Signature s = new Signature("s", 1);
		Assert.assertEquals("Signature must have name 's'", "s", s.getName());
		Assert.assertEquals("Signature must have arity 1", 1, s.getArity());
	}

	/**
	 * Test signature zero arity.
	 */
	@Test public void testSignatureZeroArity() {
		Signature s = new Signature("s", 0);
		Assert.assertEquals("Signature must have name 's'", "s", s.getName());
		Assert.assertEquals("Signature must have arity 0", 0, s.getArity());
	}

	/**
	 * Test signature empty name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testSignatureEmptyName() {
		new Signature("", 0);
	}

	/**
	 * Test signature null name.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testSignatureNullName() {
		new Signature(null, 0);
	}

	/**
	 * Test negative arity.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testNegativeArity() {
		new Signature("s", -1);
	}

	/**
	 * Test equality.
	 */
	@Test public void testEquality() {
		Signature s1 = new Signature("s", 5);
		Signature s2 = new Signature("s", 5);
		Assert.assertTrue("Signatures s1 and s2 must be the same", s1.equals(s2));
	}

	/**
	 * Test equality wrong arity.
	 */
	@Test public void testEqualityWrongArity() {
		Signature s1 = new Signature("s", 5);
		Signature s2 = new Signature("s", 1);
		Assert.assertFalse("Signatures s1 and s2 have different arities", s1.equals(s2));
	}

	/**
	 * Test equality wrong name.
	 */
	@Test public void testEqualityWrongName() {
		Signature s1 = new Signature("s", 5);
		Signature s2 = new Signature("s", 1);
		Assert.assertNotEquals("Signatures s1 and s2 have different arities", s1.equals(s2));
	}

	/**
	 * Test hash duplicates.
	 */
	@Test public void testHashDuplicates() {
		Set<Signature> set = new LinkedHashSet<>();
		set.add(new Signature("s", 0));
		set.add(new Signature("s", 1));
		set.add(new Signature("s", 2));
		set.add(new Signature("s", 4));
		set.add(new Signature("s", 0));
		set.add(new Signature("s", 1));
		set.add(new Signature("s", 2));
		set.add(new Signature("s", 4));
		Assert.assertEquals("Signature set must have 4 elements", 4, set.size());
	}

	/**
	 * Test hash no duplicates.
	 */
	@Test public void testHashNoDuplicates() {
		Set<Signature> set = new LinkedHashSet<>();
		set.add(new Signature("s", 0));
		set.add(new Signature("s", 1));
		set.add(new Signature("s", 2));
		set.add(new Signature("s", 4));
		set.add(new Signature("r", 0));
		set.add(new Signature("r", 1));
		set.add(new Signature("r", 2));
		set.add(new Signature("r", 4));
		Assert.assertEquals("Signature set must have 8 elements", 8, set.size());
	}
}
