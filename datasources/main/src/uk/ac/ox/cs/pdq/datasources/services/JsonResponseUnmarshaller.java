package uk.ac.ox.cs.pdq.datasources.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.OutputMethod;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTAttribute;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Mark Ridler
 *
 */
// JsonResponseUnmarshaller deals with a web response in Json format
public class JsonResponseUnmarshaller extends ResponseUnmarshaller {
	
	private Attribute[] attributes;
	private List<String> resultDelimiter = new LinkedList<String>(); // not supposed to be needed	

	JsonResponseUnmarshaller(Attribute[] attributes){
		this.attributes = attributes;
	}
	
	Table unmarshalJson(Response response, Table inputs) throws AccessException {
		Table result = new Table(this.attributes);

		ObjectMapper mapper = new ObjectMapper();
		try {
			String s = response.readEntity(String.class);
			List<String> delim = this.resultDelimiter;

			Object r = null;
			if (delim.isEmpty()) {
				try {
					r = mapper.readValue(s, List.class);
				} catch (JsonMappingException e) {
					r = mapper.readValue(s, Map.class);
				}
			} else {
				r = mapper.readValue(s, Map.class);
			}

			for (Tuple t: this.processItems(
					delim, r, result.getType(), inputs)) {
				result.appendRow(t);
			}
			return result;
		} catch (IOException e) {
			throw new AccessException(e.getMessage(), e);
		}
	}
}
