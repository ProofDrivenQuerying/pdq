package uk.ac.ox.cs.pdq.cost;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Hold the cost-related parameters
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class CostParameters extends uk.ac.ox.cs.pdq.Parameters {

	/** */
	private static final long serialVersionUID = -5183026336708644030L;

	/** Logger. */
	private static Logger log = Logger.getLogger(CostParameters.class);
	
	@Parameter(description="Type of query translator to use in the BLACKBOX_DB"
			+ "cost estimator (required if the cost_type=BLACKBOX_DB)")
	protected BlackBoxQueryTypes blackBoxQueryType;
	
	@Parameter(description="Connection URL for the database used by "
			+ "the BLACKBOX_DB cost estimator (required if the cost_type=BLACKBOX_DB)")
	protected String blackBoxConnectionUrl;

	@Parameter(description="Name of the database used by the BLACKBOX_DB cost "
			+ "estimator (required if the cost_type=BLACKBOX_DB)")
	protected String blackBoxDatabaseName;

	@Parameter(description="Username for the database used by the BLACKBOX_DB "
			+ "cost estimator (required if the cost_type=BLACKBOX_DB)")
	protected String blackBoxDatabaseUser;

	@Parameter(description="Password for the database used by the BLACKBOX_DB "
			+ "cost estimator (required if the cost_type=BLACKBOX_DB)")
	protected String blackBoxDatabasePassword;
	
	@Parameter(description="Type of cost estimation to use. This has an "
			+ "influence on the requirements of other planner parameters.\n"
			+ "If such requirements are violated, a PlannerException will be "
			+ "thrown upon initialization of the Planner.",
			defaultValue = "BLACKBOX")
	protected CostTypes costType = CostTypes.BLACKBOX;
	
	@Parameter(description="Type of cardinality estimation to use.",
			defaultValue = "NAIVE")
	protected CardinalityEstimationTypes cardinalityEstimationType = CardinalityEstimationTypes.NAIVE;
	
	@Parameter(description="File which stores the database metadata ")
	protected String catalog;

	/** Properties file name */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-cost.properties";

	/** Properties file path */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Constructor for PlannerParameters using default configuration file path.
	 */
	public CostParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH), false, false);
	}
	
	/**
	 * @return the version of the planner code, as given by Maven
	 */
	public static String getVersion() {
	    String path = "/cost.version";
	    try (InputStream stream = CostParameters.class.getResourceAsStream(path)) {
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
	 * Constructor for PlannerParameters.
	 * @param config path to the configuration file to read
	 */
	public CostParameters(File config) {
		this(config, false, false);
	}

	/**
	 * Constructor for PlannerParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 */
	public CostParameters(File config, boolean verbose) {
		this(config, false, verbose, false);
	}

	/**
	 * Constructor for PlannerParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public CostParameters(File config, boolean verbose, boolean strict) {
		this(config, false, verbose, strict);
	}

	/**
	 * Constructor for PlannerParameters.
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public CostParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, true, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}
	
	/**
	 * @return BlackBoxQueryTypes
	 */
	public BlackBoxQueryTypes getBlackBoxQueryType() {
		if (this.blackBoxQueryType == null) {
			return BlackBoxQueryTypes.DEFAULT;
		}
		return this.blackBoxQueryType;
	}

	/**
	 * @param sqlQueryType BlackBoxQueryTypes
	 */
	public void setBlackBoxQueryType(BlackBoxQueryTypes sqlQueryType) {
		this.blackBoxQueryType = sqlQueryType;
	}

	/**
	 * @param queryType String
	 */
	public void setBlackBoxQueryType(String queryType) {
		try {
			this.blackBoxQueryType = BlackBoxQueryTypes.valueOf(queryType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting SQL black box query type to " + BlackBoxQueryTypes.DEFAULT, e);
			this.blackBoxQueryType = BlackBoxQueryTypes.DEFAULT;
		}
	}
	
	/**
	 * @return String
	 */
	public String getBlackBoxConnectionUrl() {
		return this.blackBoxConnectionUrl;
	}

	/**
	 * @return String
	 */
	public String getBlackBoxDatabaseName() {
		return this.blackBoxDatabaseName;
	}

	/**
	 * @return String
	 */
	public String getBlackBoxDatabasePassword() {
		return this.blackBoxDatabasePassword;
	}

	/**
	 * @return String
	 */
	public String getBlackBoxDatabaseUser() {
		return this.blackBoxDatabaseUser;
	}
	
	/**
	 * @param connectionUrl String
	 */
	public void setBlackBoxConnectionUrl(String connectionUrl) {
		this.blackBoxConnectionUrl = connectionUrl;
	}

	/**
	 * @param databaseName String
	 */
	public void setBlackBoxDatabaseName(String databaseName) {
		this.blackBoxDatabaseName = databaseName;
	}

	/**
	 * @param databasePassword String
	 */
	public void setBlackBoxDatabasePassword(String databasePassword) {
		this.blackBoxDatabasePassword = databasePassword;
	}

	/**
	 * @param databaseUser String
	 */
	public void setBlackBoxDatabaseUser(String databaseUser) {
		this.blackBoxDatabaseUser = databaseUser;
	}
	
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	
	public String getCatalog() {
		return this.catalog;
	}
	
	/**
	 * @return CostTypes
	 */
	public CostTypes getCostType() {
		return this.costType;
	}

	/**
	 * @param costType String
	 */
	public void setCostType(String costType) {
		try {
			this.costType = CostTypes.valueOf(costType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting cost type to " + null, e);
			this.costType = null;
		}
	}

	/**
	 * @param costType CostTypes
	 */
	public void setCostType(CostTypes costType) {
		this.costType = costType;
	}
	
	/**
	 * @return CardinalityEstimationTypes
	 */
	public CardinalityEstimationTypes getCardinalityEstimationType() {
		return this.cardinalityEstimationType;
	}

	/**
	 * @param cardEstType String
	 */
	public void setCardinalityEstimationType(String cardEstType) {
		try {
			this.cardinalityEstimationType = CardinalityEstimationTypes.valueOf(cardEstType);
		} catch (IllegalArgumentException e) {
			log.warn("Setting cardinality estimation type to " + CardinalityEstimationTypes.NAIVE, e);
			this.cardinalityEstimationType = CardinalityEstimationTypes.NAIVE;
		}
	}

	/**
	 * @param cardEstType CardinalityEstimationTypes
	 */
	public void setCardinalityEstimationType(CardinalityEstimationTypes cardEstType) {
		this.cardinalityEstimationType = cardEstType;
	}
	
	/** The types of SQL queries the SQL estimator can use. */
	public static enum BlackBoxQueryTypes {
		@EnumParameterValue(description = "Default translator from DAG plan to SQL for blackbox cost estimation")
		DEFAULT,

		@EnumParameterValue(description = "Translator from DAG plan to SQL WITH query for blackbox cost estimation")
		SQL_WITH
	}
	
	/** */
	public static enum CostTypes {
		@EnumParameterValue(description = "Estimates the cost as the sum of the cost of all accesses in a plan, \n where access cost are provided externally")
		SIMPLE_CONSTANT,
		@EnumParameterValue(description = "Estimates the cost as the sum of the cost of all accesses in a plan, \n where cost are assigned randomly")
		SIMPLE_RANDOM,
		@EnumParameterValue(description = "Estimates the cost as the sum of the cost of all accesses in a plan, \n where cost are measured automatically from the underlying datasources")
		SIMPLE_GIVEN,
		@EnumParameterValue(description = "Estimates the cost as the sum of all accesses in a plan")
		SIMPLE_COUNT,
		@EnumParameterValue(description = "Estimates the cost through some externally defined cost function.\nCurrently, this defaults to the white box cost functions relying on textbox cost estimation techniques")
		BLACKBOX,
		@EnumParameterValue(description = "Estimates the cost by translating the query to SQL and asking its cost to a database")
		BLACKBOX_DB,
		@EnumParameterValue(description = "Experimental: estimates the cost as the number of atoms in a plan")
		INVERSE_LENGTH,
		@EnumParameterValue(description = "Estimates the cost as the sum of the estimated result size per invocation associated to each access method used in a plan")
		SIMPLE_ERSPI
	}

	/** */
	public static enum CardinalityEstimationTypes {
		@EnumParameterValue(description = "Naive cardinality estimation, based on external defined constant join/selectivity reduction factors")
		NAIVE,

//		@EnumParameterValue(description = "Cardinality estimation based on the min/max method described in the literature")
//		MIN_MAX, 
//
//		@EnumParameterValue(description = "Histogram-based cardinality estimation")
//		HISTOGRAMS
	}

}
