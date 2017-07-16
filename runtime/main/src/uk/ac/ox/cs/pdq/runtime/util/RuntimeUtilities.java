package uk.ac.ox.cs.pdq.runtime.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

public class RuntimeUtilities {

	/**
	 * Converts a list of Term to a list of Typed.
	 *
	 * @param terms List<? extends Term>
	 * @param type TupleType
	 * @return List<Typed>
	 */
	public static List<Typed> termsToTyped(List<Term> terms, TupleType type) {
		Preconditions.checkArgument(terms.size() == type.size());
		List<Typed> result = new ArrayList<>();
		int i = 0;
		for (Term t: terms) {
			result.add(termToTyped(t, type.getType(i)));
			i++;
		}
		return result;
	}
	
	/**
	 * Converts a Term to a Typed.
	 *
	 * @param t Term
	 * @param type Class<?>
	 * @return Typed
	 */
	public static Typed termToTyped(Term t, Type type) {
		if (t.isVariable() || t.isUntypedConstant()) {
			return new Attribute(type, String.valueOf(t));
		} else if (t instanceof TypedConstant) {
			return (TypedConstant) t;
		} else {
			throw new IllegalStateException("Unknown typed object: " + t);
		}
	}
	
	/**
	 * Connected components.
	 *
	 * @param clusters the clusters
	 * @return 		a partition of the given clusters, such that all predicates in the
	 *      each component are connected, and no predicates part of distinct
	 *      component are connected.
	 */
	public static List<Set<Atom>> connectedComponents(List<Set<Atom>> clusters) {
		List<Set<Atom>> result = new LinkedList<>();
		if (clusters.isEmpty()) {
			return result;
		}
		Set<Atom> first = clusters.get(0);
		if (clusters.size() > 1) {
			List<Set<Atom>> rest = connectedComponents(clusters.subList(1, clusters.size()));
			for (Set<Atom> s : rest) {
				if (!Collections.disjoint(first, s)) {
					first.addAll(s);
				} else {
					result.add(s);
				}
			}
		}
		result.add(first);
		return result;
	}
}
