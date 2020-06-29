// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.statistics;

/**
	@author Efthymia Tsamoura
	
	This package defines database level statistics.
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
	
**/