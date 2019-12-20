<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
    <projections name="fv_n_name988407592" type="java.lang.String"/>
    <projections name="fv_n_regionkey988407592" type="java.lang.Integer"/>
    <projections name="fv_n_comment988407592" type="java.lang.String"/>
    <projections name="fv_c_area988407592" type="java.lang.Integer"/>
    <RelationalTerm type="DependentJoinTerm">
        <RelationalTerm type="RenameTerm">
            <renamings name="v__14988407592" type="java.lang.Integer"/>
            <renamings name="fv_c_area988407592" type="java.lang.Integer"/>
            <RelationalTerm type="AccessTerm">
                <accessMethod name="pdqWebappCountryFree"/>
                <inputConstants/>
                <relation name="Country">
                    <attribute name="c_nationkey" type="java.lang.Integer"/>
                    <attribute name="c_area" type="java.lang.Integer"/>
                    <access-method name="pdqWebappCountryFree"/>
                </relation>
            </RelationalTerm>
        </RelationalTerm>
        <RelationalTerm type="RenameTerm">
            <renamings name="v__14988407592" type="java.lang.Integer"/>
            <renamings name="fv_n_name988407592" type="java.lang.String"/>
            <renamings name="fv_n_regionkey988407592" type="java.lang.Integer"/>
            <renamings name="fv_n_comment988407592" type="java.lang.String"/>
            <RelationalTerm type="AccessTerm">
                <accessMethod name="pdqWebappNationInput" inputs="0"/>
                <inputConstants/>
                <relation name="Nation">
                    <attribute name="n_nationkey" type="java.lang.Integer"/>
                    <attribute name="n_name" type="java.lang.String"/>
                    <attribute name="n_regionkey" type="java.lang.Integer"/>
                    <attribute name="n_comment" type="java.lang.String"/>
                    <access-method name="pdqWebappNationInput" inputs="0"/>
                </relation>
            </RelationalTerm>
        </RelationalTerm>
    </RelationalTerm>
    <cost value="4.715198817009208E12" type="DoubleCost"/>
</RelationalTermWithCost>
