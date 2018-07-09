package uk.ac.ox.cs.pdq.regression.utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Translates queries from the internal PDQ format to SQL
 * 
 */
public class ConjunctiveQueryToSQL {
	
	protected boolean aliasSelections = false;
	
	/** The alias prefix. */
	private String projectAliasPrefix = "P";

	/** The alias counter. */
	private int projectAliasCounter = 0;
	
	/**  Aliases for the relations in the query FROM statements. */
	protected BiMap<Atom, String> aliases = HashBiMap.create();

	/** The alias prefix. */
	private String tableAliasPrefix = "A";

	/** The alias counter. */
	private int tableAliasCounter = 0;
	
	public ConjunctiveQueryToSQL() {
		this.aliasSelections = false;
	}
	
	public ConjunctiveQueryToSQL(boolean aliasSelections) {
		this.aliasSelections = aliasSelections;
	}
	
	public String translate(View view, Schema schema, String databaseSchemaName) {
		Atom[] viewAtoms = view.getViewToRelationDependency().getHeadAtoms();
		List<String> fromStatemement = this.createFromStatement(viewAtoms, databaseSchemaName);
		List<String> projections = this.createProjectionStatement(view, schema);
		List<String> predicates = Lists.newArrayList();
		predicates.addAll(this.createAttributeEqualities(viewAtoms, schema));
		predicates.addAll(this.createConstantEqualities(viewAtoms, schema));
		String sql = "SELECT " 	+ Joiner.on(",").join(projections) + "\n" +  
				"FROM " 	+ Joiner.on(",").join(fromStatemement) + "\n" ;	
		if(!predicates.isEmpty()) {
			sql += "WHERE " + Joiner.on(" AND ").join(predicates);
		}	
		return sql;
	}
	
	public String translate(ConjunctiveQuery query, Schema schema, String databaseSchemaName) {
		Atom[] queryAtoms = query.getAtoms();
		List<String> fromStatemement = this.createFromStatement(queryAtoms, databaseSchemaName);
		List<String> projections = this.createProjectionStatement(query, schema);
		List<String> predicates = Lists.newArrayList();
		predicates.addAll(this.createAttributeEqualities(queryAtoms, schema));
		predicates.addAll(this.createConstantEqualities(queryAtoms, schema));
		String sql = "SELECT " 	+ Joiner.on(",").join(projections) + "\n" +  
				"FROM " 	+ Joiner.on(",").join(fromStatemement) + "\n" ;
		if(!predicates.isEmpty()) {
			sql += "WHERE " + Joiner.on(" AND ").join(predicates);
		}	
		return sql;
	}

	/**
	 * Creates the content for from statement.
	 * @param conjuncts the query body
	 * @return
	 */
	protected List<String> createFromStatement(Atom[] conjuncts, String schema) {
		List<String> relations = new ArrayList<String>();
		for (Atom fact:conjuncts) {
			String aliasName = createAlias(fact.getPredicate().getName());
			relations.add(this.createTableAliasingExpression(aliasName, fact.getPredicate(), schema));
			this.aliases.put(fact, aliasName);
			this.tableAliasCounter++;
		}
		return relations;
	}
	
	/**
	 * Creates the content for from statement.
	 * @param conjuncts the query body
	 * @return
	 */
	protected List<String> createFromStatement(Atom[] conjuncts) {
		return this.createFromStatement(conjuncts, null);
	}
	
	/**
	 * Creates the projection statements.
	 * @param query the input query
	 * @return
	 */
	protected List<String> createProjectionStatement(ConjunctiveQuery query, Schema schema) {
		List<String> projections = Lists.newArrayList();
		for(Variable variable:query.getFreeVariables()) {
			//Find the relation this variable is coming from
			for(int atomIndex = 0; atomIndex < query.getNumberOfAtoms(); ++atomIndex) {
				Atom bodyAtom = query.getAtom(atomIndex);
				int indexOf;
				if((indexOf = bodyAtom.getTermPosition(variable)) >= 0) {
					projections.add(this.createProjections(indexOf, schema.getRelation(bodyAtom.getPredicate().getName()), this.aliases.get(bodyAtom)));
					break;
				}	
			}	
		}
		return projections;
	}
	
	/**
	 * Creates the projection statements.
	 * @param view the input query
	 * @return
	 */
	protected List<String> createProjectionStatement(View view, Schema schema) {
		List<String> projections = Lists.newArrayList();
		for(Variable variable:view.getRelationToViewDependency().getHead().getFreeVariables()) {
			//Find the relation this variable is coming from
			for(Atom bodyAtom:view.getRelationToViewDependency().getHead().getAtoms()) {
				int indexOf;
				if((indexOf = bodyAtom.getTermPosition(variable)) >= 0) {
					projections.add(this.createProjections(indexOf, schema.getRelation(bodyAtom.getPredicate().getName()), this.aliases.get(bodyAtom)));
					break;
				}	
			}	
		}
		return projections;
	}
	
