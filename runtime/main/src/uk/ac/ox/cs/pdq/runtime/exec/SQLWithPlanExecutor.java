package uk.ac.ox.cs.pdq.runtime.exec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.rewrite.sql.SQLTranslator;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.Semantics;
import uk.ac.ox.cs.pdq.runtime.query.SQLQueryEvaluator;
import uk.ac.ox.cs.pdq.util.BooleanResult;
import uk.ac.ox.cs.pdq.util.Result;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * SQL-backed plan executor. Translates a linear plan a single SQL WITH 
 * statement, and runs in against the database. 
 * 
 * Note, this executor can only be used if all the relations in a plan
 * are backed by relations in the same database instance.
 * 
 * @author Julien Leblay
 *
 */
public class SQLWithPlanExecutor extends SQLPlanExecutor {

	/**
	 * Default constructor.
	 * @param plan
	 * @param q
	 * @param properties
	 * @param sem Semantics
	 */
	public SQLWithPlanExecutor(Plan plan, Query<?> q, Semantics sem, Properties properties) {
		super(plan, q, sem, properties);
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.SQLPlanExecutor#execute()
	 */
	@Override
	public Result execute() throws EvaluationException {
		return this.execute(ExecutionModes.DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.SQLPlanExecutor#execute(uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor.ExecutionModes)
	 */
	@Override
	public Result execute(ExecutionModes mode) throws EvaluationException {
		if (this.properties == null || this.properties.isEmpty()) {
			throw new EvaluationException("Unable to execute plan in SQL mode: no database properties defined.");
		}
		// Retrieve the final result
		try (Connection connection = SQLQueryEvaluator.getConnection(this.properties)) {
			String sqlStatement = 
					SQLTranslator.target(SQLPlanExecutor.resolveDialect(this.properties))
						.toSQLWith(this.plan.getEffectiveOperator());
			SQLQueryEvaluator evaluator = SQLQueryEvaluator.newEvaluator(connection);
			evaluator.setEventBus(this.eventBus);
			this.universalTable = evaluator.evaluate(sqlStatement, this.plan.getOutputAttributes());
			if (this.semantics == Semantics.SET) {
				this.universalTable.removeDuplicates();
			}
		} catch (UnsupportedOperationException e) {
			throw new EvaluationException("Check that plan language is compatible with execution model.");
		} catch (RewriterException | SQLException e) {
			throw new EvaluationException(e);
		}
		if (this.query.isBoolean()) {
			return new BooleanResult(!this.universalTable.isEmpty());
		}
		this.universalTable.setHeader(Utility.termsToAttributes(this.query.getFree(), this.universalTable.getType()));
		return this.universalTable;
	}
}
