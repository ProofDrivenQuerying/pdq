package uk.ac.ox.cs.pdq.databasemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQueryWithInequality;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Memory database manager. Does the same as the {@link LogicalDatabaseInstance}
 * but everything is stored only in memory.
 * 
 * @author Gabor
 *
 */
public class InternalDatabaseManager extends LogicalDatabaseInstance {

	/**
	 * Creates a database manager with the default databaseName
	 * 
	 * @throws DatabaseException
	 */
	public InternalDatabaseManager() throws DatabaseException {
		this(new MultiInstanceFactCache(), 1);
	}

	/**
	 * Creates a database manager with the given databaseName
	 * 
	 * @param databaseName
	 */
	public InternalDatabaseManager(MultiInstanceFactCache cache, int databaseInstanceID) throws DatabaseException {
		super(cache, databaseInstanceID);
	}

	@Override
	public void initialiseDatabaseForSchema(Schema schema) throws DatabaseException {
		super.setSchema(schema);
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
		InternalDatabaseManager vmidm = new InternalDatabaseManager(this.multiCache, newDatabaseInstanceID);
		vmidm.extendedSchema = this.extendedSchema;
		vmidm.originalSchema = this.originalSchema;
		return vmidm;
	}

	/**
	 * Stores this fact as a record in the database. Table name will be the same as
	 * the predicate name, column names will be the same as the attribute names in
	 * the relation.
	 * 
	 * For this call to work you have to have the database initialised for this
	 * schema. The software cannot check for this initialisation, since it have to
	 * be done only once (to create the tables) and then the database manager by
	 * connecting to the same database can continue work on the pre-initialised
	 * database.
	 * 
	 * @param facts
	 * @throws DatabaseException
	 */
	public void addFacts(Collection<Atom> facts) throws DatabaseException {
		multiCache.addFacts(facts, databaseInstanceID);
	}

	/**
	 * Deletes a list of facts from the database one by one. (bulk delete is not
	 * implemented yet)
	 * 
	 * @param facts
	 * @throws DatabaseException
	 */
	public void deleteFacts(Collection<Atom> facts) throws DatabaseException {
		multiCache.deleteFacts(facts, databaseInstanceID);
	}

	/**
	 * same as get cached facts, since we are in memory only mode..
	 * 
	 * @return
	 */
	public Collection<Atom> getFactsFromPhysicalDatabase() throws DatabaseException {
		return multiCache.getFacts(databaseInstanceID);
	}

	public Collection<Atom> getCachedFacts() throws DatabaseException {
		return multiCache.getFacts(databaseInstanceID);
	}

	/**
	 * A list of CQs to be executed parallel. All results are gathered and added to
	 * the list.
	 * 
	 * @param queries
	 * @return
	 * @throws DatabaseException
	 */
	public List<Match> answerConjunctiveQueries(Collection<ConjunctiveQuery> queries) throws DatabaseException {
		List<Match> matches = new ArrayList<>();
		for (ConjunctiveQuery q : queries) {
			matches.addAll(answerConjunctiveQuery(q, this.databaseInstanceID));
		}
		return matches;

	}

