package uk.ac.ox.cs.pdq.datasources.services;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Mark Ridler
 *
 */
//RESTAccess performs the low-level web target access
public class RESTRawAccess {
	
	public Response access(WebTarget target, MediaType mediaType)
	{
		return target.request(mediaType).get();
	}
}
