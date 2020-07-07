// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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

	/** */
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
		super(config, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}

	@Parameter(description="The maximum number of iterations to perform in "
			+ "planning.\nThis may have different semantic depending of which "
			+ "planning algorithm is used.",
			defaultValue = "Integer.MAX_VALUE")
	protected Integer maxIterations = Integer.MAX_VALUE;

	/** The planner type. */
	@Parameter(description="Type of planning algorithm to use.")
	protected PlannerTypes plannerType;

	/** The query match interval. */
	@Parameter(description="Number of exploration interval to wait for between "
			+ "query match checks.\n Use in linear planning algorithms only.")
	protected Integer queryMatchInterval;

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

	@Parameter(description = 
			"If true, a LimitReachedException is thrown during planning if a "
			+ "limit (e.g. time or max no. interactions) is reached.\n"
			+ "Otherwise, the event is logged and the planning completes "
			+ "gracefully")
	protected Boolean exceptionOnLimit;

	/** Checks if we want one plan or the best plan */
	@Parameter(description = "if we want the best plan or just a plan",
			defaultValue = "true")
	protected Boolean findBestPlan = true;

	/** The validator type. */
	@Parameter(description = "Type of validator to use. Only required in "
			+ "conjunction with DAG planning algorithms",
			defaultValue = "DEFAULT")
	protected ValidatorTypes validatorType = ValidatorTypes.DEFAULT_VALIDATOR;

	@Parameter(description = "Type of filter to use. Only required in "
			+ "conjunction with DAG planning algorithms")
	protected FilterTypes filterType;

	/** The dominance type. Dominance is used to avoid searching in different parts of the plan space */
	@Parameter(description = "Type of dominance checks to use. Only required "
			+ "in conjunction with DAG planning algorithms",
			defaultValue = "STRICT_OPEN")
	protected DominanceTypes dominanceType = DominanceTypes.OPEN;

	/** The success dominance type. Success domiannce refers to avoiding searching parts of the plan space since it is considered worse than a part we have already seen */
	@Parameter(description =
			"Type of sucess dominance checks to use. Only required in "
			+ "conjunction with DAG planning algorithms",
			defaultValue = "OPEN")
	protected SuccessDominanceTypes successDominanceType = SuccessDominanceTypes.OPEN;

	/** The follow up handling. Refers to how we build plans when we fire one of the access rules */
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
	protected Integer dagThreads = 10;

	/** The depth threshold. */
	@Parameter(description = "Threshold for the DEPTH_THROTTLING validator",
			defaultValue = "2")
	protected Integer depthThreshold = 2;
	
	/** Internal or external database should be used . */
	@Parameter(description = "If true, we will use an Internal database manager instead of the external one.",
			defaultValue = "true")
	private Boolean useInternalDatabase = true;
	
	/** DAG thread timeout value. should be smaller then the main reasoning timeout. */
	@Parameter(description = "DAG thread timeout value. should be smaller then the main reasoning timeout",
			defaultValue = "120000")
	private long dagThreadTimeout = 1000*60*2; // 2 minutes

	/**
	 * 
	 *
	 * @return Integer
	 */
	public Integer getMaxIterations() {
		return this.maxIterations;
	}

	/**
	 * 
	 *
	 * @param maxIterations Number
	 */
	public void setMaxIterations(Number maxIterations) {
		this.maxIterations = maxIterations != null ? maxIterations.intValue() : null;
	}

	/**
	 * 
	 *
	 * @param maxIterations String
	 */
	public void setMaxIterations(String maxIterations) {
		log.debug("Setting max iteration to infinity");
		this.maxIterations = Integer.MAX_VALUE;
	}

	/**
	 * 
	 *
	 * @param s String
	 */
	public void setTimeout(String s) {
		log.debug("Setting timeout to infinity");
		this.timeout = Double.POSITIVE_INFINITY;
	}

	/**
	 * 
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
	 * 
	 *
	 * @param plannerType PlannerTypes
	 */
	public void setPlannerType(PlannerTypes plannerType) {
		this.plannerType = plannerType;
	}

	/**
	 * 
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
	 * 
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
	 * 
	 *
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
	 * 
	 *
	 * @param postPruningType PostPruningTypes
	 */
	public void setPostPruningType(PostPruningTypes postPruningType) {
		this.postPruningType = postPruningType;
	}

	/**
	 * 
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
	 * 
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
	 * 
	 *
	 * @param chaseInterval Number
	 */
	public void setChaseInterval(Number chaseInterval) {
		this.chaseInterval = chaseInterval != null ? chaseInterval.intValue() : null;
	}

	/**
	 * 
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
	 * 
	 *
	 * @param maxDepth Number
	 */
	public void setMaxDepth(Number maxDepth) {
		this.maxDepth = maxDepth != null ? maxDepth.intValue() : null;
	}

	/**
	 * @return Boolean
	 */
	public Boolean getExceptionOnLimit() {
		return this.exceptionOnLimit == null ? false : this.exceptionOnLimit;
	}

	/**
	 * 
	 *
	 * @param exceptionOnLimit Boolean
	 */
	public void setExceptionOnLimit(Boolean exceptionOnLimit) {
		this.exceptionOnLimit = exceptionOnLimit;
	}

	/**
	 * @return Boolean
	 */
	public Boolean getFindBestPlan() {
		return this.findBestPlan == null ? true : this.findBestPlan;
	}

    /**
	 * 
	 *
	 * @param bestPlan Boolean
	 */
	public void setFindBestPlan(Boolean bestPlan) {
		this.findBestPlan = bestPlan;
	}


	/**
	 * 
	 *
	 * @return ValidatorTypes
	 */
	public ValidatorTypes getValidatorType() {
		return this.validatorType;
	}

	/**
	 * 
	 *
	 * @param validatorType ValidatorTypes
	 */
	public void setValidatorType(ValidatorTypes validatorType) {
		this.validatorType = validatorType;
	}

	/**
	 * 
	 *
	 * @param validatorType String
	 */
	public void setValidatorType(String validatorType) {
		this.validatorType = ValidatorTypes.valueOf(validatorType);
	}

	/**
	 * 
	 *
	 * @return FilterTypes
	 */
	public FilterTypes getFilterType() {
		return this.filterType;
	}

	/**
	 * 
	 *
	 * @param filterType FilterTypes
	 */
	public void setFilterType(FilterTypes filterType) {
		this.filterType = filterType;
	}

	/**
	 * 
	 *
	 * @param filterType String
	 */
	public void setFilterType(String filterType) {
		this.filterType = FilterTypes.valueOf(filterType);
	}

	/**
	 * 
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
	 *
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
	 * 
	 *
	 * @return SuccessDominanceTypes
	 */
	public SuccessDominanceTypes getSuccessDominanceType() {
		return this.successDominanceType;
	}

	/**
	 * 
	 *
	 * @param successDominanceType SuccessDominanceTypes
	 */
	public void setSuccessDominanceType(SuccessDominanceTypes successDominanceType) {
		this.successDominanceType = successDominanceType;
	}

	/**
	 * 
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
	 * @return FollowUpHandling
	 */
	public FollowUpHandling getFollowUpHandling() {
		return this.followUpHandling;
	}

	/**
	 * 
	 *
	 * @param f FollowUpHandling
	 */
	public void setFollowUpHandling(FollowUpHandling f) {
		this.followUpHandling = f;
	}

	/**
	 * 
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
	public Integer getDagThreads() {
		return this.dagThreads;
	}

	/**
	 * Sets the first phase threads.
	 *
	 * @param i Number
	 */
	public void setDagThreads(Number i) {
		this.dagThreads = i != null ? i.intValue() : null;
	}

	/**
	 * Sets the first phase threads.
	 *
	 * @param i Integer
	 */
	public void setFirstPhaseThreads(Integer i) {
		this.dagThreads = i != null ? i.intValue() : null;
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
	
	public Boolean getUseInternalDatabase() {
		return useInternalDatabase;
	}

	public void setUseInternalDatabase(Boolean useInternalDatabase) {
		this.useInternalDatabase = useInternalDatabase;
	}

	public long getDagThreadTimeout() {
		return dagThreadTimeout;
	}

	public void setDagThreadTimeout(long dagThreadTimeout) {
		this.dagThreadTimeout = dagThreadTimeout;
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

		/** The dag optimized. */
		@EnumParameterValue(description = "DAG DP planning algorithm, relying on parallelism")
		DAG_OPTIMIZED,
		
	}

	/** */
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
		@EnumParameterValue(description = "requires the left and right configurations to be non-trivial:\n"
				+ "an ordered pair of configurations (left, right) is non-trivial if the output facts of the right configuration are not included in the output facts of left configuration and vice versa.")
		DEFAULT_VALIDATOR,
		
		/** The applyrule validator. */
		@EnumParameterValue(description = "Requires the input pair of configurations to be non trivial and at least one of the input configurations to be an ApplyRule.")
		APPLYRULE_VALIDATOR,
		
		/** The depth validator. */
		@EnumParameterValue(description = "Requires the input pair of configurations to be non trivial and their combined depth to be <= the depth threshold.")
		DEPTH_VALIDATOR,
		
		/** The right depth validator. */
		@EnumParameterValue(description = "Requires the input pair of configurations to be non trivial and the right's depth to be <= the depth threshold")
		RIGHT_DEPTH_VALIDATOR,
		
		/** The linear validator. */
		@EnumParameterValue(description = "Requires the input pair of configurations to be non trivial and their composition to be a closed left-deep configuration")
		LINEAR_VALIDATOR,
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
		
	
		@EnumParameterValue(description = "Closed dominance on successful configurations.")
		CLOSED,

		
		@EnumParameterValue(description = "Open dominance on successful configurations.")
		OPEN
	}

	/**
	 * 
	 */
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

	/**
	 * The Enum IterativeExecutorTypes.
	 */
	public static enum IterativeExecutorTypes {
		
		/** The multithreaded. */
		@EnumParameterValue(description = "Multi-threaded executor for running"
				+ "DAG planning rounds in parallel")
		MULTITHREADED
	}
}
