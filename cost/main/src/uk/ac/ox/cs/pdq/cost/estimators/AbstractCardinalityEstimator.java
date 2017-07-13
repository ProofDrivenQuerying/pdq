package uk.ac.ox.cs.pdq.cost.estimators;


import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.cost.RelationalTermCardinalityMetadata;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Computes the estimated input and output cardinalities of a logical operator
 * and its descendants, for non-error-prone operators (e.g. aggregates, cross
 * products and static operators).
 *
 * @author Julien Leblay
 * @param <M> the generic type
 */
public abstract class AbstractCardinalityEstimator implements CardinalityEstimator {

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
	 * @param logOp LogicalOperator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator#estimateIfNeeded(RelationalOperator)
	 */
	@Override
	public void estimateIfNeeded(RelationalTerm logOp) {
		RelationalTermCardinalityMetadata metadata = this.getMetadata(logOp);
		if (metadata.getOutputCardinality() < 0) {
			this.estimate(logOp);
		}
	}

	/**
	 * Estimate.
	 *
	 * @param logOp LogicalOperator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator#estimate(RelationalOperator)
	 */
	@Override
	public void estimate(RelationalTerm logOp) {
		synchronized (logOp) {
			Double output = -1.0;

			RelationalTermCardinalityMetadata metadata = this.getMetadata(logOp);
			Double input = metadata.getInputCardinality();
			RelationalTerm parent = metadata.getParent();
			if (input < 0) {
				input = 0.0;
				if (parent != null) {
					input = this.getMetadata(parent).getInputCardinality();
				}
				throw new IllegalStateException("Inconsistent input cardinality '" + input + "' for " + logOp);
			}

			// For Scan, Access, Distinct, Union, Selection and Join
			// The estimation is delegated to specialised estimators.
			if (logOp instanceof JoinTerm) {
				output = this.estimateOutput((JoinTerm) logOp);
			} 
			else if (logOp instanceof AccessTerm) {
				Preconditions.checkState(input == 0, "Input cardinality for a Scan can only by 0");
				output = this.estimateOutput((AccessTerm) logOp);
			} 
//			else if (logOp instanceof Access) {
//				Preconditions.checkState(input == 0, "Input cardinality for a non-dependent Access can only by 0");
//				output = this.estimateOutput((Access) logOp);
//			} 
//			else if (logOp instanceof DependentAccess) {
//				output = this.estimateOutput((DependentAccess) logOp);
//			} 
			else if (logOp instanceof SelectionTerm) {
				output = this.estimateOutput((SelectionTerm) logOp);
			} 
//			else if (logOp instanceof Distinct) {
//				output = this.estimateOutput((Distinct) logOp);
//			} else if (logOp instanceof Union) {
//				output = this.estimateOutput((Union) logOp);
//			} 
			else

				// Cross Products: cardinality is the product of the children's cardinalities
				if (logOp instanceof CrossProduct) {
					output = 1.0;
					for (RelationalOperator child: ((NaryOperator) logOp).getChildren()) {
						RelationalTermCardinalityMetadata childMetadata = this.getMetadata(child);
						childMetadata.setParent(logOp);
						childMetadata.setInputCardinality(input);
						this.estimateIfNeeded(child);
						output *= childMetadata.getOutputCardinality();
					}
				} else

					// StaticInput: the cardinality is given by the tuple in the operator
					if (logOp instanceof StaticInput) {
						output = (double) ((StaticInput) logOp).getTuples().size();
					} else

						// SubPlanAlias: the cardinality is given directly by the operator after computing if needed.
						if (logOp instanceof SubPlanAlias) {
							RelationalOperator alias = (((SubPlanAlias) logOp).getPlan()).getOperator();
							RelationalTermCardinalityMetadata aliasMetadata = this.getMetadata(alias);
							aliasMetadata.setInputCardinality(input);
							this.estimateIfNeeded(alias);
							input = aliasMetadata.getInputCardinality();
							output = aliasMetadata.getOutputCardinality();
						} else

							// IsEmpty || Count: the cardinality is always 1.
							if (logOp instanceof IsEmpty || logOp instanceof Count) {
								output = 1.0;
							} else

								// Arbitrary unary operators: the cardinalities are unchanged from the child.
								if (logOp instanceof UnaryOperator) {
									RelationalOperator child = ((UnaryOperator) logOp).getChild();
									RelationalTermCardinalityMetadata childMetadata = this.getMetadata(child);
									childMetadata.setParent(logOp);
									childMetadata.setInputCardinality(input);
									this.estimateIfNeeded(child);
									output = childMetadata.getOutputCardinality();
								} else {
									throw new IllegalStateException("Unsupported operator " + logOp.getClass().getSimpleName() + " for cardinality estimation.");
								}

			metadata.setInputCardinality(input);
			metadata.setOutputCardinality(output);
		}
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
	 * Gets the metadata.
	 *
	 * @param o LogicalOperator
	 * @return M
	 */
	protected RelationalTermCardinalityMetadata getMetadata(RelationalTerm o) {
		M result = (M) o.getMetadata();
		if (result == null) {
			o.setMetadata((result = this.initMetadata(o)));
		}
		return result;
	}

	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a Scan operator
	 */
	protected abstract Double estimateOutput(Scan o);

	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a Access operator
	 */
	protected abstract Double estimateOutput(Access o);

	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a DependentAccess operator
	 */
	protected abstract Double estimateOutput(DependentAccess o);

	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a Join operator
	 */
	protected abstract Double estimateOutput(Join o);

	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a Selection operator
	 */
	protected abstract Double estimateOutput(Selection o);

	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a Distinct operator
	 */
	protected abstract Double estimateOutput(Distinct o);

	/**
	 * Estimate output.
	 *
	 * @param o the operator
	 * @return the estimated output cardinality of a Union operator
	 */
	protected abstract Double estimateOutput(Union o);
}
