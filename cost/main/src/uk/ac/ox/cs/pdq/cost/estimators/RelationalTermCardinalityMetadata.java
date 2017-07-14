package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

// TODO: Auto-generated Javadoc
/**
 * Interface providing cardinality estimations for plan operators.
 *
 * @author Julien Leblay
 * @param <P> the generic type
 */
 //Efi: This class should be moved to the cost package 
public interface RelationalTermCardinalityMetadata {

	/**
	 * Gets the parent.
	 *
	 * @return LogicalOperator
	 */
	RelationalTerm getParent();
	
	/**
	 * Sets the parent.
	 *
	 * @param parent LogicalOperator
	 */
	void setParent(RelationalTerm parent);
	
	/**
	 * Gets the input cardinality.
	 *
	 * @return Double
	 */
	Double getInputCardinality();
	
	/**
	 * Gets the output cardinality.
	 *
	 * @return Double
	 */
	Double getOutputCardinality();
	
	/**
	 * Sets the input cardinality.
	 *
	 * @param l Double
	 */
	void setInputCardinality(Double l);
	
	/**
	 * Sets the output cardinality.
	 *
	 * @param l Double
	 */
	void setOutputCardinality(Double l);
}
