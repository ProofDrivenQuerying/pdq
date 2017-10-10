package uk.ac.ox.cs.pdq.fol;

import java.io.Serializable;

/**
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
