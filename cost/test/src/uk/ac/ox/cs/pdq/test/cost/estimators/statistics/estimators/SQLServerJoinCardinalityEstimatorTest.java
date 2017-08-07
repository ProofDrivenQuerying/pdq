package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogramLoader;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerJoinCardinalityEstimator;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class SQLServerJoinCardinalityEstimatorTest.
 *
 * @author Efthymia Tsamoura
 */
public class SQLServerJoinCardinalityEstimatorTest {

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
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "/test/input/V1Histogram.rpt");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "/test/input/V2Histogram.rpt");
		
		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateIntersectionCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
	}
	
	/**
	 * Test2.
	 */
	@Test public void test2() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "/test/input/V1Histogram.rpt");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "/test/input/PK_PARTSUPP.rpt");
		
		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateSingleJoinAttributeCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
		
	}
	
	/**
	 * Test3.
	 */
	@Test public void test3() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "/test/input/V1Histogram.rpt");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "/test/input/PK_PARTSUPP.rpt");
		
		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateIntersectionCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
	}
	
	/**
	 * Test4.
	 */
	@Test public void test4() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "/test/input/HIST_V6_c_custkey");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "/test/input/HIST_V3_c_custkey");
		
		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateIntersectionCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
	}
	
	/**
	 * Test5.
	 */
	@Test public void test5() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "/test/input/HIST_V10_ps_suppkey");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "/test/input/LINEITEM_L_SUPPKEY");
		
		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateSingleJoinAttributeCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
	}
	
	/**
	 * Test6.
	 */
	@Test public void test6() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Double.class, "/test/input/ORDERS_O_TOTALPRICE");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Double.class, "/test/input/PART_P_RETAILPRICE");
		
		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateSingleJoinAttributeCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
	}
	
	/**
	 * Test7.
	 */
	@Test public void test7() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Double.class, "/test/input/ORDERS_O_ORDERKEY");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Double.class, "/test/input/HIST_V7_o_orderkey");

		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateSingleJoinAttributeCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
	}
	
}
