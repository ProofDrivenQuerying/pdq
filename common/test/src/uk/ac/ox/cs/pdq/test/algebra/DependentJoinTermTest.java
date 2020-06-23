// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.algebra;

import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

public class DependentJoinTermTest extends PdqTest {

	
	Attribute[] attributes_left = new Attribute[] {Attribute.create(Integer.class, "a"),
	Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")};
	
	Attribute[] attributes_right = new Attribute[] {Attribute.create(Integer.class, "c"),
			Attribute.create(Integer.class, "d"), Attribute.create(String.class, "e")};

	
	// Create 2 access terms, dependent join term, then create 2 dependent join terms to fail
	@Test
	public void testDependentJoinTerm() {

		DependentJoinTerm target;

		AccessTerm leftChild; 
		AccessTerm rightChild;
 		
		leftChild = AccessTerm.create(Relation.create("r",  new Attribute[] { Attribute.create(Integer.class, "c") }), AccessMethodDescriptor.create("am", new Integer[] {0}));

		rightChild = AccessTerm.create(Relation.create("r",  new Attribute[] { Attribute.create(Integer.class, "c") }), AccessMethodDescriptor.create("am", new Integer[] {0}));

		
		target = DependentJoinTerm.create(leftChild, rightChild);
		Assert.assertTrue(target instanceof DependentJoinTerm);

		/*
		 * Test invalid constructions.
		 */
		boolean caught;
		
		caught = false; 
		try {
			DependentJoinTerm.create(leftChild, rightChild);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertFalse(caught);
		
		caught = false; 
		try {
			DependentJoinTerm.create(leftChild, rightChild);
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertFalse(caught);
		
	}
	
	// Create 2 access terms and 4 dependent join terms
	@Test
	public void testBoundIndices() {

		Map<Integer, Integer> expected;

		AccessTerm leftChild; 
		AccessTerm rightChild;

		leftChild = AccessTerm.create(Relation.create("r",  new Attribute[] { Attribute.create(Integer.class, "c") }), AccessMethodDescriptor.create("am", new Integer[] {0}));

		rightChild = AccessTerm.create(Relation.create("r",  new Attribute[] { Attribute.create(Integer.class, "c") }), AccessMethodDescriptor.create("am", new Integer[] {0}));
		
		DependentJoinTerm.create(leftChild, rightChild);
		
		expected = new LinkedHashMap<>();
		expected.put(0, 2);
		
		DependentJoinTerm.create(leftChild, rightChild);
		
		expected = new LinkedHashMap<>();
		expected.put(0, 1);
		
		DependentJoinTerm.create(leftChild, rightChild);
		
		// The bound indices include only the inputs to the right child.
		expected = new LinkedHashMap<>();
		expected.put(0, 1);
		
		DependentJoinTerm.create(leftChild, rightChild);
		
		// The bound indices include only the inputs to the right child.
		expected = new LinkedHashMap<>();
		expected.put(0, 2);
		expected.put(1, 1);
	}
	
	// Create 2 access terms then 4 dependent join terms
	@Test
	public void testGetInputAttributes() {

		DependentJoinTerm target;
		Attribute[] result;

		AccessTerm leftChild; 
		AccessTerm rightChild;

		leftChild = Mockito.mock(AccessTerm.class);
		when(leftChild.getOutputAttributes()).thenReturn(attributes_left);

		rightChild = Mockito.mock(AccessTerm.class);
		when(rightChild.getOutputAttributes()).thenReturn(attributes_right);

		when(leftChild.getInputAttributes()).thenReturn(new Attribute[0]);
		when(rightChild.getInputAttributes()).thenReturn(new Attribute[] {Attribute.create(Integer.class, "c")});
		
		target = DependentJoinTerm.create(leftChild, rightChild);
		
		// The only input attribute is the bound one in the right child, so the plan has no input attributes. 
		result = target.getInputAttributes();
		Assert.assertEquals(0, result.length);

		// New test.
		when(leftChild.getInputAttributes()).thenReturn(new Attribute[] {Attribute.create(Integer.class, "a")});
		when(rightChild.getInputAttributes()).thenReturn(new Attribute[] {Attribute.create(Integer.class, "c")});
		
		target = DependentJoinTerm.create(leftChild, rightChild);
		
		result = target.getInputAttributes();
		Assert.assertEquals(0, result.length);

		// New test.
		when(leftChild.getInputAttributes()).thenReturn(new Attribute[0]);
		when(rightChild.getInputAttributes()).thenReturn(new Attribute[] {Attribute.create(Integer.class, "c"), 
				Attribute.create(Integer.class, "d")});
		
		target = DependentJoinTerm.create(leftChild, rightChild);
		
		result = target.getInputAttributes();
		Assert.assertEquals(0, result.length);
		
		// New test.
		when(leftChild.getInputAttributes()).thenReturn(new Attribute[] {Attribute.create(Integer.class, "b")});
		when(rightChild.getInputAttributes()).thenReturn(new Attribute[] {Attribute.create(Integer.class, "c"), 
				Attribute.create(Integer.class, "e")});
		
		target = DependentJoinTerm.create(leftChild, rightChild);
		
		result = target.getInputAttributes();
		Assert.assertEquals(0, result.length);
		//Assert.assertEquals(Attribute.create(Integer.class, "b"), result[0]);
		//Assert.assertEquals(Attribute.create(Integer.class, "e"), result[1]);
		
	}
	
	@Test
	public void testInputOnBothLeftAndRight() { 
		Relation relation1 = Relation.create("relation1", new Attribute[] {
				Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2")
				});
		Relation relation2 = Relation.create("relation2", new Attribute[] {
				Attribute.create(String.class, "attribute2"),Attribute.create(String.class, "attribute3")});
		RelationalTerm child1 = AccessTerm.create(relation1, AccessMethodDescriptor.create(new Integer[] {0})); 
		RelationalTerm child2 = AccessTerm.create(relation2, AccessMethodDescriptor.create(new Integer[] {0,1}));
	
		// Constructor tests invariant
		DependentJoinTerm target = DependentJoinTerm.create(child1, child2);
		Attribute[] result = target.getInputAttributes();
		Assert.assertEquals(2, result.length);
	}
	
	/*
	 * Tests the DependentJoinTerm class by calling every method.
	 * Other classes are used as required.
	 * INPUTS: None
	 * OUTPUTS: Documented below
	 * PERFORMANCE: On 11/4/2018 this JUnit test returned in 0.5s on Mark's machine
	 */
	
	@Test
	public void testCreation() {
	
		Relation relation1 = Relation.create("relation1", new Attribute[] {
				Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2")
				});
		Relation relation2 = Relation.create("relation2", new Attribute[] {
				Attribute.create(String.class, "attribute2")});
		RelationalTerm child1 = AccessTerm.create(relation1, AccessMethodDescriptor.create(new Integer[] {0})); 
		RelationalTerm child2 = AccessTerm.create(relation2, AccessMethodDescriptor.create(new Integer[] {0}));
	
		// Constructor tests invariant
		DependentJoinTerm djt = DependentJoinTerm.create(child1, child2);
		
		// DependentJoinTerm.equals null should be false
		boolean b = djt.equals(null);
		Assert.assertFalse(b);
		
		// DependentJoinTerm.getClass has an expected name
		Assert.assertTrue(djt.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.DependentJoinTerm");
				
		// DependentJoinTerm.hashCode is non negative
		int h = djt.hashCode();
		Assert.assertTrue(h >= 0);

		// DependentJoinTerm.toString is #1=#2&#3=#4
		String s = djt.toString();
		Assert.assertTrue(s.equals("DependentJoin{[(#1=#2)]Access{relation1.mt_0[#0=attribute1]},Access{relation2.mt_1[#0=attribute2]}}"));
		
		// RelationalTerm returned from DependentJoinTerm.getChild is invariant
		RelationalTerm p = djt.getChild(0);
		Assert.assertNotNull(p);

		// array returned from DependentJoinTerm.getChildren has zero length
		RelationalTerm[] pp = djt.getChildren();
		Assert.assertTrue(pp.length == 2);

        // Class returned from getClass has name DependentJoinTerm 
		Assert.assertTrue(djt.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.DependentJoinTerm");
		
		// RelationalTerm returned from DependentJoinTerm.getInputAttribute is invariant
		Attribute aa = djt.getInputAttribute(0);
		Assert.assertNotNull(aa);

		// array returned from DependentJoinTerm.getInputAttributes has zero length
		Attribute[] aaa = djt.getInputAttributes();
		Assert.assertTrue(aaa.length == 1);
		
		// DependentJoinTerm.getNumberOfInputAttributes is one
		int nia = djt.getNumberOfInputAttributes();
		Assert.assertTrue(nia == 1);

		// DependentJoinTerm.getNumberOfOutputAttributes is one
		int noa = djt.getNumberOfOutputAttributes();
		Assert.assertTrue(noa == 3);

		// DependentJoinTerm.getOutputAttribute is invariant
		Attribute oa = djt.getOutputAttribute(0);
		Assert.assertNotNull(oa);
		
		// DependentJoinTerm.getOutputAttributes has length three
		Attribute[] oas = djt.getOutputAttributes();
		Assert.assertTrue(oas.length == 3);
		
		// isClosed is true
		boolean b2 = djt.isClosed();
		Assert.assertFalse(b2);
			
		// getPositionsInLeftChildThatAreInputToRightChild is invariant
		Map<Integer, Integer> m = djt.getPositionsInLeftChildThatAreInputToRightChild();
		Assert.assertNotNull(m);
		Assert.assertTrue(m.toString().equals("{0=1}"));
	

	}
}
