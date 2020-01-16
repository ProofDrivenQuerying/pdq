<?xml version="1.0" encoding="UTF-8"?>
<schema name="Yahoo! API" description="">
<relations>
<relation name="YahooPlaceCode" source="yahoo" size="10000000">
		<attribute name="namespace" type="java.lang.String"  input-method="relation.1"/>
		<attribute name="code4"      type="java.lang.String"  input-method="relation.2"/>
		<attribute name="woeid"     type="java.lang.Integer" path="woeid"/>
		<access-method name="yh_geo_code" type="LIMITED" inputs="0,1" cost="1.0"/>
</relation>
<relation name="YahooWeather" source="yahoo" size="100000000">
		<attribute name="woeid"          type="java.lang.Integer" input-method="q.2" />

		<attribute name="city"           type="java.lang.String"  path="location/city"/>
		<attribute name="country2"        type="java.lang.String"  path="location/country"/>
		<attribute name="region"         type="java.lang.String"  path="location/region"/>

		<attribute name="distance_unit"  type="java.lang.String"  path="units/distance"/>
		<attribute name="pressure_unit"  type="java.lang.String"  path="units/pressure"/>
		<attribute name="speed_unit"     type="java.lang.String"  path="units/speed"/>
		<attribute name="temp_unit"      type="java.lang.String"  path="units/temperature"/>

		<attribute name="wind_chill"     type="java.lang.Integer" path="wind/chill"/>
		<attribute name="wind_direction" type="java.lang.Integer" path="wind/direction"/>
		<attribute name="wind_speed"     type="java.lang.String"  path="wind/speed"/>

		<attribute name="humidity"       type="java.lang.Double"  path="atmosphere/humidity"/>
		<attribute name="pressure"       type="java.lang.Double"  path="atmosphere/pressure"/>
		<attribute name="rising"         type="java.lang.Integer" path="atmosphere/rising"/>
		<attribute name="visibility"     type="java.lang.Double"  path="atmosphere/visibility"/>

		<attribute name="sunrise"        type="java.lang.String"  path="astronomy/sunrise"/>
		<attribute name="sunset"         type="java.lang.String"  path="astronomy/sunset"/>

		<attribute name="date"           type="java.lang.String"  path="item/condition/date"/>
		<attribute name="temperature"    type="java.lang.Double"  path="item/condition/temp"/>
		<attribute name="condition"      type="java.lang.String"  path="item/condition/text"/>
		<attribute name="code2"          type="java.lang.Integer" path="item/condition/code2"/>
<access-method name="yh_wtr_woeid" type="LIMITED" inputs="0" cost="10.0"/>
</relation>
</relations>
<dependencies>
</dependencies>
</schema>
