package uk.ac.ox.cs.pdq.datasources.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.builder.BuilderException;
import uk.ac.ox.cs.pdq.builder.SchemaDiscoverer;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;

// TODO: Auto-generated Javadoc
/**
 * This class factorises functionalities common to all SQLSchemaDiscorer.
 *  
 * @author Julien Leblay
 *
 */
public abstract class AbstractSQLSchemaDiscoverer implements SchemaDiscoverer {

	/**  The logger. */
	public static Logger log = Logger.getLogger(AbstractSQLSchemaDiscoverer.class);

	/** The properties. */
	protected Properties properties = null;

	/** The discovered. */
	protected Schema discovered = null;
	
	/**
	 * Sets the properties.
	 *
	 * @param p Properties
	 * @see uk.ac.ox.cs.pdq.builder.SchemaDiscoverer#setProperties(Properties)
	 */
	@Override
	public void setProperties(Properties p) {
		this.properties = p;
	}

	/**
	 * Gets the properties.
	 *
	 * @return Properties
	 * @see uk.ac.ox.cs.pdq.builder.SchemaDiscoverer#getProperties()
	 */
	@Override
	public Properties getProperties() {
		return this.properties;
	}

	/**
	 * Discover.
	 *
	 * @return the schema discovered from the given properties
	 * @throws BuilderException the builder exception
	 * @see uk.ac.ox.cs.pdq.builder.SchemaDiscoverer#discover()
	 */
	@Override
	public Schema discover() throws BuilderException {
		if (this.discovered == null) {
			String url = this.properties.getProperty("url");
			String driver = this.properties.getProperty("driver");
			String database = this.properties.getProperty("database");
			log.info("Discovering schema '" + url + database + "'... ");
			try {
//				if (driver != null) {
					Class.forName(driver);
//				}
				Collection<Relation> relations = new ArrayList<>(this.discoverRelations());
				Map<String, Relation> relationMap = new LinkedHashMap<>();
				for (Relation r: relations) {
					relationMap.put(r.getName(), r);
				}
				this.discoverForeignKeys(relationMap);
				relations.addAll(this.discoverViews(relationMap));
				this.discovered =  new Schema(relations.toArray(new Relation[relations.size()]));
			} catch (ClassNotFoundException | SQLException e) {
				throw new BuilderException("Exception thrown will discovering schema " + url, e);
			}
		}
		return this.discovered;
	}
	
	/**
	 * Gets the relations discovery statement.
	 *
	 * @param databaseName the database name
	 * @return a database-specific SQL statement to extract relation names of
	 * the given database.
	 */
	protected abstract String getRelationsDiscoveryStatement(String databaseName);
	
	/**
	 * Discover relations.
	 *
	 * @return a collection of relations stored in the underlying database
	 * @throws SQLException the SQL exception
	 * @throws ClassNotFoundException the class not found exception
	 */
	private Collection<Relation> discoverRelations() throws SQLException, ClassNotFoundException {

		Map<String, Relation> result = new TreeMap<>();
		String databaseName = this.properties.getProperty("database");

		// Fetch relations from the database
		try(Connection connection = getConnection(this.properties);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(this.getRelationsDiscoveryStatement(databaseName))) {
			if (rs.next()) {
				do {
					Relation r = this.discoverRelation(rs.getString(1));
					if (r != null) {
						result.put(r.getName(), r);
					}
				} while(rs.next());
			}
		} catch (SQLException e) {
			log.error(e);
			throw e;
		}
		
		
		return result.values();
	}
	
	/**
	 * Gets the relation discovery statement.
	 *
	 * @param relationName the relation name
	 * @return a database-specific SQL statement to extract the description of
	 * the given relation.
	 */
	protected String getRelationDiscoveryStatement(String relationName) {
		return "SELECT * FROM " + relationName + " LIMIT 1";
	}
	
	/**
	 * Gets the relation instance.
	 *
	 * @param p Properties
	 * @param relationName the relation name
	 * @param attributes the attributes
	 * @return an database-specific instance of Relation
	 */
	protected abstract Relation getRelationInstance(Properties p, String relationName, Attribute[] attributes);
	
	/**
	 * Gets the view instance.
	 *
	 * @param p Properties
	 * @param viewName String
	 * @param relationMap Map<String,Relation>
	 * @return an database-specific instance of View
	 */
	protected abstract View getViewInstance(Properties p,
			String viewName, Map<String, Relation> relationMap);
	
	/**
	 * Discover relation.
	 *
	 * @param relationName the relation name
	 * @return the relation which has the given relationName in the underlying database
	 * @throws BuilderException the builder exception
	 */
	public Relation discoverRelation(String relationName) throws BuilderException {
		try(Connection connection = getConnection(this.properties);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(this.getRelationDiscoveryStatement(relationName))) {
			ResultSetMetaData rsmd = rs.getMetaData();
			List<Attribute> attributes = new ArrayList<>();
			for (int i = 0, l = rsmd.getColumnCount(); i < l; i++) {
				Class<?> cl = Class.forName(rsmd.getColumnClassName(i + 1));
				attributes.add(Attribute.create(cl, rsmd.getColumnName(i + 1)));
			}
			Relation result = this.getRelationInstance(this.properties, relationName, attributes.toArray(new Attribute[attributes.size()]));
			return result;
		} catch (ClassNotFoundException | SQLException e) {
			log.error(e);
			throw new BuilderException(e.getMessage(), e);
		}
	}
	
