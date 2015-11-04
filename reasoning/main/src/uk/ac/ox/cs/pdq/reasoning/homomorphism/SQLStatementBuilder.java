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

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager.DBRelation;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.FactScope;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.ParametrisedMatch;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.SuperMap;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Creates SQL statements for relation database-backed homomorphism detectors.
 *

 * @author Efthymia Tsamoura
 * @author George Konstantinidis
 */
public abstract class SQLStatementBuilder {

	/** Logger. */
	private static Logger log = Logger.getLogger(SQLStatementBuilder.class);

	/** Aliases for facts **/
	protected BiMap<Predicate, String> aliases = HashBiMap.create();

	private String aliasPrefix = "A";
	private int aliasCounter = 0;

	public int cleanNameCounter =0;
	
	/** maps all relation names to clean ones **/
	protected BiMap<String, String> cleanMap = HashBiMap.create();

	/**
	 * @param databaseName
	 * @return the complete list of SQL statements required to set up the fact database
	 */
	public abstract Collection<String> setupStatements(String databaseName);

	/**
	 * @param databaseName String
	 * @return the complete list of SQL statements required to clean up the fact database
	 */
	public abstract Collection<String> cleanupStatements(String databaseName);

	/**
	 *
	 * @param relation the table to drop
	 * @return a SQL statement for dropping the facts table of the given relation
	 */
	protected String dropTableStatement(DBRelation relation) {
		return "DROP TABLE " + encodeName(relation.getName());
	}

	/**
	 * @param name
	 * @return a new String that is an alias of the given name modified such that
	 * it is acceptable for the underlying system
	 */
	public String encodeName(String dirtyRelationName)
	{
		if(!cleanMap.containsKey(dirtyRelationName))
			cleanMap.put(dirtyRelationName, "cleanR"+cleanNameCounter++);
		return cleanMap.get(dirtyRelationName);
	}

