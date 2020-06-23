// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression;

/**
	@author Mark Ridler and Efthymia Tsamoura
	@author gabor
	
	This package is the entry point for running regression tests.
	There are different types of regression tests:
	-planner tests: sanity checks on different planners. These tests can be found in the planner package.
	-runtime tests: sanity checks on the runtime. These tests can be found in the planner package.
	
	The regression tests require input files:
	- case.properties: parameters specific to a single execution
	- schema.xml: the input schema
	- query.xml: the input query
	- [optional] expected-plan.xml: When exists the generated plan will be compared against it, otherwise PDQ in planner mode creates it.

 	This also contains the following sub packages:
 	
 	-- acceptance, which runs regression tests with a theme of acceptance criteria.
 
 	-- casegenerator, which contains utility methods to create a new test case from scratch or convert an existing regression test
 		 to runtime-compatible test with auto generated access methods using templates.
 
 	-- chasebench, which contains tests for the Chase:
 
 	-- cost.estimators, which contains classes that provide tests for Cost Estimation functions.

	-- planner, which runs regression tests for the optimised explorer. Runs a search with and
	 	 without optimisation (global equivalence, global dominance) and compares the
	 	 resulting plans. An exception is thrown when the plans are different.
 
 	-- runtime, which is commented out pending resolution of the runtime changes.

	Top-level files include:
	- PdqRegression.java which is the entry point for the regression package.
	- RegressionTest.java which runs regression tests.
 	
*/