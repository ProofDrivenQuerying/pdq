// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoningdatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQueryWithInequality;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoningdatabase.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands.CreateTable;
import uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands.InsertSelect;

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
	protected static final String INSTANCE_ID_ATTRIBUTE_NAME = "DatabaseInstanceID";
	protected static final String MAPPING_TABLE_NAME = "InstanceIdMapping";
	protected static final String CONSTANTS_TO_ATOMS_TABLE_NAME = "ConstantsToAtoms";
	protected static final String[] CONSTANTS_TO_ATOMS_ATTRIBUTE_NAMES = {"Constant","TableName",FACT_ID_ATTRIBUTE_NAME,INSTANCE_ID_ATTRIBUTE_NAME};
	protected static final Attribute FACT_ID_ATTRIBUTE = Attribute.create(Integer.class, FACT_ID_ATTRIBUTE_NAME);
	protected static final Relation factIdInstanceIdMappingTable = Relation.create(MAPPING_TABLE_NAME,
			new Attribute[] { FACT_ID_ATTRIBUTE, Attribute.create(Integer.class, INSTANCE_ID_ATTRIBUTE_NAME) },
			new AccessMethodDescriptor[] { AccessMethodDescriptor.create(new Integer[] {}) });

	protected MultiInstanceFactCache multiCache;
	private ExternalDatabaseManager edm;
	protected int databaseInstanceID;
	/**
	 * Maps every constant to the atom it appears in. This is used for EGD chase,
	 * and the map is late initialized, it will be populated on the first time it is
	 * used, or in case the cache size grows latger then the maximum allowed size.
	 * 
	 * This cache can be externally stored or there is a memory only mode
	 * implemented in the InternalDatabaseManager.
	 */
	protected Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
	protected boolean constantsInitialized = false;
	private final int maxSizeForConstantsToAtoms = 10000;
	private Relation constToAtomsRelation;

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
	public LogicalDatabaseInstance(MultiInstanceFactCache cache, ExternalDatabaseManager edm, int databaseInstanceID)
			throws DatabaseException {
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
	 * @see uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager#addFacts(java.util.Collection)
	 */
	public void addFacts(Collection<Atom> facts) throws DatabaseException {
		// only add what's new. Each fact has a value and a mapping, this list will show
		// when the value in the current instance is new or not. If it is new we need to
		// add the mapping. But we don't know if the value exists in other instances or
		// not.
		List<Atom> extendedFacts = new ArrayList<>();
		Collection<Atom> newFactsInThisInstance = multiCache.addFacts(facts, databaseInstanceID);
		Collection<Atom> newFactsInAllInstances = multiCache.checkExistsInOtherInstances(newFactsInThisInstance,
				databaseInstanceID);
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
	 * @see uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager#deleteFacts(java.util.Collection)
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
	public List<Match> answerQueryDifferences(ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery)
			throws DatabaseException {
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
			return ConjunctiveQueryWithInequality.create(formula.getFreeVariables(), newConjunction.getAtoms(),
					((ConjunctiveQueryWithInequality) formula).getInequalities());
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
			return (Conjunction) Conjunction.create(
					Atom.create(Predicate.create(originalPredicate.getName(), originalPredicate.getArity() + 1),
							terms.toArray(new Term[terms.size()])),
					Atom.create(LogicalDatabaseInstance.factIdInstanceIdMappingTable,
							new Term[] { factId, TypedConstant.create(databaseInstanceID) }));

		} else {
			Conjunction con = (Conjunction) body;
			List<Formula> newChildren = new ArrayList<>();
			for (Formula child : con.getChildren()) {
				newChildren.add(addFactIdToConjunction(child, databaseInstanceID));
			}
			return (Conjunction) Conjunction.create(newChildren.toArray(new Formula[newChildren.size()]));
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
			Atom extendedAtom = Atom.create(
					Predicate.create(fact.getPredicate().getName(), fact.getPredicate().getArity() + 1),
					extendTerms(fact.getTerms(), fact.hashCode()));
			extendedFacts.add(extendedAtom);
		}
		return extendedFacts;
	}

	private static Collection<Atom> getFactsMapping(Collection<Atom> facts, int databaseInstanceID) {
		List<Atom> extendedFacts = new ArrayList<>();
		for (Atom fact : facts) {
			Atom atomMapping = Atom.create(factIdInstanceIdMappingTable, TypedConstant.create(fact.hashCode()),
					TypedConstant.create(databaseInstanceID));
			extendedFacts.add(atomMapping);
		}
		return extendedFacts;
	}

	/**
	 * Takes a collection of facts and extends them (returns facts with all the same
	 * plus an extra term) with the id of this instance.
	 * 
	 * @param terms the collection of facts to be changed
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
				newFacts.add(Atom.create(originalSchema.getRelation(fact.getPredicate().getName()),
						removeFactIdFromTerms(fact.getTerms())));
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
		Conjunction conjunction = (Conjunction) Conjunction.create(Atom.create(r, body.toArray(new Term[body.size()])),
				Atom.create(LogicalDatabaseInstance.factIdInstanceIdMappingTable,
						new Term[] { factID, TypedConstant.create(databaseInstanceID) }));
		return ConjunctiveQuery.create(freeVariables.toArray(new Variable[freeVariables.size()]),
				conjunction.getAtoms());
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
			edm.executeUpdateCommand(new CreateTable(this.extendedSchema.getRelation(newRelation.getName()),
					edm.parameters.isFactsAreUnique()));
	}

	@Override
	public List<String> executeQueryExplain(ConjunctiveQuery cq) throws DatabaseException {
		if (edm != null)
			return edm.executeQueryExplain(extendQuery(cq, this.databaseInstanceID));
		return null;
	}

	@Override
	public void addToConstantsToAtoms(Constant term, Atom atom) throws DatabaseException {
		if (constantsInitialized) {
			// when the initialization takes place the constants to atom table will be populated from the actual data, we don't need to cache them.
			constantsToAtoms.put(term, atom);
			if (constantsToAtoms.size() >= maxSizeForConstantsToAtoms) {
				flushConstantsToAtomsToDb();
			}
		}
	}

	/**
	 * @throws DatabaseException
	 */
	private void flushConstantsToAtomsToDb() throws DatabaseException {
		if (!constantsInitialized) {
			// Create the table in the database
			constantsInitialized = true;
			// create relation to store constants to atoms cache.
			constToAtomsRelation = Relation.create(CONSTANTS_TO_ATOMS_TABLE_NAME,
					new Attribute[] { Attribute.create(String.class, CONSTANTS_TO_ATOMS_ATTRIBUTE_NAMES[0]),Attribute.create(String.class, CONSTANTS_TO_ATOMS_ATTRIBUTE_NAMES[1]),
							Attribute.create(Integer.class, LogicalDatabaseInstance.INSTANCE_ID_ATTRIBUTE_NAME) });
			this.addRelation(constToAtomsRelation);
			constToAtomsRelation = Relation.create(CONSTANTS_TO_ATOMS_TABLE_NAME,
					new Attribute[] { Attribute.create(String.class, CONSTANTS_TO_ATOMS_ATTRIBUTE_NAMES[0]),Attribute.create(String.class, CONSTANTS_TO_ATOMS_ATTRIBUTE_NAMES[1]),
							Attribute.create(Integer.class, LogicalDatabaseInstance.INSTANCE_ID_ATTRIBUTE_NAME),FACT_ID_ATTRIBUTE });
			// populate the new relation from all stored facts. This step is very slow, but will accelerate EGD chase in a long run.
			for (Relation r : getFactRelations()) {
				populateConstantsTableFromFactsOfRelation(r);
			}
		}
		List<Atom> toWrite = new ArrayList<>();
		// populate the new relation from the memory cache
		for (Constant c : constantsToAtoms.keySet()) {
			for (Atom a : constantsToAtoms.get(c)) {
				toWrite.add(
					Atom.create(constToAtomsRelation, new Term[] { c, 
							TypedConstant.create(a.getPredicate().getName()),
							TypedConstant.create(this.databaseInstanceID),
							TypedConstant.create(a.hashCode())}));
			}
		}
		constantsToAtoms.clear();
		edm.addFacts(toWrite);
	}

	/**
	 * @return a list of relations that contains facts (a table that contains a factID attribute and is not the mapping table, neither the constantsToAtoms table
	 */
	private List<Relation> getFactRelations() {
		List<Relation> ret = new ArrayList<>();
		for (Relation r : edm.schema.getRelations()) {
			// these tables we do not search
			if (CONSTANTS_TO_ATOMS_TABLE_NAME.equals(r.getName()))
				continue;
			if (MAPPING_TABLE_NAME.equals(r.getName()))
				continue;
			// if the table doesn't have a factId we do not need to search it.
			if (r.getAttribute(FACT_ID_ATTRIBUTE_NAME) == null)
				continue;
			ret.add(r);
		}
		return ret;
	}
	/** 
	 * Returns a hashSet (in order to remove duplicates) of Atoms that at some attribute(s) contains the constant.
	 * The search is executed in both inmemory and external.
	 * 
	 */
	public Collection<Atom> getAtomsContainingConstant(Constant constantToFind) throws DatabaseException {
		flushConstantsToAtomsToDb();
		Collection<Atom> results = new HashSet<>();
		results.addAll(constantsToAtoms.get(constantToFind));
		Variable factidVar = Variable.create("V" + FACT_ID_ATTRIBUTE_NAME);
		for (Relation r : getFactRelations()) {
			Variable freeVariables[] = new Variable[r.getArity()];
			for (int i = 0; i < r.getArity(); i++) {
				if (r.getAttribute(i).equals(FACT_ID_ATTRIBUTE)) {
					freeVariables[i] = factidVar;
				} else {
					freeVariables[i] = Variable.create("V"+r.getAttribute(i).getName());
				}
			}
			Atom rAtom = Atom.create(r, freeVariables);
			Atom mapAtom = Atom.create(constToAtomsRelation, new Term[] {constantToFind,UntypedConstant.create(r.getName()),
																		 TypedConstant.create(this.databaseInstanceID),factidVar});
			ConjunctiveQuery cq =ConjunctiveQuery.create(freeVariables, new Atom[] {rAtom,mapAtom}); 
			List<Match> ret = edm.answerConjunctiveQuery(cq);
			for (Match m:ret) {
				List<Term> terms = new ArrayList<>();
				for (Attribute a:r.getAttributes()) {
					if (LogicalDatabaseInstance.FACT_ID_ATTRIBUTE.equals(a))
						continue;
					terms.add(m.getMapping().get(Variable.create("V"+a.getName())));
				}
				results.add(Atom.create(this.originalSchema.getRelation(r.getName()), terms.toArray(new Term[terms.size()])));
			}
		}
		return results;
	}


	/**
	 * Creates as many queries as many attributes R has, and executes a search for
	 * the given constant. Retrurns the whole fact for each match. Does not filter
	 * duplicates.
	 * 
	 * @param r
	 * @param constantToFind
	 * @return
	 * @throws DatabaseException
	 */
	private void populateConstantsTableFromFactsOfRelation(Relation r)
			throws DatabaseException {
		for (int attributeIndex = 0; attributeIndex < r.getArity(); attributeIndex++) {
			String currentAttr = r.getAttribute(attributeIndex).getName();
			if (currentAttr.equals(FACT_ID_ATTRIBUTE_NAME))
				continue;
			try {
				Variable factidVar = Variable.create("V" + FACT_ID_ATTRIBUTE_NAME);
				List<Term> insertVars = new ArrayList<>();
				Variable freeVariables[] = new Variable[r.getArity()];
				for (int i = 0; i < r.getArity(); i++) {
					if (r.getAttribute(i).equals(FACT_ID_ATTRIBUTE)) {
						freeVariables[i] = factidVar;
					} else {
						if (i==attributeIndex)
							insertVars.add(Variable.create("V"+r.getAttribute(i).getName())); // this will be the constant
						freeVariables[i] = Variable.create("V"+r.getAttribute(i).getName());
					}
				}
				insertVars.add(UntypedConstant.create(r.getName()));
				insertVars.add(TypedConstant.create(this.databaseInstanceID));
				insertVars.add(factidVar);
				Atom rAtom = Atom.create(r, freeVariables); 
				Atom mapAtom = Atom.create(factIdInstanceIdMappingTable, new Term[] {factidVar,TypedConstant.create(this.databaseInstanceID)});
				ConjunctiveQuery cq =ConjunctiveQuery.create(freeVariables, new Atom[] {rAtom,mapAtom}); 
				
				InsertSelect is = new InsertSelect(constToAtomsRelation,insertVars,cq, edm.schema);
				this.edm.execute(is);
			} catch (Throwable t) {
				t.printStackTrace();
				throw t;
			}
		}
	}

	/** Attempts to remove the constant from the memory cache as well as from the external db. 
	 * 
	 */
	@Override
	public void removeConstantFromMap(Constant obsoleteConstant) throws DatabaseException {
		constantsToAtoms.removeAll(obsoleteConstant);
		if (constantsInitialized) {
			Atom toDelete = Atom.create(Predicate.create(CONSTANTS_TO_ATOMS_TABLE_NAME,4),new Term[] {
					obsoleteConstant, Variable.create("VTableName"), TypedConstant.create(this.databaseInstanceID),Variable.create("VFactId")
			});
			edm.deleteFacts(Arrays.asList(new Atom[] {toDelete}));
		}
	}

	/**
	 * Will copy the memory cache and the external data as well to the new instance (this)
	 */
	@Override
	public void mergeConstantsToAtomsMap(DatabaseManager from) throws DatabaseException {
		if (!(from instanceof LogicalDatabaseInstance)) {
			throw new RuntimeException("LogicalDatabaseInstance cannot be merged into " + from);
		}
		int fromDbInstanceId = ((LogicalDatabaseInstance) from).databaseInstanceID;
		this.constantsInitialized = ((LogicalDatabaseInstance) from).constantsInitialized;
		if (constantsInitialized) {
			try {
				Variable Vfactid = Variable.create("V" + FACT_ID_ATTRIBUTE_NAME);
				Variable Vconstant = Variable.create("VConstant");
				Variable VtableName = Variable.create("VTableName");
				
				Atom rAtom = Atom.create(constToAtomsRelation, new Term[] { Vconstant, VtableName, TypedConstant.create(fromDbInstanceId), Vfactid}); 
				ConjunctiveQuery cq =ConjunctiveQuery.create(new Variable[] {Vconstant, VtableName, Vfactid}, new Atom[] {rAtom}); 
				List<Term> insertVars = new ArrayList<>();
				insertVars.add(Vconstant);
				insertVars.add(VtableName);
				insertVars.add(TypedConstant.create(this.databaseInstanceID));
				insertVars.add(Vfactid);
				InsertSelect is = new InsertSelect(constToAtomsRelation,insertVars,cq, edm.schema);
				// copy database data
				this.edm.execute(is);
			} catch (Throwable t) {
				t.printStackTrace();
				throw t;
			}
		}
		// copy cached data
		this.constantsToAtoms.putAll(((LogicalDatabaseInstance) from).constantsToAtoms);

	}
}
