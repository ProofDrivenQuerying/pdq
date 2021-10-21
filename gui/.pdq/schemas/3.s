<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<schema name="3">
    <relations>
         <relation name="R">
             <attribute name="x" type="java.lang.String"/>
               <access-method name="R_free"/>
        </relation>
         <relation name="S">
            <attribute name="x" type="java.lang.String"/>
            <attribute name="y" type="java.lang.String"/>
               <access-method name="S_free"/>
        </relation>
        <relation name="T">
            <attribute name="y" type="java.lang.String"/>
            <attribute name="z" type="java.lang.String"/>
            <attribute name="w" type="java.lang.String"/>
               <access-method name="T_free"/>
        </relation>
     </relations>
    <dependencies>
    </dependencies>
</schema>
