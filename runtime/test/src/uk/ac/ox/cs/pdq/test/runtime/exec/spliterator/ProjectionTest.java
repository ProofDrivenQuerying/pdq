package uk.ac.ox.cs.pdq.test.runtime.exec.spliterator;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantInequalityCondition;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.datasources.sql.SqlAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Projection;

public class ProjectionTest {
	PlanDecorator decor = null;

	@Before
	public void setup() throws JAXBException {
		decor = new PlanDecorator(AccessRepository.getRepository());
	}

	@Test
	public void testGetProjectionFunction() throws Exception {
		Relation relation = Mockito.mock(Relation.class);
		Attribute[] relationAttributes = new Attribute[] { Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c") };
		when(relation.getAttributes()).thenReturn(relationAttributes.clone());

		Attribute[] amAttributes = new Attribute[] { Attribute.create(String.class, "W"),
				Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "Y"),
				Attribute.create(Integer.class, "Z") };

		Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
		attributeMapping.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
		attributeMapping.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
		attributeMapping.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes, new Integer[0], relation,
				attributeMapping);

		Attribute[] projectionAttributes = new Attribute[] { Attribute.create(Integer.class, "b") };
		ProjectionTerm plan = ProjectionTerm.create(projectionAttributes,
				AccessTerm.create(amFree.getRelation(), amFree));

		Projection target = new Projection(plan, decor);

		Function<Tuple, Tuple> result = target.getProjectionFunction();

		TupleType ttIntegerIntegerString = TupleType.DefaultFactory.create(Integer.class, Integer.class, String.class);
		Tuple tuple = ttIntegerIntegerString.createTuple(10, 11, "x");

		TupleType ttInteger = TupleType.DefaultFactory.create(Integer.class);

		Assert.assertEquals(result.apply(tuple), ttInteger.createTuple(11));

		// Now test with multiple projection attributes.
		projectionAttributes = new Attribute[] { Attribute.create(String.class, "c"),
				Attribute.create(Integer.class, "a") };
		plan = ProjectionTerm.create(projectionAttributes, AccessTerm.create(amFree.getRelation(), amFree));
		target.close();

		target = new Projection(plan, decor);

		result = target.getProjectionFunction();

		TupleType tStringInteger = TupleType.DefaultFactory.create(String.class, Integer.class);

