package uk.ac.ox.cs.pdq.fol;

// TODO: Auto-generated Javadoc
/**
 * Rule interface.
 *
 * @author Julien Leblay
 * @param <S> the generic type
 * @param <T> the generic type
 */
public interface Rule<S extends Formula, T extends Formula> {

	/**
	 * Gets the head.
	 *
	 * @return T
	 */
	T getHead();

	/**
	 * Gets the body.
	 *
	 * @return S
	 */
	S getBody();

	/**
	 * Contains.
	 *
	 * @param s the s
	 * @return true if the dependency contains the given relation signature in
	 * the left or right hand side.
	 */
	boolean contains(Signature s);
}
