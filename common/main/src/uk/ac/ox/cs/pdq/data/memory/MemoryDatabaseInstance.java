package uk.ac.ox.cs.pdq.data.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.data.PhysicalDatabaseCommand;
import uk.ac.ox.cs.pdq.data.PhysicalDatabaseInstance;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.data.cache.FactCache;
import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Represents a no-sql database instance that keeps facts in the memory and
 * allows querying them.
 * 
 * @author Gabor
 *
 */
public class MemoryDatabaseInstance extends PhysicalDatabaseInstance {
	private FactCache facts;
	private DatabaseParameters parameters;
	private String databaseName;

	public MemoryDatabaseInstance(DatabaseParameters parameters) {
		this.parameters = parameters;
		facts = new FactCache("MemoryDatabaseCache");
		databaseName = this.parameters.getDatabaseName();
	}

	@Override
	protected void initialiseConnections(DatabaseParameters parameters) {
	}

	@Override
	protected void closeConnections(boolean dropDatabase) {
		facts.clearCache();
	}

	@Override
	protected void initialiseDatabaseForSchema(Schema schema) {
	}

	@Override
	protected void dropDatabase() {
		facts.clearCache();
	}

	@Override
	protected void addFacts(Collection<Atom> facts) throws DatabaseException {
		this.facts.addFacts(facts);
	}

	@Override
	protected void deleteFacts(Collection<Atom> facts) {
		this.facts.removeFacts(facts);
	}

	@Override
	protected Collection<Atom> getFactsFromPhysicalDatabase() {
		return this.facts.getFacts();
	}

	@Override
	protected List<Match> answerQueries(Collection<PhysicalQuery> queries) throws DatabaseException {
		List<Match> matches = new ArrayList<>();
		for (PhysicalQuery query : queries) {
			matches.addAll(answerQuery(query));
		}
		return matches;
	}

	/** Same as answerQueries but it deals with a single query at a time.
	 * @param query
	 * @return
	 * @throws DatabaseException
	 */
	private Collection<Match> answerQuery(PhysicalQuery query) throws DatabaseException {
		if (!(query instanceof MemoryQuery))
			throw new DatabaseException("Only MemoryQuery can be answered in MemoryDatabaseInstance!" + query);
		ConjunctiveQuery q = (ConjunctiveQuery) query.getFormula();
		Variable[] freeVariables = q.getFreeVariables();
		Collection<Match> results = new ArrayList<>();
		
		// get matching Atoms
		Collection<Atom> matchingFacts = getMatchingFactsForQuery(this.facts.getFacts(), (MemoryQuery)query);
		
		// create Variable - Constant matching form Atoms
		for (Atom a : matchingFacts) {
			Map<Variable, Constant> mapping;
			mapping = createMapping(freeVariables, a, q);
			if (!mapping.isEmpty())
				results.add(Match.create(q, mapping));
		}
		
		// check for query difference. If it is a simple query we can return the results.
		if (((MemoryQuery) query).getRightQuery()==null) {
			return results;
		}
		
		// Need to run the Right side query
		ConjunctiveQuery qRight = (ConjunctiveQuery) ((MemoryQuery) query).getRightQuery().getFormula();
		Variable[] freeVariablesRight = ((MemoryQuery) query).getRightQuery().getFormula().getFreeVariables();
		Collection<Atom> matchingFactsRight = getMatchingFactsForQuery(this.facts.getFacts(), ((MemoryQuery) query).getRightQuery());
		
		// check for matching, and remove the results that are appearing on both sides.
		for (Atom a : matchingFactsRight) {
			Map<Variable, Constant> mapping;
			Map<Variable, Constant> mappingShort;
			mapping = createMapping(freeVariablesRight, a, qRight);
			if (!mapping.isEmpty()) {
				mappingShort = createMapping(freeVariables, a, q);
				results.remove(Match.create(q, mappingShort));
			}
		}
		return results;
	}

	private Collection<Atom> getMatchingFactsForQuery(Collection<Atom> facts, MemoryQuery query) {
		if (((ConjunctiveQuery) query.getFormula()).getBody() instanceof Atom) {
			return getFactsOfRelation(facts, ((Atom) ((ConjunctiveQuery) query.getFormula()).getBody()).getPredicate().getName(),
					query.getConstantEqualityConditions());
		} else { 
			return search(facts, (Conjunction) ((ConjunctiveQuery) query.getFormula()).getBody(), query.getAttributeEqualityConditions(),
					((MemoryQuery) query).getConstantEqualityConditions());
		}
	}

