
package uk.ac.ox.cs.pdq.datasources.legacy.services.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jcs.JCS;
import org.apache.jcs.access.CacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.legacy.Pipelineable;
import uk.ac.ox.cs.pdq.datasources.legacy.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.datasources.legacy.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.legacy.services.Service;
import uk.ac.ox.cs.pdq.datasources.legacy.services.policies.UsagePolicy;
import uk.ac.ox.cs.pdq.datasources.legacy.services.policies.UsagePolicyViolationException;
import uk.ac.ox.cs.pdq.datasources.legacy.services.policies.UsageViolationExceptionHandler;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;
import uk.ac.ox.cs.pdq.util.Utility;


/**
 * Wrapper class for RESTful accessible relations.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 *
 */
public final class RESTRelation extends Relation implements Service, Pipelineable, RelationAccessWrapper {

	private static final long serialVersionUID = -5829653747851001121L;

	private static Logger log = Logger.getLogger(RESTRelation.class);

	/**
	 * All list of attributes the including the relations actual attributes,
	 * plus additional, possibly interleaved static attributes.
	 */
	private final RESTAttribute[] allAttributes;

	/** Event bus where pre- and post-process event are propagated. */
	private final EventBus eventBus = new EventBus(new UsageViolationExceptionHandler());

	/**  The service's expected media type (currently, only XML and JSON are supported). */
	private final MediaType mediaType;

	/**  The service's base URL. */
	private final String url;

	/**  The service's result delimiter. */
	private final String resultDelimiter;

	/**  All tuples that have been accessed so far. */
	private CacheAccess cache = null;

	private Integer serial = 0;

	/**
	 * Default constructor.
	 *
	 * @param name the name
	 * @param attributes the attributes
	 * @param accessMethods the access methods
	 * @param allAttributes the all attributes
	 * @param url String
	 * @param mimeType the mime type
	 * @param resultDelim String
	 * @param policies the policies
	 */
	public RESTRelation(
			String name, RESTAttribute[] attributes,
			AccessMethodDescriptor[] accessMethods,
			RESTAttribute[] allAttributes, 
			String url, 
			MediaType mimeType, 
			String resultDelim,
			Collection<UsagePolicy> policies) {
		super(name, attributes,accessMethods);
		Preconditions.checkArgument(url != null);
		Preconditions.checkArgument(policies != null);

		this.allAttributes = allAttributes.clone();
		this.url = url;
		this.mediaType = mimeType;
		this.resultDelimiter = resultDelim != null ? resultDelim : "";
		for (UsagePolicy policy: policies) {
			this.register(policy);
		}
		CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance(); 
		Properties properties = new Properties(); 
		try {
			properties.load(ClassLoader.getSystemResourceAsStream("./resources/cache.ccf")); 
			ccm.configure(properties); 
			this.cache = JCS.getInstance("restrelation");
		} catch (IOException | CacheException e) {
			throw new IllegalStateException("Cache not properly initialized.");
		}
	}

