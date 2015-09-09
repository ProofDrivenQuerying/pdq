<?xml version="1.0" encoding="UTF-8"?>
<plan type="linear" cost="6.010018363566668E14">
<command name="T1">
<operator type="PROJECT">
<outputs>
<attribute name="c66" type="java.lang.Integer"/>
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
<operator type="PROJECT">
<outputs>
<attribute name="c67" type="java.lang.Integer"/>
</outputs>
<project>
<attribute name="c67" type="java.lang.Integer"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c66" type="java.lang.Integer"/>
<attribute name="c66" type="java.lang.Integer"/>
<attribute name="c67" type="java.lang.Integer"/>
<attribute name="c68" type="java.lang.String"/>
<attribute name="c69" type="java.lang.String"/>
<attribute name="c70" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c66" type="java.lang.Integer"/>
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
<attribute name="c66" type="java.lang.Integer"/>
<attribute name="c67" type="java.lang.Integer"/>
<attribute name="c68" type="java.lang.String"/>
<attribute name="c69" type="java.lang.String"/>
<attribute name="c70" type="java.lang.String"/>
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
<predicate type="equality" left="0" value="neighbors"/>
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
<attribute name="neighbors" type="java.lang.String"/>
<attribute name="c66" type="java.lang.Integer"/>
</outputs>
<project>
<constant value="neighbors" type="java.lang.String"/>
<attribute name="c66" type="java.lang.Integer"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c66" type="java.lang.Integer"/>
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
</command>
</plan>
