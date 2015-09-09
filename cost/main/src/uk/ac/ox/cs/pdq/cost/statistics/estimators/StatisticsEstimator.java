package uk.ac.ox.cs.pdq.cost.statistics.estimators;

import uk.ac.ox.cs.pdq.plan.Plan;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public interface StatisticsEstimator {
	/**
	 * @param plan
	 * 		A plan
	 * @return
	 * 		its output size 
	 */
	double cardinality(Plan plan);
	
	StatisticsEstimator clone();
}
