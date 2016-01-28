package uk.ac.ox.cs.pdq.planner.dag.explorer;

/**
	@author Efthymia Tsamoura

	This package contains classes that explore the space of DAG proofs.
	
	Exploration proceeds roughly as follows.
 	First, create all unary configurations. Unary configuration correspond to single access plans.
 	Then in every exploration step, create a new binary configuration by combining two other configurations. 
 	Saturate the new configuration using the constraints of the accessible schema. 
 	Finally, check if the newly configuration matches the accessible query and update the best configuration appropriately.   
	
	-The DAGGeneric class explores the space of proofs exhaustively.
	-The DAGOptimized, DAGSimpleDP and DAGChaseFriendlyDP employ two DP-like heuristics to cut down the search space.
	The first heuristic prunes the configurations that map to plans with cost >= to the best plan found so far.
	The second heuristic prunes the cost dominated configurations. A configuration c and c' is fact dominated by another configuration c' 
	if there exists an homomorphism from the facts of c to the facts of c' and the input constants are preserved.
	A configuration c is cost dominated by c' if it is fact dominated by c and maps to a plan with cost >= the cost of the plan of c'.
	-The DAGOptimized class employs further techniques to speed up the planning process like reasoning in parallel and re-use of reasoning results.    

**/