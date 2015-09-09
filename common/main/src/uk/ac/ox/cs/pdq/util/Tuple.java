package uk.ac.ox.cs.pdq.util;

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

	public static final Tuple EmptyTuple = new TupleImpl(TupleType.EmptyTupleType);
	
//	public static final Tuple ErrorTuple = new TupleImpl(TupleType.EmptyTupleType);

	/**
	 * @return the tuple's type
	 */
	public TupleType getType();

	/**
	 * @return the size of the tuple
	 */
	public int size();

	/**
	 * @param i Position in a tuple
	 * @return the value of the tuple at position i
	 */
	public <T> T getValue(int i);

	/**
	 * @return the values of the tuple as an object array
	 */
	public Object[] getValues();

	/**
	 * @param t Input tuple
	 * @return A tuple that is the concatenation of t to the current one
	 */
	public Tuple appendTuple(Tuple t);
}
