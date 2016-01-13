package uk.ac.ox.cs.pdq.planner.linear.explorer.node.equivalence;

/**
	@author Efthymia Tsamoura
	This package contains classes that store tree paths having equivalent configurations.
 	Each node of the path is saturated using the chase and is associated with a unique configuration.
 	A chase sequence v is  equivalent to another
	sequence v' if there is a bijection h from the configuration of v
	to the configuration of v' that preserves any constants of the input schema S_0,
	the original relations of S_0 and the relations of the form InferredAccR. 
	 
**/