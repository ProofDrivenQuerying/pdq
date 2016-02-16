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
public interface Query<S extends Formula> extends Formula, Evaluatable, Rule<S, Predicate> {

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
	Predicate getHead();

	/**
	 * Gets the schema constants.
	 *
	 * @return the constants that appear in the query's body
	 */
	Collection<TypedConstant<?>> getSchemaConstants();
	

	/**
	 * Gets the free to canonical.
	 *
	 * @return a map of query's free variables to its canonical constants.
	 * Given a CQ Q, the canonical database of Q is the instance which has for each atom R(\vec{v}) 
	 * in Q a corresponding fact for relation R with \vec{v} as a tuple. The canonical constants are the constants of the canonical database of Q
	 */
	Map<Variable, Constant> getFreeToCanonical();
	
	/**
	 * Gets the variables to canonical.
	 *
	 * @return a map of query's variables both free and quantified to chase constants appear in the canonical query.
	 * Given a CQ Q, the canonical database of Q is the instance which has for each atom R(\vec{v}) 
	 * in Q a corresponding fact for relation R with \vec{v} as a tuple. The canonical constants are the constants of the canonical database of Q
	 */
	Map<Variable, Constant> getVariablesToCanonical();

	/**
	 * Ground.
	 *
	 * @param mapping Map<Variable,Constant>
	 * @return a copy of the query grounded using the given mapping
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Constant>)
	 */
	@Override
	Formula ground(Map<Variable, Constant> mapping);

	/**
	 * Gets the canonical.
	 *
	 * @return the canonical database of this query.
	 * Given a CQ Q, the canonical database of Q is the instance which has for each atom R(\vec{v}) 
	 * in Q a corresponding fact for relation R with \vec{v} as a tuple. The canonical constants are the constants of the canonical database of Q
	 */
	S getCanonical();
	
	/**
	 * Sets the grounding.
	 *
	 * @param grounding Grounds this query using the input mapping of free variables to constants
	 */
	void setGrounding(Map<Variable, Constant> grounding);
	
	/**
	 * Gets the important subqueries.
	 *
	 * @return any sub-query of the query for any combination of free/bound variables
	 */
	List<Query<S>> getImportantSubqueries();
}
