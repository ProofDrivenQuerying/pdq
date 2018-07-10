package uk.ac.ox.cs.pdq.datasources;

/**
	@author Mark Ridler
	
	This package contains:
	
	- AccessException.java
 		* Access exception implementation .
	- ExecutableAccessMethod.java
 		* This class extends the functionality of an AccessMethodDescriptor used in
 		* common with attribute mapping. Different accessMethod types such as database
 		* or webservice access methods can have different set of attributes, mapping of
 		* such attributes to the relation's attributes happens here.
	- Pipelineable.java
 		* Common interface to pipelinable iterators, i.e. iterators that iterate over
 		* a set of input tuples, and return output tuple one at a time.
	- RelationAccessWrapper.java
 		* I understqnd that there are two views of database objects reflected in the code in common. 
 		* On is the traditional where we don't have access restrictions, hence we have normal relations etc.
 		* The other is the "access restrictions" perspective and this is why this object exists. Is this the case? 
 		* By putting this in a package called wrappers and naming it a wrapper you don't do justice to it if it's the main
 		* "access restriction perspective" object.
 		* 
 		* The Wrapper interface provide access functions.
	- ResettableIterator.java
 		* An iterator that can be reset, i.e. the cursor can be placed back to the
 		* beginning of the underlying Iterable at any time.
	- ResettableTranslatingIterator.java
 		* An iterator that can be reset, i.e. the cursor can be placed back to the
 		* beginning of the underlying Iterable at any time.
	- TranslatingIterator.java
 		* An iterator consuming another iterator.
 	
 	The other packages consist of:
 	
 	- datasources.accessrepository
 		* It maintains a repository executable access methods. The access methods are defined in xml descriptors in a folder.
 	- datasources.builder 
 		* Builder are typically use to instantiate objects that are too complex to
 		* initialise with a single constructor calls, e.g. if many fields are
 		* mandatory and many consistency checks are required.
 	- datasources.io.jaxb
 		* Represents all executable access methods as xml files. Its main
 		* purpose is to have all necessary parameters to be able to create a
 		* DbAccessMethod, RestAccessMethod or InMemoryAccessMethod.
 	- datasources.io.jaxb.adapted
 		* This is a JAXB Xml parsing file complete with annotations. It breaks things down
		* into  sources, name, relations, description, dependencies and ars;
	- datasources.io.xml
		* Reads experiment sample elements from XML.
		* Mostly unused, outdated by jaxb IO implementation.
		* Gather all the qualified names appearing in a schema file 
 		* query file or plan file
	- datasources.memory
	 	* Represents an in-memory version of an executable access method from which it is
 		* derived.
 	- datasources.services
 		* A collection of classes for implementing RESTExecutableAccessMethod by parsing xml files
 		* and utilising UsagePolicy and RESTRequest/ResponseEvent.
 	- datasources.services.policies
 		* A collection of UsagePolicies
 	- datasources.services.service
 		* A collection of files for parsing the xml Service definition
 	- datasources.services.servicegroup
		* A collection of files for parsing the xml Service Group definition
	- datasources.sql
		* This package contains classes that build DBMS wrappers, i.e., methods that access data from different DBMSs.
		* The current implementation supports accessing data from MySQL and Postgres databases.
	- datasources.utility
		* A collection of utility classes for BooleanResult, FormulaEquivalance, SchemaConverter etc



**/