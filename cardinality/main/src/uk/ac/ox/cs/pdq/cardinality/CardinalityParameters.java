/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.Parameters;

// TODO: Auto-generated Javadoc
/**
 * Holds the parameters of a planning session.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class CardinalityParameters extends Parameters {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8077300774514524509L;

	/** The log. */
	private static Logger log = Logger.getLogger(CardinalityParameters.class);

	/**  Properties file name. */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-cardinality.properties";

	/**  Properties file path. */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Constructor for PlannerParameters using default configuration file path.
	 */
	public CardinalityParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH), false, false);
	}

	/**
	 * Gets the version.
	 *
	 * @return the version of the planner code, as given by Maven
	 */
	public static String getVersion() {
		String path = "/planner.version";
		try (InputStream stream = CardinalityParameters.class.getResourceAsStream(path)) {
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
	public CardinalityParameters(File config) {
		this(config, false, false);
	}

	/**
	 * Constructor for PlannerParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 */
	public CardinalityParameters(File config, boolean verbose) {
		this(config, false, verbose, false);
	}

	/**
	 * Constructor for PlannerParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public CardinalityParameters(File config, boolean verbose, boolean strict) {
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
	public CardinalityParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, true, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}

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

	/** The max depth. */
	@Parameter(description = 
			"Maximum depth of the exploration.\nThis may have different "
			+ "semantic depending of which planning algorithm is used.")
	protected Integer maxDepth;

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

	/** The filter type. */
	@Parameter(description = "Type of filter to use. Only required in "
			+ "conjunction with DAG planning algorithms")
	protected FilterTypes filterType;

	/** The dominance type. */
	@Parameter(description = "Type of dominance checks to use. Only required "
			+ "in conjunction with DAG planning algorithms",
			defaultValue = "STRICT_OPEN")
	protected DominanceTypes dominanceType = DominanceTypes.TIGHT;

	/** The success dominance type. */
	@Parameter(description =
			"Type of sucess dominance checks to use. Only required in "
			+ "conjunction with DAG planning algorithms",
			defaultValue = "OPEN")
	protected SuccessDominanceTypes successDominanceType = SuccessDominanceTypes.LOOSE;

	/** The iterative executor type. */
	@Parameter(description = "Type of iterative executor to use. Only applies "
			+ "to DAG planning algorithms.",
			defaultValue = "MULTITHREADED")
	protected IterativeExecutorTypes iterativeExecutorType = IterativeExecutorTypes.MULTITHREADED;

	/** The first phase threads. */
	@Parameter(description = "Number of threads to use in the first phase of "
			+ "a parallel DAG planning algorithm",
			defaultValue = "50")
	protected Integer firstPhaseThreads = 5;

	/** The second phase threads. */
	@Parameter(description = "Number of threads to use in the second phase of "
			+ "a parallel DAG planning algorithm",
			defaultValue = "50")
	protected Integer secondPhaseThreads = 50;

	/** The depth threshold. */
	@Parameter(description = "Threshold for the DEPTH_THROTTLING validator",
			defaultValue = "2")
	protected Integer depthThreshold = 2;

	/** The order aware. */
	@Parameter(description = "If true, all join orders are considered during "
			+ "plan search",
			defaultValue = "true")
	protected Boolean orderAware = true;
	
	/** The access file. */
	@Parameter(description = "Contains the desired list of accesses")
	protected String accessFile;
	
	/** The cardinality estimator type. */
	protected CardinalityEstimatorTypes cardinalityEstimatorType = CardinalityEstimatorTypes.DEFAULT;

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
	 * Gets the cardinality estimator type.
	 *
	 * @return SuccessDominanceTypes
	 */
	public CardinalityEstimatorTypes getCardinalityEstimatorType() {
		return this.cardinalityEstimatorType;
	}

	/**
	 * Sets the cardinality estimator type.
	 *
	 * @param cardinalityEstimatorType SuccessDominanceTypes
	 */
	public void setCardinalityEstimatorType (CardinalityEstimatorTypes cardinalityEstimatorType) {
		this.cardinalityEstimatorType = cardinalityEstimatorType;
	}

	/**
	 * Sets the cardinality estimator type.
	 *
	 * @param cardinalityEstimatorType the new cardinality estimator type
	 */
	public void setCardinalityEstimatorType(String cardinalityEstimatorType) {
		try {
			this.cardinalityEstimatorType = CardinalityEstimatorTypes.valueOf(cardinalityEstimatorType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting post pruning type to " + null, e);
			this.cardinalityEstimatorType = null;
		}
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
		
		/** The tight. */
		@EnumParameterValue(description = 
				"Closed dominance. Given two closed configurations, one "
				+ "dominate the other if its facts are contained in the facts "
				+ "of the other, up to homomorphism")
		TIGHT,
	}

	/**
	 * The Enum SuccessDominanceTypes.
	 */
	public static enum SuccessDominanceTypes {
		
		/** The loose. */
		@EnumParameterValue(description = "Closed dominance on successful configurations.")
		LOOSE,
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
	 * The Enum CardinalityEstimatorTypes.
	 */
	public static enum CardinalityEstimatorTypes {
		
		/** The default. */
		@EnumParameterValue(description = "")
		DEFAULT
	}
}