	/**
	 * Represent a kind of nested select that will tell the difference between two
	 * BasicSelects.
	 * 
	 * <pre>
	 * Example: 
	 * Left query: exists[x,y](R(x,y,z) & S(x,y))
	 * Right query:exists[x,y,z](R(x,y,z) & (S(x,y) & T(z,res1,res2)))
	 * 
	 * The result will be all facts that only satisfy the left query, but not the right one.
	 * 
	 * </pre>
	 * 
	 * @param leftQuery
	 * @param rightQuery
	 * @return
	 * @throws DatabaseException
	 */
	public List<Match> answerQueryDifferences(ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery) throws DatabaseException {
		Map<String, Term[]> formulaCache = new HashMap<>(); // used for analysing queries.
		// execute query left
		List<Atom> leftFacts = answerConjunctiveQueryRecursively(leftQuery.getBody(), leftQuery, this.databaseInstanceID, formulaCache);
		if (leftFacts == null || leftFacts.isEmpty())
			return new ArrayList<>();

		// execute right
		List<Atom> rightFacts = answerConjunctiveQueryRecursively(rightQuery.getBody(), rightQuery, this.databaseInstanceID, formulaCache);

		if (rightFacts == null || rightFacts.isEmpty()) {
			// nothing to sort out, convert to Match objects and go.
			Term[] resultTerms = formulaCache.get(leftFacts.get(0).getPredicate().getName());
			return convertToMatch(leftQuery, leftFacts, Arrays.asList(resultTerms));
		} else {
			// convert the right results to match left result's signature
			Term[] leftTerms = formulaCache.get(leftFacts.get(0).getPredicate().getName());
			Term[] rightTerms = formulaCache.get(rightFacts.get(0).getPredicate().getName());
			List<Atom> convertedRightFacts = convertToAtom(rightQuery, rightFacts, leftTerms, Arrays.asList(rightTerms), leftFacts.get(0).getPredicate().getName());

			// delete the parts we don't want
			leftFacts.removeAll(convertedRightFacts);

			// convert the results to Matches and return.
			return convertToMatch(leftQuery, leftFacts, Arrays.asList(leftTerms));
		}
	}

	/**
	 * In case of memory database there is nothing to drop.
	 */
	public void dropDatabase() throws DatabaseException {
		multiCache.clearCache(this.databaseInstanceID);
	}

	/**
	 * Nothing to shut down.
	 */
	public void shutdown() throws DatabaseException {
		multiCache.clearCache(this.databaseInstanceID);
	}

	public int getDatabaseInstanceID() {
		return databaseInstanceID;
	}

	public void setDatabaseInstanceID(int instanceID) {
		databaseInstanceID = instanceID;
	}

	public String getDatabaseName() {
		return "InternalDatabase";
	}

	/**
	 * Answers a basic CQ over the given instance.
	 */
	protected List<Match> answerConjunctiveQuery(ConjunctiveQuery cq, int instanceId) throws DatabaseException {
		Map<String, Term[]> formulaCache = new HashMap<>(); // used for analysing queries.
		// get facts
		List<Atom> facts = answerConjunctiveQueryRecursively(cq.getBody(), cq, instanceId, formulaCache);
		// return empty list if we have no data
		if (facts == null || facts.isEmpty())
			return new ArrayList<>();
		// convert to matches and return
		Term[] resultTerms = formulaCache.get(facts.get(0).getPredicate().getName());
		return convertToMatch(cq, facts, Arrays.asList(resultTerms));
	}

	/**
	 * From the given facts it removes the ones that do not match with the constants
	 * in the formula.
	 */
	private List<Atom> filterConstantEqualities(Formula formula, List<Atom> facts) {
		for (int i = 0; i < formula.getTerms().length; i++) {
			List<Atom> todelete = new ArrayList<>();
			Term t = formula.getTerms()[i];
			if (!t.isVariable()) {
				for (Atom a : facts) {
					if (a.getTerms()[i] != t) {
						todelete.add(a);
					}
				}
			}
			facts.removeAll(todelete);
		}
		return facts;
	}

	/**
	 * A query formula can be an atom or a conjunction. this function deals with the
	 * case when the formula is an Atom. In this case we need to have all data from
	 * the predicate specified by the atom, and filter out the constant equalities
	 * in case there are any.
	 * 
	 * @param formula
	 * @param instanceId
	 * @return
	 */
	private List<Atom> answerSingleAtomQuery(Atom formula, int instanceId, Map<String, Term[]> formulaCache) {
		// single atom query, we can have only attribute equalities
		String predicateName = ((Atom) formula).getPredicate().getName();
		List<Atom> facts = multiCache.getFactsOfRelation(predicateName, instanceId);
		facts = filterConstantEqualities(formula, facts);
		formulaCache.put(predicateName, formula.getTerms());
		return facts;
	}

