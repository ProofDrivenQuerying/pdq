<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
    <projections name="fv_s_acctbal1899500703" type="java.lang.String"/>
    <projections name="fv_s_name1899500703" type="java.lang.String"/>
    <projections name="fv_ps_supplycost1899500703" type="java.lang.String"/>
    <projections name="fv_n_regionkey1899500703" type="java.lang.Integer"/>
    <RelationalTerm type="JoinTerm">
        <RelationalTerm type="DependentJoinTerm">
            <RelationalTerm type="RenameTerm">
                <renamings name="v__21899500703" type="java.lang.Integer"/>
                <renamings name="fv_s_name1899500703" type="java.lang.String"/>
                <renamings name="v_s_address1899500703" type="java.lang.String"/>
                <renamings name="v__31899500703" type="java.lang.Integer"/>
                <renamings name="v_s_phone1899500703" type="java.lang.String"/>
                <renamings name="fv_s_acctbal1899500703" type="java.lang.String"/>
                <renamings name="v_s_comment1899500703" type="java.lang.String"/>
                <RelationalTerm type="AccessTerm">
                    <accessMethod name="m20"/>
                    <inputConstants/>
                    <relation name="supplier">
                        <attribute name="s_suppkey" type="java.lang.Integer"/>
                        <attribute name="s_name" type="java.lang.String"/>
                        <attribute name="s_address" type="java.lang.String"/>
                        <attribute name="s_nationkey" type="java.lang.Integer"/>
                        <attribute name="s_phone" type="java.lang.String"/>
                        <attribute name="s_acctbal" type="java.lang.String"/>
                        <attribute name="s_comment" type="java.lang.String"/>
                        <access-method name="m20"/>
                    </relation>
                </RelationalTerm>
            </RelationalTerm>
            <RelationalTerm type="RenameTerm">
                <renamings name="v_ps_partkey1899500703" type="java.lang.Integer"/>
                <renamings name="v__21899500703" type="java.lang.Integer"/>
                <renamings name="v_ps_availqty1899500703" type="java.lang.Integer"/>
                <renamings name="fv_ps_supplycost1899500703" type="java.lang.String"/>
                <renamings name="v_ps_comment1899500703" type="java.lang.String"/>
                <RelationalTerm type="AccessTerm">
                    <accessMethod name="m5" inputs="1"/>
                    <inputConstants/>
                    <relation name="partsupp">
                        <attribute name="ps_partkey" type="java.lang.Integer"/>
                        <attribute name="ps_suppkey" type="java.lang.Integer"/>
                        <attribute name="ps_availqty" type="java.lang.Integer"/>
                        <attribute name="ps_supplycost" type="java.lang.String"/>
                        <attribute name="ps_comment" type="java.lang.String"/>
                        <access-method name="m4" inputs="0"/>
                        <access-method name="m5" inputs="1"/>
                        <access-method name="m6" inputs="0,1"/>
                    </relation>
                </RelationalTerm>
            </RelationalTerm>
        </RelationalTerm>
        <RelationalTerm type="RenameTerm">
            <renamings name="v__31899500703" type="java.lang.Integer"/>
            <renamings name="v_n_nationkey1899500703" type="java.lang.String"/>
            <renamings name="fv_n_regionkey1899500703" type="java.lang.Integer"/>
            <renamings name="k8" type="java.lang.String"/>
            <RelationalTerm type="AccessTerm">
                <accessMethod name="m13"/>
                <inputConstants/>
                <relation name="region_nation">
                    <attribute name="nation_key" type="java.lang.Integer"/>
                    <attribute name="nation_name" type="java.lang.String"/>
                    <attribute name="region_key" type="java.lang.Integer"/>
                    <attribute name="region_name" type="java.lang.String"/>
                    <access-method name="m13"/>
                </relation>
            </RelationalTerm>
        </RelationalTerm>
    </RelationalTerm>
    <cost value="1.21827682200177536E17" type="DoubleCost"/>
</RelationalTermWithCost>
