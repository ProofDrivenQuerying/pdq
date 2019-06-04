<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
<<<<<<< HEAD
    <projections name="fv_city2020823464" type="java.lang.String"/>
    <projections name="fv_country2020823464" type="java.lang.String"/>
    <projections name="fv_region2020823464" type="java.lang.String"/>
    <projections name="fv_temperature2020823464" type="java.lang.Double"/>
=======
    <projections name="fv_city1129221117" type="java.lang.String"/>
    <projections name="fv_country1129221117" type="java.lang.String"/>
    <projections name="fv_region1129221117" type="java.lang.String"/>
    <projections name="fv_temperature1129221117" type="java.lang.Double"/>
>>>>>>> branch 'master' of https://github.com/michaelbenedikt/pdq.git
    <RelationalTerm type="DependentJoinTerm">
        <RelationalTerm type="DependentJoinTerm">
            <RelationalTerm type="DependentJoinTerm">
<<<<<<< HEAD
                <RelationalTerm type="RenameTerm">
                    <renamings name="iso" type="java.lang.String"/>
                    <renamings name="FR" type="java.lang.String"/>
                    <renamings name="v_homeCountry2020823464" type="java.lang.Integer"/>
                    <RelationalTerm type="AccessTerm">
                        <accessMethod name="yh_geo_code" inputs="0,1"/>
                        <relation name="YahooPlaceCode">
                            <attribute name="namespace" type="java.lang.String"/>
                            <attribute name="code" type="java.lang.String"/>
                            <attribute name="woeid" type="java.lang.Integer"/>
                            <access-method name="yh_geo_code" inputs="0,1"/>
                        </relation>
=======
                <RelationalTerm type="DependentJoinTerm">
                    <RelationalTerm type="RenameTerm">
                        <renamings name="iso" type="java.lang.String"/>
                        <renamings name="FR" type="java.lang.String"/>
                        <renamings name="v_homeCountry1129221117" type="java.lang.Integer"/>
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
                        <renamings name="v_homeCountry1129221117" type="java.lang.Integer"/>
                        <renamings name="v_myNextVacationSpot1129221117" type="java.lang.Integer"/>
                        <renamings name="v_x91129221117" type="java.lang.String"/>
                        <renamings name="v_x101129221117" type="java.lang.String"/>
                        <renamings name="v_x111129221117" type="java.lang.String"/>
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
>>>>>>> branch 'master' of https://github.com/michaelbenedikt/pdq.git
                    </RelationalTerm>
                </RelationalTerm>
<<<<<<< HEAD
                <RelationalTerm type="RenameTerm">
                    <renamings name="descendants" type="java.lang.String"/>
                    <renamings name="v_homeCountry2020823464" type="java.lang.Integer"/>
                    <renamings name="v_myNextVacationSpot2020823464" type="java.lang.Integer"/>
                    <renamings name="v_x92020823464" type="java.lang.String"/>
                    <renamings name="v_x102020823464" type="java.lang.String"/>
                    <renamings name="v_x112020823464" type="java.lang.String"/>
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
=======
                <RelationalTerm type="SelectionTerm">
                    <predicate type="ConjunctiveCondition">
                        <predicates position="19" type="ConstantEqualityCondition">
                            <constant value="Sunny"/>
                        </predicates>
                    </predicate>
                    <RelationalTerm type="RenameTerm">
                        <renamings name="v_myNextVacationSpot1129221117" type="java.lang.Integer"/>
                        <renamings name="fv_city1129221117" type="java.lang.String"/>
                        <renamings name="fv_country1129221117" type="java.lang.String"/>
                        <renamings name="fv_region1129221117" type="java.lang.String"/>
                        <renamings name="v_distance_unit1129221117" type="java.lang.String"/>
                        <renamings name="v_pressure_unit1129221117" type="java.lang.String"/>
                        <renamings name="v_speed_unit1129221117" type="java.lang.String"/>
                        <renamings name="v_temp_unit1129221117" type="java.lang.String"/>
                        <renamings name="v_wind_chill1129221117" type="java.lang.Integer"/>
                        <renamings name="v_wind_direction1129221117" type="java.lang.Integer"/>
                        <renamings name="v_wind_speed1129221117" type="java.lang.String"/>
                        <renamings name="v_humidity1129221117" type="java.lang.Double"/>
                        <renamings name="v_pressure1129221117" type="java.lang.Double"/>
                        <renamings name="v_rising1129221117" type="java.lang.Integer"/>
                        <renamings name="v_visibility1129221117" type="java.lang.Double"/>
                        <renamings name="v_sunrise1129221117" type="java.lang.String"/>
                        <renamings name="v_sunset1129221117" type="java.lang.String"/>
                        <renamings name="v_date1129221117" type="java.lang.String"/>
                        <renamings name="fv_temperature1129221117" type="java.lang.Double"/>
                        <renamings name="Sunny" type="java.lang.String"/>
                        <renamings name="v_code1129221117" type="java.lang.Integer"/>
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
>>>>>>> branch 'master' of https://github.com/michaelbenedikt/pdq.git
                    </RelationalTerm>
                </RelationalTerm>
            </RelationalTerm>
