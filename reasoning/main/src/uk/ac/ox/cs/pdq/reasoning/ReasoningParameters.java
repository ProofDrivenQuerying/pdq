package uk.ac.ox.cs.pdq.reasoning;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.Parameters;

/**
 * Holds the parameters of a reasoning session.
 *
 * @author Efthymia Tsamoura
 */
public class ReasoningParameters extends Parameters {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8077300774514524509L;

	/** The log. */
	private static Logger log = Logger.getLogger(ReasoningParameters.class);

	/**  Properties file name. */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-reasoning.properties";

	/**  Properties file path. */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Constructor for PlannerParameters using default configuration file path.
	 */
	public ReasoningParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH), false, false);
	}

	/**
	 * Gets the version.
	 *
	 * @return the version of the planner code, as given by Maven
	 */
	public static String getVersion() {
		String path = "/planner.version";
		try (InputStream stream = ReasoningParameters.class.getResourceAsStream(path)) {
			if (stream == null) {
				return "UNKNOWN";
			}
			Properties props = new Properties();
			props.load(stream);
			stream.close();
			return (String) props.get("version");
		} catch (IOException e) {
			log.warn(e);
			return "UNKNOWN";
		}
	}

	/**
	 * Constructor for PlannerParameters.
	 * @param config path to the configuration file to read
	 */
	public ReasoningParameters(File config) {
		this(config, false, false);
	}

	/**
	 * Constructor for PlannerParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public ReasoningParameters(File config, boolean verbose, boolean strict) {
		this(config, false, verbose, strict);
	}

	/**
	 * Constructor for PlannerParameters.
	 *
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose the verbose
	 * @param strict if true, param loading problem will throw an exception
	 */
	public ReasoningParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, verbose, strict);
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

	/** The reasoning type. */
	@Parameter(description="Type of reasoning to use.", defaultValue="RESTRICTED_CHASE")
	protected ReasoningTypes reasoningType = ReasoningTypes.RESTRICTED_CHASE;

	/** The termination k. */
	@Parameter(description = "Number of rounds of rule firings to perform, in "
			+ "a single application of the chase. "
			+ "\nOnly applies to KTERMINATION_CHASE reasoning type.",
			defaultValue = "10")
	protected Integer terminationK = 10;

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

	/**
	 * Gets the reasoning type.
	 *
	 * @return ReasoningTypes
	 */
	public ReasoningTypes getReasoningType() {
		return this.reasoningType;
	}

	/**
	 * Sets the reasoning type.
	 *
	 * @param reasoningType ReasoningTypes
	 */
	public void setReasoningType(ReasoningTypes reasoningType) {
		this.reasoningType = reasoningType;
	}

	/**
	 * Sets the reasoning type.
	 *
	 * @param reasoningType String
	 */
	public void setReasoningType(String reasoningType) {
		try {
			this.reasoningType = ReasoningTypes.valueOf(reasoningType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting reasoning type to " + null, e);
			this.reasoningType = null;
		}
	}
	
	/**
	 * Gets the termination k.
	 *
	 * @return Integer
	 */
	public Integer getTerminationK() {
		return this.terminationK;
	}

	/**
	 * Sets the termination k.
	 *
	 * @param terminationK Number
	 */
	public void setTerminationK(Number terminationK) {
		this.terminationK = terminationK != null ? terminationK.intValue() : null;
	}

	/**
	 * Sets the termination k.
	 *
	 * @param terminationK Integer
	 */
	public void setTerminationK(Integer terminationK) {
		this.terminationK = terminationK;
	}


	/**
	 * The Enum ReasoningTypes.
	 */
	public static enum ReasoningTypes {
		
		/** The restricted chase. */
		@EnumParameterValue(description = "Restricted chase algorithm. Fires only dependencies that are not already satisfied.")
		RESTRICTED_CHASE, 
		
		/** The ktermination chase. */
		@EnumParameterValue(description = "Restricted chase, where the number of rule firing rounds is bounded by a constant K")
		KTERMINATION_CHASE, 
		
		/** The parallel egd chase. */
		@EnumParameterValue(description = "Runs the parallel EGD chase algorithm")
		PARALLEL_EGD_CHASE,
	}

}
