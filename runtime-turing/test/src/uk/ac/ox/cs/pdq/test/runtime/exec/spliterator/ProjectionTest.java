package uk.ac.ox.cs.pdq.test.runtime.exec.spliterator;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantInequalityCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.datasources.AbstractAccessMethod;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.datasources.sql.DatabaseAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Projection;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

public class ProjectionTest {

	@Test
	public void testGetProjectionFunction() {

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

		Attribute[] projectionAttributes = new Attribute[]{Attribute.create(Integer.class, "b")};
		ProjectionTerm plan = ProjectionTerm.create(projectionAttributes, AccessTerm.create(amFree.getRelation(),amFree));

		Projection target = new Projection(plan);

		Function<Tuple, Tuple> result = target.getProjectionFunction();

		TupleType ttIntegerIntegerString = TupleType.DefaultFactory.create(Integer.class, Integer.class, String.class);
		Tuple tuple = ttIntegerIntegerString.createTuple(10, 11, "x");

		TupleType ttInteger = TupleType.DefaultFactory.create(Integer.class);

		Assert.assertEquals(result.apply(tuple), ttInteger.createTuple(11));

		// Now test with multiple projection attributes.
		projectionAttributes = new Attribute[]{Attribute.create(String.class, "c"), Attribute.create(Integer.class, "a")};
		plan = ProjectionTerm.create(projectionAttributes, AccessTerm.create(amFree.getRelation(),amFree));
		target.close();

		target = new Projection(plan);

		result = target.getProjectionFunction();

		TupleType tStringInteger = TupleType.DefaultFactory.create(String.class, Integer.class);

		Assert.assertEquals(result.apply(tuple), tStringInteger.createTuple("x", 10));
		target.close();
	}

	/*
	 *  The following are integration tests: Projection plans are constructed & executed. 
	 */

	@Test
	public void integrationTestInMemory1() {

		Attribute[] projectionAttributes;
		ProjectionTerm plan;
		Projection target;
		List<Tuple> result;

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
		 * Plan: free access and projection onto column "b".
		 */
		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes, new Integer[0], relation, attributeMapping);
		projectionAttributes = new Attribute[]{Attribute.create(Integer.class, "b")};
		plan = ProjectionTerm.create(projectionAttributes, AccessTerm.create(amFree.getRelation(),amFree));

		target = new Projection(plan);

		// Confirm that there are no input attributes.
		Assert.assertEquals(0, target.getInputAttributes().length);

