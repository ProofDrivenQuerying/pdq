package uk.ac.ox.cs.pdq.cost.estimators;


import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
/* import uk.ac.ox.cs.pdq.algebra.SubPlanAlias;
 */
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;

import com.google.common.collect.Lists;


// TODO: Auto-generated Javadoc
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
public class WhiteBoxCostEstimator implements BlackBoxCostEstimator {

	/** The stats. */
	protected final StatisticsCollector stats;

	/** The card estimator. */
	protected final CardinalityEstimator cardEstimator;

	protected final Catalog catalog;

	/**
	 * Constructor.
	 *
	 * @param stats the stats
	 * @param ce CardinalityEstimator
	 */
	public WhiteBoxCostEstimator(StatisticsCollector stats, CardinalityEstimator ce, Catalog catalog) {
		this.stats = stats;
		this.cardEstimator = ce;
		this.catalog = catalog;
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see java.lang.Object#clone()
	//	 */
	//	@Override
	//	public WhiteBoxCostEstimator clone() {
	//		return (WhiteBoxCostEstimator) (this.stats == null ? new WhiteBoxCostEstimator(null,  this.cardEstimator.clone()) : new WhiteBoxCostEstimator<>(this.stats.clone(),  this.cardEstimator.clone()));
	//	}

	/**
	 * Gets the cardinality estimator.
	 *
	 * @return CardinalityEstimator
	 */
	public CardinalityEstimator getCardinalityEstimator() {
		return this.cardEstimator;
	}

	/**
	 * Recursively computes the cost of the given plan.
	 *
	 * @param plan the plan
	 * @return the cost of the given plan.
	 */
	private double recursiveCost(P plan) {
		if (plan instanceof DAGPlan) {
			DAGPlan dPlan = (DAGPlan) plan;
			if (!dPlan.isClosed()) {
				return Double.POSITIVE_INFINITY;
			}
			RelationalOperator lo = plan.getOperator();
			this.cardEstimator.estimate(lo);
			return this.recursiveCost(lo, dPlan.getDescendants());
		} else if (plan instanceof LeftDeepPlan) {
			RelationalOperator lo = plan.getOperator();
			this.cardEstimator.estimate(lo);
			return this.recursiveCost(lo);
		}
		throw new IllegalStateException("Unknown plan type " + plan);
	}

	/**
	 * Recursively computes the cost of the given operator, assuming an empty
	 * descendant collection and an input cardinality of 0.
	 *
	 * @param logOp the log op
	 * @return the cost of the given operator.
	 */
	public double recursiveCost(RelationalOperator logOp) {
		return this.recursiveCost(logOp, Lists.<DAGPlan>newArrayList());
	}

	/**
	 * Recursively computes the cost of the given operator.
	 *
	 * @param logOp the log op
	 * @param descendants the descendants
	 * @return the cost of the given operator.
	 */
	private double recursiveCost(RelationalOperator logOp, Collection<DAGPlan> descendants) {
		if (descendants != null) {
			for (DAGPlan child: descendants) {
				if (child.getOperator().equals(logOp)) {
					Double result = (Double) child.getCost().getValue();
					if (result != Double.POSITIVE_INFINITY) {
						return result;
					}
				}
			}
		}

		double subCost = 0;
		double inputCard = logOp.getMetadata().getInputCardinality();
		double card = Math.max(1.0, logOp.getMetadata().getOutputCardinality());
		double localCost =
				Math.max(0.0, card * perOutputTupleCost(logOp));
//		if (logOp instanceof SubPlanAlias) {
//			Plan subPlan = ((SubPlanAlias) logOp).getPlan();
//			Cost aliasCost = new DoubleCost(Double.POSITIVE_INFINITY);
//			if (subPlan != null) {
//				aliasCost = subPlan.getCost();
//				if (aliasCost == null || aliasCost.isUpperBound()) {
//					aliasCost = new DoubleCost(this.recursiveCost((RelationalOperator) subPlan.getOperator()));
//				}
//				subPlan.setCost(aliasCost);
//			}
//			return aliasCost.getValue().doubleValue();
//
//		} else 
		if (logOp instanceof UnaryOperator) {
			RelationalOperator child = ((UnaryOperator) logOp).getChild();
			if (logOp instanceof Access) {
				if (child != null) {
					localCost *= Math.max(1.0, Math.log(child.getMetadata().getOutputCardinality()));
				} else {
					return Double.POSITIVE_INFINITY;
				}
			}
			subCost = this.recursiveCost(child, descendants);

		} else if (logOp instanceof NaryOperator) {
			int interCard = 1;
			if (logOp instanceof DependentJoin) {
				RelationalOperator leftOp = ((DependentJoin) logOp).getLeft();
				subCost += this.recursiveCost(leftOp, descendants);
				double leftInputCard = leftOp.getMetadata().getOutputCardinality();
				double rSubCost = this.recursiveCost(((DependentJoin) logOp).getRight(), descendants);
				subCost += Math.max(rSubCost, (rSubCost / Math.max(1.0, inputCard)) * leftInputCard);
			} else {
				for (RelationalOperator child: ((NaryOperator) logOp).getChildren()) {
					if (logOp instanceof Join && ((Join) logOp).hasPredicate()) {
						switch (((Join) logOp).getVariant()) {
						case ASYMMETRIC_HASH:
						case SYMMETRIC_HASH:
						case MERGE:
							subCost += this.recursiveCost(child, descendants);
							continue;
						}
					}
					subCost += interCard * this.recursiveCost(child, descendants);
					interCard *= Math.max(1.0, child.getMetadata().getOutputCardinality());
				}
			}
		}
		return subCost + localCost;
	}

	/**
	 * Per output tuple cost.
	 *
	 * @param o the o
	 * @return the per-output tuple I/O cost of the given operator.
	 */
	private static Double perOutputTupleCost(RelationalTerm o) {
		if(o instanceof JoinTerm) {
			Condition predicate = ((JoinTerm) o).getPredicate();
			if (predicate instanceof SimpleCondition) 
				return 1.0;
			else if (predicate instanceof ConjunctiveCondition) 
				return (double) ((ConjunctiveCondition) predicate).getNumberOfConjuncts();
			return 1.0;
		}
		else if(o instanceof DependentJoinTerm) {
			Condition predicate = ((DependentJoinTerm) o).getPredicate();
			if (predicate instanceof SimpleCondition) 
				return 1.0;
			else if (predicate instanceof ConjunctiveCondition) 
				return (double) ((ConjunctiveCondition) predicate).getNumberOfConjuncts();
			return 1.0;
		}
		else if(o instanceof SelectionTerm) {
			Condition predicate = ((SelectionTerm) o).getPredicate();
			if (predicate instanceof SimpleCondition) 
				return 1.0;
			else if (predicate instanceof ConjunctiveCondition) 
				return (double) ((ConjunctiveCondition) predicate).getNumberOfConjuncts();
			return 1.0;
		}
		else if (o instanceof ProjectionTerm) {
			ProjectionTerm p = ((ProjectionTerm) o);
			RelationalTerm child = p.getChildren()[0];
			Attribute[] projected = p.getOutputAttributes();
			if (projected.length == child.getOutputAttributes().length) 
				return 1.0;
			else 
				return (projected.length / (double) child.getNumberOfOutputAttributes());
		}
		if (o instanceof RenameTerm) {
			RenameTerm p = ((RenameTerm) o);
			RelationalTerm child = p.getChildren()[0];
			Attribute[] projected = p.getOutputAttributes();
			if (projected.length == child.getOutputAttributes().length) 
				return 1.0;
			return (projected.length / (double) child.getNumberOfOutputAttributes());
		}
		//		if (o instanceof SubPlanAlias) {
		//			Plan subPlan = ((SubPlanAlias) o).getPlan();
		//			if (subPlan != null) {
		//				return perOutputTupleCost((RelationalOperator) subPlan.getOperator());
		//			}
		//			return null;
		//		}
		//		if (o instanceof Count || o instanceof IsEmpty) {
		//			return 1.0;
		//		}
		//		if (o instanceof StaticInput) {
		//			return 0.0;
		//		}
		return (double) o.getNumberOfOutputAttributes();
	}

	//	/* (non-Javadoc)
	//	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#estimateCost(uk.ac.ox.cs.pdq.util.Costable)
	//	 */
	//	@Override
	//	public Cost estimateCost(P plan) {
	//		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
	//		DoubleCost result = new DoubleCost(this.recursiveCost(plan));
	//		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
	//		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
	////		this.planIndex.update(plan);
	//		return result;
	//	}

	/**
	 * Cost.
	 *
	 * @param plan P
	 * @return DoubleCost
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#cost(P)
	 */
	@Override
	public Cost cost(P plan) {
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		DoubleCost result = new DoubleCost(this.recursiveCost(plan));
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		//		this.planIndex.update(plan);
		return result;
	}
}