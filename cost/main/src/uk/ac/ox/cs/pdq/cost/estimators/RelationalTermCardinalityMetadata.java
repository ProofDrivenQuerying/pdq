package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

/**
 * Interface providing cardinality estimations for plan operators.
 *
 * @author Julien Leblay
 * @param <P> the generic type
 */
public interface RelationalTermCardinalityMetadata {

	/**
	 * 
	 *
	 * @return LogicalOperator
	 */
	RelationalTerm getParent();
	
	/**
	 * 
	 *
	 * @param parent LogicalOperator
	 */
	void setParent(RelationalTerm parent);
	
	/**
	 * 
	 *
	 * @return Double
	 */
	Double getInputCardinality();
	
	/**
	 * 
	 *
	 * @return Double
	 */
	Double getOutputCardinality();
	
	/**
	 * 
	 *
	 * @param l Double
	 */
	void setInputCardinality(Double l);
	
	/**
	 * 
	 *
	 * @param l Double
	 */
	void setOutputCardinality(Double l);
}
