
package uk.ac.ox.cs.pdq.cost.statistics;


import java.util.Map;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;

// TODO: Auto-generated Javadoc
/**
 * The database statistics .
 *
 * @author Efthymia Tsamoura
 */
public interface Catalog {
	
	/**
	 * Gets the selectivity.
	 *
	 * @param relation the relation
	 * @param attribute the attribute
	 * @param constant the constant
	 * @return 		the number of tuples in relation which satisfy relation.attribute=constant divided by |relation|.
	 */
	public Double getSelectivity(Relation relation, Attribute attribute, TypedConstant constant);
	
	/**
	 * Gets the size.
	 *
	 * @param relation the relation
	 * @param attribute the attribute
	 * @param constant the constant
	 * @return 		the number of tuples in relation which satisfy relation.attribute=constant
	 */
	public int getSize(Relation relation, Attribute attribute, TypedConstant constant);
	
	/**
	 * Gets the cardinality.
	 *
	 * @param relation the relation
	 * @return 		the cardinality of the input relation
	 */
	int getCardinality(Relation relation);
	
	/**
	 * Gets the cardinality.
	 *
	 * @param relation the relation
	 * @param attribute the attribute
	 * @return 		the cardinality associated to the input attribute
	 */
	int getCardinality(Relation relation, Attribute attribute);
	
	
	/**
	 * Gets the erpsi.
	 *
	 * @param relation the relation
	 * @param method the method
	 * @return the estimated result size per invocation of the input access
	 */
	int getERPSI(Relation relation, AccessMethod method);
	
	/**
	 * Gets the erpsi.
	 *
	 * @param relation the relation
	 * @param method the method
	 * @param inputs the inputs
	 * @return the estimated result size per invocation of the input access
	 */
	int getERPSI(Relation relation, AccessMethod method, Map<Integer, TypedConstant> inputs);
		
	/**
	 * Gets the cost.
	 *
	 * @param relation the relation
	 * @param method the method
	 * @return the estimated cost of the input access
	 */
	double getCost(Relation relation, AccessMethod method);
	
	/**
	 * Gets the cost.
	 *
	 * @param relation the relation
	 * @param method the method
	 * @param inputs the inputs
	 * @return the estimated cost of the input access
	 */
	double getCost(Relation relation, AccessMethod method, Map<Integer, TypedConstant> inputs);
	
	
	/**
	 * Gets the histogram.
	 *
	 * @param relation the relation
	 * @param attribute the attribute
	 * @return 		the histogram of the input relation attribute pair
	 */
	Histogram getHistogram(Relation relation, Attribute attribute);
	
	/**
	 * Gets the quality.
	 *
	 * @param relation the relation
	 * @return 		the quality of size estimate of the input relation
	 */
	double getQuality(Relation relation);
	
	
	/**
	 * Clone.
	 *
	 * @return the catalog
	 */
	Catalog clone();
}
