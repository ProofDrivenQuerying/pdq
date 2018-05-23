package uk.ac.ox.cs.pdq.test.runtime.exec.spliterator;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.datasources.sql.SqlAccessMethod;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Access;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

public class AccessTest {
	PlanDecorator decor = null;
	TupleType tt = TupleType.DefaultFactory.create(String.class, Integer.class, Integer.class, Integer.class);

	/*
	 * The following are integration tests: Access plans are constructed & executed.
	 */

	@Before
	public void setup() throws JAXBException {
		decor = new PlanDecorator(AccessRepository.getRepository());
	}

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

		// Create some tuples
		List<Tuple> tuples = new ArrayList<Tuple>();
		int N = 4;
		for (int i = 0; i != N; i++) {
			tuples.add(tt.createTuple("x", 10, 11, 12));
		}
		amFree.load(tuples);

		/*
		 * Plan: Free access.
		 */
		Access target = new Access(AccessTerm.create(amFree.getRelation(), amFree), decor);

		// Here we demonstrate the two plan execution methods:

		// 1. using the execute method (also test re-usability).
		Assert.assertEquals(N, target.execute().size());
		Assert.assertEquals(N, target.execute().size());

		// 2. streaming and collecting in a List
		// (requires closing the executable plan else we get a "resource leak" warning).
		List<Tuple> result = target.stream().collect(Collectors.toList());
		Assert.assertEquals(N, result.size());
		target.close();
	}

	@Test
	public void integrationTestInMemory2() throws Exception {

		Attribute[] relationAttributes = new Attribute[] { Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c") };
		Relation relation = Relation.create("R1", relationAttributes);

		Attribute[] amAttributes = new Attribute[] { Attribute.create(String.class, "W"),
				Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "Y"),
				Attribute.create(Integer.class, "Z") };

		Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
		attributeMapping.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
		attributeMapping.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
		attributeMapping.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

		InMemoryAccessMethod am1 = new InMemoryAccessMethod(amAttributes, new Integer[] { 2 }, relation,
				attributeMapping);

		/*
		 * Plan: Access with constant input on attribute "b".
		 */
		// Map<Attribute, TypedConstant> inputConstants = new HashMap<>();
		// inputConstants.put(Attribute.create(Integer.class, "b"),
		// TypedConstant.create(101));
		// Access target = new Access(AccessTerm.create(am1, inputConstants));
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(1, TypedConstant.create(101));

		Access target = new Access(AccessTerm.create(am1.getRelation(), am1, inputConstants), decor);

		// Create some tuples
		List<Tuple> tuples = new ArrayList<Tuple>();
		int N = 10;
		for (int i = 0; i != N; i++) {
			if ((i % 2) == 0)
				tuples.add(tt.createTuple("x", 100, 101, 102));
			else
				tuples.add(tt.createTuple("y", 10, 11, 12));
		}

		Assert.assertEquals(0, am1.getData().size());
		am1.load(tuples);
		Assert.assertEquals(N, am1.getData().size());

		// "Execute" the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Assert.assertEquals(N / 2, result.size());

		// Check that all of the values in the 1st position match the input constant
		// value.
		Assert.assertTrue(result.stream().allMatch(t -> ((Integer) t.getValue(1)).equals(101)));

		// Test re-executing the plan (this time using the execute method).
		Table resultTable = target.execute();
		Assert.assertEquals(N / 2, resultTable.size());
		target.close();
	}

	@Test
	public void integrationTestInMemory3() throws Exception {

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

		InMemoryAccessMethod am1 = new InMemoryAccessMethod(amAttributes, new Integer[] { 2 }, relation,
				attributeMapping);

		/*
		 * Plan: Access with dynamic input on attribute "b".
		 */
		// Access target = new Access(AccessTerm.create(am1));
		Access target = new Access(AccessTerm.create(am1.getRelation(), am1), decor);

		// Create some dynamic input.
		TupleType ttInteger = TupleType.DefaultFactory.create(Integer.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(ttInteger.createTuple(101));

		// Create some tuples
		List<Tuple> tuples = new ArrayList<Tuple>();
		int N = 10;
		for (int i = 0; i != N; i++) {
			if ((i % 2) == 0)
				tuples.add(tt.createTuple("x", 100, 101, 102));
			else
				tuples.add(tt.createTuple("y", 10, 11, 12));
		}

		Assert.assertEquals(0, am1.getData().size());
		am1.load(tuples);
		Assert.assertEquals(N, am1.getData().size());

		// Attempting to execute the plan before setting the dynamic input raises an
		// exception.
		boolean caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Set the dynamic input.
		target.setInputTuples(dynamicInput.iterator());

		// "Execute" the plan by streaming on the spliterator.
		List<Tuple> result = target.stream().collect(Collectors.toList());
		Assert.assertEquals(N / 2, result.size());

		// Check that all of the values in the 1st position match the input constant
		// value.
		Assert.assertTrue(result.stream().allMatch(t -> ((Integer) t.getValue(1)).equals(101)));

		// Test re-executing the plan (this time using the execute method).
		// Re-set the dynamic input.
		target.setInputTuples(dynamicInput.iterator());
		result = target.stream().collect(Collectors.toList());
		Assert.assertEquals(N / 2, result.size());

		// Check that all of the values in the 1st position match the input constant
		// value.
		Assert.assertTrue(result.stream().allMatch(t -> ((Integer) t.getValue(1)).equals(101)));

		/*
		 * Test with invalid dynamic input.
		 */
		// target = new Access(AccessTerm.create(am1));
		target.close();
		target = new Access(AccessTerm.create(am1.getRelation(), am1), decor);

		TupleType ttString = TupleType.DefaultFactory.create(String.class);
		List<Tuple> invalidDynamicInput = new ArrayList<Tuple>();
		invalidDynamicInput.add(ttString.createTuple("xyz"));

		// Setting the dynamic input with invalid tuples is possible but a subsequent
		// attempt
		// to execute the plan results in an IllegalArgumentExceptionexception thrown by
		// the underlying access method. Note that we cannot (easily) verify the
		// validity of
		// dynamic input when it is set because inputTuples is an iterator.
		Iterator<Tuple> inputTuples = invalidDynamicInput.iterator();
		Assert.assertTrue(inputTuples.hasNext());
		target.setInputTuples(inputTuples);

		caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		/*
		 * Test with mixed valid and invalid dynamic input.
		 */
		// target = new Access(AccessTerm.create(am1));
		target.close();
		target = new Access(AccessTerm.create(am1.getRelation(), am1), decor);

		List<Tuple> mixedDynamicInput = new ArrayList<Tuple>();
		mixedDynamicInput.add(ttInteger.createTuple(101));
		mixedDynamicInput.add(ttInteger.createTuple(22));
		mixedDynamicInput.add(ttString.createTuple("xyz"));

		// Again, attempting to execute the plan when the dynamic input contains
		// invalid tuples results in an IllegalArgumentExceptionexception.
		target.setInputTuples(inputTuples);

		caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);
		target.close();
	}

	@Test
	public void integrationTestSql1() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		Integer[] inputs = new Integer[0];
		ExecutableAccessMethod amFree = new SqlAccessMethod("NATION", TPCHelper.attrs_N, inputs, relation,
				TPCHelper.attrMap_nation, TPCHelper.getProperties());

		/*
		 * Plan: free access on relation NATION.
		 */
		// Access target = new Access(AccessTerm.create(amFree));
		Access target = new Access(AccessTerm.create(amFree.getRelation(), amFree), decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// TPC-H SQL:
		// SELECT count(*) FROM nation;
		Assert.assertEquals(25, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql2() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		// Specify input attributes by passing an array of indices.
		Integer[] inputs = new Integer[] { 2 };
		ExecutableAccessMethod am2 = new SqlAccessMethod("NATION", TPCHelper.attrs_N, inputs, relation,
				TPCHelper.attrMap_nation, TPCHelper.getProperties());

		/*
		 * Plan: Access on relation NATION with constant input on attribute "regionKey".
		 */
		// Map<Attribute, TypedConstant> inputConstants = new HashMap<>();
		// inputConstants.put(Attribute.create(Integer.class, "regionKey"),
		// TypedConstant.create(2));
		//
		// Access target = new Access(AccessTerm.create(am2, inputConstants));

		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(2, TypedConstant.create(2));
		Access target = new Access(AccessTerm.create(am2.getRelation(), am2, inputConstants), decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// TPC-H SQL:
		// SELECT count(*) FROM nation WHERE N_REGIONKEY=2;
		Assert.assertEquals(5, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql3() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		// Specify input attributes by passing a set of attributes.
		Set<Attribute> inputAttributes = Sets.newHashSet(Attribute.create(Integer.class, "N_REGIONKEY"));
		ExecutableAccessMethod am2 = new SqlAccessMethod("NATION", TPCHelper.attrs_N, inputAttributes, relation,
				TPCHelper.attrMap_nation, TPCHelper.getProperties());

		/*
		 * Plan: Access on relation NATION with constant input on attribute "regionKey".
		 */
		// Map<Attribute, TypedConstant> inputConstants = new HashMap<>();
		// inputConstants.put(Attribute.create(Integer.class, "regionKey"),
		// TypedConstant.create(2));
		//
		// Access target = new Access(AccessTerm.create(am2, inputConstants));
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(2, TypedConstant.create(2));
		Access target = new Access(AccessTerm.create(am2.getRelation(), am2, inputConstants), decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// TPC-H SQL:
		// SELECT count(*) FROM nation WHERE N_REGIONKEY=2;
		Assert.assertEquals(5, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql4() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		// Specify input attributes by passing a set of attributes.
		Set<Attribute> inputAttributes = Sets.newHashSet(Attribute.create(Integer.class, "N_REGIONKEY"));
		ExecutableAccessMethod am2 = new SqlAccessMethod("NATION", TPCHelper.attrs_N, inputAttributes, relation,
				TPCHelper.attrMap_nation, TPCHelper.getProperties());

		/*
		 * Plan: Access on relation NATION with dynamic input on attribute "regionKey".
		 */
		// Access target = new Access(AccessTerm.create(am2));
		Access target = new Access(AccessTerm.create(am2.getRelation(), am2), decor);

		// Attempting to execute the plan without specifying dynamic inputs
		// results in an IllegalStateException.
		boolean caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Set the dynamic input.
		TupleType ttInteger = TupleType.DefaultFactory.create(Integer.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(ttInteger.createTuple(2));
		dynamicInput.add(ttInteger.createTuple(3));

		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// TPC-H SQL:
		// SELECT count(*) FROM nation WHERE N_REGIONKEY IN (2, 3);
		Assert.assertEquals(10, result.size());

		// Repeat the failed attempt to execute the plan.
		caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Repeat the successful attempt to execute the plan.
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan.
		result = target.stream().collect(Collectors.toList());

		// TPC-H SQL:
		// SELECT count(*) FROM nation WHERE N_REGIONKEY IN (2, 3);
		Assert.assertEquals(10, result.size());

		target.close();
	}

	@Test
	public void integrationTestSql5() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());

		// Specify input attributes by passing a set of attributes.
		Set<Attribute> inputAttributes = Sets.newHashSet(Attribute.create(String.class, "N_NAME"),
				Attribute.create(Integer.class, "N_REGIONKEY"));

		ExecutableAccessMethod am12 = new SqlAccessMethod("NATION", TPCHelper.attrs_N, inputAttributes, relation,
				TPCHelper.attrMap_nation, TPCHelper.getProperties());

		/*
		 * Plan: Access on relation NATION with constant input on attribute "regionKey"
		 * and dynamic input on attribute "name".
		 */
		// Map<Attribute, TypedConstant> inputConstants = new HashMap<>();
		// inputConstants.put(Attribute.create(Integer.class, "regionKey"),
		// TypedConstant.create(2));
		//
		// Access target = new Access(AccessTerm.create(am12, inputConstants));
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(2, TypedConstant.create(2));
		Access target = new Access(AccessTerm.create(am12.getRelation(), am12, inputConstants), decor);

		// Attempting to execute the plan without specifying dynamic inputs
		// results in an IllegalStateException.
		boolean caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Set the dynamic input.
		TupleType ttString = TupleType.DefaultFactory.create(String.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(ttString.createTuple("INDIA"));
		dynamicInput.add(ttString.createTuple("FRANCE"));
		dynamicInput.add(ttString.createTuple("JAPAN"));

		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// TPC-H SQL:
		// SELECT count(*) FROM nation WHERE N_REGIONKEY=2 AND N_NAME IN ("INDIA",
		// "FRANCE", "JAPAN");
		Assert.assertEquals(2, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql6() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_lineItem.clone());

		// Specify input attributes by passing a set of attributes.
		Set<Attribute> inputAttributes = Sets.newHashSet(Attribute.create(Integer.class, "L_SUPPKEY"));

		ExecutableAccessMethod am2 = new SqlAccessMethod("LINEITEM", TPCHelper.attrs_L, inputAttributes, relation,
				TPCHelper.attrMap_lineItem, TPCHelper.getProperties());

		/*
		 * Plan: Access on relation LINEITEM with constant input on attribute "suppKey".
		 */
		// Map<Attribute, TypedConstant> inputConstants = new HashMap<>();
		// inputConstants.put(Attribute.create(Integer.class, "suppKey"),
		// TypedConstant.create(22));
		// Access target = new Access(AccessTerm.create(am2, inputConstants));

		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(2, TypedConstant.create(22));
		Access target = new Access(AccessTerm.create(am2.getRelation(), am2, inputConstants), decor);
		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// TPC-H SQL:
		// SELECT count(*) FROM LINEITEM WHERE L_SUPPKEY = 22;
		Assert.assertEquals(596, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql7() throws Exception {

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(TPCHelper.attrs_lineItem.clone());

		// Specify input attributes by passing a set of attributes.
		Set<Attribute> inputAttributes = Sets.newHashSet(Attribute.create(Integer.class, "L_SUPPKEY"),
				Attribute.create(Integer.class, "L_QUANTITY"));

		ExecutableAccessMethod am23 = new SqlAccessMethod("LINEITEM", TPCHelper.attrs_L, inputAttributes, relation,
				TPCHelper.attrMap_lineItem, TPCHelper.getProperties());

		/*
		 * Plan: Access on relation LINEITEM with dynamic input on attributes "suppKey"
		 * and "quantity".
		 */
		// Access target = new Access(AccessTerm.create(am23));
		Access target = new Access(AccessTerm.create(am23.getRelation(), am23), decor);

		// Attempting to execute the plan without specifying dynamic inputs
		// results in an IllegalStateException.
		boolean caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Set the dynamic input.
		TupleType tt = TupleType.DefaultFactory.create(Integer.class, Integer.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(tt.createTuple(1, 1));
		dynamicInput.add(tt.createTuple(1, 2));
		dynamicInput.add(tt.createTuple(1, 3));

		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// TPC-H SQL:
		// select count(*) from LINEITEM where l_suppkey = 1 and l_quantity IN (1,2,3);
		Assert.assertEquals(29, result.size());
		target.close();
	}

	@Test
	public void stressTestSql1() throws Exception {

		// Specify input attributes by passing a set of attributes.
		Set<Attribute> inputAttributes = Sets.newHashSet(Attribute.create(Integer.class, "L_QUANTITY"));

		ExecutableAccessMethod am3 = new SqlAccessMethod("LINEITEM", TPCHelper.attrs_L, inputAttributes,
				TPCHelper.relationLineItem, TPCHelper.attrMap_lineItem, TPCHelper.getProperties());

		/*
		 * Plan: Access on relation LINEITEM with dynamic input on attributes
		 * "quantity".
		 */
		// Access target = new Access(AccessTerm.create(am3));
		Access target = new Access(AccessTerm.create(am3.getRelation(), am3), decor);

		// Attempting to execute the plan without specifying dynamic inputs
		// results in an IllegalStateException.
		boolean caught = false;
		try {
			target.stream().collect(Collectors.toList());
		} catch (IllegalStateException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		TupleType tt = TupleType.DefaultFactory.create(Integer.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		List<Tuple> result;

		// // Set the dynamic input.
		// dynamicInput.add(tt.createTuple(1));
		// target.setInputTuples(dynamicInput.iterator());
		//
		// // Execute the plan.
		// result = target.stream().collect(Collectors.toList());
		//
		// // TPC-H SQL:
		// // select count(*) from LINEITEM where l_quantity IN (1);
		// Assert.assertEquals(120401, result.size()); // Success; 3.5s

		// // Set the dynamic input.
		// dynamicInput.add(tt.createTuple(1));
		// dynamicInput.add(tt.createTuple(2));
		// target.setInputTuples(dynamicInput.iterator());
		//
		// // Execute the plan.
		// result = target.stream().collect(Collectors.toList());
		//
		// // TPC-H SQL:
		// // select count(*) from LINEITEM where l_quantity IN (1,2);
		// Assert.assertEquals(239861, result.size()); // Success; 5.5s

		// Set the dynamic input.
		dynamicInput.add(tt.createTuple(1));
		dynamicInput.add(tt.createTuple(2));
		dynamicInput.add(tt.createTuple(3));
		dynamicInput.add(tt.createTuple(4));
		dynamicInput.add(tt.createTuple(5));
		dynamicInput.add(tt.createTuple(6));
		dynamicInput.add(tt.createTuple(7));
		dynamicInput.add(tt.createTuple(8));
		dynamicInput.add(tt.createTuple(9));
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan.
		result = target.stream().collect(Collectors.toList());

		// TPC-H SQL:
		// // select count(*) from LINEITEM where l_quantity IN (1,2,3,4,5,6,7,8,9);
		Assert.assertEquals(1079240, result.size()); // Success; 23.9s

		target.close();

	}

}
