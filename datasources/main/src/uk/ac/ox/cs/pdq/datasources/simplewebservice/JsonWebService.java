package uk.ac.ox.cs.pdq.datasources.simplewebservice;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;

public class JsonWebService extends XmlWebService {

	private static final long serialVersionUID = 5268175711548627539L;

	public JsonWebService(Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(attributes, inputs, relation, attributeMapping);
	}

	public JsonWebService(String name, Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(name, attributes, inputs, relation, attributeMapping);
	}

	public JsonWebService(Attribute[] attributes, Set<Attribute> inputAttributes,
			Relation relation, Map<Attribute, Attribute> attributeMapping) {
		super(attributes, inputAttributes, relation, attributeMapping);
	}

	public JsonWebService(String name, Attribute[] attributes, Set<Attribute> inputAttributes,
			Relation relation, Map<Attribute, Attribute> attributeMapping) {
		super(name, attributes, inputAttributes, relation, attributeMapping);
	}
	
	@Override
	public void close() {
	}
	
	@Override
	public boolean isClosed() throws Exception {
		return false;
	}
	
	@Override
	public List<Tuple> unmarshalXml(Response response,Tuple inputTuple) throws AccessException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String responseText = response.readEntity(String.class);
			Object data = null;
			try {
				data = mapper.readValue(responseText, List.class);
				System.out.println("Received a list with " + ((List<?>)data).size() + " record(s).");
			} catch (JsonMappingException e) {
				data = mapper.readValue(responseText, Map.class);
				System.out.println("Received a map with " + ((Map<?,?>)data).size() + " record(s).");
			}
			
			return this.processItems(data, inputTuple);
		} catch (IOException e) {
			throw new AccessException(e.getMessage(), e);
		}
	}
	
	
}
