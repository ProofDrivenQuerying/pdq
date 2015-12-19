package uk.ac.ox.cs.pdq.planner.dag.explorer;

/**
	@author Efthymia Tsamoura

	This package contains classes that explore the space of DAG proofs.
	The DAGGeneric class explores the space of proofs exhaustively.
	The DAGOptimized, DAGSimpleDP and DAGChaseFriendlyDP employ two DP-like heuristics to cut down the search space.
	The first heuristic prunes the configurations that map to plans with cost >= to the best plan found so far.
	The second heuristic prunes the cost dominated configurations.
	A configuration c and c' is fact dominated by another configuration c' 
	if there exists an homomorphism from the facts of c to the facts of c' and the input constants are preserved.
	A configuration c is cost dominated by c' if it is fact dominated by c and maps to a plan with cost >= the cost of the plan of c'.
	
	Finally, the DAGOptimized class employs further techniques to speed up 
	the planning process like reasoning in parallel and re-use of reasoning results.    

**/