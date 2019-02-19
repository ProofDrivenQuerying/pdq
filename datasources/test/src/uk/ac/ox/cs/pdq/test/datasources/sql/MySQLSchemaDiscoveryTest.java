package uk.ac.ox.cs.pdq.test.datasources.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.datasources.schemabuilder.BuilderException;
import uk.ac.ox.cs.pdq.datasources.schemabuilder.MySQLSchemaDiscoverer;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * The Class MySQLSchemaDiscoveryTest.
 */
@Ignore // this test requires a database called tpch_1 to exist in the MySql database and it supposed to have certain tables. This is not very good for a unit test, we should create the database on the fly.
public class MySQLSchemaDiscoveryTest {

	/** The schema. */
	private Schema schema = null;
	
	/**
	 * The Class Relation.
	 */
	private static class Relation {
		
		/** The attributes. */
		String[] attributes;
		
		/** The bindings. */
		AccessMethodDescriptor[] bindings;
	}
	
	/** The relations. */
	private Map<String, Relation> relations = new LinkedHashMap<>();
	
	/** The relation names. */
	private String[] relationNames = new String[] {"customer", "lineitem", "nation", "orders",
					"part", "partsupp", "region", "supplier"};

	/** The binding positions. */
	protected Integer[][] bindingPositions = new Integer[][] {{}, {}, {}, {}, {}, {}, {}, {}};
	
	/** The attributes names. */
	private String[][] attributesNames = new String[][] {
					{"c_custkey", "c_name", "c_address", "c_nationkey", 
						"c_phone", "c_acctbal", "c_mktsegment", "c_comment"},
					{"l_orderkey", "l_partkey", "l_suppkey", "l_linenumber",
							"l_quantity", "l_extendedprice", "l_discount",
							"l_tax", "l_returnflag", "l_linestatus", 
							"l_shipdate", "l_commitdate", "l_receiptdate", 
							"l_shipinstruct", "l_shipmode", "l_comment"},
					{"n_nationkey", "n_name", "n_regionkey", "n_comment"},
					{"o_orderkey", "o_custkey", "o_orderstatus", 
						"o_totalprice", "o_orderdate", "o_orderpriority",
						"o_clerk", "o_shippriority", "o_comment"},
					{"p_partkey", "p_name", "p_mfgr", "p_brand", "p_type",
						"p_size", "p_container", "p_retailprice", "p_comment"},
					{"ps_partkey", "ps_suppkey", "ps_availqty", "ps_supplycost",
							"ps_comment"},
					{"r_regionkey", "r_name", "r_comment"},
					{"s_suppkey", "s_name", "s_address", "s_nationkey", 
						"s_phone", "s_acctbal", "s_comment"}
			};
			
	/**
	 * Instantiates a new my sql schema discovery test.
	 *
	 * @throws BuilderException the builder exception
	 */
	public MySQLSchemaDiscoveryTest() throws BuilderException {
		Properties properties = new Properties();
		properties.put("url", "jdbc:mysql://localhost/");
		properties.put("database", "pdq");
		properties.put("driver", "com.mysql.jdbc.Driver");
		properties.put("username", "pdq");
		properties.put("password", "pdq");
		int i = 0;
		for(String n: this.relationNames) {
			Relation r = new Relation();
			r.attributes = this.attributesNames[i];
			r.bindings = new AccessMethodDescriptor[]{AccessMethodDescriptor.create(this.bindingPositions[i])};
			this.relations.put(n, r);
			i++;
		}
		MySQLSchemaDiscoverer disco = new MySQLSchemaDiscoverer();
		disco.setProperties(properties);
		this.schema = disco.discover();
	}
	
	
	@Test
	public void testFactsReading() {
		Properties properties = new Properties();
		properties.put("url", "jdbc:mysql://localhost/");
		properties.put("database", "pdq");
		properties.put("driver", "com.mysql.jdbc.Driver");
		properties.put("username", "pdq");
		properties.put("password", "pdq");
		int i = 0;
		for(String n: this.relationNames) {
			Relation r = new Relation();
			r.attributes = this.attributesNames[i];
			r.bindings = new AccessMethodDescriptor[]{AccessMethodDescriptor.create(this.bindingPositions[i])};
			this.relations.put(n, r);
			i++;
		}
		MySQLSchemaDiscoverer disco = new MySQLSchemaDiscoverer();
		disco.setProperties(properties);
		disco.discover();
		try {
			String[] relations = new String[] {"nation","region","supplier"};
			
			Collection<Atom> discoveredFacts = disco.discoverRelationFacts(Arrays.asList(relations));
			
			Assert.assertEquals(0,getNumberOfFactsForRelation(discoveredFacts,"customer"));
			Assert.assertEquals(25,getNumberOfFactsForRelation(discoveredFacts,"nation"));
			Assert.assertEquals(5,getNumberOfFactsForRelation(discoveredFacts,"region"));
			Assert.assertEquals(10000,getNumberOfFactsForRelation(discoveredFacts,"supplier"));
		} catch (SQLException | DatabaseException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	private int getNumberOfFactsForRelation(Collection<Atom> discoveredFacts, String predicateName) {
		int number = 0;
		for (Atom a: discoveredFacts) if (a.getPredicate().getName().equalsIgnoreCase(predicateName)) number++;
		return number;
	}

	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		PdqTest.assertsEnabled();
	}
		
	/**
	 * Test number of relations.
	 */
	@Test
	public void testNumberOfRelations() {
		assertEquals(this.schema.getRelations().length, this.relationNames.length);
	}
	
	/**
	 * Test relation names.
	 */
	@Test
	public void testRelationNames() {
		for (uk.ac.ox.cs.pdq.db.Relation r: this.schema.getRelations()) {
			assertTrue(r.getName() + " not present.", this.relations.containsKey(r.getName()));
		}
	}
	
	/**
	 * Test relation arities.
	 */
	@Test
	public void testRelationArities() {
		for (uk.ac.ox.cs.pdq.db.Relation r: this.schema.getRelations()) {
			assertEquals((Integer) r.getArity(), (Integer) this.relations.get(r.getName()).attributes.length);
		}
	}
	
	/**
	 * Test attribute names.
	 */
	@Test
	public void testAttributeNames() {
		for (uk.ac.ox.cs.pdq.db.Relation r: this.schema.getRelations()) {
			int j = 0;
			for (Attribute a: r.getAttributes()) {
				assertEquals(a.getName(),  this.relations.get(r.getName()).attributes[j++]);
			}
		}
	}

	/**
	 * Test access method methods.
	 */
	@Test
	public void testAccessMethodMethods() {
		for (uk.ac.ox.cs.pdq.db.Relation r: this.schema.getRelations()) {
			int i = 0;
			for (AccessMethodDescriptor b: r.getAccessMethods()) 
				Assert.assertArrayEquals(b.getInputs(), this.relations.get(r.getName()).bindings[i++].getInputs());
		}
	}

}
