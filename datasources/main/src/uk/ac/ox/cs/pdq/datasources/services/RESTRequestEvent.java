// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.legacy.services.RequestEvent;

// RESTRequestEvent occurs immediately before a REST access event
public class RESTRequestEvent implements RequestEvent {
	private static Logger log = Logger.getLogger(RESTRequestEvent.class);

	private String violationMessage = null;
	private WebTarget target = null;
	private MediaType mediaType = null;

	public RESTRequestEvent(WebTarget target, MediaType mediaType) {
		super();
		this.target = target;
		this.mediaType = mediaType;
	}
	
	@Override
	public void setUsageViolationMessage(String msg) {
		this.violationMessage = msg;
	}

	@Override
	public String getUsageViolationMessage() {
		return this.violationMessage;
	}
	
	@Override
	public boolean hasUsageViolationMessage() {
		return this.violationMessage != null;
	}

	public WebTarget getTarget() {
		return target;
	}

	public void setTarget(WebTarget target) {
		this.target = target;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	@Override
	public RESTResponseEvent processRequest() throws AccessException, ProcessingException {
		System.out.println(this.target.toString());
		log.trace(this.target.getUri());
		log.info(this.target.getUri());
		Response response = this.target.request(this.mediaType).get();
		return new RESTResponseEvent(this, response);
	}
}
