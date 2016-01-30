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

	/** */
	private static final long serialVersionUID = -8077300774514524509L;

	private static Logger log = Logger.getLogger(ReasoningParameters.class);

	/** Properties file name */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-reasoning.properties";

	/** Properties file path */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Constructor for PlannerParameters using default configuration file path.
	 */
	public ReasoningParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH), false, false);
	}

	/**
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
	 */
	public ReasoningParameters(File config, boolean verbose) {
		this(config, false, verbose, false);
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
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param strict if true, param loading problem will throw an exception
	 */
	public ReasoningParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, true, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}

	@Parameter(description="Canonical name of the driver class for the internal"
			+ " database used by the reasoner")
	protected String databaseDriver;

	@Parameter(description="Connection URL for the internal database used by the reasoner")
	protected String connectionUrl;

	@Parameter(description="Name of the internal database used by the reasoner")
	protected String databaseName;

	@Parameter(description="Username for the internal database used by the reasoner")
	protected String databaseUser;

	@Parameter(description="Password for the internal database used by the reasoner")
	protected String databasePassword;

	@Parameter(description="Type of reasoning to use.", defaultValue="RESTRICTED_CHASE")
	protected ReasoningTypes reasoningType = ReasoningTypes.RESTRICTED_CHASE;

	@Parameter(description = "Type of the homomorphism detected infrastructure")
	protected HomomorphismDetectorTypes homomorphismDetectorType;
	
	@Parameter(description = "Number of rounds of rule firings to perform, in "
			+ "a single application of the chase. "
			+ "\nOnly applies to KTERMINATION_CHASE reasoning type.",
			defaultValue = "Integer.MAX_VALUE")
	protected Integer terminationK = Integer.MAX_VALUE;

	@Parameter(description = "In true, the initial configuration is full "
			+ "initialized at the beginning of a planning algorithm.\n"
			+ "Only applies to DAG planning algorithms",
			defaultValue = "false")
	protected Boolean fullInitialization = Boolean.FALSE;

	/**
	 * @return String
	 */
	public String getDatabaseDriver() {
		return this.databaseDriver;
	}

	/**
	 * @return String
	 */
	public String getConnectionUrl() {
		return this.connectionUrl;
	}

	/**
	 * @return String
	 */
	public String getDatabaseName() {
		return this.databaseName;
	}

	/**
	 * @return String
	 */
	public String getDatabasePassword() {
		return this.databasePassword;
	}

	/**
	 * @return String
	 */
	public String getDatabaseUser() {
		return this.databaseUser;
	}
	
	/**
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
	 * @param connectionUrl String
	 */
	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	/**
	 * @param databaseName String
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * @param databasePassword String
	 */
	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	/**
	 * @param databaseUser String
	 */
	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}

	/**
	 * @return ReasoningTypes
	 */
	public ReasoningTypes getReasoningType() {
		return this.reasoningType;
	}

	/**
	 * @param reasoningType ReasoningTypes
	 */
	public void setReasoningType(ReasoningTypes reasoningType) {
		this.reasoningType = reasoningType;
	}

	/**
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
	 * @return HomomorphismDetectorTypes
	 */
	public HomomorphismDetectorTypes getHomomorphismDetectorType() {
		if (this.homomorphismDetectorType == null) {
			return HomomorphismDetectorTypes.DATABASE;
		}
		return this.homomorphismDetectorType;
	}

	/**
	 * @param type HomomorphismDetectorTypes
	 */
	public void setHomomorphismDetectorType(HomomorphismDetectorTypes type) {
		this.homomorphismDetectorType = type;
	}

	/**
	 * @param type String
	 */
	public void setHomomorphismDetectorType(String type) {
		try {
			this.homomorphismDetectorType = HomomorphismDetectorTypes.valueOf(type);
		} catch (IllegalArgumentException e) {
			log.warn("Setting homomorphism checker type to " + HomomorphismDetectorTypes.DATABASE, e);
			this.homomorphismDetectorType = HomomorphismDetectorTypes.DATABASE;
		}
	}
	
	/**
	 * @return Boolean
	 */
	public Boolean getFullInitialization() {
		return this.fullInitialization;
	}

	/**
	 * @param b Boolean
	 */
	public void setFullInitialization(Boolean b) {
		this.fullInitialization = b;
	}
	
	/**
	 * @return Integer
	 */
	public Integer getTerminationK() {
		return this.terminationK;
	}

	/**
	 * @param terminationK Number
	 */
	public void setTerminationK(Number terminationK) {
		this.terminationK = terminationK != null ? terminationK.intValue() : null;
	}

	/**
	 * @param terminationK Integer
	 */
	public void setTerminationK(Integer terminationK) {
		this.terminationK = terminationK;
	}


	/** */
	public static enum ReasoningTypes {
		
		@EnumParameterValue(description = "Restricted chase algorithm. Fires only dependencies that are not already satisfied.")
		RESTRICTED_CHASE, 
		
		@EnumParameterValue(description = "Restricted chase, where the number of rule firing rounds is bounded by a constant K")
		KTERMINATION_CHASE, 
		
		@EnumParameterValue(description = "Restricted chase, where the number of rule firing rounds is bounded by a constant K")
		BOUNDED_CHASE,
		
		@EnumParameterValue(description = "Runs the parallel EGD chase algorithm")
		PARALLEL_EGD_CHASE,
		
		@EnumParameterValue(description = "Runs a sequential version of the EGD chase algorithm")
		SEQUENTIAL_EGD_CHASE
	}

	/** */
	public static enum HomomorphismDetectorTypes {
		@EnumParameterValue(description = "Homomorphism detection relying on an internal relational database")
		DATABASE;
	}

}
