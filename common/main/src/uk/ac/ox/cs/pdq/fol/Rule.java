package uk.ac.ox.cs.pdq.fol;

/**
 * Rule interface
 *
 * @author Julien Leblay
 */
public interface Rule<S extends Formula, T extends Formula> {

	/**
	 * @return T
	 */
	T getHead();

	/**
	 * @return S
	 */
	S getBody();

	/**
	 * @return true if the dependency contains the given relation signature in
	 * the left or right hand side.
	 */
	boolean contains(Signature s);
}
