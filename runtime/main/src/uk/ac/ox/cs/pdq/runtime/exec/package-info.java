package uk.ac.ox.cs.pdq.runtime.exec;

/**
	@author Mark Ridler
	
	This package contains:
	
	- PlanDecorator.java
	 	* The decorator converts RelationalTerm objects to ExecutablePlan objects. The
	 	* decoration is done recursively by passing the decorator to the constructor of
	 	* the executablePlan to make sure it will use the same parameters/access
	 	* repository to decorate the children as well.

**/