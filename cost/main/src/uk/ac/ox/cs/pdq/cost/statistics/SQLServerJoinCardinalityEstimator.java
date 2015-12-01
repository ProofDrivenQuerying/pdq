package uk.ac.ox.cs.pdq.cost.statistics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

/**
 * Estimates the cardinality of the join of two attributes using their histograms 
 * @author Efthymia Tsamoura
 *
 */
public class SQLServerJoinCardinalityEstimator {

	protected static Logger log = Logger.getLogger(SQLServerJoinCardinalityEstimator.class);
	
	/**
	 * 
	 * @param left
	 * @param right
	 * @return
	 * 		the estimated cardinality of the join of the input histograms. 
	 * 		Very course histogram alignment takes place; a bucket b from the first histogram is aligned with a set of buckets B from the
	 * 		second histogram if b intersects with B. The cardinality estimation algorithm relies on the containment assumption. 
	 */
	public BigInteger estimateSingleJoinAttributeCardinality(SQLServerHistogram left, SQLServerHistogram right) {
		double cardinality = 0;
		//Histograms with maximum and lowest number of buckets
		SQLServerHistogram minHistogram, maxHistogram;
		if(left.getBuckets().size() < right.getBuckets().size()) {
			minHistogram = left;
			maxHistogram = right;
		}
		else {
			minHistogram = right;
			maxHistogram = left;
		}
		
		SQLServerBucket firstMinBucket = minHistogram.getBucket(0);
		SQLServerBucket firstMaxBucket = maxHistogram.getBucket(0);
		
		//The first bucket of each SQL Server histogram holds the exact number of rows with value = RANGE_HI_KEY
		//So, if the first buckets of the input histograms have the same RANGE_HI_KEY value we simply multiple 
		//the number of rows with value RANGE_HI_KEY 
		if(firstMinBucket.getRange_hi_key().equals(firstMaxBucket.getRange_hi_key())) {
			cardinality = firstMinBucket.getEq_rows().doubleValue() * firstMaxBucket.getEq_rows().doubleValue();
		}

		for(int index = 1; index < minHistogram.getBuckets().size(); ++index) {
			//For each bucket minbucket of the min histogram find the buckets of the max histogram that intersect with minbucket
			SQLServerBucket minbucket = minHistogram.getBucket(index);
			List<SQLServerBucket> intersecting = this.findOverlappingBuckets(minHistogram, index, maxHistogram);

			if(intersecting.size() > 0) {
				//Create a supper bucket from the intersecting buckets 
				BigInteger distinctSuperBucket = BigInteger.ZERO;
				BigInteger totalSuperBucket = BigInteger.ZERO;
				double avgSuperBucket = 0;
				for(SQLServerBucket b:intersecting) {
					distinctSuperBucket = distinctSuperBucket.add(b.getDistinct_range_rows());
					distinctSuperBucket = distinctSuperBucket.add(BigInteger.ONE);
					totalSuperBucket = totalSuperBucket.add(b.getEq_rows());
					totalSuperBucket = totalSuperBucket.add(b.getRange_rows());
				}
				avgSuperBucket = totalSuperBucket.doubleValue() / distinctSuperBucket.doubleValue();
						
				
				BigInteger distinctMinBucket = minbucket.getDistinct_range_rows();
				distinctMinBucket = distinctMinBucket.add(BigInteger.ONE);
				BigInteger totalMinBucket = minbucket.getEq_rows();
				totalMinBucket = totalMinBucket.add(minbucket.getRange_rows());
				double avgMinBucket = totalMinBucket.doubleValue() / distinctMinBucket.doubleValue();
				cardinality += avgSuperBucket * avgMinBucket * (distinctSuperBucket.min(distinctMinBucket)).doubleValue();
			}
		}
		Preconditions.checkArgument(cardinality > 0);
		return new BigDecimal(cardinality).toBigInteger();
	}


	public BigInteger estimateIntersectionCardinality(SQLServerHistogram left, SQLServerHistogram right) {
		double cardinality = 0;
		//Histograms with maximum and lowest number of buckets
		SQLServerHistogram minHistogram, maxHistogram;
		if(left.getBuckets().size() < right.getBuckets().size()) {
			minHistogram = left;
			maxHistogram = right;
		}
		else {
			minHistogram = right;
			maxHistogram = left;
		}
		
		SQLServerBucket firstMinBucket = minHistogram.getBucket(0);
		SQLServerBucket firstMaxBucket = maxHistogram.getBucket(0);
		
		//The first bucket of each SQL Server histogram holds the exact number of rows with value = RANGE_HI_KEY
		//So, if the first buckets of the input histograms have the same RANGE_HI_KEY value we simply multiple 
		//the number of rows with value RANGE_HI_KEY 
		if(firstMinBucket.getRange_hi_key().equals(firstMaxBucket.getRange_hi_key())) {
			cardinality = firstMinBucket.getEq_rows().doubleValue() * firstMaxBucket.getEq_rows().doubleValue();
		}

		for(int index = 1; index < minHistogram.getBuckets().size(); ++index) {
			//For each bucket minbucket of the min histogram find the buckets of the max histogram that intersect with minbucket
			SQLServerBucket minbucket = minHistogram.getBucket(index);
			List<SQLServerBucket> intersecting = this.findOverlappingBuckets(minHistogram, index, maxHistogram);

			if(intersecting.size() > 0) {
				//Create a supper bucket from the intersecting buckets 
				BigInteger distinctSuperBucket = BigInteger.ZERO;
				BigInteger totalSuperBucket = BigInteger.ZERO;
				double avgSuperBucket = 0;
				for(SQLServerBucket b:intersecting) {
					distinctSuperBucket = distinctSuperBucket.add(b.getDistinct_range_rows());
					distinctSuperBucket = distinctSuperBucket.add(BigInteger.ONE);
					totalSuperBucket = totalSuperBucket.add(b.getEq_rows());
					totalSuperBucket = totalSuperBucket.add(b.getRange_rows());
				}
				avgSuperBucket = totalSuperBucket.doubleValue() / distinctSuperBucket.doubleValue();
						
				
				BigInteger distinctMinBucket = minbucket.getDistinct_range_rows();
				distinctMinBucket = distinctMinBucket.add(BigInteger.ONE);
				BigInteger totalMinBucket = minbucket.getEq_rows();
				totalMinBucket = totalMinBucket.add(minbucket.getRange_rows());
				double avgMinBucket = totalMinBucket.doubleValue() / distinctMinBucket.doubleValue();
	
				
				double product1 = avgSuperBucket * distinctSuperBucket.doubleValue() * (distinctMinBucket.doubleValue()/totalMinBucket.doubleValue());
				double product2 = avgMinBucket * distinctMinBucket.doubleValue() * (distinctSuperBucket.doubleValue()/totalSuperBucket.doubleValue());
				cardinality += Math.min(product1, product2);
			}
		}
		Preconditions.checkArgument(cardinality > 0);
		return new BigDecimal(cardinality).toBigInteger();
	}


