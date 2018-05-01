package uk.ac.ox.cs.pdq.datasources.sql;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.DistinctIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * An access method providing access to relation in an SQL database. 
 * 
 * @author Tim Hobson
 * @author Efi Tsamoura
 * @author Julien Leblay
 * 
 */
public class DatabaseAccessMethod extends ExecutableAccessMethod {

	private static final long serialVersionUID = 8271122449201089556L;

	/** Database connection. */
	private Connection connection = null;

	/** Logger. */
	private static Logger log = Logger.getLogger(DatabaseAccessMethod.class);

	private Properties properties;
	
	public DatabaseAccessMethod(Relation relation, Properties properties) {
		super(relation.getAttributes(),new Integer[] {},relation, getDefaultMapping(relation));
		this.properties = properties;
	}

	public DatabaseAccessMethod(Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping, Properties properties) {
		super(attributes, inputs, relation, attributeMapping);
		this.properties = properties;
	}

	public DatabaseAccessMethod(String name, Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping, Properties properties) {
		super(name, attributes, inputs, relation, attributeMapping);
		this.properties = properties;
	}

	public DatabaseAccessMethod(Attribute[] attributes, Set<Attribute> inputAttributes,
			Relation relation, Map<Attribute, Attribute> attributeMapping, Properties properties) {
		super(attributes, inputAttributes, relation, attributeMapping);
		this.properties = properties;
	}

	public DatabaseAccessMethod(String name, Attribute[] attributes, Set<Attribute> inputAttributes,
			Relation relation, Map<Attribute, Attribute> attributeMapping, Properties properties) {
		super(name, attributes, inputAttributes, relation, attributeMapping);
		this.properties = properties;
	}

	@Override
	protected Stream<Tuple> fetchTuples(Iterator<Tuple> inputTuples) {

		String queryString = this.queryString(this.inputAttributes(false), inputTuples);
		return StreamSupport.stream(this.fetchTuples(queryString).spliterator(), false);
	}

	public String queryString(Attribute[] inputAttributes, Iterator<Tuple> inputTuples) {
		if (inputTuples != null && !(inputTuples instanceof DistinctIterator))
			inputTuples = new DistinctIterator<Tuple>(inputTuples);
		String ret = this.selectClause();
		if (inputAttributes == null || inputTuples == null)
			return ret;
		return ret + this.whereClause(inputAttributes, inputTuples);
	}

	/**
	 * Where clause.
	 *
	 * @param sourceAttributes the source attributes
	 * @param inputTuples the input tuples
	 * @return the where clause of the SQL statement for the given tuple
	 */
	private String whereClause(Attribute[] sourceAttributes, Iterator<Tuple> inputTuples) {
		Preconditions.checkArgument(sourceAttributes != null);
		Preconditions.checkArgument(inputTuples != null);

		StringBuilder result = new StringBuilder();
		if (inputTuples.hasNext()) {
			result.append(" WHERE ");
			Iterator<String> predicates = this.wherePredicates(sourceAttributes, inputTuples).iterator(); 
			while (predicates.hasNext()) {
				result.append(predicates.next());
				// Between predicates the condition is OR.
				if (predicates.hasNext())
					result.append(" OR ");
			}
		}
		return result.toString();
	}

	/**
	 * Where predicates.
	 *
	 * The returned vector of predicates has one element for each of the {@code inputTuples}.
	 * Each predicate contains one term for each source attribute
	 *
	 * @param sourceAttributes the source attributes
	 * @param inputTuples the input tuples
	 * @return a vector of predicates.
	 */
	private List<String> wherePredicates(Attribute[] sourceAttributes, Iterator<Tuple> inputTuples) {

		List<String> ret = new ArrayList<String>();
		char apostrophe = '\'';

		while(inputTuples.hasNext()) {
			StringBuilder strBuilder = new StringBuilder();
			Tuple tuple = inputTuples.next();
			Preconditions.checkArgument(tuple.size() == sourceAttributes.length);
			
			for (int i = 0; i != sourceAttributes.length; i++) {
				Attribute attr = sourceAttributes[i];
				Preconditions.checkArgument(attr.getType().equals(tuple.getType().getType(i)));
				
				strBuilder.append(attr.getName()).append("=");
				boolean isNumeric = Utility.isNumeric(tuple.getType().getType(i)); 
				if (!isNumeric)
					strBuilder.append(apostrophe);
				strBuilder.append((Object) tuple.getValue(i));
				if (!isNumeric)
					strBuilder.append(apostrophe);
				
				// Between attributes the condition is AND. 
				if (i != sourceAttributes.length - 1)
					strBuilder.append(" AND ");
			}
			ret.add(strBuilder.toString());
		}
		return ret;
	}

	/**
	 * Select clause.
	 *
	 * @return the select clause of the SQL statement
	 */
	private String selectClause() {
		return "SELECT " + Joiner.on(",").join(this.outputAttributes(false)) 
				+ " FROM " + this.getName();
	}

	/**
	 * Fetches the data from the underlying table from an input SQL query.
	 *
	 * @param queryString the query string
	 * @return Table
	 * @see uk.ac.ox.cs.pdq.datasources.memory.RelationAccessWrapper#access(Table)
	 */
	private Table fetchTuples(String queryString) {
		Table result = new Table(this.outputAttributes(false));
		try(Connection conn = this.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(queryString)) {
			while (rs.next()) {
				Object[] ndata = new Object[result.columns()];
				for (int index = 0; index < ndata.length; ++index) {
					Type columnType = result.getType().getType(index);
					if (columnType == Integer.class) {
						ndata[index] = new Integer(rs.getInt(index + 1));

					} else if (columnType == String.class) {
						ndata[index] = rs.getString(index + 1).trim();

					} else {
						Method m = ResultSet.class.getMethod("get" + Utility.simpleName(columnType), int.class);
						ndata[index] = m.invoke(rs, index + 1);
					}
				}
				result.appendRow(result.getType().createTuple(ndata));
			}
		} catch (SQLException | ReflectiveOperationException e) {
			log.warn(queryString, e);
			throw new AccessException(this.getName() + "\n" + queryString, e);
		}
		return result;
	}

	/**
	 * Gets the connection.
	 *
	 * @return a connection database connection for the given properties.
	 * @throws SQLException the SQL exception
	 */
	public Connection getConnection() throws SQLException {
		if (this.connection == null || this.connection.isClosed()) {
			String url = this.properties.getProperty("url");
			String database = this.properties.getProperty("database");
			String username = this.properties.getProperty("username");
			String password = this.properties.getProperty("password");
			this.connection = DriverManager.getConnection(url + database, username, password);
			//			this.prepareStatements(this.connection);
		}
		return this.connection;
	}
}