		TupleType ttStringIntegerIntegerInteger = TupleType.DefaultFactory.create(String.class, 
				Integer.class, Integer.class, Integer.class);
		Collection<Tuple> tuples = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			tuples.add(ttStringIntegerIntegerInteger.createTuple("x", 10, 11, 12));
		}
		amFree.load(tuples);

		// Execute the plan. 
		result = target.stream().collect(Collectors.toList());

		// Check that the result tuples are the ones expected.
		Assert.assertNotNull(result);
		Assert.assertEquals(N, result.size());
		Assert.assertTrue(result.stream().allMatch(t -> t.size() == 1));
		Assert.assertTrue(result.stream().allMatch(t -> (Integer) t.getValue(0) == 11));
		target.close();

		/*
		 * Plan: free access and projection onto columns ("b", "c").
		 */
		projectionAttributes = new Attribute[]{Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
		plan = ProjectionTerm.create(projectionAttributes, AccessTerm.create(amFree.getRelation(),amFree));

		target = new Projection(plan);

		// Execute the plan. 
		result = target.stream().collect(Collectors.toList());
		target.close();

		// Check that the result tuples are the ones expected.
		Assert.assertNotNull(result);
		Assert.assertEquals(N, result.size());
		Assert.assertTrue(result.stream().allMatch(t -> t.size() == 2));
		Assert.assertTrue(result.stream().allMatch(t -> (Integer) t.getValue(0) == 11));
		Assert.assertTrue(result.stream().allMatch(t -> (String) t.getValue(1) == "x"));

		// Test re-executing the plan (this time using the execute method).
		Assert.assertEquals(N, target.execute().size());
		Assert.assertEquals(N, target.execute().size());
	}

	@Test
	public void integrationTestInMemory2() {

		Attribute[] projectionAttributes;
		ProjectionTerm plan;
		Projection target;
		List<Tuple> result;

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
		 * Plan: Access with inputs on attribute "a" on and projection onto columns ("b", "c").
		 */
		Integer[] inputs = new Integer[] {1};
		InMemoryAccessMethod am12 = new InMemoryAccessMethod(amAttributes, inputs, relation, attributeMapping);

		projectionAttributes = new Attribute[]{Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
		plan = ProjectionTerm.create(projectionAttributes, AccessTerm.create(am12.getRelation(),am12));

		target = new Projection(plan);

		// Confirm that there is one input attribute, "a".
		Assert.assertArrayEquals(new Attribute[] { Attribute.create(Integer.class, "a") }, target.getInputAttributes());

		TupleType ttStringIntegerIntegerInteger = TupleType.DefaultFactory.create(String.class, 
				Integer.class, Integer.class, Integer.class);
		Collection<Tuple> tuples = new ArrayList<Tuple>();
		tuples.add(ttStringIntegerIntegerInteger.createTuple("w", 10, 11, 12));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("x", 20, 21, 22));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("y", 30, 31, 32));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("z", 40, 41, 42));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("a", 10, 51, 52));
		am12.load(tuples);

		// Attempting to execute the plan before setting the dynamic input raises an exception.
		boolean caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Set the input tuples
		TupleType ttInteger = TupleType.DefaultFactory.create(Integer.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(ttInteger.createTuple(10));
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan. 
		result = target.stream().collect(Collectors.toList());

		// Check that the result tuples are the ones expected.
		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());
		Assert.assertTrue(result.stream().allMatch(t -> t.size() == 2));
		Assert.assertEquals((Integer) 11, (Integer) result.get(0).getValue(0));
		Assert.assertEquals("w", result.get(0).getValue(1));

		Assert.assertEquals((Integer) 51, (Integer) result.get(1).getValue(0));
		Assert.assertEquals("a", result.get(1).getValue(1));
		target.close();
	}

	@Test
	public void integrationTestInMemory3() {

		Attribute[] projectionAttributes;
		ProjectionTerm plan;
		Projection target;
		List<Tuple> result;

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
		 * Plan: Free access, then select the rows where the value of attribute "a" 
		 *  is greater than the constant value 10 and projection onto columns ("b", "c").
		 */
		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes, new Integer[0], relation, attributeMapping);
		Condition condition = ConstantInequalityCondition.create(0, TypedConstant.create(10), false);
		projectionAttributes = new Attribute[]{Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
		plan = ProjectionTerm.create(projectionAttributes, SelectionTerm.create(condition, AccessTerm.create(amFree.getRelation(),amFree)));

		target = new Projection(plan);

		TupleType ttStringIntegerIntegerInteger = TupleType.DefaultFactory.create(String.class, 
				Integer.class, Integer.class, Integer.class);
		Collection<Tuple> tuples = new ArrayList<Tuple>();
		tuples.add(ttStringIntegerIntegerInteger.createTuple("w", 10, 11, 12));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("x", 20, 21, 22));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("y", 30, 31, 32));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("z", 40, 41, 42));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("a", 10, 51, 52));
		amFree.load(tuples);

		// Execute the plan. 
		result = target.stream().collect(Collectors.toList());

		// Check that the result tuples are the ones expected.
		Assert.assertNotNull(result);
		Assert.assertEquals(3, result.size());
		Assert.assertTrue(result.stream().allMatch(t -> t.size() == 2));
		Assert.assertEquals((Integer) 21, (Integer) result.get(0).getValue(0));
		Assert.assertEquals("x", result.get(0).getValue(1));

		Assert.assertEquals((Integer) 31, (Integer) result.get(1).getValue(0));
		Assert.assertEquals("y", result.get(1).getValue(1));

		Assert.assertEquals((Integer) 41, (Integer) result.get(2).getValue(0));
		Assert.assertEquals("z", result.get(2).getValue(1));

		target.close();
	}

	/*
	 * Plan: Projection(Selection(Access(CUSTOMER)))
	 */
	@Test
	public void integrationTestSql1() {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_customer.clone());

		Integer[] inputs = new Integer[0];
		AbstractAccessMethod amFree = new DatabaseAccessMethod("CUSTOMER", TPCHelper.attrs_C, inputs, relation, 
				TPCHelper.attrMap_customer, TPCHelper.getProperties());

		/*
		 *  Plan: free access on the sqlRelationCustomer relation then select the rows where the value
		 *  of the C_NATIONKEY column is 23 (United Kingdom) and finally project the C_NAME and C_ACCTBAL columns. 
		 */
		Attribute[] projectionAttributes = new Attribute[]{Attribute.create(String.class, "custName"), 
				Attribute.create(Float.class, "custAcctBal")};

		Condition condition = ConstantEqualityCondition.create(4, TypedConstant.create(23));
		RelationalTerm child = SelectionTerm.create(condition, AccessTerm.create(amFree.getRelation(),amFree));
		ProjectionTerm plan = ProjectionTerm.create(projectionAttributes, child);

		Projection target = new Projection(plan); 

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// Check that the result tuples are the ones expected. 
		Assert.assertTrue(result.stream().allMatch((t) -> t.size() == 2));

		// Test the result of the query:
		// SELECT COUNT(*) FROM CUSTOMER WHERE c_nationkey=23;
		Assert.assertEquals(6011, result.size());

		// Check the first account balance in the result against the results of the following query: 
		// SELECT c_name, c_acctbal FROM CUSTOMER WHERE c_nationkey=23;
		Assert.assertEquals(-272.60f, ((Float) result.get(0).getValue(1)).floatValue(), 0.0f); 
		target.close();
	}

	/*
	 * Plan: Projection(Access) with dynamic input.
	 */
	@Test
	public void integrationTestSql2() {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_customer.clone());

		Integer[] inputs = new Integer[] { 3 };
		AbstractAccessMethod am3 = new DatabaseAccessMethod("CUSTOMER", TPCHelper.attrs_C, inputs, relation, 
				TPCHelper.attrMap_customer, TPCHelper.getProperties());

		/*
		 *  Plan: access on the sqlRelationCustomer relation with dynamic input on attribute "C_NATIONKEY" 
		 *  and then project onto the custName and custAcctBal columns. 
		 */

		Attribute[] projectionAttributes = new Attribute[]{Attribute.create(String.class, "custName"), 
				Attribute.create(Float.class, "custAcctBal")};

		RelationalTerm child = AccessTerm.create(am3.getRelation(),am3);
		ProjectionTerm plan = ProjectionTerm.create(projectionAttributes, child);

		Projection target = new Projection(plan); 

		// Attempting to execute the plan before setting the dynamic input raises an exception.
		boolean caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Set the input tuples
		TupleType ttInteger = TupleType.DefaultFactory.create(Integer.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(ttInteger.createTuple(24));
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// Check that the result tuples are the ones expected. 
		Assert.assertTrue(result.stream().allMatch((t) -> t.size() == 2));

		// Test the result of the query:
		// SELECT COUNT(*) FROM CUSTOMER WHERE c_nationkey=24;
		Assert.assertEquals(5983, result.size());

		// SELECT c_acctbal FROM CUSTOMER WHERE c_nationkey=24 limit 1;
		Assert.assertEquals(3950.83f, ((Float) result.get(0).getValue(1)).floatValue(), 0.0f); 
		target.close();
	}

	/*
	 * Stress tests
	 */

	// See issues #209 & #254.

	/*
	 * Plan: Projection(Join(Join(NATION, SUPPLIER), Join(PART, PARTSUPP)))
	 */
	@Test
	public void stressTestSql1() {

		// Construct the target plan.
		JoinTerm leftChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
				AccessTerm.create(TPCHelper.amFreeSupplier.getRelation(),TPCHelper.amFreeSupplier));
		JoinTerm rightChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreePart.getRelation(),TPCHelper.amFreePart), 
				AccessTerm.create(TPCHelper.amFreePartSupp.getRelation(),TPCHelper.amFreePartSupp));

		Attribute[] projectionAttributes = new Attribute[]{
				Attribute.create(Integer.class, "nationKey"), 
				Attribute.create(Integer.class, "suppKey"), 
				Attribute.create(Float.class, "suppAcctBal"), 
				Attribute.create(Integer.class, "partKey"), 
				Attribute.create(Integer.class, "availQty")
		};
		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
		
		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;
		Assert.assertEquals(800000, result.size());
		target.close();
	}
	
	/*
	 * Plan: Projection(Join(Join(NATION_LESS, SUPPLIER_LESS), Join(PART_LESS, PARTSUPP_LESS)))
	 */
	@Test
	public void stressTestSql1a() {

		// Construct the target plan.
		JoinTerm leftChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(),TPCHelper.amFreeNation_less), 
				AccessTerm.create(TPCHelper.amFreeSupplier_less.getRelation(),TPCHelper.amFreeSupplier_less));
		JoinTerm rightChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreePart_less.getRelation(),TPCHelper.amFreePart_less), 
				AccessTerm.create(TPCHelper.amFreePartSupp_less.getRelation(),TPCHelper.amFreePartSupp_less));

		Attribute[] projectionAttributes = new Attribute[]{
				Attribute.create(Integer.class, "nationKey"), 
				Attribute.create(Integer.class, "suppKey"), 
				Attribute.create(Integer.class, "partKey")
		};
		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
		
		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;
		Assert.assertEquals(800000, result.size());
		target.close();
	}
	
