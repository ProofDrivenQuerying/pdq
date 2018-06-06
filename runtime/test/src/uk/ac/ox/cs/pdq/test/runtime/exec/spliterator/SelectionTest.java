package uk.ac.ox.cs.pdq.test.runtime.exec.spliterator;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.datasources.sql.SqlAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Selection;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

public class SelectionTest {
	PlanDecorator decor = null;

	@Before
	public void setup() throws JAXBException {
		decor = new PlanDecorator(AccessRepository.getRepository());
	}

	/*
	 * The following are integration tests: Selection plans are constructed &
	 * executed.
	 */

	@Test
	public void integrationTestInMemory1() throws Exception {
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

		/*
		 * Plan: free access then select the rows where the value of attribute "a" is
		 * equal to the constant value 10.
		 */
		Condition condition = ConstantEqualityCondition.create(0, TypedConstant.create(10));
		Plan plan = SelectionTerm.create(condition, AccessTerm.create(amFree.getRelation(), amFree));
		Selection target = new Selection(plan, decor);

		// Create some tuples
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
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// Check that the result tuples are the ones expected. Here all of the
		// tuples meet the constant equality condition (i.e. 10 in the zero'th
		// position).
		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());
		Assert.assertTrue(result.stream().allMatch(t -> t.size() == 3));
		Assert.assertEquals((Integer) 10, (Integer) result.get(0).getValue(0));
		Assert.assertEquals((Integer) 10, (Integer) result.get(1).getValue(0));

		Assert.assertEquals((Integer) 11, (Integer) result.get(0).getValue(1));
		Assert.assertEquals((Integer) 51, (Integer) result.get(1).getValue(1));

		Assert.assertEquals("w", result.get(0).getValue(2));
		Assert.assertEquals("a", result.get(1).getValue(2));
		target.close();

		// Test the behaviour if we construct another Selection instance using the same
		// SelectionTerm.
		// This demonstrates that no 'reset' method is necessary.
		target = new Selection(plan, decor);

		// Execute the plan.
		List<Tuple> newResult = target.stream().collect(Collectors.toList());

		Assert.assertNotNull(newResult);
		Assert.assertEquals(2, newResult.size());
		Assert.assertTrue(newResult.stream().allMatch(t -> t.size() == 3));
		Assert.assertEquals((Integer) 10, (Integer) newResult.get(0).getValue(0));
		Assert.assertEquals((Integer) 10, (Integer) newResult.get(1).getValue(0));

		Assert.assertEquals((Integer) 11, (Integer) newResult.get(0).getValue(1));
		Assert.assertEquals((Integer) 51, (Integer) newResult.get(1).getValue(1));

		Assert.assertEquals("w", newResult.get(0).getValue(2));
		Assert.assertEquals("a", newResult.get(1).getValue(2));
		target.close();

