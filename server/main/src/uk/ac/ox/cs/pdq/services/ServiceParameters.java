package uk.ac.ox.cs.pdq.services;

import java.io.File;

/**
 * Holds server-related parameters.
 * 
 * @author Julien LEBLAY
 */
public class ServiceParameters extends uk.ac.ox.cs.pdq.Parameters {

	/** */
	private static final long serialVersionUID = 1481542784450218004L;

	/** Properties file name */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-manager-service.properties";

	/** Properties file path */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Constructor for ServiceParameters using default configuration file path.
	 */
	public ServiceParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH));
	}

	/**
	 * Constructor for ServiceParameters.
	 * @param config path to the configuration file to read
	 */
	public ServiceParameters(File config) {
		this(config.isDirectory() ?
				new File(config, DEFAULT_CONFIG_FILE_PATH) :
				new File(DEFAULT_CONFIG_FILE_PATH), false, true, true);
	}

	/**
	 * Constructor for ServiceParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 */
	public ServiceParameters(File config, boolean verbose) {
		this(config, false, verbose, false);
	}

	/**
	 * Constructor for ServiceParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public ServiceParameters(File config, boolean verbose, boolean strict) {
		this(config, false, verbose, strict);
	}

	/**
	 * Constructor for ServiceParameters.
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public ServiceParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, true, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}

	@Parameter(description=
			"Name of the service. Among others, this name is used to refer to "
			+ "this service when issuing commands to it from a console")
	protected String serviceName;

	@Parameter(description="Port on which the service will listen.")
	protected Integer port;
	
	/**
	 * @return String
	 */
	public String getServiceName() {
		return this.serviceName;
	}
	
	/**
	 * @param name String
	 */
	public void setServiceName(String name) {
		this.serviceName = name;
	}
	
	/**
	 * @return Integer
	 */
	public Integer getPort() {
		return this.port;
	}
	
	/**
	 * @param port Integer
	 */
	public void setPort(Integer port) {
		this.port = port;
	}
	
	/**
	 * @param port Number
	 */
	public void setPort(Number port) {
		this.port = port.intValue();
	}
}
