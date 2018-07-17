package uk.ac.ox.cs.pdq.datasources;

/**
	@author Mark Ridler
	
	This package represents the whole concept of Datasources in PDQ as one of the 6 main projects. Everything which includes access
	to the outside world is part of Datasources,
	
	Subpackages include the access repository, the builder (with schema discovery),
  	io.jaxb (and adapted) (with dbIOManager and XMLExecutableAccessMethod), io.xml, legacy services, policies and rest, memory,
  	services, policies, service and servicegroup, sql and utility.
  	
  	Prominent files include:
  	- ExecutableAccessMethod.java which extends the functionality of AccessMethodDescriptor
  	- Pipelineable.java which is a common interface to pipelineable iterators
  	- RelationAccessWrapper.java which provides a wrapper to relations
  	- ResettableIterator.java which provides a resettable iterator
	
 	The sub packages consist of:
 	
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