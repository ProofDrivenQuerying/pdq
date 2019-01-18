package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.cost.sqlserverhistogram.SQLServerHistogram;
import uk.ac.ox.cs.pdq.cost.sqlserverhistogram.SQLServerHistogramLoader;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests SQLServerHistogramLoader with two example histogram files.
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class SQLServerHistogramLoaderTest extends PdqTest {
	/**
	 * Test1, reading /test/input/V1Histogram.rpt
	 */
	@Test
	public void test1() {
		SQLServerHistogram histogram = SQLServerHistogramLoader.load(Integer.class, "/test/src/" + 
					"uk/ac/ox/cs/pdq/test/cost/estimators/statistics/estimators/input/V1Histogram.rpt");
		Assert.assertEquals(histogram.getBuckets().size(), 162);
	}

	/**
	 * Test2, reading /test/input/V2Histogram.rpt
	 */
	@Test
	public void test2() {
		SQLServerHistogram histogram = SQLServerHistogramLoader.load(Integer.class, "/test/src/uk/ac/ox/cs/pdq/test/cost/estimators/statistics/estimators/input/V2Histogram.rpt");
		Assert.assertEquals(histogram.getBuckets().size(), 26);
	}
}
