package uk.ac.ox.cs.pdq.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Rule;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * A schema constraint.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @param <L> the generic type
 * @param <R> the generic type
 */
public interface Dependency<L extends Formula, R extends Formula> extends Evaluatable, Rule<L, R> {

	/**
	 * TOCOMMENT In literature one first detects all active triggers and then "fires" them; is this the case here?
	 * I can see the fire method gets a "match" and I assume this match is the active trigger's antecedent homomorphism
	 * depending on the chase version used. 
	 * Fires the constraint.
	 *
	 * @param match 		Input mapping from variables to constants
	 * @param canonicalNames 		True if we assign Skolem constants to the existentially quantified variables
	 * @return the grounded dependency using the input mapping
	 */
	Dependency<L, R> fire(Map<Variable, Constant> match, boolean canonicalNames);

	/**
	 * TOCOMMENT Schema constants as opposed to what other kind of constant?
	 * Gets the schema constants.
	 *
	 * @return the schema constants of this constraint
	 */
	Collection<TypedConstant<?>> getSchemaConstants();

	/**
	 * TOCOMMENT it would be better to say getBody or getHead; it's not really clear what left and right is.
	 * Gets the left-hand side of this constraint.
	 *
	 * @return the left-hand side of this constraint
	 */
	L getLeft();

	/**
	 * Gets the right-hand side of this constraint.
	 *
	 * @return the right-hand side of this constraint
	 */
	R getRight();


	/**
	 * Gets all atoms in this dependency.
	 *
	 * @return List<Atom>
	 */
	List<Atom> getAtoms();

	/**
	 * Gets all universally and existentially quantified variables.
	 *
	 * @return the variables of both sides of this constraint
	 */
	Set<Variable> getAllVariables();

	/**
	 *
	 * @param s the s
	 * @return true if the dependency contains the given relation signature in
	 * the left or right hand side.
	 */
	@Override
	boolean contains(Predicate s);
	
	Dependency<L,R> clone();
}
