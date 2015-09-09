package uk.ac.ox.cs.pdq.services.policies;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.services.AccessPostProcessor;
import uk.ac.ox.cs.pdq.services.AccessPreProcessor;
import uk.ac.ox.cs.pdq.services.ServiceRepository;
import uk.ac.ox.cs.pdq.services.rest.InputMethod;
import uk.ac.ox.cs.pdq.services.rest.PathOutputMethod;
import uk.ac.ox.cs.pdq.services.rest.RESTAccess;
import uk.ac.ox.cs.pdq.services.rest.RESTAttribute;
import uk.ac.ox.cs.pdq.services.rest.RESTRequestEvent;
import uk.ac.ox.cs.pdq.services.rest.RESTResponseEvent;

import com.google.common.base.Preconditions;

/**
 * This usage policy incorporates paging constraints on the access to be 
 * performed. It set the access 'isComplete' fields to false until the last
 * page has been reached.
 * 
 * @author Julien Leblay
 *
 */
public class PagingLimit implements UsagePolicy, 
		AccessPreProcessor<RESTRequestEvent>, AccessPostProcessor<RESTResponseEvent>  {

	protected static final String PAGE_SIZE = "limit";
	protected static final String START_INDEX = "start-index";
	protected static final String PAGE_SIZE_ATTRIBUTE = "page-size";
	protected static final String PAGE_INDEX_ATTRIBUTE = "page-index";
	protected static final String TOTAL_ITEMS_ATTRIBUTE = "total-items";

	/** Size of a single page */
	protected final int pageSize;
	
	/** The index of the first page (usually 0 or 1). */
	protected final int startIndex;

	/** The current page index*/
	protected int pageIndex;
	
	/** The total number of items expected to scroll. */
	protected int totalItems = -1;
	
	/** The attribute where the page size is defined. */
	protected RESTAttribute pageSizeAttributes;

	/** The attribute where the page index is defined. */
	protected RESTAttribute pageIndexAttributes;

	/** The attribute where the total number of items is defined (not currently used, consider removing). */
	protected RESTAttribute totalItemsAttributes;

	/** The set of input method used page the instance */
	private Set<InputMethod> inputMethods = new LinkedHashSet<>();		
	
	/**
	 * Default constructor.
	 * @param pageSize
	 * @param startIndex
	 * @param pageSizeAtt
	 * @param pageIndex
	 * @param totalItems
	 */
	protected PagingLimit(int pageSize, int startIndex, RESTAttribute pageSizeAtt, RESTAttribute pageIndex, RESTAttribute totalItems) {
		super();
		Preconditions.checkArgument(pageSizeAtt.getInputMethod() != null);
		Preconditions.checkArgument(pageIndex.getInputMethod() != null);
		this.pageSize = pageSize;
		this.startIndex = startIndex;
		this.pageIndex = startIndex;
		this.pageSizeAttributes = pageSizeAtt;
		this.pageIndexAttributes = pageIndex;
		this.totalItemsAttributes = totalItems;
		this.inputMethods.add(this.pageSizeAttributes.getInputMethod());
		this.inputMethods.add(this.pageIndexAttributes.getInputMethod());
	}
	
	/**
	 * Constructor used by the usage policy factory.
	 * @param repo
	 * @param properties
	 */
	public PagingLimit(ServiceRepository repo, Properties properties) {
		this(Integer.parseInt(properties.getProperty(PAGE_SIZE)),
			Integer.parseInt(properties.getProperty(START_INDEX)),
				new RESTAttribute(
						new Attribute(Integer.class, properties.getProperty(PAGE_SIZE_ATTRIBUTE)),
						new PathOutputMethod(properties.getProperty(PAGE_SIZE_ATTRIBUTE)),
						getInputMethod(repo, properties.getProperty(PAGE_SIZE_ATTRIBUTE))),
				new RESTAttribute(
						new Attribute(Integer.class, properties.getProperty(PAGE_INDEX_ATTRIBUTE)),
						new PathOutputMethod(properties.getProperty(PAGE_INDEX_ATTRIBUTE)),
						getInputMethod(repo, properties.getProperty(PAGE_INDEX_ATTRIBUTE))),
				new RESTAttribute(
						new Attribute(Integer.class, properties.getProperty(TOTAL_ITEMS_ATTRIBUTE)),
						new PathOutputMethod(properties.getProperty(TOTAL_ITEMS_ATTRIBUTE))));
	}

	/**
	 * @param repo
	 * @param name
	 * @return the input method the given name in the given repository.
	 */
	private static InputMethod getInputMethod(ServiceRepository repo, String name) {
		InputMethod result = repo.getInputMethod(name);
		if (result == null) {
			result = new InputMethod(name);
		}
		return result;
	}
	
	/**
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.service.UsagePolicy#copy()
	 */
	@Override
	public UsagePolicy copy() {
		return new PagingLimit(this.pageSize, this.startIndex,
				this.pageSizeAttributes, this.pageIndexAttributes,
				this.totalItemsAttributes);
	}

	/**
	 * @param event RESTRequestEvent
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.service.AccessPreProcessor#processAccessRequest(uk.ac.ox.cs.pdq.runtime.wrappers.service.RequestEvent)
	 */
	@Override
	public void processAccessRequest(RESTRequestEvent event) {
		RESTAccess access = event.getAccess();
		Map<String, Object> inputParams = new LinkedHashMap<>();
		inputParams.put(this.pageSizeAttributes.getName(), this.pageSize);
		inputParams.put(this.pageIndexAttributes.getName(), this.pageIndex);
		access.processParams(this.inputMethods, inputParams);
	}

	/**
	 * @param event RESTResponseEvent
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.service.AccessPostProcessor#processAccessResponse(uk.ac.ox.cs.pdq.runtime.wrappers.service.ResponseEvent)
	 */
	@Override
	public void processAccessResponse(RESTResponseEvent event) {
		this.increment();
		if (event.getOutput().size() < this.pageSize) {
			event.getAccess().isComplete(true);
			this.pageIndex = this.startIndex;
		} else {
			event.getAccess().isComplete(false);
		}
	}
	
	protected void increment() {
		this.pageIndex ++;
	}
}
