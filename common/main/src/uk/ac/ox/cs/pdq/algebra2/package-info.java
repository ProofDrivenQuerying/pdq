package uk.ac.ox.cs.pdq.algebra2;

/**
	@author Efthymia Tsamoura

	This package defines several logical relational operators including selections, projections, joins and unions. 
	This package also includes three access operators:
	-Access: currently deprecated 
	-Scan: a database scan operator
	-DependentAccess: an operator that requires some input to initiate an access.
	This package also contains a dependent join operator, a join where the right-hand child requires inputs from the left-hand child in order to run.
	The top-level interface is RelationalOperator.
	
	The subpackage .predicates defines classes of predicates used in selections.
	The following predicates are supported:
	-Attribute equality predicates of the form R1.a_i=R2.a_j for relations R1,R2 and attributes a_i and a_j.
	-Constant equality predicates of the form R1.a_i=c for relation R1 and attribute a_i and schema constant c.
	-Conjunctive predicates: conjunctions of attribute or constant equality predicates
**/