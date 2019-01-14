package uk.ac.ox.cs.pdq.datasources.io.jaxb;

/**
	@author Mark Ridler and Michael Benedikt
	
	This package deals with input and output of datasource metadata, based on
	an XML format for describing source properties
	
	- DbIOManager.java
 		* Reads a Schema that contains external (database) sources, such as: <code>
 		* <source name="tpch" discoverer="uk.ac.ox.cs.pdq.sql.PostgresqlSchemaDiscoverer" 
 		* 		driver="org.postgresql.Driver" 
 		* 		url="jdbc:postgresql://localhost/" 
 		* 		database="tpch_0001" username="root" password="root" />
 		* </code>
 	- Source.java
 		* Represents the sources tag in the schema.xml
 	- XmlAttribute.java
 	 	* Represents an AccessMethod attribute with name, type, mapping to relation's attribute and wheather it is an input or not.
	- XmlExecutableAccessMethod.java
 		* This class represents all executable access methods as xml files. Its main
 		* purpose is to have all necessary parameters to be able to create a
 		* DbAccessMethod, RestAccessMethod or InMemoryAccessMethod.

**/