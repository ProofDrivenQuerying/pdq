<?xml version="1.0" encoding="UTF-8"?>
<plan type="linear" cost="6.282143195488194E12">
<command name="T1">
<operator type="PROJECT">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c129" type="java.lang.Integer"/>
<attribute name="c128" type="java.lang.String"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="name" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="Country"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooCountries" access-method="yh_geo_country">
<outputs>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
</command>
<command name="T2">
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c129" type="java.lang.Integer"/>
<attribute name="c128" type="java.lang.String"/>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c110" type="java.lang.Integer"/>
<attribute name="c144" type="java.lang.String"/>
<attribute name="c145" type="java.lang.String"/>
<attribute name="c146" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="3"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c129" type="java.lang.Integer"/>
<attribute name="c128" type="java.lang.String"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="name" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="Country"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooCountries" access-method="yh_geo_country">
<outputs>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c110" type="java.lang.Integer"/>
<attribute name="c144" type="java.lang.String"/>
<attribute name="c145" type="java.lang.String"/>
<attribute name="c146" type="java.lang.String"/>
</outputs>
<project>
<attribute name="source" type="java.lang.Integer"/>
<attribute name="target" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="ACCESS" relation="YahooBelongsTo" access-method="yh_geo_belongs">
<outputs>
<attribute name="source" type="java.lang.Integer"/>
<attribute name="target" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c127" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c129" type="java.lang.Integer"/>
<attribute name="c128" type="java.lang.String"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="name" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="Country"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooCountries" access-method="yh_geo_country">
<outputs>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
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
</children>
</operator>
</command>
<command name="T3">
<operator type="PROJECT">
<outputs>
<attribute name="c128" type="java.lang.String"/>
</outputs>
<project>
<attribute name="c128" type="java.lang.String"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c129" type="java.lang.Integer"/>
<attribute name="c128" type="java.lang.String"/>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c110" type="java.lang.Integer"/>
<attribute name="c144" type="java.lang.String"/>
<attribute name="c145" type="java.lang.String"/>
<attribute name="c146" type="java.lang.String"/>
<attribute name="c110" type="java.lang.Integer"/>
<attribute name="c111" type="java.lang.Integer"/>
<attribute name="c112" type="java.lang.String"/>
<attribute name="c113" type="java.lang.String"/>
<attribute name="c114" type="java.lang.String"/>
<attribute name="c115" type="java.lang.String"/>
<attribute name="c116" type="java.lang.String"/>
<attribute name="c117" type="java.lang.String"/>
<attribute name="c118" type="java.lang.String"/>
<attribute name="c119" type="java.lang.String"/>
<attribute name="c120" type="java.lang.Double"/>
<attribute name="c121" type="java.lang.Double"/>
<attribute name="c122" type="java.lang.Double"/>
<attribute name="c123" type="java.lang.Double"/>
<attribute name="c124" type="java.lang.Double"/>
<attribute name="c125" type="java.lang.Double"/>
<attribute name="c126" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="4" right="8"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c129" type="java.lang.Integer"/>
<attribute name="c128" type="java.lang.String"/>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c110" type="java.lang.Integer"/>
<attribute name="c144" type="java.lang.String"/>
<attribute name="c145" type="java.lang.String"/>
<attribute name="c146" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="3"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c129" type="java.lang.Integer"/>
<attribute name="c128" type="java.lang.String"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="name" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="Country"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooCountries" access-method="yh_geo_country">
<outputs>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c110" type="java.lang.Integer"/>
<attribute name="c144" type="java.lang.String"/>
<attribute name="c145" type="java.lang.String"/>
<attribute name="c146" type="java.lang.String"/>
</outputs>
<project>
<attribute name="source" type="java.lang.Integer"/>
<attribute name="target" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="ACCESS" relation="YahooBelongsTo" access-method="yh_geo_belongs">
<outputs>
<attribute name="source" type="java.lang.Integer"/>
<attribute name="target" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c127" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c127" type="java.lang.Integer"/>
<attribute name="c129" type="java.lang.Integer"/>
<attribute name="c128" type="java.lang.String"/>
</outputs>
<project>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="name" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="Country"/>
</conjunction>
<child>
<operator type="ACCESS" relation="YahooCountries" access-method="yh_geo_country">
<outputs>
<attribute name="woeid" type="java.lang.Integer"/>
<attribute name="placeType" type="java.lang.Integer"/>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
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
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c110" type="java.lang.Integer"/>
<attribute name="c111" type="java.lang.Integer"/>
<attribute name="c112" type="java.lang.String"/>
<attribute name="c113" type="java.lang.String"/>
<attribute name="c114" type="java.lang.String"/>
<attribute name="c115" type="java.lang.String"/>
<attribute name="c116" type="java.lang.String"/>
<attribute name="c117" type="java.lang.String"/>
<attribute name="c118" type="java.lang.String"/>
<attribute name="c119" type="java.lang.String"/>
<attribute name="c120" type="java.lang.Double"/>
<attribute name="c121" type="java.lang.Double"/>
<attribute name="c122" type="java.lang.Double"/>
<attribute name="c123" type="java.lang.Double"/>
<attribute name="c124" type="java.lang.Double"/>
<attribute name="c125" type="java.lang.Double"/>
<attribute name="c126" type="java.lang.String"/>
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
<predicate type="equality" left="1" value="Asia"/>
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
<attribute name="Asia" type="java.lang.String"/>
</outputs>
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
