package uk.ac.ox.cs.pdq.db.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;
import java.util.concurrent.Callable;

 /**
  * Executes sql updates
 * @author Efthymia Tsamoura
 */
public class ExecuteSynchronousSQLUpdateThread implements Callable<Boolean> {

	/**  Connection to the database. */
	protected final Connection connection;
	
	protected final Queue<String> queries;

	public ExecuteSynchronousSQLUpdateThread(Queue<String> queries, Connection connection) {
		this.connection = connection;
		this.queries = queries;
	}
	
	/**
	 *
	 * @return Boolean
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String query;
		Statement sqlStatement = null;
		try {
		while ((query = this.queries.poll()) != null) {
			try {
				sqlStatement = this.connection.createStatement();
				sqlStatement.executeUpdate(query);
			} catch (SQLException ex) {
				if(ex.getCause()==null || ex.getCause().getMessage() == null || !ex.getCause().getMessage().contains("duplicate key value")) {
					throw new IllegalStateException("Error while executing query: " + query  + ", error:" + ex.getMessage(), ex);
				} else {
					ex.printStackTrace();
				}
			}
		}
		return true;
		} finally {
			if (sqlStatement!=null)
				try {
					sqlStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

}
