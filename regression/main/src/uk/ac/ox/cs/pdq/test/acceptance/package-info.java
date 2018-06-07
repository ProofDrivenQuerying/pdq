package uk.ac.ox.cs.pdq.test.acceptance;

/**
	@author Mark Ridler

	This package contains tests for Acceptance Checks:
	
	- AcceptanceCriterion.java: The result of an acceptance criterion check. 
	 	* It consists of an acceptance level and supporting information as a list 
	 	* of Strings.
	- ApproximateCostAcceptanceCheck.java: 
 		* Acceptance test request the expected plan cost to be within an order of 
		* magnitude of the expected plan cost to pass.
	- ExpectedCardinalityAcceptanceCheck.java
	 	* Acceptance test request the expected and observed results to be equivalent
	 	* under set semantics.
	- SameCostAcceptanceCheck.java
 		* Acceptance test request the expected and observed plans to have the same cost
 		* to pass.
	- SetEquilaventResultSetsAcceptanceCheck.java
 		* Acceptance test request the expected and observed results to be equivalent
 		* under set semantics.

**/