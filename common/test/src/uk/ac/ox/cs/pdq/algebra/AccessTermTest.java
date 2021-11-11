// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.util.PdqTest;

import java.util.*;

import static org.mockito.Mockito.when;

public class AccessTermTest extends PdqTest {

	// Dummy concrete class for testing.
	public class ConcreteAccessMethod  extends AccessMethodDescriptor {
		private static final long serialVersionUID = 1L;
		
		public ConcreteAccessMethod(Attribute[] attributes, Integer[] inputs, Relation relation, 
				Map<Attribute, Attribute> attributeMapping) {
			super(inputs);
		}
		
		public ConcreteAccessMethod(Attribute[] attributes, Set<Attribute> inputAttributes, 
				Relation relation, Map<Attribute, Attribute> attributeMapping) {
			super(new Integer[] {0});
		}
		
	}
	
	@Test
	public void testAccessTerm() {
		
		AccessMethodDescriptor accessMethod;
		AccessTerm target;
		Integer[] inputs;
		Map<Attribute, TypedConstant> inputConstants;
		
		Relation relation = Mockito.mock(Relation.class);
		Attribute[] relationAttributes = new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
		when(relation.getAttributes()).thenReturn(relationAttributes.clone());

		Attribute[] amAttributes = new Attribute[] {
				Attribute.create(String.class, "W"), Attribute.create(Integer.class, "X"),
				Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "Z")};
		
		Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
		attributeMapping.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
		attributeMapping.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
		attributeMapping.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

