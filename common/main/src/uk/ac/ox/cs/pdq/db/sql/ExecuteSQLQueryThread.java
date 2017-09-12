package uk.ac.ox.cs.pdq.db.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Triple;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 *
 */
public class ExecuteSQLQueryThread implements Callable<List<Match>> {

	/**  Connection to the database. */
	protected final Connection connection;
	
	/** Each triple holds, 
	 * - the query or the constraint we want to detect homomorphisms for
	 * - the SQL query expression we will execute over the database
	 * - a map of projected variables **/
	protected final Queue<Triple<Formula, String, LinkedHashMap<String, Variable>>> queries;
	
	/** List of database constants **/
	//TOCOMMENT this class should not be aware of constants
	protected final Map<String, TypedConstant> constants;

	private String databaseName;

	public ExecuteSQLQueryThread(Queue<Triple<Formula, String, LinkedHashMap<String, Variable>>> queries, 
			Map<String, TypedConstant> constants,
			Connection connection, String databaseName) {
		//TODO check input arguments
		this.connection = connection;
		this.queries = queries;
		this.constants = constants;
		this.databaseName = databaseName;
	}
	
	/**
	 * Takes one query from the pool and executes it.
	 * 
	 * @return Boolean
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public List<Match> call() {
		List<Match> results = new ArrayList<Match>();
		Triple<Formula, String, LinkedHashMap<String, Variable>> entry;
		String query = null;
		while ((entry = this.queries.poll()) != null) {
			try {
				Statement sqlStatement = this.connection.createStatement();
				Formula source = entry.getLeft();
				query = entry.getMiddle();
				LinkedHashMap<String, Variable> projectedVariables = entry.getRight();
				if (databaseName!=null) {
					sqlStatement.execute("USE " + databaseName+";\n");
				}
				ResultSet resultSet = sqlStatement.executeQuery(query);
				try {
					while (resultSet.next()) {
						int f = 1;
						Map<Variable, Constant> map = new LinkedHashMap<>();
						for(Entry<String, Variable> variables:projectedVariables.entrySet()) {
							Variable variable = variables.getValue();
							String assigned = resultSet.getString(f);
							TypedConstant constant = this.constants.get(assigned);
							Constant constantTerm = constant != null ? constant : UntypedConstant.create(assigned);
							map.put(variable, constantTerm);
							f++;
						}
						results.add(Match.create(source,map));
					}
				}finally {
					if (resultSet!=null)
						resultSet.close();
				}
			} catch (SQLException e) {
				System.err.println("Error while executing: " + query);
				e.printStackTrace();
				return null;
			}
		}
		return results;
		
	}

}
