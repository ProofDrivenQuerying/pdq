// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;
import uk.ac.ox.cs.pdq.util.PdqTest;

import java.util.Set;

public class JoinTermTest extends PdqTest {

	// Dummy concrete class for testing.
	public class DummyJoinTerm extends JoinTerm {

		private static final long serialVersionUID = 1L;

		public DummyJoinTerm(RelationalTerm leftChild, RelationalTerm rightChild) {
			super(leftChild, rightChild);
		}

		public DummyJoinTerm(RelationalTerm leftChild, RelationalTerm rightChild, Condition joinCondition) {		
			super(leftChild, rightChild);
		}
		
		public Condition computeJoinCondition() {
			return super.getJoinConditions();
		}
	}

	// Attributes in the internal schema.
	Attribute[] attributes_nation = new Attribute[] {
			Attribute.create(Integer.class, "nationKey"),
			Attribute.create(String.class, "name"),
			Attribute.create(Integer.class, "regionKey"),
			Attribute.create(String.class, "comment")
	};

	Attribute[] attributes_region = new Attribute[] {
			Attribute.create(Integer.class, "regionKey"),
			Attribute.create(String.class, "name"),
			Attribute.create(String.class, "comment")
	};

	// Attributes in the external schema.
	Attribute[] attributes_N = new Attribute[] {
			Attribute.create(Integer.class, "N_NATIONKEY"),
			Attribute.create(String.class, "N_NAME"),
			Attribute.create(Integer.class, "N_REGIONKEY"),
			Attribute.create(String.class, "N_COMMENT")
	};
	
	Attribute[] attributes_R = new Attribute[] {
			Attribute.create(Integer.class, "R_REGIONKEY"),
			Attribute.create(String.class, "R_NAME"),
			Attribute.create(String.class, "R_COMMENT")
	};
	
