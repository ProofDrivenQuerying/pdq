package uk.ac.ox.cs.pdq.cost.estimators;

/**
	@author Efthymia Tsamoura
TOCOMMENT: 
	This package contains classes related to plan cost estimation.
	The top level class CostParameters holds the parameters to initiate a cost estimation object. 
	The package estimators contains classes that accept in the input plans and return the cost of these plans. 
	The following types of plan cost estimators are supported:
	-SIMPLE_CONSTANT: Estimates the cost as the sum of the cost of all accesses in a plan, \n where access cost are provided externally	
	-SIMPLE_RANDOM: Estimates the cost as the sum of the cost of all accesses in a plan, \n where cost are assigned randomly
	-SIMPLE_GIVEN: Estimates the cost as the sum of the cost of all accesses in a plan, \n where cost are measured automatically from the underlying datasources	
	-SIMPLE_COUNT: Estimates the cost as the sum of all accesses in a plan	
	-INVERSE_LENGTH Experimental: estimates the cost as the number of atoms in a plan
	-SIMPLE_ERSPI Estimates the cost as the sum of the estimated result size per invocation associated to each access method used in a plan.
	
	The package statistics maintains database level statistics
	The supported statistics are SQL Server 2014 histograms and density vectors.
	The Catalog interface answers statistical queries, like the cardinality of a given column or relation,
	the selectivity of a given filtering predicates or the cost of a specific access.
	The class SimpleCatalog implements the Catalog interface and supports SQL Server 2014 histograms   
	
		
	
**/