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

import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.AccessMethod;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.AccessMethodAttribute;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.ServiceRoot;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.StaticAttribute;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.AttributeEncoding;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.ServiceGroupsRoot;
import uk.ac.ox.cs.pdq.datasources.services.policies.UsagePolicy;
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
	private String template;
	private Attribute[] inputattributes;
	private Attribute[] outputattributes;
	private JsonResponseUnmarshaller jsonResponseUnmarshaller;
	private XmlResponseUnmarshaller xmlResponseUnmarshaller;
	private TreeMap<String, AttributeEncoding> attributeEncodingMap1 = new TreeMap<String, AttributeEncoding>();
	private TreeMap<String, UsagePolicy> usagePolicyMap = new TreeMap<String, UsagePolicy>();

	// Constructor takes XML-derived objects and builds a structure ready to run
	public RESTExecutableAccessMethod(ServiceGroupsRoot sgr, ServiceRoot sr, AccessMethod am)
	{
		this.url = sr.getUrl();
		this.mediaType = new MediaType("application", "json");
		String mediaType = sr.getMediaType();
		if((mediaType != null) && mediaType.equals("application/xml"))	this.mediaType = new MediaType("application", "xml");
		for(AttributeEncoding ae: sgr.getAttributeEncoding()) attributeEncodingMap1.put(ae.getName(), ae);
		formatTemplate(sgr, sr, am);
		LinkedList<Attribute> inputs = new LinkedList<Attribute>();
		LinkedList<Attribute> outputs = new LinkedList<Attribute>();
		StringBuilder uri = new StringBuilder(this.url);
		Map<String, Object> params = new TreeMap<String, Object>();
		mapAttributesPhase1(sr, am, inputs, outputs, uri, params);
		if(this.template != null) uri.append(this.template);
		this.target = ClientBuilder.newClient().register(JacksonFeatures.class).target(uri.toString());
		mapAttributesPhase2(sr, am);
		
		inputattributes = new Attribute[inputs.size()];
		for(int i = 0; i < inputs.size(); i++) inputattributes[i] = inputs.get(i);
		outputattributes = new Attribute[outputs.size()];
		for(int i = 0; i < outputs.size(); i++) outputattributes[i] = outputs.get(i);
		
		jsonResponseUnmarshaller = new JsonResponseUnmarshaller(outputattributes);
		xmlResponseUnmarshaller = new XmlResponseUnmarshaller(outputattributes);
	}
	
	// Conversion from string to type ... there may be a better way of doing this
	private Type typeType(String type)
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
	
