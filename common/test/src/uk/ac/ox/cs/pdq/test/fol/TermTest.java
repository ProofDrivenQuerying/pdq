package uk.ac.ox.cs.pdq.test.fol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

public class TermTest {
	
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	// Creates a hash set of uniquely named variables then asserts that the set contains those variables
	@Test
	public void testHashCode() {
		int n = 100;
		HashSet<Variable> terms = new HashSet<>();
		for (int i = 0; i < n; i++) {
			terms.add(Variable.create("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(Variable.create("x" + i)));
		}
	}

	// Assert that variables with the same name are the same
	@Test
	public void testEquals() {
		int n = 100;
		for (int i = 0; i < n; i++) {
			assertEquals(Variable.create("x" + i), Variable.create("x" + i));
		}
	}
}
