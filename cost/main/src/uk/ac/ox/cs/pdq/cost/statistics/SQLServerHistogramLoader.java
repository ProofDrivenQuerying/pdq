package uk.ac.ox.cs.pdq.cost.statistics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.util.Types;

import com.beust.jcommander.internal.Lists;

/**
 * Loads SQL Server 2014 histograms
 * @author Efthymia Tsamoura
 *
 */
public class SQLServerHistogramLoader {

	/** Logger. */
	private static Logger log = Logger.getLogger(SQLServerHistogramLoader.class);

	/**
	 * @param type
	 * 		The type of the histogram values
	 * @param fileName
	 * 		Each line should correspond to a buckets. The data in each line should appear in the following orders
	 * 		RANGE_HI_KEY RANGE_ROWS    EQ_ROWS       DISTINCT_RANGE_ROWS  AVG_RANGE_ROWS 
	 * @return
	 * 
	 */
	public static SQLServerHistogram load(Type type, String fileName) {
		String line = null;
		try {
			List<SQLServerBucket> buckets = Lists.newArrayList();
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			try{
			while((line = bufferedReader.readLine()) != null) {
				String[] elements = line.split("\\s+");
				Object range_hi_key = Types.cast(type, elements[0]);
				BigInteger range_rows;
//				try {
//					range_rows = new BigDecimal(elements[1]).toBigInteger();
//				}
//				catch(Exception ex) {
//					range_rows = new BigInteger(elements[1]);
//				}
				range_rows = new BigDecimal(elements[1]).toBigInteger();
				BigInteger eq_rows = new BigDecimal(elements[2]).toBigInteger();
				BigInteger distinct_range_rows = new BigDecimal(elements[3]).toBigInteger();
				double avg_range_rows = new Double(elements[4]); 
				SQLServerBucket bucket = new SQLServerBucket(range_hi_key, range_rows, eq_rows, distinct_range_rows, avg_range_rows);
				buckets.add(bucket);
			}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			bufferedReader.close(); 
			return new SQLServerHistogram(buckets);
		}
		catch(FileNotFoundException ex) {      
			ex.printStackTrace(System.out);
		}
		catch(IOException ex) {
			ex.printStackTrace(System.out);
		}
		return null;
	}

}
