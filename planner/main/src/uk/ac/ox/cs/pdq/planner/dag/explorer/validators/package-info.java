package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

/**
	@author Efthymia Tsamoura

	The classes of this package check whether a pair of configurations will satisfy 
	given shape restrictions after combined into a bigger binary configuration.
	The DefaultValidator class requires the left and right configurations to be non-trivial:
	an ordered pair of configurations (left, right)
	is non-trivial if the output facts of the right configuration are not included in
	the output facts of left configuration and vice versa.
	
	Other classes impose additional restrictions, e.g., the right configuration must be always a unary one, or 
	the final configuration composed from the two input ones must be input free. 

**/