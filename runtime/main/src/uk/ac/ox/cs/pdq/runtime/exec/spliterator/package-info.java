package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

/**
	@author Mark Ridler
	
	This package contains:
	
	- Access.java
 		* An executable access plan. 
 		* 
 		* Note that the child field in an {@code Access} instance is not used, but 
 		* instead has an underlying {@code AccessMethod}. 
	- BinaryExecutablePlan.java
 		* Base class for executable plans having two children.
	- CartesianProduct.java
 		* An executable Cartesian product plan. 
	- DependentJoin.java
		* Algorithm:
		* - advance to the next right tuple; if the right child is exhausted:
		* 		- update the cache of matching tuples from the right child
		* 		- set the flag to use the cache of accessed right tuples
		* 		- advance to the next left tuple and reset the right child spliterator
		* - Add the right tuple to the cache of accessed right tuples (unless that cache is already in use) 
		* - Prepend the current leftTuple to the current rightTuple
		* - Test the join condition on the joined tuple
		* 		- recursively call this method if the condition is not satisfied -- Gabor: I changed this to use a do-while loop instead of recursively calling itself, since there is no way of knowing how deep that recursion would go ( worst case scenario is that you call itself as many times as many tuples are created from a cartesian product, causing stack overflow )  
		* - Pass the joined tuple to the given action & return true
	- ExecutablePlan.java
 		* An executable query plan. A logical plan is decorated to make it executable. 
	- NestedLoopJoin.java
 		* An executable plan implementing the nested loop join algorithm. 
	- Projection.java
 		* An executable projection plan. 
	- Selection.java
 		* An executable selection plan. 
	- SymmetricMemoryHashJoin.java
 		* An executable plan implementing the symmetric memory hash join algorithm. 
	- UnaryExecutablePlan.java
 		* Base class for executable plans having a single child.
	- UnaryPlanSpliterator.java
		* Implements constructor, trySplit, estimateSize and characteristics 
	
**/