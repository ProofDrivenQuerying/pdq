// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;

/**
 * Calculates costs by passing "EXPLAIN SELECT ..." commands to the database manager and parse the cost from the result	.
 * 
 * @author gabor
 *
 */
public class QueryExplainCostEstimator implements CostEstimator,OrderIndependentCostEstimator {
	public static final String COST_REGEXP_PATTERN_FOR_POSTGRES = "\\(cost=\\d+\\.\\d+\\.\\.(?<cost>\\d+\\.\\d+)\\s.*\\)";
	private DatabaseManager dm;
	private String pattern = null;

	/**
	 * @param dm
	 *            the database manager to execute the explain select command.
	 * @param pattern
	 *            the result pattern to parse costs. Depends on the database
	 *            manager, the other constructor will assume we are using postgres
	 *            and will pass down the default patter for it.
	 */
	public QueryExplainCostEstimator(DatabaseManager dm, String pattern) {
		this.pattern = pattern;
		this.dm = dm;
	}

	/**
	 * Default is postgres, but can be changed using the other constructor.
	 * 
	 * @param dm
	 */
	public QueryExplainCostEstimator(DatabaseManager dm) {
		this(dm, COST_REGEXP_PATTERN_FOR_POSTGRES);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#cost(uk.ac.ox.cs.pdq.algebra.RelationalTerm)
	 */
	@Override
	public Cost cost(RelationalTerm plan) {
		return costQuery(ConjunctiveQuery.createFromLogicFormula(plan.toLogic()));
	}

	/** Calculates the execution cost of a query. 
	 * @param cq
	 * @return
	 */
	public DoubleCost costQuery(ConjunctiveQuery cq) {
		List<String> explainedQuery;
		try {
			explainedQuery = dm.executeQueryExplain(cq);
		} catch (DatabaseException e) {
			e.printStackTrace();
			return new DoubleCost(Double.MAX_VALUE);
		}
		for (String line : explainedQuery) {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(line);
			if (m.find()) {
				String cost = m.group("cost");
				Double result = Double.valueOf(cost);
				return new DoubleCost(result);
			}
		}
		return null;
	}

	@Override
	public Cost cost(Collection<AccessTerm> accesses) {
		return null;
	}

	@Override
	public OrderIndependentCostEstimator clone() {
		return this;
	}
}
