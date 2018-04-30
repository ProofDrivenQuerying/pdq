package uk.ac.ox.cs.pdq.test.runtime.exec.spliterator;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.TypeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantInequalityCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.datasources.AbstractAccessMethod;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.datasources.sql.DatabaseAccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.NestedLoopJoin;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.BinaryExecutablePlan;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.DependentJoin;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

/*
 * TODO: add integration tests with dynamic input.
 * 	- on the left child
 * 	- on the right child
 *  - on both children
 */

public class NestedLoopJoinTest {

	TupleType tt3 = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);

	@Test
	public void testCreate() {

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

		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes, new Integer[0], relation, attributeMapping);

		AccessTerm leftChild = AccessTerm.create(amFree.getRelation(),amFree);
		AccessTerm rightChild = AccessTerm.create(amFree.getRelation(),amFree);

		// Construct the target from a JoinTerm.
		BinaryExecutablePlan target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));
		Assert.assertNotNull(target);
	}

	@Test
	public void testGetJoinCondition() {

		// Relation 1
		Relation relation1 = Mockito.mock(Relation.class);
		Attribute[] relation1Attributes = new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
		when(relation1.getAttributes()).thenReturn(relation1Attributes.clone());

		Attribute[] amAttributes1 = new Attribute[] {
				Attribute.create(String.class, "W"), Attribute.create(Integer.class, "X"),
				Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "Z")};

		Map<Attribute, Attribute> attributeMapping1 = new HashMap<Attribute, Attribute>();
		attributeMapping1.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
		attributeMapping1.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
		attributeMapping1.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

		InMemoryAccessMethod amFree1 = new InMemoryAccessMethod(amAttributes1, new Integer[0], relation1, attributeMapping1);

		// Relation 2
		Relation relation2 = Mockito.mock(Relation.class);
		Attribute[] relation2Attributes = new Attribute[] {Attribute.create(String.class, "c"),
				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")};
		when(relation2.getAttributes()).thenReturn(relation2Attributes.clone());

		Attribute[] amAttributes2 = new Attribute[] {
				Attribute.create(String.class, "C"), Attribute.create(Integer.class, "D"),
				Attribute.create(Integer.class, "E")};

		Map<Attribute, Attribute> attributeMapping2 = new HashMap<Attribute, Attribute>();
		attributeMapping2.put(Attribute.create(String.class, "C"), Attribute.create(String.class, "c"));
		attributeMapping2.put(Attribute.create(Integer.class, "D"), Attribute.create(Integer.class, "d"));
		attributeMapping2.put(Attribute.create(Integer.class, "E"), Attribute.create(Integer.class, "e"));

		InMemoryAccessMethod amFree2 = new InMemoryAccessMethod(amAttributes2, new Integer[0], relation2, attributeMapping2);

		/*
		 *  Test with a simple join condition, arising from the common attribute"c".
		 */
		@SuppressWarnings("resource")
		BinaryExecutablePlan target = new NestedLoopJoin(JoinTerm.create(AccessTerm.create(amFree1.getRelation(),amFree1), 
				AccessTerm.create(amFree2.getRelation(),amFree2)
				));

		Condition result = target.getJoinCondition();

		Assert.assertFalse(result instanceof ConjunctiveCondition);
		Assert.assertTrue(result instanceof TypeEqualityCondition);

		// Check the positions of the attributes in the join condition. Attribute
		// "c" has position 2 in relation1 and position 0 in relation2 (which is
		// position 3 in the concatenation of attributes from relation1 & relation2)
		Assert.assertEquals(2, ((TypeEqualityCondition) result).getPosition());
		Assert.assertEquals(3, ((TypeEqualityCondition) result).getOther());

		/*
		 *  Now test with a conjunctive join condition, arising from the two common 
		 *  attributes "b" and "c".
		 */

		// Relation 3
		Relation relation3 = Mockito.mock(Relation.class);
		Attribute[] relation3Attributes = new Attribute[] {Attribute.create(String.class, "c"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "e")};
		when(relation3.getAttributes()).thenReturn(relation3Attributes.clone());

		Attribute[] amAttributes3 = new Attribute[] {
				Attribute.create(String.class, "C"), Attribute.create(Integer.class, "D"),
				Attribute.create(Integer.class, "E")};

		Map<Attribute, Attribute> attributeMapping3 = new HashMap<Attribute, Attribute>();
		attributeMapping3.put(Attribute.create(String.class, "C"), Attribute.create(String.class, "c"));
		attributeMapping3.put(Attribute.create(Integer.class, "D"), Attribute.create(Integer.class, "b"));
		attributeMapping3.put(Attribute.create(Integer.class, "E"), Attribute.create(Integer.class, "e"));

		InMemoryAccessMethod amFree3 = new InMemoryAccessMethod(amAttributes3, new Integer[0], relation3, attributeMapping3);

		target = new NestedLoopJoin(JoinTerm.create(AccessTerm.create(amFree1.getRelation(),amFree1), 
				AccessTerm.create(amFree3.getRelation(),amFree3)
				));		

		result = target.getJoinCondition();

		Assert.assertTrue(result instanceof ConjunctiveCondition);
		Assert.assertTrue(Arrays.stream(((ConjunctiveCondition) result).getSimpleConditions())
				.allMatch((c) -> c instanceof TypeEqualityCondition));
	}

	/*
	 *  The following are integration tests: NestedLoopJoin plans are constructed & executed. 
	 */

	/*
	 * Plan: Join(Access1, Access2)
	 */
	@Test
	public void integrationTestInMemory1() {

		// Relation 1 (all integer attributes)
		Relation relation1 = Mockito.mock(Relation.class);
		Attribute[] relation1Attributes = new Attribute[] {Attribute.create(Integer.class, "i"),
				Attribute.create(Integer.class, "j"), Attribute.create(Integer.class, "k")};
		when(relation1.getAttributes()).thenReturn(relation1Attributes.clone());

		Attribute[] amAttributes1 = new Attribute[] {
				Attribute.create(Integer.class, "I"), Attribute.create(Integer.class, "J"), 
				Attribute.create(Integer.class, "K")};

		Map<Attribute, Attribute> attributeMapping1 = new HashMap<Attribute, Attribute>();
		attributeMapping1.put(Attribute.create(Integer.class, "I"), Attribute.create(Integer.class, "i"));
		attributeMapping1.put(Attribute.create(Integer.class, "J"), Attribute.create(Integer.class, "j"));
		attributeMapping1.put(Attribute.create(Integer.class, "K"), Attribute.create(Integer.class, "k"));

		InMemoryAccessMethod amFree1 = new InMemoryAccessMethod(amAttributes1, new Integer[0], relation1, attributeMapping1);

		// Relation 2 (all integer attributes)
		Relation relation2 = Mockito.mock(Relation.class);
		Attribute[] relation2Attributes = new Attribute[] {Attribute.create(Integer.class, "k"),
				Attribute.create(Integer.class, "l"), Attribute.create(Integer.class, "m")};
		when(relation2.getAttributes()).thenReturn(relation2Attributes.clone());

		Attribute[] amAttributes2 = new Attribute[] {
				Attribute.create(Integer.class, "K"), Attribute.create(Integer.class, "L"),
				Attribute.create(Integer.class, "M")};

		Map<Attribute, Attribute> attributeMapping2 = new HashMap<Attribute, Attribute>();
		attributeMapping2.put(Attribute.create(Integer.class, "K"), Attribute.create(Integer.class, "k"));
		attributeMapping2.put(Attribute.create(Integer.class, "L"), Attribute.create(Integer.class, "l"));
		attributeMapping2.put(Attribute.create(Integer.class, "M"), Attribute.create(Integer.class, "m"));

		InMemoryAccessMethod amFree2 = new InMemoryAccessMethod(amAttributes2, new Integer[0], relation2, attributeMapping2);

		// Construct the target plan.
		AccessTerm leftChild = AccessTerm.create(amFree1.getRelation(),amFree1);
		AccessTerm rightChild = AccessTerm.create(amFree2.getRelation(),amFree2);
		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));

		// Create some tuples. 
		// Here we join on columns containing no duplicates.  
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 12;
		for (int i = 0; i != N; i++) {
			Integer[] values = new Integer[] {i, i + 1, i + 2};
			tuples1.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		int M = 18;
		for (int i = 0; i != M; i++) {
			Integer[] values = new Integer[] {i + 8, i + 9, i + 10};
			tuples2.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		// Load tuples   
		amFree1.load(tuples1);
		amFree2.load(tuples2);

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// In r1 the "k" column ranges from 2 to 13. In r2 the "k" column ranges from 8 to 25.
		// Note that the result tuples contain the tuple from the right appended onto the tuple from the left.
		TupleType tt6 = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class, 
				Integer.class, Integer.class, Integer.class);
		List<Tuple> expected = new ArrayList<Tuple>();
		expected.add(tt6.createTuple((Object[]) new Integer[] { 6, 7, 8, 8, 9, 10}));
		expected.add(tt6.createTuple((Object[]) new Integer[] { 7, 8, 9, 9, 10, 11}));
		expected.add(tt6.createTuple((Object[]) new Integer[] { 8, 9, 10, 10, 11, 12}));
		expected.add(tt6.createTuple((Object[]) new Integer[] { 9, 10, 11, 11, 12, 13}));
		expected.add(tt6.createTuple((Object[]) new Integer[] { 10, 11, 12, 12, 13, 14}));
		expected.add(tt6.createTuple((Object[]) new Integer[] { 11, 12, 13, 13, 14, 15}));

		// The values in common are 10 to 13.
		Assert.assertEquals(6, result.size());
		Assert.assertEquals(expected, result);
		target.close();

		// Test re-executing the plan (this time using the execute method).
		Assert.assertEquals(6, target.execute().size());
		Assert.assertEquals(6, target.execute().size());
	}

	/*
	 * Plan: Join(Access1, Selection(Access2))
	 */
	@Test
	public void integrationTestInMemory2() {

		// Relation 1 (all integer attributes)
		Relation relation1 = Mockito.mock(Relation.class);
		Attribute[] relation1Attributes = new Attribute[] {Attribute.create(Integer.class, "i"),
				Attribute.create(Integer.class, "j"), Attribute.create(Integer.class, "k")};
		when(relation1.getAttributes()).thenReturn(relation1Attributes.clone());

		Attribute[] amAttributes1 = new Attribute[] {
				Attribute.create(Integer.class, "I"), Attribute.create(Integer.class, "J"), 
				Attribute.create(Integer.class, "K")};

		Map<Attribute, Attribute> attributeMapping1 = new HashMap<Attribute, Attribute>();
		attributeMapping1.put(Attribute.create(Integer.class, "I"), Attribute.create(Integer.class, "i"));
		attributeMapping1.put(Attribute.create(Integer.class, "J"), Attribute.create(Integer.class, "j"));
		attributeMapping1.put(Attribute.create(Integer.class, "K"), Attribute.create(Integer.class, "k"));

		InMemoryAccessMethod amFree1 = new InMemoryAccessMethod(amAttributes1, new Integer[0], relation1, attributeMapping1);

		// Relation 2 (all integer attributes)
		Relation relation2 = Mockito.mock(Relation.class);
		Attribute[] relation2Attributes = new Attribute[] {Attribute.create(Integer.class, "k"),
				Attribute.create(Integer.class, "l"), Attribute.create(Integer.class, "m")};
		when(relation2.getAttributes()).thenReturn(relation2Attributes.clone());

		Attribute[] amAttributes2 = new Attribute[] {
				Attribute.create(Integer.class, "K"), Attribute.create(Integer.class, "L"),
				Attribute.create(Integer.class, "M")};

		Map<Attribute, Attribute> attributeMapping2 = new HashMap<Attribute, Attribute>();
		attributeMapping2.put(Attribute.create(Integer.class, "K"), Attribute.create(Integer.class, "k"));
		attributeMapping2.put(Attribute.create(Integer.class, "L"), Attribute.create(Integer.class, "l"));
		attributeMapping2.put(Attribute.create(Integer.class, "M"), Attribute.create(Integer.class, "m"));

		InMemoryAccessMethod amFree2 = new InMemoryAccessMethod(amAttributes2, new Integer[0], relation2, attributeMapping2);

		// Construct the target plan.

		// Free access on relation 1.
		AccessTerm leftChild = AccessTerm.create(amFree1.getRelation(),amFree1);

		// Free access on relation 2, then select rows where attribute "k" is greater than 10.
		Condition condition = ConstantInequalityCondition.create(0, TypedConstant.create(10), false);
		SelectionTerm rightChild = SelectionTerm.create(condition, AccessTerm.create(amFree2.getRelation(),amFree2)
				);
		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));

		// Create some tuples. 
		// Here we join on columns containing no duplicates.  
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 12;
		for (int i = 0; i != N; i++) {
			Integer[] values = new Integer[] {i, i + 1, i + 2};
			tuples1.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		int M = 18;
		for (int i = 0; i != M; i++) {
			Integer[] values = new Integer[] {i + 8, i + 9, i + 10};
			tuples2.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		// Load tuples   
		amFree1.load(tuples1);
		amFree2.load(tuples2);

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// In r1 the "k" column ranges from 2 to 13. 
		// In r2 the "k" column ranges from 8 to 25 and the selection retains only those that are greater than 10.
		// Note that the result tuples contain the tuple from the right appended onto the tuple from the left.
		// The values in common are 10 to 13.
		TupleType tt6 = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class, 
				Integer.class, Integer.class, Integer.class);
		List<Tuple> expected = new ArrayList<Tuple>();
		expected.add(tt6.createTuple((Object[]) new Integer[] { 9, 10, 11, 11, 12, 13}));
		expected.add(tt6.createTuple((Object[]) new Integer[] { 10, 11, 12, 12, 13, 14}));
		expected.add(tt6.createTuple((Object[]) new Integer[] { 11, 12, 13, 13, 14, 15}));

		Assert.assertEquals(3, result.size());
		Assert.assertEquals(expected, result);
		target.close();
	}

	/*
	 * Plan: Join(Selection(Access1), Access2)
	 */
	@Test
	public void integrationTestInMemory3() {

		// Relation 1 (all integer attributes)
		Relation relation1 = Mockito.mock(Relation.class);
		Attribute[] relation1Attributes = new Attribute[] {Attribute.create(Integer.class, "i"),
				Attribute.create(Integer.class, "j"), Attribute.create(Integer.class, "k")};
		when(relation1.getAttributes()).thenReturn(relation1Attributes.clone());

		Attribute[] amAttributes1 = new Attribute[] {
				Attribute.create(Integer.class, "I"), Attribute.create(Integer.class, "J"), 
				Attribute.create(Integer.class, "K")};

		Map<Attribute, Attribute> attributeMapping1 = new HashMap<Attribute, Attribute>();
		attributeMapping1.put(Attribute.create(Integer.class, "I"), Attribute.create(Integer.class, "i"));
		attributeMapping1.put(Attribute.create(Integer.class, "J"), Attribute.create(Integer.class, "j"));
		attributeMapping1.put(Attribute.create(Integer.class, "K"), Attribute.create(Integer.class, "k"));

		InMemoryAccessMethod amFree1 = new InMemoryAccessMethod(amAttributes1, new Integer[0], relation1, attributeMapping1);

		// Relation 2 (all integer attributes)
		Relation relation2 = Mockito.mock(Relation.class);
		Attribute[] relation2Attributes = new Attribute[] {Attribute.create(Integer.class, "k"),
				Attribute.create(Integer.class, "l"), Attribute.create(Integer.class, "m")};
		when(relation2.getAttributes()).thenReturn(relation2Attributes.clone());

		Attribute[] amAttributes2 = new Attribute[] {
				Attribute.create(Integer.class, "K"), Attribute.create(Integer.class, "L"),
				Attribute.create(Integer.class, "M")};

		Map<Attribute, Attribute> attributeMapping2 = new HashMap<Attribute, Attribute>();
		attributeMapping2.put(Attribute.create(Integer.class, "K"), Attribute.create(Integer.class, "k"));
		attributeMapping2.put(Attribute.create(Integer.class, "L"), Attribute.create(Integer.class, "l"));
		attributeMapping2.put(Attribute.create(Integer.class, "M"), Attribute.create(Integer.class, "m"));

		InMemoryAccessMethod amFree2 = new InMemoryAccessMethod(amAttributes2, new Integer[0], relation2, attributeMapping2);

		// Construct the target plan.

		// Free access on relation 1, then select rows where attribute "j" is less than 8.
		Condition condition = ConstantInequalityCondition.create(1, TypedConstant.create(8));
		SelectionTerm leftChild = SelectionTerm.create(condition, AccessTerm.create(amFree1.getRelation(),amFree1)
				);

		// Free access on relation 2.
		AccessTerm rightChild = AccessTerm.create(amFree2.getRelation(),amFree2);
		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));

		// Create some tuples. 
		// Here we join on columns containing no duplicates.  
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 12;
		for (int i = 0; i != N; i++) {
			Integer[] values = new Integer[] {i, i + 1, i + 2};
			tuples1.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		int M = 18;
		for (int i = 0; i != M; i++) {
			Integer[] values = new Integer[] {i + 8, i + 9, i + 10};
			tuples2.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		// Load tuples   
		amFree1.load(tuples1);
		amFree2.load(tuples2);

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// In r1 the "k" column ranges from 2 to 13 and the selection retains only those that are less than 9. 
		// In r2 the "k" column ranges from 8 to 25.
		// Note that the result tuples contain the tuple from the right appended onto the tuple from the left.
		TupleType tt6 = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class, 
				Integer.class, Integer.class, Integer.class);
		List<Tuple> expected = new ArrayList<Tuple>();
		expected.add(tt6.createTuple((Object[]) new Integer[] { 6, 7, 8, 8, 9, 10}));

		Assert.assertEquals(1, result.size());
		Assert.assertEquals(expected, result);
		target.close();
	}

	/*
	 * Plan: Join(NATION, REGION)
	 */
	@Test
	public void integrationTestSql1() {

		Relation relationNation = Mockito.mock(Relation.class);
		when(relationNation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		Relation relationRegion = Mockito.mock(Relation.class);
		when(relationRegion.getAttributes()).thenReturn(TPCHelper.attrs_region.clone());

		Integer[] inputs = new Integer[0];
		AbstractAccessMethod amFreeNation = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N, inputs, 
				relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());
		AbstractAccessMethod  amFreeRegion = new DatabaseAccessMethod("REGION", TPCHelper.attrs_R, inputs, 
				relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());

		// Construct the target plan.
		AccessTerm leftChild = AccessTerm.create(amFreeNation.getRelation(),amFreeNation);
		AccessTerm rightChild = AccessTerm.create(amFreeRegion.getRelation(),amFreeRegion);

		// Test with and without a custom join condition (to join on attributes with different names).

		// First test without a custom join condition.
		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));

		// Check the inferred join condition. The common attribute "regionKey" is in 
		// position 2 in the nation relation and position 0 in the region relation 
		// (i.e. position 3 in the concatenated attributes).
		Assert.assertTrue(target.getJoinCondition() instanceof TypeEqualityCondition);
		TypeEqualityCondition condition = (TypeEqualityCondition) target.getJoinCondition();
		Assert.assertEquals(2, condition.getPosition());
		Assert.assertEquals(3, condition.getOther());

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// SELECT COUNT(*) FROM NATION, REGION WHERE NATION.N_REGIONKEY=REGION.R_REGIONKEY;
		Assert.assertEquals(25, result.size());

		// Now test with a custom join condition  (to join on attributes with different names).

		// Construct a custom join condition to join on the 0th NATION attribute 
		// (nationKey) and the 0th REGION attribute (regionKey).
		Condition joinCondition = TypeEqualityCondition.create(1, 3);

		target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild, joinCondition));

		// Construct some dummy tuples to test the tuple-dependent getJoinCondition method.
		TupleType ttN = TupleType.DefaultFactory.create(String.class, Integer.class, Integer.class); // name, nationKey, regionKey
		TupleType ttR = TupleType.DefaultFactory.create(Integer.class, String.class, String.class); // regionKey, regionName, comment
		Tuple tupleN = ttN.createTuple("ALGERIA", 0, 0);
		Tuple tupleR1 = ttR.createTuple(0, "AFRICA", "abc");
		Tuple tupleR2 = ttR.createTuple(1, "AMERICA", "ijk");

		// Check that the ConstantEqualityCondition join condition is as expected.
		Condition actualJoinCondition = ((JoinTerm) target.getDecoratedPlan()).getJoinCondition(tupleN);

		// Recall that the actual join condition (which depends on the type-only 
		// join condition _and_ the tuple) is of type ConstantEqualityCondition.
		Assert.assertNotEquals(joinCondition, actualJoinCondition);
		Assert.assertTrue(joinCondition instanceof TypeEqualityCondition);
		Assert.assertTrue(actualJoinCondition instanceof ConstantEqualityCondition);

		Assert.assertTrue(actualJoinCondition.isSatisfied(tupleN.appendTuple(tupleR1)));
		Assert.assertFalse(actualJoinCondition.isSatisfied(tupleN.appendTuple(tupleR2)));

		// Execute the plan. 
		result = target.stream().collect(Collectors.toList());

		// SELECT COUNT(*) FROM NATION, REGION WHERE NATION.N_NATIONKEY=REGION.R_REGIONKEY;
		Assert.assertEquals(5, result.size());
		Assert.assertTrue(result.stream()
				.allMatch(tuple -> tuple.size() == 6));
		target.close();
	}

	/*
	 * Plan: Join(NATION, REGION)
	 */
	@Test
	public void integrationTestSql2() {

		/*
		 * Join with children:
		 * 	- free access on NATION
		 *  - access on REGION with dynamic input on the REGIONKEY attribute.  
		 */

		Relation relationNation = Mockito.mock(Relation.class);
		when(relationNation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		Relation relationRegion = Mockito.mock(Relation.class);
		when(relationRegion.getAttributes()).thenReturn(TPCHelper.attrs_region.clone());

		AbstractAccessMethod  amFreeNation = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N, new Integer[0], 
				relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());

		Set<Attribute> inputAttributes = Sets.newHashSet(Attribute.create(Integer.class, "R_REGIONKEY"));
		AbstractAccessMethod  am0Region = new DatabaseAccessMethod("REGION", TPCHelper.attrs_R, inputAttributes, 
				relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());

		// Construct the target plan.
		AccessTerm leftChild = AccessTerm.create(amFreeNation.getRelation(),amFreeNation);
		AccessTerm rightChild = AccessTerm.create(am0Region.getRelation(),am0Region);

		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));

		// Attempting to execute the plan before setting the dynamic input raises an exception.
		boolean caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Create some dynamic input.
		TupleType ttInteger = TupleType.DefaultFactory.create(Integer.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(ttInteger.createTuple(0));
		dynamicInput.add(ttInteger.createTuple(3));
		dynamicInput.add(ttInteger.createTuple(1));

		// Set the dynamic input.
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// SELECT COUNT(*) FROM NATION, REGION WHERE NATION.N_REGIONKEY=REGION.R_REGIONKEY AND REGION.R_REGIONKEY IN (0, 1, 3);
		Assert.assertEquals(15, result.size());
		Assert.assertTrue(result.stream()
				.allMatch(tuple -> tuple.size() == 6));

		Set<Integer> regionKeys = result.stream()
				.map(tuple -> (Integer) tuple.getValue(2)).collect(Collectors.toSet());
		Assert.assertEquals(Sets.newHashSet(0, 1, 3), regionKeys);
		target.close();
	}

	/*
	 * Plan: Join(NATION, REGION)
	 */
	@Test
	public void integrationTestSql3() {

		/*
		 * Join with children:
		 *  - access on NATION with dynamic input on the NATIONKEY attribute.  
		 * 	- free access on REGION
		 */

		Relation relationNation = Mockito.mock(Relation.class);
		when(relationNation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		Relation relationRegion = Mockito.mock(Relation.class);
		when(relationRegion.getAttributes()).thenReturn(TPCHelper.attrs_region.clone());

		Set<Attribute> inputAttributes = Sets.newHashSet(Attribute.create(Integer.class, "N_NATIONKEY"));
		AbstractAccessMethod  am0Nation = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N, inputAttributes, 
				relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());

		AbstractAccessMethod  amFreeRegion = new DatabaseAccessMethod("REGION", TPCHelper.attrs_R, new Integer[0], 
				relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());

		// Construct the target plan.
		AccessTerm leftChild = AccessTerm.create(am0Nation.getRelation(),am0Nation);
		AccessTerm rightChild = AccessTerm.create(amFreeRegion.getRelation(),amFreeRegion);

		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));

		// Attempting to execute the plan before setting the dynamic input raises an exception.
		boolean caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Create some dynamic input.
		TupleType ttInteger = TupleType.DefaultFactory.create(Integer.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(ttInteger.createTuple(20));
		dynamicInput.add(ttInteger.createTuple(14));
		dynamicInput.add(ttInteger.createTuple(9));
		dynamicInput.add(ttInteger.createTuple(24));
		dynamicInput.add(ttInteger.createTuple(0));

		// Set the dynamic input.
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// SELECT COUNT(*) FROM NATION, REGION WHERE NATION.N_REGIONKEY=REGION.R_REGIONKEY AND NATION.N_NATIONKEY IN (0, 9, 14, 20, 24);
		Assert.assertEquals(5, result.size());
		Assert.assertTrue(result.stream()
				.allMatch(tuple -> tuple.size() == 6));

		Set<Integer> nationKeys = result.stream()
				.map(tuple -> (Integer) tuple.getValue(1)).collect(Collectors.toSet());
		Assert.assertEquals(Sets.newHashSet(0, 9, 14, 20, 24), nationKeys);
		target.close();
	}

	/*
	 * Plan: Join(NATION, REGION)
	 */
	@Test
	public void integrationTestSql4() {

		/*
		 * Join with children:
		 *  - access on NATION with dynamic input on the NATIONKEY attribute.  
		 *  - access on REGION with dynamic input on the REGIONKEY attribute.  
		 */

		Relation relationNation = Mockito.mock(Relation.class);
		when(relationNation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		Relation relationRegion = Mockito.mock(Relation.class);
		when(relationRegion.getAttributes()).thenReturn(TPCHelper.attrs_region.clone());

		Set<Attribute> inputAttributes;

		inputAttributes = Sets.newHashSet(Attribute.create(Integer.class, "N_NATIONKEY"));
		AbstractAccessMethod  am0Nation = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N, inputAttributes, 
				relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());

		inputAttributes = Sets.newHashSet(Attribute.create(Integer.class, "R_REGIONKEY"));
		AbstractAccessMethod  am0Region = new DatabaseAccessMethod("REGION", TPCHelper.attrs_R, inputAttributes, 
				relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());

		// Construct the target plan.
		AccessTerm leftChild = AccessTerm.create(am0Nation.getRelation(),am0Nation);
		AccessTerm rightChild = AccessTerm.create(am0Region.getRelation(),am0Region);

		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));

		// Attempting to execute the plan before setting the dynamic input raises an exception.
		boolean caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Create some dynamic input.
		TupleType ttIntegerInteger = TupleType.DefaultFactory.create(Integer.class, Integer.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(ttIntegerInteger.createTuple(20, 4));
		dynamicInput.add(ttIntegerInteger.createTuple(15, 0));
		dynamicInput.add(ttIntegerInteger.createTuple(3, 4)); // NOTE: nation 3 is *not* in region 4
		dynamicInput.add(ttIntegerInteger.createTuple(18, 2));
		dynamicInput.add(ttIntegerInteger.createTuple(21, 0)); // NOTE: nation 21 is *not* in region 0

		// Set the dynamic input.
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// Note that the expected number of tuples returned is 4, not 3 or 5.
		// Nation 3 is *not* included in the results because its REGIONKEY is 1, which is 
		// not included in any of the dynamic input tuples.
		// Nation 21 *is* included in the results because its REGIONKEY is 2, which is
		// included in the dynamic input tuples (just not in the same tuple as nation 21).

		// CREATE VIEW N AS SELECT * FROM NATION WHERE N_NATIONKEY IN (3,15,18,20,21);
		// CREATE VIEW R AS SELECT * FROM REGION WHERE R_REGIONKEY IN (0,2,4);
		// SELECT COUNT(*) FROM N, R WHERE N.N_REGIONKEY=R.R_REGIONKEY;
		Assert.assertEquals(4, result.size());
		Assert.assertTrue(result.stream()
				.allMatch(tuple -> tuple.size() == 6));

		Set<Integer> nationKeys = result.stream()
				.map(tuple -> (Integer) tuple.getValue(1)).collect(Collectors.toSet());
		Assert.assertEquals(Sets.newHashSet(15, 18, 20, 21), nationKeys);

		Set<Integer> regionKeys = result.stream()
				.map(tuple -> (Integer) tuple.getValue(3)).collect(Collectors.toSet());
		Assert.assertEquals(Sets.newHashSet(0, 2, 4), regionKeys);
		target.close();
	}

	/*
	 * Plan: Join(NATION, Selection(REGION))
	 */
	@Test
	public void integrationTestSql5() {

		Relation relationNation = Mockito.mock(Relation.class);
		when(relationNation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		Relation relationRegion = Mockito.mock(Relation.class);
		when(relationRegion.getAttributes()).thenReturn(TPCHelper.attrs_region.clone());

		AbstractAccessMethod  amFreeNation = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N, new Integer[0], 
				relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());

		AbstractAccessMethod  amFreeRegion = new DatabaseAccessMethod("REGION", TPCHelper.attrs_R, new Integer[0], 
				relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());

		// Construct the target plan.

		// Free access on NATION.
		AccessTerm leftChild = AccessTerm.create(amFreeNation.getRelation(),amFreeNation);

		// Free access on REGION, then select rows where regionKey < 4.
		Condition condition = ConstantInequalityCondition.create(0, TypedConstant.create(4));
		SelectionTerm rightChild = SelectionTerm.create(condition, AccessTerm.create(amFreeRegion.getRelation(),amFreeRegion)
				);

		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// SELECT COUNT(*) FROM NATION, REGION WHERE NATION.N_REGIONKEY=REGION.R_REGIONKEY AND REGION.R_REGIONKEY < 4;
		Assert.assertEquals(20, result.size());
		Assert.assertTrue(result.stream().allMatch(tuple -> tuple.size() == 6));

		Set<Integer> regionKeys = result.stream()
				.map(tuple -> (Integer) tuple.getValue(2)).collect(Collectors.toSet());
		Assert.assertEquals(Sets.newHashSet(0, 1, 2, 3), regionKeys);
		target.close();
	}

	/*
	 * Plan: Join(Selection(NATION), REGION)
	 */
	@Test
	public void integrationTestSql6() {

		Relation relationNation = Mockito.mock(Relation.class);
		when(relationNation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		Relation relationRegion = Mockito.mock(Relation.class);
		when(relationRegion.getAttributes()).thenReturn(TPCHelper.attrs_region.clone());

		AbstractAccessMethod  amFreeNation = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N, new Integer[0], 
				relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());

		AbstractAccessMethod  amFreeRegion = new DatabaseAccessMethod("REGION", TPCHelper.attrs_R, new Integer[0], 
				relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());

		// Construct the target plan.

		// Free access on NATION, then select rows where nationKey > 8.
		Condition condition = ConstantInequalityCondition.create(1, TypedConstant.create(8), false);
		SelectionTerm leftChild = SelectionTerm.create(condition, AccessTerm.create(amFreeNation.getRelation(),amFreeNation)
				);

		// Free access on REGION.
		AccessTerm rightChild = AccessTerm.create(amFreeRegion.getRelation(),amFreeRegion);

		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// SELECT COUNT(*) FROM NATION, REGION WHERE NATION.N_REGIONKEY=REGION.R_REGIONKEY AND NATION.N_NATIONKEY > 8;
		Assert.assertEquals(16, result.size());
		Assert.assertTrue(result.stream().allMatch(tuple -> tuple.size() == 6));

		Assert.assertTrue(result.stream().allMatch(tuple -> (Integer) tuple.getValue(1) > 8));
		target.close();
	}

