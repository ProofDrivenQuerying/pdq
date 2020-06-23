// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	
	/** The executor type. */
	@Parameter(description="Type of executor to use in plan evaluation")
	protected ExecutorTypes executorType;

	/** The tuples limit. */
	@Parameter(description="The maximum number of output tuples")
	protected Integer tuplesLimit;
	@Parameter(description="Relative or absolute location of the access directory")
	private String accessDirectory;
	@Parameter(description="Semicolon (;) separated list of of fully qualified class names of custom made ExecutableAccessMethods")
	private String customAccessMethods;
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

	public String getAccessDirectory() {
		if (accessDirectory==null || accessDirectory.isEmpty())
			return "accesses";
		return accessDirectory;
	}

	public void setAccessDirectory(String accessDirectory) {
		this.accessDirectory = accessDirectory;
	}

	/**
	 * Gets the executor type.
	 *
	 * @return ExecutorTypes
	 */
	public ExecutorTypes getExecutorType() {
		return this.executorType;
	}

	/**
	 * Sets the executor type.
	 *
	 * @param executorType ExecutorTypes
	 */
	public void setExecutorType(ExecutorTypes executorType) {
		this.executorType = executorType;
	}

	/**
	 * Unset executor type.
	 */
	public void unsetExecutorType() {
		this.executorType = null;
	}

	/**
	 * Sets the executor type.
	 *
	 * @param executorType String
	 */
	public void setExecutorType(String executorType) {
		this.executorType = ExecutorTypes.valueOf(executorType);
	}

	/**
	 * Gets the tuples limit.
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
	 * Sets the tuples limit.
	 *
	 * @param tuples the new tuples limit
	 */
	public void setTuplesLimit(Integer tuples) {
		this.tuplesLimit = tuples;
	}
	
	/**
	 * Sets the tuples limit.
	 *
	 * @param tuples the new tuples limit
	 */
	public void setTuplesLimit(Number tuples) {
		this.tuplesLimit = tuples != null ? tuples.intValue() : null;
	}
	
	/**
	 *  Executor types.
	 */
	public static enum ExecutorTypes {
		
		/** The pipelined. */
		@EnumParameterValue(description="Volcano-style pipelining iterator execution")
		PIPELINED, 
		
		/** The sql tree. */
		@EnumParameterValue(description=
		"Executes a query by translating it to a nested SQL query, "
				+ "and delegating its execution to an external RDBMS.")
		SQL_TREE, 
		
		/** The sql step. */
		@EnumParameterValue(description=
		"Executes a query by translating it to a sequence of SQL queries,  "
				+ "and delegating its execution to an external RDBMS."
				+ "Each query is materialized and possibly relies on a "
				+ "previously materialized one.")
		SQL_STEP, 

		/** The sql with. */
		@EnumParameterValue(description=
		"Executes a query by translating it to a SQL WITH query, "
				+ "and delegating its execution to an external RDBMS.")
		SQL_WITH}

	public List<String> getCustomAccessClassNames() {
		if (customAccessMethods==null || customAccessMethods.isEmpty())
			return new ArrayList<>();
		return Arrays.asList(customAccessMethods.split(";"));
	}

	public String getCustomAccessMethods() {
		return customAccessMethods;
	}

	public void setCustomAccessMethods(String customAccessMethods) {
		this.customAccessMethods = customAccessMethods;
	}

}
