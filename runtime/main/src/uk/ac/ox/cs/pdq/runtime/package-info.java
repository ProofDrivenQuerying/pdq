package uk.ac.ox.cs.pdq.runtime;

/**
	@author Mark Ridler
	
	This package contains:
	
	- Runtime.java
 		*  Decorates a plan, and executes queries or the plan itself. 
	- RuntimeParameters.java
 		* Hold the configuration of a runtime execution.

	This also contains the following packages:
	
	- runtime.exec
		* The decorator converts RelationalTerm objects to ExecutablePlan objects. The
	 	* decoration is done recursively by passing the decorator to the constructor of
	 	* the executablePlan to make sure it will use the same parameters/access
	 	* repository to decorate the children as well.
	- runtime.exec.spliterator
		* Algorithm:
		* - advance to the next right tuple; if the right child is exhausted:
		* 		- update the cache of matching tuples from the right child
		* 		- set the flag to use the cache of accessed right tuples
		* 		- advance to the next left tuple and reset the right child spliterator
		* - Add the right tuple to the cache of accessed right tuples (unless that cache is already in use) 
		* - Prepend the current leftTuple to the current rightTuple
		* - Test the join condition on the joined tuple
		* 		- recursively call this method if the condition is not satisfied
		* - Pass the joined tuple to the given action & return true
	
**/