// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

/**
	@author Mark Ridler
	
	The algebra sub-package defines terms and conditions in relational algebra.
	
	For an introduction to relational algebra see:
	
	https://en.wikipedia.org/wiki/Relational_algebra
	
	For example:
	
	-- RelationalTerm is the base class for a range of algebra terms
		-- SelectionTerm represents a basic selection (SELECT WHERE x)
		-- ProjectionTerm represents a basic projection (SELECT x, y, z)
		-- JoinTerm represents a join (SELECT FROM x JOIN y ON x.z = y,z)
		-- DependentJoinTerm represents a dependent join
		-- CartesianProductTerm defines a cartesian product
	-- Condition is the base class for a range of algebra conditions
		-- ConjunctiveCondition represents a conjunctive condition (WHERE x AND y)
		-- ConstantComparisonCondition represents a constant comparison condition (WHERE x < c)
		-- ConstantEqualityCondition represents a constant equality condition (WHERE x = c)
		-- ConstantInequalityCondition represents a constant inequality condition (WHERE x != c)

**/