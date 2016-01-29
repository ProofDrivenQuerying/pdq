package uk.ac.ox.cs.pdq.generator.third;

/**
 * 
 * This package defines a generator which creates inclusion dependencies and then queries using the previously created dependencies.
 * For schema generation we follow the steps below:
 * 
 *  Input parameters
 * 	NR=number of relations
	MAR=max arity
	MaxAcc= maximal number of accesses
	MaxCost
	Acc=accessibility: probability that a relation has any access at all
	Free=free-ness: probability that a position in a limited access is free
	Conn=connnectivity: probability that two relations are connected by an ID
	Proj=projectivity: probability that a position is projected in an ID
	Given this we generate a schema as follows:
	For i=1 to NR
		Choose an arity in MAR for R_i
	 	With probability (1-Acc) give R_i no access
	 	Else 
	     	Choose k randomly in [1,MaxAcc]
	     	For j=1 to k
	          	Make access to R_i with positions made free/bound with probability Free
	
	while(#created dependencies < user-defined #constraints)
		Pick two relations R_i and R_j
		Pick attributes A_i from R_i and attribute A_j from R_j
		and create a fk dependency from R_i.A_i to R_j.A_j;
		
			       
			       
			       
	For query generation we follow the steps below:
	
	Input parameters
	NumAtoms

	Q <-- \emptyset.
	L <-- 0
	Create an inclusion dependency graph.
	The vertices of this graph are the atoms of the inclusion dependencies.
	There is an edge from P_i to P_j if there is an inclusion dependency P_i(.) --> P_j(.).
	Start from a node in the dependency graph and return a connected path of nodes N of length equal to |Q|-L.
	With probability 0.5, either add the atoms of N to the query as they are,
	or create join predicates between the atoms in the query and the atoms in N.
	Set N <-- |Q| and repeat the above steps until |Q| = NumAtoms. 
	
 *  
 * 
 * @author Efthymia Tsamoura
 */