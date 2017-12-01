package uk.ac.ox.cs.pdq.databasemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
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
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * Replaces the OLD_DatabaseManager to create multiple virtual database instances
 * over one physical instance. The main idea is: <br>
 * - every fact gets extended with a fact ID and this object will maintain a
 * mapping table where each factID paired with an InstanceID, and we allow
 * querying facts of one specific instance or in the entire table.
 * 
 * 
 * @author Gabor
 *
 */
public class VirtualMultiInstanceDatabaseManager extends DatabaseManager {
	private Schema extendedSchema;
	private Schema originalSchema;
	protected static final String FACT_ID_TABLE_NAME = "DBFactID";
	protected static final String FACT_ID_ATTRIBUTE_NAME = "FactId";
	protected static final Attribute FACT_ID_ATTRIBUTE = Attribute.create(Integer.class, FACT_ID_ATTRIBUTE_NAME);
	protected static final Relation factIdInstanceIdMappingTable = Relation.create("InstanceIdMapping",
			new Attribute[] { FACT_ID_ATTRIBUTE, Attribute.create(Integer.class, "DatabaseInstanceID") }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });

	protected MultiInstanceFactCache multiCache;

	/**
	 * Creates database manager and connection if needed based on the parameters.
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	public VirtualMultiInstanceDatabaseManager(DatabaseParameters parameters) throws DatabaseException {
		super(parameters);
		multiCache = new MultiInstanceFactCache();
	}
	
	protected VirtualMultiInstanceDatabaseManager() throws DatabaseException {
		super();
	}

	/**
	 * Creates a canonical database for the schema.
	 * 
	 * @param schema
	 * @throws DatabaseException
	 */
	@Override
	public void initialiseDatabaseForSchema(Schema schema) throws DatabaseException {
		this.originalSchema = schema;
		this.extendedSchema = extendSchemaWithFactIDs(schema);
		super.initialiseDatabaseForSchema(extendedSchema);
	}

	public void addFacts(Collection<Atom> facts) throws DatabaseException {
		Collection<Atom> factsToAdd = null;
		// only add what's new.
		factsToAdd = multiCache.addFacts(facts, databaseInstanceID);
		List<Atom> extendedFacts = new ArrayList<>();
		extendedFacts.addAll(extendFactsWithFactID(factsToAdd));
		extendedFacts.addAll(getFactsMapping(factsToAdd, this.databaseInstanceID));
		super.addFacts(extendedFacts);
	}

	public void deleteFacts(Collection<Atom> facts) throws DatabaseException {
		multiCache.deleteFacts(facts, databaseInstanceID);
		// only deletes the mapping of this fact to this instance, does not delete the
		// actual fact.
		super.deleteFacts(getFactsMapping(facts, this.databaseInstanceID));
	}

	/**
	 * In case the implementation has in-memory cache this can be used to get the
	 * cached data.
	 * 
	 * @return
	 */
	public Collection<Atom> getCachedFacts() throws DatabaseException {
		return multiCache.getFacts(databaseInstanceID);
	}

	/**
	 * Actual reading from the underlying data structure.
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
			results.addAll(removeFactID(getAtomsFromMatches(super.answerQueries(queries), r), originalSchema));
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.data.OLD_DatabaseManager#answerQueries(java.util.Collection)
	 */
	public List<Match> answerQueries(Collection<ConjunctiveQuery> queries) throws DatabaseException {
		Collection<ConjunctiveQuery> queriesWithInstanceIDs = new ArrayList<>();
		Map<ConjunctiveQuery, ConjunctiveQuery> oldAndNewQueries = new HashMap<>();

		// extend queries with factIDs,
		for (ConjunctiveQuery q : queries) {
			ConjunctiveQuery extendedCQ = extendQuery(q, this.databaseInstanceID);
			oldAndNewQueries.put(extendedCQ, q);
			queriesWithInstanceIDs.add(extendedCQ);
		}
		
		// Answer new queries
		List<Match> matches = super.answerQueries(queriesWithInstanceIDs);
		
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
		List<Match> matches = super.answerQueryDifferences(extendedLQ,extendedRQ);
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

	private static ConjunctiveQuery extendQuery(ConjunctiveQuery formula, int databaseInstanceID) {
		Conjunction newConjunction = addFactIdToConjunction(formula.getBody(), databaseInstanceID);
		return ConjunctiveQuery.create(formula.getFreeVariables(), newConjunction);
	}

	private static Conjunction addFactIdToConjunction(Formula body, int databaseInstanceID) {
		if (body instanceof Atom) {
			ArrayList<Term> terms = new ArrayList<>();
			terms.addAll(Arrays.asList(body.getTerms()));
			Variable factId = Variable.create(FACT_ID_ATTRIBUTE_NAME + "_" + GlobalCounterProvider.getNext(FACT_ID_ATTRIBUTE_NAME));
			terms.add(factId);
			return Conjunction.create(Atom.create(((Atom) body).getPredicate(), terms.toArray(new Term[terms.size()])),
					Atom.create(VirtualMultiInstanceDatabaseManager.factIdInstanceIdMappingTable, new Term[] { factId, TypedConstant.create(databaseInstanceID) }));

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
		List<Dependency> deps = new ArrayList<>();
		deps.addAll(Arrays.asList(schema.getKeyDependencies()));
		deps.addAll(Arrays.asList(schema.getDependencies()));
		return new Schema(newRelations, deps.toArray(new Dependency[deps.size()]));
	}

	private static Collection<Atom> extendFactsWithFactID(Collection<Atom> facts) {
		List<Atom> extendedFacts = new ArrayList<>();
		for (Atom fact : facts) {
			Atom extendedAtom = Atom.create(fact.getPredicate(), extendTerms(fact.getTerms(), fact.hashCode()));
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

	private static Term[] extendTerms(Term[] terms, int factID) {
		List<Term> newTerms = new ArrayList<>();
		newTerms.addAll(Arrays.asList(terms));
		newTerms.add(TypedConstant.create(factID));
		return newTerms.toArray(new Term[newTerms.size()]);
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
				Atom.create(VirtualMultiInstanceDatabaseManager.factIdInstanceIdMappingTable, new Term[] { factID, TypedConstant.create(databaseInstanceID) }));
		return ConjunctiveQuery.create(freeVariables.toArray(new Variable[freeVariables.size()]), conjunction);
	}
	private static ArrayList<Atom> getAtomsFromMatches(List<Match> matches, Relation r) {
		ArrayList<Atom> ret = new ArrayList<>();
		Predicate predicate = Predicate.create(r.getName(), r.getArity(),r.isEquality());
		for (Match m: matches) {
			List<Term> terms = new ArrayList<>();
			for (Term t:m.getFormula().getTerms()) {
				Term newTerm = m.getMapping().get(t);
				if (newTerm!=null)
					terms.add(newTerm);
			}
			ret.add(Atom.create(predicate, terms.toArray(new Term[terms.size()])));
		}
		return ret;
	}

}
