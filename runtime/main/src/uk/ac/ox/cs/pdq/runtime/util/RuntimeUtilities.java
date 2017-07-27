package uk.ac.ox.cs.pdq.runtime.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

public class RuntimeUtilities {

	/**
	 * Converts a list of Term to a list of Typed.
	 *
	 * @param terms List<? extends Term>
	 * @param type TupleType
	 * @return List<Typed>
	 */
	public static List<Typed> termsToTyped(Term[] terms, TupleType type) {
		Preconditions.checkArgument(terms.length == type.size());
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
		if (t.isVariable() || t.isUntypedConstant()) 
			return Attribute.create(type, String.valueOf(t));
		else if (t instanceof TypedConstant) 
			return (TypedConstant) t;
		else 
			throw new IllegalStateException("Unknown typed object: " + t);
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
	
	/**
	 * Converts a list of Term to a list of Typed.
	 *
	 * @param variables List<? extends Term>
	 * @param type TupleType
	 * @return List<Typed>
	 */
	public static List<Typed> variablesToTyped(Variable[] variables, TupleType type) {
		Preconditions.checkArgument(variables.length == type.size());
		List<Typed> result = new ArrayList<>();
		int i = 0;
		for (Term t: variables) {
			result.add(termToTyped(t, type.getType(i)));
			i++;
		}
		return result;
	}

	/**
	 * Generates a list of terms matching the attributes of the input relation.
	 *
	 * @param q ConjunctiveQuery
	 * @return List<Attribute>
	 */
	public static List<Attribute> termsToAttributes(ConjunctiveQuery q) {
		List<Attribute> result = new ArrayList<>();
		for (Variable t:q.getFreeVariables()) {
			boolean found = false;
			for (Atom p:q.getAtoms()) {
				Predicate s = p.getPredicate();
				if (s instanceof Relation) {
					Relation r = (Relation) s;
					int i = 0;
					for (Term v : p.getTerms()) {
						if (v.equals(t)) {
							result.add(Attribute.create(r.getAttribute(i).getType(), t.toString()));
							found = true;
							break;
						}
						i++;
					}
				}
				if (found) {
					break;
				}
			}
		}
		assert result.size() == q.getFreeVariables().length : "Could not infer type of projected term in the query";
		return result;
	}
	
	/**
	 * Gets the tuple type.
	 *
	 * @param q the q
	 * @return the tuple type of the input query
	 */
	public static TupleType getTupleType(ConjunctiveQuery q) {
		Type[] result = new Class<?>[q.getFreeVariables().length];
		boolean assigned = false;
		for (int i = 0, l = result.length; i < l; i++) {
			assigned = false;
			Variable t = q.getFreeVariables()[i];
			for (Atom f: q.getAtoms()) {
				Predicate s = f.getPredicate();
				if (s instanceof Relation) {
					List<Integer> pos = Utility.getTermPositions(f, t);
					if (!pos.isEmpty()) {
						result[i] = ((Relation) s).getAttribute(pos.get(0)).getType();
						assigned = true;
						break;
					}
				}
			}
			if (!assigned) {
				throw new IllegalStateException("Could not infer query type.");
			}
		}
		return TupleType.DefaultFactory.create(result);
	}
}
