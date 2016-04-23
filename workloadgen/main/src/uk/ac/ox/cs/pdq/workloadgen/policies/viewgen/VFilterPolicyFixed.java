/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies.viewgen;

import java.util.List;




import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter;
import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter.InvalidParameterException;
import uk.ac.ox.cs.pdq.workloadgen.query.FilterPredicate;
import uk.ac.ox.cs.pdq.workloadgen.query.Query;
import uk.ac.ox.cs.pdq.workloadgen.query.View;

import com.google.common.collect.Lists;

/**
 * A filter policy that generates a fixed number of filter predicates for a
 * query. In particular, it can generate some number of equi predicates and some
 * number of range predicates.
 * 
 * @author herodotos.herodotou
 */
public class VFilterPolicyFixed extends VFilterPolicy {

	private int numTotalFilters;
	private int maxNumEquiFilters;
	private int maxNumRangeFilters;

	// For range filters, the policy will generate a range that will fall between
	// a min and a max percent.
	private float minRangePercent;
	private float maxRangePercent;

	/**
	 * Default constructor
	 */
	public VFilterPolicyFixed() {
		super();
		this.numTotalFilters = 0;
		this.maxNumEquiFilters = 0;
		this.maxNumRangeFilters = 0;
		this.minRangePercent = 0f;
		this.maxRangePercent = 100f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cy.ac.cut.cs.workloadgen.policies.IPolicy#initialize(java.util.List)
	 */
	@Override
	public boolean initialize(List<Parameter> params)
			throws InvalidParameterException {
		// Get the parameters
		for (Parameter param : params) {
			try {
				if (param.getName().equalsIgnoreCase("NumTotalFilters")) {
					numTotalFilters = Integer.parseInt(param.getValue());
				} else if (param.getName().equalsIgnoreCase("MaxNumEquiFilters")) {
					maxNumEquiFilters = Integer.parseInt(param.getValue());
				} else if (param.getName().equalsIgnoreCase("MaxNumRangeFilters")) {
					maxNumRangeFilters = Integer.parseInt(param.getValue());
				} else if (param.getName().equalsIgnoreCase("MinRangePercent")) {
					minRangePercent = Float.parseFloat(param.getValue());
				} else if (param.getName().equalsIgnoreCase("MaxRangePercent")) {
					maxRangePercent = Float.parseFloat(param.getValue());
				} else {
					throw new InvalidParameterException("Unexpected parameter name="
							+ param.getName());
				}
			} catch (NumberFormatException e) {
				throw new InvalidParameterException("Invalid parameter", e);
			}
		}

		// Validate the parameters
		return maxNumEquiFilters <= numTotalFilters
				&& maxNumRangeFilters <= numTotalFilters && minRangePercent >= 0
				&& maxRangePercent <= 100 && minRangePercent <= maxRangePercent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cy.ac.cut.cs.workloadgen.policies.querygen.IQueryFilterPolicy#
	 * createFilterPredicates(cy.ac.cut.cs.workloadgen.query.Query)
	 */
	@Override
	public boolean createFilterPredicates(Query query, View view) {
		//Gather the query filter predicates that reference tables appearing in the input view 
		List<FilterPredicate> filterPredicates = Lists.newArrayList();
		for(FilterPredicate filter:query.getWhereClause().getFilterPredicates()) {
			if(view.getFromClause().contains(filter.getAttribute().getTable())) {
				filterPredicates.add(filter);
			}
		}
		
		// Select the filter predicates to add to the view
		int iter = 0;
		int countTotal = 0;

		while (countTotal < numTotalFilters && iter < filterPredicates.size()) {
			FilterPredicate filter = filterPredicates.get(iter);
			view.addFilterPredicate(filter);
			++iter;
		}
		
		return true;
	}

}
