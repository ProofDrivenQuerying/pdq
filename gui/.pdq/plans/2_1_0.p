<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
    <projections name="fv_city1238102517" type="java.lang.String"/>
    <projections name="fv__71238102517" type="java.lang.String"/>
    <projections name="fv_region1238102517" type="java.lang.String"/>
    <projections name="fv_temperature1238102517" type="java.lang.Double"/>
    <RelationalTerm type="DependentJoinTerm">
        <RelationalTerm type="DependentJoinTerm">
            <RelationalTerm type="DependentJoinTerm">
                <RelationalTerm type="RenameTerm">
                    <renamings name="iso" type="java.lang.String"/>
                    <renamings name="FR" type="java.lang.String"/>
                    <renamings name="v__81238102517" type="java.lang.Integer"/>
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
                        <predicates position="0" type="ConstantEqualityCondition">
                            <constant value="descendants"/>
                        </predicates>
                        <predicates other="2" position="1" type="AttributeEqualityCondition"/>
                        <predicates position="3" type="ConstantEqualityCondition">
                            <constant value="PointofInterest"/>
                        </predicates>
                    </predicate>
                    <RelationalTerm type="RenameTerm">
                        <renamings name="descendants" type="java.lang.String"/>
                        <renamings name="v__81238102517" type="java.lang.Integer"/>
                        <renamings name="v__81238102517" type="java.lang.Integer"/>
                        <renamings name="PointofInterest" type="java.lang.String"/>
                        <renamings name="v__101238102517" type="java.lang.String"/>
                        <renamings name="v_uri41238102517" type="java.lang.String"/>
                        <RelationalTerm type="AccessTerm">
                            <accessMethod name="yh_geo_rel" inputs="0,1"/>
                            <inputConstants>
<entry>
    <key>0</key>
    <value value="descendants"/>
</entry>
                            </inputConstants>
                            <relation name="YahooPlaceRelationship">
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName4" type="java.lang.String"/>
<attribute name="name4" type="java.lang.String"/>
<attribute name="uri4" type="java.lang.String"/>
<access-method name="yh_geo_rel" inputs="0,1"/>
                            </relation>
                        </RelationalTerm>
                    </RelationalTerm>
                </RelationalTerm>
            </RelationalTerm>
            <RelationalTerm type="SelectionTerm">
                <predicate type="ConjunctiveCondition">
                    <predicates position="3" type="ConstantEqualityCondition">
                        <constant value="PointofInterest"/>
                    </predicates>
                </predicate>
                <RelationalTerm type="RenameTerm">
                    <renamings name="v__81238102517" type="java.lang.Integer"/>
                    <renamings name="v__101238102517" type="java.lang.String"/>
                    <renamings name="v_type1238102517" type="java.lang.Integer"/>
                    <renamings name="PointofInterest" type="java.lang.String"/>
                    <renamings name="fv__71238102517" type="java.lang.String"/>
                    <renamings name="v_admin11238102517" type="java.lang.String"/>
                    <renamings name="v_admin21238102517" type="java.lang.String"/>
                    <renamings name="v_admin31238102517" type="java.lang.String"/>
                    <renamings name="v_locality11238102517" type="java.lang.String"/>
                    <renamings name="v_locality21238102517" type="java.lang.String"/>
                    <renamings name="v_postal1238102517" type="java.lang.String"/>
                    <renamings name="v_centroid_lat1238102517" type="java.lang.Double"/>
                    <renamings name="v_centroid_lng1238102517" type="java.lang.Double"/>
                    <renamings name="v_bboxNorth1238102517" type="java.lang.Double"/>
                    <renamings name="v_bboxSouth1238102517" type="java.lang.Double"/>
                    <renamings name="v_bboxEast1238102517" type="java.lang.Double"/>
                    <renamings name="v_bboxWest1238102517" type="java.lang.Double"/>
                    <renamings name="v_timezone1238102517" type="java.lang.String"/>
                    <RelationalTerm type="AccessTerm">
                        <accessMethod name="yh_geo_woeid" inputs="0"/>
                        <inputConstants/>
                        <relation name="YahooPlaces">
                            <attribute name="woeid" type="java.lang.Integer"/>
                            <attribute name="name" type="java.lang.String"/>
                            <attribute name="type" type="java.lang.Integer"/>
                            <attribute name="placeTypeName" type="java.lang.String"/>
                            <attribute name="country" type="java.lang.String"/>
                            <attribute name="admin1" type="java.lang.String"/>
                            <attribute name="admin2" type="java.lang.String"/>
                            <attribute name="admin3" type="java.lang.String"/>
                            <attribute name="locality1" type="java.lang.String"/>
                            <attribute name="locality2" type="java.lang.String"/>
                            <attribute name="postal" type="java.lang.String"/>
                            <attribute name="centroid_lat" type="java.lang.Double"/>
                            <attribute name="centroid_lng" type="java.lang.Double"/>
                            <attribute name="bboxNorth" type="java.lang.Double"/>
                            <attribute name="bboxSouth" type="java.lang.Double"/>
                            <attribute name="bboxEast" type="java.lang.Double"/>
                            <attribute name="bboxWest" type="java.lang.Double"/>
                            <attribute name="timezone" type="java.lang.String"/>
                            <access-method name="yh_geo_name" inputs="1"/>
                            <access-method name="yh_geo_woeid" inputs="0"/>
                            <access-method name="yh_geo_type" inputs="1,2"/>
                        </relation>
                    </RelationalTerm>
                </RelationalTerm>
            </RelationalTerm>
        </RelationalTerm>
        <RelationalTerm type="SelectionTerm">
            <predicate type="ConjunctiveCondition">
                <predicates position="19" type="ConstantEqualityCondition">
                    <constant value="Sunny"/>
                </predicates>
            </predicate>
            <RelationalTerm type="RenameTerm">
                <renamings name="v__81238102517" type="java.lang.Integer"/>
                <renamings name="fv_city1238102517" type="java.lang.String"/>
                <renamings name="fv__71238102517" type="java.lang.String"/>
                <renamings name="fv_region1238102517" type="java.lang.String"/>
                <renamings name="v_distance_unit1238102517" type="java.lang.String"/>
                <renamings name="v_pressure_unit1238102517" type="java.lang.String"/>
                <renamings name="v_speed_unit1238102517" type="java.lang.String"/>
                <renamings name="v_temp_unit1238102517" type="java.lang.String"/>
                <renamings name="v_wind_chill1238102517" type="java.lang.Integer"/>
                <renamings name="v_wind_direction1238102517" type="java.lang.Integer"/>
                <renamings name="v_wind_speed1238102517" type="java.lang.String"/>
                <renamings name="v_humidity1238102517" type="java.lang.Double"/>
                <renamings name="v_pressure1238102517" type="java.lang.Double"/>
                <renamings name="v_rising1238102517" type="java.lang.Integer"/>
                <renamings name="v_visibility1238102517" type="java.lang.Double"/>
                <renamings name="v_sunrise1238102517" type="java.lang.String"/>
                <renamings name="v_sunset1238102517" type="java.lang.String"/>
                <renamings name="v_date1238102517" type="java.lang.String"/>
                <renamings name="fv_temperature1238102517" type="java.lang.Double"/>
                <renamings name="Sunny" type="java.lang.String"/>
                <renamings name="v_code21238102517" type="java.lang.Integer"/>
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
    <cost value="2.642694106546609E12" type="DoubleCost"/>
</RelationalTermWithCost>
