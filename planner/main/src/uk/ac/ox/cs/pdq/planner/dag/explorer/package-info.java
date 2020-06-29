// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dag.explorer;

/**
	@author Efthymia Tsamoura and Michael Benedikt

	This package contains classes that explore the space of DAG proofs.
	
	Exploration proceeds roughly as follows.
 	First, create all unary configurations. Unary configuration correspond to single access plans.
 	Then in every exploration step, create a new binary configuration by combining two other configurations. 
 	Saturate the new configuration using the constraints of the accessible schema. 
 	Finally, check if the newly configuration matches the accessible query and update the best configuration appropriately.   
	
	-The DAGGenericSimple class explores the space of proofs exhaustively.
	-The DAGOptimizedMultiThread (which extends the base class DAGOptimized). This employs some DP-like heuristics to cut down the search space. In the past heuristics that we consider were:
        -----Pruning the configurations that map to plans with cost >= to the best plan found so far.
	---- Pruing the cost dominated configurations. A configuration c and c' is fact dominated by another configuration c' 
	if there exists an homomorphism from the facts of c to the facts of c' and the input constants are preserved.
	A configuration c is cost dominated by c' if it is fact dominated by c and maps to a plan with cost >= the cost of the plan of c'.
	-The class employs further techniques to speed up the planning process like reasoning in parallel and re-use of reasoning results.    

**/
