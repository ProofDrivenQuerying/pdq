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
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantInequalityCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.algebra.TypeEqualityCondition;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Access;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.DependentJoinUnbatched;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

public class DependentJoinUnbatchedTest {

	/*
	 * MOST IMP TODO: add integration tests with *external* dynamic input.
	 * 	- on the left child
	 * 	- on the right child
	 *  - on both children
	 */

	TupleType tt3 = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);

	@Test
	public void testDependentJoin() {

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

		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes1, new Integer[0], relation1, attributeMapping1);

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

		Integer[] inputs = new Integer[] { 0 };
		InMemoryAccessMethod am0 = new InMemoryAccessMethod(amAttributes2, inputs, relation2, attributeMapping2);

		// Access on relation r2 that requires input on attribute "c" (i.e. position 0), 
		// in this case, a typed constant with value 100.
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(0, TypedConstant.create(100));

		// Valid dependent join construction: 
		// The right child requires input in position 0, which is supplied by 
		// the left child output in position 2 (attribute "c"). Therefore the 
		// dependent join plan has no input attributes in this case.
		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(new AccessTerm(amFree), 
				new AccessTerm(am0)));
		Assert.assertNotNull(target);
	}

	/*
	 *  The following are integration tests: DependentJoin plans are constructed & executed. 
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

		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes1, new Integer[0], relation1, attributeMapping1);

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

		Integer[] inputs = new Integer[] { 0 };
		InMemoryAccessMethod am0 = new InMemoryAccessMethod(amAttributes2, inputs, relation2, attributeMapping2);

		/*
		 * Plan: DependentJoin{k}(Access1, Access2).
		 */
		AccessTerm leftChild = new AccessTerm(amFree);
		AccessTerm rightChild = new AccessTerm(am0);
		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(leftChild, rightChild));

		// Check the inferred join condition. The common attribute "k" is in 
		// position 2 in relation1 and position 0 in relation2 
		// (i.e. position 3 in the concatenated attributes).
		Assert.assertTrue(target.getJoinCondition() instanceof TypeEqualityCondition);
		TypeEqualityCondition condition = (TypeEqualityCondition) target.getJoinCondition();
		Assert.assertEquals(2, condition.getPosition());
		Assert.assertEquals(3, condition.getOther());

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
		amFree.load(tuples1);
		am0.load(tuples2);

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// In r1 the "c" column ranges from 2 to 13. In r2 the "c" column ranges from 8 to 25.
		// Note that the result tuples contain the tuple from the right appended onto the tuple from the left.
		TupleType tte = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class, 
				Integer.class, Integer.class, Integer.class);
		List<Tuple> expected = new ArrayList<Tuple>();
		expected.add(tte.createTuple((Object[]) new Integer[] { 6, 7, 8, 8, 9, 10}));
		expected.add(tte.createTuple((Object[]) new Integer[] { 7, 8, 9, 9, 10, 11}));
		expected.add(tte.createTuple((Object[]) new Integer[] { 8, 9, 10, 10, 11, 12}));
		expected.add(tte.createTuple((Object[]) new Integer[] { 9, 10, 11, 11, 12, 13}));
		expected.add(tte.createTuple((Object[]) new Integer[] { 10, 11, 12, 12, 13, 14}));
		expected.add(tte.createTuple((Object[]) new Integer[] { 11, 12, 13, 13, 14, 15}));

		// The values in common are 10 to 13.
		Assert.assertEquals(6, result.size());
		Assert.assertEquals(expected, result);
		target.close();
	}

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

		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes1, new Integer[0], relation1, attributeMapping1);

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

		Integer[] inputs = new Integer[] { 0 };
		InMemoryAccessMethod am0 = new InMemoryAccessMethod(amAttributes2, inputs, relation2, attributeMapping2);

		/*
		 * Plan: DependentJoin{k}(Access1, Selection(Access2)).
		 */
		AccessTerm leftChild = new AccessTerm(amFree);

		Condition condition = ConstantInequalityCondition.create(0, TypedConstant.create(10));
		SelectionTerm rightChild = new SelectionTerm(condition, new AccessTerm(am0));

		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(leftChild, rightChild));

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
		amFree.load(tuples1);
		am0.load(tuples2);

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// In r1 the "c" column ranges from 2 to 13. 
		// In r2 the "c" column ranges from 8 to 25, but the selection excludes those above 9.
		// Note that the result tuples contain the tuple from the right appended onto the tuple from the left.
		TupleType tte = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class, 
				Integer.class, Integer.class, Integer.class);
		List<Tuple> expected = new ArrayList<Tuple>();
		expected.add(tte.createTuple((Object[]) new Integer[] { 6, 7, 8, 8, 9, 10}));
		expected.add(tte.createTuple((Object[]) new Integer[] { 7, 8, 9, 9, 10, 11}));

		Assert.assertEquals(2, result.size());
		Assert.assertEquals(expected, result);
		target.close();
	}

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

		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes1, new Integer[0], relation1, attributeMapping1);

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

		Integer[] inputs = new Integer[] { 0 };
		InMemoryAccessMethod am0 = new InMemoryAccessMethod(amAttributes2, inputs, relation2, attributeMapping2);

		/*
		 * Plan: DependentJoin{k}(Selection(Access1), Access2).
		 */
		Condition condition = ConstantInequalityCondition.create(0, TypedConstant.create(7), false);
		SelectionTerm leftChild = new SelectionTerm(condition, new AccessTerm(amFree));

		AccessTerm rightChild = new AccessTerm(am0);

		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(leftChild, rightChild));

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
		amFree.load(tuples1);
		am0.load(tuples2);

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// In r1 the "c" column ranges from 2 to 13, but the selection excludes those below 10. 
		// In r2 the "c" column ranges from 8 to 25.
		// Note that the result tuples contain the tuple from the right appended onto the tuple from the left.
		TupleType tte = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class, 
				Integer.class, Integer.class, Integer.class);
		List<Tuple> expected = new ArrayList<Tuple>();
		expected.add(tte.createTuple((Object[]) new Integer[] { 8, 9, 10, 10, 11, 12}));
		expected.add(tte.createTuple((Object[]) new Integer[] { 9, 10, 11, 11, 12, 13}));
		expected.add(tte.createTuple((Object[]) new Integer[] { 10, 11, 12, 12, 13, 14}));
		expected.add(tte.createTuple((Object[]) new Integer[] { 11, 12, 13, 13, 14, 15}));

		Assert.assertEquals(4, result.size());
		Assert.assertEquals(expected, result);
		target.close();
	}

	/*
	 * DependentProduct(NATION, REGION)
	 */
	@Test
	public void integrationTestSql1() {

		/*
		 * DependentProduct(NATION, REGION).
		 * Left: free access on NATION relation
		 * Right: access REGION relation with input required on position 0 (R_REGIONKEY)
		 */
		AccessTerm leftChild = new AccessTerm(TPCHelper.amFreeNation);
		AccessTerm rightChild = new AccessTerm(TPCHelper.am0Region);
		
		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(leftChild, rightChild));

		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "nationKey", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());
		
		Assert.assertTrue(result.stream().allMatch(t -> t.size() == 6));
				
		// Check that all of the values in positions 2 and 3 are equal.
		Assert.assertTrue(result.stream()
				.allMatch(t -> ((Integer) t.getValue(2)).equals((Integer) t.getValue(3))));

		// Each tuple in the NATION relation matches a unique region key.
		Assert.assertEquals(25, result.size());
		target.close();
	}
	
	@Test
	public void integrationTestSql2() {

		/*
		 * DependentJoin{nationKey}(Access1, Access2).
		 * Left: free access on NATION relation
		 * Right: access CUSTOMER relation with input required on position 3 (C_NATIONKEY)
		 */
		AccessTerm leftChild = new AccessTerm(TPCHelper.amFreeNation);
		AccessTerm rightChild = new AccessTerm(TPCHelper.am3Customer);

		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(leftChild, rightChild));

		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "nationKey", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Assert.assertNotNull(result);

		// Since all NATIONKEY values are found in the NATION relation, there is a single result tuple
		// for each tuple in the CUSTOMER relation.
		// SELECT COUNT(*) FROM NATION, CUSTOMER WHERE CUSTOMER.c_nationkey = NATION.n_nationkey;
		Assert.assertEquals(150000, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql3() {

		/*
		 * DependentJoin{nationKey}(Access1, Selection(Access2))
		 * Left: free access on NATION relation
		 * Right: access CUSTOMER relation with input required on 3rd position (NATIONKEY), 
		 * 			then Select MKTSEGMENT = "AUTOMOBILE"  
		 */
		AccessTerm leftChild = new AccessTerm(TPCHelper.amFreeNation); 

		Condition condition = ConstantEqualityCondition.create(3, TypedConstant.create("AUTOMOBILE"));
		SelectionTerm rightChild = new SelectionTerm(condition, new AccessTerm(TPCHelper.am3Customer));

		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(leftChild, rightChild));

		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "NATIONKEY", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());
		Assert.assertNotNull(result);

		// SQL CHECK:
		// WITH right_child AS (SELECT * FROM CUSTOMER WHERE CUSTOMER.c_mktsegment='AUTOMOBILE'), left_child AS (SELECT * FROM NATION) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
		Assert.assertEquals(29752, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql4() {

		/*
		 * DependentJoin{nationKey}(Selection(Access1), Access2)
		 * Left: free access on NATION relation then Select N_REGIONKEY > 1
		 * Right: access CUSTOMER relation with input required on 3rd position (NATIONKEY)
		 */
		Condition condition = ConstantInequalityCondition.create(2, TypedConstant.create(1), false);
		SelectionTerm leftChild = new SelectionTerm(condition, new AccessTerm(TPCHelper.amFreeNation)); 

		AccessTerm rightChild = new AccessTerm(TPCHelper.am3Customer);

		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(leftChild, rightChild));

		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "NATIONKEY", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());
		Assert.assertNotNull(result);

		// SQL CHECK:
		// WITH right_child AS (SELECT * FROM NATION WHERE NATION.N_REGIONKEY > 1), left_child AS (SELECT * FROM CUSTOMER) SELECT count(*) FROM left_child, right_child WHERE left_child.c_nationkey = right_child.n_nationkey;
		Assert.assertEquals(90284, result.size());
		target.close();
	}

	/*
	 * Stress tests
	 */

	// See issue #209.

	Relation relationR1 = Mockito.mock(Relation.class);
	Attribute[] relationR1Attributes = new Attribute[] {Attribute.create(String.class, "a"),
			Attribute.create(String.class, "b")};

	Relation relationR2 = Mockito.mock(Relation.class);
	Attribute[] relationR2Attributes = new Attribute[] {Attribute.create(String.class, "a"),
			Attribute.create(String.class, "c")};

	Relation relationR3 = Mockito.mock(Relation.class);
	Attribute[] relationR3Attributes = new Attribute[] {Attribute.create(String.class, "b"),
			Attribute.create(String.class, "d")};

	Relation relationR4 = Mockito.mock(Relation.class);
	Attribute[] relationR4Attributes = new Attribute[] {Attribute.create(String.class, "d"),
			Attribute.create(String.class, "e")};

	Relation relationR5 = Mockito.mock(Relation.class);
	Attribute[] relationR5Attributes = new Attribute[] {Attribute.create(String.class, "e")};

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

	Map<Attribute, Attribute> attributeMapping5 = ImmutableMap.of(
			Attribute.create(String.class, "e"), Attribute.create(String.class, "e"));

	TupleType ttString = TupleType.DefaultFactory.create(String.class);
	TupleType ttStringString = TupleType.DefaultFactory.create(String.class, String.class);

	@Test
	public void stressTestInMemory1() {

		// Scale parameter (number of tuples in each relation):
		int N = 1000;

		when(relationR1.getAttributes()).thenReturn(relationR1Attributes.clone());
		when(relationR2.getAttributes()).thenReturn(relationR2Attributes.clone());
		when(relationR3.getAttributes()).thenReturn(relationR3Attributes.clone());
		when(relationR4.getAttributes()).thenReturn(relationR4Attributes.clone());

		InMemoryAccessMethod am1Free = new InMemoryAccessMethod(relationR1Attributes, new Integer[0], relationR1, attributeMapping1);

		Set<Attribute> inputAttributes2 = Sets.newHashSet(Attribute.create(String.class, "a"));		
		InMemoryAccessMethod am20 = new InMemoryAccessMethod(relationR2Attributes, inputAttributes2, relationR2, attributeMapping2);

		Set<Attribute> inputAttributes3 = Sets.newHashSet(Attribute.create(String.class, "b"));		
		InMemoryAccessMethod am30 = new InMemoryAccessMethod(relationR3Attributes, inputAttributes3, relationR3, attributeMapping3);

		Set<Attribute> inputAttributes4 = Sets.newHashSet(Attribute.create(String.class, "d"));		
		InMemoryAccessMethod am40 = new InMemoryAccessMethod(relationR4Attributes, inputAttributes4, relationR4, attributeMapping4);

		/*
		 * DependentJoin{a}(R1, R2).
		 */
		DependentJoinUnbatched dependentJoinR1R2 = new DependentJoinUnbatched(new DependentJoinTerm(
				new AccessTerm(am1Free), new AccessTerm(am20)));

		/*
		 * DependentJoin{d}(R3, R4).
		 */
		DependentJoinUnbatched dependentJoinR3R4 = new DependentJoinUnbatched(new DependentJoinTerm(
				new AccessTerm(am30), new AccessTerm(am40)));

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
		am30.load(tuples3);
		am40.load(tuples4);

		/*
		 * DependentJoin{b}(dependentJoinR1R2, dependentJoinR3R4).
		 */
		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(
				dependentJoinR1R2, dependentJoinR3R4));

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Assert.assertEquals(N, result.size());
		target.close();
	}

	@Test
	public void stressTestInMemory2() {

		// Scale parameter (number of tuples in each relation):
		int N = 10;

		when(relationR3.getAttributes()).thenReturn(relationR3Attributes.clone());
		when(relationR4.getAttributes()).thenReturn(relationR4Attributes.clone());
		when(relationR5.getAttributes()).thenReturn(relationR5Attributes.clone());

		InMemoryAccessMethod am3Free = new InMemoryAccessMethod(relationR3Attributes, new Integer[0], relationR3, attributeMapping3);

		Set<Attribute> inputAttributes4 = Sets.newHashSet(Attribute.create(String.class, "d"), Attribute.create(String.class, "e"));		
		InMemoryAccessMethod am401 = new InMemoryAccessMethod(relationR4Attributes, inputAttributes4, relationR4, attributeMapping4);

		InMemoryAccessMethod am5Free = new InMemoryAccessMethod(relationR5Attributes, new Integer[0], relationR5, attributeMapping5);

		/*
		 * Access(R5).
		 */
		Access accessR5 = new Access(new AccessTerm(am5Free));

		/*
		 * DependentJoin{d}(R3, R4).
		 */
		DependentJoinUnbatched dependentJoinR3R4 = new DependentJoinUnbatched(new DependentJoinTerm(
				new AccessTerm(am3Free), new AccessTerm(am401)));

		// Create some tuples. 
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
		Collection<Tuple> tuples5 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			String[] values = new String[] {"e" + i};
			tuples5.add(ttString.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples   
		am3Free.load(tuples3);
		am401.load(tuples4);
		am5Free.load(tuples5);

		// Test the sub-queries.
		Assert.assertEquals(N, accessR5.execute().size());
		Assert.assertEquals(N, accessR5.execute().size());

		dependentJoinR3R4.setInputTuples(tuples5.iterator());
		Assert.assertEquals(N, dependentJoinR3R4.execute().size());

		/*
		 * DependentJoin{e}(AccessR5, dependentJoinR3R4).
		 */
		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(
				accessR5, dependentJoinR3R4));

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// Check that all of the values in positions 2 and 3 are equal.
		Assert.assertTrue(result.stream()
				.allMatch(t -> ((String) t.getValue(2)).equals((String) t.getValue(3))));

		// Check that all of the values in positions 0 and 4 are equal.
		Assert.assertTrue(result.stream()
				.allMatch(t -> ((String) t.getValue(0)).equals((String) t.getValue(4))));

		Assert.assertEquals(N, result.size());
		target.close();
	}

	/*
	 * Plan: DependentJoin(NATION, SUPPLIER)
	 */
	@Test
	public void stressTestSql1() {

		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(
				new AccessTerm(TPCHelper.amFreeNation), 
				new AccessTerm(TPCHelper.am3Supplier)));

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Tuple tuple = result.get(0);
		Assert.assertEquals(tuple.size(), TPCHelper.attrs_nation.length + TPCHelper.attrs_supplier.length);

		// select count(*) from NATION, SUPPLIER where NATION.N_NATIONKEY=SUPPLIER.S_NATIONKEY;	
		Assert.assertEquals(10000, result.size());
		target.close();
	}

	/*
	 * Plan: DependentJoin(NATION_LESS, SUPPLIER_LESS)
	 */
	@Test
	public void stressTestSql1a() {

		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(
				new AccessTerm(TPCHelper.amFreeNation_less), 
				new AccessTerm(TPCHelper.am3Supplier_less)));

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Tuple tuple = result.get(0);
		Assert.assertEquals(tuple.size(), TPCHelper.attrs_nation_less.length + TPCHelper.attrs_supplier_less.length);

		// select count(*) from NATION, SUPPLIER where NATION.N_NATIONKEY=SUPPLIER.S_NATIONKEY;	
		Assert.assertEquals(10000, result.size());
		target.close();
	}

