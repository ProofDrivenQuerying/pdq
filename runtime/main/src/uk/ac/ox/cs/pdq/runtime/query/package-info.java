/**
 * Classes in the query this package are used to process queries, i.e.
 * produce the answer of a query wrt to a data instance.
 * In particular, classes in this package of not concerned with the notion of 
 * plan or accessibility.
 * 
 * The high-level QueryEvaluator class is the main point of entry of this 
 * package. It allows evaluation a single query onto a single data instance,
 * regardless of the actual nature of the data instance.
 * Implementation of this interface, take care of the instance-specific 
 * processing the query.
 * Typically, InMemoryQueryEvaluator deals with evaluation of query where the
 * data is stored in memory, while SQLQueryEvaluation will convert a query
 * to SQL to have evaluated by an underlying database over JDBC.
 */
package uk.ac.ox.cs.pdq.runtime.query;