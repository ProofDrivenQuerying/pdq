package uk.ac.ox.cs.pdq.regression.planner;

/**
	@author Efthymia Tsamoura

	This package contain classes that perform sanity checks on different planners.
	
	The classes require in the input the files
	- case.properties: parameters specific to a single execution
	- schema.xml: the input schema
	- query.xml: the input query
	- expected-plan.xml: this plan with which the output of the planner will be 
	  compared, or if testing the runtime, the plan whose result you which to compare with.
**/