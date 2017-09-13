package uk.ac.ox.cs.pdq.runtime.exec.iterator.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
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
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

public class DependentJoinTest {

	AccessMethod am1 = AccessMethod.create("access_method1",new Integer[] {});
	AccessMethod am2 = AccessMethod.create("access_method2",new Integer[] {0});
	AccessMethod am3 = AccessMethod.create("access_method2",new Integer[] {0,1});
	
	InMemoryTableWrapper relation1 = new InMemoryTableWrapper("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
			Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
			new AccessMethod[] {am1});
	InMemoryTableWrapper relation2 = new InMemoryTableWrapper("R2", new Attribute[] {Attribute.create(Integer.class, "c"),
			Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")},
			new AccessMethod[] {am1, am2, am3});
	
	TupleType tt = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);

	@Test
	public void testDependentJoin() {

		// Free access on relation R1.
		Access relation1Free = new Access(relation1, am1);
		
		// Access on relation R2 that requires inputs on first position.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(100));
		
		// Note that it is the access method am2 that specifies that relation2 requires 
		// input(s) on the first position (i.e. position 0). The inputConstants1 map contains
		// the TypedConstant that provides that input.
		Access relation2InputonFirst = new Access(relation2, am2, inputConstants1);

		
		// A dependent join plan that takes the outputs of the first access and feeds them to the 
		// first input position (i.e. position 0) of the second accessed relation. 
		DependentJoin target = new DependentJoin(relation1Free, relation2InputonFirst);
	
		// TODO: constructor sanity checks
		
		// Note that there is currently no getJoinConditions in DependentJoin (but the fix is, probably,
		// to make DependentJoin a subclass of Join).
		
		//Assert.assertEquals(expected, target.getJoinConditions());

	}

	/*
	 * The following are integration tests: DependentJoin instances are constructed and executed.
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

		// Free access on relation R1.
		Access relation1Free = new Access(relation1, am1);
		
		// Access on relation R2 that requires inputs on first position.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(100));
		
		// Note that it is the access method am2 that specifies that relation2 requires 
		// input(s) on the first position (i.e. position 0). The inputConstants1 map contains
		// the TypedConstant that provides that input.
		Access relation2InputonFirst = new Access(relation2, am2, inputConstants1);

		// A dependent join plan that takes the outputs of the first access and feeds them to the 
		// first input position (i.e. position 0) of the second accessed relation. 
		DependentJoin target = new DependentJoin(relation1Free, relation2InputonFirst);
		
		// MOST IMP TODO: check this:
		// The dependent join knows that the values corresponding to the *last* attribute in relation1 
		// (i.e. attribute "c" in position 2) are to be used as inputs to the *first* position (i.e. position 0) 
		// in relation2 because:
		//   i) Access method am2, which specifies that inputs to position 0 must be provided, was used to 
		// 		construct the relation2 Access, *and*
		// 	ii) The name of the attribute in position 0 of relation2 matches that in position 2 of relation1 
		// 		(i.e. both attributes are named "c")
		
		// TODO: TWO IMPORTANT QUESTIONS:
		// - Is the above correct? Specifically, when deciding which outputs from relation1 are to be passed as
		// 	inputs to relation2, is this done simply by matching the name? This is easy to test!
		// - If so (i.e. "dependent attributes" must have identical names), in what way is DependentJoin different
		// 	from the standard equi-join? i.e. try to exhibit a case in which a DependentJoin (as implemented here) 
		//	cannot be re-cast as an equivalent equi-join. 
		
		// ALSO IMP TODO: do a test where the inputs to relation2 are not a pre-specified constant 
		// (i.e. values are matched from the output of relation1). 
		// - Once again, isn't this just an equi-join?
		
		// Create some tuples
		Integer[] values11 = new Integer[] {10, 11, 12};
		Integer[] values12 = new Integer[] {10, 11, 100};
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 2) == 0) ? values12 : values11;
			tuples1.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		Integer[] values21 = new Integer[] {12, 13, 14};
		Integer[] values22 = new Integer[] {100, 13, 14};
		int M = 33;
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		for (int i = 0; i != M; i++) {
			Integer[] values = ((i % 3) == 0) ? values22 : values21;
			tuples2.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples   
		relation1.load(tuples1);
		relation2.load(tuples2);
		
		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
		// Check that the result tuples are the ones expected. 
		// values12 appears N/2 times in relation1 and values22 appears M/3 times in relation 3.
		Assert.assertNotNull(result);
		Assert.assertEquals((N/2)*(M/3), result.size());
		
		// Note that the result tuples contain the tuple from the right appended onto the tuple from the left.
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(new Integer[] {10, 11, 100, 100, 13, 14}, x.getValues());
		
		/**
		 * QUESTION: why does the number of results double if we change the name of attribute "c" in one of the relations?
		 */
	}
	
	@Test
	public void test2() {

		// Free access on relation R1.
		Access relation1Free = new Access(relation1, am1);
		
		// Access on relation R2 that requires inputs on position 1 (i.e. the *second* position, attribute "d").
		// Suppose that a user already specified the typed constant "100" to access it.
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(1, TypedConstant.create(100));
		Access relation2InputConstant = new Access(relation2, am2, inputConstants);		
		
		// A dependent join plan that takes the outputs of the first access and feeds them to 
		// the first input position of the second accessed relation. 
		DependentJoin target = new DependentJoin(relation1Free, relation2InputConstant);
		
		// Create some tuples
		Integer[] values11 = new Integer[] {10, 11, 12};
		Integer[] values12 = new Integer[] {10, 11, 13};
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 2) == 0) ? values12 : values11;
			tuples1.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		
		Integer[] values21 = new Integer[] {12, 13, 14};
		Integer[] values22 = new Integer[] {12, 100, 14};
		int M = 200;
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		for (int i = 0; i != M; i++) {
			Integer[] values = ((i % 3) == 0) ? values22 : values21;
			tuples2.add(tt.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples   
		relation1.load(tuples1);
		relation2.load(tuples2);
		
		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
		// Check that the result tuples are the ones expected. 
		// values1 appears 50 times in relation2 and values3 appears 67 times in relation 3.
		Assert.assertNotNull(result);
		Assert.assertEquals(50*67, result.size());
		
		// Note that the result tuples contain the tuple from the right appended onto the tuple from the left.
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(new Integer[] {10, 11, 12, 9, 10, 11, 13}, x.getValues());

		
		// ... something like this (copied from NestLoopJoinTest):
//		// Here the join condition is only satisfied between values1 and values3.
//		// values1 appears 50 times in relation2 and values3 appears 67 times in relation 3.
//		Assert.assertNotNull(result);
//		Assert.assertEquals(50*67, result.size());
//		
//		// Note that the result tuples contain the tuple from the right appended onto the tuple from the left.
//		for (Tuple x:result.getData())
//			Assert.assertArrayEquals(new Integer[] {10, 11, 12, 9, 10, 11, 13}, x.getValues());

	}
		
	/// TODO:  
	
//	//A dependent join plan that takes the outputs of the first access and feeds them to the first input position of 
//	//the second accessed relation. It accesses the second position of the second relation using a constant 
//	Map<Integer, TypedConstant> inputConstants3 = new HashMap<>();
//	inputConstants3.put(1, TypedConstant.create(200));
//	DependentJoin dependentJoin2 = new DependentJoin(relation1Free, new Access(relation2, am3, inputConstants3));
//
//	//This dependent join plan cannot be executed since 
//	//we have inputs only for the first position of relation2 but not for the second one
//	// DependentJoin dependentJoin3 = new DependentJoin(relation1Free, new Access(relation2, am3));
//	

	
}
