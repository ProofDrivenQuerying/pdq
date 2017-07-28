package uk.ac.ox.cs.pdq.datasources.services;

import uk.ac.ox.cs.pdq.datasources.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.services.policies.UsagePolicy;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.db.Attribute;

// TODO: Auto-generated Javadoc
/**
 * General interface to online services
 *  
 * @author Julien Leblay
 *
 */
public interface Service {

	/** The Constant URL. */
	public static final String URL = "url";
	
	/** The Constant MULTIPLE_RESULT_PATH. */
	public static final String MULTIPLE_RESULT_PATH = "multiple-result-path";
	
	/** The Constant SINGLE_RESULT_PATH. */
	public static final String SINGLE_RESULT_PATH = "single-result-path";
	
	/** The Constant RESULTS_DELIMITER. */
	public static final String RESULTS_DELIMITER = "results-delimiter";

	/**
	 * Gets the name.
	 *
	 * @return the service's name
	 */
	String getName();
	
	/**
	 * Perfoms an access to the service, without any input.
	 *
	 * @return the output table resulting from the access.
	 */
	Table access();
	
	/**
	 * Perfoms an access to the service, with the given input.
	 *
	 * @param inputHeader the input header
	 * @param inputTuples the input tuples
	 * @return the output table resulting from the access.
	 */
	Table access(Attribute[] inputHeader, ResetableIterator<Tuple> inputTuples);

	/**
	 * Registers a usage policy.
	 *
	 * @param p the p
	 */
	void register(UsagePolicy p);

	/**
	 * Unregisters a usage policy.
	 *
	 * @param p the p
	 */
	void unregister(UsagePolicy p);
}
