package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogramLoader;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerJoinCardinalityEstimator;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the SQLServerJoinCardinalityEstimator and the SQLServerHistogram classes using histogram test files.
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class SQLServerJoinCardinalityEstimatorTest extends PdqTest{

	/**
	 * Reads files:
	 * /test/input/V1Histogram.rpt
	 * /test/input/V2Histogram.rpt
	 * And estimates intersection cardinality.
	 */
	@Test
	public void test1() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "/test/input/V1Histogram.rpt");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "/test/input/V2Histogram.rpt");

		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateIntersectionCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
		Assert.assertEquals(new BigInteger("13444"), cardinality);
	}

	/**
	 * Reads files:
	 * /test/input/V1Histogram.rpt
	 * /test/input/PK_PARTSUPP.rpt
	 * And estimates single Join Attribute Cardinality.
	 */
	@Test
	public void test2() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "/test/input/V1Histogram.rpt");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "/test/input/PK_PARTSUPP.rpt");

		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateSingleJoinAttributeCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
		Assert.assertEquals(new BigInteger("106455"), cardinality);
	}
	/**
	 * Reads files:
	 * /test/input/V1Histogram.rpt
	 * /test/input/PK_PARTSUPP.rpt
	 * And estimates intersection cardinality.
	 */
	@Test
	public void test3() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "/test/input/V1Histogram.rpt");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "/test/input/PK_PARTSUPP.rpt");

		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateIntersectionCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
		Assert.assertEquals(new BigInteger("5232"), cardinality);
	}
	/**
	 * Reads files:
	 * /test/input/HIST_V6_c_custkey
	 * /test/input/HIST_V3_c_custkey
	 * And estimates intersection cardinality.
	 */
	@Test
	public void test4() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "/test/input/HIST_V6_c_custkey");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "/test/input/HIST_V3_c_custkey");

		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateIntersectionCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
		Assert.assertEquals(new BigInteger("2474"), cardinality);
	}
	/**
	 * Reads files:
	 * /test/input/HIST_V10_ps_suppkey
	 * /test/input/LINEITEM_L_SUPPKEY
	 * And estimates single Join Attribute Cardinality.
	 */
	@Test
	public void test5() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Integer.class, "/test/input/HIST_V10_ps_suppkey");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Integer.class, "/test/input/LINEITEM_L_SUPPKEY");

		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateSingleJoinAttributeCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
		Assert.assertEquals(new BigInteger("7056575"), cardinality);
	}
	/**
	 * Reads files:
	 * /test/input/ORDERS_O_TOTALPRICE
	 * /test/input/PART_P_RETAILPRICE
	 * And estimates single Join Attribute Cardinality.
	 */
	@Test
	public void test6() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Double.class, "/test/input/ORDERS_O_TOTALPRICE");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Double.class, "/test/input/PART_P_RETAILPRICE");

		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateSingleJoinAttributeCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
		Assert.assertEquals(new BigInteger("60234"), cardinality);
	}
	/**
	 * Reads files:
	 * /test/input/ORDERS_O_ORDERKEY
	 * /test/input/HIST_V7_o_orderkey.rpt
	 * And estimates single Join Attribute Cardinality.
	 */
	@Test
	public void test7() {
		SQLServerHistogram hl = SQLServerHistogramLoader.load(Double.class, "/test/input/ORDERS_O_ORDERKEY");
		SQLServerHistogram hr = SQLServerHistogramLoader.load(Double.class, "/test/input/HIST_V7_o_orderkey");

		SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
		BigInteger cardinality = estimator.estimateSingleJoinAttributeCardinality(hl, hr);
		Assert.assertNotNull(cardinality);
		Assert.assertEquals(new BigInteger("2493"), cardinality);
	}

}
