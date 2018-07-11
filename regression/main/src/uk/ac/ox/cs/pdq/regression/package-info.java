package uk.ac.ox.cs.pdq.regression;

/**
	@author Mark Ridler and Efthymia Tsamoura
	@author gabor
	
	This package contains:
	
	- PDQ.java
		* The entry point for the regression package.
	- RegressionParameters.java
		* Hold the initialConfig of an execution.

	This package is the entry point for running regression tests.
	There are different types of regression tests:
	-planner tests: sanity checks on different planners. These tests can be found in the planner package.
	-runtime tests: sanity checks on the runtime. These tests can be found in the planner package.
	
	The regression tests require input files:
	- case.properties: parameters specific to a single execution
	- schema.xml: the input schema
	- query.xml: the input query
	- [optional] expected-plan.xml: When exists the generated plan will be compared against it, otherwise PDQ in planner mode creates it.
**/