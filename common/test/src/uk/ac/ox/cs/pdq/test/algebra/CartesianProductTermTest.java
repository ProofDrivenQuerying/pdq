// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.algebra;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the CartesianProductTerm class.
 * @author Mark Ridler
 *
 */
public class CartesianProductTermTest extends PdqTest {

	public CartesianProductTermTest() {
	}
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		PdqTest.reInitalize(this);
	}

	/*
	 * Tests the AccessTerm class by calling every method.
	 * Other classes are used as required.
	 * INPUTS: None
	 * OUTPUTS: Documented below
	 * PERFORMANCE: On 10/4/2018 this JUnit test returned in 0.5s on Mark's machine
	 */
	
	@Test
	public void testCreation() {
	
		Attribute[] a = new Attribute[1];
		a[0] = Attribute.create(String.class, "mark");
		Relation r = Relation.create("mark", a);
		AccessMethodDescriptor am = AccessMethodDescriptor.create("am", new Integer[] {0});
		AccessTerm at1 = AccessTerm.create(r, am);
		AccessTerm at2 = AccessTerm.create(r, am);
			
		RelationalTerm rt1 = at1;
		RelationalTerm rt2 = at2;
		
		// Constructor tests invariant
		CartesianProductTerm cpt = CartesianProductTerm.create(rt1, rt2);
		
		// CartesianProductTerm.equals null should be false
		boolean b = cpt.equals(null);
		Assert.assertFalse(b);
		
		// CartesianProductTerm.getClass has an expected name
		Assert.assertTrue(cpt.getClass().getName().equals("uk.ac.ox.cs.pdq.algebra.CartesianProductTerm"));
				
		// CartesianProductTerm.hascode non negative
		int h = cpt.hashCode();
		Assert.assertTrue(h >= 0);
			
		// Set returned from AccessTerm.getAccesses is empty
		Set<AccessTerm> sat = cpt.getAccesses();
		Assert.assertTrue(sat.size() == 1);

		// RelationalTerm returned from CartesianProductTerm.getChild is invariant
		RelationalTerm p = cpt.getChild(0);
		Assert.assertNotNull(p);

		// RelationalTerm returned from CartesianProductTerm.getInputAttribute is invariant
		Attribute aa = cpt.getInputAttribute(0);
		Assert.assertNotNull(aa);

		// array returned from CartesianProductTerm.getInputAttributes has zero length
		Attribute[] aaa = cpt.getInputAttributes();
		Assert.assertTrue(aaa.length == 2);

		// CartesianProductTerm.getNumberOfInputAttributes
		int nia = cpt.getNumberOfInputAttributes();
		Assert.assertTrue(nia <= 2);

		// CartesianProductTerm.getNumberOfOutputAttributes
		int noa = cpt.getNumberOfOutputAttributes();
		Assert.assertTrue(noa <= 2);

		// CartesianProductTerm.getOutputAttribute is invariant
		Attribute oa = cpt.getOutputAttribute(0);
		Assert.assertNotNull(oa);
		
		// CartesianProductTerm.getOutputAttributes has zero length
		Attribute[] oas = cpt.getOutputAttributes();
		Assert.assertTrue(oas.length == 2);
		
		// CartesianProductTerm.isClosed is false
        boolean ic = cpt.isClosed();
		Assert.assertFalse(ic);
        
	}
}
