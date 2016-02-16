package uk.ac.ox.cs.pdq.util;

import java.util.List;

import uk.ac.ox.cs.pdq.db.Attribute;

// TODO: Auto-generated Javadoc
/**
 * Common interface to pipelinable iterators, i.e. iterators that iterate over
 * a set of input tuples, and return output tuple one at a time.
 * 
 * @author Julien Leblay
 *
 */
public interface Pipelineable {
	
	/**
	 * Returns an iterator over tuple taking another tuple iterator as input .
	 *
	 * @param inputAttributes List<? extends Attribute>
	 * @param inputs the iterator over the input tuples
	 * @return an iterator on output tuples.
	 */
	ResetableIterator<Tuple> iterator(List<? extends Attribute> inputAttributes, ResetableIterator<Tuple> inputs);
	
	/**
	 * Returns an input free tuple iterator .
	 *
	 * @return an iterator on output tuples.
	 */
	ResetableIterator<Tuple> iterator();
}
