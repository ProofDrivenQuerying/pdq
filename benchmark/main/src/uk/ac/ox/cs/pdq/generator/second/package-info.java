package uk.ac.ox.cs.pdq.generator.second;

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
	
			For each R_i, R_j
			        With probability Conn, make an ID between R_i and R_j,
			        projecting out a position with probability Proj
			       
			       
			       
	For query generation we follow the steps below:
	
	Input parameters
	NumAtoms
	Dist= max distance to a free access
	JoinTest= probability of joining
	
	Given this we generate a query as follows:
	
	For each i in NumAtoms
	        Choose j randomly in [1,Dist]
	        Let F=relations with a free access
	        Let R_j= relations that have a path in the dependency graph
	        to a relation in F of distance at most j, and also a path from
	        a relation in F of distance at most j
	        Choose relation R randomly from R_j
	        For each position of R,
	               with probability join, choose an existing variable (uniformly at random)
	               otherwise choose a fresh variable
	
 *  
 * 
 * @author Efthymia Tsamoura
 */