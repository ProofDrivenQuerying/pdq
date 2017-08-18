package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * The Class NaiveMetadata.
 *
 * @author Julien Leblay
 */
public class NaiveRelationalTermCardinalityMetadata implements RelationalTermCardinalityMetadata {

	/** The input card. */
	private double inputCard = 0L;
	
	/** The output card. */
	private double outputCard = -1L;
	
	/** The parent. */
	private RelationalTerm parent;

	/**
	 * Gets the parent.
	 *
	 * @return LogicalOperator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.RelationalTermCardinalityMetadata#getParent()
	 */
	@Override
	public RelationalTerm getParent() {
		return this.parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param o LogicalOperator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.RelationalTermCardinalityMetadata#setParent(RelationalOperator)
	 */
	@Override
	public void setParent(RelationalTerm o) {
		this.parent = o;
	}

	/**
	 * Gets the input cardinality.
	 *
	 * @return the last estimated input cardinality of the operator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.RelationalTermCardinalityMetadata#getInputCardinality()
	 */
	@Override
	public final Double getInputCardinality() {
		return this.inputCard;
	}

	/**
	 * Gets the output cardinality.
	 *
	 * @return the last estimated output cardinality of the operator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.RelationalTermCardinalityMetadata#getOutputCardinality()
	 */
	@Override
	public final Double getOutputCardinality() {
		return this.outputCard;
	}

	/**
	 * Sets the input cardinality.
	 *
	 * @param l Double
	 * @see uk.ac.ox.cs.pdq.cost.estimators.RelationalTermCardinalityMetadata#setInputCardinality(Double)
	 */
	@Override
	public final void setInputCardinality(Double l) {
		Preconditions.checkArgument(l >= 0, "Estimated input cardinality cannot be negative. " + l);
		this.inputCard = l;
	}

	/**
	 * Sets the output cardinality.
	 *
	 * @param l Double
	 * @see uk.ac.ox.cs.pdq.cost.estimators.RelationalTermCardinalityMetadata#setOutputCardinality(Double)
	 */
	@Override
	public final void setOutputCardinality(Double  l) {
		Preconditions.checkArgument(l >= 0.0, "Estimated output cardinality cannot be negative. " + l + " " + this.parent);
		this.outputCard = l;
	}

	/**
	 * Reset the estimated cardinalities, e.g. if instance is a change occurred
	 * in a descendant.
	 */
	protected void invalidateEstimatedCardinalities() {
		this.inputCard = -1L;
		this.outputCard = -1L;
	}
	
	@Override
	public String toString() {
		return "(" + this.inputCard + " " + this.outputCard + " " + "{" + this.parent + "}" + ")";
	}
}