	/**
	 *
	 * @param inputAttributes List<? extends Attribute>
	 * @param inputs ResetableIterator<Tuple>
	 * @return ResetableIterator<Tuple>
	 * @see uk.ac.ox.cs.pdq.wrappers.Pipelineable#iterator(List<? extends Attribute>, ResetableIterator<Tuple>)
	 */
	@Override
	public ResetableIterator<Tuple> iterator(Attribute[] inputAttributes, ResetableIterator<Tuple> inputs) {
		RESTAttribute[] attributes = new RESTAttribute[inputAttributes.length];
		System.arraycopy(inputAttributes, 0, attributes, 0, inputAttributes.length);
		return new AccessIterator(attributes, inputs); 
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.Pipelineable#iterator()
	 */
	@Override
	public ResetableIterator<Tuple> iterator() {
		return new AccessIterator(); 
	}

	/**
	 *
	 * @param inputHeader the input header
	 * @param inputTuples the input tuples
	 * @return Table
	 * @see uk.ac.ox.cs.pdq.datasources.services.servicegroup.Service#access(Table)
	 */
	@Override
	public Table access(Attribute[] inputHeader, ResetableIterator<Tuple> inputTuples) {
		RESTAttribute[] inputAttributes = new RESTAttribute[inputHeader.length];
		for (int attributeIndex = 0; attributeIndex < inputHeader.length; ++attributeIndex) 
			inputAttributes[attributeIndex] = (RESTAttribute) this.getAttribute(inputHeader[attributeIndex].getName());
		ResetableIterator<Tuple> iterator = this.iterator(inputAttributes, inputTuples);
		iterator.open();
		Table result = new Table(this.getAttributes());
		while (iterator.hasNext()) {
			result.appendRow(iterator.next());
		}
		return result;
	}

	/**
	 *
	 * @return Table
	 * @see uk.ac.ox.cs.pdq.datasources.services.servicegroup.Service#access(Table)
	 */
	@Override
	public Table access() {
		ResetableIterator<Tuple> iterator = this.iterator();
		iterator.open();
		Table result = new Table(this.getAttributes());
		while (iterator.hasNext()) {
			result.appendRow(iterator.next());
		}
		return result;
	}

	/**
	 *
	 * @return the relation's media type
	 */
	public MediaType getMediaType() {
		return this.mediaType;
	}

	/**
	 *
	 * @param inputs the inputs
	 * @return the set of input methods appearing in the given REST attributes
	 * list.
	 */
	private Set<InputMethod> getInputMethod(RESTAttribute[] inputs) {
		Set<InputMethod> result = new LinkedHashSet<>();
		for (RESTAttribute a: this.allAttributes) {
			if (a instanceof StaticInput || Arrays.asList(inputs).contains(a)) {
				InputMethod m = a.getInputMethod();
				if (m != null) {
					result.add(m);
				}
			}
		}
		return result;
	}

//	/**
//	 * Gets the input params.
//	 *
//	 * @param inputs the inputs
//	 * @param tuples Table
//	 * @return a map containing input parameter to be use to build a service
//	 * request. The parameters include static parameters and access-specific
//	 * parameters. The key of the map correspond to either input method names,
//	 * template entries or the input attribute names themselves.
//	 */
//	private Map<String, Object> getInputParams(RESTAttribute[] inputs, Table tuples) {
//		Map<String, Object> result = new LinkedHashMap<>();
//		Map<String, Object> partial = null;
//		for (Tuple t: tuples) {
//			partial = this.getInputParams(inputs, t);
//			for (RESTAttribute a: inputs) {
//				InputMethod im = a.getInputMethod();
//				String pname = im.getParameterizedName(a.getInputParams());
//				Object o = result.get(pname);
//				if (o == null) {
//					result.put(pname, partial.get(pname));
//				} else if (a.allowsBatch()) {
//					result.put(pname, String.valueOf(o) + im.getBatchDelimiter() + partial.get(pname));
//				} else {
//					throw new AccessException("Attempting to perform batch input, while access method does not allow it.");
//				}
//
//			}
//		}
//		if (partial != null) {
//			for (RESTAttribute a: inputs) {
//				InputMethod im = a.getInputMethod();
//				String pname = im.getParameterizedName(a.getInputParams());
//				partial.remove(pname);
//			}
//			result.putAll(partial);
//		}
//		return result;
//	}

	/**
	 *
	 * @param inputs the inputs
	 * @param tuple the tuple
	 * @return a map containing input parameter to be use to build a service
	 * request. The parameters include static parameters and access-specific
	 * parameters. The key of the map correspond to either input method names,
	 * template entries or the input attribute names themselves.
	 */
	private Map<String, Object> getInputParams(RESTAttribute[] inputs, Tuple tuple) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (RESTAttribute input: this.allAttributes) {
			InputMethod m = input.getInputMethod();
			Object value = null;
			int i = -1;
			if (input instanceof StaticInput) {
				value = ((StaticInput<?>) input).getDefaultValue();
			} else {
				if ((i = Arrays.asList(inputs).indexOf(input)) >= 0) {
					value = tuple.getValue(i);
				} else {
					continue;
				}
			}

			if (m != null) {
				Collection<String> p = input.getInputParams();
				if (value == null) {
					value = m.getDefaultValue();
				}
				if (p != null && !p.isEmpty()) {
					for (@SuppressWarnings("unused") String s: p) {
						//s is not used since we want to use the tuple's value instead of the value in xml. 
						result.put(m.getParameterizedName(p), value);
					}
				} else {
					result.put(m.getName(), value);
				}
			} else {
				result.put(input.getName(), value);
			}
		}
		return result;
	}


