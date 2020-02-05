package uk.ac.ox.cs.pdq.datasources.simplewebservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.services.RESTResponseEvent;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;
import uk.ac.ox.cs.pdq.util.Utility;

public class XmlWebService extends ExecutableAccessMethod {
	private String url = null;

	private WebTarget target;

	private MediaType mediaType;
	
	private static final long serialVersionUID = 5268175711548627539L;

	private static final List<String> resultDelimiter = new ArrayList<>();
	

	public XmlWebService(Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(attributes, inputs, relation, attributeMapping);
	}

	public XmlWebService(String name, Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(name, attributes, inputs, relation, attributeMapping);
	}

	public XmlWebService(Attribute[] attributes, Set<Attribute> inputAttributes,
			Relation relation, Map<Attribute, Attribute> attributeMapping) {
		super(attributes, inputAttributes, relation, attributeMapping);
	}

	public XmlWebService(String name, Attribute[] attributes, Set<Attribute> inputAttributes,
			Relation relation, Map<Attribute, Attribute> attributeMapping) {
		super(name, attributes, inputAttributes, relation, attributeMapping);
	}

	@Override
	protected Stream<Tuple> fetchTuples(Iterator<Tuple> inputTuples) {
		this.mediaType = new MediaType("application", "xml");
		String inputParams = "";
		if (inputTuples != null && inputTuples.hasNext())
			inputParams+="?";
		Tuple currentInputTuple = inputTuples.next();
		int i = 0;
		for (Attribute a:inputAttributes(false)) {
			if (i>0)
				inputParams+=";";
			inputParams+=a.getName() + "=" + currentInputTuple.getValue(i); 
			i++;
		}
		this.target = ClientBuilder.newClient().register(JacksonFeatures.class).target(url+inputParams);
		Response response = this.target.request(this.mediaType).get();
		List<Tuple> data = unmarshalXml(response,inputTuples);
		return StreamSupport.stream(data.spliterator(), false);
	}
	
	@Override
	public void close() {
	}
	
	@Override
	public boolean isClosed() throws Exception {
		return false;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Tuple> unmarshalXml(Response response,Iterator<Tuple> inputTuples) throws AccessException {
		XmlMapper mapper = new XmlMapper();
		try {
			String responseText = response.readEntity(String.class);
			List data = mapper.readValue(responseText, List.class);
			System.out.println("Received " + data.size() + " amount of records.");
			return this.processItems(data, inputTuples);
		} catch (IOException e) {
			throw new AccessException(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected List<Tuple> processItems (Object response, Iterator<Tuple>  inputTable) throws AccessException{
		List<Tuple> result = new LinkedList<>();
		if (response instanceof Collection) {
			for (Object o: ((Collection) response)) {
				result.addAll(this.processItems(o, inputTable));
			}
		} else if (response instanceof Map) {
			List<Tuple> t = this.processItem((Map) response, inputTable);
			if (t != null && !t.isEmpty()) {
				result.addAll(t);
			}
		} else {
			throw new AccessException("Could not deserialize response to Map/Collections.");
		}
		return result;
	}

	protected List<Tuple> processItem(Map<String, Object> item, Iterator<Tuple> inputTable) {
		List<Attribute> serviceOutputattributes = Arrays.asList(this.outputAttributes(false));			
		Object[] result = new Object[serviceOutputattributes.size()];

		int i = 0, j = 0;
		boolean hasValue = false;
		Attribute[] inputHeader = this.inputAttributes();
		Tuple first = inputTable!=null && inputTable.hasNext()?inputTable.next():null;
		for (Attribute column: this.attributeMapping.keySet()) {
			if (!Arrays.asList(inputHeader).contains(column) || inputTable.hasNext()) {
				Attribute relationAttribute = this.attributeMapping.get(column);
				result[serviceOutputattributes.indexOf(column)]= Utility.cast(relationAttribute.getType(), item.get(column.getName()));
				hasValue |= result[i] != null;
			} else if ((j = Arrays.asList(inputHeader).indexOf(column)) >=0 ) {
				result[i]= Utility.cast(column.getType(), first.getValue(j));
				hasValue = true;
			}
			i++;
		}
		if (item.size() > 0 && !hasValue) {
			List<Tuple> results = new ArrayList<>();
			for (String key: item.keySet()) {
				Object value = item.get(key);
				if (value instanceof Map) {
					List<Tuple> t = processItem((Map)value, inputTable);
					if (t!=null)
						results.addAll(t);
				}
				if (value instanceof List) {
					List<Map> data = (List<Map>)value;
					if (!data.isEmpty()) {
						if ( data.get(0) != null && data.get(0) instanceof Map) {
							for (Map d: data) {
								List<Tuple> t = processItem((Map)d, inputTable);
								if (t!=null)
									results.addAll(t);
							}
						}
					}
				}
					
			}
			return results;
		} else {
			return hasValue ? Arrays.asList(new Tuple[] {TupleType.createFromTyped(this.outputAttributes(false)).createTuple(result)}): null ;
		}
	}
	
}