		Assert.assertEquals(result.apply(tuple), tStringInteger.createTuple("x", 10));
		target.close();
	}

	/*
	 * The following are integration tests: Projection plans are constructed &
	 * executed.
	 */

	@Test
	public void integrationTestInMemory1() throws Exception {

		Attribute[] projectionAttributes;
		ProjectionTerm plan;
		Projection target;
		List<Tuple> result;

		Relation relation = Mockito.mock(Relation.class);
		Attribute[] relationAttributes = new Attribute[] { Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c") };
		when(relation.getAttributes()).thenReturn(relationAttributes.clone());

		Attribute[] amAttributes = new Attribute[] { Attribute.create(String.class, "W"),
				Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "Y"),
				Attribute.create(Integer.class, "Z") };

		Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
		attributeMapping.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
		attributeMapping.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
		attributeMapping.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

		/*
		 * Plan: free access and projection onto column "b".
		 */
		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes, new Integer[0], relation,
				attributeMapping);
		projectionAttributes = new Attribute[] { Attribute.create(Integer.class, "b") };
		plan = ProjectionTerm.create(projectionAttributes, AccessTerm.create(amFree.getRelation(), amFree));

		target = new Projection(plan, decor);

		// Confirm that there are no input attributes.
		Assert.assertEquals(0, target.getInputAttributes().length);

		TupleType ttStringIntegerIntegerInteger = TupleType.DefaultFactory.create(String.class, Integer.class,
				Integer.class, Integer.class);
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
		projectionAttributes = new Attribute[] { Attribute.create(Integer.class, "b"),
				Attribute.create(String.class, "c") };
		plan = ProjectionTerm.create(projectionAttributes, AccessTerm.create(amFree.getRelation(), amFree));

		target = new Projection(plan, decor);

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
	public void integrationTestInMemory2() throws Exception {

		Attribute[] projectionAttributes;
		ProjectionTerm plan;
		Projection target;
		List<Tuple> result;

		Relation relation = Mockito.mock(Relation.class);
		Attribute[] relationAttributes = new Attribute[] { Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c") };
		when(relation.getAttributes()).thenReturn(relationAttributes.clone());

		Attribute[] amAttributes = new Attribute[] { Attribute.create(String.class, "W"),
				Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "Y"),
				Attribute.create(Integer.class, "Z") };

		Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
		attributeMapping.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
		attributeMapping.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
		attributeMapping.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

		/*
		 * Plan: Access with inputs on attribute "a" on and projection onto columns
		 * ("b", "c").
		 */
		Integer[] inputs = new Integer[] { 1 };
		InMemoryAccessMethod am12 = new InMemoryAccessMethod(amAttributes, inputs, relation, attributeMapping);

		projectionAttributes = new Attribute[] { Attribute.create(Integer.class, "b"),
				Attribute.create(String.class, "c") };
		plan = ProjectionTerm.create(projectionAttributes, AccessTerm.create(am12.getRelation(), am12));

		target = new Projection(plan, decor);

		// Confirm that there is one input attribute, "a".
		Assert.assertArrayEquals(new Attribute[] { Attribute.create(Integer.class, "a") }, target.getInputAttributes());

		TupleType ttStringIntegerIntegerInteger = TupleType.DefaultFactory.create(String.class, Integer.class,
				Integer.class, Integer.class);
		Collection<Tuple> tuples = new ArrayList<Tuple>();
		tuples.add(ttStringIntegerIntegerInteger.createTuple("w", 10, 11, 12));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("x", 20, 21, 22));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("y", 30, 31, 32));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("z", 40, 41, 42));
		tuples.add(ttStringIntegerIntegerInteger.createTuple("a", 10, 51, 52));
		am12.load(tuples);

		// Attempting to execute the plan before setting the dynamic input raises an
		// exception.
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
	public void integrationTestInMemory3() throws Exception {

		Attribute[] projectionAttributes;
		ProjectionTerm plan;
		Projection target;
		List<Tuple> result;

		Relation relation = Mockito.mock(Relation.class);
		Attribute[] relationAttributes = new Attribute[] { Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c") };
		when(relation.getAttributes()).thenReturn(relationAttributes.clone());

		Attribute[] amAttributes = new Attribute[] { Attribute.create(String.class, "W"),
				Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "Y"),
				Attribute.create(Integer.class, "Z") };

		Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
		attributeMapping.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
		attributeMapping.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
		attributeMapping.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

		/*
		 * Plan: Free access, then select the rows where the value of attribute "a" is
		 * greater than the constant value 10 and projection onto columns ("b", "c").
		 */
		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes, new Integer[0], relation,
				attributeMapping);
		Condition condition = ConstantInequalityCondition.create(0, TypedConstant.create(10), false);
		projectionAttributes = new Attribute[] { Attribute.create(Integer.class, "b"),
				Attribute.create(String.class, "c") };
		plan = ProjectionTerm.create(projectionAttributes,
				SelectionTerm.create(condition, AccessTerm.create(amFree.getRelation(), amFree)));

		target = new Projection(plan, decor);

		TupleType ttStringIntegerIntegerInteger = TupleType.DefaultFactory.create(String.class, Integer.class,
				Integer.class, Integer.class);
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
	public void integrationTestSql1() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_customer.clone());
		when(relation.getName()).thenReturn("CUSTOMER");

		Integer[] inputs = new Integer[0];
		ExecutableAccessMethod amFree = new SqlAccessMethod("CUSTOMER", TPCHelper.attrs_C, inputs, relation,
				TPCHelper.attrMap_customer, TPCHelper.getProperties());

		/*
		 * Plan: free access on the sqlRelationCustomer relation then select the rows
		 * where the value of the C_NATIONKEY column is 23 (United Kingdom) and finally
		 * project the C_NAME and C_ACCTBAL columns.
		 */
		Attribute[] projectionAttributes = new Attribute[] { Attribute.create(String.class, "custName"),
				Attribute.create(Float.class, "custAcctBal") };

		Condition condition = ConstantEqualityCondition.create(4, TypedConstant.create(23));
		RelationalTerm child = SelectionTerm.create(condition, AccessTerm.create(amFree.getRelation(), amFree));
		ProjectionTerm plan = ProjectionTerm.create(projectionAttributes, child);

		Projection target = new Projection(plan, decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// Check that the result tuples are the ones expected.
		Assert.assertTrue(result.stream().allMatch((t) -> t.size() == 2));

		// Test the result of the query:
		// SELECT COUNT(*) FROM CUSTOMER WHERE c_nationkey=23;
		Assert.assertEquals(6011, result.size());

		// Check the first account balance in the result against the results of the
		// following query:
		// SELECT c_name, c_acctbal FROM CUSTOMER WHERE c_nationkey=23;
		Assert.assertEquals(-272.60f, ((Float) result.get(0).getValue(1)).floatValue(), 0.0f);
		target.close();
	}

	/*
	 * Plan: Projection(Access) with dynamic input.
	 */
	@Test
	public void integrationTestSql2() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_customer.clone());
		when(relation.getName()).thenReturn("CUSTOMER");

		Integer[] inputs = new Integer[] { 3 };
		ExecutableAccessMethod am3 = new SqlAccessMethod("CUSTOMER", TPCHelper.attrs_C, inputs, relation,
				TPCHelper.attrMap_customer, TPCHelper.getProperties());

		/*
		 * Plan: access on the sqlRelationCustomer relation with dynamic input on
		 * attribute "C_NATIONKEY" and then project onto the custName and custAcctBal
		 * columns.
		 */

		Attribute[] projectionAttributes = new Attribute[] { Attribute.create(String.class, "custName"),
				Attribute.create(Float.class, "custAcctBal") };

		RelationalTerm child = AccessTerm.create(am3.getRelation(), am3);
		ProjectionTerm plan = ProjectionTerm.create(projectionAttributes, child);

		Projection target = new Projection(plan, decor);

		// Attempting to execute the plan before setting the dynamic input raises an
		// exception.
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
	public void stressTestSql1() throws Exception {

		// Construct the target plan.
		JoinTerm leftChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreeNation.getRelation(), TPCHelper.amFreeNation),
				AccessTerm.create(TPCHelper.amFreeSupplier.getRelation(), TPCHelper.amFreeSupplier));
		JoinTerm rightChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreePart.getRelation(), TPCHelper.amFreePart),
				AccessTerm.create(TPCHelper.amFreePartSupp.getRelation(), TPCHelper.amFreePartSupp));

		Attribute[] projectionAttributes = new Attribute[] { Attribute.create(Integer.class, "nationKey"),
				Attribute.create(Integer.class, "suppKey"), Attribute.create(Float.class, "suppAcctBal"),
				Attribute.create(Integer.class, "partKey"), Attribute.create(Integer.class, "availQty") };
		Projection target = new Projection(
				ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)), decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY =
		// SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE
		// PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child,
		// right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;
		Assert.assertEquals(800001, result.size());
		target.close();
	}

	/*
	 * Plan: Projection(Join(Join(NATION_LESS, SUPPLIER_LESS), Join(PART_LESS,
	 * PARTSUPP_LESS)))
	 */
	// @Test nation_less is not part of the default tcph database
	public void stressTestSql1a() throws Exception {

		// Construct the target plan.
		JoinTerm leftChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreeNation_less.getRelation(), TPCHelper.amFreeNation_less),
				AccessTerm.create(TPCHelper.amFreeSupplier_less.getRelation(), TPCHelper.amFreeSupplier_less));
		JoinTerm rightChild = JoinTerm.create(
				AccessTerm.create(TPCHelper.amFreePart_less.getRelation(), TPCHelper.amFreePart_less),
				AccessTerm.create(TPCHelper.amFreePartSupp_less.getRelation(), TPCHelper.amFreePartSupp_less));

		Attribute[] projectionAttributes = new Attribute[] { Attribute.create(Integer.class, "nationKey"),
				Attribute.create(Integer.class, "suppKey"), Attribute.create(Integer.class, "partKey") };
		Projection target = new Projection(
				ProjectionTerm.create(projectionAttributes, JoinTerm.create(leftChild, rightChild)), decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// WITH left_child AS (SELECT * FROM NATION, SUPPLIER WHERE NATION.N_NATIONKEY =
		// SUPPLIER.S_NATIONKEY), right_child AS (SELECT * FROM PART, PARTSUPP WHERE
		// PART.P_PARTKEY = PARTSUPP.PS_PARTKEY) SELECT count(*) FROM left_child,
		// right_child WHERE left_child.s_suppkey = right_child.ps_suppkey;
		Assert.assertEquals(800000, result.size());
		target.close();
	}

}
