<?xml version="1.0" encoding="UTF-8"?>
<plan type="linear" cost="5.0831598908788307E17">
<command name="T1">
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
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
<predicate type="equality" left="0" value="children"/>
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
<attribute name="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="6"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
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
<predicate type="equality" left="0" value="children"/>
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
<attribute name="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
</outputs>
<project>
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
</project>
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
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
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
<predicate type="equality" left="0" value="children"/>
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
<attribute name="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
</children>
</operator>
</command>
<command name="T4">
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k289" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="8" right="25"/>
<predicate type="equality" left="9" right="24"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="6"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
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
<predicate type="equality" left="0" value="children"/>
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
<attribute name="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
</outputs>
<project>
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
</project>
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
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
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
<predicate type="equality" left="0" value="children"/>
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
<attribute name="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k289" type="java.lang.String"/>
</outputs>
<project>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="code" type="java.lang.Integer"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="ACCESS" relation="YahooPlaceType" access-method="yh_geo_types">
<outputs>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="code" type="java.lang.Integer"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</children>
</operator>
</command>
<command name="T5">
<operator type="PROJECT">
<outputs>
<attribute name="c46" type="java.lang.String"/>
<attribute name="c47" type="java.lang.String"/>
<attribute name="c48" type="java.lang.String"/>
<attribute name="c63" type="java.lang.Double"/>
<attribute name="c64" type="java.lang.String"/>
</outputs>
<project>
<attribute name="c46" type="java.lang.String"/>
<attribute name="c47" type="java.lang.String"/>
<attribute name="c48" type="java.lang.String"/>
<attribute name="c63" type="java.lang.Double"/>
<attribute name="c64" type="java.lang.String"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k289" type="java.lang.String"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c46" type="java.lang.String"/>
<attribute name="c47" type="java.lang.String"/>
<attribute name="c48" type="java.lang.String"/>
<attribute name="c49" type="java.lang.String"/>
<attribute name="c50" type="java.lang.String"/>
<attribute name="c51" type="java.lang.String"/>
<attribute name="c52" type="java.lang.String"/>
<attribute name="c53" type="java.lang.Integer"/>
<attribute name="c54" type="java.lang.Integer"/>
<attribute name="c55" type="java.lang.String"/>
<attribute name="c56" type="java.lang.Double"/>
<attribute name="c57" type="java.lang.Double"/>
<attribute name="c58" type="java.lang.Integer"/>
<attribute name="c59" type="java.lang.Double"/>
<attribute name="c60" type="java.lang.String"/>
<attribute name="c61" type="java.lang.String"/>
<attribute name="c62" type="java.lang.String"/>
<attribute name="c63" type="java.lang.Double"/>
<attribute name="c64" type="java.lang.String"/>
<attribute name="c65" type="java.lang.Integer"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" right="27"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k289" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="8" right="25"/>
<predicate type="equality" left="9" right="24"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="6"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
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
<predicate type="equality" left="0" value="children"/>
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
<attribute name="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
</outputs>
<project>
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
</project>
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
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
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
<predicate type="equality" left="0" value="children"/>
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
<attribute name="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k289" type="java.lang.String"/>
</outputs>
<project>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="code" type="java.lang.Integer"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="ACCESS" relation="YahooPlaceType" access-method="yh_geo_types">
<outputs>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="code" type="java.lang.Integer"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c46" type="java.lang.String"/>
<attribute name="c47" type="java.lang.String"/>
<attribute name="c48" type="java.lang.String"/>
<attribute name="c49" type="java.lang.String"/>
<attribute name="c50" type="java.lang.String"/>
<attribute name="c51" type="java.lang.String"/>
<attribute name="c52" type="java.lang.String"/>
<attribute name="c53" type="java.lang.Integer"/>
<attribute name="c54" type="java.lang.Integer"/>
<attribute name="c55" type="java.lang.String"/>
<attribute name="c56" type="java.lang.Double"/>
<attribute name="c57" type="java.lang.Double"/>
<attribute name="c58" type="java.lang.Integer"/>
<attribute name="c59" type="java.lang.Double"/>
<attribute name="c60" type="java.lang.String"/>
<attribute name="c61" type="java.lang.String"/>
<attribute name="c62" type="java.lang.String"/>
<attribute name="c63" type="java.lang.Double"/>
<attribute name="c64" type="java.lang.String"/>
<attribute name="c65" type="java.lang.Integer"/>
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
<attribute name="condition" type="java.lang.String"/>
<attribute name="code" type="java.lang.Integer"/>
</project>
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
<attribute name="c42" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c42" type="java.lang.Integer"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k289" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="8" right="25"/>
<predicate type="equality" left="9" right="24"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="6"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
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
<predicate type="equality" left="0" value="children"/>
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
<attribute name="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="k272" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k275" type="java.lang.String"/>
<attribute name="k276" type="java.lang.String"/>
<attribute name="k277" type="java.lang.String"/>
<attribute name="k278" type="java.lang.String"/>
<attribute name="k279" type="java.lang.String"/>
<attribute name="k280" type="java.lang.String"/>
<attribute name="k281" type="java.lang.String"/>
<attribute name="k282" type="java.lang.Double"/>
<attribute name="k283" type="java.lang.Double"/>
<attribute name="k284" type="java.lang.Double"/>
<attribute name="k285" type="java.lang.Double"/>
<attribute name="k286" type="java.lang.Double"/>
<attribute name="k287" type="java.lang.Double"/>
<attribute name="k288" type="java.lang.String"/>
</outputs>
<project>
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
</project>
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
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
<attribute name="c41" type="java.lang.Integer"/>
<attribute name="c42" type="java.lang.Integer"/>
<attribute name="c43" type="java.lang.String"/>
<attribute name="c44" type="java.lang.String"/>
<attribute name="c45" type="java.lang.String"/>
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
<predicate type="equality" left="0" value="children"/>
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
<attribute name="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="children" type="java.lang.String"/>
<attribute name="c41" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c41" type="java.lang.Integer"/>
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
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="k274" type="java.lang.String"/>
<attribute name="k273" type="java.lang.Integer"/>
<attribute name="k289" type="java.lang.String"/>
</outputs>
<project>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="code" type="java.lang.Integer"/>
<attribute name="uri" type="java.lang.String"/>
</project>
<child>
<operator type="ACCESS" relation="YahooPlaceType" access-method="yh_geo_types">
<outputs>
<attribute name="placeTypeName" type="java.lang.String"/>
<attribute name="code" type="java.lang.Integer"/>
<attribute name="uri" type="java.lang.String"/>
</outputs>
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
</children>
</operator>
</child>
</operator>
</command>
</plan>
