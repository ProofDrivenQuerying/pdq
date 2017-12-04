package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * This class represents a SQL query. Can be created from a CQ, or a simple
 * relation (when you want to get all data from that relation)
 * 
 * @author Gabor
 *
 */
public class BasicSelect extends Command {
	/**
	 * Alias provider for this Query instance. The content aliases are globally
	 * unique.
	 */
	private Set<TableAlias> aliases = new HashSet<>();

	/**
	 * The query will produce a JDBC result set, this array will show how to process
	 * that result set, by telling which result column maps to what Variable.
	 */
	protected Term[] resultTerms;
	/**
	 * The original CQ. It is mandatory, even if we created this BasicSelect from a
	 * Relation object.
	 */
	protected ConjunctiveQuery formula;
	/**
	 * Schema is needed to know the type of the attributes.
	 */
	protected Schema schema;

	/**
	 * The table aliases have to be used multiple times in a large query. This local
	 * map holds the mapping from alias to table name.
	 */
	protected Map<String, String> aliasKeyToTable = new HashMap<>();
	/**
	 * Where conditions with table name alias keys. Every element of this list is an
	 * independent condition that needs to be printed after the WHERE keyword and
	 * connected with ANDs.
	 */
	protected List<String> whereConditions = new ArrayList<>();
	/**
	 * The part of the SQL query that comes after the FROM part but before the WHERE
	 * part. Example: "Select * From X,Y,Z" The list will contain 3 elements:X,Y and
	 * Z.
	 */
	protected List<String> fromTableName = new ArrayList<>();

	/**
	 * Same as above but this contains the actual table name and not an alias. This
	 * is needed to create large nested queries.
	 */
	protected List<String> fromTableNameNoAliases = new ArrayList<>();
	/**
	 * Select XYZ
	 */
	protected List<String> select = new ArrayList<>();

	/**
	 * Default constructor is only protected since it shouldn't be used externally,
	 * it is only needed for extending this class.
	 */
	protected BasicSelect() {
	}

	/**
	 * creates a select reading all data from given relation
	 * 
	 */
	public BasicSelect(Relation r) {
		super();
		List<Variable> variables = new ArrayList<>();
		for (Attribute a : r.getAttributes()) {
			variables.add(Variable.create(r.getName() + "_" + a.getName()));
		}
		resultTerms = variables.toArray(new Term[variables.size()]);
		Atom a = Atom.create(Predicate.create(r.getName(), r.getArity()), resultTerms);
		formula = ConjunctiveQuery.create(variables.toArray(new Variable[variables.size()]), a);
		statements.add("select * from " + DATABASENAME + "." + r.getName());
	}

	/**
	 * Creates a select based on a CQ.
	 * 
	 * @param schema
	 *            - needed for the attribute types.
	 * @param cq
	 * @throws DatabaseException 
	 */
	public BasicSelect(Schema schema, ConjunctiveQuery cq) throws DatabaseException {
		this.schema = schema;
		formula = cq;
		resultTerms = formula.getFreeVariables();
		initSelect();
		initFrom();
		initConstantEqualityConditions();
		initAttributeEqualityConditions();
		String sqlQueryString = "SELECT " + Joiner.on(",").join(select) + " FROM " + Joiner.on(",").join(fromTableName);
		if (!whereConditions.isEmpty())
			sqlQueryString += " WHERE " + Joiner.on(" AND ").join(whereConditions);
		statements.add(sqlQueryString);
	}

	/**
	 * Creates the part of the query that comes after the SELECT keyword, but before
	 * the FROM keyword.
	 * @throws DatabaseException 
	 */
	private void initSelect() throws DatabaseException {
		List<Variable> freeVariables = Arrays.asList(formula.getFreeVariables());
		// SELECT free variables
		for (Atom a : formula.getAtoms()) {
			// loop over all atoms of the query (flattened hierarchy)
			if (Collections.disjoint(freeVariables, Arrays.asList(a.getTerms()))) {
				// this atom has no free variables.
				continue;
			}
			for (int i = 0; i < a.getTerms().length; i++) {
				Term term = a.getTerm(i);
				if (freeVariables.contains(term)) {
					String relName = a.getPredicate().getName();
					Attribute attribute = schema.getRelation(relName).getAttribute(i);
					select.add(getAlias(relName,a.getTerms()).aliasName + "." + attribute.getName());
				}
			}
		}
	}
	
	/**
	 * Creates the part of the query thet comes after the "FROM" part but before the
	 * WHERE part.
	 */
	private void initFrom() {
		// FROM table names
		for (Atom a : formula.getAtoms()) {
			// loop over all atoms of the query (flattened hierarchy)
			TableAlias ta = getAlias(a.getPredicate().getName(), a.getTerms());
			fromTableName.add(DATABASENAME + "." + a.getPredicate().getName() + " AS " + ta.aliasName);
			fromTableNameNoAliases.add(ta.toKey());
		}
	}

