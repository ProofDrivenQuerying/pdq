// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.io.jaxb;

/**
	@author Mark Ridler
	
	The cost.io.jaxb sub-package contains a single file:
	
	- CostIOManager.java, which can read and write a relationalTerm object that contains a cost. This cost is
	   the full cost of the plan represented in the given RelationalTerm.
	
	CostIOManager extends IOManager from the common library.
	- It reads a cost from a plan file given a schema.
	- It reads a relational term from a plan file given a schema.
	- It writes a relational term and cost.
	All 3 functions use the JAXB unmarshalling or marshalling methods.
	

		
		
	
**/