// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.fol;

/**
 * @author Efthymia Tsamoura and Mark Ridler
 * 
 *  
 *	This fol sub-package defines several first order logic objects including
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
 * 
 *  The sub-package includes the following prominent classes:
 *  
 *  -- Atom, which is a formula that contains no logical connectives
 *  -- Clause, which is a disjunction of literals
 *  -- Conjunction, which is a formula connected by AND 
 *  -- ConjunctiveQuery, which is a first order formula of the form \exists x_1, \ldots, x_n \Wedge A_i,
 *      where A_i are atoms with arguments that are either variables or constants.
 *  -- Constant, which is a constant Term
 *  -- Dependency, which is a universally quantified implication where the body is a quantifier-free formula and 
 *      the head is an existentially-quantified or quantifier-free formula.
 *  -- Disjunction, which is a formula connected by OR
 *  -- Formula, which is the base class for formulae in this package
 *  -- Implication, which is a subclass of Formula
 *  -- Literal, which is a positive or negative atom
 *  -- Predicate, whose signature, associates a symbol with an arity
 *  -- Term, which is the base class for terms in this package
 *  -- Variable, which is a variable Term
 *  
 **/