// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.runtime.exec.spliterator;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.datasources.sql.SqlAccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.Access;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.CartesianProduct;

public class CartesianProductTest {
	PlanDecorator decor = null;

	/*
	 * The following are integration tests: CartesianProduct plans are constructed &
	 * executed.
	 */
	@Before
	public void setup() throws JAXBException {
		decor = new PlanDecorator(AccessRepository.getRepository());
	}

	@Test
	public void integrationTestSql1() throws Exception {

		Relation relationNation = Mockito.mock(Relation.class);
		when(relationNation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());
		when(relationNation.getName()).thenReturn("NATION");

		Relation relationRegion = Mockito.mock(Relation.class);
		when(relationRegion.getAttributes()).thenReturn(TPCHelper.attrs_region.clone());
		when(relationRegion.getName()).thenReturn("REGION");

		Integer[] inputs = new Integer[0];
		AccessMethodDescriptor amFreeNation = new SqlAccessMethod("NATION", TPCHelper.attrs_N, inputs,
				relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());
		AccessMethodDescriptor amFreeRegion = new SqlAccessMethod("REGION", TPCHelper.attrs_R, inputs,
				relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());

		/*
		 * Plan: Free access on the NATION and REGION relations, then Cartesian product.
		 */
		AccessTerm leftChild = AccessTerm.create(relationNation, amFreeNation);
		AccessTerm rightChild = AccessTerm.create(relationRegion, amFreeRegion);
		CartesianProduct target = new CartesianProduct(CartesianProductTerm.create(leftChild, rightChild), decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// SELECT COUNT(*) FROM NATION CROSS JOIN REGION;
		Assert.assertEquals(125, result.size());

		// Check that the first tuple contains the expected number of elements,
		// i.e. one for each of the concatenated attributes from the two relations.
		Assert.assertTrue(result.stream().allMatch(tuple -> tuple.size() == 6));
		target.close();

		// Test re-useability.
		Assert.assertEquals(125, target.execute().size());
		Assert.assertEquals(125, target.execute().size());
	}

	@Test
	public void integrationTestSql2() throws Exception {

		Relation relationNation = Mockito.mock(Relation.class);
		when(relationNation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());
		when(relationNation.getName()).thenReturn("NATION");

		Relation relationRegion = Mockito.mock(Relation.class);
		when(relationRegion.getAttributes()).thenReturn(TPCHelper.attrs_region.clone());
		when(relationRegion.getName()).thenReturn("REGION");

		Integer[] inputs = new Integer[0];
		AccessMethodDescriptor amFreeNation = new SqlAccessMethod("NATION", TPCHelper.attrs_N, inputs,
				relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());

		Set<Attribute> inputAttributes = new HashSet<Attribute>();
		inputAttributes.add(Attribute.create(Integer.class, "R_REGIONKEY"));

		AccessMethodDescriptor am0Region = new SqlAccessMethod("REGION", TPCHelper.attrs_R, inputAttributes,
				relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());

		/*
		 * Plan: Free access on the NATION relation and access on the REGION relation
		 * with input on the R_REGIONKEY attribute, then Cartesian product.
		 */
		AccessTerm leftChild = AccessTerm.create(relationNation, amFreeNation);
		AccessTerm rightChild = AccessTerm.create(relationRegion, am0Region);
		CartesianProduct target = new CartesianProduct(CartesianProductTerm.create(leftChild, rightChild), decor);

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
		dynamicInput.add(ttInteger.createTuple(0));
		dynamicInput.add(ttInteger.createTuple(1));
		dynamicInput.add(ttInteger.createTuple(3));
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// CREATE VIEW R AS SELECT * FROM REGION WHERE R_REGIONKEY IN (0,1,3);
		// SELECT COUNT(*) FROM NATION CROSS JOIN R;
		Assert.assertEquals(75, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql3() throws Exception {

		// Relation relationNation = Mockito.mock(Relation.class);
		// when(relationNation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());
		Relation relationNation = Relation.create("Nation", TPCHelper.attrs_nation.clone());
		when(relationNation.getName()).thenReturn("NATION");

		// Relation relationRegion = Mockito.mock(Relation.class);
		// when(relationRegion.getAttributes()).thenReturn(TPCHelper.attrs_region.clone());
		Relation relationRegion = Relation.create("Region", TPCHelper.attrs_region.clone());

		Set<Attribute> inputAttributes;

		inputAttributes = new HashSet<Attribute>();
		inputAttributes.add(Attribute.create(Integer.class, "N_REGIONKEY"));
		AccessMethodDescriptor am2Nation = new SqlAccessMethod("NATION", TPCHelper.attrs_N, inputAttributes,
				relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());

		inputAttributes = new HashSet<Attribute>();
		inputAttributes.add(Attribute.create(Integer.class, "R_REGIONKEY"));
		AccessMethodDescriptor am0Region = new SqlAccessMethod("REGION", TPCHelper.attrs_R, inputAttributes,
				relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());

		/*
		 * Plan: Access on the NATION relation with input on the N_REGIONKEY attribute
		 * and access on the REGION relation with input on the R_REGIONKEY attribute,
		 * then Cartesian product.
		 */
		AccessTerm leftChild = AccessTerm.create(relationNation, am2Nation);
		AccessTerm rightChild = AccessTerm.create(relationRegion, am0Region);
		CartesianProduct target = new CartesianProduct(CartesianProductTerm.create(leftChild, rightChild), decor);

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
		TupleType ttIntegerInteger = TupleType.DefaultFactory.create(Integer.class, Integer.class);
		List<Tuple> dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(ttIntegerInteger.createTuple(4, 0));
		dynamicInput.add(ttIntegerInteger.createTuple(2, 1));
		dynamicInput.add(ttIntegerInteger.createTuple(2, 3));
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		// CREATE VIEW N AS SELECT * FROM NATION WHERE N_REGIONKEY IN (4,2);
		// CREATE VIEW R AS SELECT * FROM REGION WHERE R_REGIONKEY IN (0,1,3);
		// SELECT COUNT(*) FROM N CROSS JOIN R;
		Assert.assertEquals(30, result.size());

		/*
		 * Test with dynamic input containing no duplicates for either child.
		 */
		target.close();
		target = new CartesianProduct(CartesianProductTerm.create(leftChild, rightChild), decor);
		dynamicInput = new ArrayList<Tuple>();
		dynamicInput.add(ttIntegerInteger.createTuple(4, 0));
		dynamicInput.add(ttIntegerInteger.createTuple(2, 1));
		dynamicInput.add(ttIntegerInteger.createTuple(0, 3));
		target.setInputTuples(dynamicInput.iterator());

		// Execute the plan.
		result = target.stream().collect(Collectors.toList());

		// CREATE VIEW N AS SELECT * FROM NATION WHERE N_REGIONKEY IN (4,2,0);
		// CREATE VIEW R AS SELECT * FROM REGION WHERE R_REGIONKEY IN (0,1,3);
		// SELECT COUNT(*) FROM N CROSS JOIN R;
		Assert.assertEquals(45, result.size());
		target.close();
	}

	@Test
	public void integrationTestSql4() throws Exception {

		/*
		 * Join with children: - free access on NATION - access on REGION with dynamic
		 * input on the REGIONKEY attribute.
		 */

		Relation relationNation = Mockito.mock(Relation.class);
		when(relationNation.getAttributes()).thenReturn(TPCHelper.attrs_nation.clone());
		when(relationNation.getName()).thenReturn("NATION");
	
		Relation relationRegion = Mockito.mock(Relation.class);
		when(relationRegion.getAttributes()).thenReturn(TPCHelper.attrs_region.clone());
		when(relationRegion.getName()).thenReturn("REGION");

		AccessMethodDescriptor amFreeNation = new SqlAccessMethod("NATION", TPCHelper.attrs_N, new Integer[0],
				relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());

		Set<Attribute> inputAttributes = Sets.newHashSet(Attribute.create(Integer.class, "R_REGIONKEY"));
		AccessMethodDescriptor am0Region = new SqlAccessMethod("REGION", TPCHelper.attrs_R, inputAttributes,
				relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());

		AccessTerm leftChild = AccessTerm.create(relationNation, amFreeNation);
		AccessTerm rightChild = AccessTerm.create(relationRegion, am0Region);

		// Construct the target plan.
		CartesianProduct target = new CartesianProduct(CartesianProductTerm.create(leftChild, rightChild), decor);

		// Attempting to execute the plan before setting the dynamic input raises an
		// exception.
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

		// CREATE VIEW R AS SELECT * FROM REGION WHERE R_REGIONKEY IN (0,1,3);
		// SELECT COUNT(*) FROM NATION CROSS JOIN R;
		Assert.assertEquals(75, result.size());
		Assert.assertTrue(result.stream().allMatch(tuple -> tuple.size() == 6));

		Set<Integer> regionKeys = result.stream().map(tuple -> (Integer) tuple.getValue(3)).collect(Collectors.toSet());
		Assert.assertEquals(Sets.newHashSet(0, 1, 3), regionKeys);
		target.close();
	}

	@Test
	public void stressTestInMemory1() throws Exception {

		/*
		 * The following stress test was inspired by the tests outlined in see issue
		 * #209 which highlighted a bug in the CartesianProduct implementation at
		 * 19/02/2018, the fix of which is tested below.
		 * 
		 * Here we also test the construction of ExecutablePlan instances from other
		 * ExecutablePlan objects, rather than RelationTerm objects.
		 */

		Relation relationR1 = Mockito.mock(Relation.class);
		Attribute[] relationR1Attributes = new Attribute[] { Attribute.create(String.class, "a") };

		Relation relationR2 = Mockito.mock(Relation.class);
		Attribute[] relationR2Attributes = new Attribute[] { Attribute.create(String.class, "b") };

		Relation relationR3 = Mockito.mock(Relation.class);
		Attribute[] relationR3Attributes = new Attribute[] { Attribute.create(String.class, "c") };

		Relation relationR4 = Mockito.mock(Relation.class);
		Attribute[] relationR4Attributes = new Attribute[] { Attribute.create(String.class, "d") };

		Map<Attribute, Attribute> attributeMapping1 = ImmutableMap.of(Attribute.create(String.class, "a"),
				Attribute.create(String.class, "a"));

		Map<Attribute, Attribute> attributeMapping2 = ImmutableMap.of(Attribute.create(String.class, "b"),
				Attribute.create(String.class, "b"));

		Map<Attribute, Attribute> attributeMapping3 = ImmutableMap.of(Attribute.create(String.class, "c"),
				Attribute.create(String.class, "c"));

		Map<Attribute, Attribute> attributeMapping4 = ImmutableMap.of(Attribute.create(String.class, "d"),
				Attribute.create(String.class, "d"));

		TupleType ttString = TupleType.DefaultFactory.create(String.class);

		// Scale parameter (number of tuples in each relation):
		int N = 2;

		when(relationR1.getAttributes()).thenReturn(relationR1Attributes.clone());
		when(relationR2.getAttributes()).thenReturn(relationR2Attributes.clone());
		when(relationR3.getAttributes()).thenReturn(relationR3Attributes.clone());
		when(relationR4.getAttributes()).thenReturn(relationR4Attributes.clone());

		InMemoryAccessMethod am1Free = new InMemoryAccessMethod(relationR1Attributes, new Integer[0], relationR1,
				attributeMapping1);
		InMemoryAccessMethod am2Free = new InMemoryAccessMethod(relationR2Attributes, new Integer[0], relationR2,
				attributeMapping2);
		InMemoryAccessMethod am3Free = new InMemoryAccessMethod(relationR3Attributes, new Integer[0], relationR3,
				attributeMapping3);
		InMemoryAccessMethod am4Free = new InMemoryAccessMethod(relationR4Attributes, new Integer[0], relationR4,
				attributeMapping4);

		/*
		 * CartesianProduct{a}(R1, R2).
		 */

		Access accessR1 = new Access(AccessTerm.create(relationR1, am1Free), decor);
		Access accessR2 = new Access(AccessTerm.create(relationR2, am2Free), decor);
		CartesianProduct cartesianProductR1R2 = new CartesianProduct(CartesianProductTerm
				.create(AccessTerm.create(relationR1, am1Free), AccessTerm.create(relationR2, am2Free)), decor);

		/*
		 * CartesianProduct{d}(R3, R4).
		 */
		CartesianProduct cartesianProductR3R4 = new CartesianProduct(CartesianProductTerm
				.create(AccessTerm.create(relationR3, am3Free), AccessTerm.create(relationR4, am4Free)), decor);

		// Create some tuples.
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			String[] values = new String[] { "a" + i };
			tuples1.add(ttString.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			String[] values = new String[] { "b" + i };
			tuples2.add(ttString.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuples3 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			String[] values = new String[] { "c" + i };
			tuples3.add(ttString.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuples4 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			String[] values = new String[] { "d" + i };
			tuples4.add(ttString.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples
		am1Free.load(tuples1);
		am2Free.load(tuples2);
		am3Free.load(tuples3);
		am4Free.load(tuples4);

		// Test the sub-queries.
		Assert.assertEquals(N, accessR1.execute().size());
		Assert.assertEquals(N, accessR2.execute().size());

		Assert.assertEquals(N * N, cartesianProductR1R2.execute().size());
		Assert.assertEquals(N * N, cartesianProductR1R2.execute().size());

		Assert.assertEquals(N * N, cartesianProductR3R4.execute().size());
		Assert.assertEquals(N * N, cartesianProductR3R4.execute().size());

		/*
		 * CartesianProduct(dependentJoinR1R2, dependentJoinR3R4).
		 */
		CartesianProduct target = new CartesianProduct(CartesianProductTerm.create(
				CartesianProductTerm.create(AccessTerm.create(relationR1, am1Free),
						AccessTerm.create(relationR2, am2Free)),
				CartesianProductTerm.create(AccessTerm.create(relationR3, am3Free),
						AccessTerm.create(relationR4, am4Free))),
				decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Assert.assertEquals(N * N * N * N, result.size());
		accessR1.close();
		accessR2.close();
		cartesianProductR1R2.close();
		cartesianProductR3R4.close();
		target.close();
	}

	/*
	 * Plan: CartesianProduct(NATION, SUPPLIER)
	 */
	@Test
	public void stressTestSql1() throws Exception {

		CartesianProduct target = new CartesianProduct(
				CartesianProductTerm.create(AccessTerm.create(TPCHelper.relationNation, TPCHelper.amFreeNation),
						AccessTerm.create(TPCHelper.relationSupplier, TPCHelper.amFreeSupplier)),
				decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Tuple tuple = result.get(0);
		Assert.assertEquals(tuple.size(), TPCHelper.attrs_nation.length + TPCHelper.attrs_supplier.length);

		// select count(*) from NATION, SUPPLIER where
		// NATION.N_NATIONKEY=SUPPLIER.S_NATIONKEY;
		Assert.assertEquals(25 * 10000, result.size());
		target.close();
	}

	/*
	 * Plan: CartesianProduct(NATION_LESS, SUPPLIER_LESS)
	 */
	// @Test nation_less is not part of the default tcph database
	public void stressTestSql1a() throws Exception {

		CartesianProduct target = new CartesianProduct(CartesianProductTerm.create(
				AccessTerm.create(TPCHelper.relationNation_less, TPCHelper.amFreeNation_less),
				AccessTerm.create(TPCHelper.relationSupplier_less, TPCHelper.amFreeSupplier_less)), decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Tuple tuple = result.get(0);
		Assert.assertEquals(tuple.size(), TPCHelper.attrs_nation_less.length + TPCHelper.attrs_supplier_less.length);

		// select count(*) from NATION, SUPPLIER where
		// NATION.N_NATIONKEY=SUPPLIER.S_NATIONKEY;
		Assert.assertEquals(25 * 10000, result.size());
		target.close();
	}

	/*
	 * Plan: CartesianProduct(NATION, CUSTOMER)
	 */
	@Test
	public void stressTestSql2() throws Exception {

		CartesianProduct target = new CartesianProduct(
				CartesianProductTerm.create(AccessTerm.create(TPCHelper.relationNation, TPCHelper.amFreeNation),
						AccessTerm.create(TPCHelper.relationCustomer, TPCHelper.amFreeCustomer)),
				decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Tuple tuple = result.get(0);
		Assert.assertEquals(tuple.size(), TPCHelper.attrs_nation.length + TPCHelper.attrs_customer.length);

		Assert.assertEquals(25 * 150000, result.size());
		target.close();
	}

	/*
	 * Plan: CartesianProduct(NATION_LESS, PARTSUPP_LESS)
	 */
	// @Test nation_less is not part of the default tcph database
	public void stressTestSql2a() throws Exception {

		CartesianProduct target = new CartesianProduct(CartesianProductTerm.create(
				AccessTerm.create(TPCHelper.relationNation_less, TPCHelper.amFreeNation_less),
				AccessTerm.create(TPCHelper.relationCustomer_less, TPCHelper.amFreeCustomer_less)), decor);

		// Execute the plan.
		List<Tuple> result = target.stream().collect(Collectors.toList());

		Tuple tuple = result.get(0);
		Assert.assertEquals(tuple.size(), TPCHelper.attrs_nation_less.length + TPCHelper.attrs_customer_less.length);

		Assert.assertEquals(25 * 150000, result.size());
		target.close();
	}
}
