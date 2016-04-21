package uk.ac.ox.cs.pdq.materialize.homomorphism;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Triple;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.materialize.utility.Match;

import com.beust.jcommander.internal.Lists;

// TODO: Auto-generated Javadoc
/**
 *
 * Executes input SQL queries and returns the matches
 * @author Efthymia Tsamoura
 */
public class ExecuteSQLQueryThread<Q extends Evaluatable> implements Callable<List<Match>> {

	/**  Connection to the database. */
	protected final Connection connection;
	
	/** Each triple holds, 
	 * - the query or the constraint we want to detect homomorphisms for
	 * - the SQL query expression we will execute over the database
	 * - a map of projected variables **/
	protected final Queue<Triple<Q, String, LinkedHashMap<String, Variable>>> queries;
	
	/** List of database constants **/
	protected final Map<String, TypedConstant<?>> constants;

	public ExecuteSQLQueryThread(Queue<Triple<Q, String, LinkedHashMap<String, Variable>>> queries, 
			Map<String, TypedConstant<?>> constants,
			Connection connection) {
		//TODO check input arguments
		this.connection = connection;
		this.queries = queries;
		this.constants = constants;
	}
	
	/**
	 * Call.
	 *
	 * @return Boolean
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public List<Match> call() {
		
		List<Match> results = Lists.newArrayList();
		Triple<Q, String, LinkedHashMap<String, Variable>> entry;
		while ((entry = this.queries.poll()) != null) {
			try {
				Statement sqlStatement = this.connection.createStatement();
				Q source = entry.getLeft();
				String query = entry.getMiddle();
				LinkedHashMap<String, Variable> projectedVariables = entry.getRight();
				
				long start0 = System.currentTimeMillis();
				ResultSet resultSet = sqlStatement.executeQuery(query);
				long end0 = System.currentTimeMillis();
				System.out.println("Time to execute the query " + Thread.currentThread() + " " + (end0-start0));
				
				start0 = System.currentTimeMillis();
				while (resultSet.next()) {
					int f = 1;
					Map<Variable, Constant> map = new LinkedHashMap<>();
					for(Entry<String, Variable> variables:projectedVariables.entrySet()) {
						Variable variable = variables.getValue();
						String assigned = resultSet.getString(f);
						TypedConstant<?> constant = this.constants.get(assigned);
						Constant constantTerm = constant != null ? constant : new Skolem(assigned);
						map.put(variable, constantTerm);
						f++;
					}
					results.add(new Match(source,map));
				}
				end0 = System.currentTimeMillis();
				System.out.println("Time to deserialise the result " + Thread.currentThread() + " " + (end0-start0));
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		System.gc();
		return results;
	}

}
