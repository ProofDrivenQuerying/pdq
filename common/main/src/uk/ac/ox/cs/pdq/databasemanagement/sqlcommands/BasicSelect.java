package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

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
 * @author Gabor
 *
 */
public class BasicSelect extends Command {
	/**
	 * Alias provider for this Query instance. The content aliases are globally
	 * unique.
	 */
	private Map<String, String> aliases = new HashMap<>();

	protected Term[] resultTerms;
	protected ConjunctiveQuery formula;
	protected Schema schema;

	protected Map<String,String> aliasKeyToTable = new HashMap<>();
	/**
	 * Where conditions with table name alias keys
	 */
	protected List<String> whereConditions = new ArrayList<>();
	/**
	 * Select * From XYZ tables with aliases.
	 */
	protected List<String> fromTableName = new ArrayList<>();
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

	public BasicSelect(Schema schema, ConjunctiveQuery cq) {
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

	public Term[] getResultTerms() {
		return resultTerms;
	}

	public void setResultTerms(Term[] resultTerms) {
		this.resultTerms = resultTerms;
	}

	public Formula getFormula() {
		return formula;
	}

	private void initSelect() {
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
					select.add(getAlias(relName) + "." + attribute.getName());
				}
			}
		}
	}

	private void initFrom() {
		// FROM table names
		for (Atom a : formula.getAtoms()) {
			// loop over all atoms of the query (flattened hierarchy)
			String name = getAlias(a.getPredicate().getName());
			fromTableName.add(DATABASENAME + "." + a.getPredicate().getName() + " AS " + name);
			fromTableNameNoAliases.add(DATABASENAME + "." + a.getPredicate().getName());
		}
	}

	protected void storeKeyValuePairs(String key, String value) {
		aliasKeyToTable.put(key, value);
		replaceTagsMySql.put(key, value);
		replaceTagsDerby.put(key, value);
		replaceTagsPostgres.put(key, value);
		
	}
	
	private synchronized String getAlias(String name) {
		if (aliases.containsKey(name)) {
			String aliasName = aliases.get(name);
			return aliasName;
		} else {
			String newName = "A" + GlobalCounterProvider.getNext("TableNameAlias");
			aliases.put(name, newName);
			return newName;
		}
	}

	private void initConstantEqualityConditions() {
		for (Atom a : formula.getAtoms()) {
			for (int i = 0; i < a.getTerms().length; i++) {
				if (a.getTerm(i) instanceof Constant) {
					// constant equality condition.
					String aliasKey = "{TABLE_ALIAS_"+a.getPredicate().getName()+"}";
					String tableName = a.getPredicate().getName();
					Attribute attribute = schema.getRelation(tableName).getAttribute(i);
					whereConditions.add(aliasKey + "." + attribute.getName() + " = " + convertTermToSQLString(attribute, a.getTerm(i)));
					storeKeyValuePairs(aliasKey, getAlias(tableName));
				}
			}
		}
	}

	private void initAttributeEqualityConditions() {
		for (int ai = 0; ai < formula.getAtoms().length - 1; ai++) {
			Atom a = formula.getAtoms()[ai];
			for (int i = 0; i < a.getTerms().length; i++) {
				if (a.getTerm(i) instanceof Constant) {
					// constant equality condition takes care of this case
					continue;
				}
				for (int bi = ai + 1; bi < formula.getAtoms().length; bi++) {
					Atom b = formula.getAtoms()[bi];
					for (int j = 0; j < b.getTerms().length; j++) {
						if (a.getTerm(i).equals(b.getTerm(j))) {
							String tableNameLeft = a.getPredicate().getName();
							Attribute attributeLeft = schema.getRelation(tableNameLeft).getAttribute(i);
							String aliasLeftKey = "{TABLE_ALIAS_"+a.getPredicate().getName()+"}";
							String aliasRightKey = "{TABLE_ALIAS_"+b.getPredicate().getName()+"}";

							String tableNameRight = b.getPredicate().getName();
							Attribute attributeRight = schema.getRelation(tableNameRight).getAttribute(j);
							// Attribute equality condition
							whereConditions.add(aliasLeftKey + "." + attributeLeft.getName() + " = " + aliasRightKey + "." + attributeRight.getName());
							storeKeyValuePairs(aliasLeftKey, getAlias(tableNameLeft));
							storeKeyValuePairs(aliasRightKey, getAlias(tableNameRight));
						}
					}
				}
			}
		}
	}
}
