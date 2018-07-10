package uk.ac.ox.cs.pdq.regression;

/**
	@author Mark Ridler and Efthymia Tsamoura
	@author gabor
	
	This package contains:
	
	- PDQ.java
		* The entry point for the regression package.
	- RegressionParameters.java
		* Hold the initialConfig of an execution.
<<<<<<< HEAD
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
=======
>>>>>>> branch 'master' of https://github.com/michaelbenedikt/pdq.git

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