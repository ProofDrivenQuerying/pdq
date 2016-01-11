package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.HomomorphismDetectorTypes;

import com.google.common.base.Strings;

/**
 * Returns an instance of HomomorphismDetector depending on the input parameters
 *
 * @author Efthymia Tsamoura
 * @author George Konstantinidis
 */
public class HomomorphismManagerFactory {

	/** Logger. */
	private static Logger log = Logger.getLogger(HomomorphismManagerFactory.class);

	/**
	 * Counter used to create distinct database schemata for distinct method
	 * calls.
	 */
	private static Integer counter = 0;

	/**
	 * 
	 * @param schema
	 * @param query
	 * @param parameters
	 * @return an instance of homomorphism HomomorphismDetector instantiated according to
	 *         contextual information
	 * @throws HomomorphismException
	 */
	public synchronized HomomorphismManager getInstance(
			Schema schema, 
			ReasoningParameters parameters)
					throws HomomorphismException {
		return getInstance(schema, 
				parameters.getHomomorphismDetectorType(), 
				parameters.getDatabaseDriver(), 
				parameters.getConnectionUrl(),
				parameters.getDatabaseName(), 
				parameters.getDatabaseUser(),
				parameters.getDatabasePassword()
				);
	}
	
	/**
	 * 
	 * @param schema
	 * @param query
	 * @param type
	 * @param driver
	 * @param url
	 * @param database
	 * @param username
	 * @param password
	 * @return an instance of homomorphism HomomorphismManager instantiated according to
	 *         contextual information
	 * @throws HomomorphismException
	 */
	public synchronized HomomorphismManager getInstance(
			Schema schema, 
			HomomorphismDetectorTypes type,
			String driver,
			String url,
			String database,
			String username,			
			String password
			) throws HomomorphismException {
		HomomorphismManager result = null;
		try {
			if (type != null) {
				switch (type) {
				case DATABASE:
					SQLStatementBuilder builder = null;
					if (url != null && url.contains("mysql")) {
						builder = new MySQLStatementBuilder();
					} else {
						if (Strings.isNullOrEmpty(driver)) {
							driver = "org.apache.derby.jdbc.EmbeddedDriver";
						}
						if (Strings.isNullOrEmpty(url)) {
							url = "jdbc:derby:memory:{1};create=true";
						}
						if (Strings.isNullOrEmpty(database)) {
							database = "chase";
						}
						database +=  "_" + System.currentTimeMillis() + "_" + counter++;
						synchronized (counter) {
							username = "APP_" + (counter++);
						}
						password = "";
						builder = new DerbyStatementBuilder();
					}
					result = new DBHomomorphismManager(
							driver, url, database, username, password, builder,
							schema);
					result.initialize();
					return result;
				}
			}
		} catch (SQLException e) {
			log.warn("Could not load " + database + ". Falling back to default database.");
		}
		synchronized (counter) {
			username = "APP_" + (counter++);
		}
		// Fail safe is in-memory derby
		try {
			result = new DBHomomorphismManager("org.apache.derby.jdbc.EmbeddedDriver",
					"jdbc:derby:memory:{1};create=true", "chase", username, "", new DerbyStatementBuilder(),
					schema);
			result.initialize();
			return result;
		} catch (SQLException e) {
			throw new IllegalStateException("Could not load default database.");
		}
	}
}