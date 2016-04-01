package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;
import java.util.concurrent.Callable;

// TODO: Auto-generated Javadoc
/**
 *
 * @author Efthymia Tsamoura
 */
public class ExecuteUpdateThread implements Callable<Boolean> {

	/**  Connection to the database. */
	protected final Connection connection;
	
	protected final Queue<String> queries;

	public ExecuteUpdateThread(Queue<String> queries, Connection connection) {
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
	public Boolean call() {
		String query;
		while ((query = this.queries.poll()) != null) {
			try {
				Statement sqlStatement = this.connection.createStatement();
				sqlStatement.executeUpdate(query);
			} catch (SQLException ex) {
				if(!ex.getCause().getMessage().contains("duplicate key value")) {
					throw new IllegalStateException(ex.getMessage(), ex);
				}
			}
		}
		return true;
	}

}
