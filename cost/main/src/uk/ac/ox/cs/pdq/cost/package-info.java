// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost;

/**
	@author Efthymia Tsamoura and Mark Ridler
	@Contributor Brandon Moore

	This package contains classes related to plan cost estimation. By separating it out in this way, cost becomes
	a fully independent package rather than being mixed in with one or more of the others.
	
	The following sub-packages are included:
	
	-- Estimators, which contains classes that accept in the input plans and return the cost of these plans. 
	 method used in a plan.
	
	-- Statistics, which defines database level statistics.
    	The Catalog interface answers statistical queries, like the cardinality of a given column or relation,
    	the selectivity of a given filtering predicates or the cost of a specific access.
	    The class SimpleCatalog implements the Catalog interface.
    	The statistics that are maintained are:
	    -the relation cardinalities,
	    -cardinalities of single attributes
	    -the size of output per invocation of an access method
	    -the cost of an access method
	   All the statistics are loaded by default from a catalog.properties file
	   
	-- Io.jaxb implements JAXB-based XML parsing for cost objects
	
	
	
	The top level classes are as follows:
	- CostParameters holds the parameters to initiate a cost estimation object.
	- CostEstimatorFactory is a factory for cost estimation objects.
	- Cost is an abstract class which defines the interface for Cost subclasses
	- DoubleCost implements a double-valued cost object
		
**/
