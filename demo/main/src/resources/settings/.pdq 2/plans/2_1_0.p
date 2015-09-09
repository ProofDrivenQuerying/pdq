<?xml version="1.0" encoding="UTF-8"?>
<plan type="linear" cost="6.212118363566708E14">
<command name="T1">
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</command>
<command name="T2">
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<project>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="descendants"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceRelationship" access-method="yh_geo_rel">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
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
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
</command>
<command name="T3">
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c6" type="java.lang.String"/>
<attribute name="c7" type="java.lang.Integer"/>
<attribute name="c8" type="java.lang.String"/>
<attribute name="c9" type="java.lang.String"/>
<attribute name="c10" type="java.lang.String"/>
<attribute name="c11" type="java.lang.String"/>
<attribute name="c12" type="java.lang.String"/>
<attribute name="c13" type="java.lang.String"/>
<attribute name="c14" type="java.lang.String"/>
<attribute name="c15" type="java.lang.Double"/>
<attribute name="c16" type="java.lang.Double"/>
<attribute name="c17" type="java.lang.Double"/>
<attribute name="c18" type="java.lang.Double"/>
<attribute name="c19" type="java.lang.Double"/>
<attribute name="c20" type="java.lang.Double"/>
<attribute name="c21" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" right="6"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<project>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="descendants"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceRelationship" access-method="yh_geo_rel">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
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
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c6" type="java.lang.String"/>
<attribute name="c7" type="java.lang.Integer"/>
<attribute name="c8" type="java.lang.String"/>
<attribute name="c9" type="java.lang.String"/>
<attribute name="c10" type="java.lang.String"/>
<attribute name="c11" type="java.lang.String"/>
<attribute name="c12" type="java.lang.String"/>
<attribute name="c13" type="java.lang.String"/>
<attribute name="c14" type="java.lang.String"/>
<attribute name="c15" type="java.lang.Double"/>
<attribute name="c16" type="java.lang.Double"/>
<attribute name="c17" type="java.lang.Double"/>
<attribute name="c18" type="java.lang.Double"/>
<attribute name="c19" type="java.lang.Double"/>
<attribute name="c20" type="java.lang.Double"/>
<attribute name="c21" type="java.lang.String"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="type" type="java.lang.Integer"/>
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
<predicate type="equality" left="3" value="Point of Interest"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaces" access-method="yh_geo_woeid">
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
<operator type="PROJECT">
<outputs>
<attribute name="c2" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c2" type="java.lang.Integer"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<project>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="descendants"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceRelationship" access-method="yh_geo_rel">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
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
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
</command>
<command name="T4">
<operator type="PROJECT">
<outputs>
<attribute name="c22" type="java.lang.String"/>
<attribute name="c23" type="java.lang.String"/>
<attribute name="c24" type="java.lang.String"/>
<attribute name="c39" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="c22" type="java.lang.String"/>
<attribute name="c23" type="java.lang.String"/>
<attribute name="c24" type="java.lang.String"/>
<attribute name="c39" type="java.lang.Double"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c6" type="java.lang.String"/>
<attribute name="c7" type="java.lang.Integer"/>
<attribute name="c8" type="java.lang.String"/>
<attribute name="c9" type="java.lang.String"/>
<attribute name="c10" type="java.lang.String"/>
<attribute name="c11" type="java.lang.String"/>
<attribute name="c12" type="java.lang.String"/>
<attribute name="c13" type="java.lang.String"/>
<attribute name="c14" type="java.lang.String"/>
<attribute name="c15" type="java.lang.Double"/>
<attribute name="c16" type="java.lang.Double"/>
<attribute name="c17" type="java.lang.Double"/>
<attribute name="c18" type="java.lang.Double"/>
<attribute name="c19" type="java.lang.Double"/>
<attribute name="c20" type="java.lang.Double"/>
<attribute name="c21" type="java.lang.String"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c22" type="java.lang.String"/>
<attribute name="c23" type="java.lang.String"/>
<attribute name="c24" type="java.lang.String"/>
<attribute name="c25" type="java.lang.String"/>
<attribute name="c26" type="java.lang.String"/>
<attribute name="c27" type="java.lang.String"/>
<attribute name="c28" type="java.lang.String"/>
<attribute name="c29" type="java.lang.Integer"/>
<attribute name="c30" type="java.lang.Integer"/>
<attribute name="c31" type="java.lang.String"/>
<attribute name="c32" type="java.lang.Double"/>
<attribute name="c33" type="java.lang.Double"/>
<attribute name="c34" type="java.lang.Integer"/>
<attribute name="c35" type="java.lang.Double"/>
<attribute name="c36" type="java.lang.String"/>
<attribute name="c37" type="java.lang.String"/>
<attribute name="c38" type="java.lang.String"/>
<attribute name="c39" type="java.lang.Double"/>
<attribute name="c40" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" right="23"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c6" type="java.lang.String"/>
<attribute name="c7" type="java.lang.Integer"/>
<attribute name="c8" type="java.lang.String"/>
<attribute name="c9" type="java.lang.String"/>
<attribute name="c10" type="java.lang.String"/>
<attribute name="c11" type="java.lang.String"/>
<attribute name="c12" type="java.lang.String"/>
<attribute name="c13" type="java.lang.String"/>
<attribute name="c14" type="java.lang.String"/>
<attribute name="c15" type="java.lang.Double"/>
<attribute name="c16" type="java.lang.Double"/>
<attribute name="c17" type="java.lang.Double"/>
<attribute name="c18" type="java.lang.Double"/>
<attribute name="c19" type="java.lang.Double"/>
<attribute name="c20" type="java.lang.Double"/>
<attribute name="c21" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" right="6"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<project>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="descendants"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceRelationship" access-method="yh_geo_rel">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
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
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c6" type="java.lang.String"/>
<attribute name="c7" type="java.lang.Integer"/>
<attribute name="c8" type="java.lang.String"/>
<attribute name="c9" type="java.lang.String"/>
<attribute name="c10" type="java.lang.String"/>
<attribute name="c11" type="java.lang.String"/>
<attribute name="c12" type="java.lang.String"/>
<attribute name="c13" type="java.lang.String"/>
<attribute name="c14" type="java.lang.String"/>
<attribute name="c15" type="java.lang.Double"/>
<attribute name="c16" type="java.lang.Double"/>
<attribute name="c17" type="java.lang.Double"/>
<attribute name="c18" type="java.lang.Double"/>
<attribute name="c19" type="java.lang.Double"/>
<attribute name="c20" type="java.lang.Double"/>
<attribute name="c21" type="java.lang.String"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="type" type="java.lang.Integer"/>
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
<predicate type="equality" left="3" value="Point of Interest"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaces" access-method="yh_geo_woeid">
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
<operator type="PROJECT">
<outputs>
<attribute name="c2" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c2" type="java.lang.Integer"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<project>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="descendants"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceRelationship" access-method="yh_geo_rel">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
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
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c22" type="java.lang.String"/>
<attribute name="c23" type="java.lang.String"/>
<attribute name="c24" type="java.lang.String"/>
<attribute name="c25" type="java.lang.String"/>
<attribute name="c26" type="java.lang.String"/>
<attribute name="c27" type="java.lang.String"/>
<attribute name="c28" type="java.lang.String"/>
<attribute name="c29" type="java.lang.Integer"/>
<attribute name="c30" type="java.lang.Integer"/>
<attribute name="c31" type="java.lang.String"/>
<attribute name="c32" type="java.lang.Double"/>
<attribute name="c33" type="java.lang.Double"/>
<attribute name="c34" type="java.lang.Integer"/>
<attribute name="c35" type="java.lang.Double"/>
<attribute name="c36" type="java.lang.String"/>
<attribute name="c37" type="java.lang.String"/>
<attribute name="c38" type="java.lang.String"/>
<attribute name="c39" type="java.lang.Double"/>
<attribute name="c40" type="java.lang.Integer"/>
</outputs>
<project>
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
<attribute name="code" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
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
</outputs>
<conjunction>
<predicate type="equality" left="19" value="Sunny"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooWeather" access-method="yh_wtr_woeid">
<outputs>
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
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c2" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c2" type="java.lang.Integer"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c6" type="java.lang.String"/>
<attribute name="c7" type="java.lang.Integer"/>
<attribute name="c8" type="java.lang.String"/>
<attribute name="c9" type="java.lang.String"/>
<attribute name="c10" type="java.lang.String"/>
<attribute name="c11" type="java.lang.String"/>
<attribute name="c12" type="java.lang.String"/>
<attribute name="c13" type="java.lang.String"/>
<attribute name="c14" type="java.lang.String"/>
<attribute name="c15" type="java.lang.Double"/>
<attribute name="c16" type="java.lang.Double"/>
<attribute name="c17" type="java.lang.Double"/>
<attribute name="c18" type="java.lang.Double"/>
<attribute name="c19" type="java.lang.Double"/>
<attribute name="c20" type="java.lang.Double"/>
<attribute name="c21" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" right="6"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<project>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="descendants"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceRelationship" access-method="yh_geo_rel">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
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
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c6" type="java.lang.String"/>
<attribute name="c7" type="java.lang.Integer"/>
<attribute name="c8" type="java.lang.String"/>
<attribute name="c9" type="java.lang.String"/>
<attribute name="c10" type="java.lang.String"/>
<attribute name="c11" type="java.lang.String"/>
<attribute name="c12" type="java.lang.String"/>
<attribute name="c13" type="java.lang.String"/>
<attribute name="c14" type="java.lang.String"/>
<attribute name="c15" type="java.lang.Double"/>
<attribute name="c16" type="java.lang.Double"/>
<attribute name="c17" type="java.lang.Double"/>
<attribute name="c18" type="java.lang.Double"/>
<attribute name="c19" type="java.lang.Double"/>
<attribute name="c20" type="java.lang.Double"/>
<attribute name="c21" type="java.lang.String"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="type" type="java.lang.Integer"/>
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
<predicate type="equality" left="3" value="Point of Interest"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaces" access-method="yh_geo_woeid">
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
<operator type="PROJECT">
<outputs>
<attribute name="c2" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c2" type="java.lang.Integer"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
<attribute name="c2" type="java.lang.Integer"/>
<attribute name="c3" type="java.lang.String"/>
<attribute name="c4" type="java.lang.String"/>
<attribute name="c5" type="java.lang.String"/>
</outputs>
<project>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="descendants"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceRelationship" access-method="yh_geo_rel">
<outputs>
<attribute name="relation" type="java.lang.String"/>
<attribute name="of" type="java.lang.Integer"/>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="descendants" type="java.lang.String"/>
<attribute name="c1" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c1" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="iso"/>
<predicate type="equality" left="1" value="FR"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooPlaceCode" access-method="yh_geo_code">
<outputs>
<attribute name="namespace" type="java.lang.String"/>
<attribute name="code" type="java.lang.String"/>
<attribute name="woeid" type="java.lang.Integer"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="iso" type="java.lang.String"/>
<attribute name="FR" type="java.lang.String"/>
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
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</child>
</operator>
</children>
</operator>
</child>
</operator>
</command>
</plan>