/*	@SuppressWarnings("unchecked")
	private void usagePolicies(ServiceGroupsRoot sgr)
	{
		for(GroupUsagePolicy gup : sgr.getUsagePolicy())
		{
			if(gup.getName() != null)
			{
				UsagePolicy up = usagePolicyMap.get(gup.getName());
				if(up != null)
				{
					throw new ReaderException("Duplicate usage policy '" + gup.getName() + "'");					
				}
				else if(gup.getType() != null)
				{
					String className = gup.getType();
					Class<UsagePolicy> cl = (Class<UsagePolicy>) Class.forName(className);
					if(cl != null)
					{
						usagePolicyMap.put(gup.getName(), PolicyFactory.getInstance(cl, prop));
					}
				}
			}
		}
	}*/
	
	// Format a list of templates as presented by the AttributeEncodings
	private void formatTemplate(ServiceGroupsRoot sgr, ServiceRoot sr, AccessMethod am)
	{
		String result = "";
		TreeMap<AttributeEncoding, String> map2 = new TreeMap<AttributeEncoding, String>();
		for(StaticAttribute sa: sr.getStaticAttribute())
		{
			String encoding = sa.getAttributeEncoding();
			String index = sa.getAttributeEncodingIndex();	
			if(encoding != null)
			{
				AttributeEncoding ae;
				if((ae = attributeEncodingMap1.get(encoding)) != null)
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
		for(AccessMethodAttribute aa: am.getAttributes())
		{
			String encoding = aa.getAttributeEncoding();
			String index = aa.getAttributeEncodingIndex();	
			if(encoding != null)
			{
				AttributeEncoding ae;
				if((ae = attributeEncodingMap1.get(encoding)) != null)
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
	private void mapAttributesPhase1(ServiceRoot sr, AccessMethod am, List<Attribute> inputs, List<Attribute> outputs, StringBuilder uri, Map<String, Object> params)
	{
		for(StaticAttribute sa : sr.getStaticAttribute())
		{
			if(sa.getAttributeEncoding() != null)
			{
				AttributeEncoding ae = attributeEncodingMap1.get(sa.getAttributeEncoding());
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
						if((ae = attributeEncodingMap1.get(aa.getAttributeEncoding())) != null)
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
	private void mapAttributesPhase2(ServiceRoot sr, AccessMethod am)
	{
		for(StaticAttribute sa : sr.getStaticAttribute())
		{
			if(sa.getAttributeEncoding() != null)
			{
				AttributeEncoding ae;
				if((ae = attributeEncodingMap1.get(sa.getAttributeEncoding())) != null)
				{
					if((ae.getType() != null) && ae.getType().equals("url-param"))
					{
						if(sa.getName() != null)
						{
							if(sa.getValue() != null)
							{
								this.target = target.queryParam(sa.getName(), sa.getValue());
							}
							else if(ae.getValue() != null)
							{
								this.target = target.queryParam(sa.getName(), ae.getValue());								
							}
						}
						else if(ae.getName() != null)
						{
							if(sa.getValue() != null)
							{
								this.target = target.queryParam(ae.getName(), sa.getValue());
							}
							else if(ae.getValue() != null)
							{
								this.target = target.queryParam(ae.getName(), ae.getValue());								
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
				if(aa.getAttributeEncoding() != null)
				{
					AttributeEncoding ae;
					if((ae = attributeEncodingMap1.get(aa.getAttributeEncoding())) != null)
					{
						if((ae.getType() != null) && ae.getType().equals("url-param"))
						{
							if(aa.getName() != null)
							{
								// TODO: input tuple
								if(aa.getValue() != null)
								{
									this.target = target.queryParam(aa.getName(), aa.getValue()); 
								}
								else if(ae.getValue() != null)
								{
									this.target = target.queryParam(aa.getName(), ae.getValue()); 
								}
							}
							else if(ae.getName() != null)
							{
								// TODO: input tuple
								if(aa.getValue() != null)
								{
									this.target = target.queryParam(ae.getName(), aa.getValue()); 
								}
								else if(ae.getValue() != null)
								{
									this.target = target.queryParam(ae.getName(), ae.getValue()); 
								}
							}
						}
					}
				}
			}
		}
	}
	
	// Perform the main access to the REST protocol and parse the results
	public Table access(Tuple input)
	{
		RESTRequestEvent request = new RESTRequestEvent(target, mediaType);
		
		RESTResponseEvent response = request.processRequest();
		
		Table table = new Table();
				
		int status = response.getResponse().getStatus();
		if (status == 200) {
			if(mediaType.getType().equals("application"))
			{
				if(mediaType.getSubtype().equals("xml")) return xmlResponseUnmarshaller.unmarshalXml(response.getResponse(), table);
				else if(mediaType.getSubtype().equals("json")) return jsonResponseUnmarshaller.unmarshalJson(response.getResponse(), table); 
			}
		} else if ((status == 404) || (status == 400)) {
			System.out.println(response.getResponse().getStatusInfo().getReasonPhrase());
		} else {
			throw new AccessException(status
					+ " - " + response.getResponse().getStatusInfo().getReasonPhrase()
					+ "\n" + response.getResponse().readEntity(String.class));
		}
		return new Table();		
	}
}
