package uk.ac.ox.cs.pdq.sql;

/**
	@author Efthymia Tsamoura

	This package contains classes that build DBMS wrappers, i.e., methods that access data from different DBMSs.
	The current implementation supports accessing data from MySQL and Postgres databases.
	
	The AbstractSQLSchemaDiscoverer class reads a DBMS schema and loads it into main memory in uk.ac.ox.cs.pdq.db.Schema format. 
	
**/