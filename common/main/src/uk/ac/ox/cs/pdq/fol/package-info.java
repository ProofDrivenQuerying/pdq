package uk.ac.ox.cs.pdq.fol;

/**
 * @author Efthymia Tsamoura
 * 
 *         <pre>
 *	This package defines several fol objects including
 *
 *	-logical connectives - conjunction, disjunction, implication and so on
 *
 *	-terms which can be either variables or constants
 *
 *	-atoms - a predicate applied to a tuple of terms; that is, an atomic formula is a formula of the form 
 *				P (t_1, ..., t_n) for P a predicate, and the t_i terms.
 *
 *	-formulas - conjunctive formulas, quantified formulas, unary formulas, negations, implications
 *
 *	-queries - by a query we mean a mapping from relation instances of some schema to instances of some other relation
 *
 *	-conjunctive queries - first order formulae of the form exists[x_1, ..., x_n] -> A_1,...,A_n, where A_i are atoms with arguments that are either variables or constants.
 *
 *	-acyclic conjunctive queries
 * 
 *         </pre>
 * 
 **/