	/**
	 * Performs an access to the service for a batch of input tuples.
	 *
	 * @param inputAttributes the input attributes
	 * @param inputTuples Table
	 * @return the dynamic table resulting from the access.
	 */
	public Table accessBatchInput(List<RESTAttribute> inputAttributes, Table inputTuples) {
		//		Preconditions.checkArgument(inputAttributes.size() == 1);
		//		Preconditions.checkArgument(inputTuples != null);
		//		
		//		DynamicTable cacheMisses = new DynamicTable(inputAttributes);
		//		DynamicTable output = new DynamicTable(this.getAttributes());
		//		for (Tuple t: inputTuples) {
		//			DynamicTable cached = this.cache.get(t);
		//			if (cached != null) {
		//				output.appendRows(cached);
		//			} else {
		//				cacheMisses.appendRow(t);
		//			}
		//		}
		//		
		//		// Put all dynamic in a map
		//		Set<InputMethod> inputMethods = this.getInputMethod(inputAttributes);

		Table result = new Table(this.getAttributes());
		//		int batchSize = inputAttributes.get(0).getInputMethod().getBatchSize();
		//
		//		Iterator<Tuple> it = cacheMisses.iterator();
		//		while (it.hasNext()) {
		//			
		//			// For each batch of batchSize
		//			DynamicTable batchInput = new DynamicTable(inputAttributes);
		//			for (int i = 0; i < batchSize && it.hasNext(); i++) {
		//				batchInput.appendRow(it.next());
		//			}
		//
		//			Map<String, Object> inputParams = this.getInputParams(inputAttributes, batchInput);
		//			RESTAccess access = new RESTAccess(this.properties.getProperty(URL));
		//			access.processParams(inputMethods, inputParams);
		//			
		//			AccessEvent event = null;
		//			do {
		//				// Keep requesting until the access is complete
		//				event = new RESTRequestEvent(access, batchInput);
		//				this.eventBus.post(event);
		//				if (event.hasUsageViolationMessage()) {
		//					throw new UsagePolicyViolationException(event.getUsageViolationMessage());
		//				}
		//
		//				WebTarget target = access.build();
		//				log.info(target.getUri());
		//
		//				// Connection to the remote server
		//				Response response = target.request(MediaType.APPLICATION_JSON).get();
		//				if (response.getStatus() == 200) {
		//					output = this.parseResponse(response, batchInput);
		//					event = new RESTResponseEvent(access, response, output);
		//					this.eventBus.post(event);
		//					if (event.hasUsageViolationMessage()) {
		//						throw new UsagePolicyViolationException(event.getUsageViolationMessage());
		//					}
		//					result.appendRows(output);
		//				} else if (response.getStatus() == 404) {
		//					log.warn(response.getStatusInfo().getReasonPhrase());
		//					break;
		//				} else {
		//					throw new AccessException(response.getStatus()
		//							+ " - " + response.getStatusInfo().getReasonPhrase()
		//							+ "\n" + response.readEntity(String.class));
		//				}
		//
		//			} while(event.hasMoreElements());
		//		}
		//
		//		this.cache.put(inputs, result);

		return result;
	}

	/**
	 * Performs an access to the service for a single input tuple.
	 *
	 * @param inputAttributes the input attributes
	 * @param inputTuple the input tuple
	 * @return the dynamic table resulting from the access.
	 * @throws AccessException the access exception
	 * @throws ProcessingException the processing exception
	 */
	RESTResponseEvent accessSingleInput(RESTAttribute[] inputAttributes, Tuple inputTuple) throws AccessException, ProcessingException {
		Preconditions.checkArgument(inputTuple != null ?
				inputAttributes.length == inputTuple.size() :
					inputAttributes.length == 0);

		Table inputTable = new Table(inputAttributes);
		if (inputTuple != null) {
			inputTable.appendRow(inputTuple);
		}

		// Put all dynamic in a map
		Set<InputMethod> inputMethods = this.getInputMethod(inputAttributes);
		Map<String, Object> inputParams = this.getInputParams(inputAttributes, inputTuple);

		RESTAccess access = new RESTAccess(this.url);
		access.processParams(inputMethods, inputParams);
		return this.proceedAccess(new RESTRequestEvent(this, access, inputTable));
	}

