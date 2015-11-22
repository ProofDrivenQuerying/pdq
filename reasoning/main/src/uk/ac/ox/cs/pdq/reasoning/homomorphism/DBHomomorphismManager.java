package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.reasoning.HomomorphismException;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.util.Table;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Detects homomorphisms from a conjunction to a set of facts (e.g., facts produced during chasing).
 * For each schema/accessible schema relation of N attributes it creates a table
 * with attributes x0, x1, ..., x_{N-1}, where x_i corresponds to the
 * i-th relation attribute and an additional attribute named BAG which
 * corresponds to the bag (if it exists) where this fact is placed in.
 *
 * @author Efthymia Tsamoura
 *
 */
public class DBHomomorphismManager implements HomomorphismManager {

	/** Logger. */
	private static Logger log = Logger.getLogger(DBHomomorphismManager.class);

	private final Attribute Bag = new Attribute(Integer.class, "Bag");
	private final Attribute Fact = new Attribute(Integer.class, "Fact");
	private String attrPrefix = "x";

	/** Collection of expected queries. */
	protected Set<Evaluatable> queries;

	protected final List<Relation> relations;

	/** A map of the string representation of a constant to the constant*/
	protected final Map<String, TypedConstant<?>> constants;

	/** Statement builder */
	protected final SQLStatementBuilder builder;
	/**
	 * A map from the schema relation names to the created relations (the ones that
	 * correspond to the created database tables)
	 */
	protected final Map<String, DBRelation> aliases;

	/** Connection to the database */
	protected Connection connection;

	/** Chase database name */
	protected final String database;

	/** Information to connect to the facts database*/
	protected final String driver;
	protected final String url;
	protected final String username;
	protected final String password;
	protected boolean isInitialized = false;

	protected List<Connection> clones = new ArrayList<>();

	/**
	 * 
	 * @param driver
	 * 		Database driver
	 * @param url
	 * 		Database url
	 * @param database
	 * 		Database name
	 * @param username
	 * 		Database user
	 * @param password
	 * 		Database pass
	 * @param builder
	 * 		Builds SQL queries that detect homomorphisms
	 * @param schema
	 * 		Input schema
	 * @param query
	 * 		Input query
	 * @throws SQLException
	 */
	public DBHomomorphismManager(
			String driver, 
			String url, 
			String database,
			String username, 
			String password,
			SQLStatementBuilder builder,
			Schema schema, 
			Query<?> query
			) throws SQLException {
		this.connection = getConnection(driver, url, database, username, password);
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.database = database;
		this.builder = builder;

		this.queries = this.makeQueries(query, schema);
		this.constants = schema.getConstants();
		this.relations = schema.getRelations();
		this.aliases = new LinkedHashMap<>();
	}

	/**
	 * 
	 * @param driver
	 * 		Database driver
	 * @param url
	 * 		Database url
	 * @param database
	 * 		Database name
	 * @param username
	 * 		Database user
	 * @param password
	 * 		Database pass
	 * @param builder
	 * 		Builds SQL queries that detect homomorphisms
	 * @param relations
	 * 		Database relations
	 * @param constants
	 * 		Schema constants
	 * @param aliases
	 * 		A map from the schema relation names to the created relations (the ones that
	 * 		correspond to the created database tables)
	 * @param queries
	 * 		Cached queries of interest
	 * @throws SQLException
	 */
	protected DBHomomorphismManager(
			String driver, 
			String url, 
			String database,
			String username, 
			String password,
			SQLStatementBuilder builder,
			List<Relation> relations,
			Map<String, TypedConstant<?>> constants,
			Map<String, DBRelation> aliases,
			Set<Evaluatable> queries) throws SQLException {
		this.connection = DBHomomorphismManager.getConnection(driver, url, database, username, password);
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.database = database;
		this.builder = builder;
		this.aliases = aliases;
		this.queries = queries;
		this.constants = constants;
		this.relations = relations;
	}

	/**
	 * 
	 * @param query
	 * @param schema
	 * @return
	 * 		queries of interest
	 */
	protected Set<Evaluatable> makeQueries(
			Query<?> query, 
			Schema schema
			) {
		Set<Evaluatable> result = Sets.newLinkedHashSet();
		if(query != null) {
			result.add(query);
		}
		for (Constraint ic: schema.getDependencies()) {
			result.add(ic);
		}
		return result;
	}

	/**
	 * @see uk.ac.ox.cs.pdq.homomorphism.HomomorphismManager#initialize()
	 */
	@Override
	public void initialize() {
		if (!this.isInitialized) {
			this.initialize(this.queries);
			this.isInitialized = true;
		}
	}

	/**
	 * @param queries Collection<Evaluatable>
	 * @see uk.ac.ox.cs.pdq.homomorphism.HomomorphismManager#initialize(Collection<Evaluatable>)
	 */
	@Override
	public void initialize(Collection<Evaluatable> queries) {
		if (!this.isInitialized) {
			this.setup();
		}
	}

