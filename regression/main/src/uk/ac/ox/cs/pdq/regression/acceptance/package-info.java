package uk.ac.ox.cs.pdq.regression.acceptance;

/**
	@author Mark Ridler
	
	This package contains:
	
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

	This package runs regression tests with a theme of acceptance criteria.
**/