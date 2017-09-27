package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
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
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

public class SelectionTest {

	AccessMethod amFree = AccessMethod.create("free_access", new Integer[] {});
	
	/*
	 *  InMemoryRelation construction.
	 */

	InMemoryTableWrapper inMemoryRelation = new InMemoryTableWrapper("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
			Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
			new AccessMethod[] {amFree});

	/*
	 *  PostgresqlRelation construction.
	 */
	public Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty("url", "TODO");
		properties.setProperty("database", "tpch");
		properties.setProperty("username", "admin");
		properties.setProperty("password", "admin");
		return(properties);
	}

	Attribute[] attributes_C = new Attribute[] {
			Attribute.create(Integer.class, "C_CUSTKEY"),
			Attribute.create(String.class, "C_NAME"),
			Attribute.create(Integer.class, "C_ADDRESS"),
			Attribute.create(Integer.class, "C_NATIONKEY"),
			Attribute.create(String.class, "C_PHONE"),
			Attribute.create(Float.class, "C_ACCTBAL"),
			Attribute.create(String.class, "C_MKTSEGMENT"),
			Attribute.create(String.class, "C_COMMENT")
	};

	Attribute[] attributes_N = new Attribute[] {
			Attribute.create(Integer.class, "N_NATIONKEY"),
			Attribute.create(String.class, "N_NAME"),
			Attribute.create(Integer.class, "N_REGIONKEY"),
			Attribute.create(String.class, "N_COMMENT")
	};

	SQLRelationWrapper postgresqlRelationCustomer = new PostgresqlRelationWrapper(this.getProperties(), "CUSTOMER", 
			attributes_C, new AccessMethod[] {amFree});
	SQLRelationWrapper postgresqlRelationNation = new PostgresqlRelationWrapper(this.getProperties(), "NATION", 
			attributes_N, new AccessMethod[] {amFree});
	
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
		 *  Simple plan that does a free access on relation R1, then selects the rows where the value
		 *  of first column is equal to the constant "1". 
		 */
		Condition condition = ConstantEqualityCondition.create(0, TypedConstant.create(1));
		Selection target = new Selection(condition, new Access(inMemoryRelation, amFree));

		// Create some tuples
		TupleType tt = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);

		Integer[] values1 = new Integer[] {1, 11, 12};
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			tuples1.add(tt.createTuple((Object[]) Arrays.copyOf(values1, values1.length)));
		}

		// Load tuples by calling the load method of inMemoryRelation  
		Assert.assertEquals(0, inMemoryRelation.getData().size());
		inMemoryRelation.load(tuples1);
		Assert.assertEquals(N, inMemoryRelation.getData().size());

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected. Here all of the 
		// tuples meet the constant equality condition (i.e. 1 in the zeroth position).
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
		inMemoryRelation.clear();
		Assert.assertEquals(0, inMemoryRelation.getData().size());
		inMemoryRelation.load(tuples12);
		Assert.assertEquals(N, inMemoryRelation.getData().size());

		// Reconstruct the target selection (TODO: instead, reset ought to work here)
		target = null;
		target = new Selection(condition, new Access(inMemoryRelation, amFree));

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
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(values1, x.getValues());

	}

	
	@Test
	public void test2() {

		/*
		 *  Simple plan that does a free access on relation NATION, then selects the rows where the value
		 *  of REGIONKEY column is equal to the constant 2. 
		 */
		Condition condition = ConstantEqualityCondition.create(2, TypedConstant.create(2));
		Selection target = new Selection(condition, new Access(postgresqlRelationNation, amFree));
		
		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	
		// Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		
		// TODO: Find out which nations are in Region 2
		Assert.assertEquals(22, result.size());
//		for (Tuple x:result.getData())
//			Assert.assertArrayEquals(values1, x.getValues());

	}
	
	@Test
	public void test3() {

		/*
		 *  Plan that does a free access on relation CUSTOMER, then selects the rows where the value
		 *  of NATIONKEY column is equal to the constant 44 and then selects the rows where the value
		 *  of MKTSEGMENT is "TODO". 
		 */
		Condition nationkeyCondition = ConstantEqualityCondition.create(3, TypedConstant.create(44));
		Condition mktsegmentCondition = ConstantEqualityCondition.create(6, TypedConstant.create("TODO"));
		Selection target = new Selection(mktsegmentCondition, 
				new Selection(nationkeyCondition, new Access(postgresqlRelationCustomer, amFree)));
		
		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	
		// Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		
		// TODO: Find out which nation has key 44, and which  customers have mktsegment "TODO". 
		Assert.assertEquals(22, result.size());
//		for (Tuple x:result.getData())
//			Assert.assertArrayEquals(values1, x.getValues());

	}

}
