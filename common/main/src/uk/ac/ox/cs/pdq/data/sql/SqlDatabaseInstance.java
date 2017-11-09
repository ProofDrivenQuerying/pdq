package uk.ac.ox.cs.pdq.data.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import uk.ac.ox.cs.pdq.data.PhysicalDatabaseInstance;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Represents a physical database that speaks SQL. Such as Derby, MySQL or
 * PostgresSQL.
 * 
 * @author Gabor
 *
 */
public abstract class SqlDatabaseInstance extends PhysicalDatabaseInstance {
	protected SQLDatabaseConnection connection;
	protected DatabaseParameters databaseParameters;
	/** Aliases for the relations in the query FROM statements. */
	public BiMap<Atom, String> aliases = HashBiMap.create();

	/** The alias prefix. */
	public static final String ALIAS_PREFIX = "A";
	protected Schema schema;
	
	protected SqlDatabaseInstance(DatabaseParameters parameters) {
		this.databaseParameters = parameters;
	}

	@Override
	protected synchronized void initialiseConnections(DatabaseParameters parameters) throws DatabaseException {
		try {
			if (this.connection == null) {
				this.connection = createConnection(parameters);
			}
		}catch(Throwable t) {
			throw new DatabaseException("Exception while creating connection!" + parameters, t);
		}
	}

	private static SQLDatabaseConnection createConnection(DatabaseParameters parameters) throws SQLException {
		return new SQLDatabaseConnection(parameters); 
	}

	@Override
	protected void closeConnections(boolean dropDatabase) {
		connection.close();
	}

	@Override
	protected void initialiseDatabaseForSchema(Schema schema) throws DatabaseException {
		this.schema = schema;
		List<String> commandBuffer = new ArrayList<String>();
		try {
			for (String sql : createDatabaseStatements(databaseParameters.getDatabaseName())) {
				commandBuffer.add(sql);
			}
			// Create the database tables and create column indices
			for (Relation relation : schema.getRelations()) {
				String command = createTableStatement(relation);
				commandBuffer.add(command);
			}
			this.connection.executeStatements(commandBuffer);
		} catch (Throwable t) {
			t.printStackTrace();
			if (t instanceof java.sql.BatchUpdateException) {
				if (((java.sql.BatchUpdateException)t).getNextException() !=null)
					((java.sql.BatchUpdateException)t).getNextException().printStackTrace();
			}
			throw new DatabaseException("DB init failed.", t);
		}
	}
	protected abstract Collection<String> createDatabaseStatements(String databaseName);

	@Override
	protected void dropDatabase() {
	}

	@Override
	protected void addFacts(Collection<Atom> facts) throws DatabaseException {
		try {
			connection.executeStatements(createInsertStatements(facts));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error while adding facts:" + facts,e); 
		}
	}

	@Override
	protected void deleteFacts(Collection<Atom> facts) throws DatabaseException {
		try {
			Collection<String> statements = new ArrayList<>(); 
			for (Atom a: facts) {
				statements.add(createDeleteStatement(a));
			}
			connection.executeStatements(statements);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Error while adding facts:" + facts,e); 
		}
	}

	@Override
	protected Collection<Atom> getFactsFromPhysicalDatabase() throws DatabaseException {
		ArrayList<Atom> ret = new ArrayList<Atom>();
		for (Relation r: schema.getRelations()) {
			ret.addAll(getFactsOfRelation(r));
		}
		return ret;
	}
	
	@Override
	protected Collection<Atom> getFactsOfRelation(Relation r) throws DatabaseException {
		try {
			ArrayList<Atom> ret = new ArrayList<>();
			ConjunctiveQuery formula = createQuery(r);
			List<Match> matches = connection.executeQuery(new SQLSelect(formula, this));
			ret = getAtomsFromMatches(matches,r);
			return ret;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Failed to read facts from the database",e);
		}
		
	}
	
	@Override
	protected List<Match> answerQueries(Collection<PhysicalQuery> queries) throws DatabaseException {
		List<Match> ret = new ArrayList<>();
		for (PhysicalQuery q:queries) {
			try {
				ret.addAll(connection.executeQuery((SQLSelect)q));
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DatabaseException("Error while executing query: " + q,e);
			}
		}
		return ret;
	}

	abstract protected Collection<String> createDropStatements(String databaseName);