<<<<<<< HEAD
            <RelationalTerm type="SelectionTerm">
                <predicate type="ConjunctiveCondition">
                    <predicates position="19" type="ConstantEqualityCondition">
                        <constant value="Sunny"/>
                    </predicates>
                </predicate>
                <RelationalTerm type="RenameTerm">
                    <renamings name="v_myNextVacationSpot2020823464" type="java.lang.Integer"/>
                    <renamings name="fv_city2020823464" type="java.lang.String"/>
                    <renamings name="fv_country2020823464" type="java.lang.String"/>
                    <renamings name="fv_region2020823464" type="java.lang.String"/>
                    <renamings name="v_distance_unit2020823464" type="java.lang.String"/>
                    <renamings name="v_pressure_unit2020823464" type="java.lang.String"/>
                    <renamings name="v_speed_unit2020823464" type="java.lang.String"/>
                    <renamings name="v_temp_unit2020823464" type="java.lang.String"/>
                    <renamings name="v_wind_chill2020823464" type="java.lang.Integer"/>
                    <renamings name="v_wind_direction2020823464" type="java.lang.Integer"/>
                    <renamings name="v_wind_speed2020823464" type="java.lang.String"/>
                    <renamings name="v_humidity2020823464" type="java.lang.Double"/>
                    <renamings name="v_pressure2020823464" type="java.lang.Double"/>
                    <renamings name="v_rising2020823464" type="java.lang.Integer"/>
                    <renamings name="v_visibility2020823464" type="java.lang.Double"/>
                    <renamings name="v_sunrise2020823464" type="java.lang.String"/>
                    <renamings name="v_sunset2020823464" type="java.lang.String"/>
                    <renamings name="v_date2020823464" type="java.lang.String"/>
                    <renamings name="fv_temperature2020823464" type="java.lang.Double"/>
                    <renamings name="Sunny" type="java.lang.String"/>
                    <renamings name="v_code2020823464" type="java.lang.Integer"/>
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
=======
            <RelationalTerm type="RenameTerm">
                <renamings name="v_homeCountry1129221117" type="java.lang.Integer"/>
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
>>>>>>> branch 'master' of https://github.com/michaelbenedikt/pdq.git
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
<<<<<<< HEAD
                <renamings name="v_myNextVacationSpot2020823464" type="java.lang.Integer"/>
                <renamings name="v_name2020823464" type="java.lang.String"/>
                <renamings name="v_type2020823464" type="java.lang.Integer"/>
=======
                <renamings name="v_myNextVacationSpot1129221117" type="java.lang.Integer"/>
                <renamings name="v_name1129221117" type="java.lang.String"/>
                <renamings name="v_type1129221117" type="java.lang.Integer"/>
>>>>>>> branch 'master' of https://github.com/michaelbenedikt/pdq.git
                <renamings name="Point of Interest" type="java.lang.String"/>
<<<<<<< HEAD
                <renamings name="v_countryName2020823464" type="java.lang.String"/>
                <renamings name="v_admin12020823464" type="java.lang.String"/>
                <renamings name="v_admin22020823464" type="java.lang.String"/>
                <renamings name="v_admin32020823464" type="java.lang.String"/>
                <renamings name="v_locality12020823464" type="java.lang.String"/>
                <renamings name="v_locality22020823464" type="java.lang.String"/>
                <renamings name="v_postal2020823464" type="java.lang.String"/>
                <renamings name="v_latitude2020823464" type="java.lang.Double"/>
                <renamings name="v_longitude2020823464" type="java.lang.Double"/>
                <renamings name="v_bboxNorth2020823464" type="java.lang.Double"/>
                <renamings name="v_bboxSouth2020823464" type="java.lang.Double"/>
                <renamings name="v_bboxEast2020823464" type="java.lang.Double"/>
                <renamings name="v_bboxWest2020823464" type="java.lang.Double"/>
                <renamings name="v_timezone2020823464" type="java.lang.String"/>
=======
                <renamings name="v_countryName1129221117" type="java.lang.String"/>
                <renamings name="v_admin11129221117" type="java.lang.String"/>
                <renamings name="v_admin21129221117" type="java.lang.String"/>
                <renamings name="v_admin31129221117" type="java.lang.String"/>
                <renamings name="v_locality11129221117" type="java.lang.String"/>
                <renamings name="v_locality21129221117" type="java.lang.String"/>
                <renamings name="v_postal1129221117" type="java.lang.String"/>
                <renamings name="v_latitude1129221117" type="java.lang.Double"/>
                <renamings name="v_longitude1129221117" type="java.lang.Double"/>
                <renamings name="v_bboxNorth1129221117" type="java.lang.Double"/>
                <renamings name="v_bboxSouth1129221117" type="java.lang.Double"/>
                <renamings name="v_bboxEast1129221117" type="java.lang.Double"/>
                <renamings name="v_bboxWest1129221117" type="java.lang.Double"/>
                <renamings name="v_timezone1129221117" type="java.lang.String"/>
>>>>>>> branch 'master' of https://github.com/michaelbenedikt/pdq.git
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
    <cost value="2.0858554544071904E17" type="DoubleCost"/>
</RelationalTermWithCost>
