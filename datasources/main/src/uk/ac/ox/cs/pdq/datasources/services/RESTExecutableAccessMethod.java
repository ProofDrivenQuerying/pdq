package uk.ac.ox.cs.pdq.datasources.services;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod.AccessMethodAttribute;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod.AccessMethodRoot;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.InputMethod;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTRequestEvent;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTResponseEvent;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * @author Mark Ridler
 *
 */
// RESTExecutableAccessMethod is the implementation which calls REST for an XML-defined access method
public class RESTExecutableAccessMethod {
	
	private String url;
	private WebTarget target;
	private MediaType mediaType;
	private Attribute[] inputattributes;
	private Attribute[] outputattributes;
	private List<String> resultDelimiter = new LinkedList<String>(); // not supposed to be needed
	private Map<String, Object> urlParams = new HashMap<String, Object>(); // where does this come from??
	private Set<InputMethod> pathMethods = new LinkedHashSet<>(); // where does this come from??
	private Map<String, Object> pathParams = new LinkedHashMap<>(); // where does this come from??
	private RequestMarshaller requestMarshaller = new RequestMarshaller();
	private JsonResponseUnmarshaller jsonResponseUnmarshaller;
	private XmlResponseUnmarshaller xmlResponseUnmarshaller;
	private RESTRequestEvent restRequestEvent;
	private RESTResponseEvent restResponseEvent;
	private RESTRawAccess restRawAccess = new RESTRawAccess();
	
	
	public RESTExecutableAccessMethod(String url, String documentation, String mediaType, AccessMethodRoot amr)
	{
		this.url = url;
		this.mediaType = new MediaType("application", "json");
		if((mediaType != null) && mediaType.equals("application/xml"))	this.mediaType = new MediaType("application", "xml");
		LinkedList<Attribute> inputs = new LinkedList<Attribute>();
		LinkedList<Attribute> outputs = new LinkedList<Attribute>();
		StringBuilder uri = new StringBuilder(this.url);
		for(AccessMethodAttribute aa : amr.getAttributes())
		{
			if((aa.getInput() != null) && aa.getInput().equals("true"))
			{
				inputs.add(Attribute.create(typeType(aa.getType()), aa.getName()));
				if(aa.getEncoding() != null)
				{
					if(aa.getEncoding().equals("path-element"))
					{
						uri.append("/" + aa.getValue());
					}
				}
			}
			if((aa.getOutput() != null) && aa.getOutput().equals("true"))
			{
				outputs.add(Attribute.create(typeType(aa.getType()), aa.getName()));
			}
		}
		this.target = ClientBuilder.newClient().register(JacksonFeatures.class).target(uri.toString());
		for(AccessMethodAttribute aa : amr.getAttributes())
		{
			if((aa.getInput() != null) && aa.getInput().equals("true"))
			{
				if(aa.getEncoding() != null)
				{
					if(aa.getEncoding().equals("url-param"))
					{
						this.target = target.queryParam(aa.getName(), aa.getValue());
					}
				}
			}
		}
		
		System.out.println(this.target.toString());

		inputattributes = new Attribute[inputs.size()];
		for(int i = 0; i < inputs.size(); i++) inputattributes[i] = inputs.get(i);
		outputattributes = new Attribute[outputs.size()];
		for(int i = 0; i < outputs.size(); i++) outputattributes[i] = outputs.get(i);
		
		jsonResponseUnmarshaller = new JsonResponseUnmarshaller(outputattributes);
		xmlResponseUnmarshaller = new XmlResponseUnmarshaller(outputattributes);
	}
	public Type typeType(String type)
	{
		if(type.equals("String"))
		{
			return String.class;
		}
		else if(type.equals("Integer"))
		{
			return Integer.class;
		}
		else if(type.equals("Double"))
		{
			return Double.class;
		}
		return null;
	}
	public Table access(Tuple input)
	{
		requestMarshaller.marshalInputs(target, input);
		
		//restRequestEvent = new RESTRequestEvent();
		
		Response response = restRawAccess.access(target, mediaType);
		//restResponseEvent = new RESTResponseEvent();
		
		Table table = new Table();
		table.appendRow(input);
				
		if(mediaType.getType().equals("application"))
		{
			if(mediaType.getSubtype().equals("xml")) return xmlResponseUnmarshaller.unmarshalXml(response, table);
			else if(mediaType.getSubtype().equals("json")) return jsonResponseUnmarshaller.unmarshalJson(response, table); 
		}
		return new Table();		
	}
}
