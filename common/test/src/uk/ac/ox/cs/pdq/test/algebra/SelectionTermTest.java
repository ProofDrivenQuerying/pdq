package uk.ac.ox.cs.pdq.test.algebra;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.Tuple;

public class SelectionTermTest extends PdqTest {

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
		
		protected Stream<Tuple> fetchTuples(Iterator<Tuple> inputTuples) {
			return null;
		}
	}

	// Create a relation, attributes, mapping, access method, 2 constant equality conditions and a selection term,
	@Test
	public void testSelectionTerm() {
		
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
		 * Free access.
		 */
		AccessMethodDescriptor amFree = new ConcreteAccessMethod(amAttributes, new Integer[0], relation, attributeMapping);
		
		Condition condition = ConstantEqualityCondition.create(2, TypedConstant.create("BRAZIL"));
		
		SelectionTerm target = SelectionTerm.create(condition, AccessTerm.create(relation, amFree));
		Assert.assertTrue(target instanceof SelectionTerm);
		
		// Check that construction of the SelectionTerm object fails unless the type of constant
		// in the selection condition matches the type of the corresponding output attribute.
		// In this case, the attribute with index 2 is "c" which has type String.
		condition = ConstantEqualityCondition.create(2, TypedConstant.create(1));
	}

	/*
	 * Tests the SelectionTerm class by calling every method.
	 * Other classes are used as required.
	 * INPUTS: None
	 * OUTPUTS: Documented below
	 * PERFORMANCE: On 11/4/2018 this JUnit test returned in 0.5s on Mark's machine
	 */
	
	@Test
	public void testCreation() {
		
		Relation relation = Relation.create("relation", new Attribute[] {Attribute.create(String.class, "attribute1")});
		RelationalTerm child = AccessTerm.create(relation, AccessMethodDescriptor.create("am", new Integer[] {0}));
	    Condition selection = ConstantEqualityCondition.create(0, TypedConstant.create((String) "hello"));
		// Constructor tests invariant
		SelectionTerm st = SelectionTerm.create(selection, child);
		
		// SelectionTerm.equals null should be false
		boolean b = st.equals(null);
		Assert.assertFalse(b);
		
		// SelectionTerm.getClass has an expected name
		Assert.assertTrue(st.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.SelectionTerm");
				
		// SelectionTerm.hashCode is non negative
		int h = st.hashCode();
		Assert.assertTrue(h >= 0);

		// SelectionTerm.toString is #1=#2&#3=#4
		String s = st.toString();
		Assert.assertTrue(s.equals("Select{[#0=hello]Access{relation.am[#0=attribute1]}}"));
		
		// RelationalTerm returned from SelectionTerm.getAccesses is invariant
		Set<AccessTerm> sat = st.getAccesses();
		Assert.assertNotNull(sat);
		Assert.assertFalse(sat.isEmpty());

		// RelationalTerm returned from SelectionTerm.getChild is invariant
		RelationalTerm p = st.getChild(0);
		Assert.assertNotNull(p);

        // Class returned from getClass has name SelectionTerm 
		Assert.assertTrue(st.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.SelectionTerm");
		
		// RelationalTerm returned from SelectionTerm.getInputAttribute is invariant
		Attribute aa = st.getInputAttribute(0);
		Assert.assertNotNull(aa);

		// array returned from SelectionTerm.getInputAttributes has zero length
		Attribute[] aaa = st.getInputAttributes();
		Assert.assertTrue(aaa.length == 1);
		
		// SelectionTerm.getNumberOfInputAttributes is one
		int nia = st.getNumberOfInputAttributes();
		Assert.assertTrue(nia == 1);

		// SelectionTerm.getNumberOfOutputAttributes is one
		int noa = st.getNumberOfOutputAttributes();
		Assert.assertTrue(noa == 1);

		// SelectionTerm.getOutputAttribute is invariant
		Attribute oa = st.getOutputAttribute(0);
		Assert.assertNotNull(oa);
		//Assert.assertTrue(oa.invariant());
		
		// SelectionTerm.getOutputAttributes has length one
		Attribute[] oas = st.getOutputAttributes();
		Assert.assertTrue(oas.length == 1);
		
		// getrenamings has length two
		Condition c = st.getSelectionCondition();
		Assert.assertNotNull(c);
		
		// isClosed is true
		boolean b2 = st.isClosed();
		Assert.assertFalse(b2);
	}
}

