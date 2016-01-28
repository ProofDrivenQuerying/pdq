package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

/**
	@author Efthymia Tsamoura

	The classes of this package check whether a pair of configurations c and c' will satisfy 
	given shape restrictions after combined into a new binary configuration Binary(c,c').

	-The DefaultValidator class requires the left and right configurations to be non-trivial:
	an ordered pair of configurations (left, right) is non-trivial if the output facts of the right configuration are not included in
	the output facts of left configuration and vice versa.
	-The ApplyRuleDepthValidator requires the input pair of configurations to be non trivial, their combined depth to be <= the depth threshold
	and at least one of the input configurations to be an ApplyRule.
	-The ApplyRuleValidator requires the input pair of configurations to be non trivial and at least one of the input configurations to be an ApplyRule.
	-The DepthValidator requires the input pair of configurations to be non trivial and their combined depth to be <= the depth threshold.
	-The LinearValidator requires the input pair of configurations to be non trivial and their composition to be a closed left-deep configuration
	-The RightDepthValidator requires the input pair of configurations to be non trivial and the right's depth to be <= the depth threshold

**/