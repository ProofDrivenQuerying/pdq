package uk.ac.ox.cs.pdq.datasources.services;

import java.lang.reflect.Type;
import java.util.LinkedList;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod.AccessMethodAttribute;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod.AccessMethodRest;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod.AccessMethodRoot;
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
	private RequestMarshaller requestMarshaller = new RequestMarshaller();
	private JsonResponseUnmarshaller jsonResponseUnmarshaller;
	private XmlResponseUnmarshaller xmlResponseUnmarshaller;
	private RESTRawAccess restRawAccess = new RESTRawAccess();
	
	
	public RESTExecutableAccessMethod(AccessMethodRoot amr)
	{
		AccessMethodRest rest = amr.getRest();
		this.url = rest.getUrl();
		this.mediaType = new MediaType("application", "json");
		String mediaType = rest.getMediaType();
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
