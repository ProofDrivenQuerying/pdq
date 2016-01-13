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

	/** */
	private static final long serialVersionUID = -8077300774514524509L;

	private static Logger log = Logger.getLogger(PlannerParameters.class);

	/** Properties file name */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-planner.properties";

	/** Properties file path */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Constructor for PlannerParameters using default configuration file path.
	 */
	public PlannerParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH), false, false);
	}

	/**
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
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param strict if true, param loading problem will throw an exception
	 */
	public PlannerParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, true, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}

	@Parameter(description="The maximum number of iterations to perform in "
			+ "planning.\nThis may have different semantic depending of which "
			+ "planning algorithm is used.",
			defaultValue = "Integer.MAX_VALUE")
	protected Integer maxIterations = Integer.MAX_VALUE;

	@Parameter(description="Interval (in number of iterations) between "
			+ "which detailed executions informations are logged.",
			defaultValue = "10")
	protected Integer logIntervals = 10;

	@Parameter(description="Interval (in number of iterations) between "
			+ "which succint executions informations are logged.",
			defaultValue = "1")
	protected Integer shortLogIntervals = 1;

	@Parameter(description="Path of the output file where to store the logs "
			+ "(optional). If missing, logs are printed to STDOUT")
	protected String outputLogPath;

	@Parameter(description="Type of planning algorithm to use.")
	protected PlannerTypes plannerType;

	@Parameter(description="Number of exploration interval to wait for between "
			+ "query match checks.\n Use in linear planning algorithms only.")
	protected Integer queryMatchInterval;

	@Parameter(description = "Type of post-pruning. This is only used in "
			+ "optimizer linear planning",
			defaultValue = "NONE")
	protected PostPruningTypes postPruningType;

	@Parameter(description = "Number of intervals between which to run the chase")
	protected Integer chaseInterval;

	@Parameter(description = 
			"Maximum depth of the exploration.\nThis may have different "
			+ "semantic depending of which planning algorithm is used.")
	protected Integer maxDepth;

	@Parameter(description = "Maximum level of bushiness allowed.\n This is "
			+ "only used with ILP-types of planning algorithms",
			defaultValue = "Integer.MAX_VALUE")
	protected Integer maxBushiness = Integer.MAX_VALUE;

	@Parameter(description = 
			"If true, a LimitReachedException is thrown during planning if a "
			+ "limit (e.g. time or max no. interactions) is reached.\n"
			+ "Otherwise, the event is logged and the planning completes "
			+ "gracefully")
	protected Boolean exceptionOnLimit;

	@Parameter(description = "Type of validator to use. Only required in "
			+ "conjunction with DAG planning algorithms",
			defaultValue = "DEFAULT")
	protected ValidatorTypes validatorType = ValidatorTypes.DEFAULT_VALIDATOR;

	@Parameter(description = "Type of filter to use. Only required in "
			+ "conjunction with DAG planning algorithms")
	protected FilterTypes filterType;

	@Parameter(description = "Type of dominance checks to use. Only required "
			+ "in conjunction with DAG planning algorithms",
			defaultValue = "STRICT_OPEN")
	protected DominanceTypes dominanceType = DominanceTypes.OPEN;

	@Parameter(description =
			"Type of sucess dominance checks to use. Only required in "
			+ "conjunction with DAG planning algorithms",
			defaultValue = "OPEN")
	protected SuccessDominanceTypes successDominanceType = SuccessDominanceTypes.OPEN;

	@Parameter(description = "Specifies how follow-up joins should be handled."
			+ "\nOnly applies to DAG planning algorithms",
			defaultValue = "MINIMAL")
	protected FollowUpHandling followUpHandling = FollowUpHandling.MINIMAL;

	@Parameter(description = "Type of iterative executor to use. Only applies "
			+ "to DAG planning algorithms.",
			defaultValue = "MULTITHREADED")
	protected IterativeExecutorTypes iterativeExecutorType = IterativeExecutorTypes.MULTITHREADED;

	@Parameter(description = "Number of threads to use in the first phase of "
			+ "a parallel DAG planning algorithm",
			defaultValue = "50")
	protected Integer firstPhaseThreads = 50;

	@Parameter(description = "Number of threads to use in the second phase of "
			+ "a parallel DAG planning algorithm",
			defaultValue = "50")
	protected Integer secondPhaseThreads = 50;

	@Parameter(description = "Threshold for the DEPTH_THROTTLING validator",
			defaultValue = "2")
	protected Integer depthThreshold = 2;

	@Parameter(description = "If true, all join orders are considered during "
			+ "plan search",
			defaultValue = "true")
	protected Boolean orderAware = true;
	
	@Parameter(description = "If true, then we perform (de)zombification during optimised linear plan exploration "
			+ "plan search",
			defaultValue = "true")
	protected Boolean zombification;
	
	@Parameter(description = "Contains the desired list of accesses")
	protected String accessFile;

	/**
	 * @return Integer
	 */
	public Integer getLogIntervals() {
		return this.logIntervals;
	}

	/**
	 * @return Integer
	 */
	public Integer getMaxIterations() {
		return this.maxIterations;
	}

	/**
	 * @return Integer
	 */
	public Integer getShortLogIntervals() {
		return this.shortLogIntervals;
	}

	/**
	 * @param logIntervals Number
	 */
	public void setLogIntervals(Number logIntervals) {
		this.logIntervals = logIntervals != null ? logIntervals.intValue() : null;
	}

	/**
	 * @param maxIterations Number
	 */
	public void setMaxIterations(Number maxIterations) {
		this.maxIterations = maxIterations != null ? maxIterations.intValue() : null;
	}

	/**
	 * @param maxIterations String
	 */
	public void setMaxIterations(String maxIterations) {
		log.debug("Setting max iteration to infinity");
		this.maxIterations = Integer.MAX_VALUE;
	}

	/**
	 * @param shortLogIntervals Number
	 */
	public void setShortLogIntervals(Number shortLogIntervals) {
		this.shortLogIntervals = shortLogIntervals != null ? shortLogIntervals.intValue() : null;
	}

	/**
	 * @param s String
	 */
	public void setTimeout(String s) {
		log.debug("Setting timeout to infinity");
		this.timeout = Double.POSITIVE_INFINITY;
	}

	/**
	 * @return String
	 */
	public String getOutputLogPath() {
		return this.outputLogPath;
	}

	/**
	 * @param outputLogPath String
	 */
	public void setOutputLogPath(String outputLogPath) {
		this.outputLogPath = outputLogPath;
	}

	/**
	 * @return PlannerTypes
	 */
	public PlannerTypes getPlannerType() {
		if (this.plannerType == null) {
			return PlannerTypes.DAG_OPTIMIZED;
		}
		return this.plannerType;
	}

	/**
	 * @param plannerType PlannerTypes
	 */
	public void setPlannerType(PlannerTypes plannerType) {
		this.plannerType = plannerType;
	}

	/**
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
	 * @return Integer
	 */
	public Integer getQueryMatchInterval() {
		if (this.queryMatchInterval == null) {
			return 1;
		}
		return this.queryMatchInterval;
	}

	/**
	 * @param queryMatchInterval Number
	 */
	public void setQueryMatchInterval(Number queryMatchInterval) {
		this.queryMatchInterval = queryMatchInterval != null ? queryMatchInterval.intValue() : null;
	}

	/**
	 * @return PostPruningTypes
	 */
	public PostPruningTypes getPostPruningType() {
		return this.postPruningType;
	}

	/**
	 * @param postPruningType PostPruningTypes
	 */
	public void setPostPruningType(PostPruningTypes postPruningType) {
		this.postPruningType = postPruningType;
	}

	/**
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
	 * @return Integer
	 */
	public Integer getChaseInterval() {
		if(this.chaseInterval == null) {
			return 5;
		}
		return this.chaseInterval;
	}

	/**
	 * @param chaseInterval Number
	 */
	public void setChaseInterval(Number chaseInterval) {
		this.chaseInterval = chaseInterval != null ? chaseInterval.intValue() : null;
	}

	/**
	 * @return Integer
	 */
	public Integer getMaxDepth() {
		if (this.maxDepth == null) {
			return Integer.MAX_VALUE;
		}
		return this.maxDepth;
	}

	/**
	 * @param maxDepth Number
	 */
	public void setMaxDepth(Number maxDepth) {
		this.maxDepth = maxDepth != null ? maxDepth.intValue() : null;
	}

	/**
	 * @return Integer
	 */
	public Integer getMaxBushiness() {
		if (this.maxBushiness == null) {
			return Integer.MAX_VALUE;
		}
		return this.maxBushiness;
	}

	/**
	 * @param maxBushiness Number
	 */
	public void setMaxBushiness(Number maxBushiness) {
		this.maxBushiness = maxBushiness != null ? maxBushiness.intValue() : null;
	}

	/**
	 * @return Boolean
	 */
	public Boolean getExceptionOnLimit() {
		return this.exceptionOnLimit == null ? false : this.exceptionOnLimit;
	}

	/**
	 * @param exceptionOnLimit Boolean
	 */
	public void setExceptionOnLimit(Boolean exceptionOnLimit) {
		this.exceptionOnLimit = exceptionOnLimit;
	}

	/**
	 * @return ValidatorTypes
	 */
	public ValidatorTypes getValidatorType() {
		return this.validatorType;
	}

	/**
	 * @param validatorType ValidatorTypes
	 */
	public void setValidatorType(ValidatorTypes validatorType) {
		this.validatorType = validatorType;
	}

	/**
	 * @param validatorType String
	 */
	public void setValidatorType(String validatorType) {
		this.validatorType = ValidatorTypes.valueOf(validatorType);
	}

	/**
	 * @return FilterTypes
	 */
	public FilterTypes getFilterType() {
		return this.filterType;
	}

	/**
	 * @param filterType FilterTypes
	 */
	public void setFilterType(FilterTypes filterType) {
		this.filterType = filterType;
	}

	/**
	 * @param filterType String
	 */
	public void setFilterType(String filterType) {
		this.filterType = FilterTypes.valueOf(filterType);
	}

	/**
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
	 * @return SuccessDominanceTypes
	 */
	public SuccessDominanceTypes getSuccessDominanceType() {
		return this.successDominanceType;
	}

	/**
	 * @param successDominanceType SuccessDominanceTypes
	 */
	public void setSuccessDominanceType(SuccessDominanceTypes successDominanceType) {
		this.successDominanceType = successDominanceType;
	}

	/**
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
	 * @return FollowUpHandling
	 */
	public FollowUpHandling getFollowUpHandling() {
		return this.followUpHandling;
	}

	/**
	 * @param f FollowUpHandling
	 */
	public void setFollowUpHandling(FollowUpHandling f) {
		this.followUpHandling = f;
	}

	/**
	 * @param f String
	 */
	public void setFollowUpHandling(String f) {
		this.followUpHandling = FollowUpHandling.valueOf(String.valueOf(f).toUpperCase());
	}

	/**
	 * @return IterativeExecutorTypes
	 */
	public IterativeExecutorTypes getIterativeExecutorType() {
		return this.iterativeExecutorType;
	}

	/**
	 * @param iterativeExecutorType IterativeExecutorTypes
	 */
	public void setIterativeExecutorType(IterativeExecutorTypes iterativeExecutorType) {
		this.iterativeExecutorType = iterativeExecutorType;
	}

	/**
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
	 * @return Integer
	 */
	public Integer getFirstPhaseThreads() {
		return this.firstPhaseThreads;
	}

	/**
	 * @param i Number
	 */
	public void setFirstPhaseThreads(Number i) {
		this.firstPhaseThreads = i != null ? i.intValue() : null;
	}

	/**
	 * @param i Integer
	 */
	public void setFirstPhaseThreads(Integer i) {
		this.firstPhaseThreads = i != null ? i.intValue() : null;
	}

	/**
	 * @return Integer
	 */
	public Integer getSecondPhaseThreads() {
		return this.secondPhaseThreads;
	}

	/**
	 * @param i Number
	 */
	public void setSecondPhaseThreads(Number i) {
		this.secondPhaseThreads = i != null ? i.intValue() : null;
	}

	/**
	 * @param i Integer
	 */
	public void setSecondPhaseThreads(Integer i) {
		this.secondPhaseThreads = i != null ? i.intValue() : null;
	}

	/**
	 * @return Integer
	 */
	public Integer getDepthThreshold() {
		return this.depthThreshold;
	}

	/**
	 * @param depthThreshold Integer
	 */
	public void setDepthThreshold(Integer depthThreshold) {
		this.depthThreshold = depthThreshold;
	}

	/**
	 * @param depthThreshold Number
	 */
	public void setDepthThreshold(Number depthThreshold) {
		this.depthThreshold = depthThreshold != null ? depthThreshold.intValue() : null;
	}
	
	/**
	 * @return Boolean
	 */
	public Boolean getOrderAware() {
		return this.orderAware;
	}

	/**
	 * @param orderAware Boolean
	 */
	public void setOrderAware(Boolean b) {
		this.orderAware = b;
	}
	
	public Boolean getZombification() {
		return this.zombification == null ? false : this.zombification;
	}

	public void setZombification(Boolean zombification) {
		this.zombification = zombification;
	}
	
	public String getAccessFile() {
		return this.accessFile;
	}

	public void setAccessFile(String accessFile) {
		this.accessFile = accessFile;
	}

	/** Planning algorithm types. */
	public static enum PlannerTypes {
		@EnumParameterValue(description = "Generic (exhaustive) linear planning algorithm")
		LINEAR_GENERIC, 

		@EnumParameterValue(description = "Optimized linear planning algorithm. ")
		LINEAR_OPTIMIZED, 

		@EnumParameterValue(description = "Linear planning algorithm relying on KTERMINATION_CHASE reasoning type. ")
		LINEAR_KCHASE, 

		@EnumParameterValue(description = "Generic (exhaustive) DAG planning algorithm")
		DAG_GENERIC, 

		@EnumParameterValue(description = "DAG planning algorithm, simulating classic DP plan optimization")
		DAG_SIMPLEDP,

		@EnumParameterValue(description = "DAG DP planning algorithm, avoiding redundant chasing")
		DAG_CHASEFRIENDLYDP, 

		@EnumParameterValue(description = "DAG DP planning algorithm, relying on parallelism")
		DAG_OPTIMIZED, 
	}

	/** */
	public static enum PostPruningTypes {
		@EnumParameterValue(description = "Removes redudant accesses")
		REMOVE_ACCESSES, 
	}

	/** */
	public static enum ValidatorTypes {
		
		@EnumParameterValue(description = "No shape or type restriction")
		DEFAULT_VALIDATOR,
		
		@EnumParameterValue(description = "Requires at least one of the input configurations to be an ApplyRule")
		APPLYRULE_VALIDATOR,
		
		@EnumParameterValue(description = "Restricts the depth of the plans visited ")
		DEPTH_VALIDATOR,
		
		@EnumParameterValue(description = "Restricts the depth of the RHS plans used")
		RIGHT_DEPTH_VALIDATOR,
		
		@EnumParameterValue(description = "Combination of APPLYRULE_VALIDATOR and DEPTH_VALIDATOR")
		APPLYRULE_DEPTH_VALIDATOR,
		
		@EnumParameterValue(description = "Restricts the shape of plans to left-deep ones")
		LINEAR_VALIDATOR,
	}

	/** */
	public static enum FilterTypes {
		
		@EnumParameterValue(description = "Removes the fact dominated configurations after each exploration step")
		FACT_DOMINATED_FILTER,
		
		@EnumParameterValue(description = "Removes the numerically fact dominated configurations after each exploration step")
		NUMERICALLY_DOMINATED_FILTER,
	}

	/** */
	public static enum DominanceTypes {
		
		@EnumParameterValue(description = 
				"Closed dominance. Given two closed configurations, one "
				+ "dominate the other if its facts are contained in the facts "
				+ "of the other, up to homomorphism")
		CLOSED,
		
		@EnumParameterValue(description = 
				"Open dominance. Given two possible open configurations, one "
				+ "dominate the other if ")
		OPEN
	}

	/** */
	public static enum SuccessDominanceTypes {
		@EnumParameterValue(description = "Closed dominance on successful configurations.")
		CLOSED,

		@EnumParameterValue(description = "Open dominance on successful configurations.")
		OPEN
	}

	/** */
	public static enum FollowUpHandling {
		@EnumParameterValue(description = "Minimal follow-up join.\n"
				+ "Upon initializing of a DAG plan search every follow-up join "
				+ "gives rise to an independant ApplyRule")
		MINIMAL, 
		
		@EnumParameterValue(description = "Maximal follow-up join.\n"
				+ "Upon initializing of a DAG plan search all follow-up joins "
				+ "gives rise to a single/common ApplyRule")
		MAXIMAL
	}

	/** */
	public static enum IterativeExecutorTypes {
		@EnumParameterValue(description = "Multi-threaded executor for running"
				+ "DAG planning rounds in parallel")
		MULTITHREADED
	}

//	/** */
//	public static enum PriorityAssessorTypes {
//		@EnumParameterValue(description = "Default (no-op) priority assessor")
//		DEFAULT,
//
//		@EnumParameterValue(description =
//				"Assessor given priority to the K configurations that are "
//				+ "closest to the core")
//		DISTANCE2CORETOPK,
//
//		@EnumParameterValue(description =
//				"Assessor given priority to some range of configurations that "
//				+ "are closest to the core")
//		DISTANCE2CORERANGE
//	}
}