	private List<Atom> answerConjunctiveQueryRecursively(Formula formula, ConjunctiveQuery cq, int instanceId, Map<String, Term[]> formulaCache) throws DatabaseException {

		if (formula instanceof Atom) {
			// single atom case
			List<Atom> facts = answerSingleAtomQuery((Atom) formula, instanceId, formulaCache);
			return filterInequalities(facts, cq, formulaCache);
		} else {
			// atom + atom, or atom + conjunction case.
			if (((Conjunction) formula).getChildren().length != 2)
				throw new DatabaseException("Invalid conjunction (" + formula + ") in query: " + cq + ", wrong number of children.");
			Formula fLeft = ((Conjunction) formula).getChild(0);
			Formula fRight = ((Conjunction) formula).getChild(1);
			if (!(fLeft instanceof Atom))
				throw new DatabaseException("Invalid conjunction (" + formula + ") in query: " + cq + ", left formula should be an atom.");
			// conjunction of two atoms
			// these facts will be filtered by constant equality conditions
			List<Atom> factsLeft = answerSingleAtomQuery((Atom) fLeft, instanceId, formulaCache);
			if (factsLeft.isEmpty()) {
				return new ArrayList<>();
			}
			List<Atom> factsRight = null;
			int rightArity = 0;
			String rightName = null;
			Term[] rightTerms = null;

			// prepare right side atoms
			if (fRight instanceof Atom) {
				// the conjunction was made by two atoms.
				factsRight = answerSingleAtomQuery((Atom) fRight, instanceId, formulaCache);
				if (factsRight.isEmpty()) {
					return new ArrayList<>();
				}
				rightArity = ((Atom) fRight).getPredicate().getArity();
				rightName = ((Atom) fRight).getPredicate().getName();
				rightTerms = fRight.getTerms();
			} else {
				// the conjunction was made by an atoms and a conjunction, recursion needed.
				if (!(fRight instanceof Conjunction))
					throw new DatabaseException("Invalid conjunction (" + formula + ") in query: " + cq + ", wrong children types.");
				factsRight = answerConjunctiveQueryRecursively(fRight, cq, instanceId, formulaCache);
				if (factsRight.isEmpty()) {
					return new ArrayList<>();
				}
				rightArity = factsRight.get(0).getPredicate().getArity();
				rightName = factsRight.get(0).getPredicate().getName();
				rightTerms = formulaCache.get(rightName);
			}
			List<Atom> results = new ArrayList<>();
			// now we have both left and right side atoms so we
			// have to create a cross join, accounting with attribute equalities.
			// first the new cross join predicate needs to be created
			Predicate joint = Predicate.create(((Atom) fLeft).getPredicate().getName() + "_" + rightName, ((Atom) fLeft).getPredicate().getArity() + rightArity);

			// the terms of this result is the left terms + right terms
			List<Term> formulaTerms = new ArrayList<>();
			formulaTerms.addAll(Arrays.asList(fLeft.getTerms()));
			formulaTerms.addAll(Arrays.asList(rightTerms));

			// we need to cache these terms
			formulaCache.put(joint.getName(), formulaTerms.toArray(new Term[formulaTerms.size()]));

			// the actual cross join
			for (Atom lf : factsLeft) {
				for (Atom rf : factsRight) {
					if (checkAttributeEqualities(lf, rf, (Conjunction) formula, formulaCache)) {
						List<Term> terms = new ArrayList<>();
						terms.addAll(Arrays.asList(lf.getTerms()));
						terms.addAll(Arrays.asList(rf.getTerms()));
						results.add(Atom.create(joint, terms.toArray(new Term[terms.size()])));
					}
				}
			}
			return filterInequalities(results, cq, formulaCache);
		}
	}

