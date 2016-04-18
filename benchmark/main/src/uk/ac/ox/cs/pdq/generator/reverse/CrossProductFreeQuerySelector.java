package uk.ac.ox.cs.pdq.generator.reverse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * A QuerySelector that accept only conjunctive queries without cross products.
 * 
 * @author Julien Leblay
 */
public class CrossProductFreeQuerySelector implements QuerySelector {

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean accept(ConjunctiveQuery q) {
		for (Conjunction<Atom> body : this.enumerateConjunctions(q.getBody())) {
			if (body.size() > 1) {
				Multimap<Term, Atom> clusters = LinkedHashMultimap.create();
				for (Atom pred: body) {
					for (Term t: pred.getTerms()) {
						clusters.put(t, pred);
					}
				}
				List<Set<Atom>> localClusters2 = new LinkedList<>();
				for (Term t: clusters.keySet()) {
					localClusters2.add(Sets.newHashSet(clusters.get(t)));
				}
				return Utility.connectedComponents(localClusters2).size() == 1;
			}
		}
		return true;
	}

	/**
	 * Gets a collection of conjunction of atoms found in the given formula.
	 *
	 * @param formula the formula
	 * @return the collection
	 */
	private Collection<Conjunction<Atom>> enumerateConjunctions(Formula formula) {
		Preconditions.checkArgument(formula != null);
		if (formula instanceof Conjunction) {
			List<Conjunction<Atom>> result = new LinkedList<>();
			List<Atom> localConj = new LinkedList<>();
			for (Formula subFormula: ((Conjunction<Formula>) formula)) {
				if (subFormula instanceof Atom) {
					localConj.add((Atom) subFormula);
				} else {
					result.addAll(this.enumerateConjunctions(subFormula));
				}
			}
			result.add(Conjunction.of(localConj));
			return result;
		}
		throw new IllegalStateException();
	}
}
