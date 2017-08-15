package uk.ac.ox.cs.pdq.db.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/** Tools to process database results or commands
 * @author Gabor
 *
 */
public class SQLUtilities {

	/**
	 * Prints an sql resultset to string for debuging.
	 */
	public static String resultSetPrinter(ResultSet rs) throws SQLException {
		StringBuffer sb = new StringBuffer();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		for (int i = 1; i <= columnsNumber; i++) {
			sb.append(rsmd.getColumnName(i) + "\t");
		}
		sb.append("\n");
		while (rs.next()) {
			String columnValue = rs.getString(1);
			sb.append(columnValue + "\t");
			for (int i = 2; i <= columnsNumber; i++) {
				columnValue = rs.getString(i);
				sb.append(columnValue + "\t");
			}
			sb.append("\n");
		}
		return sb.toString();		
	}
}
