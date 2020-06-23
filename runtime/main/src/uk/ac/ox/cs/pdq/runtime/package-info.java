// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.runtime;

/**
	@author Mark Ridler and Michael Benedikt
	
	This project is concerned with running plans and returning their results. 
	
	Prominent files include:
	-Runtime.java which is the main entry point for the Runtime package

	This also contains the following sub packages:
	
	- runtime.exec
		* The decorator converts RelationalTerm objects to ExecutablePlan objects,
		* which are analogs of physical plans in a traditional  DBMS.
	 	* The decoration is done recursively by passing the decorator to the constructor of
	 	* the executablePlan to make sure it will use the same parameters/access
	 	* repository to decorate the children as well.
	- runtime.exec.spliterator
	        * The root class here is ExecutablePlan.java, which has an execute method
	        * for getting results. Execution is done by recursively executing children.
	        *
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
