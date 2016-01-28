package uk.ac.ox.cs.pdq.cost.statistics;

/**
	@author Efthymia Tsamoura
	
	This package defines database level statistics.
	The supported statistics are SQL Server 2014 histograms and density vectors.
	The Catalog interface answers statistical queries, like the cardinality of a given column or relation,
	the selectivity of a given filtering predicates or the cost of a specific access.
	The class SimpleCatalog implements the Catalog interface.
	The statistics that are maintained are:
	-the relation cardinalities,
	-cardinalities of single attributes
	-the size of output per invocation of an access method
	-the cost of an access method
	-selectivities of single attribute filtering predicates
	-frequency maps of single attributes
	-and SQL Server 2014 single attribute histograms.
	 All the statistics are loaded by default from a catalog.properties file
	
		
	
**/