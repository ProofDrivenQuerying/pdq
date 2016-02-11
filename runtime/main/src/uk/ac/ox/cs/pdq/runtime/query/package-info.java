/**
 * Contains classes to evaluate queries, i.e. produce the answer of a query 
 * w.r.t. to a data instance.
 * 
 * In particular, classes in this package of not concerned with the notion of 
 * plan or accessibility.
 * 
 * The {@code QueryEvaluator} interface is the main point of entry of this 
 * package. It allows evaluation a single query onto a single data instance,
 * regardless of the actual nature of the data instance.
 * Implementations of this interface take care of the instance-specific 
 * query processing.
 * 
 * Typically, {@code InMemoryQueryEvaluator} deals with evaluation of query 
 * where the data is stored in memory, while {@code SQLQueryEvaluation} will 
 * convert a query to SQL to have evaluated by an underlying database over JDBC.
 */
package uk.ac.ox.cs.pdq.runtime.query;