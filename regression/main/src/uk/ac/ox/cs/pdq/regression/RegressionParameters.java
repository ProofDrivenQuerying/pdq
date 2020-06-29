// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Hold the initialConfig of an execution.
 * 
 * @author Julien LEBLAY
 */
public class RegressionParameters extends uk.ac.ox.cs.pdq.Parameters {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7756663320915095016L;

	/** */
	private static Logger log = Logger.getLogger(RegressionParameters.class); 

	/**  Properties file name. */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-regression.properties";

	/**  Properties file path. */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Constructor for RegressionParameters using default configuration file path.
	 */
	public RegressionParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH));
	}

	/**
	 * Constructor for RegressionParameters.
	 * @param config path to the configuration file to read
	 */
	public RegressionParameters(File config) {
		this(config, false, false, false);
	}

	/**
	 * Constructor for RegressionParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 */
	public RegressionParameters(File config, boolean verbose) {
		this(config, false, verbose, false);
	}

	/**
	 * Constructor for RegressionParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public RegressionParameters(File config, boolean verbose, boolean strict) {
		this(config, false, verbose, strict);
	}

	/**
	 * Constructor for RuntimeParameters.
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public RegressionParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}
	
	/** The expected cardinality. */
	@Parameter(description =
			"The expected cardinality of a plan execution result.\n"
			+ "This can be used for instance when there is no way to execute the "
			+ "input query to obtain the ground truth.",
			defaultValue = "-1")
	private Integer expectedCardinality = -1;
	
	/**  */
	@Parameter(description =
			"If true, skip the runtime test.\n"
			+ "This can be used for cases when testing runtime can be "
			+ "costly (e.g. web services).",
			defaultValue = "false")
	private Boolean skipRuntime = false;
	
	/**
	 * 
	 *
	 * @return Integer
	 */
	public Integer getExpectedCardinality() {
		return this.expectedCardinality;
	}
	
	/**
	 * 
	 *
	 * @param i Integer
	 */
	public void setExpectedCardinality(Integer i) {
		this.expectedCardinality = i;
	}
	
	/**
	 * G
	 *
	 * @return Boolean
	 */
	public Boolean getSkipRuntime() {
		return this.skipRuntime;
	}
	
	/**
	 * 
	 *
	 * @param b Boolean
	 */
	public void setSkipRuntime(Boolean b) {
		this.skipRuntime = b;
	}
	
	/**
	 * 
	 *
	 * @param i Integer
	 */
	public void setExpectedCardinality(Number i) {
		this.expectedCardinality = i.intValue();
	}

	/**
	 * 
	 *
	 * @return the version of the builders code, as given by Maven
	 */
	public static String getVersion() {
		String path = "/regression.version";
		try (InputStream stream = RegressionParameters.class.getResourceAsStream(path)) {
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
}
