package uk.ac.ox.cs.pdq.fol;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.rewrite.Rewritable;

/**
 * Top-level FO formula.
 *
 * @author Julien Leblay
 */
public interface Formula extends Rewritable {

	/**
	 * TOCOMMENT What are the children of a formula?
	 * Gets the children.
	 *
	 * @param <T> the generic type
	 * @return the subformula
	 */
	<T extends Formula> Collection<T> getChildren();

	/**
	 * Gets all predicates in the formula.
	 *
	 * @return the atoms of this formula
	 */
	List<Atom> getAtoms();

	/**
	 * Gets all terms in the formula.
	 *
	 * @return the terms of this formula
	 */
	List<Term> getTerms();

	/**
	 * TOCOMMENT Is this a real "grounding" in all implementations of this method? 
	 * That is, are all variables substituted by constants or an exception -or something similar- is risen, otherwise.
	 * If we are more flexible and enforce real grounding, maybe this method should be call substitution or sth like this.
	 * 
	 * Replaces all variables of (TOCOMMENT the input map? or the formula?), with the constants mapped to them in the input map.
	 *
	 * @param mapping Map of variable terms to constants
	 * @return a grounded copy of this formula whose variables have been set according to the given mapping.
	 */
	Formula ground(Map<Variable, Constant> mapping);
}
