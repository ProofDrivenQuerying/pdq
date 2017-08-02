package uk.ac.ox.cs.pdq.fol;

import java.io.Serializable;

/**
 * TOCOMMENT I disaggree with this being an interface. Interfaces are there to
 * describe common functions. Here we artificially create an interface by
 * merging a functionality onle relative to Variables (isVariable()) with one
 * only relative to Skolems (isSkolem()) (and how about constants?)
 * 
 * A formula term.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */

public abstract class Term implements Serializable {

	private static final long serialVersionUID = 3989496463927818956L;

	/**
	 * Checks if is variable.
	 *
	 * @return true if the term is a variable
	 */
	public abstract boolean isVariable();

	/**
	 * Checks if is skolem.
	 *
	 * @return true if the term is a Skolem
	 */
	public abstract boolean isUntypedConstant();
}
