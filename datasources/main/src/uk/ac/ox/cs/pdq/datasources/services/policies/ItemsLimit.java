// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services.policies;

import java.util.Properties;

import uk.ac.ox.cs.pdq.datasources.legacy.services.ServiceRepository;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTAttribute;

/**
 * Variant of the PagingLimit usage policy, where the start of a window is 
 * given by item index rather than page index.
 * 
 * @author Julien Leblay
 *
 */
public class ItemsLimit extends PagingLimit {

	/**
	 * Constructor for ItemsLimit.
	 * @param pageSize int
	 * @param startIndex int
	 * @param pageSizeAtt RESTAttribute
	 * @param pageIndex RESTAttribute
	 * @param totalItems RESTAttribute
	 */
	private ItemsLimit(int pageSize, int startIndex, RESTAttribute pageSizeAtt, RESTAttribute pageIndex, RESTAttribute totalItems) {
		super(pageSize, startIndex, pageSizeAtt, pageIndex, totalItems);
	}

	/**
	 * Constructor for ItemsLimit.
	 * @param repo ServiceRepository
	 * @param properties Properties
	 */
	public ItemsLimit(ServiceRepository repo, Properties properties) {
		super(repo, properties);
	}

	/**
	 *
	 * @return UsagePolicy
	 * @see uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.GroupUsagePolicy.UsagePolicy#copy()
	 */
	@Override
	public UsagePolicy copy() {
		return new ItemsLimit(this.pageSize, this.startIndex, this.pageSizeAttributes, this.pageIndexAttributes, this.totalItemsAttributes);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.services.policies.PagingLimit#increment()
	 */
	@Override
	protected void increment() {
		this.pageIndex += this.pageSize;
	}
}
