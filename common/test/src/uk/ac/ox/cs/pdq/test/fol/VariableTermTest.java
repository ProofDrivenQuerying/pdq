package uk.ac.ox.cs.pdq.test.fol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Variable;

public class VariableTermTest {

	@Test
	public void testHashCode() {
		int n = 100;
		HashSet<Variable> terms = new HashSet<>();
		for (int i = 0; i < n; i++) {
			terms.add(new Variable("x" + i));
		}
		assertEquals(n, terms.size());
		for (int i = 0; i < n; i++) {
			assertTrue(terms.contains(new Variable("x" + i)));
		}
	}

	@Test
	public void testEquals() {
		int n = 100;
		Set<Variable> terms1 = new LinkedHashSet<>();
		Set<Variable> terms2 = new LinkedHashSet<>();
		for (int i = 0; i < n; i++) {
			assertEquals(new Variable("x" + i), new Variable("x" + i));
		}
	}
}
