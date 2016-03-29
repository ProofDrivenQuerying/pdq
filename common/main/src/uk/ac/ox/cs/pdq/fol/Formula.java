package uk.ac.ox.cs.pdq.fol;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.rewrite.Rewritable;

// TODO: Auto-generated Javadoc
/**
 * Top-level FO formula.
 *
 * @author Julien Leblay
 */
public interface Formula extends Rewritable {

	/**
	 * Gets the children.
	 *
	 * @param <T> the generic type
	 * @return the subformula
	 */
	<T extends Formula> Collection<T> getChildren();

	/**
	 * Gets the predicates.
	 *
	 * @return the atoms of this formula
	 */
	List<Atom> getAtoms();

	/**
	 * Gets the terms.
	 *
	 * @return the terms of this formula
	 */
	List<Term> getTerms();

	/**
	 * Ground.
	 *
	 * @param mapping Map of variable terms to constants
	 * @return a grounded copy of this formula whose variables have been set according to the given mapping.
	 */
	Formula ground(Map<Variable, Constant> mapping);
}