//	/*
//	 * Plan: Projection(DependentJoin(DependentJoin(NATION, SUPPLIER), DependentJoin(PART, PARTSUPP)))
//	 */
//	@Test
//	public void stressTestSql2() {
//
//		// Construct the target plan.
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
//				AccessTerm.create(TPCHelper.am3Supplier.getRelation(),TPCHelper.am3Supplier));
//		DependentJoinTerm rightChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreePart.getRelation(),TPCHelper.amFreePart), 
//				AccessTerm.create(TPCHelper.am01PartSupp.getRelation(),TPCHelper.am01PartSupp));
//
//		Attribute[] projectionAttributes = new Attribute[]{
//				Attribute.create(Integer.class, "nationKey"), 
//				Attribute.create(Integer.class, "suppKey"), 
//				Attribute.create(Float.class, "suppAcctBal"), 
//				Attribute.create(Integer.class, "partKey"), 
//				Attribute.create(Integer.class, "availQty")
//		};
//		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, DependentJoinTerm.create(leftChild, rightChild)));
//		
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;
//		Assert.assertEquals(800000, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: Projection(DependentJoin(DependentJoin(NATION_LESS, SUPPLIER_LESS), DependentJoin(PART_LESS, PARTSUPP_LESS)))
//	 */
//	@Test
//	public void stressTestSql2a() {
//
//		// Construct the target plan.
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(),TPCHelper.amFreeNation_less), 
//				AccessTerm.create(TPCHelper.am3Supplier_less.getRelation(),TPCHelper.am3Supplier_less));
//		DependentJoinTerm rightChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreePart_less.getRelation(),TPCHelper.amFreePart_less), 
//				AccessTerm.create(TPCHelper.am01PartSupp_less.getRelation(),TPCHelper.am01PartSupp_less));
//
//		Attribute[] projectionAttributes = new Attribute[]{
//				Attribute.create(Integer.class, "nationKey"), 
//				Attribute.create(Integer.class, "suppKey"), 
//				Attribute.create(Integer.class, "partKey")
//		};
//		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, DependentJoinTerm.create(leftChild, rightChild)));
//		
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;
//		Assert.assertEquals(800000, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: Projection(Join(Join(NATION, SUPPLIER), Join(CUSTOMER, ORDERS)))
//	 */
//	@Test
//	public void stressTestSql3() {
//
//		// Construct the target plan.
//		JoinTerm leftChild = JoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
//				AccessTerm.create(TPCHelper.amFreeSupplier.getRelation(),TPCHelper.amFreeSupplier));
//		JoinTerm rightChild = JoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeCustomer.getRelation(),TPCHelper.amFreeCustomer), 
//				AccessTerm.create(TPCHelper.amFreeOrders.getRelation(),TPCHelper.amFreeOrders));
//
//		Attribute[] projectionAttributes = new Attribute[]{
//				Attribute.create(Integer.class, "nationKey"), 
//				Attribute.create(Integer.class, "suppKey"), 
//				Attribute.create(Integer.class, "custKey"), 
//				Attribute.create(Float.class, "custAcctBal"), 
//				Attribute.create(Integer.class, "orderKey"),
//				Attribute.create(String.class, "orderDate") 
//		};
//		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
//		
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
//		Assert.assertEquals(599959932, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: Projection(Join(Join(NATION_LESS, SUPPLIER_LESS), Join(CUSTOMER_LESS, ORDERS_LESS)))
//	 */
//	@Test
//	public void stressTestSql3a() {
//
//		// Construct the target plan.
//		JoinTerm leftChild = JoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(),TPCHelper.amFreeNation_less), 
//				AccessTerm.create(TPCHelper.amFreeSupplier_less.getRelation(),TPCHelper.amFreeSupplier_less));
//		JoinTerm rightChild = JoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeCustomer_less.getRelation(),TPCHelper.amFreeCustomer_less), 
//				AccessTerm.create(TPCHelper.amFreeOrders_less.getRelation(),TPCHelper.amFreeOrders_less));
//
//		Attribute[] projectionAttributes = new Attribute[]{
//				Attribute.create(Integer.class, "nationKey"), 
//				Attribute.create(Integer.class, "suppKey"), 
//				Attribute.create(Integer.class, "custKey"), 
//				Attribute.create(Integer.class, "orderKey")
//		};
//		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
//		
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
//		Assert.assertEquals(599959932, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: Projection(DependentJoin(DependentJoin(NATION, SUPPLIER), DependentJoin(CUSTOMER, ORDERS)))
//	 */
//	@Test
//	public void stressTestSql4() {
//
//		// Construct the target plan.
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
//				AccessTerm.create(TPCHelper.am3Supplier.getRelation(),TPCHelper.am3Supplier));
//		DependentJoinTerm rightChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.am3Customer.getRelation(),TPCHelper.am3Customer), 
//				AccessTerm.create(TPCHelper.am1Orders.getRelation(),TPCHelper.am1Orders));
//
//		Attribute[] projectionAttributes = new Attribute[]{
//				Attribute.create(Integer.class, "nationKey"), 
//				Attribute.create(Integer.class, "suppKey"), 
//				Attribute.create(Integer.class, "custKey"), 
//				Attribute.create(Float.class, "custAcctBal"), 
//				Attribute.create(Integer.class, "orderKey"),
//				Attribute.create(String.class, "orderDate") 
//		};
//		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, DependentJoinTerm.create(leftChild, rightChild)));
//		
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
//		Assert.assertEquals(599959932, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: Projection(DependentJoin(DependentJoin(NATION_LESS, SUPPLIER_LESS), DependentJoin(CUSTOMER_LESS, ORDERS_LESS)))
//	 */
//	@Test
//	public void stressTestSql4a() {
//
//		// Construct the target plan.
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(),TPCHelper.amFreeNation_less), 
//				AccessTerm.create(TPCHelper.am3Supplier_less.getRelation(),TPCHelper.am3Supplier_less));
//		DependentJoinTerm rightChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.am3Customer_less.getRelation(),TPCHelper.am3Customer_less), 
//				AccessTerm.create(TPCHelper.am1Orders_less.getRelation(),TPCHelper.am1Orders_less));
//
//		Attribute[] projectionAttributes = new Attribute[]{
//				Attribute.create(Integer.class, "nationKey"), 
//				Attribute.create(Integer.class, "suppKey"), 
//				Attribute.create(Integer.class, "custKey"), 
//				Attribute.create(Integer.class, "orderKey")
//		};
//		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, DependentJoinTerm.create(leftChild, rightChild)));
//		
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
//		Assert.assertEquals(599959932, result.size());
//		target.close();
//	}

	/*
	 * Plan: Projection(Join(Join(NATION, SUPPLIER), Join(PART, PARTSUPP)))
	 */
	@Test
	public void stressTestSql5() {

		// Construct the target plan.
		JoinTerm leftChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
				AccessTerm.create(TPCHelper.amFreeSupplier.getRelation(),TPCHelper.amFreeSupplier));
		JoinTerm rightChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreePart.getRelation(),TPCHelper.amFreePart), 
				AccessTerm.create(TPCHelper.amFreePartSupp.getRelation(),TPCHelper.amFreePartSupp));

		Attribute[] projectionAttributes = new Attribute[]{
				Attribute.create(Integer.class, "nationKey"), 
				Attribute.create(Integer.class, "suppKey"), 
				Attribute.create(Float.class, "suppAcctBal"), 
				Attribute.create(Integer.class, "partKey"), 
				Attribute.create(Integer.class, "availQty")
		};
		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
		
		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;
		Assert.assertEquals(800000, result.size());
		target.close();
	}
	
	/*
	 * Plan: Projection(Join(Join(NATION_LESS, SUPPLIER_LESS), Join(PART_LESS, PARTSUPP_LESS)))
	 */
	@Test
	public void stressTestSql5a() {

		// Construct the target plan.
		JoinTerm leftChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(),TPCHelper.amFreeNation_less), 
				AccessTerm.create(TPCHelper.amFreeSupplier_less.getRelation(),TPCHelper.amFreeSupplier_less));
		JoinTerm rightChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreePart_less.getRelation(),TPCHelper.amFreePart_less), 
				AccessTerm.create(TPCHelper.amFreePartSupp_less.getRelation(),TPCHelper.amFreePartSupp_less));

		Attribute[] projectionAttributes = new Attribute[]{
				Attribute.create(Integer.class, "nationKey"), 
				Attribute.create(Integer.class, "suppKey"), 
				Attribute.create(Integer.class, "partKey")
		};
		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
		
		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;
		Assert.assertEquals(800000, result.size());
		target.close();
	}
	
	/*
	 * Plan: Projection(Join(DependentJoin(NATION, SUPPLIER), DependentJoin(PART, PARTSUPP)))
	 */
	@Test
	public void stressTestSql6() {

		// Construct the target plan.
		DependentJoinTerm leftChild = DependentJoinTerm.create(
				AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
				AccessTerm.create(TPCHelper.am3Supplier.getRelation(),TPCHelper.am3Supplier));
		DependentJoinTerm rightChild = DependentJoinTerm.create(
				AccessTerm.create(TPCHelper.amFreePart.getRelation(),TPCHelper.amFreePart), 
				AccessTerm.create(TPCHelper.am0PartSupp.getRelation(),TPCHelper.am0PartSupp));

		Attribute[] projectionAttributes = new Attribute[]{
				Attribute.create(Integer.class, "nationKey"), 
				Attribute.create(Integer.class, "suppKey"), 
				Attribute.create(Float.class, "suppAcctBal"), 
				Attribute.create(Integer.class, "partKey"), 
				Attribute.create(Integer.class, "availQty")
		};
		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
		
		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;
		Assert.assertEquals(800000, result.size());
		target.close();
	}

	/*
	 * Plan: Projection(Join(DependentJoin(NATION_LESS, SUPPLIER_LESS), DependentJoin(PART_LESS, PARTSUPP_LESS)))
	 */
	@Test
	public void stressTestSql6a() {

		// Construct the target plan.
		DependentJoinTerm leftChild = DependentJoinTerm.create(
				AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(),TPCHelper.amFreeNation_less), 
				AccessTerm.create(TPCHelper.am3Supplier_less.getRelation(),TPCHelper.am3Supplier_less));
		DependentJoinTerm rightChild = DependentJoinTerm.create(
				AccessTerm.create(TPCHelper.amFreePart_less.getRelation(),TPCHelper.amFreePart_less), 
				AccessTerm.create(TPCHelper.am0PartSupp_less.getRelation(),TPCHelper.am0PartSupp_less));

		Attribute[] projectionAttributes = new Attribute[]{
				Attribute.create(Integer.class, "nationKey"), 
				Attribute.create(Integer.class, "suppKey"), 
				Attribute.create(Integer.class, "partKey")
		};
		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
		
		// Execute the plan. 
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;
		Assert.assertEquals(800000, result.size());
		target.close();
	}

