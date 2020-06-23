// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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

/**
 * Minimal example of a webservice that returns json output. Since the input
 * preparation is the same, JsonWebService extends the XmlWebService and
 * overwrites some parts of the result marshaalling.
 * 
 * @author gabor
 *
 */
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

	public JsonWebService(Attribute[] attributes, Set<Attribute> inputAttributes, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(attributes, inputAttributes, relation, attributeMapping);
	}

	public JsonWebService(String name, Attribute[] attributes, Set<Attribute> inputAttributes, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(name, attributes, inputAttributes, relation, attributeMapping);
	}

	@Override
	public void close() {
	}

	@Override
	public boolean isClosed() throws Exception {
		return false;
	}

	/**
	 * Uses com.fasterxml.jackson.databind.ObjectMapper for marshalling json text.
	 * The result from the marshaller will be a map of lists, similar to the xml
	 * marshaller, so the final List of tuples can be processed by the same code.
	 * 
	 * @see uk.ac.ox.cs.pdq.datasources.simplewebservice.XmlWebService#unmarshalXml(javax.ws.rs.core.Response,
	 *      uk.ac.ox.cs.pdq.db.tuple.Tuple)
	 */
	@Override
	public List<Tuple> unmarshalXml(Response response, Tuple inputTuple) throws AccessException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String responseText = response.readEntity(String.class);
			Object data = null;
			try {
				data = mapper.readValue(responseText, List.class);
				System.out.println("Received a list with " + ((List<?>) data).size() + " record(s).");
			} catch (JsonMappingException e) {
				data = mapper.readValue(responseText, Map.class);
				System.out.println("Received a map with " + ((Map<?, ?>) data).size() + " record(s).");
			}

			return this.processItems(data, inputTuple);
		} catch (IOException e) {
			throw new AccessException(e.getMessage(), e);
		}
	}

}