	@Test
	public void testJoinTerm() {

		JoinTerm target;

		AccessTerm leftChild; 
		AccessTerm rightChild;
		
		/*
		 * Using the internal schema.
		 */
		leftChild = AccessTerm.create(Relation.create("r",  attributes_nation), AccessMethodDescriptor.create("am", new Integer[] {0}));

		rightChild = AccessTerm.create(Relation.create("r",  attributes_region), AccessMethodDescriptor.create("am", new Integer[] {0}));
	
		// Construct with the default join condition.
		target = JoinTerm.create(leftChild, rightChild);
		Assert.assertTrue(target instanceof JoinTerm);
		
		// Construct with a custom join condition.
		//joinCondition = TypeEqualityCondition.create(2, 4);
		
		target = JoinTerm.create(leftChild, rightChild);
		Assert.assertTrue(target instanceof JoinTerm);
		
		// Test with an invalid custom join condition (types are different).
		//joinCondition = TypeEqualityCondition.create(2, 5);

		boolean caught;
		caught = false; 
		try {
			JoinTerm.create(leftChild, rightChild);
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertFalse(caught);

		/*
		 *  None of the attribute names are equal in the external schema 
		 *  (due to the N_ and R_ prefixes) so attempting to construct
		 *  a JoinTerm fails to result in a valid join condition.
		 */
		caught = false;
		try {
			JoinTerm.create(leftChild, rightChild);
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertFalse(caught);

		/*
		 *  Instead we must provide a custom join condition to join on the 2nd 
		 *  NATION attribute (i.e. N_REGIONKEY) and the 0th REGION attribute 
		 *  (i.e. R_REGIONKEY).
		 */
		//joinCondition = TypeEqualityCondition.create(2, 4);

		target = JoinTerm.create(leftChild, rightChild);
		Assert.assertTrue(target instanceof JoinTerm);
		
	}

	@Test
	public void testComputeJoinCondition() {

		DummyJoinTerm target;
		Condition result;
		RelationalTerm leftChild;
		RelationalTerm rightChild;

		leftChild = AccessTerm.create(Relation.create("r",  attributes_nation), AccessMethodDescriptor.create("am", new Integer[] {0}));

		rightChild = AccessTerm.create(Relation.create("r",  attributes_region), AccessMethodDescriptor.create("am", new Integer[] {0}));

		target = new DummyJoinTerm(leftChild, rightChild);

		result = target.computeJoinCondition();

		// By default, the join condition is that of the "natural join". That is, join 
		// on all attributes with common names.
		Assert.assertTrue(result instanceof ConjunctiveCondition);
		Assert.assertEquals(3, ((ConjunctiveCondition) result).getNumberOfConjuncts());
		
		((ConjunctiveCondition) result).getSimpleConditions();
		
	}
	
	@Test
	public void testGetJoinConditionTuple() {
		

		AccessTerm leftChild; 
		AccessTerm rightChild;

		/*
		 * Using the external schema.
		 */
		leftChild = AccessTerm.create(Relation.create("r",  new Attribute[] { Attribute.create(Integer.class, "c") }), AccessMethodDescriptor.create("am", new Integer[] {0}));

		rightChild = AccessTerm.create(Relation.create("r",  new Attribute[] { Attribute.create(Integer.class, "c") }), AccessMethodDescriptor.create("am", new Integer[] {0}));

		JoinTerm.create(leftChild, rightChild);

		// Construct some tuples to pass as arguments to getJoinCondition().
		TupleType ttNation = TupleType.DefaultFactory.create(Integer.class, String.class, Integer.class, String.class);
		Tuple tupleNation = ttNation.createTuple(5, "ETHIOPIA", 0, "abc");

		TupleType ttRegion = TupleType.DefaultFactory.create(Integer.class, String.class, String.class);
		Tuple tupleRegion1 = ttRegion.createTuple(0, "AFRICA", "xyz");
		Tuple tupleRegion2 = ttRegion.createTuple(1, "AMERICA", "ijk");

		tupleNation.appendTuple(tupleRegion1);
		tupleNation.appendTuple(tupleRegion2);


		JoinTerm.create(leftChild, rightChild);

		// The join condition is _not_ satisfied, since the value at index 0 in tupleNation
		// does not match that at index 0 in tupleRegion.

		// Now repeat with a custom join condition which matches both:
		// - N_REGIONKEY with R_REGIONKEY, and
		// - N_COMMENT with R_COMMENT.

		JoinTerm.create(leftChild, rightChild);

		Tuple tupleRegion3 = ttRegion.createTuple(0, "AFRICA", "abc");
		tupleNation.appendTuple(tupleRegion3);


		// The join condition is satisfied on tuple3 only, since that tuple
		// matches both the REGIONKEY and COMMENT from tupleNation.

	}
	
	@Test
	public void testJoinMap() {


		AccessTerm leftChild; 
		AccessTerm rightChild;

		leftChild = AccessTerm.create(Relation.create("r",  new Attribute[] { Attribute.create(Integer.class, "c") }), AccessMethodDescriptor.create("am", new Integer[] {0}));

		rightChild = AccessTerm.create(Relation.create("r",  new Attribute[] { Attribute.create(Integer.class, "c") }), AccessMethodDescriptor.create("am", new Integer[] {0}));

		// Construct with the default join condition.
		JoinTerm.create(leftChild, rightChild);
			
		JoinTerm.create(leftChild, rightChild);
		
		
		
		

	}
	/*
	 * Tests the JoinTerm class by calling every method.
	 * Other classes are used as required.
	 * INPUTS: None
	 * OUTPUTS: Documented below
	 * PERFORMANCE: On 11/4/2018 this JUnit test returned in 0.5s on Mark's machine
	 */
	
	@Test
	public void testCreation() {
	
		Relation relation1 = Relation.create("relation1", new Attribute[] {
				Attribute.create(String.class, "attribute1")
				});
		Relation relation2 = Relation.create("relation2", new Attribute[] {
				Attribute.create(String.class, "attribute2")});
		RelationalTerm child1 = AccessTerm.create(relation1, AccessMethodDescriptor.create("am", new Integer[] {0}));
		RelationalTerm child2 = AccessTerm.create(relation2, AccessMethodDescriptor.create("am", new Integer[] {0}));
	
		// Constructor tests invariant
		JoinTerm jt = JoinTerm.create(child1, child2);
		
		// JoinTerm.equals null should be false
		boolean b = jt.equals(null);
		Assert.assertFalse(b);
		
		// JoinTerm.getClass has an expected name
		Assert.assertTrue(jt.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.JoinTerm");
				
		// JoinTerm.hashCode is non negative
		int h = jt.hashCode();
		Assert.assertTrue(h >= 0);

		// JoinTerm.toString is #1=#2&#3=#4
		String s = jt.toString();
		Assert.assertTrue(s.equals("Join{[]Access{relation1,am[#0=attribute1]},Access{relation2,am[#0=attribute2]}}"));
		
		// RelationalTerm returned from JoinTerm.getAccesses is invariant
		Set<AccessTerm> sat = jt.getAccesses();
		Assert.assertNotNull(sat);
		Assert.assertFalse(sat.isEmpty());

		// RelationalTerm returned from JoinTerm.getChild is invariant
		RelationalTerm p = jt.getChild(0);
		Assert.assertNotNull(p);

		// array returned from JoinTerm.getChildren has zero length
		RelationalTerm[] pp = jt.getChildren();
		Assert.assertTrue(pp.length == 2);

        // Class returned from getClass has name JoinTerm 
		Assert.assertTrue(jt.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.JoinTerm");
		
		// RelationalTerm returned from JoinTerm.getInputAttribute is invariant
		Attribute aa = jt.getInputAttribute(0);
		Assert.assertNotNull(aa);

		// array returned from JoinTerm.getInputAttributes has zero length
		Attribute[] aaa = jt.getInputAttributes();
		Assert.assertTrue(aaa.length == 2);
		
		// JoinTerm.getJoinCondition is invariant
		Condition c = jt.getJoinConditions();
		Assert.assertNotNull(c);
						
		// JoinTerm.getNumberOfInputAttributes is one
		int nia = jt.getNumberOfInputAttributes();
		Assert.assertTrue(nia == 2);

		// JoinTerm.getNumberOfOutputAttributes is one
		int noa = jt.getNumberOfOutputAttributes();
		Assert.assertTrue(noa == 2);

		// JoinTerm.getOutputAttribute is invariant
		Attribute oa = jt.getOutputAttribute(0);
		Assert.assertNotNull(oa);
		
		// JoinTerm.getOutputAttributes has length three
		Attribute[] oas = jt.getOutputAttributes();
		Assert.assertTrue(oas.length == 2);
		
		// isClosed is true
		boolean b2 = jt.isClosed();
		Assert.assertFalse(b2);
	}
}
