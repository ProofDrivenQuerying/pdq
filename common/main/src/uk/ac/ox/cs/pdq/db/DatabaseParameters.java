package uk.ac.ox.cs.pdq.db;
import java.io.File;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.Parameters;
import uk.ac.ox.cs.pdq.data.memory.MemoryDatabaseInstance;


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

	public static final DatabaseParameters MySql = getDefaultForMySql();
	public static final DatabaseParameters Postgres = getDefaultForPostgres();
	public static final DatabaseParameters Derby = getDefaultForDerby();
	public static final DatabaseParameters Memory = getDefaultForMemory();
	public static final DatabaseParameters Empty = new DatabaseParameters();
	
	/**
	 * Constructor for DatabaseParameters using default configuration file path.
	 */
	private DatabaseParameters() {
		super(new File(DEFAULT_CONFIG_FILE_PATH), false, false);
	}

	private static DatabaseParameters getDefaultForMySql() {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setConnectionUrl("jdbc:mysql://localhost/");
		dbParam.setDatabaseDriver("com.mysql.jdbc.Driver");
		dbParam.setDatabaseName("pdq");
		dbParam.setDatabaseUser("root");
		dbParam.setDatabasePassword("root");
		return dbParam; 
	}
	
	private static DatabaseParameters getDefaultForMemory() {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setDatabaseDriver(MemoryDatabaseInstance.class.getName());
		dbParam.setDatabaseName("pdq");
		//dbParam.setProperty("database.isvirtual","true");		
		return dbParam; 
	}
	
	private static DatabaseParameters getDefaultForDerby() {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setConnectionUrl("jdbc:derby:memory:{1};create=true");
		dbParam.setDatabaseDriver("org.apache.derby.jdbc.EmbeddedDriver");
		dbParam.setDatabaseName("pdq");
		dbParam.setDatabaseUser("root");
		dbParam.setDatabasePassword("root");
		return dbParam; 
	}
	
	private static DatabaseParameters getDefaultForPostgres() {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setConnectionUrl("jdbc:postgresql://localhost/");
		dbParam.setDatabaseDriver("org.postgresql.Driver");
		dbParam.setDatabaseName("pdq");
		dbParam.setDatabaseUser("postgres");
		dbParam.setDatabasePassword("root");
		return dbParam; 
	}

	/**
	 * Constructor for DatabaseParameters.
	 * @param config path to the configuration file to read
	 */
	public DatabaseParameters(File config) {
		super(config, false, false);
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
	 *
	 * @return String
	 */
	public String getDatabaseDriver() {
		return this.databaseDriver;
	}

	/**
	 *
	 * @return String
	 */
	public String getConnectionUrl() {
		return this.connectionUrl;
	}

	/**
	 *
	 * @return String
	 */
	public String getDatabaseName() {
		return this.databaseName;
	}

	/**
	 *
	 * @return String
	 */
	public String getDatabasePassword() {
		return this.databasePassword;
	}

	/**
	 *
	 * @return String
	 */
	public String getDatabaseUser() {
		return this.databaseUser;
	}
	
	/**
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
	 *
	 * @param connectionUrl String
	 */
	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	/**
	 *
	 * @param databaseName String
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 *
	 * @param databasePassword String
	 */
	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	/**
	 *
	 * @param databaseUser String
	 */
	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}
}
