// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.logging.Periods;

/**
 * The Class PeriodsTest.
 */
public class PeriodsTest {
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		PdqTest.assertsEnabled();
	}

	/**
	 * Test parse seconds.
	 */
	@Test 
	public void testParseSeconds() {
		Assert.assertEquals(10000l, Periods.parse("10s"));
		Assert.assertEquals(22000l, Periods.parse(" 22s"));
		Assert.assertEquals(37000l, Periods.parse("37s "));
		Assert.assertEquals(59000l, Periods.parse("-59s"));
	}

	/**
	 * Test parse disconnected seconds.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseDisconnectedSeconds() {
		Assert.assertEquals(0, Periods.parse("48 s"));
	}

	/**
	 * Test parse nonesense seconds.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseNonesenseSeconds() {
		Assert.assertEquals(0, Periods.parse("@#;!s"));
	}

	/**
	 * Test parse minutes.
	 */
	@Test 
	public void testParseMinutes() {
		Assert.assertEquals(10000l * 60, Periods.parse("10m"));
		Assert.assertEquals(22000l * 60, Periods.parse(" 22m"));
		Assert.assertEquals(37000l * 60, Periods.parse("37m "));
		Assert.assertEquals(59000l * 60, Periods.parse("-59m"));
	}

	/**
	 * Test parse disconnected minutes.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseDisconnectedMinutes() {
		Assert.assertEquals(0, Periods.parse("48 m"));
	}

	/**
	 * Test parse nonesense minutes.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseNonesenseMinutes() {
		Assert.assertEquals(0, Periods.parse("@#;!m"));
	}

	/**
	 * Test parse hours.
	 */
	@Test 
	public void testParseHours() {
		Assert.assertEquals(10000l * 60 * 60, Periods.parse("10h"));
		Assert.assertEquals(22000l * 60 * 60, Periods.parse(" 22h"));
		Assert.assertEquals(37000l * 60 * 60, Periods.parse("37h "));
		Assert.assertEquals(59000l * 60 * 60, Periods.parse("-59h"));
	}

	/**
	 * Test parse disconnected hours.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseDisconnectedHours() {
		Assert.assertEquals(0, Periods.parse("48 h"));
	}

	/**
	 * Test parse nonesense hours.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseNonesenseHours() {
		Assert.assertEquals(0, Periods.parse("@#;!h"));
	}

	/**
	 * Test parse days.
	 */
	@Test 
	public void testParseDays() {
		Assert.assertEquals(10000l * 60 * 60 * 24, Periods.parse("10d"));
		Assert.assertEquals(22000l * 60 * 60 * 24, Periods.parse(" 22d"));
		Assert.assertEquals(37000l * 60 * 60 * 24, Periods.parse("37d "));
		Assert.assertEquals(59000l * 60 * 60 * 24, Periods.parse("-59d"));
	}

	/**
	 * Test parse disconnected days.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseDisconnectedDays() {
		Assert.assertEquals(0, Periods.parse("48 d"));
	}

	/**
	 * Test parse nonesense days.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseNonesenseDays() {
		Assert.assertEquals(0, Periods.parse("@#;!d"));
	}

	/**
	 * Test parse weeks.
	 */
	@Test 
	public void testParseWeeks() {
		Assert.assertEquals(10000l * 60 * 60 * 24 * 7, Periods.parse("10w"));
		Assert.assertEquals(22000l * 60 * 60 * 24 * 7, Periods.parse(" 22w"));
		Assert.assertEquals(37000l * 60 * 60 * 24 * 7, Periods.parse("37w "));
		Assert.assertEquals(59000l * 60 * 60 * 24 * 7, Periods.parse("-59w"));
	}

	/**
	 * Test parse disconnected weeks.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseDisconnectedWeeks() {
		Assert.assertEquals(0, Periods.parse("48 w"));
	}

	/**
	 * Test parse nonesense weeks.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseNonesenseWeeks() {
		Assert.assertEquals(0, Periods.parse("@#;!w"));
	}

	/**
	 * Test parse months.
	 */
	@Test 
	public void testParseMonths() {
		Assert.assertEquals(10000l * 60 * 60 * 24 * 30, Periods.parse("10M"));
		Assert.assertEquals(22000l * 60 * 60 * 24 * 30, Periods.parse(" 22M"));
		Assert.assertEquals(37000l * 60 * 60 * 24 * 30, Periods.parse("37M "));
		Assert.assertEquals(59000l * 60 * 60 * 24 * 30, Periods.parse("-59M"));
	}

	/**
	 * Test parse disconnected months.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseDisconnectedMonths() {
		Assert.assertEquals(0, Periods.parse("48 M"));
	}

	/**
	 * Test parse nonesense months.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseNonesenseMonths() {
		Assert.assertEquals(0, Periods.parse("@#;!M"));
	}

	/**
	 * Test parse years.
	 */
	@Test 
	public void testParseYears() {
		Assert.assertEquals(10000l * 60 * 60 * 24 * 365, Periods.parse("10y"));
		Assert.assertEquals(22000l * 60 * 60 * 24 * 365, Periods.parse(" 22y"));
		Assert.assertEquals(37000l * 60 * 60 * 24 * 365, Periods.parse("37y "));
		Assert.assertEquals(59000l * 60 * 60 * 24 * 365, Periods.parse("-59y"));
	}

	/**
	 * Test parse disconnected years.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseDisconnectedYears() {
		Assert.assertEquals(0, Periods.parse("48 y"));
	}

	/**
	 * Test parse nonesense years.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testParseNonesenseYears() {
		Assert.assertEquals(0, Periods.parse("@#;!y"));
	}

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
	 * @param period
	 * @return a representation in ms of the given period String
	 */
//	public static long parse(String period) {
//		Matcher m = PERIOD_REGEXP.matcher(period);
//		if (m.find()) {
//			int interval = Integer.parseInt(m.group(INTERVAL_GROUP));
//			char scale = Character.valueOf(m.group(SCALE_GROUP).charAt(0));
//			switch (scale) {
//			case 's': return interval * 1000;
//			case 'm': return interval * 1000 * 60;
//			case 'h': return interval * 1000 * 60 * 60;
//			case 'd': return interval * 1000 * 60 * 60 * 24;
//			case 'w': return interval * 1000 * 60 * 60 * 24 * 7;
//			case 'M': return interval * 1000 * 60 * 60 * 24 * 30;
//			case 'y': return interval * 1000 * 60 * 60 * 24 * 365;
//			default: throw new IllegalStateException("Invalid period scale " + scale);
//			}
//		}
//		throw new IllegalArgumentException(period + " does not parse to a valid period of time. "
//				+ "The period should satisfy the pattern <number>[s|m|h|d|w|M|y]");
//	}

}
