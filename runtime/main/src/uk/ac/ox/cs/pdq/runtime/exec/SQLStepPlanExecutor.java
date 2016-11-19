package uk.ac.ox.cs.pdq.runtime.exec;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.Semantics;
import uk.ac.ox.cs.pdq.runtime.query.SQLQueryEvaluator;
import uk.ac.ox.cs.pdq.util.BooleanResult;
import uk.ac.ox.cs.pdq.util.Result;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * SQL-backed plan executor. Translates a linear plan a sequence of SQL 
 * statement, when each step of the plan materialized a table, and inputs of
 * plan step are table materialized in previous steps. 
 * 
 * Note, this executor can only be used if all the relations in a plan
 * are backed by relations in the same database instance.
 * 
 * @author Julien Leblay
 *
 */
public class SQLStepPlanExecutor extends SQLPlanExecutor {

	/**
	 * Default constructor.
	 *
	 * @param plan the plan
	 * @param q the q
	 * @param sem Semantics
	 * @param properties the properties
	 */
	public SQLStepPlanExecutor(LeftDeepPlan plan, ConjunctiveQuery q, Semantics sem, Properties properties) {
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
		LinkedHashMap<String, String> sqlStatements = null; 
//				SQLTranslator.target(resolveDialect(this.properties)
//						.toSQLSteps(this.plan);
	
		int i = 0;
		Iterator<String> aliases = sqlStatements.keySet().iterator();
		String alias = aliases.next();
		if (aliases.hasNext()) {
			do {
				String sql = sqlStatements.get(alias);
				try (
						Connection connection = SQLQueryEvaluator.getConnection(this.properties);
						Statement stmt = connection.createStatement()) {

					// Ensure a table with that name does not already exists.
					int delta = stmt.executeUpdate("DROP TABLE IF EXISTS " + alias);
					if (delta < 0) {
						throw new EvaluationException("Command #" + i + " executed with no effect.");
					}

					// Create intermediary tables
					if (aliases.hasNext()) {
						delta = stmt.executeUpdate( "CREATE TABLE " + alias + " AS (" + sql + ")");
						if (delta < 0) {
							throw new EvaluationException("Command #" + i + " executed with no effect.");
						}
					}
				} catch (SQLException e) {
					throw new EvaluationException("Command #" + i + " - " + e.getMessage() + "\n" + sql, e);
				}
				alias = aliases.next();
				i++;
			} while (aliases.hasNext());

			// Retrieve the final result
			RelationalOperator lastCommand = this.plan.getEffectiveOperator();
			try (Connection connection = SQLQueryEvaluator.getConnection(this.properties)) {
				SQLQueryEvaluator evaluator = SQLQueryEvaluator.newEvaluator(connection);
				evaluator.setEventBus(this.eventBus);
				this.universalTable = evaluator.evaluate(
						sqlStatements.get(alias), 
						Utility.termsToTyped(this.query.getFree(), lastCommand.getType()));
			} catch (SQLException e) {
				throw new EvaluationException(e);
			}
		}
	
		if (this.query.isBoolean()) {
			return new BooleanResult(!this.universalTable.isEmpty());
		}
		
		this.cleanUp(sqlStatements.keySet());
		
		this.universalTable.setHeader(Utility.termsToAttributes(this.query.getFree(), this.universalTable.getType()));
		return this.universalTable;
	}

	/**
	 * Delete temporary table created through an execution (and given as argument).
	 *
	 * @param tables Set<String>
	 * @throws EvaluationException the evaluation exception
	 */
	public void cleanUp(Set<String> tables) throws EvaluationException {
		try (Connection connection = SQLQueryEvaluator.getConnection(this.properties);
				Statement stmt = connection.createStatement()) {

			for (String table: tables) {
				// Ensure a table with that name does not already exists.
				int delta = stmt.executeUpdate("DROP TABLE IF EXISTS " + table);
				if (delta < 0) {
					throw new EvaluationException("Table " + table + " could not be cleaned up.");
				}
			}
		} catch (SQLException e) {
			throw new EvaluationException(e);
		}
	}
}
