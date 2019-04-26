<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<schema>
    <relations>
        <view name="region_nation">
            <attribute name="nation_key" type="java.lang.Integer"/>
            <attribute name="nation_name" type="java.lang.String"/>
            <attribute name="region_key" type="java.lang.Integer"/>
            <attribute name="region_name" type="java.lang.String"/>
            <access-method name="m21" inputs="0"/>
            <access-method name="m13"/>
        </view>
        <relation name="activityFree">
            <attribute name="activity_comment" type="java.lang.String"/>
            <attribute name="activity_id" type="java.lang.String"/>
            <attribute name="assay_chembl_id" type="java.lang.String"/>
            <attribute name="assay_description" type="java.lang.String"/>
            <attribute name="assay_type" type="java.lang.String"/>
            <attribute name="bao_endpoint" type="java.lang.String"/>
            <attribute name="bao_format" type="java.lang.String"/>
            <attribute name="canonical_smiles" type="java.lang.String"/>
            <attribute name="data_validity_comment" type="java.lang.String"/>
            <attribute name="document_chembl_id" type="java.lang.String"/>
            <attribute name="document_journal" type="java.lang.String"/>
            <attribute name="document_year" type="java.lang.String"/>
            <attribute name="molecule_chembl_id" type="java.lang.String"/>
            <attribute name="pchembl_value" type="java.lang.Double"/>
            <attribute name="potential_duplicate" type="java.lang.String"/>
            <attribute name="published_relation" type="java.lang.String"/>
            <attribute name="published_type" type="java.lang.String"/>
            <attribute name="published_units" type="java.lang.String"/>
            <attribute name="published_value" type="java.lang.String"/>
            <attribute name="qudt_units" type="java.lang.String"/>
            <attribute name="record_id" type="java.lang.String"/>
            <attribute name="standard_flag" type="java.lang.Boolean"/>
            <attribute name="standard_relation" type="java.lang.String"/>
            <attribute name="standard_type" type="java.lang.String"/>
            <attribute name="standard_units" type="java.lang.String"/>
            <attribute name="standard_value" type="java.lang.Double"/>
            <attribute name="target_chembl_id" type="java.lang.String"/>
            <attribute name="target_organism" type="java.lang.String"/>
            <attribute name="target_pref_name" type="java.lang.String"/>
            <attribute name="uo_units" type="java.lang.String"/>
            <access-method name="chembl_activity_free"/>
        </relation>
      <relation name="lineitem">
            <attribute name="l_orderkey" type="java.lang.Integer"/>
            <attribute name="l_partkey" type="java.lang.Integer"/>
            <attribute name="l_suppkey" type="java.lang.Integer"/>
            <attribute name="l_linenumber" type="java.lang.Integer"/>
            <attribute name="l_quantity" type="java.math.BigDecimal"/>
            <attribute name="l_extendedprice" type="java.math.BigDecimal"/>
            <attribute name="l_discount" type="java.math.BigDecimal"/>
            <attribute name="l_tax" type="java.math.BigDecimal"/>
            <attribute name="l_returnflag" type="java.lang.String"/>
            <attribute name="l_linestatus" type="java.lang.String"/>
            <attribute name="l_shipdate" type="java.sql.Date"/>
            <attribute name="l_commitdate" type="java.sql.Date"/>
            <attribute name="l_receiptdate" type="java.sql.Date"/>
            <attribute name="l_shipinstruct" type="java.lang.String"/>
            <attribute name="l_shipmode" type="java.lang.String"/>
            <attribute name="l_comment" type="java.lang.String"/>
            <access-method name="m10" inputs="1"/>
            <access-method name="m11" inputs="2"/>
            <access-method name="m12" inputs="1,2"/>
        </relation>
        <relation name="partsupp">
            <attribute name="ps_partkey" type="java.lang.Integer"/>
            <attribute name="ps_suppkey" type="java.lang.Integer"/>
            <attribute name="ps_availqty" type="java.lang.Integer"/>
            <attribute name="ps_supplycost" type="java.math.BigDecimal"/>
            <attribute name="ps_comment" type="java.lang.String"/>
            <access-method name="m4" inputs="0"/>
            <access-method name="m5" inputs="1"/>
            <access-method name="m6" inputs="0,1"/>
        </relation>
        <relation name="nation">
            <attribute name="n_nationkey" type="java.lang.Integer"/>
            <attribute name="n_name" type="java.lang.String"/>
            <attribute name="n_regionkey" type="java.lang.Integer"/>
            <attribute name="n_comment" type="java.lang.String"/>
            <access-method name="m7" inputs="2"/>
        </relation>
        <relation name="part">
            <attribute name="p_partkey" type="java.lang.Integer"/>
            <attribute name="p_name" type="java.lang.String"/>
            <attribute name="p_mfgr" type="java.lang.String"/>
            <attribute name="p_brand" type="java.lang.String"/>
            <attribute name="p_type" type="java.lang.String"/>
            <attribute name="p_size" type="java.lang.Integer"/>
            <attribute name="p_container" type="java.lang.String"/>
            <attribute name="p_retailprice" type="java.math.BigDecimal"/>
            <attribute name="p_comment" type="java.lang.String"/>
            <access-method name="m2"/>
        </relation>
        <relation name="supplier">
            <attribute name="s_suppkey" type="java.lang.Integer"/>
            <attribute name="s_name" type="java.lang.String"/>
            <attribute name="s_address" type="java.lang.String"/>
            <attribute name="s_nationkey" type="java.lang.Integer"/>
            <attribute name="s_phone" type="java.lang.String"/>
            <attribute name="s_acctbal" type="java.math.BigDecimal"/>
            <attribute name="s_comment" type="java.lang.String"/>
            <access-method name="m3" inputs="3"/>
            <access-method name="m20"/>
        </relation>
        <relation name="orders">
            <attribute name="o_orderkey" type="java.lang.Integer"/>
            <attribute name="o_custkey" type="java.lang.Integer"/>
            <attribute name="o_orderstatus" type="java.lang.String"/>
            <attribute name="o_totalprice" type="java.math.BigDecimal"/>
            <attribute name="o_orderdate" type="java.sql.Date"/>
            <attribute name="o_orderpriority" type="java.lang.String"/>
            <attribute name="o_clerk" type="java.lang.String"/>
            <attribute name="o_shippriority" type="java.lang.Integer"/>
            <attribute name="o_comment" type="java.lang.String"/>
            <access-method name="m9" inputs="1"/>
        </relation>
        <relation name="region">
            <attribute name="r_regionkey" type="java.lang.Integer"/>
            <attribute name="r_name" type="java.lang.String"/>
            <attribute name="r_comment" type="java.lang.String"/>
            <access-method name="m8"/>
        </relation>
        <relation name="customer">
            <attribute name="c_custkey" type="java.lang.Integer"/>
            <attribute name="c_name" type="java.lang.String"/>
            <attribute name="c_address" type="java.lang.String"/>
            <attribute name="c_nationkey" type="java.lang.Integer"/>
            <attribute name="c_phone" type="java.lang.String"/>
            <attribute name="c_acctbal" type="java.math.BigDecimal"/>
            <attribute name="c_mktsegment" type="java.lang.String"/>
            <attribute name="c_comment" type="java.lang.String"/>
            <access-method name="m1" inputs="3"/>
        </relation>
    </relations>
    <dependencies>
          <dependency type="TGD">
            <body>
                <atom name="region_nation">
                    <variable name="_46"/>
                    <variable name="_47"/>
                    <variable name="_48"/>
                    <variable name="_51"/>
                </atom>
            </body>
            <head>
                <atom name="nation">
                    <variable name="_46"/>
                    <variable name="_47"/>
                    <variable name="_48"/>
                    <variable name="_49"/>
                </atom>
                <atom name="region">
                    <variable name="_48"/>
                    <variable name="_51"/>
                    <variable name="_52"/>
                </atom>
            </head>
        </dependency>
        <dependency type="TGD">
            <body>
                <atom name="nation">
                    <variable name="_46"/>
                    <variable name="_47"/>
                    <variable name="_48"/>
                    <variable name="_49"/>
                </atom>
                <atom name="region">
                    <variable name="_48"/>
                    <variable name="_51"/>
                    <variable name="_52"/>
                </atom>
            </body>
            <head>
                <atom name="region_nation">
                    <variable name="_46"/>
                    <variable name="_47"/>
                    <variable name="_48"/>
                    <variable name="_51"/>
                </atom>
            </head>
        </dependency>
    </dependencies>
</schema>