//	/*
//	 * Plan: Join(DependentJoin(NATION, REGION), Join(Selection(CUSTOMER), SUPPLIER))
//	 */
//	@Test
//	public void integrationTestSql7() {
//
//		// Construct the target plan.
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
//				AccessTerm.create(TPCHelper.am0Region).getRelation(),TPCHelper.am0Region));
//
//		Condition condition = ConstantEqualityCondition.create(4, TypedConstant.create(5));
//		NestedLoopJoin rightChild = new NestedLoopJoin(JoinTerm.create(
//				SelectionTerm.create(condition, AccessTerm.create(TPCHelper.amFreeCustomer).getRelation(),TPCHelper.amFreeCustomer)), 
//				AccessTerm.create(TPCHelper.amFreeSupplier)).getRelation(),TPCHelper.amFreeSupplier)));
//
//		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, REGION WHERE NATION.N_REGIONKEY = REGION.R_REGIONKEY), right_child AS (SELECT * FROM CUSTOMER, SUPPLIER WHERE CUSTOMER.C_NATIONKEY = 5 AND CUSTOMER.C_NATIONKEY = SUPPLIER.S_NATIONKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
//		Assert.assertEquals(2261760, result.size());
//		Assert.assertTrue(result.stream().allMatch(tuple -> tuple.size() == 16));
//
//		Assert.assertTrue(result.stream().allMatch(tuple -> (Integer) tuple.getValue(1) == 5));
//		target.close();
//	}

	/*
	 * Stress tests
	 */

	// See issue #209.

	Attribute[] relationR1Attributes = new Attribute[] {Attribute.create(String.class, "a"),
			Attribute.create(String.class, "b")};
	Relation relationR1 = Relation.create("R1", relationR1Attributes);

	Attribute[] relationR2Attributes = new Attribute[] {Attribute.create(String.class, "a"),
			Attribute.create(String.class, "c")};
	Relation relationR2 = Relation.create("R2", relationR2Attributes);

	Attribute[] relationR3Attributes = new Attribute[] {Attribute.create(String.class, "b"),
			Attribute.create(String.class, "d")};
	Relation relationR3 = Relation.create("R3", relationR3Attributes);

	Attribute[] relationR4Attributes = new Attribute[] {Attribute.create(String.class, "d"),
			Attribute.create(String.class, "e")};
	Relation relationR4 = Relation.create("R4", relationR4Attributes);

	Map<Attribute, Attribute> attributeMapping1 = ImmutableMap.of(
			Attribute.create(String.class, "a"), Attribute.create(String.class, "a"),
			Attribute.create(String.class, "b"), Attribute.create(String.class, "b"));

	Map<Attribute, Attribute> attributeMapping2 = ImmutableMap.of(
			Attribute.create(String.class, "a"), Attribute.create(String.class, "a"),
			Attribute.create(String.class, "c"), Attribute.create(String.class, "c"));

	Map<Attribute, Attribute> attributeMapping3 = ImmutableMap.of(
			Attribute.create(String.class, "b"), Attribute.create(String.class, "b"),
			Attribute.create(String.class, "d"), Attribute.create(String.class, "d"));

	Map<Attribute, Attribute> attributeMapping4 = ImmutableMap.of(
			Attribute.create(String.class, "d"), Attribute.create(String.class, "d"),
			Attribute.create(String.class, "e"), Attribute.create(String.class, "e"));

	TupleType ttStringString = TupleType.DefaultFactory.create(String.class, String.class);

	/*
	 * Join[b](dependentJoinR1R2, dependentJoinR3R4).
	 */
	@Test
	public void stressTestInMemory1() {

		// Scale parameter (number of tuples in each relation):
		int N = 1000;

		InMemoryAccessMethod am1Free = new InMemoryAccessMethod(relationR1Attributes, 
				new Integer[0], relationR1, attributeMapping1);

		Set<Attribute> inputAttributes2 = Sets.newHashSet(Attribute.create(String.class, "a"));		
		InMemoryAccessMethod am20 = new InMemoryAccessMethod(relationR2Attributes, 
				inputAttributes2, relationR2, attributeMapping2);

		InMemoryAccessMethod am3Free = new InMemoryAccessMethod(relationR3Attributes, 
				new Integer[0], relationR3, attributeMapping3);

		Set<Attribute> inputAttributes4 = Sets.newHashSet(Attribute.create(String.class, "d"));		
		InMemoryAccessMethod am40 = new InMemoryAccessMethod(relationR4Attributes, 
				inputAttributes4, relationR4, attributeMapping4);

		/*
		 * DependentJoin{a}(R1, R2).
		 */
		DependentJoin dependentJoinR1R2 = new DependentJoin(DependentJoinTerm.create(
				AccessTerm.create(am1Free.getRelation(),am1Free), 
				AccessTerm.create(am20.getRelation(),am20)
				));

		/*
		 * DependentJoin{d}(R3, R4).
		 */
		DependentJoin dependentJoinR3R4 = new DependentJoin(DependentJoinTerm.create(
				AccessTerm.create(am3Free.getRelation(),am3Free), 
				AccessTerm.create(am40.getRelation(),am40)
				));

		// Create some tuples. 
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			String[] values = new String[] {"a" + i, "b" + i};
			tuples1.add(ttStringString.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			String[] values = new String[] {"a" + i, "c" + i};
			tuples2.add(ttStringString.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuples3 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			String[] values = new String[] {"b" + i, "d" + i};
			tuples3.add(ttStringString.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuples4 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			String[] values = new String[] {"d" + i, "e" + i};
			tuples4.add(ttStringString.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples   
		am1Free.load(tuples1);
		am20.load(tuples2);
		am3Free.load(tuples3);
		am40.load(tuples4);

		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(
				dependentJoinR1R2, dependentJoinR3R4));

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Assert.assertEquals(N, result.size());
		target.close();
	}

	/*
	 * Plan: Join(NATION, SUPPLIER)
	 */
	@Test
	public void stressTestSql1() {

		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
				AccessTerm.create(TPCHelper.amFreeSupplier.getRelation(),TPCHelper.amFreeSupplier)
				));

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Tuple tuple = result.get(0);
		Assert.assertEquals(tuple.size(), TPCHelper.attrs_nation.length + TPCHelper.attrs_supplier.length);

		// select count(*) from NATION, SUPPLIER where NATION.N_NATIONKEY=SUPPLIER.S_NATIONKEY;	
		Assert.assertEquals(10000, result.size());
		target.close();
	}

	/*
	 * Plan: Join(NATION_LESS, SUPPLIER_LESS)
	 */
	@Test
	public void stressTestSql1a() {

		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(),TPCHelper.amFreeNation_less), 
				AccessTerm.create(TPCHelper.amFreeSupplier_less.getRelation(),TPCHelper.amFreeSupplier_less)
				));

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Tuple tuple = result.get(0);
		Assert.assertEquals(tuple.size(), TPCHelper.attrs_nation_less.length + TPCHelper.attrs_supplier_less.length);

		// select count(*) from NATION, SUPPLIER where NATION.N_NATIONKEY=SUPPLIER.S_NATIONKEY;	
		Assert.assertEquals(10000, result.size());
		target.close();
	}

