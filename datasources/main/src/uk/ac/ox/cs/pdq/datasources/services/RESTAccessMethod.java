package uk.ac.ox.cs.pdq.datasources.services;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.services.policies.UsagePolicy;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * @author Mark Ridler
 *
 */
// RESTAccessMethod is the implementation which calls REST for a defined access method
public class RESTAccessMethod extends ExecutableAccessMethod {

	private static final long serialVersionUID = 1L;
	
	private WebTarget target;
	private MediaType mediaType;
	private JsonResponseUnmarshaller jsonResponseUnmarshaller;
	private XmlResponseUnmarshaller xmlResponseUnmarshaller;
	private TreeMap<String, UsagePolicy> usagePolicyMap;
	
	public RESTAccessMethod(Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping, WebTarget target, MediaType mediaType,
			String delimiter, TreeMap<String, UsagePolicy> usagePolicyMap) {
		super(attributes, inputs, relation, attributeMapping);
		this.target = target;
		this.mediaType = mediaType;
		this.usagePolicyMap = usagePolicyMap;
		
		// Setup response unmarshallers
		jsonResponseUnmarshaller = new JsonResponseUnmarshaller(attributes, delimiter);
		xmlResponseUnmarshaller = new XmlResponseUnmarshaller(attributes, delimiter);
	}

	@Override
	public void close() {
		
	}

	@Override
	public boolean isClosed() throws Exception {
		return false;
	}
	

	// Perform the main access to the REST protocol and parse the results
	public Table access()
	{
		// Setup a RESTRequestEvent from web target and mediaType
		RESTRequestEvent request = new RESTRequestEvent(target, mediaType);
		
		// For all AccessPreProcessors call the processAccessRequest method with the RESTRequestEvent
		Collection<UsagePolicy> cup = usagePolicyMap.values();
		for(UsagePolicy up : cup)
		{
			if(up instanceof AccessPreProcessor)
			{
				@SuppressWarnings("unchecked")
				AccessPreProcessor<RESTRequestEvent> apep = (AccessPreProcessor<RESTRequestEvent>) up;
				apep.processAccessRequest(request);
			}
		}
		
		// Process the RESTRequestEvent to generate a RESTResponseEvent
		RESTResponseEvent response = request.processRequest();
		
		// For all AccessPostProcessors call the processAccessResponse method with the RESTResponse event
		for(UsagePolicy up : cup)
		{
			if(up instanceof AccessPostProcessor)
			{
				@SuppressWarnings("unchecked")
				AccessPostProcessor<RESTResponseEvent> apop = (AccessPostProcessor<RESTResponseEvent>) up;
				apop.processAccessResponse(response);
			}
		}

		// Create a new table as input for the unmarshallers
		Table table = new Table();
				
		// Process the HTTP response and call response unmarshallers if appropriate
		int status = response.getResponse().getStatus();
		if (status == 200) {
			if(mediaType.getType().equals("application") || mediaType.getType().equals("text"))
			{
				if(mediaType.getSubtype().equals("xml") || mediaType.getSubtype().equals("plain")) return xmlResponseUnmarshaller.unmarshalXml(response.getResponse(), table);
				else if(mediaType.getSubtype().equals("json")) return jsonResponseUnmarshaller.unmarshalJson(response.getResponse(), table); 
			}
		} else if ((status == 400) || (status == 404) || (status == 406)) {
			System.out.println(response.getResponse().getStatusInfo().getReasonPhrase());
		} else {
			throw new AccessException(status
					+ " - " + response.getResponse().getStatusInfo().getReasonPhrase()
					+ "\n" + response.getResponse().readEntity(String.class));
		}
		return new Table();		
	}

	@Override
	protected Stream<Tuple> fetchTuples(Iterator<Tuple> inputTuples) {
		// TODO Auto-generated method stub
		return null;
	}
}