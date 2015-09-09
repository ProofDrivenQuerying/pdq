package uk.ac.ox.cs.pdq.fol;

/**
 * A formula term
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface Term {
	/**
	 * @return true if the term is a variable
	 */
	public boolean isVariable();

	/**
	 * @return true if the term is a Skolem
	 */
	public boolean isSkolem();
}
