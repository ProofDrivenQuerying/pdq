// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

/**
	@author Efthymia Tsamoura

	The classes of this package check whether a pair of configurations c and c' will satisfy 
	given shape restrictions after combined into a new binary configuration Binary(c,c').

	-The ApplyRulePairValidator requires the input pair of configurations to have at least one ApplyRule.
	-The ClosedPairValidator requires requires the input pair of configurations form a closed composition.
	-The DefaultPairValidator requires the left and right configurations to be non-trivial:
			an ordered pair of configurations (left, right) is non-trivial if the output facts of the right configuration are not included in the output facts of left configuration and vice versa.
	-The DepthPairValidator requires the input pair of configurations to have their combined depth to be <= the depth threshold.
	-The LinearPairValidator requires the composition  of the input pair of configurations to be a closed left-deep configuration
	-The RightDepthPairValidator requires the right input's depth to be <= the depth threshold

**/