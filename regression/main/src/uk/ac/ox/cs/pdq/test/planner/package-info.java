
package uk.ac.ox.cs.pdq.test.planner;

/**
	@author Mark Ridler

	This package contains:
	
	- DAGExplorersTest.java
	 	* Runs regression tests for the optimised explorer. Runs a search with and
	 	* without optimisation (global equivalence, global dominance) and compares the
	 	* resulting plans. An exception is thrown when the plans are different.
	- OptimizationsTest.java
	 	* Runs regression tests for the optimized explorer. Run a search with and
	 	* without optimization (global equivalence, global dominance, post-pruning) and compares the
	 	* resulting plans.
	 	* An exception is thrown when the plans are different.
	- PlannerCostFunctionTest.java
		* Runs the planner with simple and black box cost estimators and compares the resulting plans.
		* An exception is thrown when the planner finds a plan in one case but not the other. 
	- PlannerTest.java
 		* Runs regression tests regarding the planner.
	- PlannerTestUtilities.java
		 * This is an advanced equals method that can return
		 *  - Levels.DIFFERENT in case they have different cost 
		 *  - Levels.IDENTICAL in case the two relationalTerm is the same 
		 *  - Levels.EQUIVALENT in case the two relationalTerm is different but have the same cost. 

**/