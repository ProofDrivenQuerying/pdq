package uk.ac.ox.cs.pdq.services.logicblox;

import java.io.File;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.services.ServiceParameters;

// TODO: Auto-generated Javadoc
/**
 * Holds logicblox-specific parameters.
 * 
 * @author Julien LEBLAY
 */
public class LogicBloxParameters extends ServiceParameters {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6442789858219098799L;

	/** Logger. */
	private static Logger log = Logger.getLogger(LogicBloxParameters.class); 

	/**  Properties file name. */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-logicblox-service.properties";

	/**  Properties file path. */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * The Enum OptimizationModes.
	 */
	public static enum OptimizationModes { 
 /** The in compiler. */
 IN_COMPILER, 
 /** The in engine. */
 IN_ENGINE }

	/**
	 * Constructor for LogicBloxParameters.
	 * @param config path to the configuration file to read
	 */
	public LogicBloxParameters(File config) {
		this(config.isDirectory() ?
				new File(config, DEFAULT_CONFIG_FILE_PATH) :
				new File(DEFAULT_CONFIG_FILE_PATH), false, true, true);
	}

	/**
	 * Constructor for LogicBloxParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 */
	public LogicBloxParameters(File config, boolean verbose) {
		this(config, false, verbose, false);
	}

	/**
	 * Constructor for LogicBloxParameters.
	 *
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose if true, param loading problem will be reported
	 */
	public LogicBloxParameters(File config, boolean delay, boolean verbose) {
		super(config, true, verbose, false);
		if (!delay) {
			this.load(config, verbose, false);
		}
	}

	/**
	 * Constructor for LogicBloxParameters.
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public LogicBloxParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, true, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}
	
	/** The supported version. */
	@Parameter(description = "The LogicBlox version to allow interaction with.")
	protected String supportedVersion;
	
	/** The optimization mode. */
	@Parameter(description = "The execution point where the external optimizer hooks into.")
	protected OptimizationModes optimizationMode = OptimizationModes.IN_ENGINE;
	
	/**
	 * Gets the supported version.
	 *
	 * @return String
	 */
	public String getSupportedVersion() {
		return this.supportedVersion;
	}
	
	/**
	 * Sets the supported version.
	 *
	 * @param sv String
	 */
	public void setSupportedVersion(String sv) {
		this.supportedVersion = sv;
	}
	
	/**
	 * Gets the optimization mode.
	 *
	 * @return OptimizationModes
	 */
	public OptimizationModes getOptimizationMode() {
		return this.optimizationMode;
	}
	
	/**
	 * Sets the optimization mode.
	 *
	 * @param mode OptimizationModes
	 */
	public void setOptimizationMode(OptimizationModes mode) {
		this.optimizationMode = mode;
	}
	
	/**
	 * Sets the optimization mode.
	 *
	 * @param mode String
	 */
	public void setOptimizationMode(String mode) {
		try {
			this.optimizationMode = OptimizationModes.valueOf(String.valueOf(mode).toUpperCase());
		} catch (Exception e) {
			log.warn("Unknown optimization mode: " + mode, e);
			this.optimizationMode = OptimizationModes.IN_ENGINE;
		}
	}
}
