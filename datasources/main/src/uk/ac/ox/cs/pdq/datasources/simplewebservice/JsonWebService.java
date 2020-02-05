package uk.ac.ox.cs.pdq.datasources.simplewebservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;

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
//	@Override
//	protected Stream<Tuple> fetchTuples(Iterator<Tuple> inputTuples) {
//		//test with https://www.ebi.ac.uk/chembl/api/data/activity.json
//		
//		return null;
//	}
	
	@Override
	public void close() {
	}
	
	@Override
	public boolean isClosed() throws Exception {
		return false;
	}
	
	public List<Tuple> unmarshalXml(Response response,Iterator<Tuple> inputTuples) throws AccessException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String responseText = response.readEntity(String.class);
			Object data = null;
			try {
				data = mapper.readValue(responseText, List.class);
				System.out.println("Received a list with " + ((List)data).size() + " record(s).");
			} catch (JsonMappingException e) {
				data = mapper.readValue(responseText, Map.class);
				System.out.println("Received a map with " + ((Map)data).size() + " record(s).");
			}
			
			return this.processItems(data, inputTuples);
		} catch (IOException e) {
			throw new AccessException(e.getMessage(), e);
		}
	}
	
	
}
