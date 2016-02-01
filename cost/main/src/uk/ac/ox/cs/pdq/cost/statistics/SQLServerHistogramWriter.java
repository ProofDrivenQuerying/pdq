package uk.ac.ox.cs.pdq.cost.statistics;

import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * Loads SQL Server 2014 histograms
 * @author Efthymia Tsamoura
 *
 */
public class SQLServerHistogramWriter {

	/** Logger. */
	private static Logger log = Logger.getLogger(SQLServerHistogramWriter.class);

	
	/**
	 * Writes the input histogram to the specified output. 
	 * For each bucket we output the following properties
	 * 		RANGE_HI_KEY RANGE_ROWS    EQ_ROWS       DISTINCT_RANGE_ROWS  AVG_RANGE_ROWS 
	 * @param histogram
	 * @param out
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
