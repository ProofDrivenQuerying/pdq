// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.db.tuple;

import java.io.Serializable;


/**
 * A database tuple.
 * Tuple are immutable objects.
 * Tuples should contain only immutable objects or objects that won't be modified while part of a tuple.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface Tuple extends Serializable{

	/** The Constant EmptyTuple. */
	public static final Tuple EmptyTuple = new TupleImpl(TupleType.EmptyTupleType);

	/**
 * Gets the type.
 *
 * @return the tuple's type
 */
	public TupleType getType();

	/**
	 * Size.
	 *
	 * @return the size of the tuple
	 */
	public int size();

	/**
	 * Gets the value.
	 *
	 * @param <T> the generic type
	 * @param i Position in a tuple
	 * @return the value of the tuple at position i
	 */
	public <T> T getValue(int i);

	/**
	 * Gets the values.
	 *
	 * @return the values of the tuple as an object array
	 */
	public Object[] getValues();

	/**
	 * Append tuple.
	 *
	 * @param t Input tuple
	 * @return A tuple that is the concatenation of t to the current one
	 */
	public Tuple appendTuple(Tuple t);
}
