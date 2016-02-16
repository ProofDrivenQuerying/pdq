package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.plan.EstimateProvider;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * The Class NaiveMetadata.
 *
 * @author Julien Leblay
 */
public class NaiveMetadata implements EstimateProvider<RelationalOperator> {

	/** The input card. */
	private double inputCard = 0L;
	
	/** The output card. */
	private double outputCard = -1L;
	
	/** The parent. */
	private RelationalOperator parent;

	/**
	 * Gets the parent.
	 *
	 * @return LogicalOperator
	 * @see uk.ac.ox.cs.pdq.plan.EstimateProvider#getParent()
	 */
	@Override
	public RelationalOperator getParent() {
		return this.parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param o LogicalOperator
	 * @see uk.ac.ox.cs.pdq.plan.EstimateProvider#setParent(RelationalOperator)
	 */
	@Override
	public void setParent(RelationalOperator o) {
		this.parent = o;
	}

	/**
	 * Gets the input cardinality.
	 *
	 * @return the last estimated input cardinality of the operator
	 * @see uk.ac.ox.cs.pdq.plan.EstimateProvider#getInputCardinality()
	 */
	@Override
	public final Double getInputCardinality() {
		return this.inputCard;
	}

	/**
	 * Gets the output cardinality.
	 *
	 * @return the last estimated output cardinality of the operator
	 * @see uk.ac.ox.cs.pdq.plan.EstimateProvider#getOutputCardinality()
	 */
	@Override
	public final Double getOutputCardinality() {
		return this.outputCard;
	}

	/**
	 * Sets the input cardinality.
	 *
	 * @param l Double
	 * @see uk.ac.ox.cs.pdq.plan.EstimateProvider#setInputCardinality(Double)
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
	 * @see uk.ac.ox.cs.pdq.plan.EstimateProvider#setOutputCardinality(Double)
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
}