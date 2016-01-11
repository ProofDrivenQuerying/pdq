package uk.ac.ox.cs.pdq.planner.dominance;

/**
	@author Efthymia Tsamoura

	The classes of this package implement different notions of configuration and plan dominance:
	
	Closed dominance. A closed configuration c dominates a closed configuration c', if c both cost- and fact- dominates c'.
	Closed success dominance. A closed plan p success dominates another closed plan p', if p is successful and has cost < the cost of p'.
	Fact dominance. A configuration c and c' is fact dominated by another configuration c' if there exists an homomorphism from the facts of c to the facts of c' and
	the input constants are preserved.
	Numerical success dominance. A configuration c dominates a configuration c', if c has >= facts than c'.
	
	
	Open success dominance. A plan p success dominates another closed plan ', if p is successful and has cost < the cost of p'.
 	If either of the plans is open, then a simple plan cost estimator is used to assess their cost;
 	otherwise, the costs of their corresponding (closed) plans are considered.
 	
 	Open configuration dominance. A configuration c dominates a configuration c',
 	if c both cost- and fact- dominates c' with one of the two being strict.
 	When both configurations are open, then a simple plan cost estimator is used
 	to assess the configurations' costs;
 	otherwise, the costs of their corresponding (closed) plans are considered.

**/