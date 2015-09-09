package uk.ac.ox.cs.pdq.fol;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.TypedConstant;

/**
 * Query interface
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface Query<S extends Formula> extends Formula, Evaluatable, Rule<S, Predicate> {

	/**
	 * @return true if the query is boolean
	 */
	boolean isBoolean();

	/**
	 *
	 * @return the query's body 
	 * @see uk.ac.ox.cs.pdq.formula.Evaluatable#getBody()
	 */
	@Override
	S getBody();

	/**
	 * @return the query's head 
	 * @see uk.ac.ox.cs.pdq.formula.Rule#getHead()
	 */
	@Override
	Predicate getHead();

	/**
	 * @return the constants that appear in the query's body
	 */
	Collection<TypedConstant<?>> getSchemaConstants();
	

	/**
	 * @return a map of query's free variables to its canonical constants.
	 */
	Map<Variable, Constant> getFree2Canonical();
	
	/**
	 * @return a map of query's variables both free and quantified to chase constants appear in the canonical query
	 */
	Map<Variable, Constant> getVariables2Canonical();

	/**
	 *
	 * @param mapping Map<Variable,Constant>
	 * @return a copy of the query grounded using the given mapping
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Constant>)
	 */
	@Override
	Formula ground(Map<Variable, Constant> mapping);

	/**
	 *
	 * @return the canonical query
	 */
	S getCanonical();
	
	void setGrounding(Map<Variable, Constant> grounding);
	
	/**
	 * @return any sub-query of the query for any combination of free/bound variables
	 */
	List<Query<S>> getImportantSubqueries();
}
