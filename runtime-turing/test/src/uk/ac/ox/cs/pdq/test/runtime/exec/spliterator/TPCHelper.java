package uk.ac.ox.cs.pdq.test.runtime.exec.spliterator;

import java.util.Map;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.datasources.sql.DatabaseAccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;

public class TPCHelper {

	/*
	 *  Parameters of the Postgres test instance.
	 */
	public static Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty("url", "jdbc:postgresql://localhost:5432/");
		properties.setProperty("database", "tpch");
		properties.setProperty("username", "admin");
		properties.setProperty("password", "admin");
		return(properties);
	}

	//
	// REGION attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_R = new Attribute[] {
			Attribute.create(Integer.class, "R_REGIONKEY"),
			Attribute.create(String.class, "R_NAME"),
			Attribute.create(String.class, "R_COMMENT")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_region = new Attribute[] {
			Attribute.create(Integer.class, "regionKey"),
			Attribute.create(String.class, "regionName"),
			Attribute.create(String.class, "comment")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_region = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "R_REGIONKEY"), Attribute.create(Integer.class, "regionKey"),
			Attribute.create(String.class, "R_NAME"), Attribute.create(String.class, "regionName"),
			Attribute.create(String.class, "R_COMMENT"), Attribute.create(String.class, "comment")
			);

	//
	// NATION attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_N = new Attribute[] {
			Attribute.create(Integer.class, "N_NATIONKEY"),
			Attribute.create(String.class, "N_NAME"),
			Attribute.create(Integer.class, "N_REGIONKEY"),
			Attribute.create(String.class, "N_COMMENT")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_nation = new Attribute[] {
			Attribute.create(String.class, "name"),
			Attribute.create(Integer.class, "nationKey"),
			Attribute.create(Integer.class, "regionKey"),
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_nation = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "N_NATIONKEY"), Attribute.create(Integer.class, "nationKey"),
			Attribute.create(String.class, "N_NAME"), Attribute.create(String.class, "name"),
			Attribute.create(Integer.class, "N_REGIONKEY"), Attribute.create(Integer.class, "regionKey")
			);

	//
	// SUPPLIER attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_S = new Attribute[] {
			Attribute.create(Integer.class, "S_SUPPKEY"),
			Attribute.create(String.class, "S_NAME"),
			Attribute.create(String.class, "S_ADDRESS"),
			Attribute.create(Integer.class, "S_NATIONKEY"),
			Attribute.create(String.class, "S_PHONE"),
			Attribute.create(Float.class, "S_ACCTBAL"),
			Attribute.create(String.class, "S_COMMENT")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_supplier = new Attribute[] {
			Attribute.create(Integer.class, "suppKey"),
			Attribute.create(String.class, "suppName"),
			Attribute.create(Integer.class, "nationKey"),
			Attribute.create(String.class, "suppPhone"),
			Attribute.create(Float.class, "suppAcctBal")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_supplier = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "S_SUPPKEY"), Attribute.create(Integer.class, "suppKey"),
			Attribute.create(String.class, "S_NAME"), Attribute.create(String.class, "suppName"),
			Attribute.create(Integer.class, "S_NATIONKEY"), Attribute.create(Integer.class, "nationKey"),
			Attribute.create(String.class, "S_PHONE"), Attribute.create(String.class, "suppPhone"),
			Attribute.create(Float.class, "S_ACCTBAL"), Attribute.create(Float.class, "suppAcctBal")
			);

	//
	// CUSTOMER attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_C = new Attribute[] {
			Attribute.create(Integer.class, "C_CUSTKEY"),
			Attribute.create(String.class, "C_NAME"),
			Attribute.create(String.class, "C_ADDRESS"),
			Attribute.create(Integer.class, "C_NATIONKEY"),
			Attribute.create(String.class, "C_PHONE"),
			Attribute.create(Float.class, "C_ACCTBAL"),
			Attribute.create(String.class, "C_MKTSEGMENT"),
			Attribute.create(String.class, "C_COMMENT")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_customer = new Attribute[] {
			Attribute.create(Integer.class, "custKey"),
			Attribute.create(String.class, "custName"),
			Attribute.create(Float.class, "custAcctBal"),
			Attribute.create(String.class, "custMktSegment"),
			Attribute.create(Integer.class, "nationKey")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_customer = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "C_CUSTKEY"), Attribute.create(Integer.class, "custKey"),
			Attribute.create(String.class, "C_NAME"), Attribute.create(String.class, "custName"),
			Attribute.create(Integer.class, "C_NATIONKEY"), Attribute.create(Integer.class, "nationKey"),
			Attribute.create(Float.class, "C_ACCTBAL"), Attribute.create(Float.class, "custAcctBal"),
			Attribute.create(String.class, "C_MKTSEGMENT"), Attribute.create(String.class, "custMktSegment")
			);

	//
	// PART attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_P = new Attribute[] {
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

	// Attributes in the internal schema.
	public static Attribute[] attrs_part = new Attribute[] {
			Attribute.create(Integer.class, "partKey"),
			Attribute.create(String.class, "partName"),
			Attribute.create(String.class, "partMfgr"),
			Attribute.create(Integer.class, "partSize"),
			Attribute.create(Float.class, "partRetailPrice")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_part = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "P_PARTKEY"), Attribute.create(Integer.class, "partKey"),
			Attribute.create(String.class, "P_NAME"), Attribute.create(String.class, "partName"),
			Attribute.create(String.class, "P_MFGR"), Attribute.create(String.class, "partMfgr"),
			Attribute.create(Integer.class, "P_SIZE"), Attribute.create(Integer.class, "partSize"),
			Attribute.create(Float.class, "P_RETAILPRICE"), Attribute.create(Float.class, "partRetailPrice")
			);

	//
	// PARTSUPP attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_PS = new Attribute[] {
			Attribute.create(Integer.class, "PS_PARTKEY"),
			Attribute.create(Integer.class, "PS_SUPPKEY"),
			Attribute.create(Integer.class, "PS_AVAILQTY"),
			Attribute.create(Float.class, "PS_SUPPLYCOST"),
			Attribute.create(String.class, "PS_COMMENT")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_partSupp = new Attribute[] {
			Attribute.create(Integer.class, "partKey"),
			Attribute.create(Integer.class, "suppKey"),
			Attribute.create(Integer.class, "availQty"),
			Attribute.create(Float.class, "supplyCost"),
			Attribute.create(String.class, "partSupplierComment")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_partSupp = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "PS_PARTKEY"), Attribute.create(Integer.class, "partKey"),
			Attribute.create(Integer.class, "PS_SUPPKEY"), Attribute.create(Integer.class, "suppKey"),
			Attribute.create(Integer.class, "PS_AVAILQTY"), Attribute.create(Integer.class, "availQty"),
			Attribute.create(Float.class, "PS_SUPPLYCOST"), Attribute.create(Float.class, "supplyCost"),
			Attribute.create(String.class, "PS_COMMENT"), Attribute.create(String.class, "partSupplierComment")
			);

	//
	// ORDERS attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_O = new Attribute[] {
			Attribute.create(Integer.class, "O_ORDERKEY"),
			Attribute.create(Integer.class, "O_CUSTKEY"),
			Attribute.create(String.class, "O_ORDERSTATUS"),
			Attribute.create(Float.class, "O_TOTALPRICE"),
			Attribute.create(String.class, "O_ORDERDATE"),
			Attribute.create(String.class, "O_ORDERPRIORITY"),
			Attribute.create(String.class, "O_CLERK"),
			Attribute.create(Integer.class, "O_SHIPPRIORITY"),
			Attribute.create(String.class, "O_COMMENT")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_orders = new Attribute[] {
			Attribute.create(Integer.class, "orderKey"),
			Attribute.create(Integer.class, "custKey"),
			Attribute.create(Float.class, "totalPrice"),
			Attribute.create(String.class, "orderDate")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_orders = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "O_ORDERKEY"), Attribute.create(Integer.class, "orderKey"),
			Attribute.create(Integer.class, "O_CUSTKEY"), Attribute.create(Integer.class, "custKey"),
			Attribute.create(Float.class, "O_TOTALPRICE"), Attribute.create(Float.class, "totalPrice"),
			Attribute.create(String.class, "O_ORDERDATE"), Attribute.create(String.class, "orderDate")
			);

	//
	// LINEITEM attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_L = new Attribute[] {
			Attribute.create(Integer.class, "L_ORDERKEY"),
			Attribute.create(Integer.class, "L_PARTKEY"),
			Attribute.create(Integer.class, "L_SUPPKEY"),
			Attribute.create(Integer.class, "L_LINENUMBER"),
			Attribute.create(Integer.class, "L_QUANTITY"),
			Attribute.create(Float.class, "L_EXTENDEDPRICE"),
			Attribute.create(Float.class, "L_DISCOUNT"),
			Attribute.create(Float.class, "L_TAX"),
			Attribute.create(String.class, "L_RETURNFLAG"),
			Attribute.create(String.class, "L_LINESTATUS"),
			Attribute.create(String.class, "L_SHIPDATE"),
			Attribute.create(String.class, "L_COMMITDATE"),
			Attribute.create(String.class, "L_RECEIPTDATE"),
			Attribute.create(String.class, "L_SHIPINSTRUCT"),
			Attribute.create(String.class, "L_SHIPMODE"),
			Attribute.create(String.class, "L_COMMENT")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_lineItem = new Attribute[] {
			Attribute.create(Integer.class, "orderKey"),
			Attribute.create(Integer.class, "partKey"),
			Attribute.create(Integer.class, "suppKey"),
			Attribute.create(Integer.class, "quantity"),
			Attribute.create(Float.class, "extendedPrice")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_lineItem = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "L_ORDERKEY"), Attribute.create(Integer.class, "orderKey"),
			Attribute.create(Integer.class, "L_PARTKEY"), Attribute.create(Integer.class, "partKey"),
			Attribute.create(Integer.class, "L_SUPPKEY"), Attribute.create(Integer.class, "suppKey"),
			Attribute.create(Integer.class, "L_QUANTITY"), Attribute.create(Integer.class, "quantity"),
			Attribute.create(Float.class, "L_EXTENDEDPRICE"), Attribute.create(Float.class, "extendedPrice")
			);

	/*
	 * Alternative schema with a reduced set of attributes for stress testing. 
	 */

	//
	// REGION_LESS attributes 
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_R_LESS = new Attribute[] {
			Attribute.create(Integer.class, "R_REGIONKEY"),
			Attribute.create(String.class, "R_NAME")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_region_less = new Attribute[] {
			Attribute.create(Integer.class, "regionKey"),
			Attribute.create(String.class, "regionName")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_region_less = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "R_REGIONKEY"), Attribute.create(Integer.class, "regionKey"),
			Attribute.create(String.class, "R_NAME"), Attribute.create(String.class, "regionName")
			);

	//
	// NATION_LESS attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_N_LESS = new Attribute[] {
			Attribute.create(Integer.class, "N_NATIONKEY"),
			Attribute.create(String.class, "N_NAME"),
			Attribute.create(Integer.class, "N_REGIONKEY")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_nation_less = new Attribute[] {
			Attribute.create(String.class, "name"),
			Attribute.create(Integer.class, "nationKey"),
			Attribute.create(Integer.class, "regionKey"),
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_nation_less = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "N_NATIONKEY"), Attribute.create(Integer.class, "nationKey"),
			Attribute.create(String.class, "N_NAME"), Attribute.create(String.class, "name"),
			Attribute.create(Integer.class, "N_REGIONKEY"), Attribute.create(Integer.class, "regionKey")
			);

	//
	// SUPPLIER_LESS attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_S_LESS = new Attribute[] {
			Attribute.create(Integer.class, "S_SUPPKEY"),
			Attribute.create(String.class, "S_NAME"),
			Attribute.create(Integer.class, "S_NATIONKEY"),
			Attribute.create(Float.class, "S_ACCTBAL")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_supplier_less = new Attribute[] {
			Attribute.create(Integer.class, "suppKey"),
			Attribute.create(String.class, "suppName"),
			Attribute.create(Integer.class, "nationKey"),
			Attribute.create(Float.class, "suppAcctBal")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_supplier_less = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "S_SUPPKEY"), Attribute.create(Integer.class, "suppKey"),
			Attribute.create(String.class, "S_NAME"), Attribute.create(String.class, "suppName"),
			Attribute.create(Integer.class, "S_NATIONKEY"), Attribute.create(Integer.class, "nationKey"),
			Attribute.create(Float.class, "S_ACCTBAL"), Attribute.create(Float.class, "suppAcctBal")
			);

	//
	// CUSTOMER_LESS attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_C_LESS = new Attribute[] {
			Attribute.create(Integer.class, "C_CUSTKEY"),
			Attribute.create(String.class, "C_NAME"),
			Attribute.create(Integer.class, "C_NATIONKEY"),
			Attribute.create(Float.class, "C_ACCTBAL")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_customer_less = new Attribute[] {
			Attribute.create(Integer.class, "custKey"),
			Attribute.create(String.class, "custName"),
			Attribute.create(Integer.class, "nationKey"),
			Attribute.create(Float.class, "acctBal")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_customer_less = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "C_CUSTKEY"), Attribute.create(Integer.class, "custKey"),
			Attribute.create(String.class, "C_NAME"), Attribute.create(String.class, "custName"),
			Attribute.create(Integer.class, "C_NATIONKEY"), Attribute.create(Integer.class, "nationKey"),
			Attribute.create(Float.class, "C_ACCTBAL"), Attribute.create(Float.class, "acctBal")
			);

	//
	// PART_LESS attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_P_LESS = new Attribute[] {
			Attribute.create(Integer.class, "P_PARTKEY"),
			Attribute.create(String.class, "P_NAME"),
			Attribute.create(Integer.class, "P_SIZE")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_part_less = new Attribute[] {
			Attribute.create(Integer.class, "partKey"),
			Attribute.create(String.class, "partName"),
			Attribute.create(Integer.class, "partSize")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_part_less = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "P_PARTKEY"), Attribute.create(Integer.class, "partKey"),
			Attribute.create(String.class, "P_NAME"), Attribute.create(String.class, "partName"),
			Attribute.create(Integer.class, "P_SIZE"), Attribute.create(Integer.class, "partSize")
			);

	//
	// PARTSUPP_LESS attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_PS_LESS = new Attribute[] {
			Attribute.create(Integer.class, "PS_PARTKEY"),
			Attribute.create(Integer.class, "PS_SUPPKEY"),
			Attribute.create(Integer.class, "PS_AVAILQTY")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_partSupp_less = new Attribute[] {
			Attribute.create(Integer.class, "partKey"),
			Attribute.create(Integer.class, "suppKey"),
			Attribute.create(Integer.class, "availQty")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_partSupp_less = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "PS_PARTKEY"), Attribute.create(Integer.class, "partKey"),
			Attribute.create(Integer.class, "PS_SUPPKEY"), Attribute.create(Integer.class, "suppKey"),
			Attribute.create(Integer.class, "PS_AVAILQTY"), Attribute.create(Integer.class, "availQty")
			);

	//
	// ORDERS_LESS attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_O_LESS = new Attribute[] {
			Attribute.create(Integer.class, "O_ORDERKEY"),
			Attribute.create(Integer.class, "O_CUSTKEY"),
			Attribute.create(Float.class, "O_TOTALPRICE")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_orders_less = new Attribute[] {
			Attribute.create(Integer.class, "orderKey"),
			Attribute.create(Integer.class, "custKey"),
			Attribute.create(Float.class, "totalPrice")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_orders_less = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "O_ORDERKEY"), Attribute.create(Integer.class, "orderKey"),
			Attribute.create(Integer.class, "O_CUSTKEY"), Attribute.create(Integer.class, "custKey"),
			Attribute.create(Float.class, "O_TOTALPRICE"), Attribute.create(Float.class, "totalPrice")
			);

	//
	// LINEITEM_LESS attributes
	//
	// Attributes in the external schema.
	public static Attribute[] attrs_L_LESS = new Attribute[] {
			Attribute.create(Integer.class, "L_ORDERKEY"),
			Attribute.create(Integer.class, "L_PARTKEY"),
			Attribute.create(Integer.class, "L_SUPPKEY"),
			Attribute.create(Integer.class, "L_LINENUMBER"),
			Attribute.create(Integer.class, "L_QUANTITY")
	};

	// Attributes in the internal schema.
	public static Attribute[] attrs_lineItem_less = new Attribute[] {
			Attribute.create(Integer.class, "orderKey"),
			Attribute.create(Integer.class, "partKey"),
			Attribute.create(Integer.class, "suppKey"),
			Attribute.create(Integer.class, "quantity")
	};

	// Attribute mapping
	public static Map<Attribute, Attribute> attrMap_lineItem_less = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "L_ORDERKEY"), Attribute.create(Integer.class, "orderKey"),
			Attribute.create(Integer.class, "L_PARTKEY"), Attribute.create(Integer.class, "partKey"),
			Attribute.create(Integer.class, "L_SUPPKEY"), Attribute.create(Integer.class, "suppKey"),
			Attribute.create(Integer.class, "L_QUANTITY"), Attribute.create(Integer.class, "quantity")
			);

	/*
	 * Relations and AccessMethods
	 */

	//
	// REGION relation
	// 
	public static Relation relationRegion = new Relation("region", TPCHelper.attrs_region);
	public static AccessMethod amFreeRegion = new DatabaseAccessMethod("REGION", TPCHelper.attrs_R, 
			new Integer[0], relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());
	public static AccessMethod am0Region = new DatabaseAccessMethod("REGION", TPCHelper.attrs_R, 
			Sets.newHashSet(Attribute.create(Integer.class, "R_REGIONKEY")), relationRegion, TPCHelper.attrMap_region, TPCHelper.getProperties());
	
	public static Relation relationRegion_less = new Relation("region_less", TPCHelper.attrs_region_less);
	public static AccessMethod amFreeRegion_less = new DatabaseAccessMethod("REGION", TPCHelper.attrs_R_LESS, 
			new Integer[0], relationRegion_less, TPCHelper.attrMap_region_less, TPCHelper.getProperties());
	public static AccessMethod am0Region_less = new DatabaseAccessMethod("REGION", TPCHelper.attrs_R_LESS, 
			Sets.newHashSet(Attribute.create(Integer.class, "R_REGIONKEY")), relationRegion_less, TPCHelper.attrMap_region_less, TPCHelper.getProperties());
	
	//
	// NATION relation
	// 
	public static Relation relationNation = new Relation("nation", TPCHelper.attrs_nation);
	public static AccessMethod amFreeNation = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N, 
			new Integer[0], relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());
	public static AccessMethod am0Nation = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N, 
			Sets.newHashSet(Attribute.create(Integer.class, "N_NATIONKEY")), relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());
	public static AccessMethod am2Nation = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N, 
			Sets.newHashSet(Attribute.create(Integer.class, "N_REGIONKEY")), relationNation, TPCHelper.attrMap_nation, TPCHelper.getProperties());

	public static Relation relationNation_less = new Relation("nation_less", TPCHelper.attrs_nation_less);
	public static AccessMethod amFreeNation_less = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N_LESS, 
			new Integer[0], relationNation_less, TPCHelper.attrMap_nation_less, TPCHelper.getProperties());
	public static AccessMethod am0Nation_less = new DatabaseAccessMethod("NATION", TPCHelper.attrs_N_LESS, 
			Sets.newHashSet(Attribute.create(Integer.class, "N_NATIONKEY")), relationNation_less, TPCHelper.attrMap_nation_less, TPCHelper.getProperties());

	//
	// SUPPLIER relation
	// 
	public static Relation relationSupplier = new Relation("supplier", TPCHelper.attrs_supplier);
	public static AccessMethod amFreeSupplier = new DatabaseAccessMethod("SUPPLIER", TPCHelper.attrs_S, 
			new Integer[0], relationSupplier, TPCHelper.attrMap_supplier, TPCHelper.getProperties());
	public static AccessMethod am3Supplier = new DatabaseAccessMethod("SUPPLIER", TPCHelper.attrs_S, 
			Sets.newHashSet(Attribute.create(Integer.class, "S_NATIONKEY")), relationSupplier, TPCHelper.attrMap_supplier, TPCHelper.getProperties());

	public static Relation relationSupplier_less = new Relation("supplier_less", TPCHelper.attrs_supplier_less);

	public static AccessMethod amFreeSupplier_less = new DatabaseAccessMethod("SUPPLIER", TPCHelper.attrs_S_LESS, 
			new Integer[0], relationSupplier_less, TPCHelper.attrMap_supplier_less, TPCHelper.getProperties());
	public static AccessMethod am3Supplier_less = new DatabaseAccessMethod("SUPPLIER", TPCHelper.attrs_S_LESS, 
			Sets.newHashSet(Attribute.create(Integer.class, "S_NATIONKEY")), relationSupplier_less, TPCHelper.attrMap_supplier_less, TPCHelper.getProperties());

	//
	// CUSTOMER relation
	// 
	public static Relation relationCustomer = new Relation("customer", TPCHelper.attrs_customer);
	public static AccessMethod amFreeCustomer = new DatabaseAccessMethod("CUSTOMER", TPCHelper.attrs_C, 
			new Integer[0], relationCustomer, TPCHelper.attrMap_customer, TPCHelper.getProperties());
	public static AccessMethod am03Customer = new DatabaseAccessMethod("CUSTOMER", TPCHelper.attrs_C, 
			Sets.newHashSet(Attribute.create(Integer.class, "C_CUSTKEY"), Attribute.create(Integer.class, "C_NATIONKEY")), 
			relationCustomer, attrMap_customer, TPCHelper.getProperties());
	public static AccessMethod am3Customer = new DatabaseAccessMethod("CUSTOMER", TPCHelper.attrs_C, 
			Sets.newHashSet(Attribute.create(Integer.class, "C_NATIONKEY")), relationCustomer, attrMap_customer, TPCHelper.getProperties());

	public static Relation relationCustomer_less = new Relation("customer", TPCHelper.attrs_customer_less);
	public static AccessMethod amFreeCustomer_less = new DatabaseAccessMethod("CUSTOMER", TPCHelper.attrs_C_LESS, 
			new Integer[0], relationCustomer_less, TPCHelper.attrMap_customer_less, TPCHelper.getProperties());
	public static AccessMethod am3Customer_less = new DatabaseAccessMethod("CUSTOMER", TPCHelper.attrs_C_LESS, 
			Sets.newHashSet(Attribute.create(Integer.class, "C_NATIONKEY")), relationCustomer_less, TPCHelper.attrMap_customer_less, TPCHelper.getProperties());

	//
	// PART relation
	// 
	public static Relation relationPart = new Relation("part", TPCHelper.attrs_part);
	public static AccessMethod amFreePart = new DatabaseAccessMethod("PART", TPCHelper.attrs_P, 
			new Integer[0], relationPart, TPCHelper.attrMap_part, TPCHelper.getProperties());

	public static Relation relationPart_less = new Relation("part_less", TPCHelper.attrs_part_less);
	public static AccessMethod amFreePart_less = new DatabaseAccessMethod("PART", TPCHelper.attrs_P_LESS, 
			new Integer[0], relationPart_less, TPCHelper.attrMap_part_less, TPCHelper.getProperties());


	//
	// PARTSUPP relation
	// 
	public static Relation relationPartSupp = new Relation("partSupp", TPCHelper.attrs_partSupp);
	public static AccessMethod amFreePartSupp = new DatabaseAccessMethod("PARTSUPP", TPCHelper.attrs_PS, 
			new Integer[0], relationPartSupp, TPCHelper.attrMap_partSupp, TPCHelper.getProperties());
	public static AccessMethod am0PartSupp = new DatabaseAccessMethod("PARTSUPP", TPCHelper.attrs_PS, 
			Sets.newHashSet(Attribute.create(Integer.class, "PS_PARTKEY")), 
			relationPartSupp, TPCHelper.attrMap_partSupp, TPCHelper.getProperties());
	public static AccessMethod am1PartSupp = new DatabaseAccessMethod("PARTSUPP", TPCHelper.attrs_PS, 
			Sets.newHashSet(Attribute.create(Integer.class, "PS_SUPPKEY")), 
			relationPartSupp, TPCHelper.attrMap_partSupp, TPCHelper.getProperties());
	public static AccessMethod am01PartSupp = new DatabaseAccessMethod("PARTSUPP", TPCHelper.attrs_PS, 
			Sets.newHashSet(Attribute.create(Integer.class, "PS_PARTKEY"), Attribute.create(Integer.class, "PS_SUPPKEY")), 
			relationPartSupp, TPCHelper.attrMap_partSupp, TPCHelper.getProperties());

	public static Relation relationPartSupp_less = new Relation("partSupp_less", TPCHelper.attrs_partSupp_less);
	public static AccessMethod amFreePartSupp_less = new DatabaseAccessMethod("PARTSUPP", TPCHelper.attrs_PS_LESS, 
			new Integer[0], relationPartSupp_less, TPCHelper.attrMap_partSupp_less, TPCHelper.getProperties());
	public static AccessMethod am0PartSupp_less = new DatabaseAccessMethod("PARTSUPP", TPCHelper.attrs_PS_LESS, 
			Sets.newHashSet(Attribute.create(Integer.class, "PS_PARTKEY")), 
			relationPartSupp_less, TPCHelper.attrMap_partSupp_less, TPCHelper.getProperties());
	public static AccessMethod am1PartSupp_less = new DatabaseAccessMethod("PARTSUPP", TPCHelper.attrs_PS_LESS, 
			Sets.newHashSet(Attribute.create(Integer.class, "PS_SUPPKEY")), 
			relationPartSupp_less, TPCHelper.attrMap_partSupp_less, TPCHelper.getProperties());
	public static AccessMethod am01PartSupp_less = new DatabaseAccessMethod("PARTSUPP", TPCHelper.attrs_PS_LESS, 
			Sets.newHashSet(Attribute.create(Integer.class, "PS_PARTKEY"), Attribute.create(Integer.class, "PS_SUPPKEY")), 
			relationPartSupp_less, TPCHelper.attrMap_partSupp_less, TPCHelper.getProperties());

	//
	// ORDERS relation
	// 
	public static Relation relationOrders = new Relation("orders", TPCHelper.attrs_orders);

	public static AccessMethod amFreeOrders = new DatabaseAccessMethod("ORDERS", TPCHelper.attrs_O, 
			new Integer[0], relationOrders, TPCHelper.attrMap_orders, TPCHelper.getProperties());
	public static AccessMethod am1Orders = new DatabaseAccessMethod("ORDERS", TPCHelper.attrs_O, 
			Sets.newHashSet(Attribute.create(Integer.class, "O_CUSTKEY")), relationOrders, TPCHelper.attrMap_orders, TPCHelper.getProperties());

	public static Relation relationOrders_less = new Relation("orders", TPCHelper.attrs_orders_less);

	public static AccessMethod amFreeOrders_less = new DatabaseAccessMethod("ORDERS", TPCHelper.attrs_O_LESS, 
			new Integer[0], relationOrders_less, TPCHelper.attrMap_orders_less, TPCHelper.getProperties());
	public static AccessMethod am1Orders_less = new DatabaseAccessMethod("ORDERS", TPCHelper.attrs_O_LESS, 
			Sets.newHashSet(Attribute.create(Integer.class, "O_CUSTKEY")), relationOrders_less, TPCHelper.attrMap_orders_less, TPCHelper.getProperties());

	//
	// LINEITEM relation
	// 
	public static Relation relationLineItem = new Relation("lineItem", TPCHelper.attrs_lineItem);
	public static AccessMethod amFreeLineItem = new DatabaseAccessMethod("LINEITEM", TPCHelper.attrs_L, 
			new Integer[0],	relationLineItem, TPCHelper.attrMap_lineItem, TPCHelper.getProperties());
	public static AccessMethod am012LineItem = new DatabaseAccessMethod("LINEITEM", TPCHelper.attrs_L, 
			Sets.newHashSet(Attribute.create(Integer.class, "L_ORDERKEY"), 
					Attribute.create(Integer.class, "L_PARTKEY"), Attribute.create(Integer.class, "L_SUPPKEY")), 
			relationLineItem, TPCHelper.attrMap_lineItem, TPCHelper.getProperties());

	public static Relation relationLineItem_less = new Relation("lineItem_less", TPCHelper.attrs_lineItem_less);
	public static AccessMethod amFreeLineItem_less = new DatabaseAccessMethod("LINEITEM", TPCHelper.attrs_L_LESS, 
			new Integer[0],	relationLineItem_less, TPCHelper.attrMap_lineItem_less, TPCHelper.getProperties());
	public static AccessMethod am012LineItem_less = new DatabaseAccessMethod("LINEITEM", TPCHelper.attrs_L_LESS, 
			Sets.newHashSet(Attribute.create(Integer.class, "L_ORDERKEY"), 
					Attribute.create(Integer.class, "L_PARTKEY"), Attribute.create(Integer.class, "L_SUPPKEY")), 
			relationLineItem_less, TPCHelper.attrMap_lineItem_less, TPCHelper.getProperties());

}