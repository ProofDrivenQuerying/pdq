package uk.ac.ox.cs.pdq.regression;

/**
	@author Mark Ridler and Efthymia Tsamoura
	@author gabor and Michael Benedikt
	
	This package is the entry point for running regression tests.
	There are different types of regression tests:
	-planner tests: sanity checks on different planners. These tests can be found in the planner package.
	-runtime tests: sanity checks on the runtime. These tests can be found in the planner package.
	
	The regression tests require input files:
	- case.properties: parameters specific to a single execution
	- schema.xml: the input schema
	- query.xml: the input query
	- [optional] expected-plan.xml: When exists the generated plan will be compared against it, otherwise PDQ in planner mode creates it.

 	This package also contains the following sub packages:
 	
 	-- acceptance, which gives different kinds of acceptance criteria for tests
 
 
 	-- chasebench, which contains tests for the Chase:
 
 	-- cost.estimators, which contains classes that provide tests for Cost Estimation functions.


 	
*/
