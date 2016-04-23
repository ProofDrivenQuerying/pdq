/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.logging.performance;

import uk.ac.ox.cs.pdq.logging.performance.StatKey;

import com.google.common.base.CaseFormat;


// TODO: Auto-generated Javadoc
/**
 * Static collection of key to be used in planning statistics collections.
 *
 * @author Julien Leblay
 */
public enum PlannerStatKeys implements StatKey {

	/** The timestamp. */
	TIMESTAMP, /** The rounds. */
 ROUNDS, /** The round first match. */
 ROUND_FIRST_MATCH, /** The round best match. */
 ROUND_BEST_MATCH,
	
	/** The milli total. */
	MILLI_TOTAL, 
 /** The milli first match. */
 MILLI_FIRST_MATCH, 
 /** The milli best match. */
 MILLI_BEST_MATCH,
	
	/** The milli close. */
	MILLI_CLOSE, 
 /** The milli detect candidates. */
 MILLI_DETECT_CANDIDATES, 
 /** The milli query match. */
 MILLI_QUERY_MATCH,
	
	/** The best plan. */
	BEST_PLAN, 
 /** The best cost. */
 BEST_COST, 
 /** The constants. */
 CONSTANTS, 
 /** The generated facts. */
 GENERATED_FACTS,
	
	/** The higher cost pruning. */
	HIGHER_COST_PRUNING, 
 /** The dominance pruning. */
 DOMINANCE_PRUNING, 
 /** The equivalence pruning. */
 EQUIVALENCE_PRUNING,
	
	/** The filtered. */
	FILTERED,
	
	/** The milli dominance. */
	MILLI_DOMINANCE, 
 /** The milli equivalence. */
 MILLI_EQUIVALENCE, 
 /** The milli reasoning. */
 MILLI_REASONING,
	
	/** The milli update. */
	MILLI_UPDATE, 
 /** The milli select ic. */
 MILLI_SELECT_IC, 
 /** The milli update query dependencies. */
 MILLI_UPDATE_QUERY_DEPENDENCIES, 
 /** The milli blocking check. */
 MILLI_BLOCKING_CHECK,
	
	/** The configurations. */
	CONFIGURATIONS, 
 /** The candidates. */
 CANDIDATES, 
 /** The cumulated candidates. */
 CUMULATED_CANDIDATES, 
 /** The iteration time. */
 ITERATION_TIME,
	
	/** The equivalence classes. */
	EQUIVALENCE_CLASSES, 
 /** The avg equivalence classes. */
 AVG_EQUIVALENCE_CLASSES, 
 /** The med equivalence classes. */
 MED_EQUIVALENCE_CLASSES;

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());
	}
	
}
