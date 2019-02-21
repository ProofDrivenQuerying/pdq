package uk.ac.ox.cs.pdq.test.datasources.sql;

import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;

import uk.ac.ox.cs.pdq.datasources.sql.SqlAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.tuple.Tuple;
import uk.ac.ox.cs.pdq.tuple.TupleType;

public class SqlAccessMethodTest {

	/*
	 *  Parameters of the Postgres test instance.
	 */
	Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty("url", "jdbc:postgresql://localhost:5432/");
		properties.setProperty("database", "tpch");
		properties.setProperty("username", "admin");
		properties.setProperty("password", "admin");
		return(properties);
	}

	//
	// NATION relation
	//
	// Attributes in the external schema.
	Attribute[] attrs_N = new Attribute[] {
			Attribute.create(Integer.class, "N_NATIONKEY"),
			Attribute.create(String.class, "N_NAME"),
			Attribute.create(Integer.class, "N_REGIONKEY"),
			Attribute.create(String.class, "N_COMMENT")
	};

	// Attributes in the internal schema.
	Attribute[] attrs_nation = new Attribute[] {
			Attribute.create(String.class, "name"),
			Attribute.create(Integer.class, "nationKey"),
			Attribute.create(Integer.class, "regionKey"),
	};

	// Attribute mapping
	Map<Attribute, Attribute> attrMap_nation = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "N_NATIONKEY"), Attribute.create(Integer.class, "nationKey"),
			Attribute.create(String.class, "N_NAME"), Attribute.create(String.class, "name"),
			Attribute.create(Integer.class, "N_REGIONKEY"), Attribute.create(Integer.class, "regionKey")
			);
	
	@Test
	public void testQueryString() {

		SqlAccessMethod target;
		Integer[] inputs;
		Relation relation;
		Collection<Tuple> inputTuples;

		relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(this.attrs_nation.clone());
		String name = "NATION";
		when(relation.getName()).thenReturn(name);

		inputs = new Integer[0];
		target = new SqlAccessMethod(name, this.attrs_N, inputs, relation, this.attrMap_nation, this.getProperties());

		String expected = "SELECT N_NATIONKEY,N_NAME,N_REGIONKEY,N_COMMENT FROM NATION";
		String actual = target.queryString(null, null);
		Assert.assertEquals(expected, actual);

		inputs = new Integer[] {1};
		target = new SqlAccessMethod(name, this.attrs_N, inputs, relation, this.attrMap_nation, this.getProperties());

		Attribute[] inputAttributes = new Attribute[] { Attribute.create(String.class, "N_NAME") };

		inputTuples = new ArrayList<Tuple>();

		TupleType tt = TupleType.DefaultFactory.create(String.class);
		inputTuples.add(tt.createTuple("BRAZIL"));

		actual = target.queryString(inputAttributes, inputTuples.iterator()); 
		expected = "SELECT N_NATIONKEY,N_NAME,N_REGIONKEY,N_COMMENT FROM NATION "
				+ "WHERE N_NAME='BRAZIL'";

		Assert.assertEquals(expected, actual);

		inputTuples = new ArrayList<Tuple>();
		inputTuples.add(tt.createTuple("BRAZIL"));
		inputTuples.add(tt.createTuple("INDIA"));
		inputTuples.add(tt.createTuple("PERU"));

		expected = "SELECT N_NATIONKEY,N_NAME,N_REGIONKEY,N_COMMENT FROM NATION "
				+ "WHERE N_NAME='BRAZIL' OR N_NAME='INDIA' OR N_NAME='PERU'";

		actual = target.queryString(inputAttributes, inputTuples.iterator()); 
		Assert.assertEquals(expected, actual);

		/*
		 * BUGFIX 12/01/2018
		 */
		relation = Mockito.mock(Relation.class);
		when(relation.getName()).thenReturn(name);
		when(relation.getAttributes()).thenReturn(this.attrs_nation.clone());

		// Specify input attributes in the access method constructor. 
		Set<Attribute> inputAttrs = new HashSet<Attribute>();
		inputAttrs.add(Attribute.create(String.class, "N_NAME"));
		inputAttrs.add(Attribute.create(Integer.class, "N_REGIONKEY"));

		target = new SqlAccessMethod("NATION", this.attrs_N, inputAttrs, relation, this.attrMap_nation, this.getProperties());

		TupleType ttStringInteger = TupleType.DefaultFactory.create(String.class, Integer.class);
		inputTuples = new ArrayList<Tuple>();
		inputTuples.add(ttStringInteger.createTuple("INDIA", 2));
		inputTuples.add(ttStringInteger.createTuple("FRANCE", 2));
		inputTuples.add(ttStringInteger.createTuple("JAPAN", 2));

		expected = "SELECT N_NATIONKEY,N_NAME,N_REGIONKEY,N_COMMENT FROM NATION "
				+ "WHERE N_NAME='INDIA' AND N_REGIONKEY=2 "
				+ "OR N_NAME='FRANCE' AND N_REGIONKEY=2 "
				+ "OR N_NAME='JAPAN' AND N_REGIONKEY=2";

		// Note that the following alternative string is more concise but would require
		// modification of the access() method signature to take input constants in addition
		// to input tuples.
		//		expected = "SELECT N_NATIONKEY,N_NAME,N_REGIONKEY,N_COMMENT FROM NATION "
		//				+ "WHERE N_NAME IN ('INDIA','FRANCE','JAPAN') AND (N_REGIONKEY) IN (2)";


		actual = target.queryString(target.inputAttributes(false), inputTuples.iterator());
		Assert.assertEquals(expected, actual);

		/*
		 * OPTIMISATION 17/01/2018: test that duplicate input tuples are ignored.
		 */
		inputTuples = new ArrayList<Tuple>();
		inputTuples.add(ttStringInteger.createTuple("INDIA", 2));
		inputTuples.add(ttStringInteger.createTuple("FRANCE", 2));
		inputTuples.add(ttStringInteger.createTuple("JAPAN", 2));
		inputTuples.add(ttStringInteger.createTuple("INDIA", 2));
		inputTuples.add(ttStringInteger.createTuple("INDIA", 2));
		inputTuples.add(ttStringInteger.createTuple("JAPAN", 2));

		expected = "SELECT N_NATIONKEY,N_NAME,N_REGIONKEY,N_COMMENT FROM NATION "
				+ "WHERE N_NAME='INDIA' AND N_REGIONKEY=2 "
				+ "OR N_NAME='FRANCE' AND N_REGIONKEY=2 "
				+ "OR N_NAME='JAPAN' AND N_REGIONKEY=2";

		actual = target.queryString(target.inputAttributes(false), inputTuples.iterator());
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testAccess() {

		SqlAccessMethod target;
		Iterable<Tuple> result;
		List<Tuple> tuples;

		Integer[] inputs;

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(this.attrs_nation.clone());

		String name = "NATION";
		when(relation.getName()).thenReturn(name);

		/*
		 * Free access.
		 */
		inputs = new Integer[0];
		target = new SqlAccessMethod(name, this.attrs_N, inputs, relation, this.attrMap_nation, this.getProperties());

		// Test with relationSchema = true (the default).
		result = target.access();
		tuples = StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());

		Assert.assertEquals(25, tuples.size());
		for (Tuple tuple : tuples) {
			Assert.assertEquals(3, tuple.size());
			Assert.assertArrayEquals(new Type[] {String.class, Integer.class, Integer.class}, tuple.getType().getTypes());
		}

		// Test with relationSchema = false.
		result = target.access(false);
		tuples = StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());

		Assert.assertEquals(25, tuples.size());
		for (Tuple tuple : tuples) {
			Assert.assertEquals(4, tuple.size());
			Assert.assertArrayEquals(new Type[] {Integer.class, String.class, Integer.class, String.class}, tuple.getType().getTypes());
		}
	}

	@Test
	public void testAccessIteratorOfTuple() {

		SqlAccessMethod target;
		Iterable<Tuple> result;
		List<Tuple> tuples;

		Integer[] inputs;
		List<Tuple> inputTuples;
		TupleType tt;

		Relation relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(this.attrs_nation.clone());

		String name = "NATION";
		when(relation.getName()).thenReturn(name);

		/*
		 * Access with inputs on position 1 (N_NAME).
		 */
		inputs = new Integer[] {1};
		target = new SqlAccessMethod(name, this.attrs_N, inputs, relation, this.attrMap_nation, this.getProperties());

		// Test with relationSchema = true (the default).
		tt = TupleType.DefaultFactory.create(String.class);

		inputTuples = new ArrayList<Tuple>();
		inputTuples.add(tt.createTuple("BRAZIL"));

		result = target.access(inputTuples.iterator());
		tuples = StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());

		Assert.assertEquals(1, tuples.size());
		Assert.assertEquals(3, tuples.get(0).size());
		Assert.assertArrayEquals(new Type[] {String.class, Integer.class, Integer.class}, tuples.get(0).getType().getTypes());
		Assert.assertEquals("BRAZIL", tuples.get(0).getValue(0));

		// New test.
		inputTuples = new ArrayList<Tuple>();
		inputTuples.add(tt.createTuple("BRAZIL"));
		inputTuples.add(tt.createTuple("INDIA"));
		inputTuples.add(tt.createTuple("PERU"));

		result = target.access(inputTuples.iterator());
		tuples = StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());

		Assert.assertEquals(3, tuples.size());
		Assert.assertEquals(3, tuples.get(0).size());
		Assert.assertArrayEquals(new Type[] {String.class, Integer.class, Integer.class}, tuples.get(0).getType().getTypes());
		Assert.assertEquals("BRAZIL", tuples.get(0).getValue(0));

		Assert.assertEquals(3, tuples.get(1).size());
		Assert.assertArrayEquals(new Type[] {String.class, Integer.class, Integer.class}, tuples.get(1).getType().getTypes());
		Assert.assertEquals("INDIA", tuples.get(1).getValue(0));

		Assert.assertEquals(3, tuples.get(2).size());
		Assert.assertArrayEquals(new Type[] {String.class, Integer.class, Integer.class}, tuples.get(2).getType().getTypes());
		Assert.assertEquals("PERU", tuples.get(2).getValue(0));

		// Test with relationSchema = false.
		result = target.access(inputTuples.iterator(), false);
		tuples = StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());

		Assert.assertEquals(3, tuples.size());
		for (Tuple tuple : tuples) {
			Assert.assertEquals(4, tuple.size());
			Assert.assertArrayEquals(new Type[] {Integer.class, String.class, Integer.class, String.class}, tuple.getType().getTypes());
		}

		/*
		 * Access with inputs on position 2 (N_REGIONKEY).
		 */
		inputs = new Integer[] {2};
		target = new SqlAccessMethod(name, this.attrs_N, inputs, relation, this.attrMap_nation, this.getProperties());

		// Test with relationSchema = true (the default).
		tt = TupleType.DefaultFactory.create(Integer.class);

		inputTuples = new ArrayList<Tuple>();
		inputTuples.add(tt.createTuple(2));

		result = target.access(inputTuples.iterator());
		tuples = StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());

		// TPCH SQL:
		// SELECT * FROM nation WHERE N_REGIONKEY=2
		Assert.assertEquals(5, tuples.size());
		for (Tuple tuple : tuples) {
			Assert.assertEquals(3, tuple.size());
			Assert.assertArrayEquals(new Type[] {String.class, Integer.class, Integer.class}, tuple.getType().getTypes());
		}

		Assert.assertEquals("INDIA", tuples.get(0).getValue(0));
		Assert.assertEquals("INDONESIA", tuples.get(1).getValue(0));
		Assert.assertEquals("JAPAN", tuples.get(2).getValue(0));
		Assert.assertEquals("CHINA", tuples.get(3).getValue(0));
		Assert.assertEquals("VIETNAM", tuples.get(4).getValue(0));
	}
}
