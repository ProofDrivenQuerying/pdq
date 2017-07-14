package uk.ac.ox.cs.pdq.datasources.services.rest;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.Table;
import uk.ac.ox.cs.pdq.datasources.services.ResponseEvent;

// TODO: Auto-generated Javadoc
/**
 * The event of a RESTAccess response. Holds the requestEvent itself, 
 * the response http response, and the output table extracted from it.
 * 
 * @author Julien Leblay
 *
 */
public class RESTResponseEvent implements ResponseEvent {

	/**
	 * The Enum RESTResponseStatus.
	 */
	public static enum RESTResponseStatus{/** The success. */
		SUCCESS,/** The failure. */
		FAILURE};

		/** Logger. */
		private static Logger log = Logger.getLogger(RESTResponseEvent.class);

		/**  The rest relation. */
		private final RESTRelation relation;

		/**  The request event this response event is associated with. */
		private final RESTRequestEvent requestEvent;

		/**  The requestEvent output table of the requestEvent. */
		private final Table output;

		/**  The response. */
		private final Response response;

		/**  A message set of there was any violation associated with this event. */
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

		/**
		 * Gets the access.
		 *
		 * @return RESTAccess
		 */
		public RESTAccess getAccess() {
			return this.requestEvent.getAccess();
		}

		/**
		 * Gets the request event.
		 *
		 * @return RESTRequestEvent
		 */
		public RESTRequestEvent getRequestEvent() {
			return this.requestEvent;
		}

		/**
		 * Gets the output.
		 *
		 * @return Table
		 */
		public Table getOutput() {
			return this.output;
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
		 * Checks for more request events.
		 *
		 * @return boolean
		 * @see uk.ac.ox.cs.pdq.datasources.services.ResponseEvent#hasMoreRequestEvents()
		 */
		@Override
		public boolean hasMoreRequestEvents() {
			return !this.getAccess().isComplete();
		}

		/**
		 * Gets the response.
		 *
		 * @return Response
		 */
		public Response getResponse() {
			return this.response;
		}

		/**
		 * Next request event.
		 *
		 * @return RESTRequestEvent
		 * @see uk.ac.ox.cs.pdq.datasources.services.ResponseEvent#nextRequestEvent()
		 */
		@Override
		public RESTRequestEvent nextRequestEvent() {
			return new RESTRequestEvent(this.relation, this.getAccess(), this.requestEvent.getInput());
		}
}
