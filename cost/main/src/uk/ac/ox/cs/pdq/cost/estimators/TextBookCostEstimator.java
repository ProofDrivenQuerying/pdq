package uk.ac.ox.cs.pdq.cost.estimators;


import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_TIME;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;


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
 * @param <P> the generic type
 */
public class TextBookCostEstimator implements OrderDependentCostEstimator {

	/** The stats. */
	protected final StatisticsCollector stats;

	/** The card estimator. */
	protected final CardinalityEstimator cardEstimator;

	/**
	 * Constructor.
	 *
	 * @param stats the stats
	 * @param ce CardinalityEstimator
	 */
	public TextBookCostEstimator(StatisticsCollector stats, CardinalityEstimator ce) {
		this.stats = stats;
		this.cardEstimator = ce;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TextBookCostEstimator clone() {
		return (TextBookCostEstimator) (this.stats == null ? new TextBookCostEstimator(null,  this.cardEstimator.clone()) : new TextBookCostEstimator(this.stats.clone(),  this.cardEstimator.clone()));
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
	 * @param descendants the descendants
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
	 * Cost.
	 *
	 * @param plan P
	 * @return DoubleCost
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#cost(P)
	 */
	@Override
	public Cost cost(RelationalTerm plan) {
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		if (plan.isClosed())
			this.cardEstimator.estimateCardinality(plan);
		DoubleCost result = new DoubleCost(this.recursiveCost(plan));
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		return result;
	}
}