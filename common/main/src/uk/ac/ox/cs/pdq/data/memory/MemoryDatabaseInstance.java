package uk.ac.ox.cs.pdq.data.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import uk.ac.ox.cs.pdq.fol.Formula;
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
	private Schema schema;

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
		this.schema = schema;
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
		for (PhysicalQuery query:queries) {
			matches.addAll(answerQuery(query));
		}
		return matches;
		//throw new DatabaseException("answerQueries not implemented in MemoryDatabaseInstance!");
	}

	private Collection<Match> answerQuery(PhysicalQuery query) throws DatabaseException {
		if (!(query instanceof MemoryQuery))
			throw new DatabaseException("Only MemoryQuery can be answered in MemoryDatabaseInstance!" + query);
		ConjunctiveQuery q = (ConjunctiveQuery) query.getFormula();
		Variable[] freeVariables = q.getFreeVariables();
		Collection<Match> results = new ArrayList<>();
		Collection<Atom> matchingFacts = search(this.facts.getFacts(), q.getBody(), q.getFreeVariables());
		for (Atom a:matchingFacts) {
			results.add(Match.create(q, createMapping(freeVariables,a,this.schema)));
		}
		return results;
	}

	private Map<Variable, Constant> createMapping(Variable[] freeVariables, Atom a, Schema schema2) {
		Map<Variable, Constant> results = new HashMap<>();
		for (int j = 0 ; j < freeVariables.length; j++) {
			results.put(freeVariables[j], (Constant)a.getTerms()[j]);
		}
		return results;
	}

	private Collection<Atom> search(Collection<Atom> facts, Formula body, Variable[] freeVariables) {
		Collection<Atom> results = new ArrayList<>();
		if (body instanceof Atom) {
			for (Atom f:facts) {
				// loop over all data
				if (f.getPredicate().getName().equals(((Atom) body).getPredicate().getName())) {
					boolean matching = true;
					for (int i = 0;i< body.getTerms().length; i++) {
						Term queryTerm = body.getTerms()[i];
						Term dataTerm = f.getTerms()[i];
						if (queryTerm instanceof Constant && !queryTerm.equals(dataTerm)) {
							matching = false;
						}
					}
					if (matching)
						results.add(f);
				}
			}
		} else {
			Atom[] queryAtoms = ((Conjunction) body).getAtoms();
			Map<Atom,Collection<Atom>> res = new HashMap<>();
			//List<Integer> boundTermIndexes = new ArrayList<>();
			for (Atom qa:queryAtoms) {
				res.put(qa,search(facts,qa,freeVariables));
			}
			List<Integer> indexesToPreserve = new ArrayList<>();
			for (int i1 = 0; i1 < queryAtoms.length; i1++) {
				boolean deleteResultSet = true;
				for (int index=0; index < queryAtoms[i1].getTerms().length; index++) {
					for (int j=0; j < freeVariables.length; j++) {
						if (queryAtoms[i1].getTerm(index).equals(freeVariables[j])) {
							deleteResultSet = false;
							indexesToPreserve.add(index); 
						}
					}
				}
				for (int i2 = i1+1; i2 < queryAtoms.length; i2++) {
					for (int i=0; i < queryAtoms[i1].getTerms().length; i++) {
						for (int j=0; j < queryAtoms[i2].getTerms().length; j++) {
							if (queryAtoms[i1].getTerms()[i].equals(queryAtoms[i2].getTerms()[j])) {
								List<Atom> toDelete = new ArrayList<>();
								for (Atom a:res.get(queryAtoms[i1])) {
									boolean delete = true;
									for (Atom b:res.get(queryAtoms[i2])) {
										if (a.getTerm(i).equals(b.getTerm(j))) {
											delete = false;
										}
									}
									if (delete) {
										toDelete.add(a);
									}
								}
								res.get(queryAtoms[i1]).removeAll(toDelete);
							}
						}
					}
				}
				if (deleteResultSet) {
					res.remove(queryAtoms[i1]);
				}
			}
			for (int k = 0; k < queryAtoms.length; k++) {
				if (res.containsKey(queryAtoms[k]))
					for (Atom full:res.get(queryAtoms[k])) {
						List<Term> newTerms = new ArrayList<>();
						for (int j = 0; j<full.getTerms().length; j++) {
							if (indexesToPreserve.contains(j)) {
								newTerms.add(full.getTerms()[j]);
							}
						}
						Atom newAtom = Atom.create(full.getPredicate(), newTerms.toArray(new Term[newTerms.size()]));
						results.add(newAtom);
					}
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
		Collection<Atom> results = new ArrayList<>();
		String name = r.getName();
		if (name == null)
			return results;
		for (Atom fact : this.facts.getFacts()) {
			if (name.equals(fact.getPredicate().getName()))
				results.add(fact);
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Memory database (" + databaseName + "). Contains " + this.facts.getFacts().size() + ".";
	}
}
