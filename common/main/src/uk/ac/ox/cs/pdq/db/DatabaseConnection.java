package uk.ac.ox.cs.pdq.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.DatabaseInstance.HomomorphismDetectorTypes;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismException;
import uk.ac.ox.cs.pdq.db.sql.DerbyStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.MySQLStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.io.xml.QNames;
/**
 * 
 * @author george
 *
 */
public class DatabaseConnection {
	public final int synchronousThreadsNumber = 1;

	private boolean isInitialized = false;
	
	/**  Open database connections. */
	protected List<Connection> synchronousConnections = Lists.newArrayList();

	public List<Connection> getSynchronousConnections() {
		return synchronousConnections;
	}
	/** Map schema relation to database tables. */
	private Map<String, DatabaseRelation> relationNamesToRelationObjects = null;
	
	/**  Creates SQL statements to detect homomorphisms or add/delete facts in a database. */
	private SQLStatementBuilder builder = null;
	
	public SQLStatementBuilder getSQLStatementBuilder() {
		return builder;
	}

	private ReasoningParameters resParams;

	private String database;

	private ArrayList<Relation> relations;

	private Set<Evaluatable> constraints = null;
	
	private static Integer counter = 0;
	
	private Set<String> constraintIndices =  new LinkedHashSet<String>();

	private Schema schema;

	public Schema getSchema() {
		return schema;
	}


	public DatabaseConnection(ReasoningParameters reasoningParams, Schema schema) throws SQLException {

		
		
		HomomorphismDetectorTypes type =reasoningParams.getHomomorphismDetectorType(); 
		String driver = reasoningParams.getDatabaseDriver();
		String url = reasoningParams.getConnectionUrl();
		database = reasoningParams.getDatabaseName(); 
		String username = reasoningParams.getDatabaseUser();
		String password = reasoningParams.getDatabasePassword();
		
		if (type != null && type == HomomorphismDetectorTypes.DATABASE) {
			if (url != null && url.contains("mysql")) {
				builder = new MySQLStatementBuilder();
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
				builder = new DerbyStatementBuilder();
			}
			
			this.constraints = Sets.newLinkedHashSet();
			for (Dependency<?,?> dependency: schema.getDependencies()) {
				this.constraints.add(dependency);
			}
			
			
			this.relations = Lists.newArrayList(schema.getRelations());
			this.schema = schema;
			this.relationNamesToRelationObjects = new LinkedHashMap<>();

			
			for(int j=0; j<=synchronousThreadsNumber; j++)
			{
				this.synchronousConnections.add(DatabaseInstance.getConnection(driver, url, database, username, password));
			}
			
			this.resParams = reasoningParams;
			this.relationNamesToRelationObjects = new LinkedHashMap<>();
			initialize();
		}
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

			for (String sql: this.builder.createDatabaseStatements(database)) {
				sqlStatement.addBatch(sql);
			}

			//if(this.RelationNamesToRelationObjects.get(QNames.EQUALITY.toString()) == null)
			this.relationNamesToRelationObjects.put(QNames.EQUALITY.toString(), DatabaseRelation.DatabaseEqualityRelation);
			sqlStatement.addBatch(this.builder.createTableStatement(DatabaseRelation.DatabaseEqualityRelation));
			sqlStatement.addBatch(this.builder.createColumnIndexStatement(DatabaseRelation.DatabaseEqualityRelation, DatabaseRelation.Fact));

			//Put relations into a set so as to make them unique
			Set<Relation> relationset = new HashSet<Relation>();
			relationset.addAll(this.relations);
			this.relations.clear();
			this.relations.addAll(relationset);

			//Create the database tables and create column indices
			for (Relation relation:this.relations) {
				DatabaseRelation dbRelation = DatabaseRelation.createDatabaseRelation(relation);
				this.relationNamesToRelationObjects.put(relation.getName(), dbRelation);
				sqlStatement.addBatch(this.builder.createTableStatement(dbRelation));
				sqlStatement.addBatch(this.builder.createColumnIndexStatement(dbRelation, DatabaseRelation.Fact));
			}

			//Create indices for the joins in the body of the dependencies
			Set<String> joinIndexes = Sets.newLinkedHashSet();
			for (Evaluatable constraint:this.constraints) {
				joinIndexes.addAll(this.builder.setupIndices(false, this.relationNamesToRelationObjects, constraint, this.constraintIndices).getLeft());
			}
			for (String b: joinIndexes) {
				sqlStatement.addBatch(b);
			}

			sqlStatement.executeBatch();
	}

	/**
	 * Cleans up the database.
	 *
	 * @throws HomomorphismException the homomorphism exception
	 */
	protected void dropDatabase() throws HomomorphismException {
		try {
			Statement sqlStatement = this.synchronousConnections.get(0).createStatement();
			//Statement sqlStatement = this.synchronousConnections.createStatement();

			for (String sql: this.builder.createDropStatements(database)) {
				sqlStatement.addBatch(sql);
			}
			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new HomomorphismException(ex.getMessage(), ex);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
//	@Override
	public void close() throws Exception {
		this.dropDatabase();
		for(Connection con:this.synchronousConnections) {
			con.close();
		}
	}

	@Override
	public DatabaseConnection clone() {
		try {
			DatabaseConnection cloneCon = new DatabaseConnection(resParams,schema);
			cloneCon.isInitialized = this.isInitialized;
			return cloneCon;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, DatabaseRelation> getRelationNamesToRelationObjects() {
		return relationNamesToRelationObjects;
	}
}
