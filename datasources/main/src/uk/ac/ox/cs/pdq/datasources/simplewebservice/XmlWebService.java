// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.XmlExecutableAccessMethod.PostParameter;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author gabor Implements the ExecutableAccessMethod interface and provides a
 *         minimalistic example how to create one. This XmlWebService will
 *         create a link based on the inputs, and creates a connection to a web
 *         service and parses the results as an xml.
 *
 */
public class XmlWebService extends ExecutableAccessMethod {
	private String urlTemplate = null;
	private List<PostParameter> postParams = new ArrayList<>();

	private WebTarget target;

	private static final long serialVersionUID = 5268175711548627539L;

	public XmlWebService(Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(attributes, inputs, relation, attributeMapping);
	}

	public XmlWebService(String name, Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(name, attributes, inputs, relation, attributeMapping);
	}

	public XmlWebService(Attribute[] attributes, Set<Attribute> inputAttributes, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(attributes, inputAttributes, relation, attributeMapping);
	}

	public XmlWebService(String name, Attribute[] attributes, Set<Attribute> inputAttributes, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(name, attributes, inputAttributes, relation, attributeMapping);
	}

	/**
	 * @see uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod#fetchTuples(java.util.Iterator)
	 */
	@Override
	protected Stream<Tuple> fetchTuples(Iterator<Tuple> inputTuples) {
		MediaType mediaType = new MediaType("application", "xml");
		List<Tuple> data = new ArrayList<>();
		if (inputTuples == null) {
			// No input case
			if (urlTemplate.contains("{") || this.postParams.size() > 0)
				throw new RuntimeException("Inputs are defined, but not specified!");
			this.target = ClientBuilder.newClient().register(JacksonFeatures.class).target(urlTemplate);
			Response response = this.target.request(mediaType).get();
			data.addAll(unmarshalXml(response, null));
			response.close();
		} else {
			// there might be more then one set of inputs, we call the service for each set
			// one by one.
			while (inputTuples.hasNext()) {
				// 1 or more input tuple is given, run all of them and then concatenate the
				// results.
				Tuple tuple = inputTuples.next();
				this.target = ClientBuilder.newClient().register(JacksonFeatures.class)
						.target(createUrlWithInputs(tuple));
				if (postParams!=null && !postParams.isEmpty()) {
					// parameters are in the link and or posted as objects.
					MultivaluedHashMap<String,String> parameters = new MultivaluedHashMap<>();
					for (PostParameter p:postParams) {
						String value = p.getValue();
						for (int i = 0; i < tuple.size(); i++) {
							value = value.replaceAll("\\{" + i + "\\}", "" + tuple.getValue(i));
						}
						List<String> l = new ArrayList<>();
						l.add(value);
						parameters.put(p.getName(), l);
					}
					
					Response response = this.target.request(mediaType).post(Entity.form(parameters));
					data.addAll(unmarshalXml(response, tuple));
					response.close();
				} else {
					// parameters are in the link only.
					Response response = this.target.request(mediaType).get();
					data.addAll(unmarshalXml(response, tuple));
					response.close();
				}
			}
		}
		// return the tuples as a stream.
		return StreamSupport.stream(data.spliterator(), false);
	}

	/**
	 * Forms the connection url using the templates given in the configuration, and
	 * the actual inputs. There are error checks to make sure the configuration
	 * referes to the inputs as {0}, {1}, ... {n-1} where n is the number of inputs.
	 * 
	 * @param tuple
	 * @return
	 */
	private String createUrlWithInputs(Tuple tuple) {
		// Error checkings
		if (tuple == null)
			throw new RuntimeException("Input tuple cannot be null");
		if (urlTemplate.contains("{" + tuple.size() + "}")) {
			throw new RuntimeException(
					"Input attribute number " + tuple.size() + " specified in the url pattern, but the input has only "
							+ tuple.size() + " attributes. (input indexing starts from 0)");
		}
		for (PostParameter requestTemplate : this.postParams) {
			if (requestTemplate.getValue().contains("{" + tuple.size() + "}")) {
				throw new RuntimeException("One of the request templates refers to {" + tuple.size()
						+ "}, but the input has only " + tuple.size() + " attributes. (input indexing starts from 0)");
			}
		}

		List<Integer> inputIndexes = new ArrayList<Integer>();
		for (PostParameter requestTemplate : this.postParams) {
			inputIndexes.addAll(parseTemplate(requestTemplate.getValue()));
		}
		inputIndexes.addAll(parseTemplate(urlTemplate));
		for (Integer i : inputIndexes) {
			if (i < 0 || i >= tuple.size())
				throw new RuntimeException("One of the request templates or the url template refers to {" + i
						+ "} attribute, but the input has only " + tuple.size()
						+ " attributes. (input indexing starts from 0)");
		}

		String urlWithInput = urlTemplate;
		// A web link have to have the format [url]?[parameters] so we make sure we have
		// the '?' in it.
		if (!urlTemplate.contains("?")) {
			urlWithInput += "?";
		}
		
		// replace the {x} with the actual input
		for (int i = 0; i < tuple.size(); i++) {
			urlWithInput = urlWithInput.replaceAll("\\{" + i + "\\}", "" + tuple.getValue(i));
		}
		// make sure there are no extra {?} template fragments.
		if (urlWithInput.contains("{") || urlWithInput.contains("}")) {
			throw new RuntimeException("failed to parse input parameters while preparing link: " + urlWithInput);
		}
		// return the url as a string
		return urlWithInput;
	}

