package uk.ac.ox.cs.pdq.planner;

/**
	@author Efthymia Tsamoura

	This package contains classes that initiate and start the plan exploration process.  
	The initiation process consists of the following steps:
		-selection of the appropriate reasoning tool
		-selection of the appropriate plan exploration algorithm 
		-selection of the appropriate cost function that will evaluate the performance of the created plans
		
	The algorithms find plans by exploring the space of proofs.
	The proofs are built up compositionally during each step of the exploration process.
	Each proof is translated to a plan, which is later delegated to a cost estimation module.
	We assume that the cost functions are monotonic, e.g., the more accesses we add to a plan, the higher its cost becomes.  	
	 	
	The package structure is the following one:
	The linear.* packages contain classes related to creating and exploring linear proofs
	The dag.* packages contain classes related to creating and exploring dag proofs
	The reasoning.* packages contain proof related classes, e.g., classes that represent the state of a chase configuration.  
	The util package contains utility classes and methods.
	The logging.* packages contain classes that monitor certain characteristics, like the number of rounds, 
	the planning time. 
	
**/