//	/*
//	 * Plan: Join(PART, PARTSUPP)
//	 */
//	@Test
//	public void stressTestSql2() {
//
//		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreePart.getRelation(),TPCHelper.amFreePart), 
//				AccessTerm.create(TPCHelper.amFreePartSupp)).getRelation(),TPCHelper.amFreePartSupp)));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		Tuple tuple = result.get(0);
//		Assert.assertEquals(tuple.size(), TPCHelper.attrs_part.length + TPCHelper.attrs_partSupp.length);
//
//		// select count(*) from PART, PARTSUPP where PART.P_PARTKEY=PARTSUPP.PS_PARTKEY;	
//		Assert.assertEquals(800000, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: Join(PART_LESS, PARTSUPP_LESS)
//	 */
//	@Test
//	public void stressTestSql2a() {
//
//		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreePart_less.getRelation(),TPCHelper.amFreePart_less), 
//				AccessTerm.create(TPCHelper.amFreePartSupp_less)).getRelation(),TPCHelper.amFreePartSupp_less)));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		Tuple tuple = result.get(0);
//		Assert.assertEquals(tuple.size(), TPCHelper.attrs_part_less.length + TPCHelper.attrs_partSupp_less.length);
//
//		// select count(*) from PART, PARTSUPP where PART.P_PARTKEY=PARTSUPP.PS_PARTKEY;	
//		Assert.assertEquals(800000, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: Join(Join(NATION, SUPPLIER), Join(PART, PARTSUPP))
//	 */
//	@Test
//	public void stressTestSql3() {
//
//		// Specify the nested loop join algorithm throughout. 
//		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(
//				new NestedLoopJoin(JoinTerm.create(
//						AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
//						AccessTerm.create(TPCHelper.amFreeSupplier)).getRelation(),TPCHelper.amFreeSupplier))),
//				new NestedLoopJoin(JoinTerm.create(
//						AccessTerm.create(TPCHelper.amFreePart.getRelation(),TPCHelper.amFreePart), 
//						AccessTerm.create(TPCHelper.amFreePartSupp)).getRelation(),TPCHelper.amFreePartSupp)))
//				));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		Tuple tuple = result.get(0);
//		int expected = TPCHelper.attrs_nation.length + TPCHelper.attrs_supplier.length + TPCHelper.attrs_part.length + TPCHelper.attrs_partSupp.length;
//		Assert.assertEquals(tuple.size(), expected);
//		Assert.assertEquals((Integer) tuple.getValue(1), tuple.getValue(5)); // nationKey
//		Assert.assertEquals((Integer) tuple.getValue(3), tuple.getValue(14)); // suppKey
//		Assert.assertEquals((Integer) tuple.getValue(8), tuple.getValue(13)); // partKey
//
//		// WITH left_child as (select * from NATION, SUPPLIER where NATION.N_NATIONKEY=SUPPLIER.S_NATIONKEY), right_child as (select * from PART, PARTSUPP where PART.P_PARTKEY=PARTSUPP.PS_PARTKEY) select count(*) from left_child, right_child where left_child.S_SUPPKEY=right_child.PS_SUPPKEY;
//		Assert.assertEquals(800000, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: Join(Join(NATION_LESS, SUPPLIER_LESS), Join(PART_LESS, PARTSUPP_LESS))
//	 */
//	@Test
//	public void stressTestSql3a() {
//
//		// Specify the nested loop join algorithm throughout. 
//		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(
//				new NestedLoopJoin(JoinTerm.create(
//						AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(),TPCHelper.amFreeNation_less), 
//						AccessTerm.create(TPCHelper.amFreeSupplier_less)).getRelation(),TPCHelper.amFreeSupplier_less))),
//				new NestedLoopJoin(JoinTerm.create(
//						AccessTerm.create(TPCHelper.amFreePart_less.getRelation(),TPCHelper.amFreePart_less), 
//						AccessTerm.create(TPCHelper.amFreePartSupp_less)).getRelation(),TPCHelper.amFreePartSupp_less)))
//				));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		Tuple tuple = result.get(0);
//		int expected = TPCHelper.attrs_nation_less.length + TPCHelper.attrs_supplier_less.length + TPCHelper.attrs_part_less.length + TPCHelper.attrs_partSupp_less.length;
//		Assert.assertEquals(tuple.size(), expected);
//		Assert.assertEquals((Integer) tuple.getValue(1), tuple.getValue(5)); // nationKey
//		Assert.assertEquals((Integer) tuple.getValue(3), tuple.getValue(9)); // suppKey
//		Assert.assertEquals((Integer) tuple.getValue(6), tuple.getValue(8)); // partKey
//
//		// WITH left_child as (select * from NATION, SUPPLIER where NATION.N_NATIONKEY=SUPPLIER.S_NATIONKEY), right_child as (select * from PART, PARTSUPP where PART.P_PARTKEY=PARTSUPP.PS_PARTKEY) select count(*) from left_child, right_child where left_child.S_SUPPKEY=right_child.PS_SUPPKEY;
//		Assert.assertEquals(800000, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: Join(DependentJoin(Selection(NATION), SUPPLIER),  DependentJoin(CUSTOMER, Selection(ORDERS))
//	 */
//	@Test
//	public void stressTestSql4() {
//
//		// Select nations whose nation key is greater than 8.
//		Condition nationCondition = ConstantInequalityCondition.create(1, TypedConstant.create(8), false);
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				SelectionTerm.create(nationCondition, AccessTerm.create(TPCHelper.amFreeNation).getRelation(),TPCHelper.amFreeNation)), 
//				AccessTerm.create(TPCHelper.am3Supplier).getRelation(),TPCHelper.am3Supplier));
//
//		// Select orders whose total price is greater than 200000.
//		Condition totalPriceCondition = ConstantInequalityCondition.create(2, TypedConstant.create(200000f), false);
//		DependentJoinTerm rightChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeCustomer.getRelation(),TPCHelper.amFreeCustomer), 
//				SelectionTerm.create(totalPriceCondition, AccessTerm.create(TPCHelper.am1Orders)).getRelation(),TPCHelper.am1Orders)));
//
//		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY > 8 AND NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE ORDERS.O_TOTALPRICE > 200000 AND CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;	
//		Assert.assertEquals(114443829, result.size());
//		target.close();
//	}	
//
//	/*
//	 * Plan: Join(DependentJoin(Selection(NATION_LESS), SUPPLIER_LESS),  DependentJoin(CUSTOMER_LESS, Selection(ORDERS_LESS))
//	 */
//	@Test
//	public void stressTestSql4a() {
//
//		// Select nations whose nation key is greater than 8.
//		Condition nationCondition = ConstantInequalityCondition.create(1, TypedConstant.create(8), false);
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				SelectionTerm.create(nationCondition, AccessTerm.create(TPCHelper.amFreeNation_less).getRelation(),TPCHelper.amFreeNation_less)), 
//				AccessTerm.create(TPCHelper.am3Supplier_less).getRelation(),TPCHelper.am3Supplier_less));
//
//		// Select orders whose total price is greater than 200000.
//		Condition totalPriceCondition = ConstantInequalityCondition.create(2, TypedConstant.create(200000f), false);
//		DependentJoinTerm rightChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeCustomer_less.getRelation(),TPCHelper.amFreeCustomer_less), 
//				SelectionTerm.create(totalPriceCondition, AccessTerm.create(TPCHelper.am1Orders_less)).getRelation(),TPCHelper.am1Orders_less)));
//
//		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY > 8 AND NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE ORDERS.O_TOTALPRICE > 200000 AND CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;	
//		Assert.assertEquals(114443829, result.size());
//		target.close();
//	}	
//
//	/*
//	 * Plan: Join(DependentJoin(Selection(NATION), SUPPLIER),  Selection(DependentJoin(CUSTOMER, Selection(ORDERS)))
//	 */
//	@Test
//	public void stressTestSql5() {
//
//		// Select nations whose nation key is greater than 8.
//		Condition nationCondition = ConstantInequalityCondition.create(1, TypedConstant.create(8), false);
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				SelectionTerm.create(nationCondition, AccessTerm.create(TPCHelper.amFreeNation).getRelation(),TPCHelper.amFreeNation)), 
//				AccessTerm.create(TPCHelper.am3Supplier).getRelation(),TPCHelper.am3Supplier));
//
//		// Select orders whose total price is greater than 200000.
//		Condition totalPriceCondition = ConstantInequalityCondition.create(2, TypedConstant.create(200000f), false);
//		// Select orders from customers with a negative account balance.
//		Condition acctBalCondition = ConstantInequalityCondition.create(2, TypedConstant.create(0f));
//		SelectionTerm rightChild = SelectionTerm.create(acctBalCondition, DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeCustomer.getRelation(),TPCHelper.amFreeCustomer), 
//				SelectionTerm.create(totalPriceCondition, AccessTerm.create(TPCHelper.am1Orders))).getRelation(),TPCHelper.am1Orders))));
//
//		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY > 8 AND NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE ORDERS.O_TOTALPRICE > 200000 AND CUSTOMER.C_ACCTBAL < 0 AND CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;	
//		Assert.assertEquals(10325518, result.size());
//		target.close();
//	}	
//	
//	/*
//	 * Plan: Join(DependentJoin(Selection(NATION_LESS), SUPPLIER_LESS),  Selection(DependentJoin(CUSTOMER_LESS, Selection(ORDERS_LESS)))
//	 */
//	@Test
//	public void stressTestSql5a() {
//
//		// Select nations whose nation key is greater than 8.
//		Condition nationCondition = ConstantInequalityCondition.create(1, TypedConstant.create(8), false);
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				SelectionTerm.create(nationCondition, AccessTerm.create(TPCHelper.amFreeNation_less).getRelation(),TPCHelper.amFreeNation_less)), 
//				AccessTerm.create(TPCHelper.am3Supplier_less).getRelation(),TPCHelper.am3Supplier_less));
//
//		// Select orders whose total price is greater than 200000.
//		Condition totalPriceCondition = ConstantInequalityCondition.create(2, TypedConstant.create(200000f), false);
//		// Select orders from customers with a negative account balance.
//		Condition acctBalCondition = ConstantInequalityCondition.create(2, TypedConstant.create(0f));
//		SelectionTerm rightChild = SelectionTerm.create(acctBalCondition, DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeCustomer_less.getRelation(),TPCHelper.amFreeCustomer_less), 
//				SelectionTerm.create(totalPriceCondition, AccessTerm.create(TPCHelper.am1Orders_less))).getRelation(),TPCHelper.am1Orders_less))));
//
//		NestedLoopJoin target = new NestedLoopJoin(JoinTerm.create(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY > 8 AND NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE ORDERS.O_TOTALPRICE > 200000 AND CUSTOMER.C_ACCTBAL < 0 AND CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;	
//		Assert.assertEquals(10325518, result.size());
//		target.close();
//	}	
}
