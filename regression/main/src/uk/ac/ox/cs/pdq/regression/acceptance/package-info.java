package uk.ac.ox.cs.pdq.regression.acceptance;

/**
	@author Mark Ridler
	@author gabor
	 
	This package contains different algorithms to check the result of a plan creation or plan execution.
	
	- AcceptanceCriterion.java
 		* High-level acceptance criterion.
	- ApproximateCostAcceptanceCheck.java
 		* Acceptance test request the expected plan cost to be within an order of 
 		* magnitude of the expected plan cost to pass.
	- ExpectedCardinalityAcceptanceCheck.java
 		* Acceptance test request the expected and observed results to be equivalent
 		* under set semantics.
	- SameCostAcceptanceCheck.java
 		* Acceptance test request the expected and observed plans to have the same cost
 		* to pass.
	- SetEquivalentResultSetsAcceptanceCheck.java
 		* Acceptance test request the expected and observed results to be equivalent
 		* under set semantics.

**/