	/**
	 * In case the CQ is a ConjunctiveQueryWithInequality it will check and filter
	 * out the disallowed facts. Otherwise returns the input facts.
	 * 
	 * @param facts
	 * @param cq
	 * @param formulaCache
	 * @return
	 */
	private List<Atom> filterInequalities(List<Atom> facts, ConjunctiveQuery cq, Map<String, Term[]> formulaCache) {
		if (!(cq instanceof ConjunctiveQueryWithInequality))
			return facts;
		ConjunctiveQueryWithInequality cqw = (ConjunctiveQueryWithInequality) cq;
		if (cqw.getInequalities() == null || cqw.getInequalities().isEmpty())
			return facts;
		List<Atom> results = new ArrayList<>();
		for (Atom f : facts) {
			List<Term> terms = Arrays.asList(formulaCache.get(f.getPredicate().getName()));
			boolean accepted = true;
			for (Pair<Variable, Variable> inequality : cqw.getInequalities()) {
				if (terms.contains(inequality.getKey()) && terms.contains(inequality.getValue())) {
					// the condition needs to be checked since this result contains both sides
					int leftIndex = terms.indexOf(inequality.getKey());
					int rightIndex = terms.indexOf(inequality.getValue());
					if (f.getTerm(leftIndex).equals(f.getTerm(rightIndex))) {
						accepted = false;
					}
				}
			}
			if (accepted)
				results.add(f);
		}
		return results;
	}

	/**
	 * Finds the attribute equality conditions and returns a true or false value
	 * indicating if this fact matches all conditions or not.
	 * 
	 * @param lf
	 * @param rf
	 * @param formula
	 * @param formulaCache
	 * @return
	 */
	private boolean checkAttributeEqualities(Atom lf, Atom rf, Conjunction formula, Map<String, Term[]> formulaCache) {
		Formula fLeft = ((Conjunction) formula).getChild(0);
		Formula fRight = ((Conjunction) formula).getChild(1);
		Term[] formulaTermsRight = null;
		if (fRight instanceof Atom) {
			formulaTermsRight = fRight.getTerms();
		} else {
			formulaTermsRight = formulaCache.get(rf.getPredicate().getName());
		}

		for (int leftIndex = 0; leftIndex < fLeft.getTerms().length; leftIndex++) {
			Term l = fLeft.getTerms()[leftIndex];
			for (int rightIndex = 0; rightIndex < formulaTermsRight.length; rightIndex++) {
				Term r = formulaTermsRight[rightIndex];
				if (l.equals(r)) {
					// we found an attribute equality
					if (!lf.getTerm(leftIndex).equals(rf.getTerm(rightIndex)))
						return false;
				}

			}
		}
		return true;
	}

	private List<Match> convertToMatch(ConjunctiveQuery cq, List<Atom> facts, List<Term> factVariables) {
		Term[] outputVariables = cq.getFreeVariables();
		List<Match> results = new ArrayList<>();
		for (Atom a : facts) {
			Map<Variable, Constant> mapping = new HashMap<>();
			for (int i = 0; i < outputVariables.length; i++) {
				mapping.put((Variable) outputVariables[i], (Constant) a.getTerms()[factVariables.indexOf(outputVariables[i])]);
			}
			results.add(Match.create(cq, mapping));
		}
		return results;
	}

	private List<Atom> convertToAtom(ConjunctiveQuery cq, List<Atom> facts, Term[] outputVariables, List<Term> factVariables, String atomName) {
		List<Atom> results = new ArrayList<>();
		for (Atom a : facts) {
			List<Constant> values = new ArrayList<>();
			for (int i = 0; i < outputVariables.length; i++) {
				values.add((Constant) a.getTerms()[factVariables.indexOf(outputVariables[i])]);
			}
			results.add(Atom.create(Predicate.create(atomName, values.size()), values.toArray(new Term[values.size()])));
		}
		return results;
	}

	public void addRelation(Relation newRelation) throws DatabaseException {
		Relation newRelations[] = new Relation[this.originalSchema.getRelations().length + 1];
		int i = 0;
		for (Relation r : this.originalSchema.getRelations())
			newRelations[i++] = r;
		newRelations[i] = newRelation;
		List<Dependency> deps = new ArrayList<>();
		deps.addAll(Arrays.asList(this.originalSchema.getKeyDependencies()));
		deps.addAll(Arrays.asList(this.originalSchema.getDependencies()));
		this.originalSchema = new Schema(newRelations, deps.toArray(new Dependency[deps.size()]));
		this.setSchema(originalSchema);
	}

}
