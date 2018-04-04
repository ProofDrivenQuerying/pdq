package uk.ac.ox.cs.pdq.databasemanagement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQueryWithInequality;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * Optimises a query by rearranging its atoms to join the smallest tabels first.
 * 
 * @author Gabor
 */
public class InternalDatabaseManagerQueryOptimiser {

	/**
	 * @param cq
	 *            input cq
	 * @param tableSizeStats
	 *            size of each table in the database
	 * @return cq with size - ordered tables.
	 */
	public static ConjunctiveQuery optimise(ConjunctiveQuery cq, Map<String, Integer> tableSizeStats) {
		if (cq.getAtoms().length <= 2) {
			// nothing to optimise
			return cq;
		}
		List<Atom> orderedAtoms = new LinkedList<>();

		if (cq.getAtoms().length < 4) {
			orderedAtoms = smallestFirst(Arrays.asList(cq.getAtoms()), tableSizeStats);
		} else {
			orderedAtoms = smallestFirst(Arrays.asList(cq.getAtoms()), tableSizeStats);
			orderedAtoms = mostConnectedfirstAtoms(orderedAtoms);
		}

		return createConjunctiveQuery(orderedAtoms, cq);
	}

	/** Measures the number of connections with other atoms, and orders the list to make sure we won't create large cross joins without conditions. 
	 * @param atoms
	 * @return
	 */
	private static List<Atom> mostConnectedfirstAtoms(List<Atom> atoms) {
		Map<Atom,Integer> connections = new HashMap<>();
		for (Atom a:atoms) {
			int connection = 0;
			for (Term t: a.getTerms()) {
				if (t.isVariable()) {
					for (Atom b:atoms) {
						if (!a.equals(b)) {
							for (Term tb: b.getTerms()) {
								if (t.equals(tb)) {
									connection++;
								}
							}
						}
					}
				}
			}
			connections.put(a, connection);
		}
		
		boolean changed = true;
		while(changed) {
			changed = false;
			for (int i = 1; i < atoms.size(); i++) {
				if (connections.get(atoms.get(i-1)) < connections.get(atoms.get(i))) {
					Atom tmp = atoms.get(i-1);
					atoms.set(i-1, atoms.get(i));
					atoms.set(i, tmp);
					changed = true;
				}
			}
		}
		return atoms;
	}

	/**
	 * Simplest way to optimise queries is to make sure we compute joins on the
	 * smallest tables first.
	 * 
	 * @param cq
	 * @param tableSizeStats
	 * @return
	 */
	private static List<Atom> smallestFirst(List<Atom> atoms, Map<String, Integer> tableSizeStats) {
		List<Atom> orderedAtoms = new LinkedList<>();
		int min = 0;
		while (orderedAtoms.size() < atoms.size()) {
			int nextMin = Integer.MAX_VALUE;
			for (Atom a : atoms) {
				int size = 0;
				if (tableSizeStats.containsKey(a.getPredicate().getName())) {
					if (a.getPredicate().getName().equals("EQUALITY")) {
						size = Integer.MAX_VALUE;
					} else {
						size = tableSizeStats.get(a.getPredicate().getName());
					}
				}
				if (size == min) {
					orderedAtoms.add(a);
				}
				if (size < nextMin && size > min) {
					nextMin = size;
				}			}
			min = nextMin;
		}
		return orderedAtoms;
	}
	
	/** Converts list of atoms to conjunctive query. uses the original query to copy inequalities. 
	 * @param atoms
	 * @param cq
	 * @return
	 */
	private static ConjunctiveQuery createConjunctiveQuery(List<Atom> atoms, ConjunctiveQuery cq) {
		ConjunctiveQuery newCQ = null;
		Atom atomsArray [] = atoms.toArray(new Atom[atoms.size()]);
		if ((cq instanceof ConjunctiveQueryWithInequality)) {
			newCQ = ConjunctiveQueryWithInequality.create(cq.getFreeVariables(), atomsArray,
					((ConjunctiveQueryWithInequality) cq).getInequalities());
		} else {
			newCQ = ConjunctiveQuery.create(cq.getFreeVariables(), atomsArray);
		}
		return newCQ;
	}
	
}
