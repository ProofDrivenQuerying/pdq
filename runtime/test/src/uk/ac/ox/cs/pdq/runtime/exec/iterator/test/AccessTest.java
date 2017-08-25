package uk.ac.ox.cs.pdq.runtime.exec.iterator.test;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import java.util.Map;
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

	@SuppressWarnings("resource")
	@Test
	public void case1() {
		AccessMethod am1 = AccessMethod.create("access_method1",new Integer[] {});
		AccessMethod am2 = AccessMethod.create("access_method2",new Integer[] {0});
		AccessMethod am3 = AccessMethod.create("access_method2",new Integer[] {0,1});
		
		
		InMemoryTableWrapper relation1 = new InMemoryTableWrapper("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
				new AccessMethod[] {am1});
		
		InMemoryTableWrapper relation2 = new InMemoryTableWrapper("R2", new Attribute[] {Attribute.create(Integer.class, "c"),
				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")},
				new AccessMethod[] {am1, am2, am3});
	
		
		/*
		 * Free access on relation R1
		 */
		Access relation1Free = new Access(relation1, am1);
		
		// Create some tuples
		TupleType tt = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);
		
		Integer[] values1 = new Integer[] {10, 11, 12};
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			tuples1.add(tt.createTuple((Object[]) Arrays.copyOf(values1, values1.length)));
		}
		// NB: Alternative construction of random integer arrays requires Java 8:
		// Integer[] a = new Random(22).ints(relation1.getArity(), 0, 100).boxed().toArray(Integer[]::new);
		
		// Load tuples by calling the load method of relation1  
		Assert.assertEquals(0, relation1.getData().size());
		relation1.load(tuples1);
		Assert.assertEquals(N, relation1.getData().size());
		
		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(relation1Free);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones you expected
		Assert.assertNotNull(result);
		Assert.assertEquals(N, result.size());
		Assert.assertArrayEquals(tuples1.toArray(), result.getData().toArray());
		
//		/*
//		 * Free access on relation R2
//		 */
//		Access relation2Free = new Access(relation2, am1);
		
		/*
		 * Access on relation R2 that requires inputs on first position.
		 */
		//Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(100));
		Access relation2InputonFirst = new Access(relation2, am2, inputConstants1);
		

		// Create some tuples
		Integer[] values2 = new Integer[] {100, 101, 102};
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 2) == 0) ? values2 : values1;
			tuples2.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples by calling the load method of relation2  
		Assert.assertEquals(0, relation2.getData().size());
		relation2.load(tuples2);
		Assert.assertEquals(N, relation2.getData().size());

		//Execute the plan.
		result = null;
		try {
			result = this.planExecution(relation2InputonFirst);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones you expected
		Assert.assertNotNull(result);
		Assert.assertEquals(N/2, result.size());
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(values2, x.getValues());

		/*
		 * Access on relation R2 that requires inputs on two first positions.
		 */
		//Suppose that a user already specified the typed constants "100" and "200" to access it 
		Map<Integer, TypedConstant> inputConstants2 = new HashMap<>();
		inputConstants2.put(0, TypedConstant.create(100));
		inputConstants2.put(1, TypedConstant.create(200));
		
		Access relation2InputonFirstAndSecond = new Access(relation2, am3, inputConstants2);

		// Create some tuples
		Integer[] values3 = new Integer[] {100, 200, 201};
		Collection<Tuple> tuples3 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 2) == 0) ? values2 : values1;
			if ((i % 4) == 0)
				values = values3;
			tuples3.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples by calling the load method of relation2
		relation2.clear();
		Assert.assertEquals(0, relation2.getData().size());
		relation2.load(tuples3);
		Assert.assertEquals(N, relation2.getData().size());

		//Execute the plan.
		result = null;
		try {
			result = this.planExecution(relation2InputonFirstAndSecond);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones you expected
		Assert.assertNotNull(result);
		Assert.assertEquals(N/4, result.size());
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(values3, x.getValues());

		
		// An exception is thrown if the input constants map is inconsistent with the access method:
		boolean caught = false;
		try {
			new Access(relation2, am1, inputConstants2);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);
		
		caught = false;
		try {
			new Access(relation2, am2, inputConstants2);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

	}
	
	
	// TODO: put the rest in its proper place:
	@Test
	public void case2() {
		
//		//Simple plan that does an free access on relation R1 and projects the first column
//		Projection project = new Projection(new Attribute[]{Attribute.create(Integer.class, "a")}, relation1Free);
//		
//		//Simple plan that does an free access on relation R1, 
//		//then selects the rows where the value of first column is "1" and the second column equals the third
//		//and finally projects the last column
//		Condition condition = ConjunctiveCondition.create(new SimpleCondition[]{ConstantEqualityCondition.create(0, TypedConstant.create(1)), 
//				AttributeEqualityCondition.create(1, 2)});
//		Projection accessSelectProject = new Projection(new Attribute[]{Attribute.create(Integer.class, "c")}, 
//				new Selection(condition,relation1Free));
//		
//		//Simple plan that joins the results of two accesses.
//		//The constructor assumes that two attributes should be equal if they have the same name. 
//		//This is the so called equijoin. 
//		//We use the nested loop join algorithm
//		Join nestedLoopJoin = new NestedLoopJoin(relation1Free, relation2Free);
//		
//		//Here we use the hash join algorithm instead
//		Join hashJoin = new SymmetricMemoryHashJoin(relation1Free, relation2Free);
//		//In the above cases, the constructor should create a joinCondition consisting of a single 
//		//AttributeEqualityCondition specifying that the third attribute of relation1 (2) is equal to the first 
//		//attribute of relation2 (0)
//		
//		//A dependent join plan that takes the outputs of the first access and feeds them to the first input position of 
//		//the second accessed relation 
//		DependentJoin dependentJoin = new DependentJoin(relation1Free, relation2InputonFirst);
//		
//		//A dependent join plan that takes the outputs of the first access and feeds them to the first input position of 
//		//the second accessed relation. It accesses the second position of the second relation using a constant 
//		Map<Integer, TypedConstant> inputConstants3 = new HashMap<>();
//		inputConstants3.put(1, TypedConstant.create(200));
//		DependentJoin dependentJoin2 = new DependentJoin(relation1Free, new Access(relation2, am3, inputConstants3));
//	
//		//This dependent join plan cannot be executed since 
//		//we have inputs only for the first position of relation2 but not for the second one
//		// DependentJoin dependentJoin3 = new DependentJoin(relation1Free, new Access(relation2, am3));
//		
//		
//		//TODO sanity check the constructors
//		//TODO execute all plans using the method below 
//		//TODO create more test cases...
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
