package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import uk.ac.ox.cs.pdq.runtime.exec.iterator.DependentJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Join;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.NestedLoopJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.SymmetricMemoryHashJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

public class DependentJoinTest {

	//	TODO: tidy up by moving all this inside individual tests (since there's very little common setup here).
	AccessMethod amFree = AccessMethod.create("free_access",new Integer[] {});
	AccessMethod am0 = AccessMethod.create("access_0", new Integer[] {0});
	AccessMethod am1 = AccessMethod.create("access_0", new Integer[] {1});
	AccessMethod am01 = AccessMethod.create("access_01",new Integer[] {0,1});
	AccessMethod am3 = AccessMethod.create("access_3", new Integer[] {3});
	AccessMethod am6 = AccessMethod.create("access_6", new Integer[] {6});

	TupleType tt2 = TupleType.DefaultFactory.create(Integer.class, Integer.class);
	TupleType tt3 = TupleType.DefaultFactory.create(Integer.class, Integer.class, Integer.class);

	//TODO fix the dependent join constructor such that if the right child does not require any inputs from 
	//the left, then the constructor should throw exception 
	@SuppressWarnings("resource")
	@Test
	public void testDependentJoin() {

		InMemoryTableWrapper relation1 = new InMemoryTableWrapper("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
				new AccessMethod[] {amFree});
		InMemoryTableWrapper relation2 = new InMemoryTableWrapper("R2", new Attribute[] {Attribute.create(Integer.class, "c"),
				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")},
				new AccessMethod[] {amFree, am0, am01});

		// Free access on relation R1.
		Access relation1Free = new Access(relation1, amFree);

		// Access on relation R2 that requires inputs on first position.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(100));

		Access relation2InputonFirst = new Access(relation2, am0);

		// Valid dependent join construction: the right child requires input in position 0,
		// which is supplied by the left child output in position 2 (attribute "c").
		// Hence the dependent join plan has no input attributes in this case.
		DependentJoin target = new DependentJoin(relation1Free, relation2InputonFirst);
		Assert.assertNotNull(target);
		Assert.assertEquals(0, target.getInputAttributes().length);

		// Note that there is currently no getJoinConditions in DependentJoin (but the fix is, probably,
		// to make DependentJoin a subclass of Join).

		// Assert.assertEquals(expected, target.getJoinConditions());

		/*
		 *  Constructor sanity checks
		 */

		// Note that it is the access method am0 that specifies that relation2 requires 
		// input(s) on the first position (i.e. position 0). The inputConstants1 map contains
		// the TypedConstant that provides that input.
		Access relation2ConstantInputonFirst = new Access(relation2, am0, inputConstants1);

		// An exception is thrown if the right child does not require any inputs from the left
		// (i.e. the following is not a valid dependent join).
		boolean caught = false; 
		try {
			new DependentJoin(relation1Free, relation2ConstantInputonFirst);
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// An exception is thrown if the right child requires inputs which are not supplied from the left.
		// Here the left child supplies inputs only for the first position of relation2 but not for the second one.
		caught = false; 
		try {
			new DependentJoin(relation1Free, new Access(relation2, am01));
		} catch (IllegalArgumentException e) {
			caught = true;
		}
		Assert.assertTrue(caught);

		// An exception is thrown if the right child input attributes are not a subset of the 
		// left child output attributes.  
		AccessMethod am = AccessMethod.create("access_method",new Integer[] {1});
		Access relation2InputonSecond = new Access(relation2, am);

		caught = false; 
		try {
			new DependentJoin(relation1Free, relation2InputonSecond);
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
				new AccessMethod[] {amFree});

		InMemoryTableWrapper relationS = new InMemoryTableWrapper("S", new Attribute[] {Attribute.create(Integer.class, "b"),
				Attribute.create(Integer.class, "c"), Attribute.create(Integer.class, "d")},
				new AccessMethod[] {amFree, am0, am01});

		DependentJoin target = new DependentJoin(new Access(relationR, amFree), new Access(relationS, am0));

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
				new AccessMethod[] {amFree});

		InMemoryTableWrapper relationS = new InMemoryTableWrapper("S", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "CONST"), Attribute.create(Integer.class, "c")},
				new AccessMethod[] {amFree, am0, am01});

