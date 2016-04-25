package uk.ac.ox.cs.pdq.fol;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.TypedConstant;

// TODO: Auto-generated Javadoc
/**
 * By a query we mean a mapping from relation instances of some schema
 * to instances of some other relation. A boolean query is a query where the output is a
 * relation of arity 0. Since there are only two instances for a relation of arity 0, a boolean
 * query is a mapping where the output takes one of two values, denoted True and False.
 * Given a query Q and instance I, Q(I) is the result of evaluating Q on I.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @param <S> the generic type
 */
public interface Query<S extends Formula> extends Formula, Evaluatable, Rule<S, Atom> {

	/**
	 * Checks if is boolean.
	 *
	 * @return true if the query is boolean
	 */
	boolean isBoolean();

	/**
	 * Gets the body.
	 *
	 * @return the query's body
	 * @see uk.ac.ox.cs.pdq.formula.Evaluatable#getBody()
	 */
	@Override
	S getBody();

	/**
	 * Gets the head.
	 *
	 * @return the query's head
	 * @see uk.ac.ox.cs.pdq.formula.Rule#getHead()
	 */
	@Override
	Atom getHead();

	/**
	 * Gets the schema constants.
	 *
	 * @return the constants that appear in the query's body
	 */
	Collection<TypedConstant<?>> getSchemaConstants();
}
