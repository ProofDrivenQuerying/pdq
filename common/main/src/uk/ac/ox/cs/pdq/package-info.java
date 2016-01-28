package uk.ac.ox.cs.pdq;

/**
	@author Efthymia Tsamoura


	This package defines classes used across the whole PDQ application.
	

	The package algebra defines several logical relational operators including selections, projections, joins and unions. 
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
	
	 
	 The package fol defines several fol objects including
	-logical connectives (conjunction, disjunction, implication and so on)
	-terms which can be either variables or constants
	-atoms (a predicate applied to a tuple of terms; that is, an atomic formula is a formula of the form P (t_1, \ldots, t_n) for P a predicate, and the t_i terms.)
	-formulas (conjunctive formulas, quantified formulas, unary formulas, negations, implications)
	-queries(by a query we mean a mapping from relation instances of some schema to instances of some other relation)
	-conjunctive queries (first order formulae of the form \exists x_1, \ldots, x_n \Wedge A_i, where A_i are atoms with arguments that are either variables or constants.)
	-acyclic conjunctive queries
	
	The package plan defines several types of relational algebra plans:
	--left-deep plans: plans where the operators are organised into a left-deep tree
	--tree plans: plans where the operators are organised into a tree 
	--DAG plans: plans whre the operators are organised into a DAG
	--RA plans: An RA-plan consists of a sequence of access and middleware query commands, along with a distinguished final output relation Tn.
	The top-level interface for the first three types of plans is the Plan interface.
	Each plan is associated with a cost.
	
	Finally, the package util defines several utility classes including tuples and tables.
**/