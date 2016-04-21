package uk.ac.ox.cs.pdq.materialize.sqlstatement;

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
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.materialize.homomorphism.DatabaseRelation;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismProperty.ActiveTriggerProperty;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismProperty.EGDHomomorphismProperty;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismProperty.FactProperty;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismProperty.MapProperty;
import uk.ac.ox.cs.pdq.util.Utility;

import com.beust.jcommander.internal.Lists;
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
	protected BiMap<Atom, String> aliases = HashBiMap.create();

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
	public Collection<String> createInsertStatements(Collection<? extends Atom> facts, Map<String, DatabaseRelation> toDatabaseTables) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact:facts) {
			DatabaseRelation relation = toDatabaseTables.get(fact.getName());
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

	public abstract String createBulkInsertStatement(Predicate predicate, Collection<? extends Atom> facts, Map<String, DatabaseRelation> toDatabaseTables);

	/**
	 * Make deletes.
	 *
	 * @param facts 		Facts to delete from the database
	 * @param toDatabaseTables 		Map of schema relation names to *clean* names
	 * @return 		a set of statements that delete the input facts from the fact database.
	 */
	public Collection<String> createDeleteStatements(Collection<? extends Atom> facts, Map<String, DatabaseRelation> toDatabaseTables) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact:facts) {
			Relation relation = toDatabaseTables.get(fact.getName());
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
	public String createBulkDeleteStatement(Predicate predicate, Collection<? extends Atom> facts, Map<String, DatabaseRelation> toDatabaseTables) {
		String insertInto = "DELETE FROM " + toDatabaseTables.get(predicate.getName()).getName() + " " + "WHERE "; 
		insertInto += DatabaseRelation.Fact.getName();
		insertInto += " IN" + "\n"; 

		List<String> tuples = Lists.newArrayList();
		for (Atom fact:facts) {
			String tuple = "(" + fact.getId() + ")";
			tuples.add(tuple);
		}
		insertInto += "(" + Joiner.on(",").join(tuples) + ")" + ";";
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
	 * @param constraintIndices the constraint indices
	 * @param relation the relation
	 * @param columns the columns
	 * @return a SQL statement that creates an index for the columns of the input relation
	 */
	public Pair<String,String> createTableIndices(boolean isForQuery, Set<String> constraintIndices, DatabaseRelation relation, Integer... columns) {
		StringBuilder indexName = new StringBuilder();
		StringBuilder indexColumns = new StringBuilder();
		String sep1 = "", sep2 = "";
		for (Integer i: columns) {
			indexName.append(sep1).append(relation.getAttribute(i).getName());
			indexColumns.append(sep2).append(relation.getAttribute(i).getName());
			sep1 = "_";
			sep2 = ",";
		}
		//if the index is created for the query
		if(isForQuery) {
			//and is not already existing due to the constraints
			if(constraintIndices.contains(relation.getName() + "_" + indexName))
				return new ImmutablePair<String, String>(null,null);
		}else{
			constraintIndices.add(relation.getName() + "_" + indexName );
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
	public String createColumnIndexStatement(DatabaseRelation relation, StringBuilder indexName, StringBuilder indexColumns) {
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
	public String createDropIndexStatement(DatabaseRelation relation, StringBuilder indexName, StringBuilder indexColumns) {
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
			result.add("TRUNCATE TABLE  " + toDatabaseRelation.get(pred.getName()).getName());
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
	 * @param constraintIndices the constraint indices
	 * @return the pair
	 */
	public Pair<Collection<String>,Collection<String>> setupIndices(boolean isForQuery, Map<String, DatabaseRelation> toDatabaseRelations, Evaluatable rule, Set<String> constraintIndices) {
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
							Pair<String,String> createAndDropIndices = this.createTableIndices(isForQuery, constraintIndices, toDatabaseRelations.get(atom.getName()), i);
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
	protected abstract String translateLimitConstraints(Evaluatable source, HomomorphismProperty... constraints);

	/**
	 * Creates an SQL statement that detects homomorphisms of the input query to facts kept in a database.
	 *
	 * @param source the source
	 * @param constraints 		A set of constraints that should be satisfied by the homomorphisms of the input formula to the facts of the database 
	 * @param constants the constants
	 * @param connection the connection
	 * @return homomorphisms of the input query to facts kept in a database.
	 */
	public Pair<String,LinkedHashMap<String,Variable>> createQuery(Evaluatable source, HomomorphismProperty[] constraints) {

		String query = "";
		List<String> from = this.createFromStatement(source);
		LinkedHashMap<String,Variable> projectedVariables = this.createProjectionStatements(source);
		List<String> predicates = new ArrayList<String>();
		List<String> equalityPredicates = this.createAttributeEqualities((Conjunction<Atom>) source.getBody());
		List<String> constantEqualityPredicates = this.createEqualitiesWithConstants((Conjunction<Atom>) source.getBody());
		List<String> equalityForHomRestrictionsPredicates = this.createEqualitiesForHomConstraints(source, constraints);

		/*
		 * if the target set of facts is not null, we
		 * add in the WHERE statement a predicate which limits the identifiers
		 * of the facts that satisfy any homomorphism to the
		 * identifiers of these facts
		 */
		List<String> factConstraints = this.translateFactConstraints(source, constraints);

		String egdConstraint = this.translateEGDHomomorphismConstraints(source, constraints);
		if(egdConstraint!=null) {
			predicates.add(egdConstraint);
		}
		predicates.addAll(equalityPredicates);
		predicates.addAll(constantEqualityPredicates);
		predicates.addAll(equalityForHomRestrictionsPredicates);
		predicates.addAll(factConstraints);

		//Limit the number of returned homomorphisms
		String limit = this.translateLimitConstraints(source, constraints); 

		query = "SELECT " 	+ Joiner.on(",").join(projectedVariables.keySet()) + "\n" +  
				"FROM " 	+ Joiner.on(",").join(from);
		if(!predicates.isEmpty()) {
			query += "\n" + "WHERE " + Joiner.on(" AND ").join(predicates);
		}	


		boolean activeTriggerConstraint = false;
		for(HomomorphismProperty c:constraints) {
			if(c instanceof ActiveTriggerProperty) {
				activeTriggerConstraint = true;
				break;
			}			
		}

		if(source instanceof Constraint && activeTriggerConstraint) {
			List<String> from2 = null;
			if(source instanceof TGD) {
				from2 = this.createFromStatement(((TGD)source).getHead());
			}
			else if(source instanceof EGD) {
				from2 = this.createFromStatement(((EGD)source).getHead());
			}
			LinkedHashMap<String,Variable> projectedVariables2 = this.createProjectionStatements(source);
			List<String> predicates2 = new ArrayList<String>();
			Conjunction<Atom> conjuncts = Conjunction.of(((Constraint)source).getAtoms());
			List<String> attributeEqualityPredicates2 = this.createAttributeEqualities(conjuncts);
			List<String> attributeConstantEqualityPredicates2 = this.createEqualitiesWithConstants(conjuncts);
			predicates2.addAll(attributeEqualityPredicates2);
			predicates2.addAll(attributeConstantEqualityPredicates2);

			String query2 = 
					"(SELECT " 	+ Joiner.on(",").join(projectedVariables2.keySet()) + "\n" +  
							"FROM " 	+ Joiner.on(",").join(from2);
			if(!predicates2.isEmpty()) {
				query2 += "\n" + "WHERE " + Joiner.on(" AND ").join(predicates2);
			}	
			query2 += ")";

			if(predicates.isEmpty()) {
				query += "\n" + "WHERE " + " NOT EXISTS" + "\n" + query2;
			}	
			else {
				query += "\n" + "AND " + " NOT EXISTS" + "\n" + query2;
			}
		}

		if(limit != null) {
			query += "\n" + limit;
		}

		log.trace(source);
		log.trace(query);
		log.trace("\n\n");

		return Pair.of(query, projectedVariables);
	}

	/**
	 * Creates the content for from statement.
	 *
	 * @param source the source
	 * @return 		a list of the table names that will be queried
	 */
	protected List<String> createFromStatement(Evaluatable source) {
		List<String> relations = new ArrayList<String>();
		this.aliases = HashBiMap.create();;
		for (Atom fact:source.getBody().getAtoms()) {
			String aliasName = this.aliasPrefix + this.aliasCounter;
			relations.add(createTableAliasingExpression(aliasName, (Relation) fact.getPredicate()));
			this.aliases.put(fact, aliasName);
			this.aliasCounter++;
		}
		return relations;
	}

	/**
	 * Creates the content for from statement.
	 *
	 * @param source the source
	 * @return 		a list of the table names that will be queried
	 */
	protected List<String> createFromStatement(Conjunction<? extends Atom> predicates) {
		List<String> relations = new ArrayList<String>();
		for (Atom fact:predicates) {
			String aliasName = this.aliasPrefix + this.aliasCounter;
			relations.add(createTableAliasingExpression(aliasName, (Relation) fact.getPredicate()));
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
					projected.put(createProjectionStatementForArgument(it, (Relation) fact.getPredicate(), alias), (Variable)term);
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
	 * @return 		explicit equalities (String objects of the form A.x1 = B.x2) of the implicit equalities in the input conjunction (the latter is denoted by repetition of the same term)
	 */
	protected List<String> createAttributeEqualities(Conjunction<Atom> source) {
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


	/**
	 * Creates the equalities with constants.
	 *
	 * @param source the source
	 * @return 		constant equality predicates
	 */
	protected List<String> createEqualitiesWithConstants(Conjunction<Atom> source) {
		List<String> constantPredicates = new ArrayList<>();
		for (Atom fact:source.getAtoms()) {
			String alias = this.aliases.get(fact);
			List<Term> terms = fact.getTerms();
			for (int it = 0; it < terms.size(); ++it) {
				Term term = terms.get(it);
				if (!term.isVariable() && !term.isSkolem()) {
					StringBuilder eq = new StringBuilder();
					eq.append(alias==null ? fact.getPredicate().getName():alias).append(".").append(((Relation) fact.getPredicate()).getAttribute(it).getName()).append('=');
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
	 * @param constraints the constraints
	 * @return 		predicates that correspond to fact constraints
	 */
	protected List<String> translateFactConstraints(Evaluatable source, HomomorphismProperty... constraints) {
		List<String> setPredicates = new ArrayList<>();
		for(HomomorphismProperty c:constraints) {
			if(c instanceof FactProperty) {
				List<Object> facts = new ArrayList<>();
				for (Atom atom:((FactProperty) c).atoms) {
					facts.add(atom.getId());
				}
				for(Atom fact:source.getBody().getAtoms()) {
					String alias = this.aliases.get(fact);
					setPredicates.add(createSQLMembershipExpression(fact.getTermsCount()-1, facts, (Relation) fact.getPredicate(), alias));
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
	protected List<String> createEqualitiesForHomConstraints(Evaluatable source, HomomorphismProperty... constraints) {
		List<String> constantPredicates = new ArrayList<>();
		for(HomomorphismProperty c:constraints) {
			if(c instanceof MapProperty) {
				Map<Variable, Constant> m = ((MapProperty) c).mapping;
				for(Entry<Variable, Constant> pair:m.entrySet()) {
					for (Atom fact:source.getBody().getAtoms()) {
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
	 * Translate egd homomorphism constraints.
	 *
	 * @param source the source
	 * @param constraints the constraints
	 * @return 		predicates that correspond to fact constraints
	 */
	protected String translateEGDHomomorphismConstraints(Evaluatable source, HomomorphismProperty... constraints) {
		for(HomomorphismProperty c:constraints) {
			if(c instanceof EGDHomomorphismProperty) {
				List<Atom> conjuncts = source.getBody().getAtoms();
				String lalias = this.aliases.get(conjuncts.get(0));
				String ralias = this.aliases.get(conjuncts.get(1));
				lalias = lalias==null ? conjuncts.get(0).getPredicate().getName():lalias;
				ralias = ralias==null ? conjuncts.get(1).getPredicate().getName():ralias;
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
	protected String createTableAliasingExpression(String alias, Relation relation) {
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
