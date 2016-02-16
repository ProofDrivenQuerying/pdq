package uk.ac.ox.cs.pdq.cost.estimators;

import static uk.ac.ox.cs.pdq.cost.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.CostStatKeys.COST_ESTIMATION_TIME;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.Access;
import uk.ac.ox.cs.pdq.algebra.Count;
import uk.ac.ox.cs.pdq.algebra.DependentJoin;
import uk.ac.ox.cs.pdq.algebra.IsEmpty;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.NaryOperator;
import uk.ac.ox.cs.pdq.algebra.PredicateBasedOperator;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.StaticInput;
import uk.ac.ox.cs.pdq.algebra.SubPlanAlias;
import uk.ac.ox.cs.pdq.algebra.UnaryOperator;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.plan.Plan;

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
public class WhiteBoxCostEstimator<P extends Plan> implements BlackBoxCostEstimator<P> {

	/** The stats. */
	protected final StatisticsCollector stats;

	/** The card estimator. */
	protected final CardinalityEstimator cardEstimator;

	/**
	 * Default constructor.
	 *
	 * @param ce CardinalityEstimator
	 */
	public WhiteBoxCostEstimator(CardinalityEstimator ce) {
		this(null, ce);
	}

	/**
	 * Constructor.
	 *
	 * @param stats the stats
	 * @param ce CardinalityEstimator
	 */
	public WhiteBoxCostEstimator(StatisticsCollector stats, CardinalityEstimator ce) {
		this.stats = stats;
		this.cardEstimator = ce;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public WhiteBoxCostEstimator<P> clone() {
		return (WhiteBoxCostEstimator<P>) (this.stats == null ? new WhiteBoxCostEstimator<>(null,  this.cardEstimator.clone()) : new WhiteBoxCostEstimator<>(this.stats.clone(),  this.cardEstimator.clone()));
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
		//		if (logOp instanceof AccessOperator) {
		//			localCost += Math.max(1.0, inputCard) * perInputTupleCost(logOp);
		//		}
		if (logOp instanceof SubPlanAlias) {
			Plan subPlan = ((SubPlanAlias) logOp).getPlan();
			Cost aliasCost = new DoubleCost(Double.POSITIVE_INFINITY);
			if (subPlan != null) {
				aliasCost = subPlan.getCost();
				if (aliasCost == null || aliasCost.isUpperBound()) {
					aliasCost = new DoubleCost(this.recursiveCost((RelationalOperator) subPlan.getOperator()));
				}
				subPlan.setCost(aliasCost);
			}
			return aliasCost.getValue().doubleValue();

		} else if (logOp instanceof UnaryOperator) {
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
	private static Double perOutputTupleCost(RelationalOperator o) {
		if (o instanceof PredicateBasedOperator) {
			Predicate predicate = ((PredicateBasedOperator) o).getPredicate();
			if (predicate instanceof ConjunctivePredicate) {
				return (double) ((ConjunctivePredicate) predicate).size();
			}
			return 1.0;
		}
		if (o instanceof Projection) {
			Projection p = ((Projection) o);
			RelationalOperator child = p.getChild();
			List<Term> projected = p.getColumns();
			Map<Integer, Term> renaming = p.getRenaming();
			if (renaming != null && projected.equals(child.getColumns())) {
				return 1.0;
			}
			return (projected.size() / (double) child.getColumns().size());
		}
		if (o instanceof SubPlanAlias) {
			Plan subPlan = ((SubPlanAlias) o).getPlan();
			if (subPlan != null) {
				return perOutputTupleCost((RelationalOperator) subPlan.getOperator());
			}
			return null;
		}
		if (o instanceof Count || o instanceof IsEmpty) {
			return 1.0;
		}
		if (o instanceof StaticInput) {
			return 0.0;
		}
		return (double) o.getColumns().size();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#estimateCost(uk.ac.ox.cs.pdq.util.Costable)
	 */
	@Override
	public Cost estimateCost(P plan) {
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		DoubleCost result = new DoubleCost(this.recursiveCost(plan));
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
//		this.planIndex.update(plan);
		return result;
	}
	
	/**
	 * Cost.
	 *
	 * @param plan P
	 * @return DoubleCost
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#cost(P)
	 */
	@Override
	public Cost cost(P plan) {
		plan.setCost(this.estimateCost(plan));
		return plan.getCost();
	}
}
