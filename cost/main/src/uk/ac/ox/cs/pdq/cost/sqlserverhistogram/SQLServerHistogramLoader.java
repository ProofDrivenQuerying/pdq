// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.sqlserverhistogram;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.util.Utility;


/**
 * Loads SQL Server 2014 histograms.
 *
 * @author Efthymia Tsamoura
 */
public class SQLServerHistogramLoader {

	/**
	 * Load.
	 *
	 * @param type 		The type of the histogram values
	 * @param fileName 		Each line should correspond to a buckets. The data in each line should appear in the following orders
	 * 		RANGE_HI_KEY RANGE_ROWS    EQ_ROWS       DISTINCT_RANGE_ROWS  AVG_RANGE_ROWS 
	 * @return the SQL server histogram
	 */
	public static SQLServerHistogram load(Type type, String fileName) {
		String line = null;
		try {
			fileName = fileName.substring(1);
			List<SQLServerBucket> buckets = Lists.newArrayList();
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			try{
				while((line = bufferedReader.readLine()) != null) {
					String regex = null;
					List<Integer> groupIndex = null;
					if(type instanceof Class && String.class.isAssignableFrom((Class<?>) type)) {
						regex = "^((.+)(\\s+)(\\d+(\\.\\d+)?)(\\s+)(\\d+(\\.\\d+)?)(\\s+)(\\d+(\\.\\d+)?)(\\s+)(\\d+(\\.\\d+)?))";
						groupIndex = Lists.newArrayList(2, 4, 7, 10, 13);
					}
					else if(type instanceof Class && Number.class.isAssignableFrom((Class<?>) type)) {
						regex = "^(([+-]?\\d+(\\.\\d+)?)(\\s+)(\\d+(\\.\\d+)?)(\\s+)(\\d+(\\.\\d+)?)(\\s+)(\\d+(\\.\\d+)?)(\\s+)(\\d+(\\.\\d+)?))";
						groupIndex = Lists.newArrayList(2, 5, 8, 11, 14);
					}
					else if(type instanceof Class && Date.class.isAssignableFrom((Class<?>) type)) {
						regex = "^((\\d+/\\d+/\\d+ 00:00:00)(\\s+)(\\d+(\\.\\d+)?)(\\s+)(\\d+(\\.\\d+)?)(\\s+)(\\d+(\\.\\d+)?)(\\s+)(\\d+(\\.\\d+)?))";
						groupIndex = Lists.newArrayList(2, 4, 7, 10, 13);
					}
					
					String[] elements = new String[5];
					Pattern p = Pattern.compile(regex);
					Matcher m = p.matcher(line);
					if (m.find()) {
						elements[0] = m.group(groupIndex.get(0));
						elements[1] = m.group(groupIndex.get(1));
						elements[2] = m.group(groupIndex.get(2));
						elements[3] = m.group(groupIndex.get(3));
						elements[4] = m.group(groupIndex.get(4));
					}
					else {
						new java.lang.IllegalStateException("Unsupported data type");
					}
					
					Object range_hi_key = Utility.cast(type, elements[0]);
					BigInteger range_rows;
					range_rows = new BigDecimal(elements[1]).toBigInteger();
					BigInteger eq_rows = new BigDecimal(elements[2]).toBigInteger();
					BigInteger distinct_range_rows = new BigDecimal(elements[3]).toBigInteger();
					double avg_range_rows = Double.parseDouble(elements[4]); 
					SQLServerBucket bucket = new SQLServerBucket(range_hi_key, range_rows, eq_rows, distinct_range_rows, avg_range_rows);
					buckets.add(bucket);
				}
			}
			catch(Exception ex){
				ex.printStackTrace();;
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
