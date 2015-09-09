package uk.ac.ox.cs.pdq.runtime.exec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.rewrite.sql.SQLTranslator;
import uk.ac.ox.cs.pdq.rewrite.sql.SQLTranslator.SupportedDialect;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.Semantics;
import uk.ac.ox.cs.pdq.runtime.query.SQLQueryEvaluator;
import uk.ac.ox.cs.pdq.util.BooleanResult;
import uk.ac.ox.cs.pdq.util.Result;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.eventbus.EventBus;

/**
 * Alternate executor for Linear plan, which first translates a linear plan
 * into a logical relation tree, then translates the logical tree plan into a 
 * SQL statement which is finally runs against the database.
 * 
 * Note, this executor can only be used if all the relations in a plan
 * are backed by relations in the same database instance.
 * 
 * @author Julien Leblay
 *
 */
public class SQLPlanExecutor implements PlanExecutor {

	protected final Properties properties;
	protected final Plan plan;
	protected final Query<?> query;
	protected final Semantics semantics;
	protected EventBus eventBus;
	protected Table universalTable = null;
	protected int tupleLimit = -1;
	private boolean doCache;

	/**
	 * Default constructor
	 * @param plan
	 * @param query
	 * @param properties
	 * @param sem Semantics
	 */
	public SQLPlanExecutor(Plan plan, Query<?> query, Semantics sem, Properties properties) {
		this.plan = plan;
		this.properties = properties;
		this.query = query;
		this.semantics = sem;
	}

	/**
	 * @param eventBus EventBus
	 * @see uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor#setEventBus(EventBus)
	 */
	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.runtime.plan.PlanExecutor#execute()
	 */
	@Override
	public Result execute() throws EvaluationException {
		return this.execute(ExecutionModes.DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.runtime.plan.PlanExecutor#execute(ExecutionModes mode)
	 */
	@Override
	public Result execute(ExecutionModes mode) throws EvaluationException {
		if (this.properties == null || this.properties.isEmpty()) {
			throw new EvaluationException("Unable to execute plan in SQL mode: no database properties defined.");
		}
		try (Connection connection = SQLQueryEvaluator.getConnection(this.properties)) {
			String sqlStatement =
					SQLTranslator.target(resolveDialect(this.properties))
						.toSQL(this.plan.getEffectiveOperator());
			if (0 <= this.tupleLimit && this.tupleLimit < Integer.MAX_VALUE) {
				sqlStatement = " LIMIT " + this.tupleLimit;
			}
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
	
	/**
	 * @param prop Properties
	 * @return SQLTranslator.SupportedDialect
	 */
	protected static SQLTranslator.SupportedDialect resolveDialect(Properties prop) {
		String dbUrl = prop.getProperty("url");
		if (dbUrl.contains("postgresql")) {
			return SupportedDialect.POSTGRESQL;
		}
		return SupportedDialect.SQL92;
	}

	@Override
	public void setTuplesLimit(int limit) {
		this.tupleLimit = limit;
	}

	@Override
	public void setCache(boolean doCache) {
	}
}
