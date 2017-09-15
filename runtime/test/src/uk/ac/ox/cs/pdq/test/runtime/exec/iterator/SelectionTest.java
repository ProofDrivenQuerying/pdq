package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryTableWrapper;
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

	AccessMethod am1 = AccessMethod.create("access_method1", new Integer[] {});

	InMemoryTableWrapper relation1 = new InMemoryTableWrapper("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
			Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
			new AccessMethod[] {am1});

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
		Selection target = new Selection(condition, new Access(relation1, am1));

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
		relation1.clear();
		Assert.assertEquals(0, relation1.getData().size());
		relation1.load(tuples12);
		Assert.assertEquals(N, relation1.getData().size());

		// Reconstruct the target selection (TODO: instead, reset ought to work here)
		target = null;
		target = new Selection(condition, new Access(relation1, am1));

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

}