	abstract protected String createBulkInsertStatement(Predicate predicate, Collection<Atom> facts);

	/**
	 * Creates SQL insert statements, for the input facts.
	 *
	 * @param facts
	 *            the facts
	 * @param toDatabaseTables
	 *            map to the relation objects
	 * @return insert statements that add the input fact to the fact database.
	 */
	protected Collection<String> createInsertStatements(Collection<Atom> facts) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact : facts) {
			Relation relation = this.schema.getRelation(fact.getPredicate().getName());
			String insertInto = "INSERT INTO " + databaseParameters.getDatabaseName() + "." + relation.getName() + " " + "VALUES ( ";
			for (int termIndex = 0; termIndex < fact.getNumberOfTerms(); ++termIndex) {
				Term term = fact.getTerm(termIndex);
				insertInto += convertTermToSQLString(relation.getAttribute(termIndex), term);
				if (termIndex < fact.getNumberOfTerms() - 1)
					insertInto += ",";
			}
			insertInto += ")";
			result.add(insertInto);
		}
		return result;
	}


	/**
	 * Creates delete statements
	 *
	 * @param facts
	 *            Facts to delete from the database
	 * @param relationNamesToDatabaseTables
	 *            Map of schema relation names to *clean* names
	 * @return a set of statements that delete the input facts from the fact
	 *         database.
	 */
	protected String createDeleteStatement(Atom fact) {
		String deleteFrom = "DELETE FROM " + databaseParameters.getDatabaseName() + "." + fact.getPredicate().getName() + " " + "WHERE ";
		Relation r = schema.getRelation(fact.getPredicate().getName());
		int index = 0;
		for (Attribute a:r.getAttributes()) {
			if (index!=0)
				deleteFrom += " AND ";
			deleteFrom += a.getName() + " = " + convertTermToSQLString(a,fact.getTerm(index)) + " ";
			index++;
		}
		deleteFrom += "\n";
		return deleteFrom;
	}

	protected static String convertTermToSQLString(Attribute a, Term term) {
		String termInSqlString = ""; 
		if (!term.isVariable()) {
			
			if (a.getType() == String.class && term instanceof TypedConstant && !"DatabaseInstanceID".equals(a.getName()) && !"FactId".equals(a.getName()))
				termInSqlString += "'" +  ((TypedConstant)term).serializeToString() + "'";
			else if (String.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += "'" +  term + "'";
			else if (Integer.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
			else if (Double.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
			else if (Float.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
		} else
				throw new RuntimeException("Unsupported type");
		return termInSqlString;
	}

	/**
	 * Creates the table statement.
	 *
	 * @param relation
	 *            the table to create
	 * @return a SQL statement that creates the fact table of the given relation
	 */
	protected String createTableStatement(Relation relation) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE " + databaseParameters.getDatabaseName() + ".").append(relation.getName()).append('(');
		for (int attributeIndex = 0; attributeIndex < relation.getArity(); ++attributeIndex) {
			Attribute attribute = relation.getAttribute(attributeIndex);
			result.append(' ').append(attribute.getName());
			if (String.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" VARCHAR(500)");
			else if (Integer.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" INT");
			else if (Double.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" DOUBLE");
			else if (Float.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" FLOAT");
			else
				throw new RuntimeException("Unsupported type");
			if (attributeIndex < relation.getArity() - 1)
				result.append(", ");
		}
		String keyAttributes = null;
		PrimaryKey pk = relation.getKey();
		if (pk != null) {
			for (Attribute a : pk.getAttributes()) {
				if (keyAttributes == null) {
					keyAttributes = "";
				} else {
					keyAttributes += ",";
				}
				keyAttributes += a.getName();
				keyAttributes += " ";
			}
			result.append(" PRIMARY KEY ").append("(").append(keyAttributes).append(")");
		}
		result.append(')');
		return result.toString();
	}

	/**
	 * Creates the table index.
	 *
	 * @param isForQuery
	 *            the is for query
	 * @param existingIndices
	 * @param relation
	 *            the relation
	 * @param columns
	 *            the columns
	 * @return a SQL statement that creates an index for the columns of the input
	 *         relation
	 */
	protected Pair<String, String> createTableIndices(Set<String> existingIndices, Relation relation, Integer... columns) {
		StringBuilder indexName = new StringBuilder();
		StringBuilder indexColumns = new StringBuilder();
		String sep1 = "", sep2 = "";
		for (Integer i : columns) {
			indexName.append(sep1).append(relation.getAttribute(i).getName());
			indexColumns.append(sep2).append(relation.getAttribute(i).getName());
			sep1 = "_";
			sep2 = ",";
		}
		// if the index is not already existing due to the constraints
		if (existingIndices.contains(relation.getName() + "_" + indexName))
			return null;
		else
			existingIndices.add(relation.getName() + "_" + indexName);
		String create = this.createColumnIndexStatement(relation, indexName, indexColumns);
		String drop = this.createDropIndexStatement(relation, indexName, indexColumns);
		return new ImmutablePair<String, String>(create, drop);
	}

	/**
	 * Index create statement.
	 *
	 * @param relation
	 *            the relation
	 * @param indexName
	 *            the index name
	 * @param indexColumns
	 *            the index columns
	 * @return the string
	 */
	protected String createColumnIndexStatement(Relation relation, StringBuilder indexName, StringBuilder indexColumns) {
		return "CREATE INDEX idx_" + relation.getName() + "_" + indexName + " ON " + databaseParameters.getDatabaseName() + "." + relation.getName() + "(" + indexColumns + ")";
	}

	/**
	 * Index drop statement.
	 *
	 * @param relation
	 *            the relation
	 * @param indexName
	 *            the index name
	 * @param indexColumns
	 *            the index columns
	 * @return the string
	 */
	protected String createDropIndexStatement(Relation relation, StringBuilder indexName, StringBuilder indexColumns) {
		return "DROP INDEX idx_" + relation.getName() + "_" + indexName + " ON " + databaseParameters.getDatabaseName() + "." + relation.getName();
	}

	/**
	 * Creates the table non join indexes.
	 *
	 * @param relation
	 *            the relation
	 * @param column
	 *            the column
	 * @return a SQL statement that creates an index for the bag and fact attributes
	 *         of the database tables
	 */
	protected String createColumnIndexStatement(Relation relation, Attribute column) {
		return "CREATE INDEX idx_" + relation.getName() + "_" + column.getName() + " ON " + databaseParameters.getDatabaseName() + "." +  relation.getName() + "(" + column.getName() + ")";
	}

	/**
	 * Creates the table indexes.
	 *
	 * @param isForQuery
	 *            the is for query
	 * @param relationNamesToDatabaseTables
	 *            the relation map
	 * @param rule
	 *            the rule
	 * @param existingIndices
	 *            the constraint indices
	 * @return the pair
	 */
	protected Pair<Collection<String>, Collection<String>> setupIndices(boolean isForQuery, Map<String, Relation> relationNamesToDatabaseTables, Formula rule,
			Set<String> existingIndices) {
		Formula body = null;
		if (rule instanceof Atom) {
			body = rule;
		} else if (rule instanceof TGD) {
			body = ((TGD) rule).getBody();
		} else if (rule instanceof EGD) {
			body = ((EGD) rule).getBody();
		} else if (rule instanceof ConjunctiveQuery) {
			body = rule;
		} else {
			throw new UnsupportedOperationException("Homomorphism check only supported on conjunction of atomic predicate formulas for now.");
		}
		Set<String> createIndices = new LinkedHashSet<>();
		Set<String> dropIndices = new LinkedHashSet<>();
		Multimap<Variable, Atom> clusters = LinkedHashMultimap.create();
		for (Atom subFormula : body.getAtoms()) {
			for (Term t : subFormula.getTerms()) {
				if (t instanceof Variable) {
					clusters.put((Variable) t, subFormula);
				}
			}
		}
		for (Variable t : clusters.keys()) {
			Collection<Atom> atoms = clusters.get(t);
			if (atoms.size() > 1) {
				for (Atom atom : atoms) {
					for (int i = 0; i < atom.getTerms().length; i++) {
						if (atom.getTerm(i).equals(t)) {
							Pair<String, String> createAndDropIndices = this.createTableIndices(existingIndices, relationNamesToDatabaseTables.get(atom.getPredicate().getName()),
									i);
							if (createAndDropIndices != null) {
								createIndices.add(createAndDropIndices.getLeft());
								if (isForQuery)
									dropIndices.add(createAndDropIndices.getRight());
							}
						}
					}
				}
			}
		}
		return new ImmutablePair<Collection<String>, Collection<String>>(createIndices, dropIndices);
	}

}
