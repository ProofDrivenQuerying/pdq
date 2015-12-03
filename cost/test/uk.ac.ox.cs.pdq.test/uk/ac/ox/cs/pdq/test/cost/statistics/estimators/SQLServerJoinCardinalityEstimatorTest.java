package uk.ac.ox.cs.pdq.test.cost.statistics.estimators;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogramLoader;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerJoinCardinalityEstimator;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class SQLServerJoinCardinalityEstimatorTest {

	@Before public void setup() {
        MockitoAnnotations.initMocks(this);
	}

	@Test public void test1() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "test/input/V1Histogram.rpt");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "test/input/V2Histogram.rpt");
		
		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateIntersectionCardinality(hl, hr);
	}
	
	@Test public void test2() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "test/input/V1Histogram.rpt");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "test/input/PK_PARTSUPP.rpt");
		
		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateSingleJoinAttributeCardinality(hl, hr);
	}
	
	@Test public void test3() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "test/input/V1Histogram.rpt");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "test/input/PK_PARTSUPP.rpt");
		
		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateIntersectionCardinality(hl, hr);
	}
	
}
