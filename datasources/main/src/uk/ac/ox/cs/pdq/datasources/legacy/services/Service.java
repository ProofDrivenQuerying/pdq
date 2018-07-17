package uk.ac.ox.cs.pdq.datasources.legacy.services;

import uk.ac.ox.cs.pdq.datasources.legacy.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.legacy.services.policies.UsagePolicy;
import uk.ac.ox.cs.pdq.datasources.resultstable.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * General interface to online services
 *  
 * @author Julien Leblay
 *
 */
public interface Service {

	public static final String URL = "url";
	
	public static final String MULTIPLE_RESULT_PATH = "multiple-result-path";
	
	public static final String SINGLE_RESULT_PATH = "single-result-path";
	
	public static final String RESULTS_DELIMITER = "results-delimiter";

	/**
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
