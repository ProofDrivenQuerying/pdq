// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.sqlserverhistogram;

import java.io.PrintStream;

/**
 * Loads SQL Server 2014 histograms.
 *
 * @author Efthymia Tsamoura
 */
public class SQLServerHistogramWriter {
	
	/**
	 * Writes the input histogram to the specified output. 
	 * For each bucket we output the following properties
	 * 		RANGE_HI_KEY RANGE_ROWS    EQ_ROWS       DISTINCT_RANGE_ROWS  AVG_RANGE_ROWS 
	 *
	 * @param histogram the histogram
	 * @param out the out
	 */
	public static void write(SQLServerHistogram histogram, PrintStream out) {
		for(SQLServerBucket bucket:histogram.getBuckets()) {
			out.print(bucket.getRange_hi_key() + "\t\t\t");
			out.print(bucket.getRange_rows() + "\t\t\t");
			out.print(bucket.getEq_rows()+ "\t\t\t");
			out.print(bucket.getDistinct_range_rows()+ "\t\t\t");
			out.print(bucket.getAvg_range_rows());
			out.println();
		}
	}

}
