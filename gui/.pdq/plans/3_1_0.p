<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
    <projections name="fv_w553224144" type="java.lang.String"/>
    <RelationalTerm type="JoinTerm">
        <RelationalTerm type="JoinTerm">
            <RelationalTerm type="RenameTerm">
                <renamings name="v__1553224144" type="java.lang.String"/>
                <renamings name="v_z553224144" type="java.lang.String"/>
                <renamings name="fv_w553224144" type="java.lang.String"/>
                <RelationalTerm type="AccessTerm">
                    <accessMethod name="T_free"/>
                    <relation name="T">
                        <attribute name="y" type="java.lang.String"/>
                        <attribute name="z" type="java.lang.String"/>
                        <attribute name="w" type="java.lang.String"/>
                        <access-method name="T_free"/>
                    </relation>
                </RelationalTerm>
            </RelationalTerm>
            <RelationalTerm type="RenameTerm">
                <renamings name="v__0553224144" type="java.lang.String"/>
                <renamings name="v__1553224144" type="java.lang.String"/>
                <RelationalTerm type="AccessTerm">
                    <accessMethod name="S_free"/>
                    <relation name="S">
                        <attribute name="x" type="java.lang.String"/>
                        <attribute name="y" type="java.lang.String"/>
                        <access-method name="S_free"/>
                    </relation>
                </RelationalTerm>
            </RelationalTerm>
        </RelationalTerm>
        <RelationalTerm type="RenameTerm">
            <renamings name="v__0553224144" type="java.lang.String"/>
            <RelationalTerm type="AccessTerm">
                <accessMethod name="R_free"/>
                <relation name="R">
                    <attribute name="x" type="java.lang.String"/>
                    <access-method name="R_free"/>
                </relation>
            </RelationalTerm>
        </RelationalTerm>
    </RelationalTerm>
    <cost value="0.0" type="DoubleCost"/>
</RelationalTermWithCost>
