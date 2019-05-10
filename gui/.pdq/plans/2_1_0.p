<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
    <projections name="fv_city792678760" type="java.lang.String"/>
    <projections name="fv_country792678760" type="java.lang.String"/>
    <projections name="fv_region792678760" type="java.lang.String"/>
    <projections name="fv_temperature792678760" type="java.lang.Double"/>
    <RelationalTerm type="DependentJoinTerm">
        <RelationalTerm type="DependentJoinTerm">
            <RelationalTerm type="DependentJoinTerm">
                <RelationalTerm type="DependentJoinTerm">
                    <RelationalTerm type="RenameTerm">
                        <renamings name="iso" type="java.lang.String"/>
                        <renamings name="FR" type="java.lang.String"/>
                        <renamings name="v_homeCountry792678760" type="java.lang.Integer"/>
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
                        <renamings name="v_homeCountry792678760" type="java.lang.Integer"/>
                        <renamings name="v_myNextVacationSpot792678760" type="java.lang.Integer"/>
                        <renamings name="v_x9792678760" type="java.lang.String"/>
                        <renamings name="v_x10792678760" type="java.lang.String"/>
                        <renamings name="v_x11792678760" type="java.lang.String"/>
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
                        <renamings name="v_myNextVacationSpot792678760" type="java.lang.Integer"/>
                        <renamings name="fv_city792678760" type="java.lang.String"/>
                        <renamings name="fv_country792678760" type="java.lang.String"/>
                        <renamings name="fv_region792678760" type="java.lang.String"/>
                        <renamings name="v_distance_unit792678760" type="java.lang.String"/>
                        <renamings name="v_pressure_unit792678760" type="java.lang.String"/>
                        <renamings name="v_speed_unit792678760" type="java.lang.String"/>
                        <renamings name="v_temp_unit792678760" type="java.lang.String"/>
                        <renamings name="v_wind_chill792678760" type="java.lang.Integer"/>
                        <renamings name="v_wind_direction792678760" type="java.lang.Integer"/>
                        <renamings name="v_wind_speed792678760" type="java.lang.String"/>
                        <renamings name="v_humidity792678760" type="java.lang.Double"/>
                        <renamings name="v_pressure792678760" type="java.lang.Double"/>
                        <renamings name="v_rising792678760" type="java.lang.Integer"/>
                        <renamings name="v_visibility792678760" type="java.lang.Double"/>
                        <renamings name="v_sunrise792678760" type="java.lang.String"/>
                        <renamings name="v_sunset792678760" type="java.lang.String"/>
                        <renamings name="v_date792678760" type="java.lang.String"/>
                        <renamings name="fv_temperature792678760" type="java.lang.Double"/>
                        <renamings name="Sunny" type="java.lang.String"/>
                        <renamings name="v_code792678760" type="java.lang.Integer"/>
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
                <renamings name="v_homeCountry792678760" type="java.lang.Integer"/>
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
                <renamings name="v_myNextVacationSpot792678760" type="java.lang.Integer"/>
                <renamings name="v_name792678760" type="java.lang.String"/>
                <renamings name="v_type792678760" type="java.lang.Integer"/>
                <renamings name="Point of Interest" type="java.lang.String"/>
                <renamings name="v_countryName792678760" type="java.lang.String"/>
                <renamings name="v_admin1792678760" type="java.lang.String"/>
                <renamings name="v_admin2792678760" type="java.lang.String"/>
                <renamings name="v_admin3792678760" type="java.lang.String"/>
                <renamings name="v_locality1792678760" type="java.lang.String"/>
                <renamings name="v_locality2792678760" type="java.lang.String"/>
                <renamings name="v_postal792678760" type="java.lang.String"/>
                <renamings name="v_latitude792678760" type="java.lang.Double"/>
                <renamings name="v_longitude792678760" type="java.lang.Double"/>
                <renamings name="v_bboxNorth792678760" type="java.lang.Double"/>
                <renamings name="v_bboxSouth792678760" type="java.lang.Double"/>
                <renamings name="v_bboxEast792678760" type="java.lang.Double"/>
                <renamings name="v_bboxWest792678760" type="java.lang.Double"/>
                <renamings name="v_timezone792678760" type="java.lang.String"/>
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
