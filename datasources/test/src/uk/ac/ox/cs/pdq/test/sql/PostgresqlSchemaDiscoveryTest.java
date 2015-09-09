package uk.ac.ox.cs.pdq.test.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.pdq.builder.BuilderException;
import uk.ac.ox.cs.pdq.sql.PostgresqlSchemaDiscoverer;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;

import com.google.common.collect.Lists;

@Ignore public class PostgresqlSchemaDiscoveryTest {

	private Schema schema = null;
	
	private static class Relation {
		String name;
		List<String> attributes;
		List<AccessMethod> bindings;
	}
	
	private Map<String, Relation> relations = new LinkedHashMap<>();
	
	private List<String> relationNames = Lists.newArrayList(
			new String[] {"customer", "lineitem", "nation", "orders",
					"part", "partsupp", "region", "supplier",
					"order_customer", "order_supplier", "region_nation"}
	);
	private List<Integer> relationArities = Lists.newArrayList(
			new Integer[] {8, 16, 4, 9, 9, 5, 3, 7}
	);
	private List<Types> bindingTypes = Lists.newArrayList(
			new Types[] {
					Types.FREE, Types.FREE, Types.FREE, 
					Types.FREE, Types.FREE, Types.FREE, 
					Types.FREE, Types.FREE, Types.FREE, 
					Types.FREE, Types.FREE
			}
	);
	private List<Integer[]> bindingPositions = Lists.newArrayList(
			new Integer[][] {{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}}
	);
	private List<String[]> attributesNames = Lists.newArrayList(
			new String[][] {
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
			}
	);
	
	public PostgresqlSchemaDiscoveryTest() throws BuilderException {
		Properties properties = new Properties();
		properties.put("url", "jdbc:postgresql://localhost/");
		properties.put("database", "tpch_0001");
		properties.put("username", "root");
		properties.put("password", "root");
		int i = 0;
		for(String n: this.relationNames) {
			Relation r = new Relation();
			r.name = n;
			r.attributes = Lists.newArrayList(this.attributesNames.get(i));
			r.bindings = Lists.newArrayList(new AccessMethod(
					this.bindingTypes.get(i), 
					Lists.newArrayList(this.bindingPositions.get(i))));
			this.relations.put(n, r);
			i++;
		}
		PostgresqlSchemaDiscoverer disco = new PostgresqlSchemaDiscoverer();
		disco.setProperties(properties);
		this.schema = disco.discover();
	}
	
	private List<Attribute> makeAttributes(String[] attributeNames) {
		List<Attribute> result = new ArrayList<>();
		for (String a: attributeNames) {
			result.add(new Attribute(String.class, a));
		}
		return result;
	}
	
	@Test
	public void testParseViewDefintion() {
		Properties properties = new Properties();
		properties.put("url", "jdbc:postgresql://localhost/");
		properties.put("database", "tpch_0001");
		properties.put("username", "root");
		properties.put("password", "root");
		Map<String, uk.ac.ox.cs.pdq.db.Relation> map = new LinkedHashMap<>();
		map.put("customer", new uk.ac.ox.cs.pdq.db.Relation("customer", this.makeAttributes(this.attributesNames.get(0))){});
		map.put("lineitem", new uk.ac.ox.cs.pdq.db.Relation("lineitem", this.makeAttributes(this.attributesNames.get(1))){});
		map.put("orders", new uk.ac.ox.cs.pdq.db.Relation("orders", this.makeAttributes(this.attributesNames.get(3))){});
		map.put("part", new uk.ac.ox.cs.pdq.db.Relation("part", this.makeAttributes(this.attributesNames.get(4))){});
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
	
	@Test
	public void testNumberOfRelations() {
		assertEquals(this.schema.getRelations().size(), this.relationNames.size());
	}
	
	@Test
	public void testRelationNames() {
		for (uk.ac.ox.cs.pdq.db.Relation r: this.schema.getRelations()) {
			assertTrue(r.getName() + " not present.", this.relations.containsKey(r.getName()));
		}
	}
	
	@Test
	public void testRelationArities() {
		for (uk.ac.ox.cs.pdq.db.Relation r: this.schema.getRelations()) {
			assertEquals((Integer) r.getArity(), (Integer) this.relations.get(r.getName()).attributes.size());
		}
	}
	
	@Test
	public void testAttributeNames() {
		for (uk.ac.ox.cs.pdq.db.Relation r: this.schema.getRelations()) {
			int j = 0;
			for (Attribute a: r.getAttributes()) {
				assertEquals(a.getName(),  this.relations.get(r.getName()).attributes.get(j++));
			}
		}
	}

	@Test
	public void testAccessMethodMethods() {
		for (uk.ac.ox.cs.pdq.db.Relation r: this.schema.getRelations()) {
			int i = 0;
			for (AccessMethod b: r.getAccessMethods()) {
				assertEquals(b.getType(), this.relations.get(r.getName()).bindings.get(i).getType());
				assertEquals(b.getInputs(), this.relations.get(r.getName()).bindings.get(i++).getInputs());
			}
		}
	}

}
