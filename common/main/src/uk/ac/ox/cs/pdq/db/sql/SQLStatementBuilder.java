package uk.ac.ox.cs.pdq.db.sql;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.junit.Assert;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Creates SQL statements to detect homomorphisms or add/delete facts in a
 * database.
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public abstract class SQLStatementBuilder {

	private static Logger log = Logger.getLogger(SQLStatementBuilder.class);

	/** Aliases for the relations in the query FROM statements. */
	public BiMap<Atom, String> aliases = HashBiMap.create();

	/** The alias prefix. */
	public static final String ALIAS_PREFIX = "A";

	protected String databaseName;

	/**
	 * Creates SQL insert statements, for the input facts.
	 *
	 * @param facts
	 *            the facts
	 * @param toDatabaseTables
	 *            map to the relation objects
	 * @return insert statements that add the input fact to the fact database.
	 */
	public Collection<String> createInsertStatements(Collection<Atom> facts, Map<String, Relation> relationNamesToDatabaseTables,Schema schema) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact : facts) {
			Assert.assertTrue(relationNamesToDatabaseTables.containsKey(fact.getPredicate().getName()));
			Relation relation = relationNamesToDatabaseTables.get(fact.getPredicate().getName());
			String insertInto = "INSERT INTO " + databaseName + "." + fact.getPredicate().getName() + " " + "VALUES ( ";
			for (int termIndex = 0; termIndex < fact.getNumberOfTerms(); ++termIndex) {
				Term term = fact.getTerm(termIndex);
				if (!term.isVariable())
					if (String.class.isAssignableFrom((Class<?>) relation.getAttribute(termIndex).getType()))
						insertInto += "'" + term + "'";
					else if (Integer.class.isAssignableFrom((Class<?>) relation.getAttribute(termIndex).getType()))
						insertInto += term;
					else if (Double.class.isAssignableFrom((Class<?>) relation.getAttribute(termIndex).getType()))
						insertInto += term;
					else if (Float.class.isAssignableFrom((Class<?>) relation.getAttribute(termIndex).getType()))
						insertInto += term;
					else
						throw new RuntimeException("Unsupported type");
				if (termIndex < fact.getNumberOfTerms() - 1)
					insertInto += ",";
			}
			insertInto += ")";
			result.add(insertInto);
		}
		log.trace(result);
		return result;
	}

	/**
	 * Creates one SQL insert statements for the input predicate that inserts all
	 * input facts to the underlying database relation instance for this predicate.
	 *
	 * @param facts
	 *            the facts to be inserted
	 * @return insert statements that add the input facts to the underlying RDBMS
	 */
	public abstract String createBulkInsertStatement(Predicate predicate, Collection<Atom> facts);

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
	public String createBulkDeleteStatement(Predicate predicate, Collection<Atom> facts, Map<String, Relation> relationNamesToDatabaseTables) {
		Relation relation = relationNamesToDatabaseTables.get(predicate.getName());
		String deleteFrom = "DELETE FROM " + databaseName + "." + relation.getName() + " " + "WHERE ";
		String lastAttributeName = relation.getAttribute(relation.getArity() - 1).getName();
		deleteFrom += lastAttributeName;
		deleteFrom += " IN" + "\n";

		List<String> tuples = new ArrayList<String>();
		for (Atom fact : facts) {
			String tuple = fact.getId() + "";
			tuples.add(tuple);
		}
		deleteFrom += "(" + Joiner.on(",").join(tuples) + ")";
		return deleteFrom;
	}

	/**
	 * Creates the table statement.
	 *
	 * @param relation
	 *            the table to create
	 * @return a SQL statement that creates the fact table of the given relation
	 */
	public String createTableStatement(Relation relation) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE " + databaseName + ".").append(relation.getName()).append('(');
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
		log.trace(relation);
		log.trace(result);
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
		return "CREATE INDEX idx_" + relation.getName() + "_" + indexName + " ON " + databaseName + "." + relation.getName() + "(" + indexColumns + ")";
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
		return "DROP INDEX idx_" + relation.getName() + "_" + indexName + " ON " + databaseName + "." + relation.getName();
	}

	/**
	 * Clear tables.
	 *
	 * @param queryRelations
	 *            the query relations
	 * @param toDatabaseRelation
	 *            the relation map
	 * @return the collection
	 */
	public Collection<String> createTruncateTableStatements(Atom[] queryRelations, Map<String, Relation> toDatabaseRelation) {
		Set<String> result = new LinkedHashSet<>();
		for (Atom pred : queryRelations)
			result.add("TRUNCATE TABLE  " + databaseName + "." + toDatabaseRelation.get(pred.getPredicate().getName()).getName());
		return result;
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
	public String createColumnIndexStatement(Relation relation, Attribute column) {
		return "CREATE INDEX idx_" + relation.getName() + "_" + column.getName() + " ON " + databaseName + "." +  relation.getName() + "(" + column.getName() + ")";
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
	public Pair<Collection<String>, Collection<String>> setupIndices(boolean isForQuery, Map<String, Relation> relationNamesToDatabaseTables, Formula rule,
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

	public FromCondition createFromStatement(Atom[] facts) {
		List<String> relations = new ArrayList<String>();
		for (Atom fact : facts) {
			String aliasName = null;
			synchronized (this.aliases) {
				aliasName = this.aliases.get(fact);
				if (aliasName == null) {
					aliasName = SQLStatementBuilder.ALIAS_PREFIX + GlobalCounterProvider.getNext("SQLStatmentBuilder.aliasCounter");
					this.aliases.put(fact, aliasName);
				}
			}
			relations.add(this.createTableAliasingExpression(aliasName, fact.getPredicate()));
		}
		return new FromCondition(relations);
	}

	/**
	 * Creates the projection statements.
	 *
	 * @param source
	 *            the source
	 * @return the attributes that will be projected. If the input is an egd or tgd
	 *         we project the attributes that map to universally quantified
	 *         variables. If the input is a query we project the attributes that map
	 *         to its free variables.
	 */
	public SelectCondition createProjections(Atom[] atoms,Schema schema) {
		LinkedHashMap<String, Variable> projected = new LinkedHashMap<>();
		List<Variable> attributes = new ArrayList<>();
		for (Atom fact : atoms) {
			String alias = null;
			synchronized (this.aliases) {
				alias = this.aliases.get(fact);
			}
			for (int index = 0; index < fact.getNumberOfTerms(); ++index) {
				Term term = fact.getTerm(index);
				// Most likely broken if replaced with working one.
				// if (term instanceof Variable && !attributes.contains(((Variable)
				// term).getSymbol())) {
				if (term instanceof Variable && !attributes.contains(term)) {
					projected.put(createProjectionStatementForArgument(index, schema.getRelation(fact.getPredicate().getName()), alias), (Variable) term);
					attributes.add(((Variable) term));
				}
			}
		}
		return new SelectCondition(projected);
	}

	public WhereCondition createAttributeEqualities(Atom[] source,Schema schema) {
		List<String> attributePredicates = new ArrayList<String>();
		Collection<Term> terms = Utility.getTerms(source);
		terms = Utility.removeDuplicates(terms);
		for (Term term : terms) {
			Integer leftPosition = null;
			Relation leftRelation = null;
			String leftAlias = null;
			for (Atom fact : source) {
				List<Integer> positions = Utility.search(fact.getTerms(), term); // all the positions for the same term should be equated
				for (Integer pos : positions) {
					if (leftPosition == null) {
						leftPosition = pos;
						leftRelation = schema.getRelation(fact.getPredicate().getName());
						synchronized (this.aliases) {
							leftAlias = this.aliases.get(fact);
						}
					} else {
						Integer rightPosition = pos;
						Relation rightRelation = schema.getRelation(fact.getPredicate().getName());
						String rightAlias = null;
						synchronized (this.aliases) {
							rightAlias = this.aliases.get(fact);
						}
						StringBuilder result = new StringBuilder();
						result.append(leftAlias == null ? leftRelation.getName() : leftAlias).append(".").append(leftRelation.getAttribute(leftPosition).getName()).append('=');
						result.append(rightAlias == null ? rightRelation.getName() : rightAlias).append(".").append(rightRelation.getAttribute(rightPosition).getName());
						attributePredicates.add(result.toString());
					}
				}
			}
		}
		return new WhereCondition(attributePredicates);
	}

	/**
	 * Creates a WhereCondition for and EGD to make sure only active triggers will be returned by the query.
	 * 
	 */
	public WhereCondition createEGDActivenessFilter(EGD dep, Atom[] source,Schema schema) {
		List<String> attributePredicates = new ArrayList<String>();
		Collection<Term> terms = Utility.getTerms(source);
		terms = Utility.removeDuplicates(terms);
		if (dep != null) {
			Term right = dep.getHead().getAtoms()[0].getTerm(1);
			Term left = dep.getHead().getAtoms()[0].getTerm(0);
			ArrayList<String> leftEqualities = new ArrayList<>();
			ArrayList<String> rightEqualities = new ArrayList<>();
			for (Term term : terms) {
				for (Atom fact : source) {
					List<Integer> positions = Utility.search(fact.getTerms(), term); // all the positions for the same term should be equated
					for (Integer pos : positions) {
						if (left != null && left.equals(fact.getTerm(pos))) {
							StringBuilder result = new StringBuilder();
							synchronized (this.aliases) {
								result.append(this.aliases.get(fact) == null ? fact.getPredicate().getName() : this.aliases.get(fact));
							}
							result.append(".").append(schema.getRelation(fact.getPredicate().getName()).getAttribute(pos).getName());
							leftEqualities.add(result.toString());
						}
						if (right != null && right.equals(fact.getTerm(pos))) {
							StringBuilder result = new StringBuilder();
							synchronized (this.aliases) {
								result.append(this.aliases.get(fact) == null ? fact.getPredicate().getName() : this.aliases.get(fact));
							}
							result.append(".").append(schema.getRelation(fact.getPredicate().getName()).getAttribute(pos).getName());
							rightEqualities.add(result.toString());
						}
					}
				}
			}
			for (String s1 : leftEqualities) {
				for (String s2 : rightEqualities) {
					attributePredicates.add(s1 + "<>" + s2);
				}
			}
		}
		return new WhereCondition(attributePredicates);
	}

	public WhereCondition createEqualitiesWithConstants(Atom[] source,Schema schema) {
		List<String> constantPredicates = new ArrayList<>();
		for (Atom fact : source) {
			String alias = null;
			synchronized (this.aliases) {
				alias = this.aliases.get(fact);
			}
			for (int index = 0; index < fact.getNumberOfTerms(); ++index) {
				Term term = fact.getTerm(index);
				if (!term.isVariable() && !term.isUntypedConstant()) {
					StringBuilder eq = new StringBuilder();
					eq.append(alias == null ? fact.getPredicate().getName() : alias).append(".").append(schema.getRelation(fact.getPredicate().getName()).getAttribute(index).getName()).append('=');
					eq.append("'").append(((TypedConstant)term).serializeToString()).append("'");
					constantPredicates.add(eq.toString());
				}
			}
		}
		return new WhereCondition(constantPredicates);
	}

	public WhereCondition enforceStateMembership(Atom[] source, Map<String, Relation> relationNamesToRelationObjects, Collection<Atom> facts) {
		List<String> setPredicates = new ArrayList<>();
		List<Object> factIDs = new ArrayList<>();
		for (Atom atom : facts) {
			factIDs.add(atom.getId());
		}
		for (Atom fact : source) {
			String alias = null;
			synchronized (this.aliases) {
				alias = this.aliases.get(fact);
			}
			setPredicates.add(createSQLMembershipExpression(relationNamesToRelationObjects.get(fact.getPredicate().getName()).getArity() - 1, factIDs,
					relationNamesToRelationObjects.get(fact.getPredicate().getName()), alias));
		}
		return new WhereCondition(setPredicates);
	}

	public WhereCondition createEqualitiesRespectingInputMapping(Atom[] source, Map<Variable, Constant> mapping,Schema schema) {
		List<String> constantPredicates = new ArrayList<>();
		for (Entry<Variable, Constant> pair : mapping.entrySet()) {
			for (Atom fact : source) {
				int index = Arrays.asList(fact.getTerms()).indexOf(pair.getKey());
				if (index != -1) {
					StringBuilder eq = new StringBuilder();
					synchronized (this.aliases) {
						eq.append(this.aliases.get(fact) == null ? fact.getPredicate().getName() : this.aliases.get(fact)).append(".")
						.append(schema.getRelation(fact.getPredicate().getName()).getAttribute(index).getName()).append('=');
					}
					eq.append("'").append(pair.getValue()).append("'");
					constantPredicates.add(eq.toString());
				}
			}
		}
		return new WhereCondition(constantPredicates);
	}

	/**
	 * Creates the sql membership expression.
	 *
	 * @param position
	 *            the position
	 * @param values
	 *            the values
	 * @param relation
	 *            the relation
	 * @param alias
	 *            the alias
	 * @return the string
	 */
	private String createSQLMembershipExpression(int position, List<Object> values, Relation relation, String alias) {
		StringBuilder result = new StringBuilder();
		String set = "";
		int v = 0;
		for (v = 0; v < values.size() - 1; ++v)
			set += convertValue(values.get(v)) + ",";
		set += convertValue(values.get(v));
		result.append(alias == null ? relation.getName() : alias).append(".").append(relation.getAttribute(position).getName()).append(" IN ").append("(").append(set).append(")");
		return result.toString();
	}

	private String convertValue(Object object) {
		if (object == null)
			throw new IllegalArgumentException("Null value cannot be converted to SQL statement");
		if (object instanceof String)
			return "'" + object + "'";
		return object.toString();
	}

	/**
	 * Creates the projection statement for argument.
	 *
	 * @param position
	 *            the position
	 * @param relation
	 *            the relation
	 * @param alias
	 *            the alias
	 * @return the string
	 */
	protected String createProjectionStatementForArgument(int position, Relation relation, String alias) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkArgument(position >= 0 && position < relation.getArity());
		StringBuilder result = new StringBuilder();
		result.append(alias == null ? relation.getName() : alias).append(".").append(relation.getAttribute(position).getName());
		return result.toString();
	}

	/**
	 * Creates the table aliasing expression.
	 *
	 * @param alias
	 *            the alias
	 * @param relation
	 *            the relation
	 * @return the string
	 */
	protected String createTableAliasingExpression(String alias, Predicate relation) {
		Preconditions.checkNotNull(relation);
		StringBuilder result = new StringBuilder();
		result.append(relation.getName()).append(" AS ");
		result.append(alias == null ? relation.getName() : alias);
		return result.toString();
	}

	/**
	 * Setup statements.
	 *
	 * @param databaseName
	 *            the database name
	 * @return the complete list of SQL statements required to set up the facts
	 *         database
	 */
	public abstract Collection<String> createDatabaseStatements(String databaseName);

	/**
	 * Cleanup statements.
	 *
	 * @param databaseName
	 *            String
	 * @return the complete list of SQL statements required to clean up the fact
	 *         database
	 */
	public abstract Collection<String> createDropStatements(String databaseName);

	/**
	 * Clone.
	 *
	 * @return SQLStatementBuilder
	 */
	@Override
	public abstract SQLStatementBuilder clone();

	public String buildSQLQuery(SelectCondition select, FromCondition from, WhereCondition where) {
		String query = "SELECT " + select.getConditionsSQLSubstring() + "\n" + "FROM " + from.getConditionsSQLSubstring(databaseName);
		if (!where.isEmpty())
			query += "\n" + "WHERE " + where.getConditionsSQLSubstring();
		return query;
	}

	public String nestQueries(String query, WhereCondition where, String nestedQuery) {
		if (where.isEmpty())
			query += "\n" + "WHERE " + " NOT EXISTS" + "\n" + "(" + nestedQuery + ")";
		else
			query += "\n" + "AND " + " NOT EXISTS" + "\n" + "(" + nestedQuery + ")";
		return query;
	}

	public String getDatabaseName() {
		return databaseName;
	}

}
