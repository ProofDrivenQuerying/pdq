package uk.ac.ox.cs.pdq.datasources.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;

/**
 * @author Mark Ridler
 *
 */
// XmlResponseUnmarshaller deals with a web response in Xml format
public class XmlResponseUnmarshaller extends ResponseUnmarshaller {
	
	
	XmlResponseUnmarshaller(Attribute[] attributes, String resultDelimiter){
		this.attributes = attributes;
		this.resultDelimiter.add(resultDelimiter);
	}
	
	@SuppressWarnings("rawtypes")
	public Table  unmarshalXml(Response response, Table inputs) throws AccessException {
		Table result = new Table(this.attributes);

		XmlMapper mapper = new XmlMapper();
		try {
			String s = response.readEntity(String.class);
			List<String> delim = this.resultDelimiter;
			List<?> l = mapper.readValue(s, List.class);
			int start = delim.size();
			Object r = l; 
			if (!l.isEmpty()) {
				Object p = l.get(0);
				if (p instanceof Map) {
					Map m = ((Map) p);
					if (m.size() == 1) {
						Object k = m.keySet().iterator().next();
						int i = delim.indexOf(k);
						if (i >= 0) {
							start = i;
						}
					}
				} else {
					r =  mapper.readValue(s, Map.class);
				}
			}
			for (Tuple t: this.processItems(
					delim.subList(start, delim.size()), r, result.getType(), inputs)) {
				result.appendRow(t);
			}
			return result;
		} catch (IOException e) {
			throw new AccessException(e.getMessage(), e);
		}
	}
}