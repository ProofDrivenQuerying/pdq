/**
 * Contains Volcano-style tuple iterators implementations.
 * 
 * Classes of the package are typically database physical operators,
 * inheriting the abstract {@code TupleIterator} class.
 * The top-level abstract provide main the method {@code next()} and 
 * {@code hasNext()}, which respectively returns the next tuple produced by the 
 * iterator and tells whether there are more to come.
 * 
 * A tree of tuple iterators forms a physical plan, where leafs are typically
 * data access operators.
 * The output of a plan is obtained by iterators over the tuple from the root
 * of the operator tree.
 */
package uk.ac.ox.cs.pdq.runtime.exec.iterator;