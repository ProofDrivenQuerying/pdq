package uk.ac.ox.cs.pdq.regression.acceptance;

import java.io.PrintStream;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * High-level acceptance criterion.
 *
 * @author leblay
 * @param <Expected> the generic type
 * @param <Observed> the generic type
 */
public interface AcceptanceCriterion<Expected, Observed> {

	/**
	 * The Enum AcceptanceLevels.
	 */
	enum AcceptanceLevels { 
		/** The pass. */
		PASS, 
		/** The fail. */
		FAIL }

	/**
	 * The result of an acceptance criterion check. 
	 * It consists of an acceptance level and supporting information as a list 
	 * of Strings.
	 * 
	 * @author Julien Leblay
	 */
	static class AcceptanceResult {

		/** The level. */
		private final AcceptanceLevels level;

		/** The supporting info. */
		private final List<String> supportingInfo;

		/**
		 * Default constructor .
		 *
		 * @param level the level
		 * @param supportingInfo the supporting info
		 */
		AcceptanceResult(AcceptanceLevels level, String... supportingInfo) {
			this.level = level;
			this.supportingInfo = ImmutableList.copyOf(supportingInfo);
		}

		/**
		 * Prints a report of the acceptance result to the given output stream.
		 *
		 * @param out the out
		 */
		public void report(PrintStream out) {
			out.println(this.level);
			for (String reason: this.supportingInfo) {
				out.println("\t" + reason);
			}
		}
		
		/** PASS or Fail message as a string.
		 * @return
		 */
		public String report() {
			return this.level + " ";
		}

		/**
		 * Gets the level.
		 *
		 * @return the acceptance level of the result
		 */
		public AcceptanceLevels getLevel() {
			return this.level;
		}
	}

	/**
	 * Check.
	 *
	 * @param e expected object
	 * @param o observed object
	 * @return true, iff the observed object satisfies the acceptance criterion.
	 */
	AcceptanceResult check(Expected e, Observed o);
}
