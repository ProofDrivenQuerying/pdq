package uk.ac.ox.cs.pdq.db;
import java.io.File;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.Parameters;


/**
 * Holds the parameters of a database session.
 *
 * @author George K
 */
public class DatabaseParameters extends Parameters {


	private static final long serialVersionUID = -8077300774514524509L;


	private static Logger log = Logger.getLogger(DatabaseParameters.class);

	/**  Properties file name. */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-reasoning.properties";

	/**  Properties file path. */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Constructor for DatabaseParameters using default configuration file path.
	 */
	public DatabaseParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH), false, false);
	}

	/**
	 * Constructor for DatabaseParameters.
	 * @param config path to the configuration file to read
	 */
	public DatabaseParameters(File config) {
		this(config, false, false);
	}


	/**
	 * Constructor for DatabaseParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public DatabaseParameters(File config, boolean verbose, boolean strict) {
		this(config, false, verbose, strict);
	}

	/**
	 * Constructor for DatabaseParameters.
	 *
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose the verbose
	 * @param strict if true, param loading problem will throw an exception
	 */
	public DatabaseParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, true, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}

	/** The database driver. */
	@Parameter(description="Canonical name of the driver class for the internal"
			+ " database used by the reasoner")
	protected String databaseDriver;

	/** The connection url. */
	@Parameter(description="Connection URL for the internal database used by the reasoner")
	protected String connectionUrl;

	/** The database name. */
	@Parameter(description="Name of the internal database used by the reasoner")
	protected String databaseName;

	/** The database user. */
	@Parameter(description="Username for the internal database used by the reasoner")
	protected String databaseUser;

	/** The database password. */
	@Parameter(description="Password for the internal database used by the reasoner")
	protected String databasePassword;

	/**
	 * Gets the database driver.
	 *
	 * @return String
	 */
	public String getDatabaseDriver() {
		return this.databaseDriver;
	}

	/**
	 * Gets the connection url.
	 *
	 * @return String
	 */
	public String getConnectionUrl() {
		return this.connectionUrl;
	}

	/**
	 * Gets the database name.
	 *
	 * @return String
	 */
	public String getDatabaseName() {
		return this.databaseName;
	}

	/**
	 * Gets the database password.
	 *
	 * @return String
	 */
	public String getDatabasePassword() {
		return this.databasePassword;
	}

	/**
	 * Gets the database user.
	 *
	 * @return String
	 */
	public String getDatabaseUser() {
		return this.databaseUser;
	}
	
	/**
	 * Sets the database driver.
	 *
	 * @param d String
	 */
	public void setDatabaseDriver(String d) {
		try {
			Class.forName(d);
			this.databaseDriver = d;
		} catch (ClassNotFoundException e) {
			log.warn("No such database driver '" + d + "'. Ignoring");
			this.databaseDriver = null;
		}
	}

	/**
	 * Sets the connection url.
	 *
	 * @param connectionUrl String
	 */
	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	/**
	 * Sets the database name.
	 *
	 * @param databaseName String
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * Sets the database password.
	 *
	 * @param databasePassword String
	 */
	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	/**
	 * Sets the database user.
	 *
	 * @param databaseUser String
	 */
	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}
}
