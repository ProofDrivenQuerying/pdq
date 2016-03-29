package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.predicates.EqualityPredicate;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager.DBRelation;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.EGDHomomorphismConstraint;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.FactConstraint;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.MapConstraint;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

// TODO: Auto-generated Javadoc
/**
 * Creates SQL statements for relation database-backed homomorphism detectors.
 *
 * @author George Konstantinidis
 * @author Efthymia Tsamoura
 */
public abstract class SQLStatementBuilder {

	/** Logger. */
	private static Logger log = Logger.getLogger(SQLStatementBuilder.class);

	/**  Aliases for facts *. */
	protected BiMap<Atom, String> aliases = HashBiMap.create();

	/** The alias prefix. */
	private String aliasPrefix = "A";
	
	/** The alias counter. */
	private int aliasCounter = 0;

	/** The clean name counter. */
	public int cleanNameCounter = 0;
	
	/**  maps all relation names to clean ones *. */
	protected BiMap<String, String> cleanMap = HashBiMap.create();
	
	/**
	 * Instantiates a new SQL statement builder.
	 */
	public SQLStatementBuilder() {
	}
	
	/**
	 * Instantiates a new SQL statement builder.
	 *
	 * @param cleanMap the clean map
	 */
	protected SQLStatementBuilder(BiMap<String, String> cleanMap) {
		Preconditions.checkNotNull(cleanMap);
		this.cleanMap = cleanMap;
	}

	/**
	 * Setup statements.
	 *
	 * @param databaseName the database name
	 * @return the complete list of SQL statements required to set up the facts database
	 */
	public abstract Collection<String> setupStatements(String databaseName);

	/**
	 * Cleanup statements.
	 *
	 * @param databaseName String
	 * @return the complete list of SQL statements required to clean up the fact database
	 */
	public abstract Collection<String> cleanupStatements(String databaseName);

	/**
	 * Drop table statement.
	 *
	 * @param relation the table to drop
	 * @return a SQL statement for dropping the input table
	 */
	protected String dropTableStatement(DBRelation relation) {
		return "DROP TABLE " + this.encodeName(relation.getName());
	}

	/**
	 * Encode name.
	 *
	 * @param dirtyRelationName the dirty relation name
	 * @return a new String that is a copy of the given name modified such that
	 * it is acceptable for the underlying system
	 */
	public String encodeName(String dirtyRelationName) {
		if(!this.cleanMap.containsKey(dirtyRelationName))
			this.cleanMap.put(dirtyRelationName, "cleanR"+cleanNameCounter++);
		return this.cleanMap.get(dirtyRelationName);
	}
	
	/**
	 * Decode name.
	 *
	 * @param cleanRelationName the clean relation name
	 * @return the string
	 */
	public String decodeName(String cleanRelationName) {
		return this.cleanMap.inverse().get(cleanRelationName);
	}
	

