package uk.ac.ox.cs.pdq.reasoning.logging.performance;

import uk.ac.ox.cs.pdq.logging.performance.StatKey;

import com.google.common.base.CaseFormat;


/**
 * Static collection of keys to be used in reasoning statistics collections.
 *
 * @author Efthymia Tsamoura
 */
public enum ReasoningStatKeys implements StatKey {

	CONSTANTS, 
	FACTS, 
	MILLI_BLOCKING_CHECK, 
	MILLI_UPDATE_QUERY_DEPENDENCIES, 
	MILLI_UPDATE;

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());
	}
	
}
