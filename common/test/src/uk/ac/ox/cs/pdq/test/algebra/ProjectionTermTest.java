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
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.Tuple;

public class ProjectionTermTest extends PdqTest {

	// Dummy concrete class for testing.
	public class ConcreteAccessMethod  extends AccessMethod {
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
	
	@Test
	public void testProjectionTerm() {

		ProjectionTerm target;
		AccessMethod accessMethod;
		Integer[] inputs;
		
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
		inputs = new Integer[0];
		accessMethod= new ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);

		AccessTerm accessTerm = AccessTerm.create(relation, accessMethod);

		/*
		 * Plan: projection onto attribute "a".
		 */
		target = ProjectionTerm.create(new Attribute[]{Attribute.create(Integer.class, "a")}, accessTerm);
		Assert.assertNotNull(target);
		
		// Test illegal constructions: projection attributes must be found in the relation attributes.
		boolean caught = false;
		try {
			ProjectionTerm.create(new Attribute[]{Attribute.create(Integer.class, "A")}, accessTerm);
		} catch (IllegalArgumentException e) {
			caught = true;
		} catch (AssertionError e) {
			caught = true;
		}
		Assert.assertTrue(caught);
		
		caught = false;
		try {
			ProjectionTerm.create(new Attribute[]{Attribute.create(String.class, "a")}, accessTerm);
		} catch (IllegalArgumentException e) {
			caught = true;
		} catch (AssertionError e) {
			caught = true;
		}
		Assert.assertTrue(caught);
	}
	
	/*
	 * Tests the ProjectionTerm class by calling every method.
	 * Other classes are used as required.
	 * INPUTS: None
	 * OUTPUTS: Documented below
	 * PERFORMANCE: On 11/4/2018 this JUnit test returned in 0.5s on Mark's machine
	 */
	
	@Test
	public void testCreation() {
	
		Attribute[] projections = new Attribute[] {
				Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2")
				};
		Relation relation = Relation.create("relation", projections);
		RelationalTerm child = AccessTerm.create(relation, AccessMethod.create("am", new Integer[] {0}));
	
		// Constructor tests invariant
		ProjectionTerm pt = ProjectionTerm.create(projections, child);
		
		// ProjectionTerm.equals null should be false
		boolean b = pt.equals(null);
		Assert.assertFalse(b);
		
		// ProjectionTerm.getClass has an expected name
		Assert.assertTrue(pt.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.ProjectionTerm");
				
		// ProjectionTerm.hashCode is non negative
		int h = pt.hashCode();
		Assert.assertTrue(h >= 0);

		// ProjectionTerm.toString is #1=#2&#3=#4
		String s = pt.toString();
		Assert.assertTrue(s.equals("Project{[attribute1,attribute2]Access{relation.am[#0=attribute1]}}"));
		
		// RelationalTerm returned from ProjectionTerm.getAccesses is invariant
		Set<AccessTerm> sat = pt.getAccesses();
		Assert.assertNotNull(sat);
		Assert.assertFalse(sat.isEmpty());

		// RelationalTerm returned from ProjectionTerm.getChild is invariant
		RelationalTerm p = pt.getChild(0);
		Assert.assertNotNull(p);

		// array returned from ProjectionTerm.getChildren has zero length
		RelationalTerm[] pp = pt.getChildren();
		Assert.assertTrue(pp.length == 1);

        // Class returned from getClass has name ProjectionTerm 
		Assert.assertTrue(pt.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.ProjectionTerm");
		
		// RelationalTerm returned from ProjectionTerm.getInputAttribute is invariant
		Attribute aa = pt.getInputAttribute(0);
		Assert.assertNotNull(aa);

		// array returned from ProjectionTerm.getInputAttributes has zero length
		Attribute[] aaa = pt.getInputAttributes();
		Assert.assertTrue(aaa.length == 1);
		
		// ProjectionTerm.getNumberOfInputAttributes is one
		int nia = pt.getNumberOfInputAttributes();
		Assert.assertTrue(nia == 1);

		// ProjectionTerm.getNumberOfOutputAttributes is two
		int noa = pt.getNumberOfOutputAttributes();
		Assert.assertTrue(noa == 2);

		// ProjectionTerm.getOutputAttribute is invariant
		Attribute oa = pt.getOutputAttribute(0);
		Assert.assertNotNull(oa);
		//Assert.assertTrue(oa.invariant());
		
		// ProjectionTerm.getOutputAttributes has length two
		Attribute[] oas = pt.getOutputAttributes();
		Assert.assertTrue(oas.length == 2);
		
		// getProjections has length two
		Attribute[] p2 = pt.getProjections();
		Assert.assertTrue(p2.length == 2);
		
		// isClosed is true
		boolean b2 = pt.isClosed();
		Assert.assertFalse(b2);
	}
}

