<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
    <projections name="fv_y509799921" type="java.lang.String"/>
    <RelationalTerm type="JoinTerm">
        <RelationalTerm type="RenameTerm">
            <renamings name="v__11509799921" type="java.lang.String"/>
            <renamings name="fv_y509799921" type="java.lang.String"/>
            <RelationalTerm type="AccessTerm">
                <accessMethod name="S_free"/>
                <inputConstants/>
                <relation name="S">
                    <attribute name="x" type="java.lang.String"/>
                    <attribute name="y" type="java.lang.String"/>
                    <access-method name="S_free"/>
                </relation>
            </RelationalTerm>
        </RelationalTerm>
        <RelationalTerm type="RenameTerm">
            <renamings name="v__11509799921" type="java.lang.String"/>
            <RelationalTerm type="AccessTerm">
                <accessMethod name="R_free"/>
                <inputConstants/>
                <relation name="R">
                    <attribute name="x" type="java.lang.String"/>
                    <access-method name="R_free"/>
                </relation>
            </RelationalTerm>
        </RelationalTerm>
    </RelationalTerm>
    <cost value="1.4915539188985389E13" type="DoubleCost"/>
</RelationalTermWithCost>
