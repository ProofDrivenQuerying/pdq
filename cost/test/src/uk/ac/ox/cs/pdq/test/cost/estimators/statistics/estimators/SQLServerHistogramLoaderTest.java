package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogramLoader;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class SQLServerHistogramLoaderTest.
 *
 * @author Efthymia Tsamoura
 */
public class SQLServerHistogramLoaderTest {

	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
        MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test1.
	 */
	@Test public void test1() {
		SQLServerHistogram histogram = SQLServerHistogramLoader.load(Integer.class, "test/input/V1Histogram.rpt");
		Assert.assertEquals(histogram.getBuckets().size(), 162);
	}
	
	/**
	 * Test2.
	 */
	@Test public void test2() {
		SQLServerHistogram histogram = SQLServerHistogramLoader.load(Integer.class, "test/input/V2Histogram.rpt");
		Assert.assertEquals(histogram.getBuckets().size(), 26);
	}
}
