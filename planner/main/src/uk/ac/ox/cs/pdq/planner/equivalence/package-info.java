package uk.ac.ox.cs.pdq.planner.equivalence;

/**
	@author Efthymia Tsamoura

	The classes of this package implement different notions of configuration equivalence:
	-Fact-equivalence. Two configurations c and c' are equivalent if they have the same input constants and 
 	there exists an homomorphism from the facts of c to the facts of c' and vice-versa.
 	-Fast fact equivalence. Two configurations c and c' are equivalent if the have the same inferred accessible facts. 
 	In order to perform this kind of equivalence check Skolem constants must be assigned to formula variables during chasing.

**/