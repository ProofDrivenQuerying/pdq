// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.db.Attribute;


/**
 * A whitebox cost estimation implementation.
 * According to this, the cost of a plan equals the number of the accesses.
 * Based on cost estimation as defined in the "Database Management System" by
 * Ramakrishnan and Gehrke.
 * 
 * Roughly speaking, the cost of a plan is the sum of the cost of its sub-plans,
 * plus it own IO cost multiple by an estimation of its cardinality.
 *
 * @author Julien Leblay
 * @contributor Brandon Moore
 * @param <P> the generic type
 */
public class TextBookCostEstimator implements OrderDependentCostEstimator {

	/** The card estimator. */
	protected final CardinalityEstimator cardEstimator;

	/**
	 * Constructor.
	 *
	 * @param ce CardinalityEstimator
	 */
	public TextBookCostEstimator(CardinalityEstimator ce) {
		this.cardEstimator = ce;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TextBookCostEstimator clone() {
		return (TextBookCostEstimator) new TextBookCostEstimator(this.cardEstimator.clone());
	}

	/**
	 * Gets the cardinality estimator.
	 *
	 * @return CardinalityEstimator
	 */
	public CardinalityEstimator getCardinalityEstimator() {
		return this.cardEstimator;
	}

	/**
	 * Recursively computes the cost of the given operator.
	 *
	 * @param logOp the log op
	 * @return the cost of the given operator.
	 */
	private double recursiveCost(RelationalTerm logOp) {
		double subCost = 0;
		double inputCard = this.cardEstimator.getCardinalityMetadata(logOp).getInputCardinality();
		double card = Math.max(1.0, this.cardEstimator.getCardinalityMetadata(logOp).getOutputCardinality());
		double localCost = Math.max(0.0, card * perCostPerOutputTuple(logOp));
		if(logOp instanceof AccessTerm) {
			localCost *= Math.max(1.0, Math.log(this.cardEstimator.getCardinalityMetadata(logOp).getOutputCardinality()));
		}
		else if (logOp instanceof ProjectionTerm || logOp instanceof RenameTerm || logOp instanceof SelectionTerm) 
			subCost = this.recursiveCost(logOp.getChild(0));
		else if (logOp instanceof DependentJoinTerm) {
			RelationalTerm leftOp = logOp.getChild(0);
			subCost += this.recursiveCost(leftOp);
			double leftInputCard = this.cardEstimator.getCardinalityMetadata(leftOp).getOutputCardinality();
			double rSubCost = this.recursiveCost(logOp.getChild(1));
			subCost += Math.max(rSubCost, (rSubCost / Math.max(1.0, inputCard)) * leftInputCard);
		} 
		else if (logOp instanceof JoinTerm) {
			int interCard = 1;
			for (RelationalTerm child: logOp.getChildren()) {
				subCost += interCard * this.recursiveCost(child);
				interCard *= Math.max(1.0, this.cardEstimator.getCardinalityMetadata(child).getOutputCardinality());
			}
		}
		else if (logOp instanceof DependentJoinTerm) {
			RelationalTerm leftOp = ((DependentJoinTerm) logOp).getChild(0);
			subCost += this.recursiveCost(leftOp);
			double leftInputCard = this.cardEstimator.getCardinalityMetadata(leftOp).getOutputCardinality();
			double rSubCost = this.recursiveCost(((DependentJoinTerm) logOp).getChild(1));
			subCost += Math.max(rSubCost, (rSubCost / Math.max(1.0, inputCard)) * leftInputCard);
		}
		else 
			throw new RuntimeException("Unknown relational term type");
		return subCost + localCost;
	}

	/**
	 * Per output tuple cost.
	 *
	 * @param o the o
	 * @return the per-output tuple I/O cost of the given operator.
	 */
	private static Double perCostPerOutputTuple(RelationalTerm o) {
		if(o instanceof JoinTerm) {
			Condition predicate = ((JoinTerm) o).getJoinConditions();
			if (predicate instanceof SimpleCondition) 
				return 1.0;
			else if (predicate instanceof ConjunctiveCondition) 
				return (double) ((ConjunctiveCondition) predicate).getNumberOfConjuncts();
			return 1.0;
		}
		else if(o instanceof DependentJoinTerm) {
			Condition predicate = ((DependentJoinTerm) o).getJoinConditions();
			if (predicate instanceof SimpleCondition) 
				return 1.0;
			else if (predicate instanceof ConjunctiveCondition) 
				return (double) ((ConjunctiveCondition) predicate).getNumberOfConjuncts();
			return 1.0;
		}
		else if(o instanceof SelectionTerm) {
			Condition predicate = ((SelectionTerm) o).getSelectionCondition();
			if (predicate instanceof SimpleCondition) 
				return 1.0;
			else if (predicate instanceof ConjunctiveCondition) 
				return (double) ((ConjunctiveCondition) predicate).getNumberOfConjuncts();
			return 1.0;
		}
		else if (o instanceof ProjectionTerm) {
			ProjectionTerm p = ((ProjectionTerm) o);
			RelationalTerm child = p.getChild(0);
			Attribute[] projected = p.getOutputAttributes();
			if (projected.length == child.getOutputAttributes().length) 
				return 1.0;
			else 
				return (projected.length / (double) child.getNumberOfOutputAttributes());
		}
		if (o instanceof RenameTerm) {
			return 1.0;
		}
		return (double) o.getNumberOfOutputAttributes();
	}

	/**
	 * 
	 *
	 * @param plan P
	 * @return DoubleCost
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#cost(RelationalTerm)
	 */
	@Override
	public Cost cost(RelationalTerm plan) {
		if (plan.isClosed())
			this.cardEstimator.estimateCardinality(plan);
		DoubleCost result = new DoubleCost(this.recursiveCost(plan));
		return result;
	}
}