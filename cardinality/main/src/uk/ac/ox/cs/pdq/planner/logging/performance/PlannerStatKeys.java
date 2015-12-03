package uk.ac.ox.cs.pdq.planner.logging.performance;

import uk.ac.ox.cs.pdq.logging.performance.StatKey;

import com.google.common.base.CaseFormat;


/**
 * Static collection of key to be used in planning statistics collections.
 *
 * @author Julien Leblay
 */
public enum PlannerStatKeys implements StatKey {

	TIMESTAMP, ROUNDS, ROUND_FIRST_MATCH, ROUND_BEST_MATCH,
	MILLI_TOTAL, MILLI_FIRST_MATCH, MILLI_BEST_MATCH,
	MILLI_CLOSE, MILLI_DETECT_CANDIDATES, MILLI_QUERY_MATCH,
	BEST_PLAN, BEST_COST, CONSTANTS, GENERATED_FACTS,
	HIGHER_COST_PRUNING, DOMINANCE_PRUNING, EQUIVALENCE_PRUNING,
	FILTERED,
	MILLI_DOMINANCE, MILLI_EQUIVALENCE, MILLI_REASONING,
	MILLI_UPDATE, MILLI_SELECT_IC, MILLI_UPDATE_QUERY_DEPENDENCIES, MILLI_BLOCKING_CHECK,
	CONFIGURATIONS, CANDIDATES, CUMULATED_CANDIDATES, ITERATION_TIME,
	EQUIVALENCE_CLASSES, AVG_EQUIVALENCE_CLASSES, MED_EQUIVALENCE_CLASSES;

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());
	}
	
}
