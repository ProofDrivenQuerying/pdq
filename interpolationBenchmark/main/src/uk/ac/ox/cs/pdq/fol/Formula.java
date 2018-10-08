package uk.ac.ox.cs.pdq.fol;

import java.util.List;


/**
 * Top-level FO formula.
 *
 * @author Efthymia Tsamoura
 */
public abstract class Formula {

	public abstract int getId();
	
	/**
	 * TOCOMMENT What are the children of a formula?
	 * Gets the children.
	 *
	 * @param <T> the generic type
	 * @return the subformula
	 */
	public abstract <T extends Formula> List<T> getChildren();

	/**
	 * Gets all atoms of the formula.
	 *
	 * @return the atoms of this formula
	 */
	public abstract List<Atom> getAtoms();

	/**
	 * Gets all terms in the formula.
	 *
	 * @return the terms of this formula
	 */
	public abstract List<Term> getTerms();
	
	public abstract List<Variable> getFreeVariables();
	
	public abstract List<Variable> getBoundVariables();
}
