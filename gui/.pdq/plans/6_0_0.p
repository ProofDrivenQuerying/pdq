<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
    <projections name="fv_city447424928" type="java.lang.String"/>
    <RelationalTerm type="DependentJoinTerm">
        <RelationalTerm type="RenameTerm">
            <renamings name="iso" type="java.lang.String"/>
            <renamings name="FR" type="java.lang.String"/>
            <renamings name="v__15447424928" type="java.lang.Integer"/>
            <RelationalTerm type="AccessTerm">
                <accessMethod name="yh_geo_code" inputs="0,1"/>
                <inputConstants>
                    <entry>
                        <key>0</key>
                        <value value="iso"/>
                    </entry>
                    <entry>
                        <key>1</key>
                        <value value="FR"/>
                    </entry>
                </inputConstants>
                <relation name="YahooPlaceCode">
                    <attribute name="namespace" type="java.lang.String"/>
                    <attribute name="code4" type="java.lang.String"/>
                    <attribute name="woeid" type="java.lang.Integer"/>
                    <access-method name="yh_geo_code" inputs="0,1"/>
                </relation>
            </RelationalTerm>
        </RelationalTerm>
        <RelationalTerm type="SelectionTerm">
            <predicate type="ConjunctiveCondition">
                <predicates position="19" type="ConstantEqualityCondition">
                    <constant value="Sunny"/>
                </predicates>
            </predicate>
            <RelationalTerm type="RenameTerm">
                <renamings name="v__15447424928" type="java.lang.Integer"/>
                <renamings name="fv_city447424928" type="java.lang.String"/>
                <renamings name="v_country2447424928" type="java.lang.String"/>
                <renamings name="v_region447424928" type="java.lang.String"/>
                <renamings name="v_distance_unit447424928" type="java.lang.String"/>
                <renamings name="v_pressure_unit447424928" type="java.lang.String"/>
                <renamings name="v_speed_unit447424928" type="java.lang.String"/>
                <renamings name="v_temp_unit447424928" type="java.lang.String"/>
                <renamings name="v_wind_chill447424928" type="java.lang.Integer"/>
                <renamings name="v_wind_direction447424928" type="java.lang.Integer"/>
                <renamings name="v_wind_speed447424928" type="java.lang.String"/>
                <renamings name="v_humidity447424928" type="java.lang.Double"/>
                <renamings name="v_pressure447424928" type="java.lang.Double"/>
                <renamings name="v_rising447424928" type="java.lang.Integer"/>
                <renamings name="v_visibility447424928" type="java.lang.Double"/>
                <renamings name="v_sunrise447424928" type="java.lang.String"/>
                <renamings name="v_sunset447424928" type="java.lang.String"/>
                <renamings name="v_date447424928" type="java.lang.String"/>
                <renamings name="v_temperature447424928" type="java.lang.Double"/>
                <renamings name="Sunny" type="java.lang.String"/>
                <renamings name="v_code2447424928" type="java.lang.Integer"/>
                <RelationalTerm type="AccessTerm">
                    <accessMethod name="yh_wtr_woeid" inputs="0"/>
                    <inputConstants/>
                    <relation name="YahooWeather">
                        <attribute name="woeid" type="java.lang.Integer"/>
                        <attribute name="city" type="java.lang.String"/>
                        <attribute name="country2" type="java.lang.String"/>
                        <attribute name="region" type="java.lang.String"/>
                        <attribute name="distance_unit" type="java.lang.String"/>
                        <attribute name="pressure_unit" type="java.lang.String"/>
                        <attribute name="speed_unit" type="java.lang.String"/>
                        <attribute name="temp_unit" type="java.lang.String"/>
                        <attribute name="wind_chill" type="java.lang.Integer"/>
                        <attribute name="wind_direction" type="java.lang.Integer"/>
                        <attribute name="wind_speed" type="java.lang.String"/>
                        <attribute name="humidity" type="java.lang.Double"/>
                        <attribute name="pressure" type="java.lang.Double"/>
                        <attribute name="rising" type="java.lang.Integer"/>
                        <attribute name="visibility" type="java.lang.Double"/>
                        <attribute name="sunrise" type="java.lang.String"/>
                        <attribute name="sunset" type="java.lang.String"/>
                        <attribute name="date" type="java.lang.String"/>
                        <attribute name="temperature" type="java.lang.Double"/>
                        <attribute name="condition" type="java.lang.String"/>
                        <attribute name="code2" type="java.lang.Integer"/>
                        <access-method name="yh_wtr_woeid" inputs="0"/>
                    </relation>
                </RelationalTerm>
            </RelationalTerm>
        </RelationalTerm>
    </RelationalTerm>
    <cost value="2.4288172107458594E11" type="DoubleCost"/>
</RelationalTermWithCost>