	/**
	 * @param facts
	 * @return insert statements that add the input fact to the fact database.
	 */
	protected Collection<String> makeInserts(Collection<? extends Predicate> facts, Map<String, DBRelation> dbrelations) {
		Collection<String> result = new LinkedList<>();
		for (Predicate fact : facts) {
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
		return result;
	}

	/**
	 * @param relation the table to create
	 * @return a SQL statement that creates the fact table of the given relation
	 */
	protected String createTableStatement(DBRelation relation) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE  ").append(this.encodeName(relation.getName())).append('(');
		for (int it = 0; it < relation.getAttributes().size(); ++it) {
			result.append(' ').append(relation.getAttributes().get(it).getName());
			if(relation.getAttribute(it).getType().toString().contains("java.lang.String") ) {
				result.append(" VARCHAR(500),");
			}
			else if(relation.getAttribute(it).getType().toString().contains("java.lang.Integer") ) {
				result.append(" int");
				if(it < relation.getAttributes().size() - 1) {
					result.append(",");
				}
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
		}
		result.append(')');
		return result.toString();
	}

	/**
	 * @param relation
	 * @param columns
	 * @return a SQL statement that creates an index for the columns of the input relation
	 */
	protected String createTableIndex(DBRelation relation, Integer... columns) {
		StringBuilder indexName = new StringBuilder();
		StringBuilder indexColumns = new StringBuilder();
		String sep1 = "", sep2 = "";
		for (Integer i: columns) {
			indexName.append(sep1).append(relation.getAttribute(i).getName());
			indexColumns.append(sep2).append(relation.getAttribute(i).getName());
			sep1 = "_";
			sep2 = ",";
		}
		return "CREATE INDEX idx_" + this.encodeName(relation.getName()) + "_" + indexName +
				" ON " + this.encodeName(relation.getName()) + "(" + indexColumns + ")";
	}

	/**
	 * @param relation
	 * @return a SQL statement that creates an index for the bag and fact attributes of the database tables
	 */
	protected String createTableNonJoinIndexes(DBRelation relation, Attribute column) {
		return "CREATE INDEX idx_" + this.encodeName(relation.getName()) + "_" + 
				column.getName() + " ON " + this.encodeName(relation.getName()) + "(" + column.getName() + ")";
	}

	/**
	 * 
	 * @param relationMap
	 * @param rule
	 * @return
	 */
	protected Collection<String> createTableIndexes(Map<String, DBRelation> relationMap, Evaluatable rule) {
		Conjunction<?> body = null;
		if (rule.getBody() instanceof Predicate) {
			body = Conjunction.of((Predicate) rule.getBody());
		} else if (rule.getBody() instanceof Conjunction<?>) {
			body = (Conjunction) rule.getBody();
		} else {
			throw new UnsupportedOperationException("Homomorphism check only supported on conjunction of atomic predicate formulas for now.");
		}
		Set<String> result = new LinkedHashSet<>();
		Multimap<Variable, Predicate> clusters = LinkedHashMultimap.create();
		for (Formula subFormula: body) {
			if (subFormula instanceof Predicate) {
				for (Term t: subFormula.getTerms()) {
					if (t instanceof Variable) {
						clusters.put((Variable) t, (Predicate) subFormula);
					}
				}
			} else {
				throw new UnsupportedOperationException("Homomorphism check only supported on conjunction of atomic predicate formulas for now.");
			}
		}
		for (Variable t: clusters.keys()) {
			Collection<Predicate> atoms = clusters.get(t);
			if (atoms.size() > 1) {
				for (Predicate atom: atoms) {
					for (int i = 0, l = atom.getTermsCount(); i < l; i++) {
						if (atom.getTerm(i).equals(t)) {
							result.add(this.createTableIndex(relationMap.get(atom.getName()), i));
						}
					}
				}
			}
		}
		return result;
	}

	protected abstract String translateLimitConstraints(Evaluatable source, HomomorphismConstraint... constraints);

	/**
	 * Creates and runs an SQL statement that detects homomorphisms of the input query to facts kept in a database
	 * @param source
	 * @param constraints
	 * 			The homomorphism constraints that should be satisfied
	 * @return homomorphisms of the input query to facts kept in a database.
	 */
	public Set<Map<Variable, Constant>> findHomomorphismsThroughSQL(Evaluatable source, HomomorphismConstraint[] constraints, Map<String, TypedConstant<?>> constants, Connection connection) {

		String query = "";
		List<String> from = this.createContentForFromStatement(source);
		LinkedHashMap<String,Variable> projectionStatementsAndVariables = this.createProjectionStatements(source);
		List<String> predicates = new ArrayList<String>();
		List<String> attributeEqualityPredicates = this.createAttributeEqualities((Conjunction<Predicate>) source.getBody(), this.aliases);
		List<String> attributeConstantEqualityPredicates = this.createEqualitiesWithConstants((Conjunction<Predicate>) source.getBody(), this.aliases);
		List<String> equalityForHomRestrictionsPredicates = this.createEqualitiesForHomConstraints(source, this.aliases, constraints);

		/*
		 * if the target set of facts is not null, we
		 * add in the WHERE statement a predicate which limits the identifiers
		 * of the facts that satisfy any homomorphism to the
		 * identifiers of these facts
		 */
		List<String> factConstraints = this.translateFactConstraints(source, this.aliases, constraints);

		String parametrisedMatches = this.translateParametrisedMatch(source, this.aliases, constraints);

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
		if(parametrisedMatches != null) {
			if(!predicates.isEmpty()) {
				query += "\n" + "AND " + "(" + parametrisedMatches + ")";
			}
			else {
				query += "\n" + "WHERE " + "(" + parametrisedMatches + ")";
			}
		}
		if(limit != null) {
			query += "\n" + limit;
		}

		log.debug(source);
		log.debug(query);
		log.debug("\n\n");

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
					String assigned = resultSet.getString(f);
					TypedConstant<?> constant = constants.get(assigned);
					Constant constantTerm = constant != null ? constant : new Skolem(assigned);
					map.put(entry.getValue(), constantTerm);
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
	 * 
	 * @param source
	 * @return
	 * 		a list of the table names that will be queried
	 */
	protected List<String> createContentForFromStatement(Evaluatable source) {
		this.aliasCounter = 0;
		List<String> relations = new ArrayList<String>();
		this.aliases = HashBiMap.create();;
		for (Predicate fact:source.getBody().getPredicates()) {
			String aliasName = this.aliasPrefix + this.aliasCounter;
			relations.add(createTableAliasingExpression(aliasName, (Relation) fact.getSignature()));
			this.aliases.put(fact, aliasName);
			this.aliasCounter++;
		}
		return relations;
	}

	/**
	 * 
	 * @param source
	 * @return
	 * 		the attributes that will be projected. 
	 * 		If the input is an egd or tgd we project the attributes that map to universally quantified variables.
	 * 		If the input is a query we project the attributes that map to its free variables. 
	 */
	protected LinkedHashMap<String,Variable> createProjectionStatements(Evaluatable source) {
		LinkedHashMap<String,Variable> projected = new LinkedHashMap<>();
		List<Variable> attributes = new ArrayList<>();
		for (Predicate fact:source.getBody().getPredicates()) {
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
	 * 
	 * @param source
	 * @return
	 * 		explicit equalities (String objects of the form A.x1 = B.x2) of the implicit equalities in the input conjunction (the latter is denoted by repetition of the same term)
	 */
	protected List<String> createAttributeEqualities(Conjunction<Predicate> source, BiMap<Predicate, String> aliases2) {
		List<String> attributePredicates = new ArrayList<String>();
		Collection<Term> terms = Utility.getTerms(source.getPredicates());
		terms = Utility.removeDuplicates(terms);
		for (Term term:terms) {
			Integer leftPosition = null;
			Relation leftRelation = null;
			String leftAlias = null;
			for (Predicate fact:source.getPredicates()) {
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
						result.append(leftAlias==null ? encodeName(leftRelation.getName()):leftAlias).append(".").append(leftRelation.getAttribute(leftPosition).getName()).append('=');
						result.append(rightAlias==null ? encodeName(rightRelation.getName()):rightAlias).append(".").append(rightRelation.getAttribute(rightPosition).getName());
						attributePredicates.add(result.toString());
					}
				}
			}
		}
		return attributePredicates;
	}


	/**
	 * 
	 * @param source
	 * @return
	 * 		constant equality predicates 
	 */
	protected List<String> createEqualitiesWithConstants(Conjunction<Predicate> source, BiMap<Predicate, String> aliases2) {
		List<String> constantPredicates = new ArrayList<>();
		for (Predicate fact:source.getPredicates()) {
			String alias = aliases2.get(fact);
			List<Term> terms = fact.getTerms();
			for (int it = 0; it < terms.size(); ++it) {
				Term term = terms.get(it);
				if (!term.isVariable() && !term.isSkolem()) {
					StringBuilder eq = new StringBuilder();
					eq.append(alias==null ? encodeName(fact.getSignature().getName()):alias).append(".").append(((Relation) fact.getSignature()).getAttribute(it).getName()).append('=');
					eq.append("'").append(((TypedConstant) term).toString()).append("'");
					constantPredicates.add(eq.toString());
				}
			}
		}
		return constantPredicates;
	}

	/**
	 * 
	 * @param source
	 * @param constraints
	 * @return
	 * 		predicates that correspond to fact constraints 
	 */
	protected List<String> translateFactConstraints(Evaluatable source, BiMap<Predicate, String> aliases2, HomomorphismConstraint... constraints) {
		List<String> setPredicates = new ArrayList<>();
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof FactScope) {
				List<Object> facts = new ArrayList<>();
				for (Predicate atom:((FactScope) c).atoms) {
					facts.add(atom.getId());
				}
				for(Predicate fact:source.getBody().getPredicates()) {
					String alias = aliases2.get(fact);
					setPredicates.add(createSQLMembershipExpression(fact.getTermsCount()-1, facts, (Relation) fact.getSignature(), alias));
				}
			}
		}
		return setPredicates;
	}