//	/*
//	 * Plan: Projection(Join(Join(NATION, SUPPLIER), Join(CUSTOMER, ORDERS)))
//	 */
//	@Test
//	public void stressTestSql7() {
//
//		// Construct the target plan.
//		JoinTerm leftChild = JoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
//				AccessTerm.create(TPCHelper.amFreeSupplier.getRelation(),TPCHelper.amFreeSupplier));
//		JoinTerm rightChild = JoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeCustomer.getRelation(),TPCHelper.amFreeCustomer), 
//				AccessTerm.create(TPCHelper.amFreeOrders.getRelation(),TPCHelper.amFreeOrders));
//
//		Attribute[] projectionAttributes = new Attribute[]{
//				Attribute.create(Integer.class, "nationKey"), 
//				Attribute.create(Integer.class, "suppKey"), 
//				Attribute.create(Integer.class, "custKey"), 
//				Attribute.create(Float.class, "custAcctBal"), 
//				Attribute.create(Integer.class, "orderKey"),
//				Attribute.create(String.class, "orderDate") 
//		};
//		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
//		
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
//		Assert.assertEquals(599959932, result.size());
//		target.close();
//	}
//
//	/*
//	 * Plan: Projection(Join(Join(NATION_LESS, SUPPLIER_LESS), Join(CUSTOMER_LESS, ORDERS_LESS)))
//	 */
//	@Test
//	public void stressTestSql7a() {
//
//		// Construct the target plan.
//		JoinTerm leftChild = JoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(),TPCHelper.amFreeNation_less), 
//				AccessTerm.create(TPCHelper.amFreeSupplier_less.getRelation(),TPCHelper.amFreeSupplier_less));
//		JoinTerm rightChild = JoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeCustomer_less.getRelation(),TPCHelper.amFreeCustomer_less), 
//				AccessTerm.create(TPCHelper.amFreeOrders_less.getRelation(),TPCHelper.amFreeOrders_less));
//
//		Attribute[] projectionAttributes = new Attribute[]{
//				Attribute.create(Integer.class, "nationKey"), 
//				Attribute.create(Integer.class, "suppKey"), 
//				Attribute.create(Integer.class, "custKey"), 
//				Attribute.create(Integer.class, "orderKey")
//		};
//		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
//		
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
//		Assert.assertEquals(599959932, result.size());
//		target.close();
//	}

