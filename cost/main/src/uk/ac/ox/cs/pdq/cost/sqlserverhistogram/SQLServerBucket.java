// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.sqlserverhistogram;

import java.math.BigInteger;
import java.util.Objects;

import uk.ac.ox.cs.pdq.cost.statistics.Bucket;

/**
 * 
 * Buckets for SQL Server 2014.
 * For more details see https://msdn.microsoft.com/en-gb/library/ms174384.aspx  
 * @author Efthymia Tsamoura
 *
 */
public class SQLServerBucket implements Bucket{

	/** Upper bound column value for a histogram step. The column value is also called a key value.*/
	private final Object range_hi_key;

	/**Estimated number of rows whose column value falls within a histogram step, excluding the upper bound.**/
	private final BigInteger range_rows;

	/**Estimated number of rows whose column value equals the upper bound of the histogram step.**/
	private final BigInteger eq_rows;

	/**Estimated number of rows with a distinct column value within a histogram step, excluding the upper bound.**/
	private final BigInteger distinct_range_rows;

	/** Average number of rows with duplicate column values within a histogram step, 
	 * excluding the upper bound (RANGE_ROWS / DISTINCT_RANGE_ROWS for DISTINCT_RANGE_ROWS > 0).**/
	private final double avg_range_rows;


	/**
	 * Default constructor for SQL server buckets.
	 *
	 * @param range_hi_key 		 Upper bound column value for a histogram step. The column value is also called a key value.
	 * @param range_rows 		Estimated number of rows whose column value falls within a histogram step, excluding the upper bound.
	 * @param eq_rows 		Estimated number of rows whose column value equals the upper bound of the histogram step.
	 * @param distinct_range_rows 		Estimated number of rows with a distinct column value within a histogram step, excluding the upper bound.
	 * @param avg_range_rows 		Average number of rows with duplicate column values within a histogram step, 
	 * 		excluding the upper bound (RANGE_ROWS / DISTINCT_RANGE_ROWS for DISTINCT_RANGE_ROWS > 0).
	 */
	public SQLServerBucket(Object range_hi_key, BigInteger range_rows, BigInteger eq_rows, BigInteger distinct_range_rows,
			double avg_range_rows) {
		this.range_hi_key = range_hi_key;
		this.range_rows = range_rows;
		this.eq_rows = eq_rows;
		this.distinct_range_rows = distinct_range_rows;
		this.avg_range_rows = avg_range_rows;
	}


	/**
	 * Gets the range_hi_key.
	 *
	 * @return the range_hi_key
	 */
	public Object getRange_hi_key() {
		return this.range_hi_key;
	}


	/**
	 * Gets the range_rows.
	 *
	 * @return the range_rows
	 */
	public BigInteger getRange_rows() {
		return this.range_rows;
	}


	/**
	 * Gets the eq_rows.
	 *
	 * @return the eq_rows
	 */
	public BigInteger getEq_rows() {
		return this.eq_rows;
	}


	/**
	 * Gets the distinct_range_rows.
	 *
	 * @return the distinct_range_rows
	 */
	public BigInteger getDistinct_range_rows() {
		return this.distinct_range_rows;
	}


	/**
	 * Gets the avg_range_rows.
	 *
	 * @return the avg_range_rows
	 */
	public double getAvg_range_rows() {
		return this.avg_range_rows;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.range_hi_key + "\t" + this.range_rows + 
				"\t" + this.eq_rows + 
				"\t" + this.distinct_range_rows + 
				"\t" + this.avg_range_rows;
	}

	/**
	 * Equals.
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		return super.equals(o)
				&& this.getClass().isInstance(o)
				&& this.range_hi_key.equals(((SQLServerBucket) o).range_hi_key)
				&& this.range_rows.equals(((SQLServerBucket) o).range_rows)
				&& this.eq_rows.equals(((SQLServerBucket) o).eq_rows)
				&& this.distinct_range_rows.equals(((SQLServerBucket) o).distinct_range_rows)
				&& this.avg_range_rows == ((SQLServerBucket) o).avg_range_rows
				;

	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.range_hi_key, this.range_rows, this.eq_rows,
				this.distinct_range_rows, this.avg_range_rows);
	}

}
