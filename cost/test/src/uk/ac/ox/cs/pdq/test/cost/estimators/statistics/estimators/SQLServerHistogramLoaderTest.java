package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogramLoader;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class SQLServerHistogramLoaderTest {

	@Before public void setup() {
        MockitoAnnotations.initMocks(this);
	}

	@Test public void test1() {
		SQLServerHistogram histogram = SQLServerHistogramLoader.load(Integer.class, "test/input/V1Histogram.rpt");
		Assert.assertEquals(histogram.getBuckets().size(), 162);
	}
	
	@Test public void test2() {
		SQLServerHistogram histogram = SQLServerHistogramLoader.load(Integer.class, "test/input/V2Histogram.rpt");
		Assert.assertEquals(histogram.getBuckets().size(), 26);
	}
}
