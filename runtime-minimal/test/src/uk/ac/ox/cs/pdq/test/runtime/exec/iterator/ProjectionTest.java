package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.datasources.sql.PostgresqlRelationWrapper;
import uk.ac.ox.cs.pdq.datasources.sql.SQLRelationWrapper;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.TimeoutException;
import uk.ac.ox.cs.pdq.runtime.exec.PipelinedPlanExecutor.TimeoutChecker;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Access;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

public class ProjectionTest {

	AccessMethod amFree = AccessMethod.create("free_access",new Integer[] {});

	/*
	 *  InMemoryRelation construction.
	 */

	InMemoryTableWrapper relation1 = new InMemoryTableWrapper("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
			Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
			new AccessMethod[] {amFree});

	@SuppressWarnings("resource")
	@Test
	public void testProjection() {

		// Sanity check the Projection constructor
		boolean caught = false;
		try {
			new Projection(new Attribute[]{Attribute.create(Integer.class, "d")}, new Access(relation1, amFree));
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Inconsistent attributes", e.getMessage());
			caught = true;
		}
		Assert.assertTrue(caught);

		caught = false;
		try {
			new Projection(new Attribute[]{Attribute.create(String.class, "b")}, new Access(relation1, amFree));
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Inconsistent attributes", e.getMessage());
			caught = true;
		}
		Assert.assertTrue(caught);
	}

	/*
	 * The following are integration tests: Projection instances are constructed and executed.
	 */

	// Execute plans by passing them to this method
	private Table planExecution(TupleIterator plan) throws TimeoutException {
		Table results = new Table(plan.getOutputAttributes());
		ExecutorService execService = Executors.newFixedThreadPool(1);
		execService.execute(new TimeoutChecker(3600000, plan));
		plan.open();
		while (plan.hasNext()) {
			Tuple t = plan.next();
			results.appendRow(t);
			// System.out.println(t);
		}
		execService.shutdownNow();
		if (plan.isInterrupted()) {
			throw new TimeoutException();
		}
		return results;
	}
	
	@Test
	public void test1() {

		/*
		 * Simple plan that does an free access on relation R1 and projects the second column
		 */
		Projection target = new Projection(new Attribute[]{Attribute.create(Integer.class, "b")}, 
				new Access(relation1, amFree));

		// Create some tuples
		TupleType tt = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);

