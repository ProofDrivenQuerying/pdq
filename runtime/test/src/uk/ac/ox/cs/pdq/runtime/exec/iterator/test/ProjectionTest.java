package uk.ac.ox.cs.pdq.runtime.exec.iterator.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

	@SuppressWarnings("resource")
	@Test
	public void test() {
		
		AccessMethod am1 = AccessMethod.create("access_method1",new Integer[] {});

		InMemoryTableWrapper relation1 = new InMemoryTableWrapper("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
				new AccessMethod[] {am1});

		// Free access relation to act as child of the Projection relation.
		TupleIterator relation1Free = new Access(relation1, am1);
		
		// Sanity check the Projection constructor
		boolean caught = false;
		try {
			new Projection(new Attribute[]{Attribute.create(Integer.class, "d")}, relation1Free);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Inconsistent attributes", e.getMessage());
			caught = true;
		}
		Assert.assertTrue(caught);

		caught = false;
		try {
			new Projection(new Attribute[]{Attribute.create(String.class, "b")}, relation1Free);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Inconsistent attributes", e.getMessage());
			caught = true;
		}
		Assert.assertTrue(caught);
		
		/*
		 * Simple plan that does an free access on relation R1 and projects the second column
		 */
		Projection accessProjection = new Projection(new Attribute[]{Attribute.create(Integer.class, "b")}, relation1Free);
		
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
		
		// TODO: plan execution fails: apparent bug at Projection.java line 174/176
		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(accessProjection);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones you expected
		Assert.assertNotNull(result);
		Assert.assertEquals(N, result.size());
		
		// TODO: test that the result data include Tuples containing only the second element (i.e. attribute "b") 
		// Assert.assertArrayEquals(tuples1.toArray(), result.getData().toArray());

		
		/*
		 *  Simple plan that does a free access on relation R1, then selects the rows where the value
		 *  of first column is "1" and the second column equals the third and finally projects the last column 
		 */
		Condition condition = ConjunctiveCondition.create(new SimpleCondition[]{ConstantEqualityCondition.create(0, TypedConstant.create(1)), 
				AttributeEqualityCondition.create(1, 2)});
		Projection accessSelectProjection = new Projection(new Attribute[]{Attribute.create(Integer.class, "c")}, 
				new Selection(condition,relation1Free));

		//Execute the plan
		result = null;
		try {
			result = this.planExecution(accessSelectProjection);
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
		
		//Execute the plan
		result = null;
		try {
			result = this.planExecution(accessSelectProjection);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		Assert.assertEquals(N/4, result.size());
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(values2, x.getValues());
		
		

		
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
