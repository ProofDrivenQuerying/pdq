<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
    <projections name="fv_n_name487182022" type="java.lang.String"/>
    <projections name="fv_n_regionkey487182022" type="java.lang.Integer"/>
    <projections name="fv_n_comment487182022" type="java.lang.String"/>
    <projections name="fv_c_area487182022" type="java.lang.Integer"/>
    <RelationalTerm type="DependentJoinTerm">
        <RelationalTerm type="RenameTerm">
            <renamings name="v_n_nationkey487182022" type="java.lang.Integer"/>
            <renamings name="fv_c_area487182022" type="java.lang.Integer"/>
            <RelationalTerm type="AccessTerm">
                <accessMethod name="pdqWebappCountryFree"/>
                <relation name="Country">
                    <attribute name="c_nationkey" type="java.lang.Integer"/>
                    <attribute name="c_area" type="java.lang.Integer"/>
                    <access-method name="pdqWebappCountryFree"/>
                </relation>
            </RelationalTerm>
        </RelationalTerm>
        <RelationalTerm type="RenameTerm">
            <renamings name="v_n_nationkey487182022" type="java.lang.Integer"/>
            <renamings name="fv_n_name487182022" type="java.lang.String"/>
            <renamings name="fv_n_regionkey487182022" type="java.lang.Integer"/>
            <renamings name="fv_n_comment487182022" type="java.lang.String"/>
            <RelationalTerm type="AccessTerm">
                <accessMethod name="pdqWebappNationInput" inputs="0"/>
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
    <cost value="0.0" type="DoubleCost"/>
</RelationalTermWithCost>
