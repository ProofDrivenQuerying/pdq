package uk.ac.ox.cs.pdq.plan;

/**
	@author Efthymia Tsamoura

	This package defines several types of relational algebra plans:
	--left-deep plans: plans where the operators are organised into a left-deep tree
	--tree plans: plans where the operators are organised into a tree 
	--DAG plans: plans whre the operators are organised into a DAG
	--RA plans: An RA-plan consists of a sequence of access and middleware query commands, along with a distinguished final output relation Tn.
	The top-level interface for the first three types of plans is the Plan interface.
	Each plan is associated with a cost.
*
**/