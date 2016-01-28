package uk.ac.ox.cs.pdq.generator;

/**
 * 
 * This package contains top-level interfaces for query, view and dependency generators.
 * 
 * The package first creates tuple generating dependencies or views given an input query.
 * The generation process proceeds as follows: 
 * First, the relations of the schema are created. Second, given the schema relations, 
 * the generator creates the query and, finally, it creates the views/dependencies.
 * This generator supports the creation of guarded queries (queries having a guard in their body), 
 * chain guarded queries (a chain query with a guard) and acyclic queries.
 * 
 * The package second creates inclusion dependencies and then queries using the previously created dependencies.
 * 
 * The package third creates inclusion dependencies and then queries using the previously created dependencies.
 *  
 * 
 * @author Efthymia Tsamoura
 */