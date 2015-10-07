package uk.ac.ox.cs.pdq.planner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.Parameters;


/**
 * Holds the parameters of a planning session.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class PlannerParameters extends Parameters {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8077300774514524509L;

	/** The log. */
	private static Logger log = Logger.getLogger(PlannerParameters.class);

	/**  Properties file name. */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-planner.properties";

	/**  Properties file path. */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Constructor for PlannerParameters using default configuration file path.
	 */
	public PlannerParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH), false, false);
	}

	/**
	 * Gets the version.
	 *
	 * @return the version of the planner code, as given by Maven
	 */
	public static String getVersion() {
		String path = "/planner.version";
		try (InputStream stream = PlannerParameters.class.getResourceAsStream(path)) {
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
	public PlannerParameters(File config) {
		this(config, false, false);
	}

	/**
	 * Constructor for PlannerParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 */
	public PlannerParameters(File config, boolean verbose) {
		this(config, false, verbose, false);
	}

	/**
	 * Constructor for PlannerParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public PlannerParameters(File config, boolean verbose, boolean strict) {
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
	public PlannerParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, true, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}

	/** The database driver. */
	@Parameter(description="Canonical name of the driver class for the internal"
			+ " database used by the reasoner",
			defaultValue="org.apache.derby.jdbc.EmbeddedDriver")
	protected String databaseDriver = "org.apache.derby.jdbc.EmbeddedDriver";

	/** The connection url. */
	@Parameter(description="Connection URL for the internal database used by the reasoner",
			defaultValue = "jdbc:derby:memory:{1};create=true")
	protected String connectionUrl = "jdbc:derby:memory:{1};create=true";

	/** The database name. */
	@Parameter(description="Name of the internal database used by the reasoner")
	protected String databaseName;

	/** The database user. */
	@Parameter(description="Username for the internal database used by the reasoner")
	protected String databaseUser;

	/** The database password. */
	@Parameter(description="Password for the internal database used by the reasoner")
	protected String databasePassword;

	/** The max iterations. */
	@Parameter(description="The maximum number of iterations to perform in "
			+ "planning.\nThis may have different semantic depending of which "
			+ "planning algorithm is used.",
			defaultValue = "Integer.MAX_VALUE")
	protected Integer maxIterations = Integer.MAX_VALUE;

	/** The log intervals. */
	@Parameter(description="Interval (in number of iterations) between "
			+ "which detailed executions informations are logged.",
			defaultValue = "10")
	protected Integer logIntervals = 10;

	/** The short log intervals. */
	@Parameter(description="Interval (in number of iterations) between "
			+ "which succint executions informations are logged.",
			defaultValue = "1")
	protected Integer shortLogIntervals = 1;

	/** The output log path. */
	@Parameter(description="Path of the output file where to store the logs "
			+ "(optional). If missing, logs are printed to STDOUT")
	protected String outputLogPath;

	/** The planner type. */
	@Parameter(description="Type of planning algorithm to use.")
	protected PlannerTypes plannerType;

	/** The query match interval. */
	@Parameter(description="Number of exploration interval to wait for between "
			+ "query match checks.\n Use in linear planning algorithms only.")
	protected Integer queryMatchInterval;

	/** The blocking interval. */
	@Parameter(description="Number of exploration interval to wait for between "
			+ "blocking checks.")
	protected Integer blockingInterval;

	/** The reasoning type. */
	@Parameter(description="Type of reasoning to use.", defaultValue="RESTRICTED_CHASE")
	protected ReasoningTypes reasoningType = ReasoningTypes.RESTRICTED_CHASE;

	/** The homomorphism detector type. */
	@Parameter(description = "Type of the homomorphism detected infrastructure")
	protected HomomorphismDetectorTypes homomorphismDetectorType;

	/** The post pruning type. */
	@Parameter(description = "Type of post-pruning. This is only used in "
			+ "optimizer linear planning")
	protected PostPruningTypes postPruningType;

	/** The chase interval. */
	@Parameter(description = "Number of intervals between which to run the chase")
	protected Integer chaseInterval;

	/** The max depth. */
	@Parameter(description = 
			"Maximum depth of the exploration.\nThis may have different "
			+ "semantic depending of which planning algorithm is used.")
	protected Integer maxDepth;

	/** The max bushiness. */
	@Parameter(description = "Maximum level of bushiness allowed.\n This is "
			+ "only used with ILP-types of planning algorithms",
			defaultValue = "Integer.MAX_VALUE")
	protected Integer maxBushiness = Integer.MAX_VALUE;

	/** The range. */
	@Parameter(description = "Range configurations to consider as inputs to "
			+ "each subsequent phase of a DAG planning algorithm.\n"
			+ "Use in conjunction to DISTANCE2CORETOPK priority assessor",
			defaultValue = "Integer.MAX_VALUE")
	protected Integer range = Integer.MAX_VALUE;

	/** The exception on limit. */
	@Parameter(description = 
			"If true, a LimitReachedException is thrown during planning if a "
			+ "limit (e.g. time or max no. interactions) is reached.\n"
			+ "Otherwise, the event is logged and the planning completes "
			+ "gracefully")
	protected Boolean exceptionOnLimit;

	/** The validator type. */
	@Parameter(description = "Type of validator to use. Only required in "
			+ "conjunction with DAG planning algorithms",
			defaultValue = "DEFAULT")
	protected ValidatorTypes validatorType = ValidatorTypes.DEFAULT_VALIDATOR;

	/** The priority assessor type. */
	@Parameter(description = "Type of priority assessor to use. Only required "
			+ "in conjunction with DAG planning algorithms",
			defaultValue = "DEFAULT")
	protected PriorityAssessorTypes priorityAssessorType = PriorityAssessorTypes.DEFAULT;

	/** The filter type. */
	@Parameter(description = "Type of filter to use. Only required in "
			+ "conjunction with DAG planning algorithms")
	protected FilterTypes filterType;

	/** The dominance type. */
	@Parameter(description = "Type of dominance checks to use. Only required "
			+ "in conjunction with DAG planning algorithms",
			defaultValue = "STRICT_OPEN")
	protected DominanceTypes dominanceType = DominanceTypes.OPEN;

	/** The success dominance type. */
	@Parameter(description =
			"Type of sucess dominance checks to use. Only required in "
			+ "conjunction with DAG planning algorithms",
			defaultValue = "OPEN")
	protected SuccessDominanceTypes successDominanceType = SuccessDominanceTypes.OPEN;

	/** The follow up handling. */
	@Parameter(description = "Specifies how follow-up joins should be handled."
			+ "\nOnly applies to DAG planning algorithms",
			defaultValue = "MINIMAL")
	protected FollowUpHandling followUpHandling = FollowUpHandling.MINIMAL;

	/** The iterative executor type. */
	@Parameter(description = "Type of iterative executor to use. Only applies "
			+ "to DAG planning algorithms.",
			defaultValue = "MULTITHREADED")
	protected IterativeExecutorTypes iterativeExecutorType = IterativeExecutorTypes.MULTITHREADED;

	/** The first phase threads. */
	@Parameter(description = "Number of threads to use in the first phase of "
			+ "a parallel DAG planning algorithm",
			defaultValue = "50")
	protected Integer firstPhaseThreads = 50;

	/** The second phase threads. */
	@Parameter(description = "Number of threads to use in the second phase of "
			+ "a parallel DAG planning algorithm",
			defaultValue = "50")
	protected Integer secondPhaseThreads = 50;

	/** The depth threshold. */
	@Parameter(description = "Threshold for the DEPTH_THROTTLING validator",
			defaultValue = "2")
	protected Integer depthThreshold = 2;

	/** The topk. */
	@Parameter(description = "Top-K configurations to consider as inputs to "
			+ "each subsequent phase of a DAG planning algorithm.\n"
			+ "Use in conjunction to ILP SI planning algorithm",
			defaultValue = "4")
	protected Integer topk = 4;

	/** The top configurations. */
	@Parameter(description = "Top configurations to consider as inputs to "
			+ "each subsequent phase of a DAG planning algorithm.\n"
			+ "Use in conjunction to DISTANCE2CORETOPK priority assessor",
			defaultValue = "Integer.MAX_VALUE")
	protected Integer topConfigurations = Integer.MAX_VALUE;

	/** The termination k. */
	@Parameter(description = "Number of rounds of rule firings to perform, in "
			+ "a single application of the chase. "
			+ "\nOnly applies to KTERMINATION_CHASE reasoning type.",
			defaultValue = "Integer.MAX_VALUE")
	protected Integer terminationK = Integer.MAX_VALUE;

	/** The full initialization. */
	@Parameter(description = "In true, the initial configuration is full "
			+ "initialized at the beginning of a planning algorithm.\n"
			+ "Only applies to DAG planning algorithms",
			defaultValue = "false")
	protected Boolean fullInitialization = Boolean.FALSE;

	/** The reachability filtering. */
	@Parameter(description = "If true, relation that are not reachable from "
			+ "the input query through the firing of constraints are not "
			+ "considered in the plan search.\n Only applies to DAG planning "
			+ "algorithms",
			defaultValue = "false")
	protected Boolean reachabilityFiltering = false;

	/** The order aware. */
	@Parameter(description = "If true, all join orders are considered during "
			+ "plan search",
			defaultValue = "true")
	protected Boolean orderAware = true;
	
	/** The zombification. */
	@Parameter(description = "If true, then we perform (de)zombification during optimised linear plan exploration "
			+ "plan search",
			defaultValue = "true")
	protected Boolean zombification;
	
	
	/** The access file. */
	@Parameter(description = "Contains the desired list of accesses")
	protected String accessFile;

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
	 * Gets the log intervals.
	 *
	 * @return Integer
	 */
	public Integer getLogIntervals() {
		return this.logIntervals;
	}

	/**
	 * Gets the max iterations.
	 *
	 * @return Integer
	 */
	public Integer getMaxIterations() {
		return this.maxIterations;
	}

	/**
	 * Gets the short log intervals.
	 *
	 * @return Integer
	 */
	public Integer getShortLogIntervals() {
		return this.shortLogIntervals;
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
	 * Sets the log intervals.
	 *
	 * @param logIntervals Number
	 */
	public void setLogIntervals(Number logIntervals) {
		this.logIntervals = logIntervals != null ? logIntervals.intValue() : null;
	}

	/**
	 * Sets the max iterations.
	 *
	 * @param maxIterations Number
	 */
	public void setMaxIterations(Number maxIterations) {
		this.maxIterations = maxIterations != null ? maxIterations.intValue() : null;
	}

	/**
	 * Sets the max iterations.
	 *
	 * @param maxIterations String
	 */
	public void setMaxIterations(String maxIterations) {
		log.debug("Setting max iteration to infinity");
		this.maxIterations = Integer.MAX_VALUE;
	}

	/**
	 * Sets the short log intervals.
	 *
	 * @param shortLogIntervals Number
	 */
	public void setShortLogIntervals(Number shortLogIntervals) {
		this.shortLogIntervals = shortLogIntervals != null ? shortLogIntervals.intValue() : null;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param s String
	 */
	public void setTimeout(String s) {
		log.debug("Setting timeout to infinity");
		this.timeout = Double.POSITIVE_INFINITY;
	}

	/**
	 * Gets the output log path.
	 *
	 * @return String
	 */
	public String getOutputLogPath() {
		return this.outputLogPath;
	}

	/**
	 * Sets the output log path.
	 *
	 * @param outputLogPath String
	 */
	public void setOutputLogPath(String outputLogPath) {
		this.outputLogPath = outputLogPath;
	}

	/**
	 * Gets the planner type.
	 *
	 * @return PlannerTypes
	 */
	public PlannerTypes getPlannerType() {
		if (this.plannerType == null) {
			return PlannerTypes.DAG_OPTIMIZED;
		}
		return this.plannerType;
	}

	/**
	 * Sets the planner type.
	 *
	 * @param plannerType PlannerTypes
	 */
	public void setPlannerType(PlannerTypes plannerType) {
		this.plannerType = plannerType;
	}

	/**
	 * Sets the planner type.
	 *
	 * @param plannerType String
	 */
	public void setPlannerType(String plannerType) {
		try {
			this.plannerType = PlannerTypes.valueOf(plannerType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting planner type to " + null, e);
			this.plannerType = null;
		}
	}

	/**
	 * Gets the query match interval.
	 *
	 * @return Integer
	 */
	public Integer getQueryMatchInterval() {
		if (this.queryMatchInterval == null) {
			return 1;
		}
		return this.queryMatchInterval;
	}

	/**
	 * Sets the query match interval.
	 *
	 * @param queryMatchInterval Number
	 */
	public void setQueryMatchInterval(Number queryMatchInterval) {
		this.queryMatchInterval = queryMatchInterval != null ? queryMatchInterval.intValue() : null;
	}

	/**
	 * Gets the blocking interval.
	 *
	 * @return Integer
	 */
	public Integer getBlockingInterval() {
		return this.blockingInterval;
	}

	/**
	 * Sets the blocking interval.
	 *
	 * @param blockingInterval Number
	 */
	public void setBlockingInterval(Number blockingInterval) {
		this.blockingInterval = blockingInterval != null ? blockingInterval.intValue() : null;
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
	 * Gets the homomorphism detector type.
	 *
	 * @return HomomorphismDetectorTypes
	 */
	public HomomorphismDetectorTypes getHomomorphismDetectorType() {
		if (this.homomorphismDetectorType == null) {
			return HomomorphismDetectorTypes.DATABASE;
		}
		return this.homomorphismDetectorType;
	}

	/**
	 * Sets the homomorphism detector type.
	 *
	 * @param type HomomorphismDetectorTypes
	 */
	public void setHomomorphismDetectorType(HomomorphismDetectorTypes type) {
		this.homomorphismDetectorType = type;
	}

	/**
	 * Sets the homomorphism detector type.
	 *
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
	 * Gets the post pruning type.
	 *
	 * @return PostPruningTypes
	 */
	public PostPruningTypes getPostPruningType() {
		return this.postPruningType;
	}

	/**
	 * Sets the post pruning type.
	 *
	 * @param postPruningType PostPruningTypes
	 */
	public void setPostPruningType(PostPruningTypes postPruningType) {
		this.postPruningType = postPruningType;
	}

	/**
	 * Sets the post pruning type.
	 *
	 * @param postPruningType String
	 */
	public void setPostPruningType(String postPruningType) {
		try {
			this.postPruningType = PostPruningTypes.valueOf(postPruningType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting post pruning type to " + null, e);
			this.postPruningType = null;
		}
	}

	/**
	 * Gets the chase interval.
	 *
	 * @return Integer
	 */
	public Integer getChaseInterval() {
		if(this.chaseInterval == null) {
			return 5;
		}
		return this.chaseInterval;
	}

	/**
	 * Sets the chase interval.
	 *
	 * @param chaseInterval Number
	 */
	public void setChaseInterval(Number chaseInterval) {
		this.chaseInterval = chaseInterval != null ? chaseInterval.intValue() : null;
	}

	/**
	 * Gets the max depth.
	 *
	 * @return Integer
	 */
	public Integer getMaxDepth() {
		if (this.maxDepth == null) {
			return Integer.MAX_VALUE;
		}
		return this.maxDepth;
	}

	/**
	 * Sets the max depth.
	 *
	 * @param maxDepth Number
	 */
	public void setMaxDepth(Number maxDepth) {
		this.maxDepth = maxDepth != null ? maxDepth.intValue() : null;
	}

	/**
	 * Gets the max bushiness.
	 *
	 * @return Integer
	 */
	public Integer getMaxBushiness() {
		if (this.maxBushiness == null) {
			return Integer.MAX_VALUE;
		}
		return this.maxBushiness;
	}

	/**
	 * Sets the max bushiness.
	 *
	 * @param maxBushiness Number
	 */
	public void setMaxBushiness(Number maxBushiness) {
		this.maxBushiness = maxBushiness != null ? maxBushiness.intValue() : null;
	}

	/**
	 * Gets the range.
	 *
	 * @return Integer
	 */
	public Integer getRange() {
		if (this.range == null) {
			return Integer.MAX_VALUE;
		}
		return this.range;
	}

	/**
	 * Sets the range.
	 *
	 * @param range Number
	 */
	public void setRange(Number range) {
		this.range = range != null ? range.intValue() : null;
	}

	/**
	 * Gets the exception on limit.
	 *
	 * @return Boolean
	 */
	public Boolean getExceptionOnLimit() {
		return this.exceptionOnLimit == null ? false : this.exceptionOnLimit;
	}

	/**
	 * Sets the exception on limit.
	 *
	 * @param exceptionOnLimit Boolean
	 */
	public void setExceptionOnLimit(Boolean exceptionOnLimit) {
		this.exceptionOnLimit = exceptionOnLimit;
	}

	/**
	 * Gets the validator type.
	 *
	 * @return ValidatorTypes
	 */
	public ValidatorTypes getValidatorType() {
		return this.validatorType;
	}

	/**
	 * Sets the validator type.
	 *
	 * @param validatorType ValidatorTypes
	 */
	public void setValidatorType(ValidatorTypes validatorType) {
		this.validatorType = validatorType;
	}

	/**
	 * Sets the validator type.
	 *
	 * @param validatorType String
	 */
	public void setValidatorType(String validatorType) {
		this.validatorType = ValidatorTypes.valueOf(validatorType);
	}

	/**
	 * Gets the filter type.
	 *
	 * @return FilterTypes
	 */
	public FilterTypes getFilterType() {
		return this.filterType;
	}

	/**
	 * Sets the filter type.
	 *
	 * @param filterType FilterTypes
	 */
	public void setFilterType(FilterTypes filterType) {
		this.filterType = filterType;
	}

	/**
	 * Sets the filter type.
	 *
	 * @param filterType String
	 */
	public void setFilterType(String filterType) {
		this.filterType = FilterTypes.valueOf(filterType);
	}

	/**
	 * Gets the dominance type.
	 *
	 * @return DominanceTypes
	 */
	public DominanceTypes getDominanceType() {
		return this.dominanceType;
	}

	/**
	 * Method setDominanceType.
	 * @param dominanceType DominanceTypes
	 */
	public void setDominanceType(DominanceTypes dominanceType) {
		this.dominanceType = dominanceType;
	}

	/**
	 * Sets the dominance type.
	 *
	 * @param dominanceType String
	 */
	public void setDominanceType(String dominanceType) {
		try {
			this.dominanceType = DominanceTypes.valueOf(dominanceType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting post pruning type to " + null, e);
			this.dominanceType = null;
		}
	}

	/**
	 * Gets the success dominance type.
	 *
	 * @return SuccessDominanceTypes
	 */
	public SuccessDominanceTypes getSuccessDominanceType() {
		return this.successDominanceType;
	}

	/**
	 * Sets the success dominance type.
	 *
	 * @param successDominanceType SuccessDominanceTypes
	 */
	public void setSuccessDominanceType(SuccessDominanceTypes successDominanceType) {
		this.successDominanceType = successDominanceType;
	}

	/**
	 * Sets the success dominance type.
	 *
	 * @param successDominanceType String
	 */
	public void setSuccessDominanceType(String successDominanceType) {
		try {
			this.successDominanceType = SuccessDominanceTypes.valueOf(successDominanceType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting post pruning type to " + null, e);
			this.successDominanceType = null;
		}
	}

	/**
	 * Gets the priority assessor type.
	 *
	 * @return PriorityAssessorTypes
	 */
	public PriorityAssessorTypes getPriorityAssessorType() {
		return this.priorityAssessorType;
	}

	/**
	 * Sets the priority assessor type.
	 *
	 * @param priorityAssessorType PriorityAssessorTypes
	 */
	public void setPriorityAssessorType(PriorityAssessorTypes priorityAssessorType) {
		this.priorityAssessorType = priorityAssessorType;
	}

	/**
	 * Sets the priority assessor type.
	 *
	 * @param priorityAssessorType String
	 */
	public void setPriorityAssessorType(String priorityAssessorType) {
		try {
			this.priorityAssessorType = PriorityAssessorTypes.valueOf(priorityAssessorType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting post pruning type to " + null, e);
			this.priorityAssessorType = null;
		}
	}

	/**
	 * Gets the follow up handling.
	 *
	 * @return FollowUpHandling
	 */
	public FollowUpHandling getFollowUpHandling() {
		return this.followUpHandling;
	}

	/**
	 * Sets the follow up handling.
	 *
	 * @param f FollowUpHandling
	 */
	public void setFollowUpHandling(FollowUpHandling f) {
		this.followUpHandling = f;
	}

	/**
	 * Sets the follow up handling.
	 *
	 * @param f String
	 */
	public void setFollowUpHandling(String f) {
		this.followUpHandling = FollowUpHandling.valueOf(String.valueOf(f).toUpperCase());
	}

	/**
	 * Gets the iterative executor type.
	 *
	 * @return IterativeExecutorTypes
	 */
	public IterativeExecutorTypes getIterativeExecutorType() {
		return this.iterativeExecutorType;
	}

	/**
	 * Sets the iterative executor type.
	 *
	 * @param iterativeExecutorType IterativeExecutorTypes
	 */
	public void setIterativeExecutorType(IterativeExecutorTypes iterativeExecutorType) {
		this.iterativeExecutorType = iterativeExecutorType;
	}

	/**
	 * Sets the iterative executor type.
	 *
	 * @param iterativeExecutorType String
	 */
	public void setIterativeExecutorType(String iterativeExecutorType) {
		try {
			this.iterativeExecutorType = IterativeExecutorTypes.valueOf(iterativeExecutorType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting post pruning type to " + null, e);
			this.iterativeExecutorType = null;
		}
	}

	/**
	 * Gets the first phase threads.
	 *
	 * @return Integer
	 */
	public Integer getFirstPhaseThreads() {
		return this.firstPhaseThreads;
	}

	/**
	 * Sets the first phase threads.
	 *
	 * @param i Number
	 */
	public void setFirstPhaseThreads(Number i) {
		this.firstPhaseThreads = i != null ? i.intValue() : null;
	}

	/**
	 * Sets the first phase threads.
	 *
	 * @param i Integer
	 */
	public void setFirstPhaseThreads(Integer i) {
		this.firstPhaseThreads = i != null ? i.intValue() : null;
	}

	/**
	 * Gets the second phase threads.
	 *
	 * @return Integer
	 */
	public Integer getSecondPhaseThreads() {
		return this.secondPhaseThreads;
	}

	/**
	 * Sets the second phase threads.
	 *
	 * @param i Number
	 */
	public void setSecondPhaseThreads(Number i) {
		this.secondPhaseThreads = i != null ? i.intValue() : null;
	}

	/**
	 * Sets the second phase threads.
	 *
	 * @param i Integer
	 */
	public void setSecondPhaseThreads(Integer i) {
		this.secondPhaseThreads = i != null ? i.intValue() : null;
	}

	/**
	 * Gets the depth threshold.
	 *
	 * @return Integer
	 */
	public Integer getDepthThreshold() {
		return this.depthThreshold;
	}

	/**
	 * Sets the depth threshold.
	 *
	 * @param depthThreshold Integer
	 */
	public void setDepthThreshold(Integer depthThreshold) {
		this.depthThreshold = depthThreshold;
	}

	/**
	 * Sets the depth threshold.
	 *
	 * @param depthThreshold Number
	 */
	public void setDepthThreshold(Number depthThreshold) {
		this.depthThreshold = depthThreshold != null ? depthThreshold.intValue() : null;
	}

	/**
	 * Gets the topk.
	 *
	 * @return Integer
	 */
	public Integer getTopk() {
		return this.topk;
	}

	/**
	 * Sets the topk.
	 *
	 * @param topk Integer
	 */
	public void setTopk(Integer topk) {
		this.topk = topk;
	}

	/**
	 * Sets the topk.
	 *
	 * @param topk Number
	 */
	public void setTopk(Number topk) {
		this.topk = topk != null ? topk.intValue() : null;
	}

	/**
	 * Gets the full initialization.
	 *
	 * @return Boolean
	 */
	public Boolean getFullInitialization() {
		return this.fullInitialization;
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
	 * Gets the top configurations.
	 *
	 * @return Integer
	 */
	public Integer getTopConfigurations() {
		return this.topConfigurations;
	}

	/**
	 * Sets the top configurations.
	 *
	 * @param topConfigurations Number
	 */
	public void setTopConfigurations(Number topConfigurations) {
		this.topConfigurations = topConfigurations != null ? topConfigurations.intValue() : null;
	}

	/**
	 * Sets the top configurations.
	 *
	 * @param topConfigurations Integer
	 */
	public void setTopConfigurations(Integer topConfigurations) {
		this.topConfigurations = topConfigurations;
	}

	/**
	 * Sets the full initialization.
	 *
	 * @param b Boolean
	 */
	public void setFullInitialization(Boolean b) {
		this.fullInitialization = b;
	}

	/**
	 * Gets the reachability filtering.
	 *
	 * @return Boolean
	 */
	public Boolean getReachabilityFiltering() {
		return this.reachabilityFiltering;
	}

	/**
	 * Sets the reachability filtering.
	 *
	 * @param reachabilityFiltering Boolean
	 */
	public void setReachabilityFiltering(Boolean reachabilityFiltering) {
		this.reachabilityFiltering = reachabilityFiltering;
	}
	
	/**
	 * Gets the order aware.
	 *
	 * @return Boolean
	 */
	public Boolean getOrderAware() {
		return this.orderAware;
	}

	/**
	 * Sets the order aware.
	 *
	 * @param b the new order aware
	 */
	public void setOrderAware(Boolean b) {
		this.orderAware = b;
	}
	
	/**
	 * Gets the zombification.
	 *
	 * @return the zombification
	 */
	public Boolean getZombification() {
		return this.zombification == null ? false : this.zombification;
	}

	/**
	 * Sets the zombification.
	 *
	 * @param zombification the new zombification
	 */
	public void setZombification(Boolean zombification) {
		this.zombification = zombification;
	}
	
	/**
	 * Gets the access file.
	 *
	 * @return the access file
	 */
	public String getAccessFile() {
		return this.accessFile;
	}

	/**
	 * Sets the access file.
	 *
	 * @param accessFile the new access file
	 */
	public void setAccessFile(String accessFile) {
		this.accessFile = accessFile;
	}

	/** Planning algorithm types. */
	public static enum PlannerTypes {
		
		/** The linear generic. */
		@EnumParameterValue(description = "Generic (exhaustive) linear planning algorithm")
		LINEAR_GENERIC, 

		/** The linear optimized. */
		@EnumParameterValue(description = "Optimized linear planning algorithm. ")
		LINEAR_OPTIMIZED, 

		/** The linear kchase. */
		@EnumParameterValue(description = "Linear planning algorithm relying on KTERMINATION_CHASE reasoning type. ")
		LINEAR_KCHASE, 

		/** The dag generic. */
		@EnumParameterValue(description = "Generic (exhaustive) DAG planning algorithm")
		DAG_GENERIC, 

		/** The dag simpledp. */
		@EnumParameterValue(description = "DAG planning algorithm, simulating classic DP plan optimization")
		DAG_SIMPLEDP,

		/** The dag chasefriendlydp. */
		@EnumParameterValue(description = "DAG DP planning algorithm, avoiding redundant chasing")
		DAG_CHASEFRIENDLYDP, 

		/** The dag optimized. */
		@EnumParameterValue(description = "DAG DP planning algorithm, relying on parallelism")
		DAG_OPTIMIZED, 

		/** The dag ilp. */
		@EnumParameterValue(description = "DAG planning algorithm using linear planning iteratively (Iterative Linear Planning)")
		DAG_ILP, 

		/** The dag ilp si. */
		@EnumParameterValue(description = "DAG planning algorithm, a variant of ILP")
		DAG_ILP_SI, 

		/** The dag mixed ilp. */
		@EnumParameterValue(description = "DAG planning algorithm, a variant of ILP")
		DAG_MIXED_ILP
	}

	/**
	 * The Enum ReasoningTypes.
	 */
	public static enum ReasoningTypes {
		
		/** The blocking chase. */
		@EnumParameterValue(description = "Chase algorithm with blocking. To be use in cases where the chase does not terminate.")
		BLOCKING_CHASE, 
		
		/** The restricted chase. */
		@EnumParameterValue(description = "Restricted chase as defined in the literature")
		RESTRICTED_CHASE, 
		
		/** The ktermination chase. */
		@EnumParameterValue(description = "Restricted chase, where the number of rule firing rounds is bounded by a constant K")
		KTERMINATION_CHASE, 
		

	}

	/**
	 * The Enum HomomorphismDetectorTypes.
	 */
	public static enum HomomorphismDetectorTypes {
		
		/** The database. */
		@EnumParameterValue(description = "Homomorphism checker relying on an internal relational database")
		DATABASE;
	}

	/**
	 * The Enum PostPruningTypes.
	 */
	public static enum PostPruningTypes {
		
		/** The remove accesses. */
		@EnumParameterValue(description = "Removes redudant accesses")
		REMOVE_ACCESSES, 
	}

	/**
	 * The Enum ValidatorTypes.
	 */
	public static enum ValidatorTypes {
		
		/** The default validator. */
		@EnumParameterValue(description = "No shape or type restriction")
		DEFAULT_VALIDATOR,
		
		/** The applyrule validator. */
		@EnumParameterValue(description = "Requires at least one of the input configurations to be an ApplyRule")
		APPLYRULE_VALIDATOR,
		
		/** The depth validator. */
		@EnumParameterValue(description = "Restricts the depth of the plans visited ")
		DEPTH_VALIDATOR,
		
		/** The right depth validator. */
		@EnumParameterValue(description = "Restricts the depth of the RHS plans used")
		RIGHT_DEPTH_VALIDATOR,
		
		/** The applyrule depth validator. */
		@EnumParameterValue(description = "Combination of APPLYRULE_VALIDATOR and DEPTH_VALIDATOR")
		APPLYRULE_DEPTH_VALIDATOR,
		
		/** The linear validator. */
		@EnumParameterValue(description = "Restricts the shape of plans to left-deep ones")
		LINEAR_VALIDATOR,
		
		/** The strict inclusion. */
		@EnumParameterValue(description = "Miscellaneous")
		STRICT_INCLUSION
	}

	/**
	 * The Enum FilterTypes.
	 */
	public static enum FilterTypes {
		
		/** The fact dominated filter. */
		@EnumParameterValue(description = "Removes the fact dominated configurations after each exploration step")
		FACT_DOMINATED_FILTER,
		
		/** The numerically dominated filter. */
		@EnumParameterValue(description = "Removes the numerically fact dominated configurations after each exploration step")
		NUMERICALLY_DOMINATED_FILTER,
	}

	/**
	 * The Enum DominanceTypes.
	 */
	public static enum DominanceTypes {
		
		/** The closed. */
		@EnumParameterValue(description = 
				"Closed dominance. Given two closed configurations, one "
				+ "dominate the other if its facts are contained in the facts "
				+ "of the other, up to homomorphism")
		CLOSED,
		
		/** The open. */
		@EnumParameterValue(description = 
				"Open dominance. Given two possible open configurations, one "
				+ "dominate the other if ")
		OPEN
	}

	/**
	 * The Enum SuccessDominanceTypes.
	 */
	public static enum SuccessDominanceTypes {
		
		/** The closed. */
		@EnumParameterValue(description = "Closed dominance on successful configurations.")
		CLOSED,

		/** The open. */
		@EnumParameterValue(description = "Open dominance on successful configurations.")
		OPEN
	}

	/**
	 * The Enum FollowUpHandling.
	 */
	public static enum FollowUpHandling {
		
		/** The minimal. */
		@EnumParameterValue(description = "Minimal follow-up join.\n"
				+ "Upon initializing of a DAG plan search every follow-up join "
				+ "gives rise to an independant ApplyRule")
		MINIMAL, 
		
		/** The maximal. */
		@EnumParameterValue(description = "Maximal follow-up join.\n"
				+ "Upon initializing of a DAG plan search all follow-up joins "
				+ "gives rise to a single/common ApplyRule")
		MAXIMAL
	}

	/**
	 * The Enum IterativeExecutorTypes.
	 */
	public static enum IterativeExecutorTypes {
		
		/** The multithreaded. */
		@EnumParameterValue(description = "Multi-threaded executor for running"
				+ "DAG planning rounds in parallel")
		MULTITHREADED
	}

	/**
	 * The Enum PriorityAssessorTypes.
	 */
	public static enum PriorityAssessorTypes {
		
		/** The default. */
		@EnumParameterValue(description = "Default (no-op) priority assessor")
		DEFAULT,

		/** The DISTANC e2 coretopk. */
		@EnumParameterValue(description =
				"Assessor given priority to the K configurations that are "
				+ "closest to the core")
		DISTANCE2CORETOPK,

		/** The DISTANC e2 corerange. */
		@EnumParameterValue(description =
				"Assessor given priority to some range of configurations that "
				+ "are closest to the core")
		DISTANCE2CORERANGE
	}
}
