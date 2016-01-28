
package uk.ac.ox.cs.pdq.cost.statistics;


import java.util.Collection;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Query;

/**
 * The database statistics 
 * @author Efthymia Tsamoura
 *
 */
public interface Catalog {
	
	/**
	 * 
	 * @param relation
	 * @param attribute
	 * @param constant
	 * @return	
	 * 		the number of tuples in relation which satisfy relation.attribute=constant divided by |relation|.
	 * 
	 */
	public Double getSelectivity(Relation relation, Attribute attribute, TypedConstant<?> constant);
	
	/**
	 * 
	 * @param relation
	 * @param attribute
	 * @param constant
	 * @return	
	 * 		the number of tuples in relation which satisfy relation.attribute=constant
	 * 
	 */
	public int getSize(Relation relation, Attribute attribute, TypedConstant<?> constant);
	
	/**
	 * 
	 * @param relation
	 * @return
	 * 		the cardinality of the input relation
	 */
	int getCardinality(Relation relation);
	
	/**
	 * 
	 * @param relation
	 * @param attribute
	 * @return
	 * 		the cardinality associated to the input attribute
	 */
	int getCardinality(Relation relation, Attribute attribute);
	
	
	/**
	 * 
	 * @param access
	 * @return the estimated result size per invocation of the input access
	 */
	int getERPSI(Relation relation, AccessMethod method);
	
	/**
	 * 
	 * @param access
	 * @return the estimated result size per invocation of the input access
	 */
	int getERPSI(Relation relation, AccessMethod method, Map<Integer, TypedConstant<?>> inputs);
		
	/**
	 * 
	 * @param access
	 * @return the estimated cost of the input access
	 */
	double getCost(Relation relation, AccessMethod method);
	
	/**
	 * 
	 * @param access
	 * @return the estimated cost of the input access
	 */
	double getCost(Relation relation, AccessMethod method, Map<Integer, TypedConstant<?>> inputs);
	
	/**
	 * 
	 * @return
	 * 		the schema statistics in form of queries 
	 */
	Collection<Query<?>> getStatisticsExpressions();
	
	
	/**
	 * 
	 * @param relation
	 * @param attribute
	 * @return
	 * 		the histogram of the input relation attribute pair
	 */
	Histogram getHistogram(Relation relation, Attribute attribute);
	
	/**
	 * 
	 * @param relation
	 * @return
	 * 		the quality of size estimate of the input relation
	 */
	double getQuality(Relation relation);
	
	
	Catalog clone();
}
