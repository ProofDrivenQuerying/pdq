package uk.ac.ox.cs.pdq.databasemanagement;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQueryWithInequality;

public class InternalDatabaseManagerQueryOptimiser {

	public static ConjunctiveQuery optimise(ConjunctiveQuery cq, Map<String, Integer> tableSizeStats) {
		if (cq.getAtoms().length <= 2) {
			// nothing to optimise
			return cq;
		}
		Conjunction newConjunction = null; 		
		
		newConjunction = smallestFirst(cq,tableSizeStats);
		
		
		ConjunctiveQuery newCQ = null;
		if ((cq instanceof ConjunctiveQueryWithInequality)) {
			newCQ = ConjunctiveQueryWithInequality.create(cq.getFreeVariables(), newConjunction, ((ConjunctiveQueryWithInequality)cq).getInequalities());
		} else {
			newCQ = ConjunctiveQuery.create(cq.getFreeVariables(), newConjunction);
		}
		return newCQ;
	}

	public static boolean isQueryPointingToEmptyTable(ConjunctiveQuery cq, Map<String, Integer> tableSizeStats) {
		for (Atom a: cq.getAtoms()) {
			int size = 0; 
			if (tableSizeStats.containsKey(a.getPredicate().getName()))
				size = tableSizeStats.get(a.getPredicate().getName());
			if (size==0)
				return true;
		}
		return false;
	}
	private static Conjunction smallestFirst(ConjunctiveQuery cq, Map<String, Integer> tableSizeStats) {
		List<Atom> orderedAtoms = new LinkedList<>();
		int min = 0;
		while (orderedAtoms.size() <cq.getAtoms().length) {
			int nextMin = Integer.MAX_VALUE;
			for (Atom a: cq.getAtoms()) {
				int size = 0; 
				if (tableSizeStats.containsKey(a.getPredicate().getName()))
					size = tableSizeStats.get(a.getPredicate().getName());
//				try {
//					size = tableSizeStats.get(a.getPredicate().getName());
//				}catch(Exception e) {
//					e.printStackTrace();
//				}
				if (size==min) {
					orderedAtoms.add(a);
				}
				if (size<nextMin && size > min) {
					nextMin = size;
				}
			}
			min = nextMin;
		}
		Conjunction ret = Conjunction.create(orderedAtoms.get(0),orderedAtoms.get(1));
		for (int i = 2; i < orderedAtoms.size(); i++) {
			ret = Conjunction.create(orderedAtoms.get(i),ret);
		}
		return ret;
	}
}
