package uk.ac.ox.cs.pdq.util;

/**
	@author Efthymia Tsamoura and Mark Ridler
	
	This package implements a number of utility classes.
	
	The contents of this sub-package include:
	-- ConsistencyChecker, which is a generic interface to check the consistency of the input parameters.
	-- DistinctIterator, which is a wrapper for an iterator to ignore duplicate elements.
	-- EventHandler, which is a super-interface to all event planner handlers.
	-- GlobalCounterProvider, which provides an auto-incremented number.
	-- LimitReachedException, which is an exception that occurs when a task's timeout is reached.
	-- Triple, which is a triple of elements.
	-- Tuple, which is a database tuple. Tuples are immutable objects.
	-- Utility, which is a collection of functions that don't fit anywhere else
*
**/