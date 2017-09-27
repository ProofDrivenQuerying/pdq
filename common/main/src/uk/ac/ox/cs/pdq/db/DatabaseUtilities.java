package uk.ac.ox.cs.pdq.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

public class DatabaseUtilities {
	protected static Logger log = Logger.getLogger(DatabaseInstance.class);
	
	/**
	 * Gets the connection.
	 *
	 * @param driver String
	 * @param url the url
	 * @param database the database
	 * @param username the username
	 * @param password the password
	 * @return a connection database connection for the given properties.
	 * @throws SQLException the SQL exception
	 */
	public static Connection getConnection(String driver, String url, String database, String username, String password) throws SQLException {
		if (!Strings.isNullOrEmpty(driver)) {
			try {
				Class.forName(driver).newInstance();
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Could not load chase database driver '" + driver + "'");
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(),e);
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(),e);
				throw new RuntimeException(e);
			}
		}
		String u = null;
		if (url.contains("{1}")) {
			u = url.replace("{1}", database);
		} else {
			u = url + database;
		}
		try {
			Connection result = DriverManager.getConnection(u, username, password);
			result.setAutoCommit(true);
			return result;
		} catch (SQLException e) {
			e.getNextException().printStackTrace();
			log.debug(e.getMessage());
		}
		Connection result = DriverManager.getConnection(url, username, password);
		result.setAutoCommit(true);
		return result;
	}
}