	/**
	 * Used excusively for error checking. Given an input string it will find
	 * "{[something]}" parts, makes sure the [something] is a number, and returns
	 * the list of all numbers found. This can be later used to assess if the
	 * smallest number is 0 or not, and the largest number is still a valid input
	 * index...
	 * 
	 * @param string
	 * @return
	 */
	private Collection<Integer> parseTemplate(String string) {
		Collection<Integer> inputIndexes = new ArrayList<Integer>();
		int begin = 0;
		while (string.indexOf('{', begin) > 0) {
			begin = string.indexOf('{', begin);
			int end = string.indexOf('}', begin);
			if (end < begin)
				throw new RuntimeException("Broken input template: " + string);
			String number = string.substring(begin + 1, end);
			try {
				inputIndexes.add(Integer.parseInt(number));
			} catch (Exception e) {
				throw new RuntimeException("Broken input template: " + string, e);
			}
			begin = end + 1;
		}
		return inputIndexes;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean isClosed() throws Exception {
		return false;
	}

	public String getUrl() {
		return urlTemplate;
	}

	public void setUrl(String url) {
		this.urlTemplate = url;
	}

	/**
	 * Given the current response from the webservice, generates a list of tuples as
	 * results. The inputTuple is needed because in some cases the output attributes
	 * contains the input,but the access method might not return it. In this case
	 * the output tuple will be formed by adding the input to the output set.
	 * 
	 * @param response
	 * @param inputTuple
	 * @return
	 * @throws AccessException
	 */
	@SuppressWarnings({ "rawtypes" })
	public List<Tuple> unmarshalXml(Response response, Tuple inputTuple) throws AccessException {
		XmlMapper mapper = new XmlMapper();
		try {
			String responseText = response.readEntity(String.class);
			Object data = null;
			try {
				data = mapper.readValue(responseText, List.class);
				System.out.println("Received a list with " + ((List) data).size() + " amount of records.");
			} catch (Exception e) {
				data = mapper.readValue(responseText, Map.class);
				System.out.println("Received a map with " + ((Map) data).size() + " amount of records.");
			}
			return this.processItems(data, inputTuple);
		} catch (IOException e) {
			throw new AccessException(e.getMessage(), e);
		}
	}

	/**
	 * The output might be a Map or a List of Maps. this function will check and
	 * process them accordingly.
	 * 
	 * @param response
	 * @param inputTuple
	 * @return
	 * @throws AccessException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected List<Tuple> processItems(Object response, Tuple inputTuple) throws AccessException {
		List<Tuple> result = new LinkedList<>();
		if (response instanceof Collection) {
			for (Object o : ((Collection) response)) {
				result.addAll(this.processItems(o, inputTuple));
			}
		} else if (response instanceof Map) {
			List<Tuple> t = this.processItem((Map) response, inputTuple);
			if (t != null && !t.isEmpty()) {
				result.addAll(t);
			}
		} else {
			throw new AccessException("Could not deserialize response to Map/Collections.");
		}
		return result;
	}

	/**
	 * A Map as input represents an attribute name / value set, that needs to be
	 * turned into a Tuple. The return value is a list for the rare cases when the
	 * results we received from the web service is a Map of lists of Maps. In such
	 * case the function will recursively call itself.
	 * 
	 * @param item
	 * @param inputTuple
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected List<Tuple> processItem(Map<String, Object> item, Tuple inputTuple) {
		List<Attribute> serviceOutputattributes = Arrays.asList(this.outputAttributes(false));
		Object[] result = new Object[serviceOutputattributes.size()];

		int i = 0, j = 0;
		boolean hasValue = false;
		Attribute[] inputHeader = this.inputAttributes();
		Tuple first = inputTuple;
		for (Attribute column : this.attributeMapping.keySet()) {
			if (!Arrays.asList(inputHeader).contains(column) || inputTuple != null) {
				Attribute relationAttribute = this.attributeMapping.get(column);
				result[serviceOutputattributes.indexOf(column)] = Utility.cast(relationAttribute.getType(),
						item.get(column.getName()));
				hasValue |= result[i] != null;
			} else if ((j = Arrays.asList(inputHeader).indexOf(column)) >= 0) {
				result[i] = Utility.cast(column.getType(), first.getValue(j));
				hasValue = true;
			}
			i++;
		}
		if (item.size() > 0 && !hasValue) {
			List<Tuple> results = new ArrayList<>();
			for (String key : item.keySet()) {
				Object value = item.get(key);
				if (value instanceof Map) {
					List<Tuple> t = processItem((Map) value, inputTuple);
					if (t != null)
						results.addAll(t);
				}
				if (value instanceof List) {
					List<Map> data = (List<Map>) value;
					if (!data.isEmpty()) {
						if (data.get(0) != null && data.get(0) instanceof Map) {
							for (Map d : data) {
								List<Tuple> t = processItem((Map) d, inputTuple);
								if (t != null)
									results.addAll(t);
							}
						}
					}
				}

			}
			return results;
		} else {
			return hasValue
					? Arrays.asList(
							new Tuple[] { TupleType.createFromTyped(this.outputAttributes(false)).createTuple(result) })
					: null;
		}
	}

	public List<PostParameter> getRequestTemplates() {
		return this.postParams;
	}

	public void setRequestTemplates(List<PostParameter> postParams) {
		this.postParams = postParams;
	}

}
