package uk.ac.ox.cs.pdq.materialize.logging.performance;

import uk.ac.ox.cs.pdq.logging.performance.StatKey;

import com.google.common.base.CaseFormat;


// TODO: Auto-generated Javadoc
/**
 * Static collection of keys to be used in reasoning statistics collections.
 *
 * @author Efthymia Tsamoura
 */
public enum ReasoningStatKeys implements StatKey {

	/** The constants. */
	CONSTANTS, 
	
	/** The facts. */
	FACTS, 
	
	/** The milli blocking check. */
	MILLI_BLOCKING_CHECK, 
	
	/** The milli update query dependencies. */
	MILLI_UPDATE_QUERY_DEPENDENCIES, 
	
	/** The milli update. */
	MILLI_UPDATE;

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
