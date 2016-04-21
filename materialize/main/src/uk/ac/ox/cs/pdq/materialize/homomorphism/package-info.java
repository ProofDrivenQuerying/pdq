package uk.ac.ox.cs.pdq.materialize.homomorphism;

/**
	@author Efthymia Tsamoura

	This package contains classes that detect homomorphisms of the atoms of a formula F to a set of facts. 
	Homomorphism detection works as follows: the chase facts are stored in a database. 
	Every time we check if there is an homomorphism of a formula F to the facts of a chase instance, 
	we create an SQL query from F's atoms and submit it to a database engine. The database engine returns all the facts that 
	are homomorphic to F. The database engines that are supported are MySQL and Derby.
	
**/