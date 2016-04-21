package uk.ac.ox.cs.pdq.materialize.homomorphism;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.materialize.ReasoningParameters;
import uk.ac.ox.cs.pdq.materialize.ReasoningParameters.HomomorphismDetectorTypes;
import uk.ac.ox.cs.pdq.materialize.factmanager.FactDatabaseManager;
import uk.ac.ox.cs.pdq.materialize.factmanager.FactManager;
import uk.ac.ox.cs.pdq.materialize.sqlstatement.DerbyStatementBuilder;
import uk.ac.ox.cs.pdq.materialize.sqlstatement.MySQLStatementBuilder;
import uk.ac.ox.cs.pdq.materialize.sqlstatement.SQLStatementBuilder;

import com.google.common.base.Strings;

// TODO: Auto-generated Javadoc
/**
 * Returns an instance of HomomorphismDetector depending on the input parameters.
 *
 * @author Efthymia Tsamoura
 * @author George Konstantinidis
 */
public class HomomorphismDetectorFactory {

	/** Logger. */
	private static Logger log = Logger.getLogger(HomomorphismDetectorFactory.class);

	/**
	 * Counter used to create distinct database schemata for distinct method
	 * calls.
	 */
	private static Integer counter = 0;

	/**
	 * Gets the single instance of HomomorphismManagerFactory.
	 *
	 * @param schema the schema
	 * @param parameters the parameters
	 * @return an instance of homomorphism HomomorphismDetector instantiated according to
	 *         contextual information
	 * @throws HomomorphismException the homomorphism exception
	 */
	public synchronized HomomorphismDetector getInstance(
			Schema schema, 
			ReasoningParameters parameters,
			FactManager manager)
					throws HomomorphismException {
		return getInstance(schema, 
				parameters.getHomomorphismDetectorType(), 
				parameters.getDatabaseDriver(), 
				parameters.getConnectionUrl(),
				parameters.getDatabaseName(), 
				parameters.getDatabaseUser(),
				parameters.getDatabasePassword(),
				manager
				);
	}
	
	/**
	 * Gets the single instance of HomomorphismManagerFactory.
	 *
	 * @param schema the schema
	 * @param type the type
	 * @param driver the driver
	 * @param url the url
	 * @param database the database
	 * @param username the username
	 * @param password the password
	 * @return an instance of homomorphism HomomorphismManager instantiated according to
	 *         contextual information
	 * @throws HomomorphismException the homomorphism exception
	 */
	public synchronized HomomorphismDetector getInstance(
			Schema schema, 
			HomomorphismDetectorTypes type,
			String driver,
			String url,
			String database,
			String username,			
			String password,
			FactManager manager
			) throws HomomorphismException {
		HomomorphismDetector result = null;
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("No suitable driver found for homomorphism checker.", e);
		}
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
					result = new DatabaseHomomorphismDetector(
							driver, url, database, username, password, builder,
							schema, ((FactDatabaseManager)manager).getToDatabaseTables());
					return result;
				}
			}
		} catch (SQLException e) {
			log.warn("Could not load " + database + ". Falling back to default database.", e);
		}
		synchronized (counter) {
			username = "APP_" + (counter++);
		}
		// Fail safe is in-memory derby
		try {
			result = new DatabaseHomomorphismDetector("org.apache.derby.jdbc.EmbeddedDriver",
					"jdbc:derby:memory:{1};create=true", "chase", username, "", new DerbyStatementBuilder(),
					schema, ((FactDatabaseManager)manager).getToDatabaseTables());
			return result;
		} catch (SQLException e) {
			throw new IllegalStateException("Could not load default database.");
		}
	}
}