//	/*
//	 * Plan: DependentJoin(PART, PARTSUPP)
//	 */
//	@Test
//	public void stressTestSql2() {
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(
//				new AccessTerm(TPCHelper.amFreePart), 
//				new AccessTerm(TPCHelper.am0PartSupp)));
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
	/*
	 * Plan: DependentJoin(PART_LESS, PARTSUPP_LESS)
	 */
	@Test
	public void stressTestSql2a() {

		DependentJoinUnbatched target = new DependentJoinUnbatched(new DependentJoinTerm(
				new AccessTerm(TPCHelper.amFreePart_less), 
				new AccessTerm(TPCHelper.am0PartSupp_less)));

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Tuple tuple = result.get(0);
		Assert.assertEquals(tuple.size(), TPCHelper.attrs_part_less.length + TPCHelper.attrs_partSupp_less.length);

		// select count(*) from PART, PARTSUPP where PART.P_PARTKEY=PARTSUPP.PS_PARTKEY;	
		Assert.assertEquals(800000, result.size());
		target.close();
	}
//
//	/*
//	 * Plan: DependentJoin(DependentJoin(NATION, SUPPLIER), DependentJoin(PART, PARTSUPP))
//	 */
//	@Test
//	public void stressTestSql3() {
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(
//				new DependentJoinTerm(
//						new AccessTerm(TPCHelper.amFreeNation), 
//						new AccessTerm(TPCHelper.am3Supplier)),
//				new DependentJoinTerm(
//						new AccessTerm(TPCHelper.amFreePart), 
//						new AccessTerm(TPCHelper.am01PartSupp))
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
//	 * Plan: DependentJoin(DependentJoin(NATION_LESS, SUPPLIER_LESS), DependentJoin(PART_LESS, PARTSUPP_LESS))
//	 */
//	@Test
//	public void stressTestSql3a() {
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(
//				new DependentJoinTerm(
//						new AccessTerm(TPCHelper.amFreeNation_less), 
//						new AccessTerm(TPCHelper.am3Supplier_less)),
//				new DependentJoinTerm(
//						new AccessTerm(TPCHelper.amFreePart_less), 
//						new AccessTerm(TPCHelper.am01PartSupp_less))
//				));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		Tuple tuple = result.get(0);
//		int expected = TPCHelper.attrs_nation_less.length + TPCHelper.attrs_supplier_less.length + 
//				TPCHelper.attrs_part_less.length + TPCHelper.attrs_partSupp_less.length;
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
//	 * Plan: DependentJoin(DependentJoin(NATION, Selection(SUPPLIER)),  DependentJoin(Selection(PART), PARTSUPP) )
//	 */
//	@Test
//	public void stressTestSql4() {
//
//		// Select suppliers whose account balance is negative.
//		Condition acctBalCondition = ConstantInequalityCondition.create(4, TypedConstant.create(0.0f));
//		DependentJoinTerm leftChild = new DependentJoinTerm(new 
//				AccessTerm(TPCHelper.amFreeNation), 
//				new SelectionTerm(acctBalCondition, new AccessTerm(TPCHelper.am3Supplier)));
//
//		// Select parts whose size is greater than 10.
//		Condition sizeCondition = ConstantInequalityCondition.create(3, TypedConstant.create(10), false);
//		DependentJoinTerm rightChild = new DependentJoinTerm(new 
//				SelectionTerm(sizeCondition, new AccessTerm(TPCHelper.amFreePart)), 
//				new AccessTerm(TPCHelper.am01PartSupp));
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE SUPPLIER.S_ACCTBAL < 0 AND NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_SIZE > 10 AND PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;	
//		Assert.assertEquals(56648, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: DependentJoin(DependentJoin(NATION_LESS, Selection(SUPPLIER_LESS)),  DependentJoin(Selection(PART_LESS), PARTSUPP_LESS) )
//	 */
//	@Test
//	public void stressTestSql4a() {
//
//		// Select suppliers whose account balance is negative.
//		Condition acctBalCondition = ConstantInequalityCondition.create(4, TypedConstant.create(0.0f));
//		DependentJoinTerm leftChild = new DependentJoinTerm(new 
//				AccessTerm(TPCHelper.amFreeNation_less), 
//				new SelectionTerm(acctBalCondition, new AccessTerm(TPCHelper.am3Supplier_less)));
//
//		// Select parts whose size is greater than 10.
//		Condition sizeCondition = ConstantInequalityCondition.create(3, TypedConstant.create(10), false);
//		DependentJoinTerm rightChild = new DependentJoinTerm(new 
//				SelectionTerm(sizeCondition, new AccessTerm(TPCHelper.amFreePart_less)), 
//				new AccessTerm(TPCHelper.am01PartSupp_less));
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE SUPPLIER.S_ACCTBAL < 0 AND NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_SIZE > 10 AND PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;	
//		Assert.assertEquals(56648, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: DependentJoin(DependentJoin(Selection(NATION), SUPPLIER),  DependentJoin(CUSTOMER, Selection(ORDERS))
//	 */
//	@Test
//	public void stressTestSql5() {
//
//		// Select nations whose nation key is greater than 8.
//		Condition nationCondition = ConstantInequalityCondition.create(1, TypedConstant.create(8), false);
//		DependentJoinTerm leftChild = new DependentJoinTerm(
//				new SelectionTerm(nationCondition, new AccessTerm(TPCHelper.amFreeNation)), 
//				new AccessTerm(TPCHelper.am3Supplier));
//
//		// Select orders whose total price is greater than 200000.
//		Condition totalPriceCondition = ConstantInequalityCondition.create(2, TypedConstant.create(200000f), false);
//		DependentJoinTerm rightChild = new DependentJoinTerm(
//				new AccessTerm(TPCHelper.am3Customer), 
//				new SelectionTerm(totalPriceCondition, new AccessTerm(TPCHelper.am1Orders)));
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(leftChild, rightChild));
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
//	 * Plan: DependentJoin(DependentJoin(Selection(NATION_LESS), SUPPLIER_LESS),  DependentJoin(CUSTOMER_LESS, Selection(ORDERS_LESS))
//	 */
//	@Test
//	public void stressTestSql5a() {
//
//		// Select nations whose nation key is greater than 8.
//		Condition nationCondition = ConstantInequalityCondition.create(1, TypedConstant.create(8), false);
//		DependentJoinTerm leftChild = new DependentJoinTerm(
//				new SelectionTerm(nationCondition, new AccessTerm(TPCHelper.amFreeNation_less)), 
//				new AccessTerm(TPCHelper.am3Supplier_less));
//
//		// Select orders whose total price is greater than 200000.
//		Condition totalPriceCondition = ConstantInequalityCondition.create(2, TypedConstant.create(200000f), false);
//		DependentJoinTerm rightChild = new DependentJoinTerm(
//				new AccessTerm(TPCHelper.am3Customer_less), 
//				new SelectionTerm(totalPriceCondition, new AccessTerm(TPCHelper.am1Orders_less)));
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(leftChild, rightChild));
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
//	 * Plan: DependentJoin(Join(NATION, Selection(SUPPLIER)),  DependentJoin(Selection(PART), PARTSUPP) )
//	 */
//	@Test
//	public void stressTestSql6() {
//
//		// Select suppliers whose account balance is negative.
//		Condition acctBalCondition = ConstantInequalityCondition.create(4, TypedConstant.create(0.0f));
//		JoinTerm leftChild = new JoinTerm(
//				new AccessTerm(TPCHelper.amFreeNation), 
//				new SelectionTerm(acctBalCondition, new AccessTerm(TPCHelper.amFreeSupplier)));
//
//		// Select parts whose size is greater than 10.
//		Condition sizeCondition = ConstantInequalityCondition.create(3, TypedConstant.create(10), false);
//		DependentJoinTerm rightChild = new DependentJoinTerm(
//				new SelectionTerm(sizeCondition, new AccessTerm(TPCHelper.amFreePart)), 
//				new AccessTerm(TPCHelper.am01PartSupp));
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE SUPPLIER.S_ACCTBAL < 0 AND NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_SIZE > 10 AND PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;	
//		Assert.assertEquals(56648, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: DependentJoin(Join(NATION_LESS, Selection(SUPPLIER_LESS)),  DependentJoin(Selection(PART_LESS), PARTSUPP_LESS) )
//	 */
//	@Test
//	public void stressTestSql6a() {
//
//		// Select suppliers whose account balance is negative.
//		Condition acctBalCondition = ConstantInequalityCondition.create(4, TypedConstant.create(0.0f));
//		JoinTerm leftChild = new JoinTerm(
//				new AccessTerm(TPCHelper.amFreeNation_less), 
//				new SelectionTerm(acctBalCondition, new AccessTerm(TPCHelper.amFreeSupplier_less)));
//
//		// Select parts whose size is greater than 10.
//		Condition sizeCondition = ConstantInequalityCondition.create(3, TypedConstant.create(10), false);
//		DependentJoinTerm rightChild = new DependentJoinTerm(
//				new SelectionTerm(sizeCondition, new AccessTerm(TPCHelper.amFreePart_less)), 
//				new AccessTerm(TPCHelper.am01PartSupp_less));
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE SUPPLIER.S_ACCTBAL < 0 AND NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_SIZE > 10 AND PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;	
//		Assert.assertEquals(56648, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: DependentJoin{PARTKEY, SUPPKEY}(
//	 * 			DependentJoin{SUPPKEY}(
//	 * 				Join[NATIONKEY](
//	 * 					DependentJoin{REGIONKEY}(REGION, NATION), 
//	 * 					SUPPLIER), 
//	 * 				Join[PARTKEY](PART, PARTSUPP))
//	 * 			Join[ORDERKEY](
//	 * 				DependentJoin{CUSTKEY}(Selection(CUSTOMER), ORDERS), 
//	 * 				Selection(LINEITEM))
//	 */
//	@Test
//	public void stressTestSql7() {
//
//		// Construct the target plan in stages.
//
//		// 1. Join[NATIONKEY](DependentJoin{REGIONKEY}(REGION, NATION), SUPPLIER)
//		JoinTerm joinRNS = new JoinTerm(
//				new DependentJoinTerm(
//						new AccessTerm(TPCHelper.amFreeRegion), new AccessTerm(TPCHelper.am2Nation)), 
//				new AccessTerm(TPCHelper.amFreeSupplier));
//
//		// 2. Join[PARTKEY](PART, PARTSUPP)
//		JoinTerm joinPPS = new JoinTerm(
//				new AccessTerm(TPCHelper.amFreePart), 
//				new AccessTerm(TPCHelper.am1PartSupp));
//
//		// 3. DependentJoin{SUPPKEY}(Join[NATIONKEY](DependentJoin{REGIONKEY}(REGION, NATION), SUPPLIER), Join[PARTKEY](PART, PARTSUPP))
//		DependentJoinTerm leftChild = new DependentJoinTerm(joinRNS, joinPPS);
//
//		// 4. DependentJoin{CUSTKEY}(Selection(CUSTOMER), ORDERS)
//		// Select customers whose account balance is negative.
//		Condition customerCondition = ConstantInequalityCondition.create(2, TypedConstant.create(0.0f));
//		DependentJoinTerm depJoinCO = new DependentJoinTerm(
//				new SelectionTerm(customerCondition, new AccessTerm(TPCHelper.amFreeCustomer)), 
//				new AccessTerm(TPCHelper.am1Orders));
//
//		// 5. Join[ORDERKEY](DependentJoin{CUSTKEY}(Selection(CUSTOMER), ORDERS), Selection(LINEITEM))
//		// Select lineItems whose quantity is less that 20.
//		Condition lineItemCondition = ConstantInequalityCondition.create(3, TypedConstant.create(20));
//		JoinTerm rightChild = new JoinTerm(
//				depJoinCO, 
//				new SelectionTerm(lineItemCondition, new AccessTerm(TPCHelper.am012LineItem)));
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (WITH leftleft_child AS (WITH leftleftleft_child AS (SELECT * FROM REGION, NATION WHERE REGION.R_REGIONKEY = NATION.N_REGIONKEY) SELECT * FROM leftleftleft_child, SUPPLIER WHERE leftleftleft_child.N_NATIONKEY = SUPPLIER.S_NATIONKEY), leftright_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT * FROM leftleft_child, leftright_child WHERE leftleft_child.S_SUPPKEY = leftright_child.PS_SUPPKEY), right_child AS (WITH rightleft_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE CUSTOMER.C_ACCTBAL < 0 AND CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY), rightright_child AS (SELECT * FROM LINEITEM WHERE L_QUANTITY < 20) SELECT * FROM rightleft_child, rightright_child WHERE rightleft_child.O_ORDERKEY = rightright_child.L_ORDERKEY) SELECT COUNT(*) FROM left_child, right_child WHERE left_child.P_PARTKEY = right_child.L_PARTKEY AND left_child.PS_SUPPKEY = right_child.L_SUPPKEY;
//		Assert.assertEquals(207602, result.size());
//		target.close();
//	}
//	
//	/*
//	 * Plan: DependentJoin{PARTKEY, SUPPKEY}(
//	 * 			DependentJoin{SUPPKEY}(
//	 * 				Join[NATIONKEY](
//	 * 					DependentJoin{REGIONKEY}(REGION_LESS, NATION_LESS), 
//	 * 					SUPPLIER_LESS), 
//	 * 				Join[PARTKEY](PART_LESS, PARTSUPP_LESS))
//	 * 			Join[ORDERKEY](
//	 * 				DependentJoin{CUSTKEY}(Selection(CUSTOMER_LESS), ORDERS_LESS), 
//	 * 				Selection(LINEITEM_LESS))
//	 */
//	@Test
//	public void stressTestSql7a() {
//
//		// Construct the target plan in stages.
//
//		// 1. Join[NATIONKEY](DependentJoin{REGIONKEY}(REGION, NATION), SUPPLIER)
//		JoinTerm joinRNS = new JoinTerm(
//				new DependentJoinTerm(
//						new AccessTerm(TPCHelper.amFreeRegion_less), new AccessTerm(TPCHelper.am2Nation_less)), 
//				new AccessTerm(TPCHelper.amFreeSupplier_less));
//
//		// 2. Join[PARTKEY](PART, PARTSUPP)
//		JoinTerm joinPPS = new JoinTerm(
//				new AccessTerm(TPCHelper.amFreePart_less), 
//				new AccessTerm(TPCHelper.am1PartSupp_less));
//
//		// 3. DependentJoin{SUPPKEY}(Join[NATIONKEY](DependentJoin{REGIONKEY}(REGION, NATION), SUPPLIER), Join[PARTKEY](PART, PARTSUPP))
//		DependentJoinTerm leftChild = new DependentJoinTerm(joinRNS, joinPPS);
//
//		// 4. DependentJoin{CUSTKEY}(Selection(CUSTOMER), ORDERS)
//		// Select customers whose account balance is negative.
//		Condition customerCondition = ConstantInequalityCondition.create(2, TypedConstant.create(0.0f));
//		DependentJoinTerm depJoinCO = new DependentJoinTerm(
//				new SelectionTerm(customerCondition, new AccessTerm(TPCHelper.amFreeCustomer_less)), 
//				new AccessTerm(TPCHelper.am1Orders_less));
//
//		// 5. Join[ORDERKEY](DependentJoin{CUSTKEY}(Selection(CUSTOMER), ORDERS), Selection(LINEITEM))
//		// Select lineItems whose quantity is less that 20.
//		Condition lineItemCondition = ConstantInequalityCondition.create(3, TypedConstant.create(20));
//		JoinTerm rightChild = new JoinTerm(
//				depJoinCO, 
//				new SelectionTerm(lineItemCondition, new AccessTerm(TPCHelper.am012LineItem_less)));
//
//		DependentJoin target = new DependentJoin(new DependentJoinTerm(leftChild, rightChild));
//
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (WITH leftleft_child AS (WITH leftleftleft_child AS (SELECT * FROM REGION, NATION WHERE REGION.R_REGIONKEY = NATION.N_REGIONKEY) SELECT * FROM leftleftleft_child, SUPPLIER WHERE leftleftleft_child.N_NATIONKEY = SUPPLIER.S_NATIONKEY), leftright_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT * FROM leftleft_child, leftright_child WHERE leftleft_child.S_SUPPKEY = leftright_child.PS_SUPPKEY), right_child AS (WITH rightleft_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE CUSTOMER.C_ACCTBAL < 0 AND CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY), rightright_child AS (SELECT * FROM LINEITEM WHERE L_QUANTITY < 20) SELECT * FROM rightleft_child, rightright_child WHERE rightleft_child.O_ORDERKEY = rightright_child.L_ORDERKEY) SELECT COUNT(*) FROM left_child, right_child WHERE left_child.P_PARTKEY = right_child.L_PARTKEY AND left_child.PS_SUPPKEY = right_child.L_SUPPKEY;
//		Assert.assertEquals(207602, result.size());
//		target.close();
//	}
}

