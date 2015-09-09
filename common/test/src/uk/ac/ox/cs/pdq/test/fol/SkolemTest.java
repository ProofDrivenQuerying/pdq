package uk.ac.ox.cs.pdq.test.fol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Skolem;


/**
 *
 * @author Julien Leblay
 *
 */
public final class SkolemTest {

	@Test public void testNoTwoSimilaryGeneratedSkolems() {
		Assert.assertNotEquals(
				"Two Skolem generated consecutively must not be equals",
				Skolem.getFreshConstant(),
				Skolem.getFreshConstant());
	}

	@Test public void testGeneratedSkolemsSimilarAfterReset() {
		Skolem.resetCounter();
		Skolem s1 = Skolem.getFreshConstant();
		Skolem.resetCounter();
		Skolem s2 = Skolem.getFreshConstant();
		Assert.assertEquals(
				"Two Skolem generated right after a reset must be equal",
				s1, s2);
	}


	@Test public void testSkolemIsSkolem() {
		Assert.assertTrue("Skolem.isSkolem must be always true", new Skolem("v").isSkolem());
	}

	@Test public void testSkolemIsNotVariable() {
		Assert.assertFalse("Skolem.isVariable must be always false", new Skolem("v").isVariable());
	}

	@Test public void testSkolemValid() {
		Skolem v = new Skolem("v");
		Assert.assertEquals("Skolem must have name 'v'", "v", v.getName());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSkolemEmptyName() {
		new Skolem("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSkolemNullName() {
		new Skolem(null);
	}

	@Test public void testHashCode() {
		int n = 100;
		HashSet<Skolem> terms = new HashSet<>();
		for (int i = 0; i < n; i++) {
			terms.add(new Skolem("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(new Skolem("x" + i)));
		}
		for (int i = 0; i < n; i++) {
			terms.add(new Skolem("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(new Skolem("x" + i)));
		}
	}

	@Test public void testEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertEquals(new Skolem("x" + i), new Skolem("x" + i));
		}
	}

	@Test public void testNotEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertNotEquals(new Skolem("x" + i), new Skolem("y" + i));
		}
	}
}
