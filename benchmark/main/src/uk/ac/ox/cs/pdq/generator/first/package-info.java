package uk.ac.ox.cs.pdq.generator.first;

/**
 * 
 * This package defines a generator which creates tuple generating dependencies or views given an input query.
 * First, it creates the relations of the schema. 
 * Second, given the schema relations, it creates the query and, finally, it creates the views/dependencies.
 * It supports the creation of guarded queries (queries having a guard in their body), 
 * chain guarded queries (a chain query with a guard) and acyclic queries.
 * 
 * 
 * Acyclic query generation details:
 * The algorithms starts by randomly selecting the predicates that will appear in the query's body, 
 * along with their variables. Different predicates are associated with different variables.
 * The user can choose whether a query will have repeated predicates or not.
 * Then the algorithm continues by populating the body of the query with atoms.
 * Two atoms A_i and A_i+1 join have only one join variable. 
 * 
 * 
 * 
 * Guarded conjunctive query generation details:
 * The algorithm starts by creating a list of variables V.
 * These variables are passed to a method for creating the body of the query.
 * The body is then created as follows: 
 * the algorithm picks a random sets of relations.
 * These relations form atoms with variables randomly selected variables from V. 
 * The relation of the maximum arity forms the guard which is populated with all variables from V. 
 *  
 * 
 * @author Efthymia Tsamoura
 */