		// Test re-executing the plan (this time using the execute method).
		Assert.assertEquals(2, target.execute().size());
		Assert.assertEquals(2, target.execute().size());
	}

	@Test
	public void integrationTestInMemory2() throws Exception {

		// Relation relation = Mockito.mock(Relation.class);
		Attribute[] relationAttributes = new Attribute[] { Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c") };
		// when(relation.getAttributes()).thenReturn(relationAttributes.clone());
		Relation relation = Relation.create("Test", relationAttributes);

		Attribute[] amAttributes = new Attribute[] { Attribute.create(String.class, "W"),
				Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "Y"),
				Attribute.create(Integer.class, "Z") };

		Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
		attributeMapping.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
		attributeMapping.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
		attributeMapping.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

		InMemoryAccessMethod amFree = new InMemoryAccessMethod(amAttributes, new Integer[0], relation,
				attributeMapping);

		/*
		 * Plan: free access then select the rows where the value of attribute "a" is
		 * greater than the constant value 10, then select the rows where the value of
		 * attribute "b" is less than 40.
		 */
		Condition conditionA = ConstantInequalityCondition.create(0, TypedConstant.create(10), false);
		Condition conditionB = ConstantInequalityCondition.create(0, TypedConstant.create(40), true);

		Plan plan = SelectionTerm.create(conditionB,
				SelectionTerm.create(conditionA, AccessTerm.create(amFree.getRelation(), amFree)));
		Selection target = new Selection(plan, decor);

		// Create some tuples
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
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// Check that the result tuples are the ones expected. Here all of the
		// tuples meet the constant equality condition (i.e. 10 in the zero'th
		// position).
		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());
		Assert.assertTrue(result.stream().allMatch(t -> t.size() == 3));
		Assert.assertEquals((Integer) 20, (Integer) result.get(0).getValue(0));
		Assert.assertEquals((Integer) 30, (Integer) result.get(1).getValue(0));

		Assert.assertEquals((Integer) 21, (Integer) result.get(0).getValue(1));
		Assert.assertEquals((Integer) 31, (Integer) result.get(1).getValue(1));

		Assert.assertEquals("x", result.get(0).getValue(2));
		Assert.assertEquals("y", result.get(1).getValue(2));
		target.close();
	}

	@Test
	public void integrationTestSql1() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
		attributeMapping.put(Attribute.create(Integer.class, "N_NATIONKEY"),
				Attribute.create(Integer.class, "nationKey"));
		attributeMapping.put(Attribute.create(String.class, "N_NAME"), Attribute.create(String.class, "name"));
		attributeMapping.put(Attribute.create(Integer.class, "N_REGIONKEY"),
				Attribute.create(Integer.class, "regionKey"));

		Integer[] inputs = new Integer[0];
		ExecutableAccessMethod amFree = new SqlAccessMethod("NATION", TPCHelper.attrs_N, inputs, relation,
				attributeMapping, TPCHelper.getProperties());

		/*
		 * Plan: free access on relation NATION then select the rows where the N_NAME
		 * attribute is equal to the constant 'BRAZIL'.
		 */
		Condition condition = ConstantEqualityCondition.create(0, TypedConstant.create("BRAZIL"));
		Plan plan = SelectionTerm.create(condition, AccessTerm.create(amFree.getRelation(), amFree));
		Selection target = new Selection(plan, decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Assert.assertEquals(1, result.size());

		// Check BRAZIL's N_NATIONKEY attribute (i.e. primary key) value, which is 2.
		// SELECT N_NATIONKEY FROM NATION WHERE N_NAME='BRAZIL';
		Assert.assertEquals(2, result.get(0).getValues()[1]);
		target.close();
	}

	@Test
	public void integrationTestSql2() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		Integer[] inputs = new Integer[0];
		ExecutableAccessMethod amFree = new SqlAccessMethod("NATION", TPCHelper.attrs_N, inputs, relation,
				TPCHelper.attrMap_nation, TPCHelper.getProperties());

		/*
		 * Plan: free access on relation NATION then select the rows where the value of
		 * the N_REGIONKEY attribute is equal to the constant 2.
		 */
		Condition condition = ConstantEqualityCondition.create(2, TypedConstant.create(2));
		Plan plan = SelectionTerm.create(condition, AccessTerm.create(amFree.getRelation(), amFree));
		Selection target = new Selection(plan, decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// Verify the SQL query:
		// SELECT COUNT(*) FROM NATION WHERE n_regionkey=2;
		Assert.assertEquals(5, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql3() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_customer.clone());

		Set<Attribute> inputAttributes = new HashSet<Attribute>();
		inputAttributes.add(Attribute.create(Integer.class, "C_NATIONKEY"));

		ExecutableAccessMethod am4 = new SqlAccessMethod("CUSTOMER", TPCHelper.attrs_C, inputAttributes, relation,
				TPCHelper.attrMap_customer, TPCHelper.getProperties());

		/*
		 * Plan: access on relation CUSTOMER with dynamic input on the C_NATIONKEY
		 * attribute, then select the rows where the value of of C_MKTSEGMENT is
		 * "MACHINERY".
		 */
		Condition condition = ConstantEqualityCondition.create(3, TypedConstant.create("MACHINERY"));
		Plan plan = SelectionTerm.create(condition, AccessTerm.create(am4.getRelation(), am4));
		Selection target = new Selection(plan, decor);

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
		dynamicInput.add(ttInteger.createTuple(20));
		dynamicInput.add(ttInteger.createTuple(21));
		dynamicInput.add(ttInteger.createTuple(22));
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// Verify the SQL query:
		// SELECT COUNT(*) FROM CUSTOMER WHERE c_mktsegment='MACHINERY' AND c_nationkey
		// IN (20, 21, 22);
		Assert.assertEquals(3546, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql4() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_customer.clone());

		Set<Attribute> inputAttributes = new HashSet<Attribute>();
		inputAttributes.add(Attribute.create(Integer.class, "C_NATIONKEY"));

		ExecutableAccessMethod am4 = new SqlAccessMethod("CUSTOMER", TPCHelper.attrs_C, inputAttributes, relation,
				TPCHelper.attrMap_customer, TPCHelper.getProperties());

		/*
		 * Plan: Selection(Selection(Access))
		 * 
		 * Access on relation CUSTOMER with dynamic input on the C_NATIONKEY attribute,
		 * then select the rows where the value of of C_MKTSEGMENT is "MACHINERY", then
		 * select the rows where the value of of C_ACCTBAL is less than 0.
		 */
		Condition mktSegmentCondition = ConstantEqualityCondition.create(3, TypedConstant.create("MACHINERY"));
		Condition acctBalCondition = ConstantInequalityCondition.create(2, TypedConstant.create(0.0f));

		Plan plan = SelectionTerm.create(acctBalCondition,
				SelectionTerm.create(mktSegmentCondition, AccessTerm.create(am4.getRelation(), am4)));
		Selection target = new Selection(plan, decor);

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
		dynamicInput.add(ttInteger.createTuple(20));
		dynamicInput.add(ttInteger.createTuple(21));
		dynamicInput.add(ttInteger.createTuple(22));
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// Verify the SQL query:
		// SELECT COUNT(*) FROM CUSTOMER WHERE c_mktsegment='MACHINERY' AND c_nationkey
		// IN (20, 21, 22) AND c_acctbal < 0;
		Assert.assertEquals(302, result.size());
		target.close();
	}

}
