// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.schemabuilder;

/**
	@author Mark Ridle and Michael Benedikt.
	
	This package relates to creating java objects representing datasource schemas, based on some 
	external information (e.g. a DBMS catalog). It contains:
	
	- Builder.java
 		* Interface common to builder class.
 		* Builders are typically used to instantiate objects that are too complex to
 		* initialise with a single constructor call, e.g. if many fields are
 		* mandatory and many consistency checks are required.
 		* The builder class works as a proxy, receiving all necessary initialisations
 		* on behalf to the object to be created.
 		* The final object is actually instantiated upon a call to the build() method.
 	- BuilderException.java
 	 	* Exception that occurs during a building operation.
 	- SchemaDiscoverer.java
 		* Common interface to all schema discoverers.
 		* Creates a schema based on a database instance. This has to be implemented for each database manager like postgres, mysql and so on.

**/