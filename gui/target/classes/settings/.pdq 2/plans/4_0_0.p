<?xml version="1.0" encoding="UTF-8"?>
<plan type="linear" cost="4.025E7">
<command name="T1">
<operator type="PROJECT">
<outputs>
<attribute name="c108" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="c108" type="java.lang.Double"/>
</project>
<child>
<operator type="PROJECT">
<outputs>
<attribute name="c108" type="java.lang.Double"/>
<attribute name="c109" type="java.lang.Double"/>
</outputs>
<project>
<attribute name="elevation" type="java.lang.Double"/>
<attribute name="resolution" type="java.lang.Double"/>
</project>
<child>
<operator type="SELECT">
<outputs>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
<attribute name="elevation" type="java.lang.Double"/>
<attribute name="resolution" type="java.lang.Double"/>
</outputs>
<conjunction>
<predicate type="equality" left="0" value="40.714728"/>
<predicate type="equality" left="1" value="-73.998672"/>
</conjunction>
<child>
<operator type="ACCESS" relation="GoogleElevation" access-method="by_location">
<outputs>
<attribute name="latitude" type="java.lang.Double"/>
<attribute name="longitude" type="java.lang.Double"/>
<attribute name="elevation" type="java.lang.Double"/>
<attribute name="resolution" type="java.lang.Double"/>
</outputs>
<child>
<operator type="STATIC_INPUT">
<outputs>
<attribute name="40.714728" type="java.lang.Double"/>
<attribute name="-73.998672" type="java.lang.Double"/>
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