		Integer[] values1 = new Integer[] {1, 11, 12};
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			tuples1.add(tt.createTuple((Object[]) Arrays.copyOf(values1, values1.length)));
		}

		// Load tuples by calling the load method of relation1  
		Assert.assertEquals(0, relation1.getData().size());
		relation1.load(tuples1);
		Assert.assertEquals(N, relation1.getData().size());

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected.
		Assert.assertNotNull(result);
		Assert.assertEquals(N, result.size());

		Collection<Tuple> expected = new ArrayList<Tuple>();
		Integer[] expectedValues = new Integer[] {11};
		TupleType ett = TupleType.DefaultFactory.create(Integer.class);
		for (int i = 0; i != N; i++) {
			expected.add(ett.createTuple((Object[]) Arrays.copyOf(expectedValues, expectedValues.length)));
		}

		// Test that the result data include Tuples containing only the second element (i.e. attribute "b") 
		Assert.assertArrayEquals(expected.toArray(), result.getData().toArray());
	}

	@Test
	public void test2() {

		/*
		 *  Simple plan that does a free access on relation R1, then selects the rows where the value
		 *  of first column is "1" and projects the last column. 
		 */
		Condition condition = ConstantEqualityCondition.create(0, TypedConstant.create(1));
		Projection target = new Projection(new Attribute[]{Attribute.create(Integer.class, "c")}, 
				new Selection(condition, new Access(relation1, amFree))); 

		// Create some tuples
		TupleType tt = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);

		Integer[] values1 = new Integer[] {1, 11, 12};
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			tuples1.add(tt.createTuple((Object[]) Arrays.copyOf(values1, values1.length)));
		}

		// Load tuples by calling the load method of relation1  
		Assert.assertEquals(0, relation1.getData().size());
		relation1.load(tuples1);
		Assert.assertEquals(N, relation1.getData().size());

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected. Here all of the tuples meet the constant equality condition.
		Assert.assertNotNull(result);
		Assert.assertEquals(N, result.size());

		// Create some more tuples
		Integer[] values12 = new Integer[] {2, 11, 12};
		Collection<Tuple> tuples12 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 2) == 0) ? values12 : values1;
			tuples12.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples by calling the load method.
		relation1.clear();
		Assert.assertEquals(0, relation1.getData().size());
		relation1.load(tuples12);
		Assert.assertEquals(N, relation1.getData().size());

		// Reconstruct the target projection (TODO: instead, reset ought to work here)
		target = new Projection(new Attribute[]{Attribute.create(Integer.class, "c")}, 
				new Selection(condition, new Access(relation1, amFree)));

		//Execute the plan
		result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected. 
		// We expect N/2 Tuples in the query results because only half of the Tuples 
		// in tuples12 satisfy the constant equality condition.
		Assert.assertNotNull(result);
		Assert.assertEquals(N/2, result.size());

		Collection<Tuple> expected = new ArrayList<Tuple>();
		Integer[] expectedValues = new Integer[] {12};
		TupleType ett = TupleType.DefaultFactory.create(Integer.class);
		for (Tuple t: tuples12) {
			if (Arrays.equals(t.getValues(), values1))
				expected.add(ett.createTuple((Object[]) Arrays.copyOf(expectedValues, expectedValues.length)));
		}
		
		// Test that the result data include Tuples containing only the last element (i.e. attribute "c") 
		Assert.assertArrayEquals(expected.toArray(), result.getData().toArray());		
	}

	@Test
	public void test3() {

		/*
		 *  Plan that does a free access on relation R1, then selects the rows where the value
		 *  of first column is "1" and the second column equals the third and finally projects the first 
		 *  and last columns. 
		 */
		Condition condition = ConjunctiveCondition.create(new SimpleCondition[]{ConstantEqualityCondition.create(0, TypedConstant.create(1)), 
				AttributeEqualityCondition.create(1, 2)});
		Projection target = new Projection(new Attribute[]{Attribute.create(Integer.class, "a"), 
				Attribute.create(Integer.class, "c")}, 
				new Selection(condition, new Access(relation1, amFree))); 

		// Create some tuples
		TupleType tt = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);

		Integer[] values1 = new Integer[] {1, 11, 12};
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			tuples1.add(tt.createTuple((Object[]) Arrays.copyOf(values1, values1.length)));
		}

		// Load tuples by calling the load method of relation1  
		relation1.clear();
		Assert.assertEquals(0, relation1.getData().size());
		relation1.load(tuples1);
		Assert.assertEquals(N, relation1.getData().size());

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected. Here we expect an empty results set 
		// since none of the tuples meets the equality condition on the second & third columns.
		Assert.assertNotNull(result);
		Assert.assertEquals(0, result.size());

		// Create some more tuples
		Integer[] values2 = new Integer[] {1, 12, 12};
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 4) == 0) ? values2 : values1;
			tuples2.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples by calling the load method.
		relation1.clear();
		Assert.assertEquals(0, relation1.getData().size());
		relation1.load(tuples2);
		Assert.assertEquals(N, relation1.getData().size());

		// Reconstruct the target projection (TODO: instead, reset ought to work here)
		target = new Projection(new Attribute[]{Attribute.create(Integer.class, "a"), 
				Attribute.create(Integer.class, "c")}, 
				new Selection(condition, new Access(relation1, amFree)));

		//Execute the plan
		result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected. 
		// We expect N/2 Tuples in the query results because only half of the Tuples 
		// in tuples12 satisfy the constant equality condition.
		Assert.assertNotNull(result);
		Assert.assertEquals(N/4, result.size());

		Collection<Tuple> expected = new ArrayList<Tuple>();
		Integer[] expectedValues = new Integer[] {1, 12};
		TupleType ett = TupleType.DefaultFactory.create(Integer.class, Integer.class);
		for (Tuple t: tuples2) {
			if (Arrays.equals(t.getValues(), values2))
				expected.add(ett.createTuple((Object[]) Arrays.copyOf(expectedValues, expectedValues.length)));
		}
		
		// Test that the result data include Tuples containing only the first & last elements 
		// (i.e. attributes "a" & "c"). 
		Assert.assertArrayEquals(expected.toArray(), result.getData().toArray());		
	}
	
	/*
	 *  PostgresqlRelation construction.
	 */
	public Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty("url", "jdbc:postgresql://localhost:5432/");
		properties.setProperty("database", "tpch");
		properties.setProperty("username", "admin");
		properties.setProperty("password", "admin");
		return(properties);
	}

	Attribute[] attributes_C = new Attribute[] {
			Attribute.create(Integer.class, "C_CUSTKEY"),
			Attribute.create(String.class, "C_NAME"),
			Attribute.create(String.class, "C_ADDRESS"),
			Attribute.create(Integer.class, "C_NATIONKEY"),
			Attribute.create(String.class, "C_PHONE"),
			Attribute.create(Float.class, "C_ACCTBAL"),
			Attribute.create(String.class, "C_MKTSEGMENT"),
			Attribute.create(String.class, "C_COMMENT")
	};
	
	SQLRelationWrapper postgresqlRelationCustomer = new PostgresqlRelationWrapper(this.getProperties(), "CUSTOMER", 
			attributes_C, new AccessMethod[] {amFree});


	@Test
	public void test4() {

		/*
		 *  Plan that does a free access on the CUSTOMER relation, then selects the rows where the value
		 *  of the NATIONKEY column is 23 (United Kingdom) and the MKTSEGMENT column is "MACHINERY" and 
		 *  finally projects the NAME and ACCTBAL columns. 
		 */
		Condition condition = ConjunctiveCondition.create(new SimpleCondition[]{ConstantEqualityCondition.create(3, TypedConstant.create(23)), 
				ConstantEqualityCondition.create(6, TypedConstant.create("MACHINERY"))});
		Projection target = new Projection(new Attribute[]{Attribute.create(String.class, "C_NAME"), 
				Attribute.create(Float.class, "C_ACCTBAL")}, 
				new Selection(condition, new Access(postgresqlRelationCustomer, amFree))); 

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	
		// Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		
		Assert.assertEquals(2L, result.columns().longValue());
		
		// SELECT COUNT(*) FROM CUSTOMER WHERE c_nationkey=23 AND c_mktsegment='MACHINERY';
		Assert.assertEquals(1158, result.size());

		// TODO: Check data in the result against the results of the following query: 
		// SELECT c_name, c_acctbal FROM CUSTOMER WHERE c_nationkey=23 AND c_mktsegment='MACHINERY';
		
	}
}