	/**
	 * The search function will give us joined atoms such as
	 * ConjunctionOf[A,B](c1,c2,c3,c4). This function will map the query results
	 * with the free variables of the query, and provide Variable(x)=c2 type of
	 * answers.
	 * 
	 * @param freeVariables
	 * @param acurrentFact
	 * @param q
	 * @return
	 * @throws Exception
	 */
	private Map<Variable, Constant> createMapping(Variable[] freeVariables, Atom acurrentFact, ConjunctiveQuery q) throws DatabaseException {
		if (acurrentFact.getPredicate().getName().startsWith("ConjunctionOf")) {
			String originalPredicateName = acurrentFact.getPredicate().getName();
			String subPredicateNames = originalPredicateName.substring("ConjunctionOf".length() + 1, originalPredicateName.length() - 1);
			String leftPredicateName = subPredicateNames.substring(0, subPredicateNames.indexOf(','));
			String rightPredicateName = subPredicateNames.substring(subPredicateNames.indexOf(',') + 1);

			Map<Variable, Constant> results = new HashMap<>();
			for (int j = 0; j < freeVariables.length; j++) {
				for (int l = 0; l < q.getAtoms().length; l++) {
					for (int k = 0; k < q.getAtoms()[l].getTerms().length; k++) {
						if (freeVariables[j].equals(q.getAtoms()[l].getTerms()[k])) {
							if (leftPredicateName.equals(q.getAtoms()[l].getPredicate().getName())) {
								results.put(freeVariables[j], (Constant) acurrentFact.getTerms()[k]);
							} else {
								if (rightPredicateName.contains(q.getAtoms()[l].getPredicate().getName())) {
									results.put(freeVariables[j], (Constant) acurrentFact.getTerms()[getIndexFor(originalPredicateName, freeVariables[j], q.getAtoms()[l], q)]);
								}
							}
						}
					}
				}
			}
			return results;
		}
		Map<Variable, Constant> results = new HashMap<>();
		for (int j = 0; j < freeVariables.length; j++) {
			for (int l = 0; l < q.getAtoms().length; l++) {
				for (int k = 0; k < q.getAtoms()[l].getTerms().length; k++) {
					if (freeVariables[j].equals(q.getAtoms()[l].getTerms()[k]) && acurrentFact.getPredicate().getName() == q.getAtoms()[l].getPredicate().getName()) {
						results.put(freeVariables[j], (Constant) acurrentFact.getTerms()[k]);
					}
				}
			}
		}
		return results;
	}

	/**
	 * Figures out the index of a desired Variable in a virtual predicate created as
	 * a conjunction of multiple atoms. For example we have an atom
	 * ConjunctionOf[A,B](c1,c2,c3,c4) where c1 and c2 are columns of relation A,
	 * while c3 and c4 are columns of relation B, and we have a variable(X) that
	 * appears in the ConjunctiveQuery under an atom with Predicate name B, then the
	 * return value should be the index of this variable(X) in B plus the arity of
	 * relation A.
	 * 
	 * This is a recursive function since Conjunctions can be nested.
	 * 
	 * @throws Exception
	 */
	private int getIndexFor(String predicateName, Variable variable, Atom queryAtom, ConjunctiveQuery q) throws DatabaseException {
		if (!predicateName.contains("ConjunctionOf")) {
			for (int i = 0; i < queryAtom.getTerms().length; i++) {
				if (queryAtom.getTerm(i).equals(variable)) {
					return i;
				}
			}
			throw new DatabaseException("Variable not found! V: " + variable + " Query: " + queryAtom);
		}
		String originalPredicateName = predicateName;
		String subPredicateNames = originalPredicateName.substring("ConjunctionOf".length() + 1, originalPredicateName.length() - 1);
		String leftPredicateName = subPredicateNames.substring(0, subPredicateNames.indexOf(','));
		String rightPredicateName = subPredicateNames.substring(subPredicateNames.indexOf(',') + 1);
		if (leftPredicateName.equals(queryAtom.getPredicate().getName())) {
			for (int i = 0; i < queryAtom.getTerms().length; i++) {
				if (queryAtom.getTerm(i).equals(variable)) {
					return i;
				}
			}
			throw new DatabaseException("Variable not found! V: " + variable + " Query: " + queryAtom);
		} else {
			int shift = -1;
			for (int i = 0; i < q.getAtoms().length; i++) {
				if (q.getAtom(i).getPredicate().getName().equals(leftPredicateName)) {
					shift = q.getAtom(i).getPredicate().getArity();
					break;
				}
			}
			if (shift < 0)
				throw new DatabaseException("LeftPredicate not found! predicate name: " + leftPredicateName + " Query: " + queryAtom);
			return shift + getIndexFor(rightPredicateName, variable, queryAtom, q);
		}
	}

