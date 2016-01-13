package uk.ac.ox.cs.pdq.reasoning;

/**
	@author Efthymia Tsamoura

	This package contains classes that initiate and start the reasoning process.
	The initiation process consists of the following steps:
	
		-selection of the appropriate reasoning mechanism. The only reasoning mechanism that is supported is the chase.
		-selection of the appropriate mechanism to detect homomorphisms during chasing or to detect query matches. 
		Homomorphism detection works as follows: the chase facts are stored in a database. 
		Every time we check if there is an homomorphism of a formula F to the facts of a chase instance, 
		we create an SQL query from F's atoms and submit it to a database engine. The database engine returns all the facts that 
		are homomorphic to F. The database engine that are supported are MySQL and Derby.
	
**/