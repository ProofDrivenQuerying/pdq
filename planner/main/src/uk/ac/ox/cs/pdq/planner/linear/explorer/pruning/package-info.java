package uk.ac.ox.cs.pdq.planner.linear.explorer.pruning;

/**
	@author Efthymia Tsamoura
	This package contains classes that postprune successful node paths. 
	Postpruning a successful path involves removing the nodes that correspond to redundant accesses (accesses that are not necessary to answer the query) or 
	removing redundant exposed candidate facts inside a node (facts that are not necessary to find a query match). 
	Redundant exposed candidates lead to redundant joins when translating a linear proof to a plan.
	
	 
**/