	/**
	 * 
	 * @param source
	 * @param constraints
	 * @return
	 * 		predicates that correspond to canonical constraints
	 */
	protected List<String> createEqualitiesForHomConstraints(Evaluatable source, BiMap<Predicate, String> aliases2, HomomorphismConstraint... constraints) {
		List<String> constantPredicates = new ArrayList<>();
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof SuperMap) {
				Map<Variable, Constant> m = ((SuperMap) c).mapping;
				for(Entry<Variable, Constant> pair:m.entrySet()) {
					for (Predicate fact:source.getBody().getPredicates()) {
						int it = fact.getTerms().indexOf(pair.getKey());
						if(it != -1) {
							StringBuilder eq = new StringBuilder();
							eq.append(aliases2.get(fact)==null ? encodeName(fact.getSignature().getName()):aliases2.get(fact)).append(".").append(((Relation) fact.getSignature()).getAttribute(it).getName()).append('=');
							eq.append("'").append(pair.getValue()).append("'");
							constantPredicates.add(eq.toString());
						}
					}
				}
			}
		}
		return constantPredicates;
	}

	protected String translateParametrisedMatch(Evaluatable source, BiMap<Predicate, String> aliases2, HomomorphismConstraint... constraints) {
		List<String> constantPredicates = new ArrayList<>();
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof ParametrisedMatch) {
				boolean isStrong = ((ParametrisedMatch) c).isStrong;
				Collection<Variable> variables = ((ParametrisedMatch) c).variables;
				List<Object> constants = Lists.newArrayList();
				constants.addAll(((ParametrisedMatch) c).constants);
				for(Variable variable:variables) {
					for (Predicate fact:source.getBody().getPredicates()) {
						int it = fact.getTerms().indexOf(variable);
						if(it != -1) {
							constantPredicates.add(createSQLMembershipExpression(it, constants, (Relation) fact.getSignature(), aliases2.get(fact)));
						}
					}
				}
				return isStrong == true ? Joiner.on(" AND ").join(constantPredicates) : Joiner.on(" OR ").join(constantPredicates);
			}
		}
		return null;
	}

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
		
		result.append(alias==null ? encodeName(relation.getName()):alias).append(".").append(relation.getAttribute(position).getName()).
		append(" IN ").append("(").append(set).append(")");
		return result.toString();
	
	}

	/**
	 * @return SQLStatementBuilder
	 */
	@Override
	public abstract SQLStatementBuilder clone();

	protected String createProjectionStatementForArgument(int position, Relation relation, String alias) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkArgument(position >= 0 && position < relation.getArity());
		StringBuilder result = new StringBuilder();
		result.append(alias==null ? encodeName(relation.getName()):alias).
		append(".").append(relation.getAttribute(position).getName());
		return result.toString();
	}
	

	/**
	 * 
	 *
	 *
	 */
	protected String createTableAliasingExpression(String alias, Relation relation) {
		Preconditions.checkNotNull(relation);
		StringBuilder result = new StringBuilder();
		result.append(encodeName(relation.getName())).append(" AS ");
		result.append(alias==null ? relation.getName():alias);
		return result.toString();
	}


}
