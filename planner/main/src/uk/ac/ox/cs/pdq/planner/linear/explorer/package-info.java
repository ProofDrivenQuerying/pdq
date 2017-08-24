package uk.ac.ox.cs.pdq.planner.linear.explorer;

/**
	@author Efthymia Tsamoura

	This package contains classes that explore the space of linear proofs.
	The configurations that are explored are stored in a tree and each configuration maps to a plan. 
	Exploration proceeds roughly as follows:
	In each step of the exploration phase, select a configuration to expand.  
	A partial proof can be expanded if it contains schema facts that are not already exposed, and their input chase constants are accessible. 
	Create a new configuration with facts the facts of the parent configuration \sub the the newly exposed facts.
	Saturate the new configuration using the constraints of the accessible schema. 
	Finally, check if the newly configuration matches the accessible query and update the best configuration appropriately.    
	
	-The LinearGeneric explores the space of linear proofs exhaustively. 
	-The LinearOptimized employs several heuristics to cut down the search space. 
	The first heuristic prunes the configurations that map to plans with cost >= to the best plan found so far.
	The second heuristic prunes the cost dominated configurations. A configuration c and c' is fact dominated by another configuration c' 
	if there exists an homomorphism from the facts of c to the facts of c' and the input constants are preserved.
	A configuration c is cost dominated by c' if it is fact dominated by c and maps to a plan with cost >= the cost of the plan of c'.
	The LinearOptimized class also employs the notion of equivalence in order not to revisit configurations already visited before.
	Both the LinearGeneric and LinearOptimized perform reasoning every time a new node is added to the plan tree. 
	-The LinearKChase class works similarly to the LinearOptimized class.
	However, it does not perform reasoning every time a new node is added to the plan tree but every k steps.  
	

**/
