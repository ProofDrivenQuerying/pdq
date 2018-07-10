package uk.ac.ox.cs.pdq.regression;

/**
	@author Mark Ridler and Efthymia Tsamoura
	
	This package contains:
	
	- Bootstrap.java
		* The entry point for the regression package.
	- PlanConverter.java
		* Hard-wired to run on Gabor's machine
	- RegressionParameters.java
		* Hold the initialConfig of an execution.
	- RegressionTest.java
		* Runs regression tests.
	- RegressionTestException.java
 		* Exception that occurred during a regression test.
 	
 	This also contains the following packages:
 	
 	- regression.acceptance
		* This package runs regression tests with a theme of acceptance criteria.
 	- regression.casegenerator
 		* Contains utility methods to create a new test case from scratch or convert an existing regression test
 		* to runtime-compatible test with auto generated access methods using templates.
 	- regression.chasebench
		* This package contains tests for the Chase:
 	- regression.cost.estimators
 		* This package contains classes that provide tests for Cost Estimation functions.
	- regression.planner
	 	* Runs regression tests for the optimised explorer. Runs a search with and
	 	* without optimisation (global equivalence, global dominance) and compares the
	 	* resulting plans. An exception is thrown when the plans are different.
 	- regression.runtime
		* This package is commented out pending resolution of the runtime changes.

	This package is the entry point for running regression tests.
	There are different types of regression tests:
	-planner tests: sanity checks on different planners. These tests can be found in the planner package.
	-runtime tests: sanity checks on the runtime. These tests can be found in the planner package.
	-reasoning tests: sanity checks on the reasoners. These tests can be found in the reasoning package.
	-cost tests: sanity checks on the cost functions. These tests can be found in the cost package.
	
	The first two types of regression tests require in the input the files
	- case.properties: parameters specific to a single execution
	- schema.xml: the input schema
	- query.xml: the input query
	- expected-plan.xml: this plan with which the output of the planner will be compared, or if testing the runtime, the plan whose result you which to compare with.
	  The other regression tests do not require any input to run.
**/