package uk.ac.ox.cs.pdq.generator.first;

/**
 * 
 * This package defines a generator which creates tuple generating dependencies or views given an input query.
 * In brief the generation process proceeds as follows:
 * The generator creates the relations of the schema along with their access methods. 
 * Second, given the schema relations, the generator creates the query and, finally, the dependencies.
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
 * For query generation, users must provide the following parameters:
 * --The query type: GUARDED, CHAINGUARDED, ACYCLIC 
 * --The number of query conjuncts
 * --A boolean variable which specifies if the query will have or not repeated variables 
 * --The number of free variables of the query
 * 
 * Acyclic query generation details:
 * The algorithms starts by randomly selecting the predicates that will appear in the query's body.
 * For each predicate it creates a list of different variables.
 * The predicates are sorted in ascending order based on the size of their associated variables.
 * The atoms that will be created out of these predicates will appear in the final query in that order.
 * The algorithm proceeds by populating the body of the query with atoms.
 * Two atoms A_i and A_i+1 have only one join variable. 
 * The join variable is randomly chosen.
 * 
 * Guarded conjunctive query generation details:
 * The algorithm starts by creating a list of variables V.
 * Then the algorithm picks a random sets of relations.
 * These relations form atoms with variables randomly selected variables from V. 
 * The relation of the maximum arity forms the guard which is populated with all variables from V. 
 * The atoms that are created above populate the query's body.
 *  
 * For dependency generation, users must provide the following details:
 * --The query 
 * --The number of dependencies D
 * --A boolean variable which specifies if the dependency's left-hand or right-hand sides will have or not repeated variables 
 * 
 * The dependency generation algorithm takes in the input the query Q. 
 * For guarded queries, the dependency generation proceeds as follows:
 * Let A be the atoms of Q.
 * The algorithm creates the powerset O of Q.
 * Then the algorithm picks one random set P from O. 
 * If the atoms of P do not contain the guard and |P|>1, then the guard of the query is added to P.
 * The atoms of P will form the left-hand side of the newly created dependency d. 
 * Then the algorithm creates the right-hand side of d, by randomly selecting predicates from the input schema, 
 * and populating these predicates with a mix of variables from P and fresh variables.   
 * 
 * If the number of dependencies that are created that way are less than D,
 * then the algorithm creates new guarded dependencies following the steps below:
 * The algorithm starts by creating a list of variables V.
 * These variables will be the universally quantified variables of the newly created dependency d.
 * The algorithm then picks a random sets of relations whose predicates will populate the left-hand side of d. 
 * These relations form atoms with variables randomly selected variables from V. 
 * The relation of the maximum arity forms the guard which is populated with all variables from V. 
 * Then the algorithm creates the right-hand side of d, by randomly selecting predicates from the input schema, 
 * and populating these predicates with a mix of variables from P and fresh variables.   
 * 
 * 
 * For acyclic queries, the dependency generation proceeds as follows:
 * The algorithm picks an atom A form the query's body.
 * This atom will form the left-hand side of the newly created dependency d.
 * Then, the algorithm creates the right-hand side of d, by randomly selecting predicates from the input schema, 
 * and populating these predicates with a mix of variables from A and fresh variables.  
 * 
 * If the number of dependencies that are created that way are less than D,
 * then the algorithm creates new guarded dependencies as described above. 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author Efthymia Tsamoura
 */