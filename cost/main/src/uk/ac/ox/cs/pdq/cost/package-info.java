package uk.ac.ox.cs.pdq.cost;

/**
	@author Efthymia Tsamoura and Mark Ridler

	This package contains classes related to plan cost estimation. By separating it out in this way, cost becomes
	a fully independent package rather than being mixed in with one or more of the others.
	
	The following sub-packages are included:
	
	-- Estimators, which contains classes that accept in the input plans and return the cost of these plans. 
	    The following types of plan cost estimators are supported:
	    -SIMPLE_CONSTANT: Estimates the cost as the sum of the cost of all accesses in a plan, \n where access cost are provided externally	
	    -SIMPLE_RANDOM: Estimates the cost as the sum of the cost of all accesses in a plan, \n where cost are assigned randomly
	    -SIMPLE_GIVEN: Estimates the cost as the sum of the cost of all accesses in a plan, \n where cost are measured automatically from the underlying datasources	
	    -SIMPLE_COUNT: Estimates the cost as the sum of all accesses in a plan	
	    -BLACKBOX: Estimates the cost through some externally defined cost function.\nCurrently, this defaults to the white box cost functions relying on textbox cost estimation techniques	
	    -BLACKBOX_DB Estimates the cost by translating the query to SQL and asking its cost to a DBMS. The current implementation supports Postgres 
	    -INVERSE_LENGTH: Experimental: estimates the cost as the number of atoms in a plan
	    -SIMPLE_ERSPI Estimates the cost as the sum of the estimated result size per invocation associated to each access method used in a plan.
	
	-- Statistics, which defines database level statistics.
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
	   
	-- Io.jaxb implements JAXB-based XML parsing for cost objects
	
	-- Logging provides the single class CostStatKeys.java, which used in planning statistics collections.
	
	
	The top level classes are as follows:
	- CostParameters holds the parameters to initiate a cost estimation object.
	- CostEstimatorFactory is a factory for cost estimation objects.
	- Cost is an abstract class which defines the interface for Cost subclasses
	- DoubleCost implements a double-valued cost object
		
**/