package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
import uk.ac.ox.cs.pdq.runtime.exec.iterator.DependentJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Join;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.NestedLoopJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.SymmetricMemoryHashJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

@SuppressWarnings("unused")
public class AccessTest {

	AccessMethod amFree = AccessMethod.create("free_access",new Integer[] {});
	AccessMethod am0 = AccessMethod.create("access_0", new Integer[] {0});
	AccessMethod am01 = AccessMethod.create("access_method3",new Integer[] {0,1});
	
	TupleType tt = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);

	InMemoryTableWrapper relation1 = new InMemoryTableWrapper("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
			Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
			new AccessMethod[] {amFree});
	
	InMemoryTableWrapper relation2 = new InMemoryTableWrapper("R2", new Attribute[] {Attribute.create(Integer.class, "c"),
			Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")},
			new AccessMethod[] {amFree, am0, am01});
	
	@SuppressWarnings("resource")
	@Test
	public void testAccess() {

		/*
		 * Free access on relation R1 
		 */
		
		// Construct an Access instance by providing consistent access method & input constants 
		// (in this case free access, i.e. no input constants).
		Access target = new Access(relation1, amFree);
		Assert.assertNotNull(target);
		
		// Note that the Access constructor may be called with an access method not found in the relation.
		// TODO: - include an integration test for this case (below). 
		// 		 - Q: given that this construction works, what is the purpose of the "AccessMethod[] methods" 
		// 			  argument to the InMemoryTableWrapper constructor? 
		target = new Access(relation1, am0);
		Assert.assertNotNull(target);

		/*
		 * Access on relation R2 that requires inputs on first position.
		 */
		
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(0, TypedConstant.create(100));
		
		// Construct an Access instance by providing consistent access method & input constants.
		target = new Access(relation2, am0, inputConstants);
		Assert.assertNotNull(target);
		
		// An exception is thrown if the input constants map is inconsistent with the access method.
		boolean caught = false; 
		try {
			new Access(relation2, amFree, inputConstants);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// An exception is thrown if the input constants map is inconsistent with the access method.
		inputConstants = new HashMap<>();
		inputConstants.put(1, TypedConstant.create(100));
		caught = false; 
		try {
			new Access(relation2, amFree, inputConstants);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		
	}
	
	/*
	 * The following are integration tests: Access instances are constructed and executed.
	 */

	//Execute plans by passing them to this method
	private Table planExecution(TupleIterator plan) throws TimeoutException {
		Table results = new Table(plan.getOutputAttributes());
		ExecutorService execService = Executors.newFixedThreadPool(1);
		execService.execute(new TimeoutChecker(3600000, plan));
		plan.open();
		while (plan.hasNext()) {
			Tuple t = plan.next();
			results.appendRow(t);
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
		 * Free access on relation R1
		 */
		// Construct an Access instance by providing consistent access method & input constants 
		// (in this case free access, i.e. no input constants).
		 Access target = new Access(relation1, amFree);
		
		// Create some tuples
		Integer[] values = new Integer[] {10, 11, 12};
		Collection<Tuple> tuples = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			tuples.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		// NB: Alternative construction of random integer arrays requires Java 8:
		// Integer[] a = new Random(22).ints(relation1.getArity(), 0, 100).boxed().toArray(Integer[]::new);
		
		// Load tuples by calling the load method of relation1  
		Assert.assertEquals(0, relation1.getData().size());
		
		relation1.load(tuples);
		Assert.assertEquals(N, relation1.getData().size());
		
		
		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones you expected
		Assert.assertNotNull(result);
		Assert.assertEquals(N, result.size());
		Assert.assertArrayEquals(tuples.toArray(), result.getData().toArray());
	}
		
	@SuppressWarnings("resource")
	@Test
	public void test2() {

		/*
		 * Access on relation R2 that requires inputs on first position.
		 */
		
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(0, TypedConstant.create(100));
		
		// An exception is thrown if the input constants map is inconsistent with the access method.
		boolean caught = false; 
		try {
			new Access(relation2, amFree, inputConstants);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Construct an Access instance by providing consistent access method & input constants.
		Access target = new Access(relation2, am0, inputConstants);

		// Create some tuples
		Integer[] values1 = new Integer[] {10, 11, 12};
		Integer[] values2 = new Integer[] {100, 101, 102};
		Collection<Tuple> tuples = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 2) == 0) ? values2 : values1;
			tuples.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples by calling the load method of relation2  
		Assert.assertEquals(0, relation2.getData().size());
		relation2.load(tuples);
		Assert.assertEquals(N, relation2.getData().size());

		//Execute the plan.
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected.
		// Here, only half of the tuples in relation2 satisfy the inputConstants condition 
		// (i.e. that the first element in the tuple is 100).  
		Assert.assertNotNull(result);
		Assert.assertEquals(N/2, result.size());
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(values2, x.getValues());

	}
		
	@SuppressWarnings("resource")
	@Test
	public void test3() {

		/*
		 * Access on relation R2 that requires inputs on two first positions.
		 */
		
		//Suppose that a user already specified the typed constants "100" and "200" in positions 0 & 1. 
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(0, TypedConstant.create(100));
		inputConstants.put(1, TypedConstant.create(200));
		
		// An exception is thrown if the input constants map is inconsistent with the access method.
		boolean caught = false; 
		try {
			new Access(relation2, amFree, inputConstants);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		caught = false;
		try {
			new Access(relation2, am0, inputConstants);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Construct an Access instance by providing consistent access method & input constants.
		Access target = new Access(relation2, am01, inputConstants);

		// Create some tuples
		Integer[] values1 = new Integer[] {10, 11, 12};
		Integer[] values2 = new Integer[] {100, 101, 102};
		Integer[] values3 = new Integer[] {100, 200, 201};
		Collection<Tuple> tuples3 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 2) == 0) ? values2 : values1;
			if ((i % 4) == 0)
				values = values3;
			tuples3.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples by calling the load method of relation2
		Assert.assertEquals(0, relation2.getData().size());
		relation2.load(tuples3);
		Assert.assertEquals(N, relation2.getData().size());

		//Execute the plan.
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones you expected
		Assert.assertNotNull(result);
		Assert.assertEquals(N/4, result.size());
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(values3, x.getValues());

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

	Attribute[] attributes_N = new Attribute[] {
			Attribute.create(Integer.class, "N_NATIONKEY"),
			Attribute.create(String.class, "N_NAME"),
			Attribute.create(Integer.class, "N_REGIONKEY"),
			Attribute.create(String.class, "N_COMMENT")
	};

	SQLRelationWrapper postgresqlRelationNation = new PostgresqlRelationWrapper(this.getProperties(), "NATION", 
			attributes_N, new AccessMethod[] {amFree});

	@Test
	public void test4() {

		/*
		 *  Free access on relation NATION. 
		 */
		Access target = new Access(postgresqlRelationNation, amFree);
		
		// Debugging:
		// Fails at the call to open()
		// queryString passed to SQLRelationWrapper.fetchTuples is "SELECT N_NATIONKEY,N_NAME,N_REGIONKEY,N_COMMENT FROM NATION) IN ())"
		// due to inputTuples iterator having one next value, which is empty
		// because inputTuples iterates over the inputs Table constructed as follows on line 203:
		// 			Table inputs = new Table(this.attributesInInputPositions);
		// 
		
		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	
		// Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		// TODO. Assert.assertEquals(22, result.size());
	}
	
}
