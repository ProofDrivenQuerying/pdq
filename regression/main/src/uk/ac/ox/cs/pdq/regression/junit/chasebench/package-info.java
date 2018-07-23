package uk.ac.ox.cs.pdq.regression.junit.chasebench;

/**
	@author Mark Ridler

	This package contains tests for the Chase:
	- CommonToPDQTranslator.java 
		* Parses tables, headers, footers, atoms, equality, dependency and query
		* Imports facts from a csv file
	- Deep.java
		* Opens a doctors internal db, creates a schema, gets test facts
		* Gets test queries
	- Doctors.java
		* Opens internal or external doctors db, creates a schema, gets test facts
		* Gets test queries
	- LUBM.java
		* Opens internal doctors db, creates a schema, gets test facts
		* Gets test queries
	- Ontology256.java
		* Opens internal doctors db, creates a schema, gets test facts
		* Gets test queries
	- STB128.java
		* Opens internal doctors db, creates a schema, gets test facts
		* Gets test queries
	- TestMainInternal.java
		* tgds, tgds5, tgdsEgds, tgdsEgdsLarge, importFacts, weak, convertShemaFiles
	- TestMainProgress.java
		* tgds, tgds5, tgdsEgds, tgdsEgdsLarge, importFacts, weak, convertShemaFiles
**/