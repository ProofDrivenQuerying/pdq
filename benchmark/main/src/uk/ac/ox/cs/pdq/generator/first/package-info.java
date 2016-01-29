package uk.ac.ox.cs.pdq.generator.first;

/**
 * 
 * This package defines a generator which creates tuple generating dependencies or views given an input query.
 * First, it creates the relations of the schema along with their access methods. 
 * Second, given the schema relations, it creates the query and, finally, it creates the views/dependencies.
 * It supports the creation of guarded queries (queries having a guard in their body), 
 * chain guarded queries (a chain query with a guard) and acyclic queries.
 * 
 * For schema generation, users must provide the following parameters:
 * --The number of schema relations
 * --The arity of the relations
 * --The cardinality of the relations
 * --The number of access method per relations
 * --The probability that a position is an input for an access method
 * --The access method cost
 * 
 * 
 * For query generation, users must provide the following parameters:
 * --The query type: GUARDED, CHAINGUARDED, ACYCLIC 
 * --The number of query conjuncts
 * --A boolean variable which specifies if the query will have or not repeated variables 
 * --The number of free variables of the query
 * 
 * Acyclic query generation details:
 * The algorithms starts by randomly selecting the predicates that will appear in the query's body.
 * For each predicate it creates a list of different variables.
 * The predicates are sorted based on the size of their associated variables in ascending order.
 * The user can choose whether a query will have repeated predicates or not.
 * Then the algorithm continues by populating the body of the query with atoms.
 * Two atoms A_i and A_i+1 have only one join variable. 
 * The join variable is randomly chosen.
 * 
 * 
 * Guarded conjunctive query generation details:
 * The algorithm starts by creating a list of variables V.
 * These variables are passed to a method for creating the body of the query.
 * For creating the body of the query the algorithm picks a random sets of relations.
 * These relations form atoms with variables randomly selected variables from V. 
 * The relation of the maximum arity forms the guard which is populated with all variables from V. 
 *  
 * For dependency generation, users must provide the following details:
 * --The query 
 * --The number of constraints
 * --A boolean variable which specifies if the dependency's left-hand or right-hand sides will have or not repeated variables 
 * 
 * 
 * @author Efthymia Tsamoura
 */