package uk.ac.ox.cs.pdq.datasources.legacy.services.rest;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.legacy.services.RequestEvent;
import uk.ac.ox.cs.pdq.util.Table;

/**
 * The event occurring directly before a RESTAccess request. Holds the access 
 * itself, and the input table to be used.
 * 
 * @author Julien Leblay
 *
 */
public class RESTRequestEvent implements RequestEvent {
	private static Logger log = Logger.getLogger(RESTRequestEvent.class);

	private final RESTRelation relation ;

	private final RESTAccess access;

	/**  The input to the access. */
	private final Table input;

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

	public RESTRequestEvent(RESTAccess access, Table input) {
		super();
		this.relation = null;
		this.access = access;
		this.input = input;
	}

	/**
	 *
	 * @return RESTAccess
	 */
	public RESTAccess getAccess() {
		return this.access;
	}

	/**
	 *
	 * @return Table
	 */
	public Table getInput() {
		return this.input;
	}

	/**
	 *
	 * @param msg String
	 * @see uk.ac.ox.cs.pdq.datasources.services.AccessEvent#setUsageViolationMessage(String)
	 */
	@Override
	public void setUsageViolationMessage(String msg) {
		this.violationMessage = msg;
	}

	/**
	 *
	 * @return String
	 * @see uk.ac.ox.cs.pdq.datasources.services.AccessEvent#getUsageViolationMessage()
	 */
	@Override
	public String getUsageViolationMessage() {
		return this.violationMessage;
	}

	/**
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.datasources.services.AccessEvent#hasUsageViolationMessage()
	 */
	@Override
	public boolean hasUsageViolationMessage() {
		return this.violationMessage != null;
	}

	/**
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
		MediaType mediaType = new MediaType("application", "json");
		if(relation != null) mediaType = relation.getMediaType();
		Response response = target.request(mediaType).get();
		return new RESTResponseEvent(relation, this, response);
	}
}
