package uk.ac.ox.cs.pdq.runtime.exec;

/**
	@author Mark Ridler and Michael Benedikt
	
	This package contains:
	
	- PlanDecorator.java
	 	* The decorator converts RelationalTerm objects to ExecutablePlan objects,
	 	* which are analogous to physical plans.

	Most of the work of executation is done in the contained package 
        runtime.exec.spliterator, which does the actual execution of the Executable plans, 
**/