	/**
	 * Stores the mapping for alias table names, and also for the different SQL
	 * dialect. This is needed to be processed last to support further modification
	 * of this select such as NestedSelect works in the DifferenceQuery class.
	 * 
	 * @param key
	 * @param value
	 */
	protected void storeReplacementKeyValuePairs(String key, String value) {
		aliasKeyToTable.put(key, value);
		replaceTagsMySql.put(key, value);
		replaceTagsDerby.put(key, value);
		replaceTagsPostgres.put(key, value);

	}

	/**
	 * Retrieves the existing alias of creates a new one for the given table name.
	 * 
	 * @param tableName
	 *            actual name of the table in the database.
	 * @return the alias name of the table.
	 */
	private TableAlias getAlias(String tableName, Term[] terms) {
		TableAlias ta = new TableAlias();
		ta.tableName = tableName;
		ta.setVariables(terms);
		for (TableAlias taExisting:aliases) {
			if (ta.equals(taExisting)) {
				return taExisting;
			}
		}
		ta.aliasName = "A" + GlobalCounterProvider.getNext("TableNameAlias");
		aliases.add(ta);
		return ta;
	}

	/**
	 * When a CQ contains a constant we have to create a corresponding where
	 * condition. This function searches for these constants and generates the where
	 * conditions.
	 * @throws DatabaseException 
	 */
	private void initConstantEqualityConditions() throws DatabaseException {
		for (Atom a : formula.getAtoms()) {
			for (int i = 0; i < a.getTerms().length; i++) {
				if (a.getTerm(i) instanceof Constant) {
					// constant equality condition.
					String tableName = a.getPredicate().getName();
					TableAlias tableAlias = getAlias(tableName, a.getTerms());
					Attribute attribute = schema.getRelation(tableName).getAttribute(i);
					whereConditions.add(tableAlias.toKey() + "." + attribute.getName() + " = " + convertTermToSQLString(attribute, a.getTerm(i)));
					storeReplacementKeyValuePairs(tableAlias.toKey(), tableAlias.aliasName);
				}
			}
		}
	}

	/**
	 * When a CQ has more then one atom, some variables can appear multiple times.
	 * These cases are the attribute equality conditions, and we have to find them
	 * and create corresponding SQL WHERE conditions from them.
	 * @throws DatabaseException 
	 */
	private void initAttributeEqualityConditions() throws DatabaseException {
		// go over each atom of the CQ.
		for (int ai = 0; ai < formula.getAtoms().length - 1; ai++) {
			// the current atom is "a", its index is "ai"
			Atom a = formula.getAtoms()[ai];

			// go over all terms of this current atom.
			for (int i = 0; i < a.getTerms().length; i++) {
				// constant equality conditions are not checked in this function, so we skip
				// constants.
				if (a.getTerm(i) instanceof Constant) {
					continue;
				}
				// loop over the atoms of the query, make sure we only check for new possible
				// matches.
				for (int bi = ai + 1; bi < formula.getAtoms().length; bi++) {
					// the current atom is "b" its index is "bi"
					Atom b = formula.getAtoms()[bi];
					// go over the terms of this atom as well.
					for (int j = 0; j < b.getTerms().length; j++) {
						// in case the current variable is the same in both a and b atoms, we have a
						// match.
						if (a.getTerm(i).equals(b.getTerm(j))) {

							// figure out the table and column name for both left and right sides,
							// respecting table aliases.
							String tableNameLeft = a.getPredicate().getName();
							String tableNameRight = b.getPredicate().getName();
							TableAlias aliasLeft = getAlias(tableNameLeft,a.getTerms());
							TableAlias aliasRight = getAlias(tableNameRight,b.getTerms());

							Attribute attributeLeft = schema.getRelation(tableNameLeft).getAttribute(i);
							Attribute attributeRight = schema.getRelation(tableNameRight).getAttribute(j);
							// Attribute equality condition
							whereConditions.add(aliasLeft.toKey() + "." + attributeLeft.getName() + " = " + aliasRight.toKey() + "." + attributeRight.getName());
							storeReplacementKeyValuePairs(aliasLeft.toKey(), aliasLeft.aliasName);
							storeReplacementKeyValuePairs(aliasRight.toKey(),aliasRight.aliasName);
						}
					}
				}
			}
		}
	}

	/**
	 * List of Variables that needs to be mapped to the JDBC resultSet.
	 * 
	 * @return
	 */
	public Term[] getResultTerms() {
		return resultTerms;
	}

	/**
	 * @return the ConjunctiveQuery.
	 */
	public Formula getFormula() {
		return formula;
	}

	public class TableAlias {
		protected String aliasName="";
		protected String tableName="";
		protected Collection<Variable> variables= new ArrayList<>();
		public void setVariables(Term[] terms) {
			for (Term t:terms) {
				if (t.isVariable())
					variables.add((Variable)t);
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TableAlias)
				return tableName.equals(((TableAlias)obj).tableName) && variables.size() == ((TableAlias)obj).variables.size() &&
						variables.containsAll(((TableAlias)obj).variables);
			return false;
		}
		@Override
		public String toString() {
			return "Alias("+aliasName+") for table("+tableName+") over variables("+variables+")";
		}
		public String toKey() {
			String key = "{" + tableName+"_";
			for (Variable v:variables) key+=v.getSymbol()+"_";
			return key + "}";
		}
	}
}
