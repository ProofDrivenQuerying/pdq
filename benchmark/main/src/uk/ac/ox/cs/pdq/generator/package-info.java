package uk.ac.ox.cs.pdq.generator;

/**
 * 
 * This package contains top-level interfaces for query, view and dependency generators.
 * 
 * The package first defines a generator which creates tuple generating dependencies given an input query.
 * In brief the generation process proceeds as follows:
 * The generator creates the relations of the schema along with their access methods. 
 * Second, given the schema relations, the generator creates the query and, finally, the dependencies.
 * It supports the creation of guarded queries (queries having a guard in their body), 
 * chain guarded queries (a chain query with a guard) and acyclic queries.
 * 
 * The package second creates inclusion dependencies and then queries using the previously created dependencies.
 * 
 * The package third creates inclusion dependencies and then queries using the previously created dependencies.
 *  
 * 
 * @author Efthymia Tsamoura
 */