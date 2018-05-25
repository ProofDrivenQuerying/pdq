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
import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.ServiceUsagePolicy;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.StaticAttribute;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.AttributeEncoding;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.GroupUsagePolicy;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.ServiceGroupsRoot;
import uk.ac.ox.cs.pdq.datasources.services.policies.PolicyFactory;
import uk.ac.ox.cs.pdq.datasources.services.policies.URLAuthentication;
import uk.ac.ox.cs.pdq.datasources.services.policies.UsagePolicy;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.io.ReaderException;
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
	private TreeMap<String, AttributeEncoding> attributeEncodingMap = new TreeMap<String, AttributeEncoding>();
	private TreeMap<String, UsagePolicy> usagePolicyMap = new TreeMap<String, UsagePolicy>();

	// Constructor takes XML-derived objects and builds a structure ready to run
	public RESTExecutableAccessMethod(ServiceGroupsRoot sgr, ServiceRoot sr, AccessMethod am, Tuple tuple)
	{
		this.url = sr.getUrl();
		this.mediaType = new MediaType("application", "json");
		String mediaType = sr.getMediaType();
		if((mediaType != null) && mediaType.equals("application/xml"))	this.mediaType = new MediaType("application", "xml");
		for(AttributeEncoding ae: sgr.getAttributeEncoding()) attributeEncodingMap.put(ae.getName(), ae);
		compileUsagePolicies(sgr);
		formatTemplate(sgr, sr, am);
		LinkedList<Attribute> inputs = new LinkedList<Attribute>();
		LinkedList<Attribute> outputs = new LinkedList<Attribute>();
		StringBuilder uri = new StringBuilder(this.url);
		Map<String, Object> params = new TreeMap<String, Object>();
		mapAttributesPhase1(sr, am, inputs, outputs, uri, params, tuple);
		if(this.template != null) uri.append(this.template);
		this.target = ClientBuilder.newClient().register(JacksonFeatures.class).target(uri.toString());
		mapAttributesPhase2(sr, am, tuple);
		
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
	
	// Put all usage polcies in a map for future reference
	@SuppressWarnings("unchecked")
	public void compileUsagePolicies(ServiceGroupsRoot sgr)
	{
		for(GroupUsagePolicy gup : sgr.getUsagePolicy())
		{
			if(gup.getName() != null)
			{
				UsagePolicy up = usagePolicyMap.get(gup.getName());
				if(up != null)
				{
					try
					{
						throw new ReaderException("Duplicate usage policy '" + gup.getName() + "'");
					}
					catch(ReaderException e)
					{
						System.out.println(e.toString());
					}
				}
				else if(gup.getType() != null)
				{
					try
					{
						String className = gup.getType();
						Class<UsagePolicy> cl = (Class<UsagePolicy>) Class.forName(className);
						if(cl != null)
						{
							usagePolicyMap.put(gup.getName(), PolicyFactory.getInstance(cl, gup));
						}
					}
					catch(ClassNotFoundException e)
					{
						System.out.println(e.toString());
					}
				}
			}
		}
	}
	
	// Format a list of templates as presented by the AttributeEncodings
	private void formatTemplate(ServiceGroupsRoot sgr, ServiceRoot sr, AccessMethod am)
	{
		String result = "";
		TreeMap<AttributeEncoding, String> attributeEncodingMap2 = new TreeMap<AttributeEncoding, String>();
		if(sr.getServiceUsagePolicy() != null)
		{
			for(ServiceUsagePolicy sup: sr.getServiceUsagePolicy())
			{
				if(sup.getName() != null)
				{
					UsagePolicy up = usagePolicyMap.get(sup.getName());
					if(up != null)
					{
						if(up instanceof uk.ac.ox.cs.pdq.datasources.services.policies.URLAuthentication)
						{
							URLAuthentication uae = (URLAuthentication) up;
							String encoding = uae.getAttributeEncoding();
							String index = "0";
							formatTemplateProcessParams(encoding, index, null, attributeEncodingMap2);
						}
					}
				}
			}
		}
		if(sr.getStaticAttribute() != null)
		{
			for(StaticAttribute sa: sr.getStaticAttribute())
			{
				String encoding = sa.getAttributeEncoding();
				String index = sa.getAttributeEncodingIndex();
				formatTemplateProcessParams(encoding, index, sa.getValue(), attributeEncodingMap2);
			}
		}
		if(am.getAttributes() != null)
		{
			for(AccessMethodAttribute aa: am.getAttributes())
			{
				String encoding = aa.getAttributeEncoding();
				String index = aa.getAttributeEncodingIndex();	
				formatTemplateProcessParams(encoding, index, aa.getValue(), attributeEncodingMap2);		
			}
		}
		Collection<String> cs = attributeEncodingMap2.values();
		for(String s: cs) result += s;
		this.template = result;
	}

	// Do the donkey work for formatTemplate()
	public void formatTemplateProcessParams(String encoding, String index, String value, TreeMap<AttributeEncoding, String> attributeEncodingMap2)
	{
		if(encoding != null)
		{
			AttributeEncoding ae;
			if((ae = attributeEncodingMap.get(encoding)) != null)
			{
				String template;
				if((template = attributeEncodingMap2.get(ae)) != null)
				{
					if(index != null)
					{
						template = template.replace("{" + index + "}", value);
						attributeEncodingMap2.remove(ae);
						attributeEncodingMap2.put(ae, template);
					}
				}
				else
				{
					if((template = ae.getTemplate()) != null)
					{
						if(index != null)
						{
							template = template.replace("{" + index + "}", value);
						}
						attributeEncodingMap2.put(ae, template);
					}
				}
			}
		}
	}

	// Phase 1 builds structures and processes path-elements
	private void mapAttributesPhase1(ServiceRoot sr, AccessMethod am, List<Attribute> inputs, List<Attribute> outputs, StringBuilder uri, Map<String, Object> params, Tuple tuple)
	{
		if(sr.getStaticAttribute() != null)
		{
			for(StaticAttribute sa : sr.getStaticAttribute())
			{
				mapAttributesPhase1ProcessParams(sa.getAttributeEncoding(), sa.getName(), sa.getType(), sa.getValue(), inputs, uri, params, null);
			}
		}
		if(am.getAttributes() != null)
		{
			int a = 0;
			for(AccessMethodAttribute aa : am.getAttributes())
			{
				if((aa.getInput() != null) && aa.getInput().equals("true"))
				{
					mapAttributesPhase1ProcessParams(aa.getAttributeEncoding(), aa.getName(), aa.getType(), aa.getValue(), inputs, uri, params, (tuple != null) ? tuple.getValue(a) : null);
					a++;
				}
				if((aa.getOutput() != null) && aa.getOutput().equals("true"))
				{
					outputs.add(Attribute.create(typeType(aa.getType()), aa.getName()));
				}
			}
		}
	}
	
	// Do the donkey work for mapAttributesPhase1()
	public void mapAttributesPhase1ProcessParams(String encoding, String name, String type, String value, List<Attribute> inputs, StringBuilder uri, Map<String, Object> params, String tuplevalue)
	{
		if((name != null) && (type != null))
		{
			inputs.add(Attribute.create(typeType(type), name));
			if(encoding != null)
			{
				AttributeEncoding ae = attributeEncodingMap.get(encoding);
				if(ae != null)
				{
					if((ae.getType() != null) && ae.getType().equals("path-element"))
					{
						if(tuplevalue != null) value = tuplevalue;
						if(value != null)
						{
							if(this.template == null)
							{
								uri.append("/" + value);
							}
							else
							{
								params.put(name, value);
							}
						}
					}
				}
			}
		}
	}
	
	// Phase 2 processes the name/value pairs, adding them onto the web target
	private void mapAttributesPhase2(ServiceRoot sr, AccessMethod am, Tuple tuple)
	{
		if(sr.getServiceUsagePolicy() != null)
		{
			for(ServiceUsagePolicy sup: sr.getServiceUsagePolicy())
			{
				if(sup.getName() != null)
				{
					UsagePolicy up = usagePolicyMap.get(sup.getName());
					if(up != null)
					{
						if(up instanceof uk.ac.ox.cs.pdq.datasources.services.policies.URLAuthentication)
						{
							URLAuthentication uae = (URLAuthentication) up;
							String encoding = uae.getAttributeEncoding();
							mapAttributesPhase2ProcessParams(encoding, sup.getName(), null, null);
						}
					}
				}
			}
		}
		if(sr.getStaticAttribute() != null)
		{
			for(StaticAttribute sa : sr.getStaticAttribute())
			{
				mapAttributesPhase2ProcessParams(sa.getAttributeEncoding(), sa.getName(), sa.getValue(), null);
			}
		}
		if(am.getAttributes() != null)
		{
			int a = 0;
			for(AccessMethodAttribute aa : am.getAttributes())
			{
				if((aa.getInput() != null) && aa.getInput().equals("true"))
				{
					mapAttributesPhase2ProcessParams(aa.getAttributeEncoding(), aa.getName(), aa.getValue(), tuple.getValue(a));
				}
			}
		}
	}
	
	// Do the donkey work for mapAttributesPhase2()
	public void mapAttributesPhase2ProcessParams(String encoding, String name, String value, String tuplevalue)
	{
		if(encoding != null)
		{
			AttributeEncoding ae;
			if((ae = attributeEncodingMap.get(encoding)) != null)
			{
				if((ae.getType() != null) && ae.getType().equals("url-param"))
				{
					if(name != null)
					{
						if(tuplevalue != null)
						{
							this.target = target.queryParam(name, tuplevalue);
						}
						else if(value != null)
						{
							this.target = target.queryParam(name, value);
						}
						else if(ae.getValue() != null)
						{
							this.target = target.queryParam(name, ae.getValue());								
						}
					}
					else if(ae.getName() != null)
					{
						if(tuplevalue != null)
						{
							this.target = target.queryParam(ae.getName(), tuplevalue);
						}
						else if(value != null)
						{
							this.target = target.queryParam(ae.getName(), value);
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

	// Perform the main access to the REST protocol and parse the results
	public Table access()
	{
		RESTRequestEvent request = new RESTRequestEvent(target, mediaType);
		
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
		
		RESTResponseEvent response = request.processRequest();
		
		for(UsagePolicy up : cup)
		{
			if(up instanceof AccessPostProcessor)
			{
				@SuppressWarnings("unchecked")
				AccessPostProcessor<RESTResponseEvent> apop = (AccessPostProcessor<RESTResponseEvent>) up;
				apop.processAccessResponse(response);
			}
		}

		Table table = new Table();
				
		int status = response.getResponse().getStatus();
		if (status == 200) {
			if(mediaType.getType().equals("application"))
			{
				if(mediaType.getSubtype().equals("xml")) return xmlResponseUnmarshaller.unmarshalXml(response.getResponse(), table);
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
}
