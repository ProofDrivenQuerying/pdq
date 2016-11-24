package uk.ac.ox.cs.pdq.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import uk.ac.ox.cs.pdq.db.sql.DerbyStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.MySQLStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.SQLStatementBuilder;
/**
 * Models a database connection. It is responsible for creating database tables for the input schema.
 * 
 * @author george
 *
 */
public class DatabaseConnection implements AutoCloseable{
	public final int synchronousThreadsNumber = 1;

	private boolean isInitialized = false;
	
	/**  Open database connections. */
	protected List<Connection> synchronousConnections = Lists.newArrayList();

	public List<Connection> getSynchronousConnections() {
		return synchronousConnections;
	}
	/** Map schema relation to database tables. */
	private Map<String, Relation> relationNamesToRelationObjects = null;
	
	/**  Creates SQL statements to detect homomorphisms or add/delete facts in a database. */
	private SQLStatementBuilder builder = null;
	
	public SQLStatementBuilder getSQLStatementBuilder() {
		return getBuilder();
	}

	private DatabaseParameters dbParams;

	private String database;

	private ArrayList<Relation> relations;

	private static Integer counter = 0;
	
	private Schema schema;

	public Schema getSchema() {
		return schema;
	}


	public DatabaseConnection(DatabaseParameters dbParams, Schema schema) throws SQLException {
		String driver = dbParams.getDatabaseDriver();
		String url = dbParams.getConnectionUrl();
		database = dbParams.getDatabaseName(); 
		String username = dbParams.getDatabaseUser();
		String password = dbParams.getDatabasePassword();

		if (url != null && url.contains("mysql")) {
			setBuilder(new MySQLStatementBuilder());
		} else {
			if (Strings.isNullOrEmpty(driver)) {
				driver = "org.apache.derby.jdbc.EmbeddedDriver";
			}
			if (Strings.isNullOrEmpty(url)) {
				url = "jdbc:derby:memory:{1};create=true";
			}
			if (Strings.isNullOrEmpty(database)) {
				database = "chase";
			}
			database +=  "_" + System.currentTimeMillis() + "_" + counter++;
			synchronized (counter) {
				username = "APP_" + (counter++);
			}
			password = "";
			setBuilder(new DerbyStatementBuilder());
		}

		this.relations = Lists.newArrayList(schema.getRelations());
		this.schema = schema;


		for(int j=0; j<=synchronousThreadsNumber; j++)
		{
			this.synchronousConnections.add(DatabaseInstance.getConnection(driver, url, database, username, password));
		}

		this.dbParams = dbParams;
		this.relationNamesToRelationObjects = new LinkedHashMap<>();
		initialize();
	}



	public void initialize() throws SQLException {
		if (!this.isInitialized) {
			this.setup();
			this.isInitialized = true;
		}
	}
	
	/**
	 * Sets up the database that will store the facts.
	 * @throws SQLException 
	 */
	protected void setup() throws SQLException {
			Statement sqlStatement = this.synchronousConnections.get(0).createStatement();

			for (String sql: this.getBuilder().createDatabaseStatements(database)) {
				sqlStatement.addBatch(sql);
			}

			//Put relations into a set so as to make them unique
			Set<Relation> relationset = new HashSet<Relation>();
			relationset.addAll(this.relations);
			this.relations.clear();
			this.relations.addAll(relationset);

			//Create the database tables and create column indices
			for (Relation relation:this.relations) {
				Relation dbRelation = this.createDatabaseRelation(relation);
				this.relationNamesToRelationObjects.put(relation.getName(), dbRelation);
				sqlStatement.addBatch(this.getBuilder().createTableStatement(dbRelation));
				sqlStatement.addBatch(this.getBuilder().createColumnIndexStatement(dbRelation, dbRelation.getAttribute(dbRelation.getArity()-1)));
			}
			sqlStatement.executeBatch();
	}
	
	/**
	 * Creates the db relation. Currently codes in the position numbers into the names, but this should change
	 *
	 * @param relation the relation
	 * @return a new database relation with attributes x0,x1,...,x_{N-1}, Fact where
	 *         x_i maps to the i-th relation's attribute
	 */
	private Relation createDatabaseRelation(Relation relation) {
		/** The attr prefix. THIS SHOULD DISAPPEAR */
		String attrPrefix = "x";
		/** A FactID attribute. THIS SHOULD DISAPPEAR */
		Attribute Fact = new Attribute(Integer.class, "Fact");
		List<Attribute> attributes = new ArrayList<>();
		for (int index = 0, l = relation.getAttributes().size(); index < l; ++index) {
			attributes.add(new Attribute(String.class, attrPrefix + index));
		}
		attributes.add(Fact);
		return new Relation(relation.getName(), attributes, relation.isEquality()){};
	}

	/**
	 * Cleans up the database.
	 *
	 * @throws HomomorphismException the homomorphism exception
	 * @throws SQLException 
	 */
	protected void dropDatabase() throws SQLException {
			Statement sqlStatement = this.synchronousConnections.get(0).createStatement();
			//Statement sqlStatement = this.synchronousConnections.createStatement();

			for (String sql: this.getBuilder().createDropStatements(database)) {
				sqlStatement.addBatch(sql);
			}
			sqlStatement.executeBatch();
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		this.dropDatabase();
		for(Connection con:this.synchronousConnections) {
			con.close();
		}
	}

	@Override
	public DatabaseConnection clone() {
		try {
			DatabaseConnection cloneCon = new DatabaseConnection(dbParams,schema);
			cloneCon.isInitialized = this.isInitialized;
			return cloneCon;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Map from relation names to the main memory objects existing for these names. 
	 * TOCOMMENT: See issue 168
	 */
	public Map<String, Relation> getRelationNamesToRelationObjects() {
		return relationNamesToRelationObjects;
	}


	public SQLStatementBuilder getBuilder() {
		return builder;
	}


	public void setBuilder(SQLStatementBuilder builder) {
		this.builder = builder;
	}

}
