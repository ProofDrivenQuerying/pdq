package uk.ac.ox.cs.pdq.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Rule;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * A schema constraint
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface Constraint<L extends Formula, R extends Formula> extends Evaluatable, Rule<L, R> {

	/**
	 * @param match
	 * 		Input mapping from variables to constants
	 * @param canonicalNames
	 * 		True if we assign Skolem constants to the existentially quantified variables
	 * @return the grounded dependency using the input mapping
	 */
	Constraint<L, R> fire(Map<Variable, Constant> match, boolean canonicalNames);

	/**
	 * @return the schema constants of this constraint
	 */
	Collection<TypedConstant<?>> getSchemaConstants();

	/**
	 * @return the left-hand side of this constraint
	 */
	L getLeft();

	/**
	 * @return the right-hand side of this constraint
	 */
	R getRight();


	/**
	 * @return List<PredicateFormula>
	 */
	List<Predicate> getPredicates();

	/**
	 * @return the variables of both sides of this constraint
	 */
	Set<Variable> getBothSideVariables();

	/**
	 * @return true if the dependency contains the given relation signature in
	 * the left or right hand side.
	 */
	@Override
	boolean contains(Signature s);
}
