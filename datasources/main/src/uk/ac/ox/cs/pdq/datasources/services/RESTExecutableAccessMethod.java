package uk.ac.ox.cs.pdq.datasources.services;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.AccessMethod;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.AccessMethodAttribute;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.ServiceRoot;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.StaticAttribute;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.AttributeEncoding;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.ServiceGroupsRoot;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * @author Mark Ridler
 *
 */
// RESTExecutableAccessMethod is the implementation which calls REST for an XML-defined access method
public class RESTExecutableAccessMethod {
	
	private String url;
	private WebTarget target;
	private MediaType mediaType;
	private String template;
	private Attribute[] inputattributes;
	private Attribute[] outputattributes;
	private JsonResponseUnmarshaller jsonResponseUnmarshaller;
	private XmlResponseUnmarshaller xmlResponseUnmarshaller;
	private RESTRawAccess restRawAccess = new RESTRawAccess();
	private TreeMap<String, AttributeEncoding> map1 = new TreeMap<String, AttributeEncoding>();

	// Constructor takes XML-derived objects and builds a structure ready to run
	public RESTExecutableAccessMethod(ServiceGroupsRoot sgr, ServiceRoot sr, AccessMethod am)
	{
		this.url = sr.getUrl();
		this.mediaType = new MediaType("application", "json");
		String mediaType = sr.getMediaType();
		if((mediaType != null) && mediaType.equals("application/xml"))	this.mediaType = new MediaType("application", "xml");
		for(AttributeEncoding ae: sgr.getAttributeEncoding()) map1.put(ae.getName(), ae);
		formatTemplate(sgr, sr, am);
		LinkedList<Attribute> inputs = new LinkedList<Attribute>();
		LinkedList<Attribute> outputs = new LinkedList<Attribute>();
		StringBuilder uri = new StringBuilder(this.url);
		Map<String, Object> params = new TreeMap<String, Object>();
		mapAttributesPhase1(sr, am, inputs, outputs, uri, params);
		if(this.template != null) uri.append(this.template);
		this.target = ClientBuilder.newClient().register(JacksonFeatures.class).target(uri.toString());
		mapAttributesPhase2(sr, am);
		
		System.out.println(this.target.toString());

		inputattributes = new Attribute[inputs.size()];
		for(int i = 0; i < inputs.size(); i++) inputattributes[i] = inputs.get(i);
		outputattributes = new Attribute[outputs.size()];
		for(int i = 0; i < outputs.size(); i++) outputattributes[i] = outputs.get(i);
		
		jsonResponseUnmarshaller = new JsonResponseUnmarshaller(outputattributes);
		xmlResponseUnmarshaller = new XmlResponseUnmarshaller(outputattributes);
	}
	
	// Conversion from string to type ... there may be a better way of doing this
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
	
	// Format a list of templates as presented by the AttributeEncodings
	public void formatTemplate(ServiceGroupsRoot sgr, ServiceRoot sr, AccessMethod am)
	{
		String result = "";
		TreeMap<AttributeEncoding, String> map2 = new TreeMap<AttributeEncoding, String>();
		for(AccessMethodAttribute aa: am.getAttributes())
		{
			String encoding = aa.getAttributeEncoding();
			String index = aa.getAttributeEncodingIndex();	
			if(encoding != null)
			{
				AttributeEncoding ae;
				if((ae = map1.get(encoding)) != null)
				{
					String template;
					if((template = map2.get(ae)) != null)
					{
						if(index != null)
						{
							template = template.replace("{" + index + "}", index);
						}
					}
					else
					{
						if((template = ae.getTemplate()) != null)
						{
							if(index != null)
							{
								template = template.replace("{" + index + "}", index);
							}
							map2.put(ae, template);
						}
					}
				}
			}
		}
		Collection<String> cs = map2.values();
		for(String s: cs) result += s;
		this.template = result;
	}

	// Phase 1 builds structures and processes path-elements
	public void mapAttributesPhase1(ServiceRoot sr, AccessMethod am, List<Attribute> inputs, List<Attribute> outputs, StringBuilder uri, Map<String, Object> params)
	{
		for(StaticAttribute sa : sr.getStaticAttribute())
		{
			if(sa.getAttributeEncoding() != null)
			{
				AttributeEncoding ae = map1.get(sa.getAttributeEncoding());
				if(ae != null)
				{
					if((sa.getName() != null) && (sa.getType() != null))
					{
						inputs.add(Attribute.create(typeType(sa.getType()), sa.getName()));
						if((ae.getType() != null) && ae.getType().equals("path-element"))
						{
							if(sa.getValue() != null)
							{
								if(this.template == null)
								{
									uri.append("/" + sa.getValue());
								}
								else
								{
									params.put(sa.getName(), sa.getValue());
								}
							}
						}
					}
				}
			}
		}
		for(AccessMethodAttribute aa : am.getAttributes())
		{
			if((aa.getInput() != null) && aa.getInput().equals("true"))
			{
				if((aa.getName() != null) && (aa.getType() != null))
				{
					inputs.add(Attribute.create(typeType(aa.getType()), aa.getName()));
					if(aa.getAttributeEncoding() != null)
					{
						AttributeEncoding ae;
						if((ae = map1.get(aa.getAttributeEncoding())) != null)
						{
							if((ae.getType() != null) && (ae.getType().equals("path-element")))
							{
								if(aa.getValue() != null)
								{
									if(this.template == null)
									{
										uri.append("/" + aa.getValue());
									}
									else
									{
										params.put(aa.getName(), aa.getValue()); // TODO: input tuple
									}
								}
							}
						}
					}
				}
			}
			if((aa.getOutput() != null) && aa.getOutput().equals("true"))
			{
				outputs.add(Attribute.create(typeType(aa.getType()), aa.getName()));
			}
		}
	}
	
	// Phase 2 processes the name/value pairs, adding them onto the web target
	public void mapAttributesPhase2(ServiceRoot sr, AccessMethod am)
	{
		for(StaticAttribute sa : sr.getStaticAttribute())
		{
			if(sa.getAttributeEncoding() != null)
			{
				AttributeEncoding ae;
				if((ae = map1.get(sa.getAttributeEncoding())) != null)
				{
					if((ae.getType() != null) && ae.getType().equals("url-index"))
					{
						if((sa.getName() != null) && (sa.getValue() != null))
						{
							this.target = target.queryParam(sa.getName(), sa.getValue());
						}
					}
				}
			}
		}
		for(AccessMethodAttribute aa : am.getAttributes())
		{
			if((aa.getInput() != null) && aa.getInput().equals("true"))
			{
				if(aa.getAttributeEncoding() != null)
				{
					AttributeEncoding ae;
					if((ae = map1.get(aa.getAttributeEncoding())) != null)
					{
						if((ae.getType() != null) && ae.getType().equals("url-index"))
						{
							if((aa.getName() != null) && (aa.getValue() != null))
							{
								this.target = target.queryParam(aa.getName(), aa.getValue()); // TODO: input tuple
							}
						}
					}
				}
			}
		}
	}
	
	// Perform the main access to the REST protocol and parse the results
	public Table access()
	{
		Response response = restRawAccess.access(target, mediaType);
		
		Table table = new Table();
				
		if(mediaType.getType().equals("application"))
		{
			if(mediaType.getSubtype().equals("xml")) return xmlResponseUnmarshaller.unmarshalXml(response, table);
			else if(mediaType.getSubtype().equals("json")) return jsonResponseUnmarshaller.unmarshalJson(response, table); 
		}
		return new Table();		
	}
	
}