	/**
	 * Sets up the database that will store the facts
	 */
	private void setup() {
		try(Statement sqlStatement = this.connection.createStatement()) {
			try {
				for (String sql: this.builder.setupStatements(this.database)) {
					sqlStatement.addBatch(sql);
				}
				this.createEqualityTable(sqlStatement);

				//putting relations into a set so as to make them unique
				Set<Relation> relationset = new HashSet<Relation>();
				relationset.addAll(this.relations);
				this.relations.clear();
				this.relations.addAll(relationset);

				this.createBaseTables(this.relations, sqlStatement);
				this.createJoinIndexes(sqlStatement);
				sqlStatement.executeBatch();
			} catch (SQLException ex) {
				throw new IllegalStateException(ex.getMessage(), ex);
			}
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	/**
	 * @param relations List<Relation>
	 * @param stmt Statement
	 * @throws SQLException
	 */
	private void createBaseTables(List<Relation> relations, Statement stmt) throws SQLException {
		DBRelation dbRelation = null;
		for (Relation relation:relations) {
			dbRelation = this.createDBRelation(relation);
			this.aliases.put(relation.getName(), dbRelation);
			stmt.addBatch(this.builder.createTableStatement(dbRelation));
			stmt.addBatch(this.builder.createTableNonJoinIndexes(dbRelation, this.Bag));
			stmt.addBatch(this.builder.createTableNonJoinIndexes(dbRelation, this.Fact));
		}
	}

	private void createEqualityTable(Statement stmt) throws SQLException {
		DBRelation equality = this.createEquality();
		this.aliases.put(QNames.EQUALITY.toString(), equality);
		stmt.addBatch(this.builder.createTableStatement(equality));
		stmt.addBatch(this.builder.createTableNonJoinIndexes(equality, this.Bag));
		stmt.addBatch(this.builder.createTableNonJoinIndexes(equality, this.Fact));
	}

	/**
	 * @param stmt Statement
	 * @throws SQLException
	 */
	private void createJoinIndexes(Statement stmt) throws SQLException {
		Set<String> joinIndexes = Sets.newLinkedHashSet();
		for (Evaluatable query:this.queries) {
			joinIndexes.addAll(this.builder.createTableIndexes(this.aliases, query));
		}
		for (String b: joinIndexes) {
			stmt.addBatch(b);
		}
	}

	/**
	 * Cleans up the database
	 * @throws HomomorphismException
	 */
	protected void cleanupDB() throws HomomorphismException {
		try (Statement sqlStatement = this.connection.createStatement();) {
			for (String sql: this.builder.cleanupStatements(this.database)) {
				sqlStatement.addBatch(sql);
			}
			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new HomomorphismException(ex.getMessage(), ex);
		}
	}


	@Override
	public <Q extends Evaluatable> List<Match> getMatches(Q source, HomomorphismConstraint... constraints) {
		Preconditions.checkNotNull(source);
		List<Match> result = new LinkedList<>();
		Q s = this.convert(source, this.aliases, constraints);

		HomomorphismConstraint[] c = null;
		if(source instanceof EGD) {
			c = new HomomorphismConstraint[constraints.length+1];
			System.arraycopy(constraints, 0, c, 0, constraints.length);
			c[constraints.length] = HomomorphismConstraint.createEGDHomomorphismConstraint();
		}
		else {
			c = constraints;
		}

		Set<Map<Variable, Constant>> maps = this.builder.findHomomorphismsThroughSQL(s, c, this.constants, this.connection);
		for(Map<Variable, Constant> map:maps) {
			result.add(new Match(source, map));
		}
		return result;
	}

	/**
	 * @param queries Collection<Q>
	 * @param constraints HomomorphismConstraint[]
	 * @return Map<Q,List<Matching>>
	 * @see uk.ac.ox.cs.pdq.homomorphism.HomomorphismDetector#getMatches(Collection<Q>, HomomorphismConstraint[])
	 */
	@Override
	public <Q extends Evaluatable> List<Match> getMatches(Collection<Q> queries, HomomorphismConstraint... constraints) {
		List<Match> result = new ArrayList<>();
		for (Q q: queries) {
			List<Match> matches = this.getMatches(q, constraints);
			if (!matches.isEmpty()) {
				result.addAll(matches);
			}
		}
		return result;
	}

	/**
	 * 
	 * @param source
	 * @param aliases
	 * @param constraints
	 * @return 
	 * 		a converted formula using the input mapping aliases
	 */
	private <Q extends Evaluatable> Q convert(Q source, Map<String, DBRelation> aliases, HomomorphismConstraint... constraints) {
		boolean singleBag = false;
		if(source instanceof Constraint) {
			int f = 0;
			int b = 0;
			List<Predicate> left = Lists.newArrayList();
			for(Predicate atom:((Constraint) source).getLeft().getPredicates()) {
				Relation relation = aliases.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(singleBag == true ? this.Bag.getName() : (this.Bag.getName() + b++)));
				terms.add(new Variable(this.Fact.getName() + f++));
				left.add(new Predicate(relation, terms));
			}
			List<Predicate> right = Lists.newArrayList();
			List<Equality> equalities = Lists.newArrayList();

			for(Predicate atom:((Constraint) source).getRight().getPredicates()) {
				Relation relation = aliases.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(singleBag == true ? this.Bag.getName() : (this.Bag.getName() + b++)));
				terms.add(new Variable(this.Fact.getName() + f++));
				right.add(new Predicate(relation, terms));
				right.add(new Predicate(relation, terms));
			}
			return (Q) new TGD(Conjunction.of(left), Conjunction.of(right));
		}
		else if(source instanceof Query) {
			int f = 0;
			int b = 0;
			List<Predicate> body = Lists.newArrayList();
			for(Predicate atom:((Query) source).getBody().getPredicates()) {
				Relation relation = aliases.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(singleBag == true ? this.Bag.getName() : this.Bag.getName() + b++));
				terms.add(new Variable(this.Fact.getName() + f++));
				body.add(new Predicate(relation, terms));
			}
			return (Q) new ConjunctiveQuery(((Query) source).getHead(), Conjunction.of(body));
		}
		else {
			throw new java.lang.UnsupportedOperationException();
		}
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		this.cleanupDB();
		this.connection.close();
		for(Connection con:this.clones) {
			con.close();
		}
	}

	/**
	 * @param url
	 * @param database
	 * @param username
	 * @param password
	 * @param driver String
	 * @return a connection database connection for the given properties.
	 * @throws SQLException
	 */
	public static Connection getConnection(String driver, String url, String database, String username, String password) throws SQLException {
		if (!Strings.isNullOrEmpty(driver)) {
			try {
				Class.forName(driver).newInstance();
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Could not load chase database driver '" + driver + "'");
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String u = null;
		if (url.contains("{1}")) {
			u = url.replace("{1}", database);
		} else {
			u = url + database;
		}
		try {
			Connection result = DriverManager.getConnection(u, username, password);
			result.setAutoCommit(true);
			return result;
		} catch (SQLException e) {
			log.debug(e.getMessage());
		}
		Connection result = DriverManager.getConnection(url, username, password);
		result.setAutoCommit(true);
		return result;
	}

	/**
	 * @param facts Collection<? extends PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.homomorphism.HomomorphismManager#addFacts(Collection<? extends PredicateFormula>)
	 */
	@Override
	public void addFacts(Collection<? extends Predicate> facts) {
		try (Statement sqlStatement = this.connection.createStatement()) {
			for (String stmt : this.builder.makeInserts(facts, this.aliases)) {
				sqlStatement.addBatch(stmt);
			}
			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	/**
	 * Deletes the facts of the list in the database
	 * @param facts Input list of facts
	 */
	@Override
	public void deleteFacts(Collection<? extends Predicate> facts) {
		try (Statement sqlStatement = this.connection.createStatement()) {
			for (String stmt : this.builder.makeDeletes(facts, this.aliases)) {
				sqlStatement.addBatch(stmt);
			}
			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	/**
	 * @return DBHomomorphismManager
	 * @see uk.ac.ox.cs.pdq.homomorphism.HomomorphismDetector#clone()
	 */
	@Override
	public DBHomomorphismManager clone() {
		try {
			DBHomomorphismManager ret = new DBHomomorphismManager(
					this.driver, this.url, this.database, this.username, this.password,
					this.builder.clone(), 
					this.relations, 
					this.constants,
					this.aliases, 
					this.queries);
			ret.isInitialized = this.isInitialized;
			this.clones.add(ret.connection);
			return ret;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * A relation built-up for homomorphism detection 
	 * @author Efthymia Tsamoura
	 *
	 */
	protected static class DBRelation extends Relation {

		private static final long serialVersionUID = 3503553786085749666L;

		/**
		 * Constructor for DBRelation.
		 * @param name String
		 * @param attributes List<Attribute>
		 */
		public DBRelation(String name, List<Attribute> attributes) {
			super(name, attributes);
		}
	}

	/**
	 * @param relation 
	 * @return a new database relation with attributes x0,x1,...,x_{N-1}, Bag, Fact where
	 *         x_i maps to the i-th relation's attribute
	 */
	protected DBRelation createDBRelation(Relation relation) {
		List<Attribute> attributes = new ArrayList<>();
		for (int index = 0, l = relation.getAttributes().size(); index < l; ++index) {
			attributes.add(new Attribute(String.class, this.attrPrefix + index));
		}
		attributes.add(this.Bag);
		attributes.add(this.Fact);
		return new DBRelation(relation.getName(), attributes);
	}

	protected DBRelation toDBRelation(Table table) {
		List<Attribute> attributes = new ArrayList<>();
		for (int index = 0, l = table.getHeader().size(); index < l; ++index) {
			attributes.add(new Attribute(String.class, this.attrPrefix + index));
		}
		attributes.add(this.Bag);
		attributes.add(this.Fact);
		return new DBRelation(table.getName(), attributes);
	}

	protected DBRelation createEquality() {
		List<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute(String.class, this.attrPrefix + 0));
		attributes.add(new Attribute(String.class, this.attrPrefix + 1));
		attributes.add(this.Bag);
		attributes.add(this.Fact);
		return new DBRelation(QNames.EQUALITY.toString(), attributes);
	}

}