	/**
	 * Recursive function, receives a set of facts and a Conjunction and creates
	 * joined facts filtering according to dependent join conditions and constants
	 * in the query.
	 * 
	 * @param facts
	 *            list of facts as input.
	 * @param currentConjunction
	 *            current conjunction, recursively calls itself to loop through all
	 *            levels of the conjunction hierarchy.
	 * @param conditions
	 *            Attribute equality conditions grouped in ConjunctiveCondition
	 *            objects. For simplicity the whole map is passed down and each
	 *            search round will find it's own set of conditions in it.
	 * @param equalityConditionsPerRelations
	 *            Attribute equality conditions are applied when we read a relation
	 *            with the getFactsOfRelation function.
	 * @return
	 */
	private Collection<Atom> search(Collection<Atom> facts, Conjunction currentConjunction, Map<Conjunction, ConjunctiveCondition> conditions,
			Map<String, List<ConstantEqualityCondition>> equalityConditionsPerRelations) {
		Collection<Atom> results = new ArrayList<>();
		Collection<Atom> leftFacts = null;
		Collection<Atom> rightFacts = null;

		// get facts for each side of the conjunction. This could be a recursive call in
		// case we have nested conjunctions.
		if (currentConjunction.getChild(0) instanceof Atom)
			leftFacts = getFactsOfRelation(facts, ((Atom) currentConjunction.getChild(0)).getPredicate().getName(), equalityConditionsPerRelations);
		if (currentConjunction.getChild(0) instanceof Conjunction)
			leftFacts = search(facts, (Conjunction) currentConjunction.getChild(0), conditions, equalityConditionsPerRelations);
		if (currentConjunction.getChild(1) instanceof Atom)
			rightFacts = getFactsOfRelation(facts, ((Atom) currentConjunction.getChild(1)).getPredicate().getName(), equalityConditionsPerRelations);
		if (currentConjunction.getChild(1) instanceof Conjunction)
			rightFacts = search(facts, (Conjunction) currentConjunction.getChild(1), conditions, equalityConditionsPerRelations);

		ConjunctiveCondition condition = conditions.get(currentConjunction);
		if (condition != null) {
			// We have conditions for this conjunction so it is a
			// dependent join case
			for (Atom fLeft : leftFacts) {
				for (Atom fRight : rightFacts) {
					boolean matchesAllConditions = true;
					// check if we match all conditions
					for (SimpleCondition cs : condition.getSimpleConditions()) {
						if (cs instanceof AttributeEqualityCondition) {
							AttributeEqualityCondition acs = (AttributeEqualityCondition) cs;
							if (fLeft.getTerm(acs.getPosition()) != fRight.getTerm(acs.getOther())) {
								matchesAllConditions = false;
							}
						}
					}
					// create new record. For example A(1,2) joined with B(3,4) will create a record
					// with a virtual predicate called "ConjunctionOf[A,B]" and it will have arity =
					// A.arity + B.arity, so it will look like ConjunctionOf[A,B](1,2,3,4)
					if (matchesAllConditions) {
						List<Term> terms = new ArrayList<>();
						terms.addAll(Arrays.asList(fLeft.getTerms()));
						terms.addAll(Arrays.asList(fRight.getTerms()));
						String newPredicateName = "ConjunctionOf[" + fLeft.getPredicate().getName() + "," + fRight.getPredicate().getName() + "]";
						Atom newAtom = Atom.create(Predicate.create(newPredicateName, fLeft.getPredicate().getArity() + fRight.getPredicate().getArity()),
								terms.toArray(new Term[terms.size()]));
						results.add(newAtom);
					}
				}
			}
		} else {
			// We have no conditions for this conjunction so it is a
			// normal cross join case
			for (Atom fLeft : leftFacts) {
				for (Atom fRight : rightFacts) {
					List<Term> terms = new ArrayList<>();
					terms.addAll(Arrays.asList(fLeft.getTerms()));
					terms.addAll(Arrays.asList(fRight.getTerms()));
					String newPredicateName = "ConjunctionOf[" + fLeft.getPredicate().getName() + "," + fRight.getPredicate().getName() + "]";
					Atom newAtom = Atom.create(Predicate.create(newPredicateName, fLeft.getPredicate().getArity() + fRight.getPredicate().getArity()),
							terms.toArray(new Term[terms.size()]));
					results.add(newAtom);
				}
			}
		}
		return results;
	}

	/**
	 * Reads all facts with the given relationName that does not conflicts with the
	 * conditions. (In case there are no conditions all facts will be returned for
	 * the given relationName)
	 * 
	 * @param facts
	 *            input facts to seatch in.
	 * @param relationName
	 *            facts that we are interested in.
	 * @param equalityConditionsPerRelations
	 *            optional conditions, will be evaluated when given.
	 * @return filtered list of facts.
	 */
	private Collection<Atom> getFactsOfRelation(Collection<Atom> facts, String relationName, Map<String, List<ConstantEqualityCondition>> equalityConditionsPerRelations) {
		Collection<Atom> results = new ArrayList<>();
		for (Atom f : facts) {
			// loop over all data
			if (f.getPredicate().getName().equals(relationName)) {
				boolean matching = true;
				if (equalityConditionsPerRelations.containsKey(relationName)) {
					for (ConstantEqualityCondition condition : equalityConditionsPerRelations.get(relationName)) {
						if (!f.getTerm(condition.getPosition()).equals(condition.getConstant())) {
							matching = false;
						}
					}
				}
				if (matching)
					results.add(f);
			}
		}
		return results;
	}

	@Override
	protected int executeUpdates(List<PhysicalDatabaseCommand> update) throws DatabaseException {
		throw new DatabaseException("executeUpdates not implemented in MemoryDatabaseInstance!");
	}

	@Override
	protected Collection<Atom> getFactsOfRelation(Relation r) {
		return getFactsOfRelation(this.facts.getFacts(), r.getName(), new HashMap<>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Memory database (" + databaseName + "). Contains " + this.facts.getFacts().size() + ".";
	}
}
