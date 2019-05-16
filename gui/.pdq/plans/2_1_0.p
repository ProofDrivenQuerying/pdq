<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
    <projections name="fv_city1274707035" type="java.lang.String"/>
    <projections name="fv_country1274707035" type="java.lang.String"/>
    <projections name="fv_region1274707035" type="java.lang.String"/>
    <projections name="fv_temperature1274707035" type="java.lang.Double"/>
    <RelationalTerm type="DependentJoinTerm">
        <RelationalTerm type="DependentJoinTerm">
            <RelationalTerm type="DependentJoinTerm">
                <RelationalTerm type="DependentJoinTerm">
                    <RelationalTerm type="RenameTerm">
                        <renamings name="iso" type="java.lang.String"/>
                        <renamings name="FR" type="java.lang.String"/>
                        <renamings name="v_homeCountry1274707035" type="java.lang.Integer"/>
                        <RelationalTerm type="AccessTerm">
                            <accessMethod name="yh_geo_code" inputs="0,1"/>
                            <relation name="YahooPlaceCode">
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
<access-method name="yh_geo_code" inputs="0,1"/>
                            </relation>
                        </RelationalTerm>
                    </RelationalTerm>
                    <RelationalTerm type="RenameTerm">
                        <renamings name="descendants" type="java.lang.String"/>
                        <renamings name="v_homeCountry1274707035" type="java.lang.Integer"/>
                        <renamings name="v_myNextVacationSpot1274707035" type="java.lang.Integer"/>
                        <renamings name="v_x91274707035" type="java.lang.String"/>
                        <renamings name="v_x101274707035" type="java.lang.String"/>
                        <renamings name="v_x111274707035" type="java.lang.String"/>
                        <RelationalTerm type="AccessTerm">
                            <accessMethod name="yh_geo_rel" inputs="0,1"/>
                            <relation name="YahooPlaceRelationship">
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
<access-method name="yh_geo_rel" inputs="0,1"/>
                            </relation>
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
                        <renamings name="v_myNextVacationSpot1274707035" type="java.lang.Integer"/>
                        <renamings name="fv_city1274707035" type="java.lang.String"/>
                        <renamings name="fv_country1274707035" type="java.lang.String"/>
                        <renamings name="fv_region1274707035" type="java.lang.String"/>
                        <renamings name="v_distance_unit1274707035" type="java.lang.String"/>
                        <renamings name="v_pressure_unit1274707035" type="java.lang.String"/>
                        <renamings name="v_speed_unit1274707035" type="java.lang.String"/>
                        <renamings name="v_temp_unit1274707035" type="java.lang.String"/>
                        <renamings name="v_wind_chill1274707035" type="java.lang.Integer"/>
                        <renamings name="v_wind_direction1274707035" type="java.lang.Integer"/>
                        <renamings name="v_wind_speed1274707035" type="java.lang.String"/>
                        <renamings name="v_humidity1274707035" type="java.lang.Double"/>
                        <renamings name="v_pressure1274707035" type="java.lang.Double"/>
                        <renamings name="v_rising1274707035" type="java.lang.Integer"/>
                        <renamings name="v_visibility1274707035" type="java.lang.Double"/>
                        <renamings name="v_sunrise1274707035" type="java.lang.String"/>
                        <renamings name="v_sunset1274707035" type="java.lang.String"/>
                        <renamings name="v_date1274707035" type="java.lang.String"/>
                        <renamings name="fv_temperature1274707035" type="java.lang.Double"/>
                        <renamings name="Sunny" type="java.lang.String"/>
                        <renamings name="v_code1274707035" type="java.lang.Integer"/>
                        <RelationalTerm type="AccessTerm">
                            <accessMethod name="yh_wtr_woeid" inputs="0"/>
                            <relation name="YahooWeather">
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="city" type="java.lang.String"/>
<attribute name="country" type="java.lang.String"/>
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
<attribute name="code" type="java.lang.Integer"/>
<access-method name="yh_wtr_woeid" inputs="0"/>
                            </relation>
                        </RelationalTerm>
                    </RelationalTerm>
                </RelationalTerm>
            </RelationalTerm>
            <RelationalTerm type="RenameTerm">
                <renamings name="v_homeCountry1274707035" type="java.lang.Integer"/>
                <renamings name="k1" type="java.lang.String"/>
                <renamings name="k2" type="java.lang.Integer"/>
                <renamings name="k3" type="java.lang.String"/>
                <renamings name="k4" type="java.lang.String"/>
                <renamings name="k5" type="java.lang.String"/>
                <renamings name="k6" type="java.lang.String"/>
                <renamings name="k7" type="java.lang.String"/>
                <renamings name="k8" type="java.lang.String"/>
                <renamings name="k9" type="java.lang.String"/>
                <renamings name="k10" type="java.lang.String"/>
                <renamings name="k11" type="java.lang.Double"/>
                <renamings name="k12" type="java.lang.Double"/>
                <renamings name="k13" type="java.lang.Double"/>
                <renamings name="k14" type="java.lang.Double"/>
                <renamings name="k15" type="java.lang.Double"/>
                <renamings name="k16" type="java.lang.Double"/>
                <renamings name="k17" type="java.lang.String"/>
                <RelationalTerm type="AccessTerm">
                    <accessMethod name="yh_geo_woeid" inputs="0"/>
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
        <RelationalTerm type="SelectionTerm">
            <predicate type="ConjunctiveCondition">
                <predicates position="3" type="ConstantEqualityCondition">
                    <constant value="Point of Interest"/>
                </predicates>
            </predicate>
            <RelationalTerm type="RenameTerm">
                <renamings name="v_myNextVacationSpot1274707035" type="java.lang.Integer"/>
                <renamings name="v_name1274707035" type="java.lang.String"/>
                <renamings name="v_type1274707035" type="java.lang.Integer"/>
                <renamings name="Point of Interest" type="java.lang.String"/>
                <renamings name="v_countryName1274707035" type="java.lang.String"/>
                <renamings name="v_admin11274707035" type="java.lang.String"/>
                <renamings name="v_admin21274707035" type="java.lang.String"/>
                <renamings name="v_admin31274707035" type="java.lang.String"/>
                <renamings name="v_locality11274707035" type="java.lang.String"/>
                <renamings name="v_locality21274707035" type="java.lang.String"/>
                <renamings name="v_postal1274707035" type="java.lang.String"/>
                <renamings name="v_latitude1274707035" type="java.lang.Double"/>
                <renamings name="v_longitude1274707035" type="java.lang.Double"/>
                <renamings name="v_bboxNorth1274707035" type="java.lang.Double"/>
                <renamings name="v_bboxSouth1274707035" type="java.lang.Double"/>
                <renamings name="v_bboxEast1274707035" type="java.lang.Double"/>
                <renamings name="v_bboxWest1274707035" type="java.lang.Double"/>
                <renamings name="v_timezone1274707035" type="java.lang.String"/>
                <RelationalTerm type="AccessTerm">
                    <accessMethod name="yh_geo_woeid" inputs="0"/>
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
    <cost value="4.0" type="DoubleCost"/>
</RelationalTermWithCost>
