package uk.ac.ox.cs.pdq.test.datasources.sql;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.sql.PostgresqlRelationWrapper;
import uk.ac.ox.cs.pdq.datasources.sql.SQLRelationWrapper;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;

public class PostgresqlRelationWrapperTest {

	AccessMethod amFree= AccessMethod.create("free_access", new Integer[] {});
	AccessMethod am0 = AccessMethod.create("access_0", new Integer[] {0});
	AccessMethod am1 = AccessMethod.create("access_1", new Integer[] {1});
	AccessMethod am4 = AccessMethod.create("access_4", new Integer[] {4});
	AccessMethod am5 = AccessMethod.create("access_5", new Integer[] {5});

	AccessMethod am015 = AccessMethod.create("access_015", new Integer[] {015});

	TupleType ttInteger = TupleType.DefaultFactory.create(Integer.class);
	TupleType ttFloat = TupleType.DefaultFactory.create(Float.class);
	TupleType ttString = TupleType.DefaultFactory.create(String.class);
	TupleType ttIntegerStringFloat = TupleType.DefaultFactory.create(Integer.class, String.class, Float.class);
	
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

	Attribute[] attributes_S = new Attribute[] {
			Attribute.create(Integer.class, "S_SUPPKEY"),
			Attribute.create(String.class, "S_NAME"),
			Attribute.create(String.class, "S_ADDRESS"),
			Attribute.create(Integer.class, "S_NATIONKEY"),
			Attribute.create(String.class, "S_PHONE"),
			Attribute.create(Float.class, "S_ACCTBAL"),
			Attribute.create(String.class, "S_COMMENT")
	};
	
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

	// Helper methods:
	public Properties getProperties() {

		Properties properties = new Properties();
		properties.setProperty("url", "jdbc:postgresql://localhost:5432/");
		properties.setProperty("database", "tpch");
		properties.setProperty("username", "admin");
		properties.setProperty("password", "admin");
		return(properties);
	}

