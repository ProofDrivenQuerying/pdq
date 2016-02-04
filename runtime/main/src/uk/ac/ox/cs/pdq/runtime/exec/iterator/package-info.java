/**
 * Volcano-iterator implementations.
 * 
 * Classes of the package are typically database physical operators,
 * inheriting the abstract TupleIterator class.
 * The top-level abstract provide main the method next() and hasNext(), 
 * which respectively return the tuple produced by the iterator and tells
 * whether there are more to return.
 * 
 * A tree of tuple iterators forms a physical plan, where leafs are typically
 * data access operators.
 * The output of a plan is obtained by iterators over the tuple from the root
 * of the operator tree.
 */
package uk.ac.ox.cs.pdq.runtime.exec.iterator;