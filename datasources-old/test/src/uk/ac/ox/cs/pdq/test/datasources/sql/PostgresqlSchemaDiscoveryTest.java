package uk.ac.ox.cs.pdq.test.datasources.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.builder.BuilderException;
import uk.ac.ox.cs.pdq.datasources.sql.PostgresqlSchemaDiscoverer;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.util.Utility;


/**
 * The Class PostgresqlSchemaDiscoveryTest.
 * This test requires "tpch_0001" database in postgres, username: "root", password: "root"
 */
public class PostgresqlSchemaDiscoveryTest {

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
					"part", "partsupp", "region", "supplier",
					"order_customer", "order_supplier", "region_nation"};
	
	/** The binding positions. */
	private Integer[][] bindingPositions = new Integer[][] {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}};
	
	/** The attributes names. */
	private String[][] attributesNames = new String[][] {
					{"c_custkey", "c_name", "c_address", "c_nationkey", "c_phone", "c_acctbal", "c_mktsegment", "c_comment"},
					{"l_orderkey", "l_partkey", "l_suppkey", "l_linenumber", "l_quantity", "l_extendedprice", "l_discount", "l_tax", "l_returnflag", "l_linestatus", "l_shipdate", "l_commitdate", "l_receiptdate", "l_shipinstruct", "l_shipmode", "l_comment"},
					{"n_nationkey", "n_name", "n_regionkey", "n_comment"},
					{"o_orderkey", "o_custkey", "o_orderstatus", "o_totalprice", "o_orderdate", "o_orderpriority", "o_clerk", "o_shippriority", "o_comment"},
					{"p_partkey", "p_name", "p_mfgr", "p_brand", "p_type", "p_size", "p_container", "p_retailprice", "p_comment"},
					{"ps_partkey", "ps_suppkey", "ps_availqty", "ps_supplycost", "ps_comment"},
					{"r_regionkey", "r_name", "r_comment"},
					{"s_suppkey", "s_name", "s_address", "s_nationkey",  "s_phone", "s_acctbal", "s_comment"},
					{"cname", "caddress", "cnation", "cactbal", "opriority", "oclerk", "pname", "pbrand", "ptype", "lextendedprice", "ldiscount", "ltax", "lflag"},
					{"sname", "saddress", "snation", "sactbal", "opriority", "oclerk", "pname", "pbrand", "ptype", "lextendedprice", "ldiscount", "ltax", "lflag"},
					{"nation_key", "nation_name", "region_key", "region_name"}
			};
	
	/**
	 * Instantiates a new postgresql schema discovery test.
	 *
	 * @throws BuilderException the builder exception
	 */
	public PostgresqlSchemaDiscoveryTest() throws BuilderException {
		Properties properties = new Properties();
		properties.put("url", "jdbc:postgresql://localhost/");
		properties.put("database", "tpch_0001");
		properties.put("username", "root");
		properties.put("password", "root");
		properties.put("driver","org.postgresql.Driver");		
		int i = 0;
		for(String n: this.relationNames) {
			Relation r = new Relation();
			r.attributes = this.attributesNames[i];
			r.bindings = new AccessMethodDescriptor[]{AccessMethodDescriptor.create(this.bindingPositions[i])};
			this.relations.put(n, r);
			i++;
		}
		PostgresqlSchemaDiscoverer disco = new PostgresqlSchemaDiscoverer();
		disco.setProperties(properties);
		this.schema = disco.discover();
	}
	
	/**
	 * Make attributes.
	 *
	 * @param attributeNames the attribute names
	 * @return the list
	 */
	private Attribute[] makeAttributes(String[] attributeNames) {
		Attribute[] result = new Attribute[attributeNames.length];
		for (int index = 0; index < attributeNames.length; ++index) 
			result[index] = Attribute.create(String.class, attributeNames[index]);
		return result;
	}
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
		
	/**
	 * Test parse view defintion.
	 */
	@Test
	public void testParseViewDefintion() {
		Properties properties = new Properties();
		properties.put("url", "jdbc:postgresql://localhost/");
		properties.put("database", "tpch_0001");
		properties.put("username", "root");
		properties.put("password", "root");
		Map<String, uk.ac.ox.cs.pdq.db.Relation> map = new LinkedHashMap<>();
		map.put("customer", uk.ac.ox.cs.pdq.db.Relation.create("customer", this.makeAttributes(this.attributesNames[0])));
		map.put("lineitem", uk.ac.ox.cs.pdq.db.Relation.create("lineitem", this.makeAttributes(this.attributesNames[1])));
		map.put("orders", uk.ac.ox.cs.pdq.db.Relation.create("orders", this.makeAttributes(this.attributesNames[3])));
		map.put("part", uk.ac.ox.cs.pdq.db.Relation.create("part", this.makeAttributes(this.attributesNames[4])));
		PostgresqlSchemaDiscoverer disco = new PostgresqlSchemaDiscoverer();
		disco.setProperties(properties);
//		disco.parseViewDefinition(
//				"order_supplier",
//				"SELECT c.c_name AS cname, c.c_address AS caddress, c.c_nationkey AS cnation, c.c_acctbal AS cactbal, o.o_orderpriority AS opriority, o.o_clerk AS oclerk, p.p_name AS pname, p.p_brand AS pbrand, p.p_type AS ptype, l.l_extendedprice AS lextendedprice, l.l_discount AS ldiscount, l.l_tax AS ltax, l.l_returnflag AS lflag FROM customer c, orders o, lineitem l, part p WHERE (((o.o_orderkey = l.l_orderkey) AND (o.o_custkey = c.c_custkey)) AND (l.l_partkey = p.p_partkey))",
//				map);
		disco.parseViewDefinition(
				"order_supplier",
				"SELECT \n" +
				"c.c_name AS cname, \n" +
				"    c.c_address AS caddress, \n" +
				"    c.c_nationkey AS cnation, \n" +
				"    c.c_acctbal AS cactbal, \n" +
				"    o.o_orderpriority AS opriority, \n" +
				"    o.o_clerk AS oclerk, \n" +
				"    p.p_name AS pname, \n" +
				"    p.p_brand AS pbrand, \n" +
				"    p.p_type AS ptype, \n" +
				"    l.l_extendedprice AS lextendedprice, \n" +
				"    l.l_discount AS ldiscount, \n" +
				"    l.l_tax AS ltax, \n" +
				"    l.l_returnflag AS lflag \n" +
				"   FROM customer c, \n" +
				"    orders o, \n" +
				"    lineitem l, \n" +
				"    part p \n" +
				"  WHERE (((o.o_orderkey = l.l_orderkey) AND (o.o_custkey = c.c_custkey)) AND \n" +
				"(l.l_partkey = p.p_partkey)); \n",
				map);
	}
	
	/**
	 * Test number of relations.
	 */
	@Test
	public void testNumberOfRelations() {
		Assert.assertTrue(this.schema.getNumberOfRelations() > this.relationNames.length);
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
