package uk.ac.ox.cs.pdq.planner.explorer;

/**
	@author Efthymia Tsamoura

	This package contains the top level interface for the proof explorers.
	The proofs are built up compositionally during each step of the exploration process.
	Each proof is translated to a plan, which is later delegated to a cost estimation module.
	We assume that the cost functions are monotonic, e.g., the more accesses we add to a plan, the higher its cost becomes.  
	Two types of proofs/plans are explored: linear and bushy ones. The following types of explorers are available:
	
	-The LinearGeneric class explores the space of linear proofs exhaustively. 
	-The LinearOptimized class employs several heuristics to cut down the search space. 
	The first heuristic prunes the configurations that map to plans with cost >= to the best plan found so far.
	The second heuristic prunes the cost dominated configurations.
	A configuration c and c' is fact dominated by another configuration c' 
	if there exists an homomorphism from the facts of c to the facts of c' and the input constants are preserved.
	A configuration c is cost dominated by c' if it is fact dominated by c and maps to a plan with cost >= the cost of the plan of c'.
	The LinearOptimized class also employs the notion of equivalence in order not to revisit configurations already visited before.
	Both the LinearGeneric and LinearOptimized perform reasoning every time a new node is added to the plan tree. 
	-The LinearKChase class works similarly to the LinearOptimized class.
	However, it does not perform reasoning every time a new node is added to the plan tree but every k steps.  

	-The DAGGeneric class explores the space of proofs exhaustively.
	-The DAGOptimized, DAGSimpleDP and DAGChaseFriendlyDP employ two DP-like heuristics to cut down the search space.
	The first heuristic prunes the configurations that map to plans with cost >= to the best plan found so far.
	The second heuristic prunes the cost dominated configurations. A configuration c and c' is fact dominated by another configuration c' 
	if there exists an homomorphism from the facts of c to the facts of c' and the input constants are preserved.
	A configuration c is cost dominated by c' if it is fact dominated by c and maps to a plan with cost >= the cost of the plan of c'.
	-The DAGOptimized class employs further techniques to speed up the planning process like reasoning in parallel and re-use of reasoning results. 

**/