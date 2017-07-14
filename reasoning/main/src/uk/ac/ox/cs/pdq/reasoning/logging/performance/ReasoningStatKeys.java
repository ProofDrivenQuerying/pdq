package uk.ac.ox.cs.pdq.reasoning.logging.performance;

import com.google.common.base.CaseFormat;

import uk.ac.ox.cs.pdq.logging.StatKey;


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
