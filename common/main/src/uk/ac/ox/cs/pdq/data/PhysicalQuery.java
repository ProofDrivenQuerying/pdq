package uk.ac.ox.cs.pdq.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.data.memory.MemoryNestedQuery;
import uk.ac.ox.cs.pdq.data.memory.MemoryQuery;
import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.data.sql.SQLNestedSelect;
import uk.ac.ox.cs.pdq.data.sql.SQLSelect;
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
public abstract class PhysicalQuery {
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
	 * Attribute equality condition means that the left and right side of a
	 * Conjunction has one or more variable that needs to have the same value. These
	 * conditions are stored under the corresponding Conjunction object in this map.
	 */
	private Map<Conjunction, MappedConjunctiveQuery> mappedConjunctiveQuery;

	/**
	 * Property mappings between schema and query for each Atom of this conjunctiveQuery.
	 */
	protected Collection<ConjunctiveQueryDescriptor> queryAtoms;

	/**
	 * The formula must be conjunctiveQuery or Dependency
	 * 
	 * @param formula
	 */
	protected PhysicalQuery(ConjunctiveQuery formula, Schema schema) {
		this.conjunctiveQuery = formula;
		this.schema = schema;
		mappedConjunctiveQuery= new HashMap<>();
		queryAtoms = new ArrayList<>();
		for (Atom a : formula.getAtoms()) {
			queryAtoms.addAll(initQueryAtoms(a,formula.getFreeVariables()));
		}
		if (formula.getBody() instanceof Conjunction) {
			initAttributeEqualityConditions((Conjunction) formula.getBody(), formula.getBoundVariables());
		}

	}

	/**
	 * Populates a ConjunctiveQueryDescriptor object for the current atom. 
	 */
	private Collection<ConjunctiveQueryDescriptor> initQueryAtoms(Atom atom,Variable[] freeVariables) {
		Collection<ConjunctiveQueryDescriptor> results = new ArrayList<>();
		
		Relation r = this.schema.getRelation(atom.getPredicate().getName());
		ConjunctiveQueryDescriptor queryAtom = new ConjunctiveQueryDescriptor(atom,r);
		
		// get constant equalities
		for (int i = 0; i < atom.getTerms().length; i++) {
			if (atom.getTerms()[i] instanceof TypedConstant) {
				queryAtom.addConstantEqualityCondition(queryAtom.getAttributeAtIndex(i), (TypedConstant) atom.getTerms()[i]);
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
		if (con.getChild(0) instanceof Atom && con.getChild(1) instanceof Atom) {
			MappedConjunctiveQuery map = new MappedConjunctiveQuery(findQueryAtomDescriptor((Atom)con.getChild(0)), findQueryAtomDescriptor((Atom)con.getChild(1)), con);
			mappedConjunctiveQuery.put(con, map );
			for (Variable v : variables) {
				int leftIndex = Arrays.asList(con.getChild(0).getTerms()).indexOf(v);
				int rightIndex = Arrays.asList(con.getChild(1).getTerms()).indexOf(v);
				if (leftIndex >= 0 && rightIndex >= 0) {
					map.addMatchingColumnIndexes(leftIndex, rightIndex);
				}
			}
		} else {
			MappedConjunctiveQuery map = new MappedConjunctiveQuery(findQueryAtomDescriptor((Atom)con.getChild(0)), mappedConjunctiveQuery.get(con.getChild(1)), con);
			mappedConjunctiveQuery.put(con, map );
			for (Variable v : variables) {
				int leftIndex = Arrays.asList(con.getChild(0).getTerms()).indexOf(v);
				int rightIndex = Arrays.asList(con.getChild(1).getTerms()).indexOf(v);
				if (leftIndex >= 0 && rightIndex >= 0) {
					map.addMatchingColumnIndexes(leftIndex, rightIndex);
				}
			}
			if (con.getChild(1) instanceof Conjunction) {
				initAttributeEqualityConditions((Conjunction) con.getChild(1), variables);
			}
		}
	}

	/**
	 * For each conjunctions that represents a dependent join it describes what
	 * attributes have to be equal.
	 * 
	 * @return
	 */
	public Map<Conjunction, MappedConjunctiveQuery> getMappedConjunctiveQuery() {
		return mappedConjunctiveQuery;
	}

	/**
	 * For each predicate name it lists all constants that needs to match this
	 * query.
	 * 
	 * @return
	 */
	public Collection<ConjunctiveQueryDescriptor> getQueryAtoms() {
		return queryAtoms;
	}

	abstract protected PhysicalQuery getRightQuery();
	
	public String toString() {
		return "PhysicalQuery (" + conjunctiveQuery + ")";
	}

	protected ConjunctiveQuery getConjunctiveQuery() {
		return conjunctiveQuery;
	}

	public static PhysicalQuery create(DatabaseManager manager, ConjunctiveQuery query) throws DatabaseException {
		try {
			if (manager.getQueryClass() == SQLSelect.class) {
				SQLSelect q = new SQLSelect(query, (SqlDatabaseInstance) manager.databaseInstance);
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
			if (manager.getQueryClass() == SQLSelect.class) {
				SQLSelect q = new SQLNestedSelect(leftQuery, new SQLSelect(rightQuery, (SqlDatabaseInstance) manager.databaseInstance), (SqlDatabaseInstance) manager.databaseInstance);
				return q;
			}

			if (manager.getQueryClass() == MemoryQuery.class) {
				MemoryNestedQuery q = new MemoryNestedQuery(leftQuery, new MemoryQuery(rightQuery,manager.getSchema()),manager.getSchema());
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
	protected ConjunctiveQueryDescriptor findQueryAtomDescriptor(Atom a) {
		for (ConjunctiveQueryDescriptor atomDescriptor:queryAtoms) {
			if (a.equals(atomDescriptor.getConjunctiveQueryAtom())) {
				return atomDescriptor;
			}
		}
		return null;
	}
}
