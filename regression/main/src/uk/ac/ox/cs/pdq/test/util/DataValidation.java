package uk.ac.ox.cs.pdq.test.util;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.runtime.util.AccessException;

// TODO: Auto-generated Javadoc
/**
 * Class that checks if the data are consistent w.r.t. the schema dependencies
 * @author Efi Tsamoura
 *
 */

public abstract class DataValidation {
	
	/**  The input schema. */
	protected final Schema schema;
	
	/**
	 * Constructor for DataValidation.
	 * @param schema Schema
	 */
	public DataValidation(Schema schema) {
		assert schema != null;
		this.schema = schema;
	}

	/**
	 * Validate.
	 *
	 * @return true if validation succeeds
	 * @throws PlannerException the planner exception
	 * @throws AccessException the access exception
	 */
	abstract public Boolean validate() throws PlannerException, AccessException;
}
