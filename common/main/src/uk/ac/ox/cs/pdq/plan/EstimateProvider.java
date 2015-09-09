package uk.ac.ox.cs.pdq.plan;

import uk.ac.ox.cs.pdq.util.Operator;


/**
 * Interface providing cardinality estimations for plan operators.
 * @author Julien Leblay
 */
 //Efi: This class should be moved to the cost package 
public interface EstimateProvider<P extends Operator> {

	/**
	 * @return LogicalOperator
	 */
	P getParent();
	/**
	 * @param parent LogicalOperator
	 */
	void setParent(P parent);
	/**
	 * @return Double
	 */
	Double getInputCardinality();
	/**
	 * @return Double
	 */
	Double getOutputCardinality();
	/**
	 * @param l Double
	 */
	void setInputCardinality(Double l);
	/**
	 * @param l Double
	 */
	void setOutputCardinality(Double l);
}
