package uk.ac.ox.cs.pdq.datasources.schemabuilder;

/**
	@author Mark Ridler
	
	This package contains:
	
	- Builder.java
 		* Interface common to builder class.
 		* Builder are typically use to instantiate objects that are too complex to
 		* initialise with a single constructor calls, e.g. if many fields are
 		* mandatory and many consistency checks are required.
 		* The builder class works as a proxy, receiving all necessary initialisations
 		* on behalf to the object to be created.
 		* The final object is actually instantiated upon a call to the build() method.
 	- BuilderException.java
 	 	* Exception that occurs during a building operation.
 	- SchemaDiscoverer.java
 		* Common interface to all schema discoverers.
 		* Creates a schema based on a database instance. Have to be implemented for each database manager like postgres, mysql and so on.

**/