package uk.ac.ox.cs.pdq.cost.statistics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Estimates the cardinality of the join of two attributes using their histograms .
 *
 * @author Efthymia Tsamoura
 */
public class SQLServerJoinCardinalityEstimator {

	/** The log. */
	protected static Logger log = Logger.getLogger(SQLServerJoinCardinalityEstimator.class);
	
	/**
	 * Estimate single join attribute cardinality.
	 *
	 * @param left the left
	 * @param right the right
	 * @return 		the estimated cardinality of the join of the input histograms. 
	 * 		Let the buckets of the histogram are the same after the bucket alignment, namely B = {b1,...,bK}. The returned estimate is
	 * 			SizeOf(AnnPlan)=\Sum{i\leqK} AvgSize(R1[bi]) \times AvgSize(R2[bi]) \times min{NumDistinct(R1[bi]),NumDistinct(R2[bi])}
	 * 			where AvgSize(Rj[bi]) is the average number of tuples in Rj per element in the bucket
	 * 			bi and NumDistinct(Rj[bi]) is the number of distinct values of the position corresponding
	 * 			to chase constant c in bucket bi of Rj.
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


	/**
	 * Estimate intersection cardinality.
	 *
	 * @param left the left
	 * @param right the right
	 * @return 		the estimated cardinality of the intersection of the input histograms. 
	 * 		Let the buckets of the histogram are the same after the bucket alignment, namely B = {b1,...,bK}. The returned estimate is
	 * 
	 * 			SizeOf(AnnPlan) = \Sum_{i\leqK}
	 * 			min{AvgSize(R1[bi]) \times NumDistinct(R1[bi]),AvgSize(R2[bi]) \time NumDistinct(R2[bi])}
	 * 
	 * 			where AvgSize(Rj[bi]) is the average number of tuples in Rj per element in the bucket
	 * 			bi and NumDistinct(Rj[bi]) is the number of distinct values of the position corresponding
	 * 			to chase constant c in bucket bi of Rj.
	 * 		Very course histogram alignment takes place; a bucket b from the first histogram is aligned with a set of buckets B from the
	 * 		second histogram if b intersects with B. The cardinality estimation algorithm relies on the containment assumption.
	 */
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
	 * Find overlapping buckets.
	 *
	 * @param source the source
	 * @param bucketIndex the bucket index
	 * @param target the target
	 * @return 		the buckets of the target histogram that intersect with the bucketIndex-th bucket of the source histogram.
	 * 		The SQL Server histograms do not keep the leftmost boundaries of their buckets. Due to this limitation, we never deal
	 * 		with the lowest buckets in the input histogram. For the lowest buckets we apply specific join cardinality estimations. 
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
			//Check if the two buckets intersect
			if(this.doIntersect(leftBoundary, rightBoundary, leftCurrentBoundary, rightCurrentBoundary)) {
				lowestBound = j;
				break;
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
			//Check if the two buckets intersect
			if(this.doIntersect(leftBoundary, rightBoundary, leftCurrentBoundary, rightCurrentBoundary)) {
				highestBound = j;
				break;
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
	
	
	/**
	 * Do intersect.
	 *
	 * @param l1 left boundary of a object 1
	 * @param r1 right boundary of a object 1
	 * @param l2 left boundary of a object 2
	 * @param r2 right boundary of a object 2
	 * @return 		true if the object boundaries intersect
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	boolean doIntersect(Object l1, Object r1, Object l2, Object r2) {
		if(l1 instanceof Comparable && r1 instanceof Comparable &&
				l2 instanceof Comparable && r2 instanceof Comparable) {
						
			if(((Comparable)r1).compareTo(l2) >= 0 && ((Comparable)r1).compareTo(r2) <= 0) {
				return true;
			}
			
			if(((Comparable)r2).compareTo(l1) >= 0 && ((Comparable)r2).compareTo(r1) <= 0) {
				return true;
			}
			
			if(((Comparable)l1).compareTo(l2) >= 0 && ((Comparable)l1).compareTo(r2) <= 0) {
				return true;
			}
			
			if(((Comparable)l2).compareTo(l1) >= 0 && ((Comparable)l2).compareTo(r1) <= 0) {
				return true;
			}
			
			
			if(((Comparable)l1).compareTo(l2) <= 0 && ((Comparable)r1).compareTo(r2) >= 0) {
				return true;
			}
			
			if(((Comparable)l2).compareTo(l1) <= 0 && ((Comparable)r2).compareTo(r1) >= 0) {
				return true;
			}
		}
	
		return false;
	}

}
