package uk.ac.ox.cs.pdq.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.data.memory.MemoryQuery;
import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.data.sql.SQLQuery;
import uk.ac.ox.cs.pdq.data.sql.SqlDatabaseInstance;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * This is a query that was created from formulas such as ConjunctiveQuery or a
 * dependency activeness check, but converted to the language of a physical
 * database such as SQL.
 * 
 * @author Gabor
 *
 */
public class PhysicalQuery {
	/**
	 * The main ConjunctiveQuery or the left Query in case we have query
	 * differences.
	 */
	protected ConjunctiveQuery conjunctiveQuery;

	/**
	 * A ConjunctiveQuery can be realised as a physical query only over an actual
	 * schema, since we need Attribute names and types.
	 */
	protected Schema schema;
	/**
	 * This will be used in case this query was created by the difference of two
	 * conjunctive query.
	 */
	protected PhysicalQuery rightQuery;
	/**
	 * Attribute equality condition means that the left and right side of a
	 * Conjunction has one or more variable that needs to have the same value. These
	 * conditions are stored under the corresponding Conjunction object in this map.
	 */
	private Map<Conjunction, ConjunctiveCondition> attributeEqualityConditions;

	/**
	 * Property mappings between schema and query for each Atom of this conjunctiveQuery.
	 */
	protected Collection<QueryAtom> queryAtoms;

	/**
	 * The formula must be conjunctiveQuery or Dependency
	 * 
	 * @param formula
	 */
	protected PhysicalQuery(ConjunctiveQuery formula, Schema schema) {
		this.conjunctiveQuery = formula;
		this.schema = schema;
		attributeEqualityConditions = new HashMap<>();
		queryAtoms = new ArrayList<>();
		for (Atom a : formula.getAtoms()) {
			queryAtoms.addAll(initQueryAtoms(a,formula.getFreeVariables()));
		}
		if (formula.getBody() instanceof Conjunction) {
			initAttributeEqualityConditions((Conjunction) formula.getBody(), formula.getBoundVariables());
		}

	}

	protected PhysicalQuery(ConjunctiveQuery leftQuery, PhysicalQuery rightQuery, Schema schema) {
		this(leftQuery, schema);
		this.rightQuery = rightQuery;

	}

	/**
	 * Populates a QueryAtom object for the current atom. 
	 */
	private Collection<QueryAtom> initQueryAtoms(Atom atom,Variable[] freeVariables) {
		Collection<QueryAtom> results = new ArrayList<>();
		
		Relation r = this.schema.getRelation(atom.getPredicate().getName());
		QueryAtom queryAtom = new QueryAtom(atom,r);
		
		// get constant equalities
		for (int i = 0; i < atom.getTerms().length; i++) {
			if (atom.getTerms()[i] instanceof TypedConstant) {
				queryAtom.addConstantEqualityCondition(ConstantEqualityCondition.create(i, (TypedConstant) atom.getTerms()[i]));
			}
		}
		
		// get free variable mappings
		for (Variable v : freeVariables) {
			for (int index = 0; index < atom.getTerms().length; index++) {
				if (atom.getTerms()[index].equals(v)) {
					queryAtom.addFreeVariable(v,r.getAttribute(index));
				}
			}
		}
		results.add(queryAtom);
		return results;
	}