	/**
	 * 
	 * @param source
	 * @param bucketIndex
	 * @param target
	 * @return
	 * 		the buckets of the target histogram that intersect with the bucketIndex-th bucket of the source histogram.
	 * 		The SQL Server histograms do not keep the leftmost boundaries of their buckets. Due to this limitation, we never deal
	 * 		with the lowest buckets in the input histogram. For the lowest buckets we apply specific join cardinality estimations. 
	 * 		
	 */
	public List<SQLServerBucket> findOverlappingBuckets(SQLServerHistogram source, int bucketIndex, SQLServerHistogram target) {
		Preconditions.checkArgument(source != null);
		Preconditions.checkArgument(target != null);
		Preconditions.checkArgument(source.getBuckets().size() > 1);
		Preconditions.checkArgument(target.getBuckets().size() > 1);
		Preconditions.checkArgument(source.getBuckets().size() > bucketIndex);
		Preconditions.checkArgument(bucketIndex > 0);
		List<SQLServerBucket> intersectingBuckets = Lists.newArrayList();

		//The indexes of the lower and upper intersecting buckets
		int lowestBound = -1, highestBound = -1;

		//Find the left boundary of the input bucket;
		Object leftBoundary = source.getBucket(bucketIndex-1).getRange_hi_key();
		//Find the left boundary of the input bucket;
		Object rightBoundary = source.getBucket(bucketIndex).getRange_hi_key();
		//Find the index of the lowest intersecting bucket
		for(int j = 1; j < target.getBuckets().size(); ++j) {
			//Find the left and right boundaries of the current target bucket
			Object leftCurrentBoundary = target.getBucket(j-1).getRange_hi_key();
			Object rightCurrentBoundary = target.getBucket(j).getRange_hi_key();
			if(leftBoundary instanceof Comparable && leftCurrentBoundary instanceof Comparable) {
				if( ((Comparable)leftCurrentBoundary).compareTo(leftBoundary) <= 0 &&
						((Comparable)leftBoundary).compareTo(rightCurrentBoundary) <= 0) {
					lowestBound = j;
					break;
				}
			}
			if(rightBoundary instanceof Comparable && leftCurrentBoundary instanceof Comparable) {
				if( ((Comparable)leftCurrentBoundary).compareTo(rightBoundary) <= 0 &&
						((Comparable)rightBoundary).compareTo(rightCurrentBoundary) <= 0) {
					lowestBound = j;
					break;
				}
			}
		}

		//Find the left boundary of the input bucket;
		leftBoundary = source.getBucket(bucketIndex-1).getRange_hi_key();
		//Find the right boundary of the input bucket;
		rightBoundary = source.getBucket(bucketIndex).getRange_hi_key();
		//Find the index of the highest intersecting bucket
		for(int j = target.getBuckets().size() - 1; j > 0; --j) {
			//Find the left and right boundaries of the current target bucket
			Object leftCurrentBoundary = target.getBucket(j-1).getRange_hi_key();
			Object rightCurrentBoundary = target.getBucket(j).getRange_hi_key();
			if(rightBoundary instanceof Comparable && leftCurrentBoundary instanceof Comparable) {
				if( ((Comparable)leftCurrentBoundary).compareTo(rightBoundary) <= 0 &&
						((Comparable)rightBoundary).compareTo(rightCurrentBoundary) <= 0) {
					highestBound = j;
					break;
				}
				if( ((Comparable)leftCurrentBoundary).compareTo(leftBoundary) <= 0 &&
						((Comparable)leftBoundary).compareTo(rightCurrentBoundary) <= 0) {
					highestBound = j;
					break;
				}
			}
		}

		if(lowestBound >= 0 && highestBound >= 0) {
			for(int i = lowestBound; i <= highestBound; ++i) {
				intersectingBuckets.add(target.getBucket(i));
			}
		}

		log.trace("Buckets intersecting " + source.getBucket(bucketIndex));
		log.trace(Joiner.on("\n").join(intersectingBuckets));
		return intersectingBuckets;
	}

}
