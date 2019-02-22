package uk.ac.ox.cs.pdq.datasources.legacy.services.rest;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.legacy.services.ResponseEvent;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;

/**
 * The event of a RESTAccess response. Holds the requestEvent itself, 
 * the response http response, and the output table extracted from it.
 * 
 * @author Julien Leblay
 *
 */
public class RESTResponseEvent implements ResponseEvent {

	/**
	 */
	public static enum RESTResponseStatus{
		SUCCESS,
		FAILURE};

		private static Logger log = Logger.getLogger(RESTResponseEvent.class);

		private final RESTRelation relation;

		/**  The request event this response event is associated with. */
		private final RESTRequestEvent requestEvent;

		private final Table output;

		private final Response response;

		/**  A message set if there was any violation associated with this event. */
		private String violationMessage = null;

		/**
		 * Default constructor.
		 *
		 * @param relation RESTRelation
		 * @param requestEvent the request event
		 * @param response the response
		 * @throws AccessException the access exception
		 * @throws ProcessingException the processing exception
		 */
		public RESTResponseEvent(RESTRelation relation, RESTRequestEvent requestEvent, Response response) throws AccessException, ProcessingException {
			super();
			this.relation = relation;
			this.requestEvent = requestEvent;
			this.response = response;
			if (response.getStatus() == 200) {
				if (MediaType.APPLICATION_XML_TYPE.equals(relation.getMediaType())) {
					this.output = relation.parseXml(response, requestEvent.getInput());
				} else {
					this.output = relation.parseJson(response, requestEvent.getInput());
				}
			} else if (response.getStatus() == 404
					|| response.getStatus() == 400) {
				log.warn(response.getStatusInfo().getReasonPhrase());
				this.output = new Table(relation.getAttributes());
			} else {
				throw new AccessException(response.getStatus()
						+ " - " + response.getStatusInfo().getReasonPhrase()
						+ "\n" + response.readEntity(String.class));
			}
		}
		
		public RESTResponseEvent(RESTRequestEvent requestEvent, Response response) throws AccessException, ProcessingException {
			super();
			this.relation = null;
			this.requestEvent = requestEvent;
			this.response = response;
			if (response.getStatus() == 200) {
				if (MediaType.APPLICATION_XML_TYPE.equals(relation.getMediaType())) {
					this.output = relation.parseXml(response, requestEvent.getInput());
				} else {
					this.output = relation.parseJson(response, requestEvent.getInput());
				}
			} else if (response.getStatus() == 404
					|| response.getStatus() == 400) {
				log.warn(response.getStatusInfo().getReasonPhrase());
				this.output = new Table(relation.getAttributes());
			} else {
				throw new AccessException(response.getStatus()
						+ " - " + response.getStatusInfo().getReasonPhrase()
						+ "\n" + response.readEntity(String.class));
			}
		}

		/**
		 *
		 * @return RESTAccess
		 */
		public RESTAccess getAccess() {
			return this.requestEvent.getAccess();
		}

		/**
		 *
		 * @return RESTRequestEvent
		 */
		public RESTRequestEvent getRequestEvent() {
			return this.requestEvent;
		}

		/**
		 *
		 * @return Table
		 */
		public Table getOutput() {
			return this.output;
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
		 * @return boolean
		 * @see uk.ac.ox.cs.pdq.datasources.services.ResponseEvent#hasMoreRequestEvents()
		 */
		@Override
		public boolean hasMoreRequestEvents() {
			return !this.getAccess().isComplete();
		}

		/**
		 *
		 * @return Response
		 */
		public Response getResponse() {
			return this.response;
		}

		/**
		 *
		 * @return RESTRequestEvent
		 * @see uk.ac.ox.cs.pdq.datasources.services.ResponseEvent#nextRequestEvent()
		 */
		@Override
		public RESTRequestEvent nextRequestEvent() {
			return new RESTRequestEvent(this.relation, this.getAccess(), this.requestEvent.getInput());
		}
}