	@Test
	public void testPostgresqlRelationWrapperPropertiesStringAttributeArrayAccessMethodArray() {
		
		
		SQLRelationWrapper target = new PostgresqlRelationWrapper(this.getProperties(), "NATION", 
				attributes_N, new AccessMethod[] {amFree});
		
		Assert.assertNotNull(target);
		
		// Test the connection.
		try {
			Assert.assertTrue(target.getConnection().isValid(1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAccess() {
		
		SQLRelationWrapper target = new PostgresqlRelationWrapper(this.getProperties(), "NATION", 
				attributes_N, new AccessMethod[] {amFree});
		
		Table result = target.access(); 
		
		// The NATION relation contains 25 tuples.
		Assert.assertEquals(25, result.size());
	}

	@Test
	public void testAccessAttributeArrayResetableIteratorOfTuple() {

		/*
		 * Test on the NATION relation.
		 */
		SQLRelationWrapper target = new PostgresqlRelationWrapper(this.getProperties(), "NATION", 
				attributes_N, new AccessMethod[] {am1});
		
		Attribute[] inputAttributes = new Attribute[] { Attribute.create(String.class, "N_NAME") };
		
		// Construct the inputs by hand.
		Table expected = new Table(inputAttributes);
		// Nation names must match those in the NATIONS table in tpc-h.  
		expected.appendRow(ttString.createTuple((Object[]) Arrays.copyOf(new String[] {"ARGENTINA"}, 1)));
		expected.appendRow(ttString.createTuple((Object[]) Arrays.copyOf(new String[] {"JAPAN"}, 1)));
		expected.appendRow(ttString.createTuple((Object[]) Arrays.copyOf(new String[] {"UNITED KINGDOM"}, 1)));
		ResetableIterator<Tuple> inputs = expected.iterator(); 
		
		Table result = target.access(inputAttributes, inputs); 
		
		Assert.assertEquals(expected.size(), result.size());
		// The nation names are in column 1.

		// Note: This fails with ClassCastException (even though String is the correct type):
		// String[] nationNames = result.getColumn(1);
		Object[] nameColumn = result.getColumn(1);
		String[] actual = Arrays.copyOf(nameColumn, nameColumn.length, String[].class);
		
		//// TODO: Ask Efi about appropriate use of getColumn - how specify the generic?
		//// Compare to how getColumn would look using the Stream API.
		Assert.assertArrayEquals(expected.getData().toArray(new String[0]), actual);
		
		expected = null;
		result = null;
		
		/*
		 * Test on the SUPPLIER relation.
		 */
		target = new PostgresqlRelationWrapper(this.getProperties(), "SUPPLIER", 
				attributes_S, new AccessMethod[] {am5});
		
		inputAttributes = new Attribute[] { Attribute.create(String.class, "S_SUPPKEY") };
		
		// Construct the inputs by hand.
		expected = new Table(inputAttributes);
		// TODO: Float is appropriate ACCTBAL attribute type? Values must match existing data.  
		expected.appendRow(ttFloat.createTuple((Object[]) Arrays.copyOf(new Float[] {1.22f}, 1)));
		expected.appendRow(ttFloat.createTuple((Object[]) Arrays.copyOf(new Float[] {0.89f}, 1)));
		inputs = expected.iterator(); 
		
		result = target.access(inputAttributes, inputs); 
		
		Assert.assertEquals(expected.size(), result.size());
		Assert.assertEquals(expected.getData(), result.getData());
		
		/*
		 * Test on the PART relation.
		 */
		target = new PostgresqlRelationWrapper(this.getProperties(), "PART", 
				attributes_P, new AccessMethod[] {am5});
		
		inputAttributes = new Attribute[] { Attribute.create(String.class, "P_SIZE") };
		
		// Construct the inputs by hand.
		expected = new Table(inputAttributes);
		// TODO: Values must match existing data.
		expected.appendRow(ttInteger.createTuple((Object[]) Arrays.copyOf(new Integer[] {2}, 1)));
		expected.appendRow(ttInteger.createTuple((Object[]) Arrays.copyOf(new Integer[] {1}, 1)));
		expected.appendRow(ttInteger.createTuple((Object[]) Arrays.copyOf(new Integer[] {4}, 1)));
		inputs = expected.iterator(); 
		
		result = target.access(inputAttributes, inputs); 
		
		Assert.assertEquals(expected.size(), result.size());
		Assert.assertEquals(expected.getData(), result.getData());
		
		/*
		 * Test on the CUSTOMER relation.
		 */
		target = new PostgresqlRelationWrapper(this.getProperties(), "CUSTOMER", 
				attributes_C, new AccessMethod[] {am015});
		
		inputAttributes = new Attribute[] { Attribute.create(Integer.class, "C_CUSTKEY"), 
				Attribute.create(String.class, "C_NAME"), Attribute.create(Float.class, "C_ACCTBAL") };
		
		// Construct the inputs by hand.
		expected = new Table(inputAttributes);
		// TODO:   Values must match existing data.
		expected.appendRow(ttIntegerStringFloat.createTuple((Object[]) Arrays.copyOf(new Object[] {12345678, "MR CUSTOMER", 0.89f}, 3)));
		expected.appendRow(ttIntegerStringFloat.createTuple((Object[]) Arrays.copyOf(new Object[] {12345678, "MRS CUSTOMER", 2.16f}, 3)));
		expected.appendRow(ttIntegerStringFloat.createTuple((Object[]) Arrays.copyOf(new Object[] {12345678, "MS CUSTOMER", 11.22f}, 3)));
		expected.appendRow(ttIntegerStringFloat.createTuple((Object[]) Arrays.copyOf(new Object[] {12345678, "DR CUSTOMER", 1201.44f}, 3)));
		expected.appendRow(ttIntegerStringFloat.createTuple((Object[]) Arrays.copyOf(new Object[] {12345678, "A. CUSTOMER", 15.40f}, 3)));
		inputs = expected.iterator(); 
		
		result = target.access(inputAttributes, inputs); 
		
		Assert.assertEquals(expected.size(), result.size());
		Assert.assertEquals(expected.getData(), result.getData());

		
	}
}