		// Access on relation R2 that requires inputs on first position.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(1, TypedConstant.create(100));

		// Note that it is the access method am0 that specifies that relation2 requires 
		// input(s) on the first two positions (i.e. positions 0 & 1). The inputConstants1 
		// map contains the TypedConstant that provides the input on position 1.
		Access relationSInputs01 = new Access(relationS, am01, inputConstants);

		// A dependent join plan that takes the outputs of the first access and feeds them to the 
		// first input position (i.e. position 0) of the second accessed relation. 
		DependentJoin target = new DependentJoin(new Access(relationR, amFree), relationSInputs01);

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
				new AccessMethod[] {amFree});
		InMemoryTableWrapper relationR = new InMemoryTableWrapper("R", new Attribute[] {Attribute.create(Integer.class, "b1"),
				Attribute.create(Integer.class, "c1")},
				new AccessMethod[] {amFree, am0, am01});

		// Free access on relation S.
		Access relationSFree = new Access(relationS, amFree);
		// Access on relation R that requires inputs on first position.
		Access relationRInputonFirst = new Access(relationR, am0);

		DependentJoin dj = new DependentJoin(relationSFree, relationRInputonFirst);

		//
		// Equi-join on T1 & T2
		//
		InMemoryTableWrapper relationT1 = new InMemoryTableWrapper("T1", new Attribute[] {Attribute.create(Integer.class, "a1"),
				Attribute.create(Integer.class, "b2")},
				new AccessMethod[] {amFree});
		InMemoryTableWrapper relationT2 = new InMemoryTableWrapper("T2", new Attribute[] {Attribute.create(Integer.class, "b2"),
				Attribute.create(Integer.class, "c2")},
				new AccessMethod[] {amFree, am0, am01});

		// Free access on relations T1 & T2.
		Access relationT1Free = new Access(relationT1, amFree);
		Access relationT2Free = new Access(relationT2, amFree);

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
				new AccessMethod[] {amFree});
		InMemoryTableWrapper relationS = new InMemoryTableWrapper("S", new Attribute[] {Attribute.create(Integer.class, "CONST"),
				Attribute.create(Integer.class, "c1")},
				new AccessMethod[] {amFree, am0, am01});

		// Free access on relation R.
		Access relationRFree = new Access(relationR, amFree);

		// Access on relation S that requires inputs on both positions.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(0, TypedConstant.create(100));
		Access relationSInputOnFirst = new Access(relationS, am0, inputConstants);

		DependentJoin dj = new DependentJoin(relationRFree, relationSInputOnFirst);

		//
		// Equi-join on T & V
		//
		InMemoryTableWrapper relationT = new InMemoryTableWrapper("T", new Attribute[] {Attribute.create(Integer.class, "b1"),
				Attribute.create(Integer.class, "c2")},
				new AccessMethod[] {amFree});
		InMemoryTableWrapper relationV = new InMemoryTableWrapper("V", new Attribute[] {Attribute.create(Integer.class, "b1"),
				Attribute.create(Integer.class, "c3")},
				new AccessMethod[] {amFree, am0, am01});

		// Free access on relations T & V.
		Access relationTInputOnFirst = new Access(relationT, am0);
		Access relationVInputOnFirst = new Access(relationV, am0);

		Join ej = new SymmetricMemoryHashJoin(relationTInputOnFirst, relationVInputOnFirst);

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
				new AccessMethod[] {amFree});
		InMemoryTableWrapper relationTV = new InMemoryTableWrapper("S", new Attribute[] {Attribute.create(Integer.class, "b"),
				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "e")},
				new AccessMethod[] {amFree, am0, am01});

		AccessMethod am = AccessMethod.create("access_method2",new Integer[] {0,2});

		// Free access on relation R.
		Access relationRSFree = new Access(relationRS, amFree);
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

	Attribute[] attributes_S = new Attribute[] {
			Attribute.create(Integer.class, "S_SUPPKEY"),
			Attribute.create(String.class, "S_NAME"),
			Attribute.create(String.class, "S_ADDRESS"),
			Attribute.create(Integer.class, "S_NATIONKEY"),
			Attribute.create(String.class, "S_PHONE"),
			Attribute.create(Float.class, "S_ACCTBAL"),
			Attribute.create(String.class, "S_COMMENT")
	};

	Attribute[] attributes_P = new Attribute[] {
			Attribute.create(Integer.class, "P_PARTKEY"),
			Attribute.create(String.class, "P_NAME"),
			Attribute.create(String.class, "P_MFGR"),
			Attribute.create(String.class, "P_BRAND"),
			Attribute.create(String.class, "P_TYPE"),
			Attribute.create(Integer.class, "P_SIZE"),
			Attribute.create(String.class, "P_CONTAINER"),
			Attribute.create(Float.class, "P_RETAILPRICE"),
			Attribute.create(String.class, "P_COMMENT")
	};

	Attribute[] attributes_PS = new Attribute[] {
			Attribute.create(Integer.class, "PS_PARTKEY"),
			Attribute.create(String.class, "PS_SUPPKEY"),
			Attribute.create(String.class, "PS_AVAILQTY"),
			Attribute.create(String.class, "PS_SUPPLYCOST"),
			Attribute.create(String.class, "PS_COMMENT")
	};

	Attribute[] attributes_O = new Attribute[] {
			Attribute.create(String.class, "O_ORDERKEY"),
			Attribute.create(Integer.class, "O_CUSTKEY"),
			Attribute.create(Integer.class, "O_ORDERSTATUS"),
			Attribute.create(Integer.class, "O_TOTALPRICE"),
			Attribute.create(String.class, "O_ORDERDATE"),
			Attribute.create(Float.class, "O_ORDER-PRIORITY"),
			Attribute.create(String.class, "O_CLERK"),
			Attribute.create(String.class, "O_SHIP-PRIORITY"),
			Attribute.create(String.class, "O_COMMENT")
	};

	SQLRelationWrapper postgresqlRelationCustomer = new PostgresqlRelationWrapper(this.getProperties(), "CUSTOMER", 
			attributes_C, new AccessMethod[] {amFree, am3, am6});
	SQLRelationWrapper postgresqlRelationNation = new PostgresqlRelationWrapper(this.getProperties(), "NATION", 
			attributes_N, new AccessMethod[] {amFree});
	SQLRelationWrapper postgresqlRelationSupplier = new PostgresqlRelationWrapper(this.getProperties(), "SUPPLIER", 
			attributes_S, new AccessMethod[] {amFree});
	SQLRelationWrapper postgresqlRelationPart = new PostgresqlRelationWrapper(this.getProperties(), "PART", 
			attributes_P, new AccessMethod[] {amFree});
	SQLRelationWrapper postgresqlRelationPartsupp = new PostgresqlRelationWrapper(this.getProperties(), "PARTSUPP", 
			attributes_PS, new AccessMethod[] {amFree});
	SQLRelationWrapper postgresqlRelationOrders = new PostgresqlRelationWrapper(this.getProperties(), "ORDERS", 
			attributes_O, new AccessMethod[] {amFree});


	@Test
	public void test6() {

		/*
		 * DependentJoin(Access1, Access2).
		 * Left: free access on CUSTOMER relation
		 * Right: access NATION relation with input required on 0'th position (NATIONKEY)
		 */
		DependentJoin target = new DependentJoin(new Access(postgresqlRelationCustomer, amFree), 
				new Access(postgresqlRelationNation, am0));
 
		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "NATIONKEY", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		Assert.assertNotNull(result);

		// Since the NATIONKEY is unique in the NATION relation, there is a single result tuple
		// for each tuple in the CUSTOMER relation.
		// SELECT COUNT(*) FROM CUSTOMER, NATION WHERE CUSTOMER.c_nationkey = NATION.n_nationkey;
		Assert.assertEquals(150000, result.size());
	}

	@Test
	public void test6a() {

		/*
		 * DependentJoin(Access1, Access2).
		 * Left: access on CUSTOMER relation with constant input required on MKTSEGMENT 
		 * Right: access NATION relation with input required on 0'th position (NATIONKEY)
		 */
		Map<Integer, TypedConstant> inputConstants = new HashMap<>();
		inputConstants.put(6, TypedConstant.create("AUTOMOBILE"));

		// TODO:
		// The following construction fails due to the naming convention used in TPC-H. The 
		// NATIONKEY attribute is prefixed with C_ in the left child output attributes (from CUSTOMER),
		// and with N_ in the right child input attributes (from NATION), so the DependentJoin
		// constructor fails (at line 86), since it requires the right child input attribute names
		// to be a subset of the left child output attribute names.
		
		DependentJoin target = new DependentJoin(new Access(postgresqlRelationCustomer, am6, inputConstants), 
				new Access(postgresqlRelationNation, am0));
 
		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "NATIONKEY", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		Assert.assertNotNull(result);

		// Since the NATIONKEY is unique in the NATION relation, there is a single result tuple
		// for each tuple in the CUSTOMER relation.
		// SELECT COUNT(*) FROM CUSTOMER, NATION WHERE CUSTOMER.c_mktsegment='AUTOMOBILE' AND CUSTOMER.c_nationkey=NATION.n_nationkey;
		Assert.assertEquals(29752, result.size());
	}
	
	@Test
	public void test7() {

		/*
		 * DependentJoin(Access1, Selection(Access2))
		 * Left: free access on NATION relation
		 * Right: access CUSTOMER relation with input required on 3rd position (NATIONKEY), 
		 * 			then Select MKTSEGMENT = "AUTOMOBILE"  
		 */
		Condition mktsegmentCondition = ConstantEqualityCondition.create(6, TypedConstant.create("AUTOMOBILE"));
		Selection selection = new Selection(mktsegmentCondition, new Access(postgresqlRelationCustomer, am3));

		DependentJoin target = new DependentJoin(new Access(postgresqlRelationNation, amFree), 
				selection);

		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "NATIONKEY", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		Assert.assertNotNull(result);
		
		// Execute a join manually to determine the expected size.
		
//		WITH right_child AS (
//				SELECT * FROM CUSTOMER WHERE CUSTOMER.c_mktsegment='AUTOMOBILE'
//				), left_child AS ( 
//		        SELECT * FROM NATION
//		        )
//		SELECT * FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;
		
		// equivalently:
		// WITH right_child AS (SELECT * FROM CUSTOMER WHERE CUSTOMER.c_mktsegment='AUTOMOBILE'), left_child AS (SELECT * FROM NATION) SELECT count(*) FROM left_child, right_child WHERE left_child.n_nationkey = right_child.c_nationkey;

		Assert.assertEquals(29752, result.size());
		// TODO: 
		// - check the column names if possible (or number of columns if not)
		// - check that the common attributes (by name) have common values.

	}

	@Test
	public void test8() {

		/*
		 * Plan: DependentJoin(Selection(Access1), Access2)
		 */
		Condition suppkeyCondition = ConstantEqualityCondition.create(0, TypedConstant.create(22)); // TODO.
		Selection selection = new Selection(suppkeyCondition, new Access(postgresqlRelationSupplier, amFree));

		DependentJoin target = new DependentJoin(selection, 
				new Access(postgresqlRelationNation, am0));

		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "NATIONKEY", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// TODO. Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		// TODO: execute a join manually to determine the expected size.
		Assert.assertEquals(1000, result.size());
		// TODO: check that the common attributes (by name) have common values.

	}

	/*
	 * Stress tests
	 */

	@Test
	public void stressTest1() {

		/*
		 * Plan: PROJECTION(DependentJoin(DependentJoin(NATION, SUPPLIER), DependentJoin(PART, PARTSUPP)))
		 */
		DependentJoin djLeft = new DependentJoin(new Access(postgresqlRelationNation, amFree), 
				new Access(postgresqlRelationSupplier, am3));
		// In the access on the PARTSUPP relation the first attribute (PARTKEY) is supplied by
		// the free access on the PART relation. The second attribute (SUPPKEY) will be supplied
		// in the outer nested DependentJoin.
		DependentJoin djRight = new DependentJoin(new Access(postgresqlRelationPart, amFree), 
				new Access(postgresqlRelationPartsupp, am01));

		// Check that the left DependentJoin plan has no input attributes (since the required 
		// NATIONKEY attribute is supplied by the free access on the NATION relation).
		Assert.assertEquals(0, djLeft.getInputAttributes().length);
		
		// Check that the right DependentJoin plan has one input attribute (to be supplied
		// by the outer nested DependentJoin).
		Assert.assertEquals(1, djRight.getInputAttributes().length);

		DependentJoin dj = new DependentJoin(djLeft, djRight);
		
		Projection target = new Projection(new Attribute[]{ Attribute.create(String.class, "N_NAME"), 
				Attribute.create(String.class, "S_NAME"), Attribute.create(String.class, "P_NAME"), 
				Attribute.create(Integer.class, "PS_AVAILQTY")}, dj); 


		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "SUPPKEY", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// TODO. Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		// TODO: execute a join manually to determine the expected size.
		Assert.assertEquals(1000, result.size());
		// TODO: check that the common attributes (by name) have common values.	

	}

	@Test
	public void stressTest2() {

		/*
		 * Plan: PROJECTION(DependentJoin(DependentJoin(NATION, SUPPLIER), DependentJoin(CUSTOMER, ORDERS)))
		 */
		DependentJoin djLeft = new DependentJoin(new Access(postgresqlRelationNation, amFree), 
				new Access(postgresqlRelationSupplier, am3));
		// In the access on the ORDERS relation the second attribute (CUSTKEY) is supplied by
		// the access on the CUSTOMER relation, which itself requires input on the NATIONKEY 
		// attribute, to be supplied by the outer nested DependentJoin.  
		DependentJoin djRight = new DependentJoin(new Access(postgresqlRelationCustomer, am3), 
				new Access(postgresqlRelationOrders, am1));
		
		// Check that the left DependentJoin plan has no input attributes (since the required 
		// NATIONKEY attribute is supplied by the free access on the NATION relation).
		Assert.assertEquals(0, djLeft.getInputAttributes().length);
		
		// Check that the right DependentJoin plan has one input attribute (to be supplied
		// by the outer nested DependentJoin).
		Assert.assertEquals(1, djRight.getInputAttributes().length);

		DependentJoin dj = new DependentJoin(djLeft, djRight);
		
		Projection target = new Projection(new Attribute[]{ Attribute.create(String.class, "N_NAME"), 
				Attribute.create(String.class, "S_NAME"), Attribute.create(String.class, "C_NAME"), 
				Attribute.create(String.class, "O_ORDERSTATUS")}, dj); 

		// Check that the plan has no input attributes (the left child has no input attributes
		// and the right child has only one, namely "NATIONKEY", which is supplied by the left child).
		Assert.assertEquals(0, target.getInputAttributes().length);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// TODO. Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		// TODO: execute a join manually to determine the expected size.
		Assert.assertEquals(1000, result.size());
		// TODO: check that the common attributes (by name) have common values.	

	}

	/*
	 * TODO: MISSING TWO TESTS HERE:
	 */
	/*
	 * Plan: PROJECTION(Join(DependentJoin(NATION, SUPPLIER), DependentJoin(PART, PARTSUPP)))
	 */
	/*
	 * Plan: PROJECTION(Join(DependentJoin(NATION, SUPPLIER), DependentJoin(CUSTOMER, ORDERS)))
	 */

	
	@Test
	public void stressTest3() {

		/*
		 * Plan: 
		 * DependentJoin(DependentJoin(NATION, Selection(SUPPLIER)), DependentJoin(Selection(PART), PARTSUPP))
		 */
		
		// Select on the SUPPLIER SUPPKEY attribute with input required on the NATIONKEY attribute.
		Condition conditionLeft = ConstantEqualityCondition.create(0, TypedConstant.create(22));
		Selection selectionLeft = new Selection(conditionLeft, new Access(postgresqlRelationSupplier, am3));
		DependentJoin djLeft = new DependentJoin(new Access(postgresqlRelationNation, amFree), selectionLeft);

		// Select on the PART TYPE attribute.
		Condition conditionRight = ConstantEqualityCondition.create(4, TypedConstant.create("TODO"));
		Selection selectionRight = new Selection(conditionRight, new Access(postgresqlRelationPart, amFree));
		DependentJoin djRight = new DependentJoin(selectionRight, new Access(postgresqlRelationPartsupp, am01));
	
		// Check that the left DependentJoin plan has no input attributes (since the required 
		// NATIONKEY attribute is supplied by the free access on the NATION relation).
		Assert.assertEquals(0, djLeft.getInputAttributes().length);
		
		// Check that the right DependentJoin plan has one input attribute, SUPPKEY, to be supplied
		// by the outer nested DependentJoin. (The PARTKEY attribute is supplied by the selectionRight).
		Assert.assertEquals(1, djRight.getInputAttributes().length);

		DependentJoin target = new DependentJoin(djLeft, djRight);
	
		Assert.assertEquals(0, target.getInputAttributes().length);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// TODO. Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		// TODO: execute a join manually to determine the expected size.
		Assert.assertEquals(1000, result.size());
		// TODO: check that the common attributes (by name) have common values.	
	}
	
	/*
	 * TODO: MISSING TWO TESTS HERE:
	 */
	/*
	 * Plan: 
	 * DependentJoin(DependentJoin(Selection(NATION), SUPPLIER), DependentJoin(CUSTOMER, Selection(ORDERS)))
	 */
	/*
	 * Plan: 
	 * DependentJoin(Join(NATION, Selection(SUPPLIER)), DependentJoin(Selection(PART), PARTSUPP))
	 */

	
	@Test
	public void stressTest4() {

		/*
		 * Plan: 
		 * Join(DependentJoin(Selection(NATION), SUPPLIER), DependentJoin(CUSTOMER, Selection(ORDERS)))
		 */
		// Select on the SUPPLIER SUPPKEY attribute with input required on the NATIONKEY attribute.
		Condition conditionLeft = ConstantEqualityCondition.create(0, TypedConstant.create(22));
		Selection selectionLeft = new Selection(conditionLeft, new Access(postgresqlRelationSupplier, am3));
		DependentJoin djLeft = new DependentJoin(new Access(postgresqlRelationNation, amFree), selectionLeft);
		
		// Select on the ORDERS ORDERSTATUS attribute with input required on the CUSTKEY attribute.
		Condition conditionRight = ConstantEqualityCondition.create(2, TypedConstant.create("TODO"));
		Selection selectionRight = new Selection(conditionRight, new Access(postgresqlRelationOrders, am1));
		DependentJoin djRight = new DependentJoin(new Access(postgresqlRelationCustomer, amFree), selectionRight);
		
		// Check that the left & right DependentJoin have no no input attributes.
		Assert.assertEquals(0, djLeft.getInputAttributes().length);
		Assert.assertEquals(0, djRight.getInputAttributes().length);

		Join target = new NestedLoopJoin(djLeft, djRight);

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// TODO. Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		// TODO: execute a join manually to determine the expected size.
		Assert.assertEquals(1000, result.size());
		// TODO: check that the common attributes (by name) have common values.	
		
	}
	
	@Test
	public void stressTest5() {

		/*
		 * Plan: 
		 * Join(DependentJoin(Selection(NATION), SUPPLIER), Selection(DependentJoin(CUSTOMER, Selection(ORDERS))))
		 */

		// Select on the SUPPLIER SUPPKEY attribute with input required on the NATIONKEY attribute.
		Condition conditionLeft = ConstantEqualityCondition.create(0, TypedConstant.create(22));
		Selection selectionLeft = new Selection(conditionLeft, new Access(postgresqlRelationSupplier, am3));
		DependentJoin djLeft = new DependentJoin(new Access(postgresqlRelationNation, amFree), selectionLeft);
		
		// Select on the ORDERS ORDERSTATUS attribute with input required on the CUSTKEY attribute.
		Condition conditionRight = ConstantEqualityCondition.create(2, TypedConstant.create("TODO"));
		Selection selectionRight = new Selection(conditionRight, new Access(postgresqlRelationOrders, am1));
		DependentJoin djRight = new DependentJoin(new Access(postgresqlRelationCustomer, amFree), selectionRight);
		
		// Check that the left & right DependentJoin have no no input attributes.
		Assert.assertEquals(0, djLeft.getInputAttributes().length);
		Assert.assertEquals(0, djRight.getInputAttributes().length);

		// For the outer-right selection condition, select on the SHIP-PRIORITY attribute.
		Condition condition = ConstantEqualityCondition.create(15, TypedConstant.create(2L)); // TODO.
		Join target = new NestedLoopJoin(djLeft, new Selection(condition, djRight));

		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// TODO. Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		// TODO: execute a join manually to determine the expected size.
		Assert.assertEquals(1000, result.size());
		// TODO: check that the common attributes (by name) have common values.	
		
	}
	
	@Test
	public void stressTest6() {

		/*
		 * Plan: 
		 * DependentJoin(
		 * 		Selection(DependentJoin(CUSTOMER, Selection(ORDERS))),
		 * 		DependentJoin(DependentJoin(Selection(NATION), SUPPLIER), Selection(DependentJoin(Selection(PART), PARTSUPP))) 
		 * )
		 */

		// Select on the SUPPLIER SUPPKEY attribute with input required on the NATIONKEY attribute.
		Condition conditionRightLeft = ConstantEqualityCondition.create(0, TypedConstant.create(22));
		Selection selectionRightLeft = new Selection(conditionRightLeft, new Access(postgresqlRelationSupplier, am3));
		DependentJoin djRightLeft = new DependentJoin(new Access(postgresqlRelationNation, amFree), selectionRightLeft);
		
		// Select on the PART MFGR attribute. 
		Condition conditionRightRight = ConstantEqualityCondition.create(2, TypedConstant.create("TODO"));
		Selection selectionRightRight = new Selection(conditionRightRight, new Access(postgresqlRelationPart, amFree));
		DependentJoin djLeftRight = new DependentJoin(selectionRightRight, new Access(postgresqlRelationPartsupp, am01));
		
		// Select on the PART TYPE attribute. 
		Condition conditionRight = ConstantEqualityCondition.create(4, TypedConstant.create("TODO"));
		
		DependentJoin djRight = new DependentJoin(djRightLeft, new Selection(conditionRight, djLeftRight));
		
		// Check that the left DependentJoin plan has one input attribute, SUPPKEY, to be supplied
		// by the outer nested DependentJoin.
		Assert.assertEquals(1, djRight.getInputAttributes().length);

		
		// Select on the ORDERS ORDERSTATUS attribute with input required on the CUSTKEY attribute.
		Condition conditionLeftRight = ConstantEqualityCondition.create(2, TypedConstant.create("TODO"));
		Selection selectionLeftRight = new Selection(conditionLeftRight, new Access(postgresqlRelationOrders, am1));
		DependentJoin djLeft = new DependentJoin(new Access(postgresqlRelationCustomer, amFree), selectionLeftRight);
		
		// For the outer-left selection condition, select on the SHIP-PRIORITY attribute.
		Condition conditionLeft = ConstantEqualityCondition.create(15, TypedConstant.create(2L)); // TODO.
		Selection selectionLeft = new Selection(conditionLeft, djLeft);
		
		DependentJoin target = new DependentJoin(selectionLeft, djRight);
		
		// Check that the target DependentJoin has no no input attributes.
		Assert.assertEquals(0, target.getInputAttributes().length);
		
		//Execute the plan
		Table result = null;
		try {
			result = this.planExecution(target);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

		// TODO. Check that the result tuples are the ones expected. 
		Assert.assertNotNull(result);
		// TODO: execute a join manually to determine the expected size.
		Assert.assertEquals(1000, result.size());
		// TODO: check that the common attributes (by name) have common values.			
	}
}