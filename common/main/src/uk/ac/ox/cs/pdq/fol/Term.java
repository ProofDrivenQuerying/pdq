package uk.ac.ox.cs.pdq.fol;

// TODO: Auto-generated Javadoc
/**
 * A formula term.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface Term {
	
	/**
	 * Checks if is variable.
	 *
	 * @return true if the term is a variable
	 */
	public boolean isVariable();

	/**
	 * Checks if is skolem.
	 *
	 * @return true if the term is a Skolem
	 */
	public boolean isSkolem();
	
	public Term clone();
}
