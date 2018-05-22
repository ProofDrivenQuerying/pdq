package uk.ac.ox.cs.pdq.regression;

/**
	@author Efthymia Tsamoura

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