	/**
	 * Gets the relation size discovery statement.
	 *
	 * @param relationName the relation name
	 * @return a database-specific SQL statement to extract relation names of
	 * the given relation.
	 */
	protected String getRelationSizeDiscoveryStatement(String relationName) {
		return "SELECT count(*) FROM " + relationName;
	}

	
	/**
	 * Discover relation size.
	 *
	 * @param relationName the relation name
	 * @return the relation which has the given relationName in the underlying database * @throws SQLException
	 * @throws SQLException the SQL exception
	 */
	public int discoverRelationSize(String relationName) throws SQLException {
		try(Connection connection = getConnection(this.properties);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(this.getRelationSizeDiscoveryStatement(relationName))) {
			if (rs.next()) {
				return rs.getInt(1);
			}
			log.warn("Relation '" + relationName + "' is empty. Could not infer attributes. Ignoring.");
		} catch (SQLException e) {
			log.error(e);
			throw e;
		}
		return 0;
	}
	
	/**
	 * Gets the foreign key discovery statement.
	 *
	 * @param relationName the relation name
	 * @return a database-specific SQL statement to extract the foreign keys of
	 * the given relation.
	 */
	protected abstract String getForeignKeyDiscoveryStatement(String relationName);

	/**
	 * Discover foreign keys.
	 *
	 * @param relationMap a map from relation names to their corresponding instances
	 * @throws BuilderException the builder exception
	 */
	public void discoverForeignKeys(Map<String, Relation> relationMap) throws BuilderException {
		for (String relationName: relationMap.keySet()) {
			try(Connection connection = getConnection(this.properties);
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(this.getForeignKeyDiscoveryStatement(relationName))) {
				Map<String, Relation> remoteRelations = new LinkedHashMap<>();
				Map<String, Collection<Reference>> refMap = new LinkedHashMap<>();
				if (rs.next()) {
					do {
						String constraintName = rs.getString(1);
						Relation localRel = relationMap.get(relationName);
						Relation remoteRel = remoteRelations.get(constraintName);
						if (remoteRel == null) {
							remoteRel = relationMap.get(rs.getString(3));
							if (remoteRel == null) {
								log.warn("Foreign key ignored, as relation '" + rs.getString(3) + "' is not defined.");
								continue;
							}
							remoteRelations.put(constraintName, remoteRel);
						}
						Collection<Reference> refs = refMap.get(constraintName);
						if (refs == null) {
							refs = new LinkedList<>();
							refMap.put(constraintName, refs);
						}
						refs.add(new Reference(
								localRel.getAttribute(localRel.getAttributePosition(rs.getString(2))),
								remoteRel.getAttribute(remoteRel.getAttributePosition(rs.getString(4)))));
					} while(rs.next());
				}
				for (String constraintName : remoteRelations.keySet()) {
					ForeignKey fk = new ForeignKey();
					fk.setForeignRelation(remoteRelations.get(constraintName));
					for (Reference ref : refMap.get(constraintName)) {
						fk.addReference(ref);
					}
					relationMap.get(relationName).addForeignKey(fk);
				}
			} catch (SQLException e) {
				log.error(e);
				throw new BuilderException(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Gets the views discovery statement.
	 *
	 * @param databaseName the database name
	 * @return a database-specific SQL statement to extract the views of
	 * the given database.
	 */
	protected abstract String getViewsDiscoveryStatement(String databaseName);
	
	/**
	 * Gets the view definition.
	 *
	 * @param viewName String
	 * @return a database-specific SQL statement to extract the description of
	 * the given view.
	 */
	protected abstract String getViewDefinition(String viewName);
	
	/**
	 * Discover views.
	 *
	 * @param relationMap Map<String,Relation>
	 * @return a collection of views stored in the underlying database
	 * @throws SQLException the SQL exception
	 */
	private Collection<View> discoverViews(Map<String, Relation> relationMap) throws SQLException {
		Collection<View> result = new LinkedHashSet<>();
		String database = this.properties.getProperty("database");
		try(Connection connection = getConnection(this.properties);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(this.getViewsDiscoveryStatement(database))) {
			if (rs.next()) {
				do {
					result.add(this.discoverView(rs.getString(1), relationMap));
				} while(rs.next());
			}
			
		} catch (SQLException e) {
			log.error(e);
			throw e;
		}
		return result;
	}
	
	/**
	 * Discover view.
	 *
	 * @param viewName the view name
	 * @param relationMap Map<String,Relation>
	 * @return the view which has the given viewName in the underlying database
	 * @throws BuilderException the builder exception
	 */
	public View discoverView(String viewName, Map<String, Relation> relationMap) throws BuilderException  {
		try(Connection connection = getConnection(this.properties);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(this.getRelationDiscoveryStatement(viewName))) {
			ResultSetMetaData rsmd = rs.getMetaData();
			List<Attribute> attributes = new ArrayList<>();
			for (int i = 0, l = rsmd.getColumnCount(); i < l; i++) {
				Class<?> cl = Class.forName(rsmd.getColumnClassName(i + 1));
				attributes.add(Attribute.create(cl, rsmd.getColumnName(i + 1)));
			}
			View result = this.getViewInstance(this.properties, viewName, relationMap);
			return result;
		} catch (ClassNotFoundException | SQLException e) {
			log.error(e);
			throw new BuilderException(e.getMessage(), e);
		}
	}

	/**
	 * Gets the connection.
	 *
	 * @param properties the properties
	 * @return a connection database connection for the given properties.
	 * @throws SQLException the SQL exception
	 */
	protected static Connection getConnection(Properties properties) throws SQLException {
		String url = properties.getProperty("url");
		String database = properties.getProperty("database");
		String username = properties.getProperty("username");
		String password = properties.getProperty("password");
		return DriverManager.getConnection(url + database, username, password);
	}
}
