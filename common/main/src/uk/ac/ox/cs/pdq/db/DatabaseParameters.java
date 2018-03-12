package uk.ac.ox.cs.pdq.db;
import java.io.File;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import uk.ac.ox.cs.pdq.Parameters;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;


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
	static final int DEFAULT_NUMBER_OF_THREADS = 10;
	static final String NUMBER_OF_THREADS_PROPERTY = "number.of.threads";

	public static final DatabaseParameters MySql = getDefaultForMySql();
	public static final DatabaseParameters Postgres = getDefaultForPostgres();
	public static final DatabaseParameters Empty = new DatabaseParameters();
	public static final DatabaseParameters Internal = getDefaultForInternal();
	
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
	
	/** The number of threads. */
	@Parameter(description="The number of threads and connections to access the database")
	private int numberOfThreads = DEFAULT_NUMBER_OF_THREADS;

	/** The number of threads. */
	@Parameter(description="True in case the internal database manager should be used")
	private boolean useInternalDatabaseManager = false;


	@Parameter(description="The database should have a constraint for making every fact unique. Default is false.")
	private boolean factsAreUnique = false;
	
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
		dbParam.setNumberOfThreads(DEFAULT_NUMBER_OF_THREADS);
		return dbParam; 
	}
	
	/** 
	 * This is not supported since 2018 feb 14.
	 */
	public static DatabaseParameters getDefaultForDerby() {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setConnectionUrl("jdbc:derby:memory:{1};create=true");
		dbParam.setDatabaseDriver("org.apache.derby.jdbc.EmbeddedDriver");
		dbParam.setDatabaseName("pdq");
		dbParam.setDatabaseUser("APP_" + GlobalCounterProvider.getNext("DatabaseConnectionName"));
		dbParam.setDatabasePassword("");
		dbParam.setNumberOfThreads(DEFAULT_NUMBER_OF_THREADS);
		
		return dbParam; 
	}
	
	private static DatabaseParameters getDefaultForPostgres() {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setConnectionUrl("jdbc:postgresql://localhost/");
		dbParam.setDatabaseDriver("org.postgresql.Driver");
		dbParam.setDatabaseName("pdq");
		dbParam.setDatabaseUser("postgres");
		dbParam.setDatabasePassword("root");
				
//		dbParam.setDatabaseName("pdq");
//		dbParam.setDatabaseName("PDQ");
//		dbParam.setDatabaseUser("pdq");
//		dbParam.setDatabasePassword("root");
//		dbParam.setDatabaseUser("gabor");
//		dbParam.setDatabasePassword("");
		dbParam.setNumberOfThreads(DEFAULT_NUMBER_OF_THREADS);
		return dbParam; 
	}
	
	private static DatabaseParameters getDefaultForInternal() {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setNumberOfThreads(DEFAULT_NUMBER_OF_THREADS);
		dbParam.setUseInternalDatabaseManager(true);
		return dbParam; 
	}

	/**
	 * Constructor for DatabaseParameters.
	 * @param config path to the configuration file to read
	 */
	public DatabaseParameters(File config) {
		super(config, false, false);
	}

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
		this.databaseName = validateDatabaseName(databaseName);
	}
	/**
	 * Limits the max lengh of the database name, makes sure it is all uppercase.
	 * 
	 * @param databaseIn
	 * @return
	 */
	private String validateDatabaseName(String databaseIn) {
		String newDatabaseName = databaseIn;
		if (Strings.isNullOrEmpty(newDatabaseName)) {
			newDatabaseName = "pdq";
		}
		// database name cannot be longer then 128 character, so if it is close we
		// shorten it,
		if (newDatabaseName.length() > 126) {
			newDatabaseName = newDatabaseName.substring(0, 126);
		}
		return newDatabaseName;
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

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	public void setNumberOfThreads(String numberOfThreads) {
		this.numberOfThreads = Integer.parseInt(numberOfThreads);
	}
	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public boolean getUseInternalDatabaseManager() {
		return useInternalDatabaseManager;
	}
	public void setUseInternalDatabaseManager(boolean useInternalDatabaseManager) {
		this.useInternalDatabaseManager = useInternalDatabaseManager;
	}

	public boolean isFactsAreUnique() {
		return factsAreUnique;
	}
	public boolean getFactsAreUnique() {
		return factsAreUnique;
	}
	public void setFactsAreUnique(boolean factsAreUnique) {
		this.factsAreUnique = factsAreUnique;
	}
	
}
