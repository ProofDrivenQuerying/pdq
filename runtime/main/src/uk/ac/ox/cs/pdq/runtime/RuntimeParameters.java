package uk.ac.ox.cs.pdq.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Hold the configuration of a runtime execution.
 * 
 * @author Julien Leblay
 */
public class RuntimeParameters extends uk.ac.ox.cs.pdq.Parameters {

	private static final long serialVersionUID = -5183026336708644030L;

	private static Logger log = Logger.getLogger(RuntimeParameters.class);

	/**  Properties file name. */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-runtime.properties";

	/**  Properties file path. */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;
	
	/** The tuples limit. */
	@Parameter(description="The maximum number of output tuples")
	protected Integer tuplesLimit;
	@Parameter(description="Relative or absolute location of the access directory")
	private String accessDirectory;
	/**
	 * Gets the version.
	 *
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
		super(config, false, false);
	}

	/**
	 * Constructor for RuntimeParameters.
	 * @param config path to the configuration file to read
	 * @param verbose the verbose
	 */
	public RuntimeParameters(File config, boolean verbose) {
		super(config, verbose, false);
	}
	
	/**
	 *
	 * @return the tuples limit
	 */
	public Integer getTuplesLimit() {
		if (this.tuplesLimit == null) {
			return Integer.MAX_VALUE;
		}
		return this.tuplesLimit;
	}
	
	/**
	 *
	 * @param tuples the new tuples limit
	 */
	public void setTuplesLimit(Integer tuples) {
		this.tuplesLimit = tuples;
	}
	
	/**
	 *
	 * @param tuples the new tuples limit
	 */
	public void setTuplesLimit(Number tuples) {
		this.tuplesLimit = tuples != null ? tuples.intValue() : null;
	}

	public String getAccessDirectory() {
		if (accessDirectory==null || accessDirectory.isEmpty())
			return "accesses";
		return accessDirectory;
	}

	public void setAccessDirectory(String accessDirectory) {
		this.accessDirectory = accessDirectory;
	}

}
