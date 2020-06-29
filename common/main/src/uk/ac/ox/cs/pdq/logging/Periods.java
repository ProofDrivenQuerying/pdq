// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Helper class for representing timed intervals of various granularities.
 * 
 * @author Julien Leblay
 */
public class Periods {

	/**  Regular expression group name. */
	private static final String INTERVAL_GROUP = "interval";

	/**  Regular expression group name. */
	private static final String SCALE_GROUP = "scale";

	/**
	 * The regular expression used to parse a period and convert it to milliseconds.
	 */
	private static final Pattern PERIOD_REGEXP = Pattern.compile(
				"(?<" + INTERVAL_GROUP + ">\\d+)(?<" + SCALE_GROUP + ">[s|m|h|d|w|M|y])");

	/**
	 * Parse a string of the form &lt;number&gt;[s|m|h|d|w|M|y] and return the
	 * corresponding period of time in milliseconds.
	 * The meaning of the letters is:
	 * 	- s: seconds
	 * 	- m: minutes
	 * 	- h: hours
	 * 	- d: days
	 * 	- w: weeks
	 * 	- M: months
	 * 	- y: years
	 *
	 * @param period the period
	 * @return a representation in ms of the given period String
	 */
	public static long parse(String period) {
		Matcher m = PERIOD_REGEXP.matcher(period);
		if (m.find()) {
			long interval = Integer.parseInt(m.group(INTERVAL_GROUP));
			char scale = Character.valueOf(m.group(SCALE_GROUP).charAt(0));
			switch (scale) {
			case 's': return interval * 1000;
			case 'm': return interval * 1000 * 60;
			case 'h': return interval * 1000 * 60 * 60;
			case 'd': return interval * 1000 * 60 * 60 * 24;
			case 'w': return interval * 1000 * 60 * 60 * 24 * 7;
			case 'M': return interval * 1000 * 60 * 60 * 24 * 30;
			case 'y': return interval * 1000 * 60 * 60 * 24 * 365;
			default: throw new IllegalArgumentException("Invalid period scale " + scale);
			}
		}
		throw new IllegalArgumentException(period + " does not parse to a valid period of time. "
				+ "The period should satisfy the pattern <number>[s|m|h|d|w|M|y]");
	}

}