	/**
	 * populates the attributeEqualityConditions hashMap. For each Conjunction it
	 * will check if we have a normal or dependent join, and for dependent joins it
	 * will describe what attributes have to be the same.
	 * 
	 * @param con
	 * @param variables
	 */
	private void initAttributeEqualityConditions(Conjunction con, Variable[] variables) {
		Collection<SimpleCondition> predicates = new ArrayList<>();
		if (con.getChild(0) instanceof Atom && con.getChild(1) instanceof Atom) {
			for (Variable v : variables) {
				int leftIndex = Arrays.asList(con.getChild(0).getTerms()).indexOf(v);
				int rightIndex = Arrays.asList(con.getChild(1).getTerms()).indexOf(v);
				if (leftIndex >= 0 && rightIndex >= 0) {
					predicates.add(AttributeEqualityCondition.create(leftIndex, rightIndex));
				}
			}
			if (predicates.size() > 0) {
				ConjunctiveCondition cc = ConjunctiveCondition.create(predicates.toArray(new SimpleCondition[predicates.size()]));
				attributeEqualityConditions.put(con, cc);
			}
		} else {
			for (Variable v : variables) {
				int leftIndex = Arrays.asList(con.getChild(0).getTerms()).indexOf(v);
				int rightIndex = Arrays.asList(con.getChild(1).getTerms()).indexOf(v);
				if (leftIndex >= 0 && rightIndex >= 0) {
					predicates.add(AttributeEqualityCondition.create(leftIndex, rightIndex));
				}
			}
			if (predicates.size() > 0) {
				ConjunctiveCondition cc = ConjunctiveCondition.create(predicates.toArray(new SimpleCondition[predicates.size()]));
				if (con.getChild(0) instanceof Conjunction) {
					initAttributeEqualityConditions((Conjunction) con.getChild(0), variables);
				}
				if (con.getChild(1) instanceof Conjunction) {
					initAttributeEqualityConditions((Conjunction) con.getChild(1), variables);
				}
				attributeEqualityConditions.put(con, cc);
			}
		}
	}

	/**
	 * For each conjunctions that represents a dependent join it describes what
	 * attributes have to be equal.
	 * 
	 * @return
	 */
	public Map<Conjunction, ConjunctiveCondition> getAttributeEqualityConditions() {
		return attributeEqualityConditions;
	}

	/**
	 * For each predicate name it lists all constants that needs to match this
	 * query.
	 * 
	 * @return
	 */
	public Collection<QueryAtom> getQueryAtoms() {
		return queryAtoms;
	}

	protected PhysicalQuery getRightQuery() {
		return rightQuery;
	}

	public String toString() {
		return "PhysicalQuery (" + conjunctiveQuery + ")";
	}

	protected ConjunctiveQuery getConjunctiveQuery() {
		return conjunctiveQuery;
	}

	public static PhysicalQuery create(DatabaseManager manager, ConjunctiveQuery query) throws DatabaseException {
		try {
			if (manager.getQueryClass() == SQLQuery.class) {
				SQLQuery q = new SQLQuery(query, (SqlDatabaseInstance) manager.databaseInstance);
				return q;
			}
			if (manager.getQueryClass() == MemoryQuery.class) {
				MemoryQuery q = new MemoryQuery(query,manager.getSchema());
				return q;
			}

			Class<? extends PhysicalQuery> qclass = manager.getQueryClass();
			Constructor<? extends PhysicalQuery> constructor;
			constructor = qclass.getConstructor(Formula.class);
			PhysicalQuery q = constructor.newInstance(query);
			return q;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new DatabaseException("Failed to create query from : " + query + " using manager: " + manager + ".", e);
		}
	}

	public static PhysicalQuery createQueryDifference(DatabaseManager manager, ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery) throws DatabaseException {
		try {
			if (manager.getQueryClass() == SQLQuery.class) {
				SQLQuery q = new SQLQuery(leftQuery, new SQLQuery(rightQuery, (SqlDatabaseInstance) manager.databaseInstance), (SqlDatabaseInstance) manager.databaseInstance);
				return q;
			}

			if (manager.getQueryClass() == MemoryQuery.class) {
				MemoryQuery q = new MemoryQuery(leftQuery, new MemoryQuery(rightQuery,manager.getSchema()),manager.getSchema());
				return q;
			}
			Class<? extends PhysicalQuery> qclass = manager.getQueryClass();
			Constructor<? extends PhysicalQuery> constructor;
			constructor = qclass.getConstructor(Formula.class, Formula.class);
			PhysicalQuery q = constructor.newInstance(leftQuery, rightQuery);
			return q;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new DatabaseException("Failed to create query from l: " + leftQuery + ", r:" + rightQuery + " using manager: " + manager + ".", e);
		}

	}

}
