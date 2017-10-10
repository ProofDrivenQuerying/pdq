package uk.ac.ox.cs.pdq.datasources.services.rest;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.services.RequestEvent;
import uk.ac.ox.cs.pdq.datasources.utility.Table;

/**
 * The event occurred directly before a RESTAccess request. Holds the access 
 * itself, and the input table to be used.
 * 
 * @author Julien Leblay
 *
 */
public class RESTRequestEvent implements RequestEvent {

	/** Logger. */
	private static Logger log = Logger.getLogger(RESTRequestEvent.class);

	/**  The relation. */
	private final RESTRelation relation ;

	/**  The access. */
	private final RESTAccess access;

	/**  The input to the access. */
	private final Table input;

	/** The violation message. */
	private String violationMessage = null;

	/**
	 * Default constructor.
	 *
	 * @param relation RESTRelation
	 * @param access the access
	 * @param input the input
	 */
	public RESTRequestEvent(RESTRelation relation, RESTAccess access, Table input) {
		super();
		this.relation = relation;
		this.access = access;
		this.input = input;
	}

	/**
	 * Gets the access.
	 *
	 * @return RESTAccess
	 */
	public RESTAccess getAccess() {
		return this.access;
	}

	/**
	 * Gets the input.
	 *
	 * @return Table
	 */
	public Table getInput() {
		return this.input;
	}

	/**
	 * Sets the usage violation message.
	 *
	 * @param msg String
	 * @see uk.ac.ox.cs.pdq.datasources.services.AccessEvent#setUsageViolationMessage(String)
	 */
	@Override
	public void setUsageViolationMessage(String msg) {
		this.violationMessage = msg;
	}

	/**
	 * Gets the usage violation message.
	 *
	 * @return String
	 * @see uk.ac.ox.cs.pdq.datasources.services.AccessEvent#getUsageViolationMessage()
	 */
	@Override
	public String getUsageViolationMessage() {
		return this.violationMessage;
	}

	/**
	 * Checks for usage violation message.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.datasources.services.AccessEvent#hasUsageViolationMessage()
	 */
	@Override
	public boolean hasUsageViolationMessage() {
		return this.violationMessage != null;
	}

	/**
	 * Process request.
	 *
	 * @return RESTResponseEvent
	 * @throws AccessException the access exception
	 * @throws ProcessingException the processing exception
	 * @see uk.ac.ox.cs.pdq.datasources.services.RequestEvent#processRequest()
	 */
	@Override
	public RESTResponseEvent processRequest() throws AccessException, ProcessingException {
		WebTarget target = this.access.build();
		log.trace(target.getUri());
		log.info(target.getUri());
		Response response = target.request(relation.getMediaType()).get();
		return new RESTResponseEvent(this.relation, this, response);
	}
}