	/**
	 * Performs an access to the service for a single input tuple.
	 *
	 * @return the dynamic table resulting from the access.
	 * @throws AccessException the access exception
	 * @throws ProcessingException the processing exception
	 */
	RESTResponseEvent accessInputFree() throws AccessException, ProcessingException {
		RESTAttribute[] inputAttributes = new RESTAttribute[]{};
		Table inputTable = new Table(inputAttributes);

		Set<InputMethod> inputMethods = this.getInputMethod(inputAttributes);
		Map<String, Object> inputParams = this.getInputParams(inputAttributes, Tuple.EmptyTuple);

		RESTAccess access = new RESTAccess(this.url);
		access.processParams(inputMethods, inputParams);
		return this.proceedAccess(new RESTRequestEvent(this, access, inputTable));
	}

	/**
	 * Processes an access request and response events in a row.
	 *
	 * @param requestEvent the request event
	 * @return RESTResponseEvent
	 * @throws AccessException the access exception
	 * @throws ProcessingException the processing exception
	 */
	RESTResponseEvent proceedAccess(RESTRequestEvent requestEvent) throws AccessException, ProcessingException {
		this.eventBus.post(requestEvent);
		if (requestEvent.hasUsageViolationMessage()) {
			throw new UsagePolicyViolationException(requestEvent.getUsageViolationMessage());
		}

		RESTResponseEvent responseEvent = requestEvent.processRequest();
		this.eventBus.post(responseEvent);
		if (responseEvent.hasUsageViolationMessage()) {
			throw new UsagePolicyViolationException(responseEvent.getUsageViolationMessage());
		}
		return responseEvent;
	}

