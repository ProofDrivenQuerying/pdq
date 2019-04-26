<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="JoinTerm">
    <RelationalTerm type="RenameTerm">
        <renamings name="c5" type="java.lang.String"/>
        <renamings name="c6" type="java.lang.String"/>
        <RelationalTerm type="AccessTerm">
            <accessMethod name="S_limited"/>
            <relation name="S">
                <attribute name="x" type="java.lang.String"/>
                <attribute name="y" type="java.lang.String"/>
                <access-method name="S_limited"/>
            </relation>
        </RelationalTerm>
    </RelationalTerm>
    <RelationalTerm type="RenameTerm">
        <renamings name="c5" type="java.lang.String"/>
        <RelationalTerm type="AccessTerm">
            <accessMethod name="R_free"/>
            <relation name="R">
                <attribute name="x" type="java.lang.String"/>
                <access-method name="R_free"/>
            </relation>
        </RelationalTerm>
    </RelationalTerm>
    <cost value="1.4915539188985389E13" type="DoubleCost"/>
</RelationalTermWithCost>