		/*
		 *  Plan: Free access
		 */
		inputs = new Integer[0];
		accessMethod= new ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);
		
		target = AccessTerm.create(relation, accessMethod);
		Assert.assertNotNull(target);

		/*
		 *  Plan: Access with input at index 0 and no input constants.
		 */
		inputs = new Integer[] {0};
		accessMethod= new ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);

		target = AccessTerm.create(relation, accessMethod);
		Assert.assertNotNull(target);

		/*
		 *  Plan: Access with input at index 0 in the access method schema and an input constant for that input.
		 */
		inputConstants = new HashMap<>();
		inputConstants.put(Attribute.create(String.class, "c"), TypedConstant.create("CONSTANT STRING"));
		
		target = AccessTerm.create(relation, accessMethod);
		Assert.assertNotNull(target);
		
		/*
		 *  Plan: Access with inputs at indices 0 & 1 in the access method schema and input constants for those inputs.
		 */
		inputs = new Integer[] {0, 1};
		accessMethod= new ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);

		inputConstants = new HashMap<>();
		inputConstants.put(Attribute.create(Integer.class, "a"), TypedConstant.create(1));
		inputConstants.put(Attribute.create(String.class, "c"), TypedConstant.create("CONSTANT STRING"));

		target = AccessTerm.create(relation, accessMethod);
		Assert.assertNotNull(target);

		// Test consistency of the attributes and input constants.
		boolean caught;

		inputs = new Integer[] {0, 1};
		accessMethod= new ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);
		
		inputConstants = new HashMap<>();
		inputConstants.put(Attribute.create(String.class, "a"), TypedConstant.create("TYPE CONFLICT"));
		inputConstants.put(Attribute.create(String.class, "c"), TypedConstant.create("CONSTANT STRING"));
		
		caught = false;
		try {
			target = AccessTerm.create(relation, accessMethod);
		} catch(IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertFalse(caught);
	}
	
	@Test
	public void testGetInputAttributes() {
		
		AccessMethodDescriptor accessMethod;
		AccessTerm target;
		Integer[] inputs;
		Set<Attribute> inputAttributes;
		Map<Integer, TypedConstant> inputConstants;
		Attribute[] result;
		
		/*
		 *  Plan: Free access
		 */
		Relation relation = Mockito.mock(Relation.class);
		Attribute[] relationAttributes = new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
		when(relation.getAttributes()).thenReturn(relationAttributes.clone());

		Attribute[] amAttributes = new Attribute[] {
				Attribute.create(String.class, "W"), Attribute.create(Integer.class, "X"),
				Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "Z")};
		
		Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
		attributeMapping.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
		attributeMapping.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
		attributeMapping.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

		inputs = new Integer[0];
		accessMethod= new ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);
		
		target = AccessTerm.create(relation, accessMethod);
		
		// "Free access" is synonymous with "no input attributes".
		result = target.getInputAttributes();
		Assert.assertTrue(result.length == 0);
		
		/*
		 *  Plan: Access with input at index 0 and no input constants.
		 */
		inputs = new Integer[] {0};
		accessMethod = new ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);
		
		target = AccessTerm.create(relation, accessMethod);
		
		result = target.getInputAttributes();
		Assert.assertTrue(result.length == 1);
		Assert.assertFalse(Arrays.asList(result).contains(Attribute.create(String.class, "c")));
		
		// Repeat with input attributes, rather than indices.
		inputAttributes = new HashSet<>(Arrays.asList(new Attribute[] {Attribute.create(String.class, "W")}));
		accessMethod = new ConcreteAccessMethod(amAttributes, inputAttributes, relation, attributeMapping);

		target = AccessTerm.create(relation, accessMethod);
		
		result = target.getInputAttributes();
		Assert.assertTrue(result.length == 1);
		Assert.assertFalse(Arrays.asList(result).contains(Attribute.create(String.class, "c")));
		
		/*
		 *  Plan: Access with input at index 0 in the access method schema and an input constant for that input.
		 */
		inputs = new Integer[] {0};
		accessMethod = new ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);
		
		inputConstants = new HashMap<>();
		inputConstants.put(1, TypedConstant.create("CONSTANT STRING"));
		
		target = AccessTerm.create(relation, accessMethod);

		result = target.getInputAttributes();
		
		// Here the input is supplied by the inputConstants, so the plan has no (leftover) input attributes.
		Assert.assertFalse(target.getInputAttributes().length == 0);

		// Repeat with input attributes, rather than indices.
		inputAttributes = new HashSet<>(Arrays.asList(new Attribute[] {Attribute.create(String.class, "W")}));
		accessMethod = new ConcreteAccessMethod(amAttributes, inputAttributes, relation, attributeMapping);
		
		inputConstants = new HashMap<>();
		inputConstants.put(1, TypedConstant.create("CONSTANT STRING"));
		
		target = AccessTerm.create(relation, accessMethod);

		result = target.getInputAttributes();
		
		// Here the input is supplied by the inputConstants, so the plan has no (leftover) input attributes.
		Assert.assertFalse(target.getInputAttributes().length == 0);

		/*
		 *  Plan: Access with input at indices 0 & 1 and an input constant on index 0.
		 */
		inputs = new Integer[] {0, 1};
		accessMethod = new ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);

		inputConstants = new HashMap<>();
		inputConstants.put(0, TypedConstant.create("CONSTANT STRING"));
		
		target = AccessTerm.create(relation, accessMethod,inputConstants);
		//AccessTerm.create(relation, accessMethod)
		result = target.getInputAttributes();
		
		// Here the input at index 0 is supplied by the inputConstants but the
		// input at index 1 remains as an input attribute of the plan.
		Assert.assertTrue(result.length == 1);
		Assert.assertFalse(Arrays.asList(result).contains(Attribute.create(Integer.class, "a")));
		
		// Repeat with input attributes, rather than indices.
		inputAttributes = new HashSet<>(Arrays.asList(new Attribute[] {Attribute.create(String.class, "W"), 
				Attribute.create(Integer.class, "X")}));
		accessMethod = new ConcreteAccessMethod(amAttributes, inputAttributes, relation, attributeMapping);

		target = AccessTerm.create(relation, accessMethod);
		result = target.getInputAttributes();
		
		Assert.assertTrue(result.length == 1);
		Assert.assertTrue(Arrays.asList(result).contains(Attribute.create(Integer.class, "a")));
	}

	@Test
	public void testGetOutputAttributes() {

		Relation relation = Mockito.mock(Relation.class);
		Attribute[] relationAttributes = new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
		when(relation.getAttributes()).thenReturn(relationAttributes.clone());

		Attribute[] amAttributes = new Attribute[] {
				Attribute.create(String.class, "W"), Attribute.create(Integer.class, "X"),
				Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "Z")};
		
		Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
		attributeMapping.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
		attributeMapping.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
		attributeMapping.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

		Integer[] inputs = new Integer[0];
		AccessMethodDescriptor accessMethod= new ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);
		
		AccessTerm target = AccessTerm.create(relation, accessMethod);

		Attribute[] result = target.getOutputAttributes();
		
		// The output attributes are the attributes from the underlying relation.
		Assert.assertEquals(3, result.length);
		Assert.assertArrayEquals(relation.getAttributes(), result);
	}

	/*
	 * Tests the AccessTerm class by calling every method.
	 * Other classes are used as required.
	 * INPUTS: None
	 * OUTPUTS: Documented below
	 * PERFORMANCE: On 9/4/2018 this JUnit test returned in 0.5s on Mark's machine
	 */
	
	@Test
	public void testCreation() {
		Attribute[] a = new Attribute[1];
		a[0] = Attribute.create(String.class, "mark");
		Relation r = Relation.create("mark", a);
		AccessMethodDescriptor am = AccessMethodDescriptor.create("am", new Integer[] {0});
		AccessTerm at = AccessTerm.create(r, am);
		
		// Set returned from AccessTerm.getAccesses is empty
		Set<AccessTerm> sat = at.getAccesses();
		Assert.assertTrue(sat.size() == 1);
	
		// AccessMethod returned from AccessTerm.getAccessMethod is invariant
		AccessMethodDescriptor ams = at.getAccessMethod();
		Assert.assertNotNull(ams);

		// array returned from AccessTerm.getChildren has zero length
		RelationalTerm[] pp = at.getChildren();
		Assert.assertTrue(pp.length == 0);

        // Class returned from getClass has name AccessTerm 
		Assert.assertTrue(at.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.AccessTerm");
		
		// RelationalTerm returned from AccessTerm.getInputAttribute is invariant
		Attribute aa = at.getInputAttribute(0);
		Assert.assertNotNull(aa);

		// array returned from AccessTerm.getInputAttributes has zero length
		Attribute[] aaa = at.getInputAttributes();
		Assert.assertTrue(aaa.length == 1);
		
		// AccessTerm.getNumberOfInputAttributes is zero
		int nia = at.getNumberOfInputAttributes();
		Assert.assertTrue(nia == 1);

		// AccessTerm.getNumberOfOutputAttributes is zero
		int noa = at.getNumberOfOutputAttributes();
		Assert.assertTrue(noa == 1);

		// AccessTerm.getOutputAttribute is invariant
		Attribute oa = at.getOutputAttribute(0);
		Assert.assertNotNull(oa);
		
		// AccessTerm.getOutputAttributes has zero length
		Attribute[] oas = at.getOutputAttributes();
		Assert.assertTrue(oas.length == 1);
			
		// AccessTerm.getRelation is invariant
		Relation rr = at.getRelation();
		Assert.assertNotNull(rr);
	}
}