	/**
	 * Make inserts.
	 *
	 * @param facts the facts
	 * @param dbrelations the dbrelations
	 * @return insert statements that add the input fact to the fact database.
	 */
	protected Collection<String> makeInserts(Collection<? extends Atom> facts, Map<String, DBRelation> dbrelations) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact : facts) {
			DBRelation rel = dbrelations.get(fact.getName());
			List<Term> terms = fact.getTerms();
			String insertInto = "INSERT INTO " + this.encodeName(rel.getName()) + " " + "VALUES ( ";
			for (Term term : terms) {
				if (!term.isVariable()) {
					insertInto += "'" + term + "'" + ",";
				}
			}
			insertInto += 0 + ",";
			insertInto += fact.getId();
			insertInto += ")";
			result.add(insertInto);
		}
		log.trace(result);
		return result;
	}
	
	/**
	 * Make deletes.
	 *
	 * @param facts 		Facts to delete from the database
	 * @param aliases 		Map of schema relation names to *clean* names
	 * @return 		a set of statements that delete the input facts from the fact database.
	 */
	protected Collection<String> makeDeletes(Collection<? extends Atom> facts, Map<String, DBRelation> aliases) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact : facts) {
			Relation alias = aliases.get(fact.getName());
			String delete = "DELETE FROM " + this.encodeName(alias.getName()) + " " + "WHERE ";
			Attribute attribute = alias.getAttributes().get(alias.getAttributes().size()-1);
			delete += attribute.getName() + "=" + fact.getId();
			result.add(delete);
		}
		return result;
	}

	/**
	 * Creates the table statement.
	 *
	 * @param relation the table to create
	 * @return a SQL statement that creates the fact table of the given relation
	 */
	protected String createTableStatement(DBRelation relation) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE  ").append(this.encodeName(relation.getName())).append('(');
		for (int it = 0; it < relation.getAttributes().size(); ++it) {
			result.append(' ').append(relation.getAttributes().get(it).getName());
			if (relation.getAttribute(it).getType() instanceof Class && String.class.isAssignableFrom((Class) relation.getAttribute(it).getType())) {
				result.append(" VARCHAR(500),");
			}
			else if (relation.getAttribute(it).getType() instanceof Class && Integer.class.isAssignableFrom((Class) relation.getAttribute(it).getType())) {
				result.append(" int,");
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
		}
		result.append(" PRIMARY KEY ").append("(").append("Fact").append(")");
		result.append(')');
		log.trace(relation);
		log.trace(result);
		return result.toString();
	}

	/**
	 * Creates the table index.
	 *
	 * @param isForQuery the is for query
	 * @param constraintIndices the constraint indices
	 * @param relation the relation
	 * @param columns the columns
	 * @return a SQL statement that creates an index for the columns of the input relation
	 */
	protected Pair<String,String> createTableIndex(boolean isForQuery,Set<String> constraintIndices,DBRelation relation, Integer... columns) {
		StringBuilder indexName = new StringBuilder();
		StringBuilder indexColumns = new StringBuilder();
		String sep1 = "", sep2 = "";
		for (Integer i: columns) {
			indexName.append(sep1).append(relation.getAttribute(i).getName());
			indexColumns.append(sep2).append(relation.getAttribute(i).getName());
			sep1 = "_";
			sep2 = ",";
		}
		
		if(isForQuery) //if the index is created for the query
		{
			//and is not already existing due to the constraints
			if(constraintIndices.contains(this.encodeName(relation.getName()) + "_" + indexName))
				return new ImmutablePair<String, String>(null,null);
		}else{
			constraintIndices.add(this.encodeName(relation.getName()) + "_" + indexName );
		}
		String create = indexCreateStatement(relation,indexName,indexColumns);		
		String drop =  indexDropStatement(relation,indexName,indexColumns);
		
		return new ImmutablePair<String, String>(create,drop);
	}
	
	/**
	 * Index create statement.
	 *
	 * @param relation the relation
	 * @param indexName the index name
	 * @param indexColumns the index columns
	 * @return the string
	 */
	protected String indexCreateStatement(DBRelation relation, StringBuilder indexName, StringBuilder indexColumns) {
		return "CREATE INDEX idx_" + this.encodeName(relation.getName()) + "_" + indexName +
		" ON " + this.encodeName(relation.getName()) + "(" + indexColumns + ")";
	}
	
	/**
	 * Index drop statement.
	 *
	 * @param relation the relation
	 * @param indexName the index name
	 * @param indexColumns the index columns
	 * @return the string
	 */
	protected String indexDropStatement(DBRelation relation, StringBuilder indexName, StringBuilder indexColumns) {
		return "DROP INDEX idx_" + this.encodeName(relation.getName()) + "_" + indexName +
				" ON " + this.encodeName(relation.getName());
	}

	/**
	 * Clear tables.
	 *
	 * @param queryRelations the query relations
	 * @param relationMap the relation map
	 * @return the collection
	 */
	public Collection<String> clearTables(List<Atom> queryRelations, Map<String, DBRelation> relationMap) {
		Set<String> result = new LinkedHashSet<>();
		for(Atom pred: queryRelations)
			result.add(this.createClearTable(relationMap.get(pred.getName())));
		return result;
	}

	/**
	 * Creates the clear table.
	 *
	 * @param dbRelation the db relation
	 * @return the string
	 */
	private String createClearTable(DBRelation dbRelation) {
		return "TRUNCATE TABLE  "+this.encodeName(dbRelation.getName());
	}

	/**
	 * Creates the table non join indexes.
	 *
	 * @param relation the relation
	 * @param column the column
	 * @return a SQL statement that creates an index for the bag and fact attributes of the database tables
	 */
	protected String createTableNonJoinIndexes(DBRelation relation, Attribute column) {
		return "CREATE INDEX idx_" + this.encodeName(relation.getName()) + "_" + 
				column.getName() + " ON " + this.encodeName(relation.getName()) + "(" + column.getName() + ")";
	}

	/**
	 * Creates the table indexes.
	 *
	 * @param isForQuery the is for query
	 * @param relationMap the relation map
	 * @param rule the rule
	 * @param constraintIndices the constraint indices
	 * @return the pair
	 */
	protected Pair<Collection<String>,Collection<String>> createTableIndexes(boolean isForQuery, Map<String, DBRelation> relationMap, Evaluatable rule, Set<String> constraintIndices) {
		Conjunction<?> body = null;
		if (rule.getBody() instanceof Atom) {
			body = Conjunction.of((Atom) rule.getBody());
		} else if (rule.getBody() instanceof Conjunction<?>) {
			body = (Conjunction) rule.getBody();
		} else {
			throw new UnsupportedOperationException("Homomorphism check only supported on conjunction of atomic predicate formulas for now.");
		}
		Set<String> createIndices = new LinkedHashSet<>();
		Set<String> dropIndices = new LinkedHashSet<>();
		Multimap<Variable, Atom> clusters = LinkedHashMultimap.create();
		for (Formula subFormula: body) {
			if (subFormula instanceof Atom) {
				for (Term t: subFormula.getTerms()) {
					if (t instanceof Variable) {
						clusters.put((Variable) t, (Atom) subFormula);
					}
				}
			} else {
				throw new UnsupportedOperationException("Homomorphism check only supported on conjunction of atomic predicate formulas for now.");
			}
		}
		for (Variable t: clusters.keys()) {
			Collection<Atom> atoms = clusters.get(t);
			if (atoms.size() > 1) {
				for (Atom atom: atoms) {
					for (int i = 0, l = atom.getTermsCount(); i < l; i++) {
						if (atom.getTerm(i).equals(t)) {
							Pair<String,String> createAndDropIndices = this.createTableIndex(isForQuery,constraintIndices, relationMap.get(atom.getName()), i);
							if(createAndDropIndices.getLeft() != null)
								createIndices.add(createAndDropIndices.getLeft());
							if(isForQuery && createAndDropIndices.getRight()!=null)
								dropIndices.add(createAndDropIndices.getRight());
						}
					}
				}
			}
		}
		return new ImmutablePair<Collection<String>, Collection<String>>(createIndices, dropIndices);
	}

	/**
	 * Creates a statement that narrows down the number of homomorphisms returned for the source formula to the facts of the database. 
	 * @param source
	 * 		An input formula
	 * @param constraints
	 * 		A set of constraints that should be satisfied by the homomorphisms of the input formula to the facts of the database 
	 * @return
	 * 		an SQL LIMIT statement 
	 */
	protected abstract String translateLimitConstraints(Evaluatable source, HomomorphismConstraint... constraints);

	/**
	 * Creates and runs an SQL statement that detects homomorphisms of the input query to facts kept in a database.
	 *
	 * @param source the source
	 * @param constraints 		A set of constraints that should be satisfied by the homomorphisms of the input formula to the facts of the database 
	 * @param constants the constants
	 * @param connection the connection
	 * @return homomorphisms of the input query to facts kept in a database.
	 */
	public Set<Map<Variable, Constant>> findHomomorphismsThroughSQL(Evaluatable source, HomomorphismConstraint[] constraints, Map<String, TypedConstant<?>> constants, Connection connection) {

		String query = "";
		List<String> from = this.createContentForFromStatement(source);
		LinkedHashMap<String,Variable> projectionStatementsAndVariables = this.createProjectionStatements(source);
		List<String> predicates = new ArrayList<String>();
		List<String> attributeEqualityPredicates = this.createAttributeEqualities((Conjunction<Atom>) source.getBody(), this.aliases);
		List<String> attributeConstantEqualityPredicates = this.createEqualitiesWithConstants((Conjunction<Atom>) source.getBody(), this.aliases);
		List<String> equalityForHomRestrictionsPredicates = this.createEqualitiesForHomConstraints(source, this.aliases, constraints);
		
		String egdConstraint = this.translateEGDHomomorphismConstraints(source, this.aliases, constraints);

		/*
		 * if the target set of facts is not null, we
		 * add in the WHERE statement a predicate which limits the identifiers
		 * of the facts that satisfy any homomorphism to the
		 * identifiers of these facts
		 */
		List<String> factConstraints = this.translateFactConstraints(source, this.aliases, constraints);

		if(egdConstraint!=null) {
			predicates.add(egdConstraint);
		}
		
		predicates.addAll(attributeEqualityPredicates);
		predicates.addAll(attributeConstantEqualityPredicates);
		predicates.addAll(equalityForHomRestrictionsPredicates);
		predicates.addAll(factConstraints);

		//Limit the number of returned homomorphisms
		String limit = this.translateLimitConstraints(source, constraints); 

		query = "SELECT " 	+ Joiner.on(",").join(projectionStatementsAndVariables.keySet()) + "\n" +  
				"FROM " 	+ Joiner.on(",").join(from);
		if(!predicates.isEmpty()) {
			query += "\n" + "WHERE " + Joiner.on(" AND ").join(predicates);
		}	

		if(limit != null) {
			query += "\n" + limit;
		}

		log.trace(source);
		log.trace(query);
		log.trace("\n\n");

		/*
		 * For each returned homomorphism (each homomorphism corresponds to each
		 * returned tuple) we create a mapping of variables to constants.
		 * We also maintain the bag where this homomorphism is found
		 */
		Set<Map<Variable, Constant>> maps = new LinkedHashSet<>();
		try(Statement sqlStatement = connection.createStatement();
				ResultSet resultSet = sqlStatement.executeQuery(query)) {
			while (resultSet.next()) {

				int f = 1;
				Map<Variable, Constant> map = new LinkedHashMap<>();
				for(Entry<String, Variable> entry:projectionStatementsAndVariables.entrySet()) {
					Variable var = entry.getValue();
					String assigned = resultSet.getString(f);
					TypedConstant<?> constant = constants.get(assigned);
					Constant constantTerm = constant != null ? constant : new Skolem(assigned);
					map.put(var, constantTerm);
					f++;
				}
				maps.add(map);
			}
		} catch (SQLException ex) {
			log.debug(query);
			throw new IllegalStateException(ex.getMessage(), ex);
		}

		return maps;
	}

	
	/**
	 * Creates the content for from statement.
	 *
	 * @param source the source
	 * @return 		a list of the table names that will be queried
	 */
	protected List<String> createContentForFromStatement(Evaluatable source) {
		this.aliasCounter = 0;
		List<String> relations = new ArrayList<String>();
		this.aliases = HashBiMap.create();;
		for (Atom fact:source.getBody().getAtoms()) {
			String aliasName = this.aliasPrefix + this.aliasCounter;
			relations.add(createTableAliasingExpression(aliasName, (Relation) fact.getSignature()));
			this.aliases.put(fact, aliasName);
			this.aliasCounter++;
		}
		return relations;
	}

	/**
	 * Creates the projection statements.
	 *
	 * @param source the source
	 * @return 		the attributes that will be projected. 
	 * 		If the input is an egd or tgd we project the attributes that map to universally quantified variables.
	 * 		If the input is a query we project the attributes that map to its free variables.
	 */
	protected LinkedHashMap<String,Variable> createProjectionStatements(Evaluatable source) {
		LinkedHashMap<String,Variable> projected = new LinkedHashMap<>();
		List<Variable> attributes = new ArrayList<>();
		for (Atom fact:source.getBody().getAtoms()) {
			String alias = this.aliases.get(fact);
			List<Term> terms = fact.getTerms();
			for (int it = 0; it < terms.size(); ++it) {
				Term term = terms.get(it);
				if (term instanceof Variable && !attributes.contains(((Variable) term).getName())) {
					projected.put(createProjectionStatementForArgument(it, (Relation) fact.getSignature(), alias), (Variable)term);
					attributes.add(((Variable) term));
				}
			}
		}
		return projected;
	}

	/**
	 * Creates the attribute equalities.
	 *
	 * @param source the source
	 * @param aliases2 the aliases2
	 * @return 		explicit equalities (String objects of the form A.x1 = B.x2) of the implicit equalities in the input conjunction (the latter is denoted by repetition of the same term)
	 */
	protected List<String> createAttributeEqualities(Conjunction<Atom> source, BiMap<Atom, String> aliases2) {
		List<String> attributePredicates = new ArrayList<String>();
		Collection<Term> terms = Utility.getTerms(source.getAtoms());
		terms = Utility.removeDuplicates(terms);
		for (Term term:terms) {
			Integer leftPosition = null;
			Relation leftRelation = null;
			String leftAlias = null;
			for (Atom fact:source.getAtoms()) {
				List<Integer> positions = fact.getTermPositions(term); //all the positions for the same term should be equated
				for (Integer pos:positions) {
					if(leftPosition == null) {
						leftPosition = pos;
						leftRelation = (Relation) fact.getSignature();
						leftAlias = aliases2.get(fact);
					}
					else {					
						Integer rightPosition = pos;
						Relation rightRelation = (Relation) fact.getSignature();
						String rightAlias = aliases2.get(fact);
						
						StringBuilder result = new StringBuilder();
						result.append(leftAlias==null ? this.encodeName(leftRelation.getName()):leftAlias).append(".").append(leftRelation.getAttribute(leftPosition).getName()).append('=');
						result.append(rightAlias==null ? this.encodeName(rightRelation.getName()):rightAlias).append(".").append(rightRelation.getAttribute(rightPosition).getName());
						attributePredicates.add(result.toString());
					}
				}
			}
		}
		return attributePredicates;
	}


	/**
	 * Creates the equalities with constants.
	 *
	 * @param source the source
	 * @param aliases2 the aliases2
	 * @return 		constant equality predicates
	 */
	protected List<String> createEqualitiesWithConstants(Conjunction<Atom> source, BiMap<Atom, String> aliases2) {
		List<String> constantPredicates = new ArrayList<>();
		for (Atom fact:source.getAtoms()) {
			String alias = aliases2.get(fact);
			List<Term> terms = fact.getTerms();
			for (int it = 0; it < terms.size(); ++it) {
				Term term = terms.get(it);
				if (!term.isVariable() && !term.isSkolem()) {
					StringBuilder eq = new StringBuilder();
					eq.append(alias==null ? this.encodeName(fact.getSignature().getName()):alias).append(".").append(((Relation) fact.getSignature()).getAttribute(it).getName()).append('=');
					eq.append("'").append(((TypedConstant) term).toString()).append("'");
					constantPredicates.add(eq.toString());
				}
			}
		}
		return constantPredicates;
	}

	/**
	 * Translate fact constraints.
	 *
	 * @param source the source
	 * @param aliases2 the aliases2
	 * @param constraints the constraints
	 * @return 		predicates that correspond to fact constraints
	 */
	protected List<String> translateFactConstraints(Evaluatable source, BiMap<Atom, String> aliases2, HomomorphismConstraint... constraints) {
		List<String> setPredicates = new ArrayList<>();
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof FactConstraint) {
				List<Object> facts = new ArrayList<>();
				for (Atom atom:((FactConstraint) c).atoms) {
					facts.add(atom.getId());
				}
				for(Atom fact:source.getBody().getAtoms()) {
					String alias = aliases2.get(fact);
					setPredicates.add(createSQLMembershipExpression(fact.getTermsCount()-1, facts, (Relation) fact.getSignature(), alias));
				}
			}
		}
		return setPredicates;
	}

	/**
	 * Creates the equalities for hom constraints.
	 *
	 * @param source the source
	 * @param aliases2 the aliases2
	 * @param constraints the constraints
	 * @return 		predicates that correspond to canonical constraints
	 */
	protected List<String> createEqualitiesForHomConstraints(Evaluatable source, BiMap<Atom, String> aliases2, HomomorphismConstraint... constraints) {
		List<String> constantPredicates = new ArrayList<>();
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof MapConstraint) {
				Map<Variable, Constant> m = ((MapConstraint) c).mapping;
				for(Entry<Variable, Constant> pair:m.entrySet()) {
					for (Atom fact:source.getBody().getAtoms()) {
						int it = fact.getTerms().indexOf(pair.getKey());
						if(it != -1) {
							StringBuilder eq = new StringBuilder();
							eq.append(aliases2.get(fact)==null ? this.encodeName(fact.getSignature().getName()):aliases2.get(fact)).append(".").append(((Relation) fact.getSignature()).getAttribute(it).getName()).append('=');
							eq.append("'").append(pair.getValue()).append("'");
							constantPredicates.add(eq.toString());
						}
					}
				}
			}
		}
		return constantPredicates;
	}

	
	/**
	 * Translate egd homomorphism constraints.
	 *
	 * @param source the source
	 * @param aliases the aliases
	 * @param constraints the constraints
	 * @return 		predicates that correspond to fact constraints
	 */
	protected String translateEGDHomomorphismConstraints(Evaluatable source, BiMap<Atom, String> aliases, HomomorphismConstraint... constraints) {
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof EGDHomomorphismConstraint) {
				List<Atom> conjuncts = source.getBody().getAtoms();
				String lalias = aliases.get(conjuncts.get(0));
				String ralias = aliases.get(conjuncts.get(1));
				lalias = lalias==null ? this.encodeName(conjuncts.get(0).getSignature().getName()):lalias;
				ralias = ralias==null ? this.encodeName(conjuncts.get(1).getSignature().getName()):ralias;
				StringBuilder eq = new StringBuilder();
				eq.append(lalias).append(".").
				append("FACT").append("<>");
				
				eq.append(ralias).append(".").
				append("FACT");
				return eq.toString();
			}
		}
		return null;
	}

	/**
	 * Creates the sql membership expression.
	 *
	 * @param position the position
	 * @param values the values
	 * @param relation the relation
	 * @param alias the alias
	 * @return the string
	 */
	private String createSQLMembershipExpression(int position, List<Object> values, Relation relation, String alias) {
		
		StringBuilder result = new StringBuilder();
		String set = "";
		int v = 0;
		for(v = 0; v < values.size()-1; ++v) {
			if(values.get(v) instanceof Number) {
				set += values.get(v) + ",";
			}
			else {
				set += "\"" + values.get(v) + "\"" + ",";
			}
		}
		
		if(values.get(v) instanceof Number) {
			set += values.get(v);
		}
		else {
			set += "\"" + values.get(v) + "\"";
		}
		
		result.append(alias==null ? this.encodeName(relation.getName()):alias).append(".").append(relation.getAttribute(position).getName()).
		append(" IN ").append("(").append(set).append(")");
		return result.toString();
	
	}

	/**
	 * Clone.
	 *
	 * @return SQLStatementBuilder
	 */
	@Override
	public abstract SQLStatementBuilder clone();

	/**
	 * Creates the projection statement for argument.
	 *
	 * @param position the position
	 * @param relation the relation
	 * @param alias the alias
	 * @return the string
	 */
	protected String createProjectionStatementForArgument(int position, Relation relation, String alias) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkArgument(position >= 0 && position < relation.getArity());
		StringBuilder result = new StringBuilder();
		result.append(alias==null ? this.encodeName(relation.getName()):alias).
		append(".").append(relation.getAttribute(position).getName());
		return result.toString();
	}
	

	/**
	 * Creates the table aliasing expression.
	 *
	 * @param alias the alias
	 * @param relation the relation
	 * @return the string
	 */
	protected String createTableAliasingExpression(String alias, Relation relation) {
		Preconditions.checkNotNull(relation);
		StringBuilder result = new StringBuilder();
		result.append(this.encodeName(relation.getName())).append(" AS ");
		result.append(alias==null ? relation.getName():alias);
		return result.toString();
	}

}
