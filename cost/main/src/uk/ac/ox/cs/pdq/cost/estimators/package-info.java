// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.estimators;

/**
	@author Efthymia Tsamoura
	@Contributed Brandon Moore
	This package contains classes related to plan cost estimation.
	The top level class CostParameters holds the parameters to initiate a cost estimation object. 
	The package estimators contains classes that accept in the input plans and return the cost of these plans. 
	The following types of plan cost estimators are supported:

	-FIXED_COST_PER_ACCESS: Estimates the cost as the sum of the cost of all accesses in a plan, where access cost are provided externally
		@see uk.ac.ox.cs.pdq.cost.estimators.FixedCostPerAccessCostEstimator
	-COUNT_NUMBER_OF_ACCESSED_RELATIONS: Estimates the cost as the sum of all accesses in a plan
		@see uk.ac.ox.cs.pdq.cost.estimators.CountNumberOfAccessedRelationsCostEstimator
	-TEXTBOOK: Estimates the cost through some externally defined cost function. Currently, this defaults to the white box cost functions relying on textbox cost estimation techniques
		@see uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator
	-LENGTH: estimates the cost as the number of accesses in a plan
		@see uk.ac.ox.cs.pdq.cost.estimators.LengthBasedCostEstimator
	-NUMBER_OF_OUTPUT_TUPLES_PER_ACCESS: Estimates the cost as the sum of the estimated result size per invocation associated to each access method used in a plan
		@see uk.ac.ox.cs.pdq.cost.estimators.TotalNumberOfOutputTuplesPerAccessCostEstimator
	-BLACKBOX_DB Estimates the cost by translating the query to SQL and asking its cost to a database
		@see uk.ac.ox.cs.pdq.cost.estimators.QueryExplainCostEstimator
	
	The package statistics maintains database level statistics
	The Catalog interface answers statistical queries, like the cardinality of a given column or relation,
	the cost of a specific access.
	The class SimpleCatalog implements the Catalog interface 
**/
