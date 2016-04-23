package uk.ac.ox.cs.pdq.workloadgen.database;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.cost.statistics.Histogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerBucket;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogram;
import uk.ac.ox.cs.pdq.workloadgen.query.View;
import uk.ac.ox.cs.pdq.workloadgen.schema.Attribute;
import uk.ac.ox.cs.pdq.workloadgen.schema.Table;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

public class SQLServerManager extends DatabaseManager{

	/**
	 * 
	 * @param driver
	 * 		Database driver
	 * @param url
	 * 		Database url
	 * @param database
	 * 		Database name
	 * @param username
	 * 		Database user
	 * @param password
	 * 		Database pass
	 * @param builder
	 * 		Builds SQL queries that detect homomorphisms
	 * @param schema
	 * 		Input schema
	 * @param query
	 * 		Input query
	 * @throws SQLException
	 */
	public SQLServerManager(
			String driver, 
			String url, 
			String database,
			String username, 
			String password
			) throws SQLException {
		super(driver, url, database, username, password);
	}

	/**
	 * 
	 * @param schemaName
	 * @param table
	 * @return
	 * 		the DBMS statistics for the input table
	 */
	@Override
	public Map<Attribute, String> getStatistics(String schemaName, Table table) {
		String statement = "SELECT  [s].[object_id]," +
				"[s].[name]," +
				"[s].[auto_created]," +
				"COL_NAME([s].[object_id], [sc].[column_id]) AS [col_name]" +
				"FROM    sys.[stats] AS s" +
				"INNER JOIN sys.[stats_columns] AS [sc]" +
				"        ON [s].[stats_id] = [sc].[stats_id] AND" +
				"[s].[object_id] = [sc].[object_id]" +
				"WHERE   [s].[object_id] = OBJECT_ID(\'" + schemaName + "." + table.getName() + "\');";

		try(Statement sqlStatement = connection.createStatement();
				ResultSet resultSet = sqlStatement.executeQuery(statement)) {
			Map<Attribute, String> staMap = Maps.newHashMap();
			while (resultSet.next()) {
				String statistics = resultSet.getString("name");
				String attributeName = resultSet.getString("col_name");
				Attribute attribute = table.getAttribute(attributeName);
				Preconditions.checkNotNull(attribute);	
				staMap.put(attribute, statistics);
			}
			return staMap;
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	/**
	 * 
	 * @param schemaName
	 * @param view
	 * @return
	 * 		the DBMS statistics for the input view
	 */
	@Override
	public Map<Attribute, String> getStatistics(String schemaName, View table) {
		String statement = "SELECT  [s].[object_id]," +
				"[s].[name]," +
				"[s].[auto_created]," +
				"COL_NAME([s].[object_id], [sc].[column_id]) AS [col_name]" +
				"FROM    sys.[stats] AS s" +
				"INNER JOIN sys.[stats_columns] AS [sc]" +
				"        ON [s].[stats_id] = [sc].[stats_id] AND" +
				"[s].[object_id] = [sc].[object_id]" +
				"WHERE   [s].[object_id] = OBJECT_ID(\'" + schemaName + "." + table.getName() + "\');";

		try(Statement sqlStatement = connection.createStatement();
				ResultSet resultSet = sqlStatement.executeQuery(statement)) {
			Map<Attribute, String> staMap = Maps.newHashMap();
			while (resultSet.next()) {
				String statistics = resultSet.getString("name");
				String attributeName = resultSet.getString("col_name");
				Attribute attribute = table.getAttribute(attributeName);
				Preconditions.checkNotNull(attribute);	
				staMap.put(attribute, statistics);
			}
			return staMap;
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	/**
	 * 
	 * @param schemaName
	 * @param table
	 * @param statistic
	 * @return
	 * 		the histogram associated with the input table statistic
	 */
	@Override
	public Histogram getHistogram(String schemaName, Table table, Attribute attribute, String statistic) {
		return this.getHistogram(schemaName, table.getName(), attribute, statistic);
	}

	/**
	 * 
	 * @param schemaName
	 * @param view
	 * @param statistic
	 * @return
	 * 		the histogram associated with the input view statistic
	 */
	@Override
	public Histogram getHistogram(String schemaName, View view, Attribute attribute, String statistic) {
		return this.getHistogram(schemaName, view.getName(), attribute, statistic);
	}

	/**
	 * 
	 * @param schemaName
	 * @param table
	 * @param attribute
	 * @param statistic
	 * @return
	 */
	private Histogram getHistogram(String schemaName, String table, Attribute attribute, String statistic) {
		String statement = "DBCC SHOW_STATISTICS (\"" + schemaName + "." + table + "\", \'" + statistic + "\') WITH HISTOGRAM;";
		try(Statement sqlStatement = connection.createStatement();
				ResultSet resultSet = sqlStatement.executeQuery(statement)) {

			List<SQLServerBucket> buckets = Lists.newArrayList();
			while (resultSet.next()) {
				Object range_hi_key = Attribute.cast(attribute.getType(), resultSet.getString("RANGE_HI_KEY"));
				BigInteger range_rows;
				range_rows = new BigDecimal(resultSet.getString("RANGE_ROWS")).toBigInteger();
				BigInteger eq_rows = new BigDecimal(resultSet.getString("EQ_ROWS")).toBigInteger();
				BigInteger distinct_range_rows = new BigDecimal(resultSet.getString("DISTINCT_RANGE_ROWS")).toBigInteger();
				double avg_range_rows = new Double(resultSet.getString("AVG_RANGE_ROWS")); 
				SQLServerBucket bucket = new SQLServerBucket(range_hi_key, range_rows, eq_rows, distinct_range_rows, avg_range_rows);
				buckets.add(bucket);
			}
			return new SQLServerHistogram(buckets);
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	@Override
	public void createView(String schemaName, View view) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ps.print("SELECT DISTINCT ");
		Iterator<Attribute> iterAttrs = view.getSelectClause().iterator();
		while (iterAttrs.hasNext()) {
			ps.print(iterAttrs.next().getFullName());
			if (iterAttrs.hasNext())
				ps.print(", ");
		}
		ps.println();
		ps.println("INTO " + view.getName());
		ps.print("FROM ");
		Iterator<Table> iterTables = view.getFromClause().iterator();
		while (iterTables.hasNext()) {
			ps.print(schemaName + "." + iterTables.next().getName());
			if (iterTables.hasNext())
				ps.print(", ");
		}
		ps.println();

		if(view.getWhereClause().getFilterPredicates().size() > 0 || view.getWhereClause().getJoinPredicates().size() > 0) {
			ps.print("WHERE ");
			ps.println(view.getWhereClause().toString());
			ps.println();
		}

		try(Statement sqlStatement = this.connection.createStatement()) {
			sqlStatement.addBatch(baos.toString());
			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}				
	}

	@Override
	public void dropView(View view) {
		String command = "DROP TABLE " + view.getName();
		try(Statement sqlStatement = this.connection.createStatement()) {
			sqlStatement.addBatch(command);
			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	@Override
	public String createStatistics(String schemaName, View view, List<Attribute> attributes) {
		//Create statistics on the input view
		try(Statement sqlStatement = this.connection.createStatement()) {
			List<String> attributeNames = Lists.newArrayList();
			for(Attribute attribute:attributes) {
				attributeNames.add(attribute.getName());
			}

			String statistics = "_S" + view.getName() + "_" + Joiner.on("_").join(attributeNames); 
			String clustIndexStm = "CREATE STATISTICS " + statistics + " ON" + " " + schemaName+"."+view.getName()+"("+ Joiner.on(",").join(attributeNames)  +")" + 
					"\nWITH SAMPLE 35 PERCENT;";
			sqlStatement.addBatch(clustIndexStm);
			sqlStatement.executeBatch();
			return statistics;
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}

	}

}
