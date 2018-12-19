<?xml version="1.0" encoding="UTF-8"?>
<plan type="linear" cost="1.8194444444444444E7">
<command name="T1">
<operator type="PROJECT">
<outputs>
<attribute name="c74" type="java.lang.String"/>
</outputs>
<project>
<attribute name="c74" type="java.lang.String"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c71" type="java.lang.Integer"/>
<attribute name="c72" type="java.lang.Integer"/>
<attribute name="c73" type="java.lang.String"/>
<attribute name="c74" type="java.lang.String"/>
<attribute name="c75" type="java.lang.String"/>
<attribute name="c76" type="java.lang.String"/>
<attribute name="c77" type="java.lang.String"/>
<attribute name="c78" type="java.lang.String"/>
<attribute name="c79" type="java.lang.String"/>
<attribute name="c80" type="java.lang.String"/>
<attribute name="c81" type="java.lang.Double"/>
<attribute name="c82" type="java.lang.Double"/>
<attribute name="c83" type="java.lang.Double"/>
<attribute name="c84" type="java.lang.Double"/>
<attribute name="c85" type="java.lang.Double"/>
<attribute name="c86" type="java.lang.Double"/>
<attribute name="c87" type="java.lang.String"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
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
</project>
<child>
<operator type="SELECT">
<outputs>
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
</outputs>
<conjunction>
<predicate type="equality" left="1" value="Eiffel Tower"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaces" access-method="yh_geo_name">
<outputs>
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
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="Eiffel Tower" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</command>
</plan>
