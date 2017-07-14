package uk.ac.ox.cs.pdq.cost.estimators;


import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.cost.RelationalTermCardinalityMetadata;

// TODO: Auto-generated Javadoc
/**
 * Computes the estimated input and output cardinalities of a logical operator
 * and its descendants, for non-error-prone operators (e.g. aggregates, cross
 * products and static operators).
 *
 * @author Julien Leblay
 */
public abstract class AbstractCardinalityEstimator implements CardinalityEstimator {

	private Map<RelationalTerm,RelationalTermCardinalityMetadata> cardinalityMetadata;
	
	/**
	 * Clone.
	 *
	 * @return AbstractCardinalityEstimator<M>
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator#clone()
	 */
	@Override
	public abstract AbstractCardinalityEstimator clone();

	/**
	 * Estimate if needed.
	 *
	 * @param term LogicalOperator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator#estimateIfNeeded(RelationalOperator)
	 */
	@Override
	public void estimateIfNeeded(RelationalTerm term) {
		RelationalTermCardinalityMetadata metadata = this.getMetadata(term);
		if (metadata.getOutputCardinality() < 0) {
			this.estimate(term);
		}
	}

	/**
	 * Estimate.
	 *
	 * @param term LogicalOperator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator#estimate(RelationalOperator)
	 */
	@Override
	public void estimate(RelationalTerm term) {
		synchronized (term) {
			Double output = -1.0;
			RelationalTermCardinalityMetadata metadata = this.getMetadata(term);
			Double input = metadata.getInputCardinality();
			RelationalTerm parent = metadata.getParent();
			if (input < 0) {
				input = 0.0;
				if (parent != null) {
					input = this.getMetadata(parent).getInputCardinality();
				}
				throw new IllegalStateException("Inconsistent input cardinality '" + input + "' for " + term);
			}

			// For Scan, Access, Distinct, Union, Selection and Join
			// The estimation is delegated to specialised estimators.
			if (term instanceof JoinTerm) {
				output = this.estimateOutput((JoinTerm) term);
			} 
			else if (term instanceof AccessTerm) {
				output = this.estimateOutput((AccessTerm) term);
			} 
			else if (term instanceof SelectionTerm) {
				output = this.estimateOutput((SelectionTerm) term);
			} 
			else if (term instanceof ProjectionTerm) {
				RelationalTerm child = ((ProjectionTerm) term).getChildren()[0];
				RelationalTermCardinalityMetadata childMetadata = this.getMetadata(child);
				childMetadata.setParent(term);
				childMetadata.setInputCardinality(input);
				this.estimateIfNeeded(child);
				output = childMetadata.getOutputCardinality();
			} 
			// Cross Products: cardinality is the product of the children's cardinalities
			else if (term instanceof CartesianProductTerm) {
				output = 1.0;
				for (RelationalTerm child: term.getChildren()) {
					RelationalTermCardinalityMetadata childMetadata = this.getMetadata(child);
					childMetadata.setParent(term);
					childMetadata.setInputCardinality(input);
					this.estimateIfNeeded(child);
					output *= childMetadata.getOutputCardinality();
				}
			} 
			metadata.setInputCardinality(input);
			metadata.setOutputCardinality(output);
		}
	}
	
	/**
	 * Gets the metadata.
	 *
	 * @param o LogicalOperator
	 * @return M
	 */
	@Override
	public RelationalTermCardinalityMetadata getMetadata(RelationalTerm o) {
		RelationalTermCardinalityMetadata result = this.cardinalityMetadata.get(o);
		if (result == null) {
			this.cardinalityMetadata.put(o, this.initMetadata(o));
		}
		return result;
	}

	/**
	 * Inits the metadata.
	 *
	 * @param o the operator
	 * @return the dataguide associated with this operator. This cannot be null,
	 * i.e. implementation should returned a fresh dataguide object if none
	 * already existed on the given operator.
	 */
	protected abstract RelationalTermCardinalityMetadata initMetadata(RelationalTerm o);

	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a Access operator
	 */
	protected abstract Double estimateOutput(AccessTerm o);

	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a Join operator
	 */
	protected abstract Double estimateOutput(JoinTerm o);
	
	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a Join operator
	 */
	protected abstract Double estimateOutput(DependentJoinTerm o);

	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a Selection operator
	 */
	protected abstract Double estimateOutput(SelectionTerm o);
}
