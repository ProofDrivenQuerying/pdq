package uk.ac.ox.cs.pdq.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.runtime.exec.Middleware.ControlFlows;

/**
 * Hold the configuration of an runtime execution.
 * 
 * @author Julien Leblay
 */
public class RuntimeParameters extends uk.ac.ox.cs.pdq.Parameters {

	/** */
	private static final long serialVersionUID = -5183026336708644030L;

	/** Logger. */
	private static Logger log = Logger.getLogger(RuntimeParameters.class);

	/** Properties file name */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-runtime.properties";

	/** Properties file path */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;
	
	@Parameter(description="Type of executor to use in plan evaluation")
	protected ExecutorTypes executorType;

	@Parameter(description="Semantics to use in plan execution",
			defaultValue = "SET")
	protected Semantics semantics = Semantics.SET;
	
	@Parameter(description="The maximum number of output tuples")
	protected Integer tuplesLimit;

	@Parameter(description="Canonical name of the driver class for the internal"
			+ " database used by the reasoner")
	protected String databaseDriver;

	@Parameter(description="Connection URL for the database for sql query evaluation")
	protected String connectionUrl;

	@Parameter(description="Name of the internal database used for sql query evaluation")
	protected String databaseName;

	@Parameter(description="Username for the internal database used for sql query evaluation")
	protected String databaseUser;

	@Parameter(description="Password for the internal database used for sql query evaluation")
	protected String databasePassword;

	@Parameter(description="Enable caching at access level")
	protected Boolean doCache;
	
	@Parameter(description = "Plan control flow.\n ",
			defaultValue = "PULL")
	protected ControlFlows controlFlow = ControlFlows.PULL;
	
	/**
	 * @return ControlFlows
	 */
	public ControlFlows getControlFlow() {
		return this.controlFlow;
	}

	/**
	 * @param controlFlow ControlFlows
	 */
	public void setControlFlow(ControlFlows controlFlow) {
		this.controlFlow = controlFlow;
	}

	/**
	 * @param controlFlow String
	 */
	public void setControlFlow(String controlFlow) {
		this.controlFlow = ControlFlows.valueOf(String.valueOf(controlFlow).toUpperCase());
	}

	/**
	 * @return the version of the planner code, as given by Maven
	 */
	public static String getVersion() {
	    String path = "/runtime.version";
	    try (InputStream stream = RuntimeParameters.class.getResourceAsStream(path)) {
		    if (stream == null) {
				return "UNKNOWN";
			}
		    Properties props = new Properties();
	        props.load(stream);
	        stream.close();
	        return (String) props.get("version");
	    } catch (IOException e) {
	    	log.debug(e);
			return "UNKNOWN";
	    }
	}

	/**
	 * Constructor for RuntimeParameters using default configuration file path.
	 */
	public RuntimeParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH));
	}

	/**
	 * Constructor for RuntimeParameters.
	 * @param config path to the configuration file to read
	 */
	public RuntimeParameters(File config) {
		this(config, false, false, false);
	}

	/**
	 * Constructor for RuntimeParameters.
	 * @param config path to the configuration file to read
	 * @param verbose the verbose
	 */
	public RuntimeParameters(File config, boolean verbose) {
		this(config, false, verbose, false);
	}

	/**
	 * Constructor for RuntimeParameters.
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose if true, param loading problem will be reported
	 */
	public RuntimeParameters(File config, boolean delay, boolean verbose) {
		super(config, true, verbose, false);
		if (!delay) {
			this.load(config, verbose, false);
		}
	}

	/**
	 * Constructor for RuntimeParameters.
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public RuntimeParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, true, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}

	/**
	 * @return ExecutorTypes
	 */
	public ExecutorTypes getExecutorType() {
		return this.executorType;
	}

	/**
	 * @param executorType ExecutorTypes
	 */
	public void setExecutorType(ExecutorTypes executorType) {
		this.executorType = executorType;
	}

	public void unsetExecutorType() {
		this.executorType = null;
	}

	/**
	 * @param executorType String
	 */
	public void setExecutorType(String executorType) {
		this.executorType = ExecutorTypes.valueOf(executorType);
	}

	/**
	 * @return Semantics
	 */
	public Semantics getSemantics() {
		if (this.semantics == null) {
			return Semantics.SET;
		}
		return this.semantics;
	}

	/**
	 * @param semantics Semantics
	 */
	public void setSemantics(Semantics semantics) {
		this.semantics = semantics;
	}

	/**
	 * @param s String
	 */
	public void setSemantics(String s) {
		try {
			this.semantics = Semantics.valueOf(s);
		} catch (IllegalArgumentException e) {
			log.debug(e);
			log.warn("Setting semantics type to " + Semantics.SET);
			this.semantics = Semantics.SET;
		}
	}
	
	public Integer getTuplesLimit() {
		if (this.tuplesLimit == null) {
			return Integer.MAX_VALUE;
		}
		return this.tuplesLimit;
	}
	
	public void setTuplesLimit(Integer tuples) {
		this.tuplesLimit = tuples;
	}
	
	public void setTuplesLimit(Number tuples) {
		this.tuplesLimit = tuples != null ? tuples.intValue() : null;
	}
	
	/**
	 * @return Boolean
	 */
	public Boolean getDoCache() {
		return this.doCache == null ? true : this.doCache;
	}

	/**
	 * @param orderAware Boolean
	 */
	public void setDoCache(Boolean doCache) {
		this.doCache = doCache;
	}

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
	 * @param connectionUrl String
	 */
	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	/**
	 * @param databaseDriver String
	 */
	public void setDatabaseDriver(String driver) {
		this.databaseDriver = driver;
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

	/** Executor types */
	public static enum ExecutorTypes {
		@EnumParameterValue(description="Volcano-style pipelining iterator execution")
		PIPELINED, 
		
		@EnumParameterValue(description=
		"Executes a query by translating it to a nested SQL query, "
				+ "and delegating its execution to an external RDBMS.")
		SQL_TREE, 
		
		@EnumParameterValue(description=
		"Executes a query by translating it to a sequence of SQL queries,  "
				+ "and delegating its execution to an external RDBMS."
				+ "Each query is materialized and possibly relies on a "
				+ "previously materialized one.")
		SQL_STEP, 

		@EnumParameterValue(description=
		"Executes a query by translating it to a SQL WITH query, "
				+ "and delegating its execution to an external RDBMS.")
		SQL_WITH}

	/** Semantics */
	public static enum Semantics {
		@EnumParameterValue(description="Use set semantics in runtime evaluation")
		SET,
		@EnumParameterValue(description="Use bag semantics in runtime evaluation")
		BAG
	}
}
