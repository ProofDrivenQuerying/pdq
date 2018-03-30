package uk.ac.ox.cs.pdq.datasources.sql;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.datasources.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Wrapper for SQL relations. This class inherits Relation and add functionality
 * to retrieve data from the underlying database tables.
 * 
 * @author Efi Tsamoura
 * @author Julien Leblay
 */
public class SQLRelationWrapper extends Relation implements RelationAccessWrapper {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3167783211904676965L;

	private static Logger log = Logger.getLogger(SQLRelationWrapper.class);

	private Connection connection = null;

	/**
	 * Instantiates a new SQL relation wrapper.
	 *
	 * @param properties the properties
	 * @param relation Relation
	 */
	public SQLRelationWrapper(Properties properties, Relation relation) {
		this(properties, relation.getName(), relation.getAttributes(), relation.getAccessMethods());
	}

	/**
	 * Instantiates a new SQL relation wrapper.
	 *
	 * @param properties the properties
	 * @param name String
	 * @param attributes List<Attribute>
	 */
	public SQLRelationWrapper(Properties properties, String name, Attribute[] attributes) {
		this(properties, name, attributes, null);
	}

	/**
	 * Instantiates a new SQL relation wrapper.
	 *
	 * @param properties the properties
	 * @param name String
	 * @param attributes List<Attribute>
	 * @param methods List<AccessMethod>
	 */
	public SQLRelationWrapper(Properties properties, String name, Attribute[] attributes, AccessMethod[] methods) {
		super(name, attributes, methods);
		this.properties.putAll(properties);
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
			String sep = " WHERE (";
			for (Attribute a: sourceAttributes) {
				result.append(sep).append(a.getName());
				sep = ",";
			}
			result.append(") IN ");
			sep = "(";

			Set<Tuple> cache = new LinkedHashSet<>();
			while (inputTuples.hasNext()) {
				Tuple tuple = inputTuples.next();
				if (!cache.contains(tuple)) {
					char sep2 = '(';
					result.append(sep);
					for (int i = 0, l = tuple.size(); i < l; i++) {
						if (Utility.isNumeric(sourceAttributes[i].getType())) {
							result.append(sep2).append((Object) tuple.getValue(i));
						} else {
							result.append(sep2).append('\'').append((Object) tuple.getValue(i)).append('\'');
						}
						sep2 = ',';
					}
					result.append(')');
				}
				cache.add(tuple);
				sep = ",";
			}
			result.append(')');
		}

		return result.toString();
	}

	/**
	 * Select clause.
	 *
	 * @return the select clause of the SQL statement
	 */
	private String selectClause() {
		return "SELECT " + Joiner.on(",").join(this.attributes);
	}

	/**
	 * Fetches the data from the underlying table from an input SQL query.
	 *
	 * @param queryString the query string
	 * @return Table
	 * @see uk.ac.ox.cs.pdq.datasources.memory.RelationAccessWrapper#access(Table)
	 */
	private Table fetchTuples(String queryString) {
		Table result = new Table(this.getAttributes());
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
	 *
	 * @return Table
	 * @see uk.ac.ox.cs.pdq.datasources.memory.RelationAccessWrapper#access(Table)
	 */
	@Override
	public Table access() {
		return fetchTuples(this.selectClause() + " FROM " + this.getName());
	}
	
	/**
	 *
	 * @param inputAttributes the input attributes
	 * @param inputs the inputs
	 * @return Table
	 * @see uk.ac.ox.cs.pdq.datasources.memory.RelationAccessWrapper#access(Table)
	 */
	@Override
	public Table access(
			Attribute[] inputAttributes,
			ResetableIterator<Tuple> inputs) {
		return fetchTuples(this.selectClause() + " FROM " + this.getName()
				+ this.whereClause(inputAttributes, inputs));
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.util.Pipelineable#iterator(java.util.List, uk.ac.ox.cs.pdq.util.ResetableIterator)
	 */
	@Override
	public ResetableIterator<Tuple> iterator(
			Attribute[] inputAttributes,
			ResetableIterator<Tuple> inputs) {
		return this.access(inputAttributes, inputs).iterator();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.util.Pipelineable#iterator()
	 */
	@Override
	public ResetableIterator<Tuple> iterator() {
		return this.access().iterator();
	}

	/**
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
