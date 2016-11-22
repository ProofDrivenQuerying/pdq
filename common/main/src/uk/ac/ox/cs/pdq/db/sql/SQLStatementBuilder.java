package uk.ac.ox.cs.pdq.db.sql;

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

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseRelation;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty.FactProperty;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty.MapProperty;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

// TODO: Auto-generated Javadoc
/**
 * Creates SQL statements to detect homomorphisms or add/delete facts in a database.
 *
 * @author Efthymia Tsamoura
 */
public abstract class SQLStatementBuilder {

	/** Logger. */
	private static Logger log = Logger.getLogger(SQLStatementBuilder.class);

	/**  Aliases for the relations in the query FROM statements. */
	public BiMap<Atom, String> aliases = HashBiMap.create();

	/** The alias prefix. */
	private String aliasPrefix = "A";

	/** The alias counter. */
	private int aliasCounter = 0;

	/**
	 * Make inserts.
	 *
	 * @param facts the facts
	 * @param toDatabaseTables the dbrelations
	 * @return insert statements that add the input fact to the fact database.
	 */
	public Collection<String> createInsertStatements(Collection<Atom> facts, Map<String, DatabaseRelation> toDatabaseTables) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact:facts) {
			DatabaseRelation relation = toDatabaseTables.get(fact.getPredicate().getName());
			List<Term> terms = fact.getTerms();
			String insertInto = "INSERT INTO " + relation.getName() + " " + "VALUES ( ";
			for (Term term : terms) {
				if (!term.isVariable()) {
					insertInto += "'" + term + "'" + ",";
				}
			}
			insertInto += fact.getId();
			insertInto += ")";
			result.add(insertInto);
		}
		log.trace(result);
		return result;
	}


	//used for debugging purposes
	public Collection<String> createGetAllTuplesStatement(Map<String, DatabaseRelation> toDatabaseTables) {
		Collection<String> result = new LinkedList<>();
		for (String key:toDatabaseTables.keySet()) {
			DatabaseRelation relation = toDatabaseTables.get(key);
			String selectAll = "SELECT * FROM " + relation.getName();
			result.add(selectAll);
		}
		log.trace(result);
		return result;
	}

	public abstract String createBulkInsertStatement(Predicate predicate, Collection<Atom> facts, Map<String, DatabaseRelation> toDatabaseTables);

	/**
	 * Creates an SQL statement that deletes the set of input facts, by deleting tuples whose fact id attribute is the fact id of the input fact.
	 *
	 * @param facts 		Facts to delete from the database
	 * @param toDatabaseTables 		Map of schema relation names to *clean* names
	 * @return 		a set of statements that delete the input facts from the fact database.
	 */
	protected Collection<String> createDeleteStatements(Collection<Atom> facts, Map<String, DatabaseRelation> toDatabaseTables) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact:facts) {
			Relation relation = toDatabaseTables.get(fact.getPredicate().getName());
			String delete = "DELETE FROM " + relation.getName() + " " + "WHERE ";
			Attribute attribute = DatabaseRelation.Fact;
			delete += attribute.getName() + "=" + fact.getId();
			result.add(delete);
		}
		return result;
	}

	/**
	 * Make deletes.
	 *
	 * @param facts 		Facts to delete from the database
	 * @param toDatabaseTables 		Map of schema relation names to *clean* names
	 * @return 		a set of statements that delete the input facts from the fact database.
	 */
	public String createBulkDeleteStatement(Predicate predicate, Collection<Atom> facts, Map<String, DatabaseRelation> toDatabaseTables) {
		String insertInto = "DELETE FROM " + toDatabaseTables.get(predicate.getName()).getName() + " " + "WHERE "; 
		insertInto += DatabaseRelation.Fact.getName();
		insertInto += " IN" + "\n"; 

		List<String> tuples = new ArrayList<String>();
		for (Atom fact:facts) {
			String tuple = "(" + fact.getId() + ")";
			tuples.add(tuple);
		}
		insertInto += "(" + Joiner.on(",").join(tuples) + ")";
		return insertInto;
	}

	/**
	 * Creates the table statement.
	 *
	 * @param relation the table to create
	 * @return a SQL statement that creates the fact table of the given relation
	 */
	public String createTableStatement(DatabaseRelation relation) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE  ").append(relation.getName()).append('(');
		for (int it = 0; it < relation.getAttributes().size(); ++it) {
			result.append(' ').append(relation.getAttributes().get(it).getName());
			if (relation.getAttribute(it).getType() instanceof Class && String.class.isAssignableFrom((Class<?>) relation.getAttribute(it).getType())) {
				result.append(" VARCHAR(500),");
			}
			else if (relation.getAttribute(it).getType() instanceof Class && Integer.class.isAssignableFrom((Class<?>) relation.getAttribute(it).getType())) {
				result.append(" int,");
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
		}
		result.append(" PRIMARY KEY ").append("(").append(DatabaseRelation.Fact.getName()).append(")");
		result.append(')');
		log.trace(relation);
		log.trace(result);
		return result.toString();
	}

	/**
	 * Creates the table index.
	 *
	 * @param isForQuery the is for query
	 * @param existingIndices
	 * @param relation the relation
	 * @param columns the columns
	 * @return a SQL statement that creates an index for the columns of the input relation
	 */
	protected Pair<String,String> createTableIndices(Set<String> existingIndices, DatabaseRelation relation, Integer... columns) {
		StringBuilder indexName = new StringBuilder();
		StringBuilder indexColumns = new StringBuilder();
		String sep1 = "", sep2 = "";
		for (Integer i: columns) {
			indexName.append(sep1).append(relation.getAttribute(i).getName());
			indexColumns.append(sep2).append(relation.getAttribute(i).getName());
			sep1 = "_";
			sep2 = ",";
		}
		//if the index is not already existing due to the constraints
		if(existingIndices.contains(relation.getName() + "_" + indexName))
		{	
			return null;
		}
		else{
			existingIndices.add(relation.getName() + "_" + indexName );
		}
		String create = this.createColumnIndexStatement(relation,indexName,indexColumns);		
		String drop =  this.createDropIndexStatement(relation,indexName,indexColumns);
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
	protected String createColumnIndexStatement(DatabaseRelation relation, StringBuilder indexName, StringBuilder indexColumns) {
		return "CREATE INDEX idx_" + relation.getName() + "_" + indexName + 
				" ON " + relation.getName() + "(" + indexColumns + ")";
	}

	/**
	 * Index drop statement.
	 *
	 * @param relation the relation
	 * @param indexName the index name
	 * @param indexColumns the index columns
	 * @return the string
	 */
	protected String createDropIndexStatement(DatabaseRelation relation, StringBuilder indexName, StringBuilder indexColumns) {
		return "DROP INDEX idx_" + relation.getName() + "_" + indexName + 
				" ON " + relation.getName(); 
	}

	/**
	 * Clear tables.
	 *
	 * @param queryRelations the query relations
	 * @param toDatabaseRelation the relation map
	 * @return the collection
	 */
	public Collection<String> createTruncateTableStatements(List<Atom> queryRelations, Map<String, DatabaseRelation> toDatabaseRelation) {
		Set<String> result = new LinkedHashSet<>();
		for(Atom pred: queryRelations)
			result.add("TRUNCATE TABLE  " + toDatabaseRelation.get(pred.getPredicate().getName()).getName());
		return result;
	}

	/**
	 * Creates the table non join indexes.
	 *
	 * @param relation the relation
	 * @param column the column
	 * @return a SQL statement that creates an index for the bag and fact attributes of the database tables
	 */
	public String createColumnIndexStatement(DatabaseRelation relation, Attribute column) {
		return "CREATE INDEX idx_" + relation.getName() + "_" + 
				column.getName() + " ON " + relation.getName() + "(" + column.getName() + ")"; 
	}

	/**
	 * Creates the table indexes.
	 *
	 * @param isForQuery the is for query
	 * @param toDatabaseRelations the relation map
	 * @param rule the rule
	 * @param existingIndices the constraint indices
	 * @return the pair
	 */
	public Pair<Collection<String>,Collection<String>> setupIndices(boolean isForQuery, Map<String, DatabaseRelation> toDatabaseRelations, Formula rule, Set<String> existingIndices) {
		Formula body = null;
		if (rule instanceof Atom) {
			body = rule;
		} else if (rule instanceof TGD) {
			body = ((TGD)rule).getBody();
		} else if (rule instanceof EGD) {
			body = ((EGD)rule).getBody();
		} else if (rule instanceof ConjunctiveQuery) {
			body = rule;
		} else {
			throw new UnsupportedOperationException("Homomorphism check only supported on conjunction of atomic predicate formulas for now.");
		}
		Set<String> createIndices = new LinkedHashSet<>();
		Set<String> dropIndices = new LinkedHashSet<>();
		Multimap<Variable, Atom> clusters = LinkedHashMultimap.create();
		for (Atom subFormula:body.getAtoms()) {
			for (Term t: subFormula.getTerms()) {
				if (t instanceof Variable) {
					clusters.put((Variable) t, subFormula);
				}
			}
		}
		for (Variable t: clusters.keys()) {
			Collection<Atom> atoms = clusters.get(t);
			if (atoms.size() > 1) {
				for (Atom atom: atoms) {
					for (int i = 0, l = atom.getPredicate().getArity(); i < l; i++) {
						if (atom.getTerm(i).equals(t)) {
							Pair<String,String> createAndDropIndices = this.createTableIndices(existingIndices, toDatabaseRelations.get(atom.getPredicate().getName()), i);
							if(createAndDropIndices != null) {	
								createIndices.add(createAndDropIndices.getLeft());
								if(isForQuery)
									dropIndices.add(createAndDropIndices.getRight());
							}
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
	public abstract String translateLimitConstraints(HomomorphismProperty... constraints);


	public List<String> createFromStatement(Collection<Atom> facts) {
		List<String> relations = new ArrayList<String>();
		for (Atom fact:facts) {
			String aliasName = this.aliasPrefix + this.aliasCounter;
			relations.add(this.createTableAliasingExpression(aliasName, fact.getPredicate()));
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
	public LinkedHashMap<String,Variable> createProjections(Dependency source) {
		LinkedHashMap<String,Variable> projected = new LinkedHashMap<>();
		List<Variable> attributes = new ArrayList<>();
		for (Atom fact:source.getBody().getAtoms()) {
			String alias = this.aliases.get(fact);
			List<Term> terms = fact.getTerms();
			for (int it = 0; it < terms.size(); ++it) {
				Term term = terms.get(it);
				if (term instanceof Variable && !attributes.contains(((Variable) term).getSymbol())) {
					projected.put(createProjectionStatementForArgument(it, (Relation) fact.getPredicate(), alias), (Variable)term);
					attributes.add(((Variable) term));
				}
			}
		}
		return projected;
	}

	public LinkedHashMap<String,Variable> createProjections(ConjunctiveQuery source) {
		LinkedHashMap<String,Variable> projected = new LinkedHashMap<>();
		List<Variable> attributes = new ArrayList<>();
		for (Atom fact:source.getAtoms()) {
			String alias = this.aliases.get(fact);
			List<Term> terms = fact.getTerms();
			for (int it = 0; it < terms.size(); ++it) {
				Term term = terms.get(it);
				if (term instanceof Variable && !attributes.contains(((Variable) term).getSymbol()) && source.getFreeVariables().contains(term)) {
					projected.put(createProjectionStatementForArgument(it, (Relation) fact.getPredicate(), alias), (Variable)term);
					attributes.add(((Variable) term));
				}
			}
		}
		return projected;
	}



	public List<String> createAttributeEqualities(Collection<Atom> source) {
		List<String> attributePredicates = new ArrayList<String>();
		Collection<Term> terms = Utility.getTerms(source);
		terms = Utility.removeDuplicates(terms);
		for (Term term:terms) {
			Integer leftPosition = null;
			Relation leftRelation = null;
			String leftAlias = null;
			for (Atom fact:source) {
				List<Integer> positions = fact.getTermPositions(term); //all the positions for the same term should be equated
				for (Integer pos:positions) {
					if(leftPosition == null) {
						leftPosition = pos;
						leftRelation = (Relation) fact.getPredicate();
						leftAlias = this.aliases.get(fact);
					}
					else {					
						Integer rightPosition = pos;
						Relation rightRelation = (Relation) fact.getPredicate();
						String rightAlias = this.aliases.get(fact);

						StringBuilder result = new StringBuilder();
						result.append(leftAlias==null ? leftRelation.getName():leftAlias).append(".").append(leftRelation.getAttribute(leftPosition).getName()).append('=');
						result.append(rightAlias==null ? rightRelation.getName():rightAlias).append(".").append(rightRelation.getAttribute(rightPosition).getName());
						attributePredicates.add(result.toString());
					}
				}
			}
		}
		return attributePredicates;
	}

	public List<String> createEqualitiesWithConstants(Collection<Atom> source) {
		List<String> constantPredicates = new ArrayList<>();
		for (Atom fact:source) {
			String alias = this.aliases.get(fact);
			List<Term> terms = fact.getTerms();
			for (int it = 0; it < terms.size(); ++it) {
				Term term = terms.get(it);
				if (!term.isVariable() && !term.isUntypedConstant()) {
					StringBuilder eq = new StringBuilder();
					eq.append(alias==null ? fact.getPredicate().getName():alias).append(".").append(((Relation) fact.getPredicate()).getAttribute(it).getName()).append('=');
					eq.append("'").append(((TypedConstant<?>) term).toString()).append("'");
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
	 * @param constraints the constraints
	 * @return 		predicates that correspond to fact constraints
	 */
	public List<String> translateFactProperties(Collection<Atom> source, HomomorphismProperty... constraints) {
		List<String> setPredicates = new ArrayList<>();
		for(HomomorphismProperty c:constraints) {
			if(c instanceof FactProperty) {
				List<Object> facts = new ArrayList<>();
				for (Atom atom:((FactProperty) c).atoms) {
					facts.add(atom.getId());
				}
				for(Atom fact:source) {
					String alias = this.aliases.get(fact);
					setPredicates.add(createSQLMembershipExpression(fact.getPredicate().getArity()-1, facts, (Relation) fact.getPredicate(), alias));
				}
			}
		}
		return setPredicates;
	}

	/**
	 * Creates the equalities for hom constraints.
	 *
	 * @param source the source
	 * @param constraints the constraints
	 * @return 		predicates that correspond to canonical constraints
	 */
	public List<String> createEqualitiesForHomomorphicProperties(Collection<Atom> source, HomomorphismProperty... constraints) {
		List<String> constantPredicates = new ArrayList<>();
		for(HomomorphismProperty c:constraints) {
			if(c instanceof MapProperty) {
				Map<Variable, Constant> m = ((MapProperty) c).mapping;
				for(Entry<Variable, Constant> pair:m.entrySet()) {
					for (Atom fact:source) {
						int it = fact.getTerms().indexOf(pair.getKey());
						if(it != -1) {
							StringBuilder eq = new StringBuilder();
							eq.append(this.aliases.get(fact)==null ? fact.getPredicate().getName():this.aliases.get(fact)).append(".").append(((Relation) fact.getPredicate()).getAttribute(it).getName()).append('=');
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
		result.append(alias==null ? relation.getName():alias).append(".").append(relation.getAttribute(position).getName()).
		append(" IN ").append("(").append(set).append(")");
		return result.toString();

	}

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
		result.append(alias==null ? relation.getName():alias).
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
	protected String createTableAliasingExpression(String alias, Predicate relation) {
		Preconditions.checkNotNull(relation);
		StringBuilder result = new StringBuilder();
		result.append(relation.getName()).append(" AS ");
		result.append(alias==null ? relation.getName():alias);
		return result.toString();
	}

	/**
	 * Setup statements.
	 *
	 * @param databaseName the database name
	 * @return the complete list of SQL statements required to set up the facts database
	 */
	public abstract Collection<String> createDatabaseStatements(String databaseName);

	/**
	 * Cleanup statements.
	 *
	 * @param databaseName String
	 * @return the complete list of SQL statements required to clean up the fact database
	 */
	public abstract Collection<String> createDropStatements(String databaseName);

	/**
	 * Clone.
	 *
	 * @return SQLStatementBuilder
	 */
	@Override
	public abstract SQLStatementBuilder clone();

}
