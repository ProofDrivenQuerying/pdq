package uk.ac.ox.cs.pdq.services.rest;

import java.io.IOException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.AccessException;
import uk.ac.ox.cs.pdq.services.RequestEvent;
import uk.ac.ox.cs.pdq.util.Table;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

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

	/** The relation */
	private final RESTRelation relation ;

	/** The access */
	private final RESTAccess access;

	/** The input to the access */
	private final Table input;

	private String violationMessage = null;

	/**
	 * Default constructor
	 * @param access
	 * @param input
	 * @param relation RESTRelation
	 */
	public RESTRequestEvent(RESTRelation relation, RESTAccess access, Table input) {
		super();
		this.relation = relation;
		this.access = access;
		this.input = input;
	}

	/**
	 * @return RESTAccess
	 */
	public RESTAccess getAccess() {
		return this.access;
	}

	/**
	 * @return Table
	 */
	public Table getInput() {
		return this.input;
	}

	/**
	 * @param msg String
	 * @see uk.ac.ox.cs.pdq.services.AccessEvent#setUsageViolationMessage(String)
	 */
	@Override
	public void setUsageViolationMessage(String msg) {
		this.violationMessage = msg;
	}

	/**
	 * @return String
	 * @see uk.ac.ox.cs.pdq.services.AccessEvent#getUsageViolationMessage()
	 */
	@Override
	public String getUsageViolationMessage() {
		return this.violationMessage;
	}

	/**
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.services.AccessEvent#hasUsageViolationMessage()
	 */
	@Override
	public boolean hasUsageViolationMessage() {
		return this.violationMessage != null;
	}

	/**
	 * @return RESTResponseEvent
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 * @see uk.ac.ox.cs.pdq.services.RequestEvent#processRequest()
	 */
	@Override
	public RESTResponseEvent processRequest() throws AccessException, ProcessingException {
		WebTarget target = this.access.build();
		log.info(target.getUri());
		Response response = target.request(relation.getMediaType()).get();
		return new RESTResponseEvent(this.relation, this, response);
	}
}
