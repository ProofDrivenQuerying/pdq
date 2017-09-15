package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

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
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Join;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.SymmetricMemoryHashJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

public class DependentJoinTest {

	//	TODO: tidy up by moving all this inside individual tests (since there's very little common setup here).
	AccessMethod am1 = AccessMethod.create("access_method1",new Integer[] {});
	AccessMethod am2 = AccessMethod.create("access_method2",new Integer[] {0});
	AccessMethod am3 = AccessMethod.create("access_method3",new Integer[] {0,1});

	TupleType tt2 = TupleType.DefaultFactory.create(Integer.class, Integer.class);
	TupleType tt3 = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);

	//TODO fix the dependent join constructor such that if the right child does not require any inputs from 
	//the left, then the constructor should throw exception 
	@SuppressWarnings("resource")
	@Test
	public void testDependentJoin() {

		InMemoryTableWrapper relation1 = new InMemoryTableWrapper("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
				new AccessMethod[] {am1});
		InMemoryTableWrapper relation2 = new InMemoryTableWrapper("R2", new Attribute[] {Attribute.create(Integer.class, "c"),
				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")},
				new AccessMethod[] {am1, am2, am3});

		// Free access on relation R1.
		Access relation1Free = new Access(relation1, am1);

		// Access on relation R2 that requires inputs on first position.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(100));

		Access relation2InputonFirst = new Access(relation2, am2);

		// Valid dependent join construction: the right child requires input in position 0,
		// which is supplied by the left child output in position 2 (attribute "c").
		// Hence the dependent join plan has no input attributes in this case.
		DependentJoin target = new DependentJoin(relation1Free, relation2InputonFirst);
		Assert.assertNotNull(target);
		Assert.assertEquals(0, target.getInputAttributes().length);

		// Constructor sanity checks
		
		AccessMethod am = AccessMethod.create("access_method",new Integer[] {1});
		Access relation2InputonSecond = new Access(relation2, am);

		boolean caught = false; 
		// Right child input attributes must be a subset of the left child output attributes.  
		try {
			new DependentJoin(relation1Free, relation2InputonSecond);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Note that it is the access method am2 that specifies that relation2 requires 
		// input(s) on the first position (i.e. position 0). The inputConstants1 map contains
		// the TypedConstant that provides that input.
		Access relation2ConstantInputonFirst = new Access(relation2, am2, inputConstants1);

		// A dependent join plan that takes the outputs of the first access and feeds them to the 
		// first input position (i.e. position 0) of the second accessed relation. 

		// An exception is thrown if the right child does not require any inputs from the left
		// (i.e. the following is not a valid dependent join).
		caught = false; 
		try {
			new DependentJoin(relation1Free, relation2ConstantInputonFirst);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// Note that there is currently no getJoinConditions in DependentJoin (but the fix is, probably,
		// to make DependentJoin a subclass of Join).

		//Assert.assertEquals(expected, target.getJoinConditions());

		// An exception is thrown if the right child requires inputs which are not supplied from the left.
		// Here the left child supplies inputs only for the first position of relation2 but not for the second one.
		caught = false; 
		try {
			new DependentJoin(relation1Free, new Access(relation2, am3));
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);
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

		InMemoryTableWrapper relationR = new InMemoryTableWrapper("R", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
				new AccessMethod[] {am1});

		InMemoryTableWrapper relationS = new InMemoryTableWrapper("S", new Attribute[] {Attribute.create(Integer.class, "b"),
				Attribute.create(Integer.class, "c"), Attribute.create(Integer.class, "d")},
				new AccessMethod[] {am1, am2, am3});

		DependentJoin target = new DependentJoin(new Access(relationR, am1), new Access(relationS, am2));

		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "b", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		// Create some tuples
		Integer[] values11 = new Integer[] {10, 11, 12};
		Integer[] values12 = new Integer[] {10, 11, 100};
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 2) == 0) ? values12 : values11;
			tuples1.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		Integer[] values21 = new Integer[] {11, 13, 14}; // matches "b" but not "c"
		Integer[] values22 = new Integer[] {99, 13, 14}; // fails to match "b"
		Integer[] values23 = new Integer[] {11, 12, 13}; // matches "b" with both values11 & values12 and "c" with values11 only.  
		int M = 33;
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		for (int i = 0; i != M; i++) {
			Integer[] values = values21;
			if (i % 2 == 0) {
				values = values22;
			}
			if (i % 3 == 0) {
				values = values23;
			}
			tuples2.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples   
		relationR.load(tuples1);
		relationS.load(tuples2);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected. 
		// values11 appears N/2 times in relation1 and values23 appears M/3 times in relation 3.
		Assert.assertNotNull(result);
		Assert.assertEquals((N/2)*(M/3), result.size());

	}

	@Test
	public void test2() {

		InMemoryTableWrapper relationR = new InMemoryTableWrapper("R", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
				new AccessMethod[] {am1});

		InMemoryTableWrapper relationS = new InMemoryTableWrapper("S", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "CONST"), Attribute.create(Integer.class, "c")},
				new AccessMethod[] {am1, am2, am3});

		// Access on relation R2 that requires inputs on first position.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(1, TypedConstant.create(100));

		// Note that it is the access method am2 that specifies that relation2 requires 
		// input(s) on the first two positions (i.e. positions 0 & 1). The inputConstants1 
		// map contains the TypedConstant that provides the input on position 1.
		Access relationSInputs01 = new Access(relationS, am3, inputConstants);

		// A dependent join plan that takes the outputs of the first access and feeds them to the 
		// first input position (i.e. position 0) of the second accessed relation. 
		DependentJoin target = new DependentJoin(new Access(relationR, am1), relationSInputs01);

		// Create some tuples
		Integer[] values11 = new Integer[] {10, 55, 12};
		Integer[] values12 = new Integer[] {9, 55, 12};
		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 100;
		for (int i = 0; i != N; i++) {
			Integer[] values = ((i % 2) == 0) ? values12 : values11;
			tuples1.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		Integer[] values21 = new Integer[] {10, 13, 14};
		Integer[] values22 = new Integer[] {10, 100, 14};
		Integer[] values23 = new Integer[] {10, 100, 12}; // matches "a" & "c" in values11
		int M = 33;
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		for (int i = 0; i != M; i++) {
			Integer[] values = values21;
			if (i % 2 == 0) {
				values = values22;
			}
			if (i % 3 == 0) {
				values = values23;
			}
			tuples2.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples   
		relationR.load(tuples1);
		relationS.load(tuples2);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// Check that the result tuples are the ones expected. 
		// values11 appears N/2 times in relation1 and values23 appears M/3 times in relation 3.
		Assert.assertNotNull(result);
		Assert.assertEquals((N/2)*(M/3), result.size());

		// Note that the result tuples contain the tuple from the right appended onto the tuple from the left.
		for (Tuple x:result.getData())
			Assert.assertArrayEquals(new Integer[] {10, 55, 12, 10, 100, 12}, x.getValues());

	}

	@Test
	public void test3() {

		//
		// Dependent join on S & R
		//
		InMemoryTableWrapper relationS = new InMemoryTableWrapper("S", new Attribute[] {Attribute.create(Integer.class, "a1"),
				Attribute.create(Integer.class, "b1"), Attribute.create(Integer.class, "c1")},
				new AccessMethod[] {am1});
		InMemoryTableWrapper relationR = new InMemoryTableWrapper("R", new Attribute[] {Attribute.create(Integer.class, "b1"),
				Attribute.create(Integer.class, "c1")},
				new AccessMethod[] {am1, am2, am3});

		// Free access on relation S.
		Access relationSFree = new Access(relationS, am1);
		// Access on relation R that requires inputs on first position.
		Access relationRInputonFirst = new Access(relationR, am2);

		DependentJoin dj = new DependentJoin(relationSFree, relationRInputonFirst);

		//
		// Equi-join on T1 & T2
		//
		InMemoryTableWrapper relationT1 = new InMemoryTableWrapper("T1", new Attribute[] {Attribute.create(Integer.class, "a1"),
				Attribute.create(Integer.class, "b2")},
				new AccessMethod[] {am1});
		InMemoryTableWrapper relationT2 = new InMemoryTableWrapper("T2", new Attribute[] {Attribute.create(Integer.class, "b2"),
				Attribute.create(Integer.class, "c2")},
				new AccessMethod[] {am1, am2, am3});

		// Free access on relations T1 & T2.
		Access relationT1Free = new Access(relationT1, am1);
		Access relationT2Free = new Access(relationT2, am1);

		Join ej = new SymmetricMemoryHashJoin(relationT1Free, relationT2Free);

		Join target = new SymmetricMemoryHashJoin(dj, ej);

		// Create some tuples
		Collection<Tuple> tuplesS = new ArrayList<Tuple>();
		int M = 116;
		for (int i = 0; i != M; i++) {
			Integer[] values = new Integer[] {((i % 2) == 0) ? 200 : 100, i, 1};
			tuplesS.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuplesR = new ArrayList<Tuple>();
		int N = 112;
		for (int i = 0; i != N; i++) {
			Integer[] values = new Integer[] {i + 10, ((i % 2) == 0) ? 0: 1};
			tuplesR.add(tt2.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		Collection<Tuple> tuplesT1 = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			Integer[] values = new Integer[] {((i % 2) == 0) ? 200 : 100, i + 50};
			tuplesT1.add(tt2.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuplesT2 = new ArrayList<Tuple>();
		for (int i = 0; i != M; i++) {
			Integer[] values = new Integer[] {i + 40,  -1};
			tuplesT2.add(tt2.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}


		// Load tuples   
		relationR.load(tuplesR);
		relationS.load(tuplesS);

		relationT1.load(tuplesT1);
		relationT2.load(tuplesT2);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		Assert.assertNotNull(result);

		// The result of dependent join dj has size (min(M, N + 10) - 10)/2
		// The result of equi-join ej has size min(M + 40, N + 50) - 50
		// The final result has size equal to (size(dj) * size(ej))/2
		Assert.assertEquals(((Math.min(M, N + 10) - 10)/2)*(Math.min(M + 40, N + 50) - 50)/2, result.size());

		for (Tuple x:result.getData()) {
			Assert.assertEquals(100, x.getValues()[0]);
			Assert.assertEquals(100, x.getValues()[5]);
			Assert.assertEquals(1, x.getValues()[2]);
			Assert.assertEquals(1, x.getValues()[4]);
		}
	}

	@Test
	public void test4() {

		//
		// Dependent join on R & S
		//
		InMemoryTableWrapper relationR = new InMemoryTableWrapper("R", new Attribute[] {Attribute.create(Integer.class, "a1"),
				Attribute.create(Integer.class, "b1"), Attribute.create(Integer.class, "c1")},
				new AccessMethod[] {am1});
		InMemoryTableWrapper relationS = new InMemoryTableWrapper("S", new Attribute[] {Attribute.create(Integer.class, "CONST"),
				Attribute.create(Integer.class, "c1")},
				new AccessMethod[] {am1, am2, am3});

		// Free access on relation R.
		Access relationRFree = new Access(relationR, am1);

		// Access on relation S that requires inputs on both positions.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(0, TypedConstant.create(100));
		Access relationSInputsonBoth = new Access(relationS, am2, inputConstants);

		DependentJoin dj = new DependentJoin(relationRFree, relationSInputsonBoth);

		//
		// Equi-join on T & V
		//
		InMemoryTableWrapper relationT = new InMemoryTableWrapper("T", new Attribute[] {Attribute.create(Integer.class, "b1"),
				Attribute.create(Integer.class, "c2")},
				new AccessMethod[] {am1});
		InMemoryTableWrapper relationV = new InMemoryTableWrapper("V", new Attribute[] {Attribute.create(Integer.class, "b1"),
				Attribute.create(Integer.class, "c3")},
				new AccessMethod[] {am1, am2, am3});

		// Free access on relations T & V.
		Access relationTFree = new Access(relationT, am1);
		Access relationVFree = new Access(relationV, am1);

		Join ej = new SymmetricMemoryHashJoin(relationTFree, relationVFree);

		DependentJoin target = new DependentJoin(dj, ej);

		// Create some tuples
		Collection<Tuple> tuplesR = new ArrayList<Tuple>();
		int N = 10;
		for (int i = 0; i != N; i++) {
			Integer[] values = new Integer[] {99, i, -i};
			tuplesR.add(tt3.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		int M = 10;
		Collection<Tuple> tuplesS = new ArrayList<Tuple>();
		for (int i = 0; i != M; i++) {
			Integer[] values = new Integer[] {100, -i};
			tuplesS.add(tt2.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		Collection<Tuple> tuplesT = new ArrayList<Tuple>();
		for (int i = 0; i != N; i++) {
			Integer[] values = new Integer[] {i, 2};
			tuplesT.add(tt2.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}
		Collection<Tuple> tuplesV = new ArrayList<Tuple>();
		for (int i = 0; i != M; i++) {
			Integer[] values = new Integer[] {i, 3};
			tuplesV.add(tt2.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples   
		relationR.load(tuplesR);
		relationS.load(tuplesS);

		relationT.load(tuplesT);
		relationV.load(tuplesV);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		Assert.assertNotNull(result);

		// The result of dependent join dj has size min(M, N)
		// The result of equi-join ej has size min(M, N)
		// The final result has size min(M, N)
		Assert.assertEquals(Math.min(M, N), result.size());
	}

	@Test
	public void test5() {

		// Bugfix identified in test4

		//
		// Dependent join on R & S
		//
		InMemoryTableWrapper relationRS = new InMemoryTableWrapper("R", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c"), Attribute.create(Integer.class, "CONST"), 
				Attribute.create(Integer.class, "c")},
				new AccessMethod[] {am1});
		InMemoryTableWrapper relationTV = new InMemoryTableWrapper("S", new Attribute[] {Attribute.create(Integer.class, "b"),
				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "e")},
				new AccessMethod[] {am1, am2, am3});

		AccessMethod am = AccessMethod.create("access_method2",new Integer[] {0,2});

		// Free access on relation R.
		Access relationRSFree = new Access(relationRS, am1);
		Access relationTVInputOnFirstAndThird = new Access(relationTV, am);

		DependentJoin target = new DependentJoin(relationRSFree, relationTVInputOnFirstAndThird);

		// Create some tuples

		TupleType tt4 = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class, Integer.class);
		TupleType tt5 = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class, Integer.class, Integer.class);

		Collection<Tuple> tuples1 = new ArrayList<Tuple>();
		int N = 1000;
		for (int i = 0; i != N; i++) {
			Integer[] values = new Integer[] {99, i, -i, 100, -i};
			tuples1.add(tt5.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		int M = 1000;
		Collection<Tuple> tuples2 = new ArrayList<Tuple>();
		for (int i = 0; i != M; i++) {
			Integer[] values = new Integer[] {i, 2, i, 3};
			tuples2.add(tt4.createTuple((Object[]) Arrays.copyOf(values, values.length)));
		}

		// Load tuples   
		relationRS.load(tuples1);
		relationTV.load(tuples2);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		Assert.assertNotNull(result);
		Assert.assertEquals(Math.min(M, N), result.size());

	}
}
