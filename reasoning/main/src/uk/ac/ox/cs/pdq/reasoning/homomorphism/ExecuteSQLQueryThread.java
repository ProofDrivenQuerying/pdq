package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

// TODO: Auto-generated Javadoc
/**
 *
 * @author Efthymia Tsamoura
 */
public class ExecuteSQLQueryThread implements Callable<List<Pair<String,ResultSet>>> {

	/**  Connection to the database. */
	protected final Connection connection;
	
	protected final Queue<String> queries;

	public ExecuteSQLQueryThread(Queue<String> queries, Connection connection) {
		//TODO check input arguments
		this.connection = connection;
		this.queries = queries;
	}
	
	/**
	 * Call.
	 *
	 * @return Boolean
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public List<Pair<String,ResultSet>> call() {
		String query;
		List<Pair<String,ResultSet>> results = Lists.newArrayList();
		while ((query = this.queries.poll()) != null) {
			try {
				Statement sqlStatement = this.connection.createStatement();
				results.add(Pair.of(query, sqlStatement.executeQuery(query)));
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		return results;
	}

}
