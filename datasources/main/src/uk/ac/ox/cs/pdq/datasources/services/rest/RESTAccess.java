package uk.ac.ox.cs.pdq.datasources.services.rest;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.fasterxml.jackson.jaxrs.json.annotation.JacksonFeatures;

// TODO: Auto-generated Javadoc
/**
 * Represents an access to a REST service. This class can be used as a request 
 * builder, i.e. for modifying a request until it is eventually sent.
 * @author Julien Leblay
 */
public class RESTAccess implements uk.ac.ox.cs.pdq.datasources.builder.Builder<WebTarget> {

	/** The access's URL. */
	private final String baseUrl;
	
	/**
	 * The access's url-based input methods.
	 */
	private final Set<InputMethod> urlMethods = new LinkedHashSet<>();
	
	/**
	 * The access's URL input parameters.
	 */
	private final Map<String, Object> urlParams = new LinkedHashMap<>();
	
	/**
	 * The access's path-based input methods.
	 */
	private final Set<InputMethod> pathMethods = new LinkedHashSet<>();

	/**
	 * The access's path-based input parameters.
	 */
	private final Map<String, Object> pathParams = new LinkedHashMap<>();
	
	/**  true unless the access must be followed by at least one other request for to be complete. */
	private boolean isComplete = true;
	
	/**
	 * Default constructor.
	 * @param url the base URL
	 */
	public RESTAccess(String url) {
		super();
		this.baseUrl = url;
	}
	
	/**
	 * Builds the.
	 *
	 * @return a WebTarget forms by append the input params respecting
	 * input parameters and methods.
	 * @see uk.ac.ox.cs.pdq.datasources.builder.Builder#build()
	 */
	@Override 
	public WebTarget build() {
		StringBuilder url = new StringBuilder(this.baseUrl);
		for (InputMethod key: this.pathMethods) {
			url.append(this.pathParams.get(key.getName()));
		}
		@SuppressWarnings("deprecation")
		WebTarget result = ClientBuilder.newClient().register(JacksonFeatures.class).target(url.toString());
		for (Map.Entry<String, Object> entry: this.urlParams.entrySet()) {
			result = result.queryParam(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Format a URI according to a set of input methods and the corresponding
	 * parameters. The formatting only considers PATH_ELEMENT type input 
	 * methods.
	 * For URL parameters, use processURLParams
	 *
	 * @param methods the methods
	 * @param inputParams the input params
	 */
	public void processParams(Set<InputMethod> methods, Map<String, Object> inputParams) {
		for (InputMethod m: methods) {
			if (m.getType() == InputMethod.Types.PATH_ELEMENT) {
				this.pathParams.put(m.getName(), m.format(inputParams));
				this.pathMethods.add(m);
			} else if (m.getType() == InputMethod.Types.URL_PARAM) {
				this.urlParams.put(m.getName(), m.format(inputParams));
				this.urlMethods.add(m);
			}
		}
	}

	/**
	 * Checks if is complete.
	 *
	 * @return boolean
	 */
	public boolean isComplete() {
		return this.isComplete;
	}

	/**
	 * Checks if is complete.
	 *
	 * @param b boolean
	 */
	public void isComplete(boolean b) {
		this.isComplete = b;
	}
}
