<?xml version="1.0" encoding="UTF-8"?>
<plan type="linear" cost="9.35184778480226E14">
<command name="T1">
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" value="Low income"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBIncomeLevels" access-method="il_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
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
<attribute name="c95" type="java.lang.String"/>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" value="Low income"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBIncomeLevels" access-method="il_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" right="9"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
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
<operator type="PROJECT">
<outputs>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
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
</children>
</operator>
</children>
</operator>
</command>
<command name="T3">
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c89" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="c91" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" right="17"/>
<predicate type="equality" left="11" right="19"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" value="Low income"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBIncomeLevels" access-method="il_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" right="9"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
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
<operator type="PROJECT">
<outputs>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
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
</children>
</operator>
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c89" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="c91" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="country" type="java.lang.String"/>
<attribute name="indicator" type="java.lang.String"/>
<attribute name="countryName" type="java.lang.String"/>
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
<predicate type="equality" left="2" value="2010"/>
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
<attribute name="c88" type="java.lang.String"/>
</outputs>
<project>
<attribute name="c88" type="java.lang.String"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" value="Low income"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBIncomeLevels" access-method="il_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" right="9"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
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
<operator type="PROJECT">
<outputs>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
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
</children>
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
<attribute name="c90" type="java.lang.String"/>
<attribute name="c91" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="c90" type="java.lang.String"/>
<attribute name="c91" type="java.lang.Double"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c89" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="c91" type="java.lang.Double"/>
<attribute name="c89" type="java.lang.String"/>
<attribute name="c92" type="java.lang.Integer"/>
<attribute name="c93" type="java.lang.Integer"/>
<attribute name="c94" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="18" right="21"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c89" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="c91" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="2" right="17"/>
<predicate type="equality" left="11" right="19"/>
</conjunction>
<children>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" value="Low income"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBIncomeLevels" access-method="il_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" right="9"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
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
<operator type="PROJECT">
<outputs>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
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
</children>
</operator>
</children>
</operator>
<operator type="PROJECT">
<outputs>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c89" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="c91" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="country" type="java.lang.String"/>
<attribute name="indicator" type="java.lang.String"/>
<attribute name="countryName" type="java.lang.String"/>
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
<predicate type="equality" left="2" value="2010"/>
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
<attribute name="c88" type="java.lang.String"/>
</outputs>
<project>
<attribute name="c88" type="java.lang.String"/>
</project>
<child>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" right="1"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" value="Low income"/>
</conjunction>
<child>
<operator type="ACCESS" relation="WBIncomeLevels" access-method="il_free">
<outputs>
<attribute name="id" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
</outputs>
</operator>
</child>
</operator>
</child>
</operator>
<operator type="JOIN" variant="SYMMETRIC_HASH">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="1" right="9"/>
</conjunction>
<children>
<operator type="PROJECT">
<outputs>
<attribute name="c95" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c96" type="java.lang.String"/>
<attribute name="c97" type="java.lang.String"/>
<attribute name="c98" type="java.lang.String"/>
<attribute name="c99" type="java.lang.String"/>
<attribute name="c100" type="java.lang.Double"/>
<attribute name="c101" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
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
<operator type="PROJECT">
<outputs>
<attribute name="k201" type="java.lang.String"/>
<attribute name="c88" type="java.lang.String"/>
<attribute name="c90" type="java.lang.String"/>
<attribute name="k202" type="java.lang.String"/>
<attribute name="k203" type="java.lang.String"/>
<attribute name="k204" type="java.lang.String"/>
<attribute name="k205" type="java.lang.Double"/>
<attribute name="k206" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="id" type="java.lang.String"/>
<attribute name="iso2" type="java.lang.String"/>
<attribute name="name" type="java.lang.String"/>
<attribute name="incomeLevel" type="java.lang.String"/>
<attribute name="lendingType" type="java.lang.String"/>
<attribute name="capitalCity" type="java.lang.String"/>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
</project>
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
</children>
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
<attribute name="c89" type="java.lang.String"/>
<attribute name="c92" type="java.lang.Integer"/>
<attribute name="c93" type="java.lang.Integer"/>
<attribute name="c94" type="java.lang.String"/>
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
<predicate type="equality" left="2" value="Population living in areas where elevation is below 5 meters (% of total population)"/>
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
