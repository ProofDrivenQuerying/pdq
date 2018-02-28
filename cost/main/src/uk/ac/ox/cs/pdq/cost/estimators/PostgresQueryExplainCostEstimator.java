package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

public class PostgresQueryExplainCostEstimator implements CostEstimator {
	private static final String COST_REGEXP_PATTERN =
			"\\(cost=\\d+\\.\\d+\\.\\.(?<cost>\\d+\\.\\d+)\\s.*\\)";
	private DatabaseManager dm;

	public PostgresQueryExplainCostEstimator(DatabaseManager dm) {
		this.dm = dm;
	}
	
	@Override
	public Cost cost(RelationalTerm plan) {
		return cost(ConjunctiveQuery.create(plan.toLogic()));
	}

	@Override
	public CostEstimator clone() {
		return this;
	}

	public DoubleCost cost(ConjunctiveQuery cq) {
		List<String> explainedQuery;
		try {
			explainedQuery = dm.executeQueryExplain(cq);
		} catch (DatabaseException e) {
			e.printStackTrace();
			return new DoubleCost(Double.MAX_VALUE);
		}
		for (String line : explainedQuery) {
			Pattern p = Pattern.compile(COST_REGEXP_PATTERN);
			Matcher m = p.matcher(line);
			if (m.find()) {
				String cost = m.group("cost");
				Double result = Double.valueOf(cost);
				return new DoubleCost(result);
			}
		}
		return null;
	}
}
