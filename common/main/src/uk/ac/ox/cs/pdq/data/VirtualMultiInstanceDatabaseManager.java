package uk.ac.ox.cs.pdq.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.data.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.data.sql.SQLQuery;
import uk.ac.ox.cs.pdq.data.sql.SqlDatabaseInstance;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Replaces the DatabaseManager to create multiple virtual database instances
 * over one physical instance. The main idea is: <br>
 * - every fact gets extended with a fact ID and this object will maintain a
 * maping table where each factID paired with an InstanceID, and we allow
 * querying facts of one specific instance or in the entire table.
 * 
 * 
 * @author Gabor
 *
 */
public class VirtualMultiInstanceDatabaseManager extends DatabaseManager {
	private Schema extendedSchema;
	private Collection<String> databaseInstanceIDs;
	private static final Attribute FACT_ID_ATTRIBUTE = Attribute.create(String.class, "FactId");
	protected static final Relation factIdInstanceIdMappingTable = Relation.create("InstanceIdMapping",
			new Attribute[] { FACT_ID_ATTRIBUTE, Attribute.create(String.class, "DatabaseInstanceID") }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });

	private MultiInstanceFactCache multiCache;

	/**
	 * Creates database manager and connection if needed based on the parameters.
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	public VirtualMultiInstanceDatabaseManager(DatabaseParameters parameters) throws DatabaseException {
		super(parameters);
		databaseInstanceIDs = new LinkedList<>();
		databaseInstanceIDs.add(getDatabaseInstanceID());
		if (!isMemoryDb)
			multiCache = new MultiInstanceFactCache();
	}

	/**
	 * Creates a canonical database for the schema.
	 * 
	 * @param schema
	 * @throws DatabaseException
	 */
	@Override
	public void initialiseDatabaseForSchema(Schema schema) throws DatabaseException {
		extendedSchema = extendSchemaWithFactIDs(schema);
		super.initialiseDatabaseForSchema(extendedSchema);
	}

	public Collection<Atom> addFacts(Collection<Atom> facts) throws DatabaseException {
		Collection<Atom> factsToAdd = null;
		if (!isMemoryDb) {
			// only add what's new.
			factsToAdd = multiCache.addFacts(facts, databaseInstanceID);
		} else {
			factsToAdd = facts;
		}
		List<Atom> extendedFacts = new ArrayList<>();
		extendedFacts.addAll(extendFactsWithFactID(factsToAdd));
		extendedFacts.addAll(getFactsMapping(factsToAdd));
		super.addFacts(extendedFacts);
		return factsToAdd;
	}

	public void deleteFacts(Collection<Atom> facts) throws DatabaseException {
		if (!isMemoryDb) {
			multiCache.deleteFacts(facts, databaseInstanceID);
		}

		// only deletes the mapping of this fact to this instance, does not delete the
		// actual fact.
		super.deleteFacts(getFactsMapping(facts));
	}

	/**
	 * In case the implementation has in-memory cache this can be used to get the
	 * cached data.
	 * 
	 * @return
	 */
	public Collection<Atom> getCachedFacts() throws DatabaseException {
		if (!isMemoryDb) {
			return multiCache.getFacts(databaseInstanceID);
		} else {
			throw new DatabaseException("Caching is disabled.");
		}
	}

	/**
	 * Actual reading from the underlying data structure.
	 * 
	 * @return
	 */
	public Collection<Atom> getFactsFromPhysicalDatabase() throws DatabaseException {
		Collection<Atom> results = new ArrayList<>();
		for (Relation r : this.extendedSchema.getRelations()) {
			Collection<PhysicalQuery> queries = new ArrayList<>();
			// queries.add(PhysicalQuery.create(this,
			// PhysicalDatabaseInstance.createQuery(r,databaseInstanceID)));
			ConjunctiveQuery q = PhysicalDatabaseInstance.createQuery(r, databaseInstanceID);
			Map<Variable, Constant> finalProjectionMapping = PhysicalDatabaseInstance.createProjectionMapping(r, q);
			queries.add(SQLQuery.createSQLQuery(q, finalProjectionMapping, (SqlDatabaseInstance) this.databaseInstance));
			results.addAll(removeFactID(PhysicalDatabaseInstance.getAtomsFromMatches(super.answerQueries(queries), r)));
		}
		return results;
	}

	public List<Match> answerQueries(Collection<PhysicalQuery> queries) throws DatabaseException {
		Collection<PhysicalQuery> newQueries = new ArrayList<>();
		for (PhysicalQuery q : queries) {
			ConjunctiveQuery extendedCQ = extendQuery((ConjunctiveQuery) q.getFormula());
			newQueries.add(PhysicalQuery.create(this,
					PhysicalDatabaseInstance.createProjectionMapping(this.extendedSchema.getRelation(q.getFormula().getAtoms()[0].getPredicate().getName()), extendedCQ),
					extendedCQ));
		}
		return super.answerQueries(newQueries);
	}

	private ConjunctiveQuery extendQuery(ConjunctiveQuery formula) {
		Conjunction newConjunction = addFactIdToConjunction(formula.getBody());
		return ConjunctiveQuery.create(formula.getFreeVariables(), newConjunction);
	}

	private Conjunction addFactIdToConjunction(Formula body) {
		if (body instanceof Atom) {
			ArrayList<Term> terms = new ArrayList<>();
			terms.addAll(Arrays.asList(body.getTerms()));
			Variable factID = Variable.create("DBFactID");
			terms.add(factID);
			return Conjunction.create(Atom.create(((Atom) body).getPredicate(), terms.toArray(new Term[terms.size()])),
					Atom.create(VirtualMultiInstanceDatabaseManager.factIdInstanceIdMappingTable, new Term[] { factID, TypedConstant.create(databaseInstanceID) }));

		} else {
			Conjunction con = (Conjunction)body;
			List<Formula> newChildren = new ArrayList<>();
			for (Formula child:con.getChildren()) {
				newChildren.add(addFactIdToConjunction(child));
			}
			return Conjunction.create(newChildren.toArray(new Formula[newChildren.size()]));
		}
	}

	/**
	 * Executes a change in the database such as deleting facts or creating tables.
	 * 
	 * @param update
	 * @return
	 */
	public int executeUpdates(List<PhysicalDatabaseCommand> update) throws DatabaseException {
		// UNFINISHED
		return super.executeUpdates(update);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ox.cs.pdq.data.DatabaseManager#setDatabaseInstanceID(java.lang.String)
	 */
	@Override
	public void setDatabaseInstanceID(String instanceID) {
		super.setDatabaseInstanceID(instanceID);
		databaseInstanceIDs.add(instanceID);
	}

	private Schema extendSchemaWithFactIDs(Schema schema) {
		Relation newRelations[] = new Relation[schema.getRelations().length + 1];
		int index = 0;
		for (Relation r : schema.getRelations()) {
			List<Attribute> attributes = new ArrayList<>();
			attributes.addAll(Arrays.asList(r.getAttributes()));
			attributes.add(FACT_ID_ATTRIBUTE);
			newRelations[index] = Relation.create(r.getName(), attributes.toArray(new Attribute[attributes.size()]));
			index++;
		}
		newRelations[newRelations.length - 1] = factIdInstanceIdMappingTable;
		List<Dependency> deps = new ArrayList<>();
		deps.addAll(Arrays.asList(schema.getKeyDependencies()));
		deps.addAll(Arrays.asList(schema.getDependencies()));
		return new Schema(newRelations, deps.toArray(new Dependency[deps.size()]));
	}

	private Collection<Atom> extendFactsWithFactID(Collection<Atom> facts) {
		List<Atom> extendedFacts = new ArrayList<>();
		for (Atom fact : facts) {
			Atom extendedAtom = Atom.create(fact.getPredicate(), extendTerms(fact.getTerms(), "" + fact.hashCode()));
			extendedFacts.add(extendedAtom);
		}
		return extendedFacts;
	}

	private Collection<Atom> getFactsMapping(Collection<Atom> facts) {
		List<Atom> extendedFacts = new ArrayList<>();
		for (Atom fact : facts) {
			Atom atomMapping = Atom.create(factIdInstanceIdMappingTable, TypedConstant.create("" + fact.hashCode()), TypedConstant.create(databaseInstanceID));
			extendedFacts.add(atomMapping);
		}
		return extendedFacts;
	}

	private Term[] extendTerms(Term[] terms, String factID) {
		List<Term> newTerms = new ArrayList<>();
		newTerms.addAll(Arrays.asList(terms));
		newTerms.add(TypedConstant.create(factID));
		return newTerms.toArray(new Term[newTerms.size()]);
	}

	private Collection<Atom> removeFactID(Collection<Atom> facts) {
		List<Atom> newFacts = new ArrayList<>();
		for (Atom fact : facts) {
			if (fact.getPredicate().getName().equals(factIdInstanceIdMappingTable.getName()))
				continue;
			newFacts.add(Atom.create(fact.getPredicate(), removeFactIdFromTerms(fact.getTerms())));
		}
		return newFacts;
	}

	private Term[] removeFactIdFromTerms(Term[] terms) {
		List<Term> newTerms = new ArrayList<>();
		newTerms.addAll(Arrays.asList(terms));
		newTerms.remove(newTerms.size() - 1);
		return newTerms.toArray(new Term[newTerms.size()]);
	}

}
