// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * Specialises parameters for demo purpose.
 * 
 * @author Julien LEBLAY
 */
public class UIParameters extends uk.ac.ox.cs.pdq.Parameters {

	/** Logger. */
	private static Logger log = Logger.getLogger(UIParameters.class);

	/**  Properties file name. */
	static final String DEFAULT_CONFIG_FILE_NAME = "pdq-demo.properties";

	/**  Properties file path. */
	static final String DEFAULT_CONFIG_FILE_PATH = "./" + DEFAULT_CONFIG_FILE_NAME;

	/**
	 * Gets the version.
	 *
	 * @return the version of the planner code, as given by Maven
	 */
	public static String getVersion() {
	    String path = "/demo.version";
	    try (InputStream stream = UIParameters.class.getResourceAsStream(path)) {
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
	 * Instantiates a new UI parameters.
	 */
	 public UIParameters() {
		this(new File(DEFAULT_CONFIG_FILE_PATH));
	} 

	/**
	 * Constructor for DemoParameters.
	 * @param config path to the configuration file to read
	 */
	public UIParameters(File config) {
		this(config, false, false);
	}

	/**
	 * Constructor for DemoParameters.
	 * @param config path to the configuration file to read
	 * @param verbose if true, param loading problem will be reported
	 */
	public UIParameters(File config, boolean verbose) {
		this(config, false, verbose, false);
	}

	/**
	 * Constructor for DemoParameters.
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose if true, param loading problem will be reported
	 */
	public UIParameters(File config, boolean delay, boolean verbose) {
		super(config, verbose, false);
		if (!delay) {
			this.load(config, verbose, false);
		}
	}

	/**
	 * Constructor for DemoParameters.
	 * @param config path to the configuration file to read
	 * @param delay true if the loading of the given config file should be delayed
	 * @param verbose if true, param loading problem will be reported
	 * @param strict if true, param loading problem will throw an exception
	 */
	public UIParameters(File config, boolean delay, boolean verbose, boolean strict) {
		super(config, verbose, strict);
		if (!delay) {
			this.load(config, verbose, strict);
		}
	}
}
