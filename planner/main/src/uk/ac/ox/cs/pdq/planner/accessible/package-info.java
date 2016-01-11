package uk.ac.ox.cs.pdq.planner.accessible;

/**
	@author Efthymia Tsamoura

	This package contains classes to create an accessible schema.
	
	Given schema S_0, the Accessible Schema for S_0, denoted
	AcSch(S_0), is the schema without any access restrictions, such
	that:
	i. The constants are those of S_0.
	ii.The relations are those of S_0, a copy of each relation R
	denoted AccessedR (the “accessible version of R”), a unary
	relation accessible(x) (“x is an accessible value”) plus another
	copy of each relation R of S0 called InferredAccR
	– the “inferred accessible version of R”. 
	iii.The constraints are the constraints of S_0 plus inferred accessible copies of the those constraints
	and accessibility axioms created from the schema access methods. 
	 
**/