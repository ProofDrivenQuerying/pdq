package uk.ac.ox.cs.pdq.datasources.services;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import uk.ac.ox.cs.pdq.datasources.services.service.RESTExecutableAccessMethodAttributeSpecification;
import uk.ac.ox.cs.pdq.datasources.services.service.RESTExecutableAccessMethodSpecification;
import uk.ac.ox.cs.pdq.datasources.services.service.Service;
import uk.ac.ox.cs.pdq.datasources.services.servicegroup.ServiceGroup;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;

/**
 * @author Mark Ridler
 *
 */
// RESTExecutableAccessMethod is the implementation which calls REST for a defined access method
// It provides functionality to support the constructor, which does much of the processing, followed
// by the access method, where the REST call and associated events take place.
public class RESTAccessMethodGenerator {
	
	private WebTarget target;
	private MediaType mediaType;
	private RESTAccessMethod[] restAccessMethods;

	// Constructor takes XML-derived objects and builds a structure ready to run
	public RESTAccessMethodGenerator(ServiceGroup sgr, Service sr, RESTExecutableAccessMethodSpecification am)
	{
		// Get the url and mediatype from the ServiceRoot object
		this.mediaType = new MediaType("application", "json");
		String mediaType = sr.getMediaType();
		if((mediaType != null) && mediaType.equals("application/xml"))	this.mediaType = new MediaType("application", "xml");
		if((mediaType != null) && mediaType.equals("text/plain"))	this.mediaType = new MediaType("text", "plain");
		
		
		// Setup executable access method
		RESTExecutableAccessMethodSpecification[] reamss = sr.getAccessMethod();
		restAccessMethods = new RESTAccessMethod[reamss.length];
		for(int i = 0; i < reamss.length; i++)
		{
			RESTExecutableAccessMethodSpecification reams = reamss[i];
			RESTExecutableAccessMethodAttributeSpecification[] attrspecs = reams.getAttributes();
			Attribute[] accessMethodAttributes = new Attribute[attrspecs.length];
			List<Attribute> relationAttributes = new ArrayList<>();
			Integer[] integerinputs = new Integer[attrspecs.length];
			Map<Attribute, Attribute> map = new HashMap<Attribute, Attribute>();
			for(int j = 0; j < accessMethodAttributes.length; j++)
			{
				RESTExecutableAccessMethodAttributeSpecification attrspec = attrspecs[j];
				accessMethodAttributes[j] = Attribute.create(typeType(attrspec.getType()), attrspec.getName());
				if((attrspec.getInput() != null) && (attrspec.getInput().equals("true")))
				{
					integerinputs[j] = new Integer(j);
				}
				else
				{
					integerinputs[j] = new Integer(-1);
				}
				if (attrspec.getRelationAttribute()!=null) {
					Attribute a = Attribute.create(typeType(attrspec.getType()), (attrspec.getRelationAttribute() == null) ? "" : attrspec.getRelationAttribute());
					relationAttributes.add(a);
					map.put(accessMethodAttributes[j], a);
				}
			}
			Relation relation = Relation.create(reams.getName(), relationAttributes.toArray(new Attribute[relationAttributes.size()]));
			restAccessMethods[i] = new RESTAccessMethod(accessMethodAttributes, eliminateMinus1(integerinputs), relation, map, this.target, this.mediaType, sgr, sr, am);
		}
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
		else if(type.equals("Boolean"))
		{
			return Boolean.class;
		}
		return null;
	}
	
	// Reformat the list of integers so it doesn't contain -1
	private Integer[] eliminateMinus1(Integer[] integerinputs)
	{
		LinkedList<Integer> list = new LinkedList<>();
		for(int i = 0; i < integerinputs.length; i++)
		{
			if(integerinputs[i] != null)
			{
				if(integerinputs[i].intValue() >= 0) list.add(integerinputs[i]);
			}
		}
		Integer[] result = new Integer[list.size()];
		for(int j = 0; j < list.size(); j++)
		{
			result[j] = list.get(j);
		}
		return result;
	}
	
	public RESTAccessMethod[] getRestAccessMethods()
	{
		return restAccessMethods;
	}
}
