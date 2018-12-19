<?xml version="1.0" encoding="UTF-8"?>
<plan type="linear" cost="1.362306497362875E12">
<command name="T1">
<operator type="PROJECT">
<outputs>
<attribute name="k174" type="java.lang.String"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="k175" type="java.lang.String"/>
<attribute name="k176" type="java.lang.String"/>
<attribute name="k177" type="java.lang.String"/>
<attribute name="k178" type="java.lang.Double"/>
<attribute name="k179" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="France"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBCountries" access-method="c_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
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
<attribute name="k174" type="java.lang.String"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="k175" type="java.lang.String"/>
<attribute name="k176" type="java.lang.String"/>
<attribute name="k177" type="java.lang.String"/>
<attribute name="k178" type="java.lang.Double"/>
<attribute name="k179" type="java.lang.Double"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="c103" type="java.lang.String"/>
<attribute name="c104" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" right="7"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="k174" type="java.lang.String"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="k175" type="java.lang.String"/>
<attribute name="k176" type="java.lang.String"/>
<attribute name="k177" type="java.lang.String"/>
<attribute name="k178" type="java.lang.Double"/>
<attribute name="k179" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="France"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBCountries" access-method="c_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c102" type="java.lang.String"/>
<attribute name="c103" type="java.lang.String"/>
<attribute name="c104" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="country" type="java.lang.String"/>
<attribute name="indicator" type="java.lang.String"/>
<attribute name="value" type="java.lang.Double"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="country" type="java.lang.String"/>
<attribute name="indicator" type="java.lang.String"/>
<attribute name="date" type="java.lang.Integer"/>
<attribute name="countryName" type="java.lang.String"/>
<attribute name="value" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="2000"/>
<predicate type="equality" left="3" value="France"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBData" access-method="wb_c">
<outputs>
<attribute name="country" type="java.lang.String"/>
<attribute name="indicator" type="java.lang.String"/>
<attribute name="date" type="java.lang.Integer"/>
<attribute name="countryName" type="java.lang.String"/>
<attribute name="value" type="java.lang.Double"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c102" type="java.lang.String"/>
</outputs>
<project>
<attribute name="c102" type="java.lang.String"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="k174" type="java.lang.String"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="k175" type="java.lang.String"/>
<attribute name="k176" type="java.lang.String"/>
<attribute name="k177" type="java.lang.String"/>
<attribute name="k178" type="java.lang.Double"/>
<attribute name="k179" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="France"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBCountries" access-method="c_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
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
</children>
</operator>
</command>
<command name="T3">
<operator type="PROJECT">
<outputs>
<attribute name="c102" type="java.lang.String"/>
<attribute name="c104" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="c102" type="java.lang.String"/>
<attribute name="c104" type="java.lang.Double"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="k174" type="java.lang.String"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="k175" type="java.lang.String"/>
<attribute name="k176" type="java.lang.String"/>
<attribute name="k177" type="java.lang.String"/>
<attribute name="k178" type="java.lang.Double"/>
<attribute name="k179" type="java.lang.Double"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="c103" type="java.lang.String"/>
<attribute name="c104" type="java.lang.Double"/>
<attribute name="c103" type="java.lang.String"/>
<attribute name="c105" type="java.lang.Integer"/>
<attribute name="c106" type="java.lang.Integer"/>
<attribute name="c107" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="8" right="10"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="k174" type="java.lang.String"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="k175" type="java.lang.String"/>
<attribute name="k176" type="java.lang.String"/>
<attribute name="k177" type="java.lang.String"/>
<attribute name="k178" type="java.lang.Double"/>
<attribute name="k179" type="java.lang.Double"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="c103" type="java.lang.String"/>
<attribute name="c104" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" right="7"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="k174" type="java.lang.String"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="k175" type="java.lang.String"/>
<attribute name="k176" type="java.lang.String"/>
<attribute name="k177" type="java.lang.String"/>
<attribute name="k178" type="java.lang.Double"/>
<attribute name="k179" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="France"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBCountries" access-method="c_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c102" type="java.lang.String"/>
<attribute name="c103" type="java.lang.String"/>
<attribute name="c104" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="country" type="java.lang.String"/>
<attribute name="indicator" type="java.lang.String"/>
<attribute name="value" type="java.lang.Double"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="country" type="java.lang.String"/>
<attribute name="indicator" type="java.lang.String"/>
<attribute name="date" type="java.lang.Integer"/>
<attribute name="countryName" type="java.lang.String"/>
<attribute name="value" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="2000"/>
<predicate type="equality" left="3" value="France"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBData" access-method="wb_c">
<outputs>
<attribute name="country" type="java.lang.String"/>
<attribute name="indicator" type="java.lang.String"/>
<attribute name="date" type="java.lang.Integer"/>
<attribute name="countryName" type="java.lang.String"/>
<attribute name="value" type="java.lang.Double"/>
</outputs>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c102" type="java.lang.String"/>
</outputs>
<project>
<attribute name="c102" type="java.lang.String"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="k174" type="java.lang.String"/>
<attribute name="c102" type="java.lang.String"/>
<attribute name="k175" type="java.lang.String"/>
<attribute name="k176" type="java.lang.String"/>
<attribute name="k177" type="java.lang.String"/>
<attribute name="k178" type="java.lang.Double"/>
<attribute name="k179" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="France"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBCountries" access-method="c_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
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
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c103" type="java.lang.String"/>
<attribute name="c105" type="java.lang.Integer"/>
<attribute name="c106" type="java.lang.Integer"/>
<attribute name="c107" type="java.lang.String"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="topicId" type="java.lang.Integer"/>
<attribute name="sourceId" type="java.lang.Integer"/>
<attribute name="sourceNote" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="topicId" type="java.lang.Integer"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="sourceId" type="java.lang.Integer"/>
<attribute name="sourceNote" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" value="Population (Total)"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBIndicators" access-method="i_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="topicId" type="java.lang.Integer"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="sourceId" type="java.lang.Integer"/>
<attribute name="sourceNote" type="java.lang.String"/>
</outputs>
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
