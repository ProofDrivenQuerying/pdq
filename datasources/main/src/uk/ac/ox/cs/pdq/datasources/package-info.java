package uk.ac.ox.cs.pdq.datasources;

/**
	@author Michael Benedikt 
	
This package represents interfaces for getting external data that is to be integrated.
Ways of getting data include REST services, SQL access to databases, and data available

The package includes:
--  internal representation of metadata about external data,
--  methods for calling the external sources, which are used as a base step
in the plan language
-- tools for creating these objects from XML spefications of services
--  tools for reading these datasource objects from external repositories (currently,
from SQL-based databases).

Two XML-based specifications are maintained, the current one, which
is fully operatioinal; a legacy one, which can be parsed but
the resulting parsed files cannot be used; it is useful only for creating converters
to the current format.
	
	Subpackages include 

 --- the access repository, which represents a directory of services

--schema builder, which allows one to read in external schemas (currently from a DBMS)

-- io.jaxb, for reading specifications in the current format;

-- io.xml for reading specifications in the old format

-- legacy services, givng the java object representation of the legacy REST metadata specifications 

-- services, giving the java object representation of the current REST services -- metadata
nad access

-- SQL, giving the java object representations of SQL-based datasources

-- memory, giving the java object representation of acces to main memory data

-- utility classes
  	
	 The main top-level files in the package 
	is  ExecutableAccessMethod.java
 		* This class extends the functionality of an AccessMethodDescriptor (an
 		* abstract descriptioin of the service), extending it by including the
 		* ability to execute the access. An ExecutableAccessMethod is described
 		* as a function with some input attributes, while an AccessMethodDescriptor is described
 		* as giving access to a relation on some input positions. It is thus
 		* necessary to map between attributes of the ExecutableAccessMethod
 		* and positions of the corresponding relation, and this class includes
 		* that mpaping.



**/
