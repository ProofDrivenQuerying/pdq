package uk.ac.ox.cs.pdq.cost;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * Hold the cost-related parameters.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class CostParameters extends uk.ac.ox.cs.pdq.Parameters {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5183026336708644030L;

	/** Logger. */
	private static Logger log = Logger.getLogger(CostParameters.class);
	
	/** The black box connection url. */
	@Parameter(description="Connection URL for the database used by "
			+ "the BLACKBOX_DB cost estimator (required if the cost_type=BLACKBOX_DB)")
	protected String blackBoxConnectionUrl;

	/** The black box database name. */
	@Parameter(description="Name of the database used by the BLACKBOX_DB cost "
			+ "estimator (required if the cost_type=BLACKBOX_DB)")
	protected String blackBoxDatabaseName;

	/** The black box database driver. */
	@Parameter(description="Driver for the database used by the BLACKBOX_DB cost "
			+ "estimator (required if the cost_type=BLACKBOX_DB)")
	protected String blackBoxDatabaseDriver;

	/** The black box database user. */
	@Parameter(description="Username for the database used by the BLACKBOX_DB "
			+ "cost estimator (required if the cost_type=BLACKBOX_DB)")
	protected String blackBoxDatabaseUser;

	/** The black box database password. */
	@Parameter(description="Password for the database used by the BLACKBOX_DB "
			+ "cost estimator (required if the cost_type=BLACKBOX_DB)")
	protected String blackBoxDatabasePassword;
	
	/** The cost type. */
	@Parameter(description="Type of cost estimation to use. This has an "
			+ "influence on the requirements of other planner parameters.\n"
			+ "If such requirements are violated, a PlannerException will be "
			+ "thrown upon initialization of the Planner.",
			defaultValue = "TEXTBOOK")
	protected CostTypes costType = CostTypes.TEXTBOOK;
	
	/** The cardinality estimation type. */
	@Parameter(description="Type of cardinality estimation to use.",
			defaultValue = "NAIVE")
	protected CardinalityEstimationTypes cardinalityEstimationType = CardinalityEstimationTypes.NAIVE;
	
	/** The catalog. */
	@Parameter(description="File which stores the database metadata ")
	protected String catalog;

	/**  Properties file name. */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-cost.properties";

	/**  Properties file path. */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Constructor for PlannerParameters using default configuration file path.
	 */
	public CostParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH), false, false);
	}
	
	/**
	 * Gets the version.
	 *
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
		super(config, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}
	
	/**
	 * Gets the black box connection url.
	 *
	 * @return String
	 */
	public String getBlackBoxConnectionUrl() {
		return this.blackBoxConnectionUrl;
	}

	/**
	 * Gets the black box database name.
	 *
	 * @return String
	 */
	public String getBlackBoxDatabaseName() {
		return this.blackBoxDatabaseName;
	}

	/**
	 * Gets the black box database driver.
	 *
	 * @return String
	 */
	public String getBlackBoxDatabaseDriver() {
		return this.blackBoxDatabaseDriver;
	}

	/**
	 * Gets the black box database password.
	 *
	 * @return String
	 */
	public String getBlackBoxDatabasePassword() {
		return this.blackBoxDatabasePassword;
	}

	/**
	 * Gets the black box database user.
	 *
	 * @return String
	 */
	public String getBlackBoxDatabaseUser() {
		return this.blackBoxDatabaseUser;
	}
	
	/**
	 * Sets the black box connection url.
	 *
	 * @param connectionUrl String
	 */
	public void setBlackBoxConnectionUrl(String connectionUrl) {
		this.blackBoxConnectionUrl = connectionUrl;
	}

	/**
	 * Sets the black box database driver.
	 *
	 * @param databaseDriver the new black box database driver
	 */
	public void setBlackBoxDatabaseDriver(String databaseDriver) {
		this.blackBoxDatabaseDriver = databaseDriver;
	}

	/**
	 * Sets the black box database name.
	 *
	 * @param databaseName String
	 */
	public void setBlackBoxDatabaseName(String databaseName) {
		this.blackBoxDatabaseName = databaseName;
	}

	/**
	 * Sets the black box database password.
	 *
	 * @param databasePassword String
	 */
	public void setBlackBoxDatabasePassword(String databasePassword) {
		this.blackBoxDatabasePassword = databasePassword;
	}

	/**
	 * Sets the black box database user.
	 *
	 * @param databaseUser String
	 */
	public void setBlackBoxDatabaseUser(String databaseUser) {
		this.blackBoxDatabaseUser = databaseUser;
	}
	
	/**
	 * Sets the catalog.
	 *
	 * @param catalog the new catalog
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	
	/**
	 * Gets the catalog.
	 *
	 * @return the catalog
	 */
	public String getCatalog() {
		return this.catalog;
	}
	
	/**
	 * Gets the cost type.
	 *
	 * @return CostTypes
	 */
	public CostTypes getCostType() {
		return this.costType;
	}

	/**
	 * Sets the cost type.
	 *
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
	 * Sets the cost type.
	 *
	 * @param costType CostTypes
	 */
	public void setCostType(CostTypes costType) {
		this.costType = costType;
	}
	
	/**
	 * Gets the cardinality estimation type.
	 *
	 * @return CardinalityEstimationTypes
	 */
	public CardinalityEstimationTypes getCardinalityEstimationType() {
		return this.cardinalityEstimationType;
	}

	/**
	 * Sets the cardinality estimation type.
	 *
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
	 * Sets the cardinality estimation type.
	 *
	 * @param cardEstType CardinalityEstimationTypes
	 */
	public void setCardinalityEstimationType(CardinalityEstimationTypes cardEstType) {
		this.cardinalityEstimationType = cardEstType;
	}
	
	/**
	 * The Enum CostTypes.
	 */
	public static enum CostTypes {
		
		/** The simple constant. */
		@EnumParameterValue(description = "Estimates the cost as the sum of the cost of all accesses in a plan, \n where access cost are provided externally")
		FIXED_COST_PER_ACCESS,
		
		/** The simple count. */
		@EnumParameterValue(description = "Estimates the cost as the sum of all accesses in a plan")
		COUNT_NUMBER_OF_ACCESSED_RELATIONS,
		
		/** The blackbox. */
		@EnumParameterValue(description = "Estimates the cost through some externally defined cost function.\nCurrently, this defaults to the white box cost functions relying on textbox cost estimation techniques")
		TEXTBOOK,
		
		/** The blackbox db. */
		@EnumParameterValue(description = "Estimates the cost by translating the query to SQL and asking its cost to a database")
		BLACKBOX_DB,
		
		/** The inverse length. */
		@EnumParameterValue(description = "Experimental: estimates the cost as the number of atoms in a plan")
		INVERSE_LENGTH,
		
		/** The simple erspi. */
		@EnumParameterValue(description = "Estimates the cost as the sum of the estimated result size per invocation associated to each access method used in a plan")
		NUMBER_OF_OUTPUT_TUPLES_PER_ACCESS
	}

	/**
	 * The Enum CardinalityEstimationTypes.
	 */
	public static enum CardinalityEstimationTypes {
		
		/** The naive. */
		@EnumParameterValue(description = "Naive cardinality estimation, based on external defined constant join/selectivity reduction factors")
		NAIVE
	}

}