	/**
	 * Creates the attribute equalities.
	 *
	 * @param conjuncts the query body
	 * @return 		
	 */
	protected List<String> createAttributeEqualities(Atom[] conjuncts, Schema schema) {
		List<String> predicates = new ArrayList<String>();
		Collection<Term> terms = Utility.getTerms(conjuncts);
		terms = Utility.removeDuplicates(terms);
		for (Term term:terms) {
			Integer leftPosition = null;
			Relation leftRelation = null;
			String leftAlias = null;
			for (Atom fact:conjuncts) {
				List<Integer> positions = Utility.getTermPositions(fact, term); //all the positions for the same term should be equated
				for (Integer pos:positions) {
					if(leftPosition == null) {
						leftPosition = pos;
						leftRelation = schema.getRelation(fact.getPredicate().getName());
						leftAlias = this.aliases.get(fact);
					}
					else {					
						Integer rightPosition = pos;
						Relation rightRelation = schema.getRelation(fact.getPredicate().getName());
						String rightAlias = this.aliases.get(fact);

						StringBuilder result = new StringBuilder();
						result.append(leftAlias==null ? leftRelation.getName():leftAlias).append(".").append(leftRelation.getAttribute(leftPosition).getName()).append('=');
						result.append(rightAlias==null ? rightRelation.getName():rightAlias).append(".").append(rightRelation.getAttribute(rightPosition).getName());
						predicates.add(result.toString());
					}
				}
			}
		}
		return predicates;
	}
	
	
	/**
	 * Creates the equalities with constants.
	 *
	 * @param conjuncts the source
	 * @return 		constant equality predicates
	 */
	protected List<String> createConstantEqualities(Atom[] conjuncts, Schema schema) {
		List<String> predicates = new ArrayList<>();
		for (Atom fact:conjuncts) {
			String alias = this.aliases.get(fact);
			for(int termIndex = 0; termIndex < fact.getNumberOfTerms(); ++termIndex) {
				Term term = fact.getTerm(termIndex);
				if (term instanceof TypedConstant) {
					StringBuilder eq = new StringBuilder();
					Relation relation = schema.getRelation(fact.getPredicate().getName());
					eq.append(alias==null ? fact.getPredicate().getName():alias).append(".").append(relation.getAttribute(termIndex).getName()).append('=');
					if(((TypedConstant) term).getValue() instanceof Number) 
						eq.append(term.toString());
					else 
						eq.append("'").append(term).append("'");
					predicates.add(eq.toString());
				}
			}
		}
		return predicates;
	}
	
	/**
	 * Creates the projection statement for argument.
	 *
	 * @param position the position
	 * @param relation the relation
	 * @param alias the alias
	 * @return the string
	 */
	protected String createProjections(int position, Relation relation, String alias) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkArgument(position >= 0 && position < relation.getArity());
		StringBuilder result = new StringBuilder();
		result.append(alias==null ? relation.getName():alias).
		append(".").append(relation.getAttribute(position).getName());
		if(this.aliasSelections) {
			result.append(" AS ").append(projectAliasPrefix + projectAliasCounter++);
		}
		return result.toString();
	}
	
	/**
	 * Creates the table aliasing expression.
	 *
	 * @param alias the alias
	 * @param relation the relation
	 * @return the string
	 */
	private String createTableAliasingExpression(String alias, Predicate relation, String schema) {
		Preconditions.checkNotNull(relation);
		StringBuilder result = new StringBuilder();
		if(schema != null) {
			result.append(schema + ".");
		}
		result.append(relation.getName()).append(" AS ");
		result.append(alias==null ? relation.getName():alias);
		return result.toString();
	}
	
	
	public static String translateQueryToSQL(ConjunctiveQuery query, Schema schema, String databaseSchemaName) {
		ConjunctiveQueryToSQL translator = new ConjunctiveQueryToSQL();
		return translator.translate(query, schema, databaseSchemaName);
	}
	
	public static String translateViewToSQL(View view, Schema schema, String databaseSchemaName) {
		ConjunctiveQueryToSQL translator = new ConjunctiveQueryToSQL();
		return translator.translate(view, schema, databaseSchemaName);
	}
	
	private String createAlias(String name) {
		return this.tableAliasPrefix + this.tableAliasCounter;
	}

}