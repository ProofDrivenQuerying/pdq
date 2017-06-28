package uk.ac.ox.cs.pdq.datasources.sql;

/**
	@author Efthymia Tsamoura

	This package contains classes that build DBMS wrappers, i.e., methods that access data from different DBMSs.
	The current implementation supports accessing data from MySQL and Postgres databases.
	The top-level DBMS interfaces are SQLRelationWrapper and SQLViewWrapper.
	The top-level wrapper interface is uk.ac.ox.cs.pdq.db.wrappers.RelationAccessWrapper.
	
	The AbstractSQLSchemaDiscoverer class reads a DBMS schema and loads it into main memory in uk.ac.ox.cs.pdq.db.Schema format. 
	
**/