//	/*
//	 * Plan: Projection(Join(DependentJoin(NATION, SUPPLIER), DependentJoin(CUSTOMER, ORDERS)))
//	 */
//	@Test
//	public void stressTestSql8() {
//
//		// Construct the target plan.
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation.getRelation(),TPCHelper.amFreeNation), 
//				AccessTerm.create(TPCHelper.am3Supplier.getRelation(),TPCHelper.am3Supplier));
//		DependentJoinTerm rightChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeCustomer.getRelation(),TPCHelper.amFreeCustomer), 
//				AccessTerm.create(TPCHelper.am1Orders.getRelation(),TPCHelper.am1Orders));
//
//		Attribute[] projectionAttributes = new Attribute[]{
//				Attribute.create(Integer.class, "nationKey"), 
//				Attribute.create(Integer.class, "suppKey"), 
//				Attribute.create(Integer.class, "custKey"), 
//				Attribute.create(Float.class, "custAcctBal"), 
//				Attribute.create(Integer.class, "orderKey"),
//				Attribute.create(String.class, "orderDate") 
//		};
//		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
//		
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
//		Assert.assertEquals(599959932, result.size());
//		target.close();
//	}
//	
//	/*
//	 * Plan: Projection(Join(DependentJoin(NATION_LESS, SUPPLIER_LESS), DependentJoin(CUSTOMER_LESS, ORDERS_LESS)))
//	 */
//	@Test
//	public void stressTestSql8a() {
//
//		// Construct the target plan.
//		DependentJoinTerm leftChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(),TPCHelper.amFreeNation_less), 
//				AccessTerm.create(TPCHelper.am3Supplier_less.getRelation(),TPCHelper.am3Supplier_less));
//		DependentJoinTerm rightChild = DependentJoinTerm.create(
//				AccessTerm.create(TPCHelper.amFreeCustomer_less.getRelation(),TPCHelper.amFreeCustomer_less), 
//				AccessTerm.create(TPCHelper.am1Orders_less.getRelation(),TPCHelper.am1Orders_less));
//
//		Attribute[] projectionAttributes = new Attribute[]{
//				Attribute.create(Integer.class, "nationKey"), 
//				Attribute.create(Integer.class, "suppKey"), 
//				Attribute.create(Integer.class, "custKey"), 
//				Attribute.create(Float.class, "custAcctBal"), 
//				Attribute.create(Integer.class, "orderKey")
//		};
//		Projection target = new Projection(ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)));
//		
//		// Execute the plan. 
//		List<Tuple> result = target.stream().collect(Collectors.toList());
//
//		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY = SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM CUSTOMER, ORDERS WHERE CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
//		Assert.assertEquals(599959932, result.size());
//		target.close();
//	}
}