	/**
	 * Parse the response of a service call, so as to fit it is a dynamic table
	 * according to the output methods defined externally.
	 *
	 * @param response the response
	 * @param inputs Table
	 * @return a DynamicTable whose data has been extract from the given
	 * response, using the output methods defined externally.
	 * @throws AccessException the access exception
	 */
	Table parseResponse(Response response, Table inputs) throws AccessException {
		Table result = new Table(this.getAttributes());

		ObjectMapper mapper = new ObjectMapper();
		try {
			String s = response.readEntity(String.class);
			List<String> delim = this.getResultDelimiter();

			Object r = null;
			if (delim.isEmpty()) {
				try {
					r = mapper.readValue(s, List.class);
				} catch (JsonMappingException e) {
					log.debug(e);
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

	/**
	 * Parse the response of a service call, assuming it is Json-formatted,
	 * so as to fit it is a dynamic table according to the output methods 
	 * defined externally.
	 *
	 * @param response the response
	 * @param inputs the inputs
	 * @return a DynamicTable whose data has been extract from the given
	 * response, using the output methods defined externally.
	 * @throws AccessException the access exception
	 */
	Table parseJson(Response response, Table inputs) throws AccessException {
		Table result = new Table(this.getAttributes());

		ObjectMapper mapper = new ObjectMapper();
		try {
			String s = response.readEntity(String.class);
			List<String> delim = this.getResultDelimiter();

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

	/**
	 * Parse the response of a service call, assuming it is XML-formatted,
	 * so as to fit it is a dynamic table according to the output methods 
	 * defined externally.
	 *
	 * @param response the response
	 * @param inputs the inputs
	 * @return a DynamicTable whose data has been extract from the given
	 * response, using the output methods defined externally.
	 * @throws AccessException the access exception
	 */
	@SuppressWarnings("rawtypes")
	Table  parseXml(Response response, Table inputs) throws AccessException {
		Table result = new Table(this.getAttributes());

		XmlMapper mapper = new XmlMapper();
		try {
			String s = response.readEntity(String.class);
			List<String> delim = this.getResultDelimiter();
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

	/**
	 *
	 * @return a list of string representation path where the collection of
	 * results can be found in a reponse.
	 */
	private List<String> getResultDelimiter() {
		String[] result = this.resultDelimiter.split("/");
		if (result.length == 1 && result[0].trim().isEmpty()) {
			result = new String[0];
		}
		return Lists.newArrayList(result);
	}

	/**
	 * Converts a collection of a loosely-typed maps to a collection of tuples.
	 *
	 * @param delimiters List<String>
	 * @param response Object
	 * @param type the type
	 * @param inputTable Table
	 * @return a collection of tuple representations of the given map,
	 * matching the given header and type.
	 * @throws AccessException the access exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<Tuple> processItems (
			List<String> delimiters, Object response, TupleType type,
			Table inputTable) throws AccessException{
		Collection<Tuple> result = new LinkedList<>();
		if (!delimiters.isEmpty() && (response instanceof Map)) {
			int i = 0;
			Object items = null;
			do {
				items = ((Map) response).get(delimiters.get(i));
				if (items != null && items instanceof Map) {
					return this.processItems(
							delimiters.subList(i + 1, delimiters.size()),
							items, type, 
							inputTable);
				} else if (items != null && items instanceof Collection) {
					for (Map<String, Object> m: (Collection<Map<String, Object>>) items) {
						result.add(this.processItem(m, type, inputTable));
					}
					return result;
				}
				i++;
			} while (items == null && i < delimiters.size());
		} else if (response instanceof Collection) {
			for (Object o: ((Collection) response)) {
				result.addAll(this.processItems(delimiters, o, type, inputTable));
			}
		} else if (response instanceof Map) {
			Tuple t = this.processItem((Map) response, type, inputTable);
			if (t != null) {
				result.add(t);
			}
		} else {
			throw new AccessException("Could not deserialize response to Map/Collections.");
		}
		return result;
	}

	/**
	 * Converts the content of a loosely-typed map to a tuple.
	 *
	 * @param item the item
	 * @param type the type
	 * @param inputTable Table
	 * @return a tuple representation of the given map, matching the given header and type.
	 */
	private Tuple processItem(Map<String, Object> item, TupleType type, Table inputTable) {
		Object[] result = new Object[type.size()];
		int i = 0, j = 0;
		boolean hasValue = false;
		Attribute[] inputHeader = inputTable.getHeader();
		Tuple first = inputTable.isEmpty() ? null: inputTable.iterator().next();
		for (Attribute column: this.attributes) {
			if (!Arrays.asList(inputHeader).contains(column) || inputTable.size() > 1) {
				OutputMethod om = ((RESTAttribute) column).getOutputMethod();
				if (om != null) {
					result[i]= Utility.cast(column.getType(), om.extract(item));
				}
				hasValue |= result[i] != null;
			} else if ((j = Arrays.asList(inputHeader).indexOf(column)) >=0 ) {
				result[i]= Utility.cast(column.getType(), first.getValue(j));
				hasValue = true;
			}

			i++;
		}
		return hasValue ? type.createTuple(result): null ;
	}

	/**
	 *
	 * @param p UsagePolicy
	 * @see uk.ac.ox.cs.pdq.datasources.services.servicegroup.Service#register(UsagePolicy)
	 */
	@Override
	public void register(UsagePolicy p) {
		this.eventBus.register(p);
	}

	/**
	 *
	 * @param p UsagePolicy
	 * @see uk.ac.ox.cs.pdq.datasources.services.servicegroup.Service#unregister(UsagePolicy)
	 */
	@Override
	public void unregister(UsagePolicy p) {
		this.eventBus.unregister(p);
	}

	/**
	 * The class encapsulates the pipelined behaviour of the Wrapper.
	 * 
	 * @author Julien Leblay
	 * @author Efthymia Tsamoura
	 */
	private class AccessIterator implements ResetableIterator<Tuple> {

		/** Iterator over the input tuples. */
		private final ResetableIterator<Tuple> inputs;

		/**  The list of input attributes. */
		private final RESTAttribute[] inputAttributes;

		/** Iterator over a subset of the output tuples. */
		private List<Tuple> cached;

		/** Cursor of the current iterator over the current list of outputs. */
		private int cursor = 0;

		/**
		 * The next tuple to return. This variable can only be null before the
		 * operator is instantiation or when there is not more tuple to output.
		 */
		private Tuple nextTuple;

		/**  The access event that occurred. */
		private RESTResponseEvent event = null;

		/**  The last collection tuple considered as input. */
		private Collection<Tuple> lastInput = null;

//		/** The failed tuple. */
//		private Tuple failedTuple = null;
//
//		/** The failed request. */
//		private RESTRequestEvent failedRequest = null; 

		/**
		 * Constructor without input tuples, i.e. free access
		 */
		public AccessIterator() {
			this(null, null);
		}

		/**
		 *
		 * @param inputAttributes List<RESTAttribute>
		 * @param inputTuples ResetableIterator<Tuple>
		 */
		public AccessIterator(RESTAttribute[] inputAttributes, ResetableIterator<Tuple> inputTuples) {
			this.inputs = inputTuples;
			this.inputAttributes = inputAttributes;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#open()
		 */
		@Override
		public void open() {
			synchronized (RESTRelation.this.cache) {
				this.lastInput = null;
				this.event = null;
				this.nextTuple = null;
				this.cached = null;
				this.cursor = 0;
				if (this.inputs != null) {
					this.inputs.open();
				}
				this.nextTuple();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#reset()
		 */
		@Override
		public void reset() {
			synchronized (RESTRelation.this.cache) {
				this.lastInput = null;
				this.event = null;
				this.nextTuple = null;
				this.cached = null;
				this.cursor = 0;
				if (this.inputs != null) {
					this.inputs.reset();
				}
				this.nextTuple();
			}
		}

//		/**
//		 * Deep copy.
//		 *
//		 * @return AccessIterator
//		 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#deepCopy()
//		 */
//		@Override
//		public AccessIterator deepCopy() {
//			return new AccessIterator(
//					this.inputAttributes,
//					this.inputs.deepCopy());
//		}

		/**
		 *
		 * @return boolean
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			synchronized (RESTRelation.this.cache) {
				return this.nextTuple != null;
			}
		}

		/**
		 *
		 * @return Tuple
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Tuple next() {
			synchronized (RESTRelation.this.cache) {
				if (this.nextTuple == null) {
					throw new NoSuchElementException();
				}
				Tuple result = this.nextTuple;
				if (this.cached == null) {
					this.nextTuple();
				} else if (this.cursor < this.cached.size()) {
					this.nextTuple = this.proceed();
				} else {
					// If this access is already started,
					// check if the last access input is complete
					if (this.event != null && this.event.hasMoreRequestEvents()) {
						RESTRequestEvent req = this.event.nextRequestEvent();
						RESTRelation.this.serial++;
						this.event = RESTRelation.this.proceedAccess(req);
	
						if (!this.event.getOutput().isEmpty()) {
							this.cached.addAll(this.event.getOutput().getData());
							//This data is not added to RESTRelation.this.cache
							this.nextTuple = this.proceed();
							return result;
						}
					}
					this.cached = null;
					this.nextTuple();
				}
				return result;
			}
		}

		/**
		 * Set the next tuple to the following item on the output iterator, 
		 * using the next input tuple if necessary.
		 * The nextTuple is set to null, if the iterator has reached the end.
		 *
		 * @throws AccessException the access exception
		 */
		@SuppressWarnings("unchecked")
		public void nextTuple() throws AccessException {
			this.nextTuple = null;
			// No more intermediary result, and the access is not free
			if (this.inputs != null) {
				// Iterator over the input until you get a non-empty intermediary result
				while (this.inputs.hasNext() && this.cached == null) {
					Tuple input = this.inputs.next();
					this.lastInput = Lists.newArrayList(input);
					this.cached = (List<Tuple>) RESTRelation.this.cache.get(Pair.of(RESTRelation.this.name, this.lastInput));
					this.cursor = 0;

					// Cache miss: get new tuple from the service 
					if (this.cached == null) {
						RESTRelation.this.serial++;
						this.event = RESTRelation.this.accessSingleInput(this.inputAttributes, input);
						if (!this.event.getOutput().isEmpty()) {
							try {
								this.cached = new ArrayList<>(this.event.getOutput().getData());
								RESTRelation.this.cache.put(Pair.of(RESTRelation.this.name, this.lastInput), this.cached);
							} catch (CacheException e) {
								throw new IllegalStateException();
							}
							break;
						}
					}
				}
			} else {
				this.lastInput = Lists.newArrayList(Tuple.EmptyTuple);
				this.cached = (List<Tuple>) RESTRelation.this.cache.get(Pair.of(RESTRelation.this.name, this.lastInput));
				if (this.cached == null) {
					RESTRelation.this.serial++;
					this.event = RESTRelation.this.accessInputFree();
					if (!this.event.getOutput().isEmpty()) {
						try {
							this.cached = new ArrayList<>(this.event.getOutput().getData());
							this.cursor = 0;
							RESTRelation.this.cache.put(Pair.of(RESTRelation.this.name, this.lastInput), this.cached);
						} catch (CacheException e) {
							throw new IllegalStateException();
						}
					}
				}
			}
			this.nextTuple = this.proceed();
		}

		/**
		 *
		 * @return Tuple
		 */
		private Tuple proceed() {
			if (this.cached != null
					&& this.cursor < this.cached.size()
					) {
				return this.cached.get(this.cursor++);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(RESTRelation.class.getSimpleName()).append('{')
			.append(RESTRelation.this.getName()).append('}').append('.')
			.append(this.getClass().getSimpleName()).append('(')
			.append(this.inputs).append(')');

			return result.toString();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		/*
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
