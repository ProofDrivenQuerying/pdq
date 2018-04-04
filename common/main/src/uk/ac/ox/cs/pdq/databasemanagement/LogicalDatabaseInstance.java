package uk.ac.ox.cs.pdq.databasemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.CreateTable;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQueryWithInequality;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Each instance of this LogicalDatabase class will create a logical database
 * over an external database. This means the tables will be extended with an
 * instanceId, and each fact will be extended with a factId, and a mapping table
 * is added to record which fact exists in which logical database. <br>
 * Queries are updated to handles these changes, so from a user's point of view
 * there is no difference using this or the ExternalDatabaseManager, however
 * since it will not store the same facts multiple times it saves space.
 * 
 * Uses the built in fact cache to make sure it won't insert duplicated facts,
 * such duplicates will be ignored.
 * 
 * @author Gabor
 *
 */
public class LogicalDatabaseInstance implements DatabaseManager {
	protected Schema extendedSchema;
	protected Schema originalSchema;
	protected static final String FACT_ID_TABLE_NAME = "DBFactID";
	protected static final String FACT_ID_ATTRIBUTE_NAME = "FactId";
	protected static final Attribute FACT_ID_ATTRIBUTE = Attribute.create(Integer.class, FACT_ID_ATTRIBUTE_NAME);
	protected static final Relation factIdInstanceIdMappingTable = Relation.create("InstanceIdMapping",
			new Attribute[] { FACT_ID_ATTRIBUTE, Attribute.create(Integer.class, "DatabaseInstanceID") }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });

	protected MultiInstanceFactCache multiCache;
	private ExternalDatabaseManager edm;
	protected int databaseInstanceID;

	/**
	 * This constructor creates a logical instance over an existing remote database
	 * manager with the given instanceID. Creating multiple logical databases over
	 * the same external instance will allow to avoid double storing facts, a
	 * mapping table will map the facts to specific instances. The use case scenario
	 * is: - Create an external database, - Create one logical instance using this
	 * constructor, - initialise the the database with a schema using the
	 * initialiseDatabaseForSchema function - create further instances using the
	 * clone function.
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	public LogicalDatabaseInstance(MultiInstanceFactCache cache, ExternalDatabaseManager edm, int databaseInstanceID) throws DatabaseException {
		this.edm = edm;
		multiCache = cache;
		this.databaseInstanceID = databaseInstanceID;
	}

	/**
	 * Used by the Internal DatabaseManager. This constructor is not to be used
	 * externally, it allows the Internal database to use this class in a cache only
	 * mode without external database.
	 * 
	 * @param cache
	 * @param databaseInstanceID
	 * @throws DatabaseException
	 */
	protected LogicalDatabaseInstance(MultiInstanceFactCache cache, int databaseInstanceID) throws DatabaseException {
		super();
		multiCache = cache;
		this.databaseInstanceID = databaseInstanceID;
	}

	/**
	 * Creates a new logical database (a new instance) over the same external
	 * database.
	 * 
	 * @param newDatabaseInstanceID
	 * @return
	 * @throws DatabaseException
	 */
	public LogicalDatabaseInstance clone(int newDatabaseInstanceID) throws DatabaseException {
		LogicalDatabaseInstance vmidm;
		vmidm = new LogicalDatabaseInstance(multiCache, edm, newDatabaseInstanceID);
		vmidm.extendedSchema = this.extendedSchema;
		vmidm.originalSchema = this.originalSchema;
		return vmidm;
	}

	/**
	 * Creates a canonical database for the schema. This function should be called
	 * only once, when the first logical database instance is created. Further
	 * instances do not need to call it (it would drop all existing tables from the
	 * external database making all other logical instance lose all data)
	 * 
	 * @param schema
	 * @throws DatabaseException
	 */
	@Override
	public void initialiseDatabaseForSchema(Schema schema) throws DatabaseException {
		setSchema(schema);
		edm.initialiseDatabaseForSchema(extendedSchema);
	}

	/**
	 * Extends the facts with factId and factId -> InstanceId mappings. Makes sure
	 * both the facts table and the mapping table is kept unique, no duplicated
	 * records. This functionality only works if you started from clean database.
	 * When you connect to an existing database, currently there is no way to
	 * populate the cache to keep them in sync. Such feature can be implemented when
	 * needed.
	 * 
	 * @see uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager#addFacts(java.util.Collection)
	 */
	public void addFacts(Collection<Atom> facts) throws DatabaseException {
		// only add what's new. Each fact has a value and a mapping, this list will show
		// when the value in the current instance is new or not. If it is new we need to
		// add the mapping. But we don't know if the value exists in other instances or
		// not.
		List<Atom> extendedFacts = new ArrayList<>();
		Collection<Atom> newFactsInThisInstance = multiCache.addFacts(facts, databaseInstanceID);
		Collection<Atom> newFactsInAllInstances = multiCache.checkExistsInOtherInstances(newFactsInThisInstance, databaseInstanceID);
		// we need to add new facts only if they are new to all instances
		extendedFacts.addAll(extendFactsWithFactID(newFactsInAllInstances));
		// add mapping for new facts
		extendedFacts.addAll(getFactsMapping(newFactsInThisInstance, this.databaseInstanceID));

		// write database
		edm.addFacts(extendedFacts);
	}

	/**
	 * only deletes the mappings for the current instanceId. A check to see if there
	 * are other mappings to the same fact can be added, but for sake of operation
	 * speed it is not added currently.
	 * 
	 * @see uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager#deleteFacts(java.util.Collection)
	 */
	public void deleteFacts(Collection<Atom> facts) throws DatabaseException {
		edm.deleteFacts(extendFactsWithFactID(multiCache.deleteFactsAndListUnusedFacts(facts, databaseInstanceID)));
		// only deletes the mapping of this fact to this instance, does not delete the
		// actual fact.
		edm.deleteFacts(getFactsMapping(facts, this.databaseInstanceID));
	}

	/**
	 * Uses the in-memory cache to return all facts of the current
	 * databaseInsatance.
	 * 
	 * @return
	 */
	public Collection<Atom> getCachedFacts() throws DatabaseException {
		return multiCache.getFacts(databaseInstanceID);
	}

	/**
	 * Reads all data for the current database instance, and then removes the factID
	 * - instanceID mapping from the results.
	 * 
	 * @return
	 */
	public Collection<Atom> getFactsFromPhysicalDatabase() throws DatabaseException {
		Collection<Atom> results = new ArrayList<>();
		for (Relation r : this.extendedSchema.getRelations()) {
			if (r.equals(factIdInstanceIdMappingTable))
				continue;
			Collection<ConjunctiveQuery> queries = new ArrayList<>();
			ConjunctiveQuery q = createQuery(r, databaseInstanceID);
			queries.add(q);
			results.addAll(removeFactID(getAtomsFromMatches(edm.answerConjunctiveQueries(queries), r), originalSchema));
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ox.cs.pdq.data.OLD_DatabaseManager#answerQueries(java.util.Collection)
	 */
	public List<Match> answerConjunctiveQuery(ConjunctiveQuery query) throws DatabaseException {
		return answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { query }));
	}

	public List<Match> answerConjunctiveQueries(Collection<ConjunctiveQuery> queries) throws DatabaseException {
		Collection<ConjunctiveQuery> queriesWithInstanceIDs = new ArrayList<>();
		Map<ConjunctiveQuery, ConjunctiveQuery> oldAndNewQueries = new HashMap<>();

		// extend queries with factIDs,
		for (ConjunctiveQuery q : queries) {
			ConjunctiveQuery extendedCQ = extendQuery(q, this.databaseInstanceID);
			oldAndNewQueries.put(extendedCQ, q);
			queriesWithInstanceIDs.add(extendedCQ);
		}

		// Answer new queries
		List<Match> matches = edm.answerConjunctiveQueries(queriesWithInstanceIDs);

		// Remove unnecessary factIDs from the answer
		List<Match> result = new ArrayList<Match>();
		for (Match m : matches) {
			result.add(Match.create(oldAndNewQueries.get(m.getFormula()), removeFactID(m.getMapping())));
		}
		return result;
	}

	/**
	 * Similar to the answer queries, but it will execute two queries and returns
	 * the difference between the two sets of results.
	 * 
	 * @param leftQuery
	 * @param rightQuery
	 * @return
	 * @throws DatabaseException
	 */
	public List<Match> answerQueryDifferences(ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery) throws DatabaseException {
		ConjunctiveQuery extendedLQ = extendQuery(leftQuery, this.databaseInstanceID);
		ConjunctiveQuery extendedRQ = extendQuery(rightQuery, this.databaseInstanceID);
		Map<ConjunctiveQuery, ConjunctiveQuery> oldAndNewQueries = new HashMap<>();
		oldAndNewQueries.put(extendedLQ, leftQuery);
		oldAndNewQueries.put(extendedRQ, rightQuery);
		List<Match> result = new ArrayList<Match>();
		List<Match> matches = edm.answerQueryDifferences(extendedLQ, extendedRQ);
		for (Match m : matches) {
			result.add(Match.create(oldAndNewQueries.get(m.getFormula()), removeFactID(m.getMapping())));
		}
		return result;
	}

	private Map<Variable, Constant> removeFactID(Map<Variable, Constant> mapping) {
		Map<Variable, Constant> results = new HashMap<>();
		for (Variable v : mapping.keySet()) {
			if (!v.getSymbol().startsWith(FACT_ID_ATTRIBUTE_NAME + "_")) {
				results.put(v, mapping.get(v));
			}
		}
		return results;
	}

	private static int factIdNameCounter = 0;

	private static synchronized ConjunctiveQuery extendQuery(ConjunctiveQuery formula, int databaseInstanceID) {
		factIdNameCounter = 0;
		Conjunction newConjunction = addFactIdToConjunction(formula.getBody(), databaseInstanceID);
		if (formula instanceof ConjunctiveQueryWithInequality) {
			return ConjunctiveQueryWithInequality.create(formula.getFreeVariables(), newConjunction.getAtoms(), ((ConjunctiveQueryWithInequality) formula).getInequalities());
		}
		return ConjunctiveQuery.create(formula.getFreeVariables(), newConjunction.getAtoms());
	}

	private static Conjunction addFactIdToConjunction(Formula body, int databaseInstanceID) {
		if (body instanceof Atom) {
			ArrayList<Term> terms = new ArrayList<>();
			terms.addAll(Arrays.asList(body.getTerms()));
			Variable factId = Variable.create(FACT_ID_ATTRIBUTE_NAME + "_" + factIdNameCounter++);
			terms.add(factId);
			Predicate originalPredicate = ((Atom) body).getPredicate();
			return Conjunction.create(Atom.create(Predicate.create(originalPredicate.getName(), originalPredicate.getArity() + 1), terms.toArray(new Term[terms.size()])),
					Atom.create(LogicalDatabaseInstance.factIdInstanceIdMappingTable, new Term[] { factId, TypedConstant.create(databaseInstanceID) }));

		} else {
			Conjunction con = (Conjunction) body;
			List<Formula> newChildren = new ArrayList<>();
			for (Formula child : con.getChildren()) {
				newChildren.add(addFactIdToConjunction(child, databaseInstanceID));
			}
			return Conjunction.create(newChildren.toArray(new Formula[newChildren.size()]));
		}
	}

	private static Schema extendSchemaWithFactIDs(Schema schema) {
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
		return new Schema(newRelations, schema.getAllDependencies());
	}

	private static Collection<Atom> extendFactsWithFactID(Collection<Atom> facts) {
		List<Atom> extendedFacts = new ArrayList<>();
		for (Atom fact : facts) {
			Atom extendedAtom = Atom.create(Predicate.create(fact.getPredicate().getName(), fact.getPredicate().getArity() + 1), extendTerms(fact.getTerms(), fact.hashCode()));
			extendedFacts.add(extendedAtom);
		}
		return extendedFacts;
	}

	private static Collection<Atom> getFactsMapping(Collection<Atom> facts, int databaseInstanceID) {
		List<Atom> extendedFacts = new ArrayList<>();
		for (Atom fact : facts) {
			Atom atomMapping = Atom.create(factIdInstanceIdMappingTable, TypedConstant.create(fact.hashCode()), TypedConstant.create(databaseInstanceID));
			extendedFacts.add(atomMapping);
		}
		return extendedFacts;
	}

	/**
	 * Takes a collection of facts and extends them (returns facts with all the same
	 * plus an extra term) with the id of this instance.
	 * 
	 * @param terms
	 *            the collection of facts to be changed
	 * @return the input facts each one extended by one term: the instance id
	 */
	private static Term[] extendTerms(Term[] terms, int factID) {
		Term[] newterms = new Term[terms.length + 1];
		System.arraycopy(terms, 0, newterms, 0, terms.length);
		newterms[newterms.length - 1] = TypedConstant.create(factID);
		return newterms;
	}

	private static Collection<Atom> removeFactID(Collection<Atom> facts, Schema originalSchema) {
		List<Atom> newFacts = new ArrayList<>();
		for (Atom fact : facts) {
			if (fact.getPredicate().getName().equals(factIdInstanceIdMappingTable.getName()))
				continue;
			if (originalSchema.getRelation(fact.getPredicate().getName()).getArity() < fact.getTerms().length) {
				// SQL database query returns the factID, we need to remove it.
				newFacts.add(Atom.create(originalSchema.getRelation(fact.getPredicate().getName()), removeFactIdFromTerms(fact.getTerms())));
			} else {
				// memory DB will return exactly the free variables we needed.
				newFacts.add(Atom.create(originalSchema.getRelation(fact.getPredicate().getName()), fact.getTerms()));
			}
		}
		return newFacts;
	}

	private static Term[] removeFactIdFromTerms(Term[] terms) {
		List<Term> newTerms = new ArrayList<>();
		newTerms.addAll(Arrays.asList(terms));
		newTerms.remove(newTerms.size() - 1);
		return newTerms.toArray(new Term[newTerms.size()]);
	}

	private static ConjunctiveQuery createQuery(Relation r, int databaseInstanceID) {
		ArrayList<Variable> freeVariables = new ArrayList<>();
		ArrayList<Variable> body = new ArrayList<>();
		for (int i = 0; i < r.getAttributes().length - 1; i++) {
			freeVariables.add(Variable.create("x" + i));
			body.add(Variable.create("x" + i));
		}
		Variable factID = Variable.create(FACT_ID_TABLE_NAME);
		body.add(factID);
		Conjunction conjunction = Conjunction.create(Atom.create(r, body.toArray(new Term[body.size()])),
				Atom.create(LogicalDatabaseInstance.factIdInstanceIdMappingTable, new Term[] { factID, TypedConstant.create(databaseInstanceID) }));
		return ConjunctiveQuery.create(freeVariables.toArray(new Variable[freeVariables.size()]), conjunction.getAtoms());
	}

	/**
	 * When we query a single table it is possible to convert the result matches
	 * into Atoms as they were the facts stored in the table.
	 * 
	 * @param matches
	 * @param r
	 * @return
	 */
	private static ArrayList<Atom> getAtomsFromMatches(List<Match> matches, Relation r) {
		ArrayList<Atom> ret = new ArrayList<>();
		Predicate predicate = Predicate.create(r.getName(), r.getArity() - 1, r.isEquality());
		for (Match m : matches) {
			List<Term> terms = new ArrayList<>();
			for (Term t : m.getFormula().getTerms()) {
				Term newTerm = m.getMapping().get(t);
				if (newTerm != null)
					terms.add(newTerm);
			}
			ret.add(Atom.create(predicate, terms.toArray(new Term[terms.size()])));
		}
		return ret;
	}

	/**
	 * Drops the database, and clears the cache for the current instance.
	 * 
	 * For safety reasons it recreates the same database, and leaves the empty
	 * database there. This is needed since most database provider will not allow
	 * remote connection to a none existing database, so we would force the user to
	 * manually create an empty database after each usage of this system.
	 */
	@Override
	public void dropDatabase() throws DatabaseException {
		multiCache.clearCache(databaseInstanceID);
		if (edm != null)
			edm.dropDatabase();
	}

	@Override
	public void shutdown() throws DatabaseException {
		if (edm != null)
			edm.shutdown();
	}

	@Override
	public void setSchema(Schema schema) {
		this.originalSchema = schema;
		this.extendedSchema = extendSchemaWithFactIDs(schema);
		if (edm != null)
			edm.setSchema(this.extendedSchema);
	}

	@Override
	public Schema getSchema() {
		return originalSchema;
	}

	@Override
	public int getDatabaseInstanceID() {
		return databaseInstanceID;
	}

	@Override
	public String getDatabaseName() {
		if (edm != null)
			return edm.getDatabaseName();
		return this.toString();
	}

	/**
	 * Adds an extra relation to the existing schema, updates the extended schema
	 * accordingly, and creates the new table in the database.
	 * 
	 * @param newRelation
	 * @throws DatabaseException
	 */
	public void addRelation(Relation newRelation) throws DatabaseException {
		Relation newRelations[] = new Relation[this.originalSchema.getRelations().length + 1];
		int i = 0;
		for (Relation r : this.originalSchema.getRelations())
			newRelations[i++] = r;
		newRelations[i] = newRelation;
		this.originalSchema = new Schema(newRelations, this.originalSchema.getAllDependencies());
		this.setSchema(originalSchema);
		if (edm != null)
			edm.executeUpdateCommand(new CreateTable(this.extendedSchema.getRelation(newRelation.getName()), edm.parameters.isFactsAreUnique()));
	}

	@Override
	public List<String> executeQueryExplain(ConjunctiveQuery cq) throws DatabaseException {
		if (edm != null)
			return edm.executeQueryExplain(extendQuery(cq, this.databaseInstanceID));
		return null;
	}

}
