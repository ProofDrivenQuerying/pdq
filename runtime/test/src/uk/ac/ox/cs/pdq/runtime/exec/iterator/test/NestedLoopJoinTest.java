package uk.ac.ox.cs.pdq.runtime.exec.iterator.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.memory.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.runtime.TimeoutException;
import uk.ac.ox.cs.pdq.runtime.exec.PipelinedPlanExecutor.TimeoutChecker;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Access;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Join;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.NestedLoopJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

public class NestedLoopJoinTest {

	@Test
	public void test() {
		AccessMethod am1 = AccessMethod.create("access_method1",new Integer[] {});
		AccessMethod am2 = AccessMethod.create("access_method2",new Integer[] {0});
		AccessMethod am3 = AccessMethod.create("access_method2",new Integer[] {0,1});

		InMemoryTableWrapper relation1 = new InMemoryTableWrapper("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
				new AccessMethod[] {am1});
		InMemoryTableWrapper relation2 = new InMemoryTableWrapper("R2", new Attribute[] {Attribute.create(Integer.class, "c"),
				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")},
				new AccessMethod[] {am1, am2, am3});

		// Free access relations to act as children of the Join relation.
		TupleIterator relation1Free = new Access(relation1, am1);
		TupleIterator relation2Free = new Access(relation2, am1);

		/*
		 * Simple plan that joins the results of two accesses.
		 * The constructor assumes that two attributes should be equal if they have the same name.
		 * This is the so called equijoin. 
		 * We use the nested loop join algorithm
		 */
		Join nestedLoopJoin = new NestedLoopJoin(relation1Free, relation2Free);

		// Create some tuples
		TupleType tt = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);
	
		Integer[] values1 = new Integer[] {10, 11, 12};
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			tuples1.add(tt.createTuple((Object[]) Arrays.copyOf(values1, values1.length)));
		}

		// Load tuples   
		relation1.load(tuples1);
		relation2.load(tuples1);
		
		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(nestedLoopJoin);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected. Here the join condition is *never* satisfied.
		Assert.assertNotNull(result);
		Assert.assertEquals(0, result.size());
		
		Integer[] values2 = new Integer[] {12, 13, 14};
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			tuples2.add(tt.createTuple((Object[]) Arrays.copyOf(values2, values2.length)));
		}

		// Load tuples into relation2 such that the join condition is *always* satisfied. 
		relation2.clear();
		relation2.load(tuples2);

		// Note that we must reconstruct the NestedLoopJoin instance to make sure the children are not open initially. 
		relation1Free = new Access(relation1, am1);
		relation2Free = new Access(relation2, am1);
		nestedLoopJoin = new NestedLoopJoin(relation1Free, relation2Free);
		
		//Execute the plan
		result = null;
		try {
			result = this.planExecution(nestedLoopJoin);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones you expected
		Assert.assertNotNull(result);
		Assert.assertEquals(N, result.size());
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(new Integer[] {10, 11, 12, 13, 14}, x.getValues());

		tuples2.clear();
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 4) == 0) ? values2 : values1;
			tuples2.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples into relation2 such that the join condition is *sometimes* satisfied. 
		relation2.clear();
		relation2.load(tuples2);
		
		// Note that we must reconstruct the NestedLoopJoin instance to make sure the children are not open initially. 
		relation1Free = new Access(relation1, am1);
		relation2Free = new Access(relation2, am1);
		nestedLoopJoin = new NestedLoopJoin(relation1Free, relation2Free);

		//Execute the plan
		result = null;
		try {
			result = this.planExecution(nestedLoopJoin);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones you expected
		Assert.assertNotNull(result);
		Assert.assertEquals(N/4, result.size());
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(new Integer[] {10, 11, 12, 13, 14}, x.getValues());

	}

	//Execute plans by passing them to this method
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
}
