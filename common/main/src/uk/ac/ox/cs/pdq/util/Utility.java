package uk.ac.ox.cs.pdq.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Provide utility function, that don't fit anywhere else.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class Utility {

	/**  The logger. */
	public static Logger log = Logger.getLogger(Utility.class);

	/**
	 * Search.
	 *
	 * @param <T> the generic type
	 * @param collection the collection
	 * @param object the object
	 * @return 		the positions where the input object appears in collection.
	 * 		If object does not appear in source, then an empty list is returned
	 */
	public static <T> List<Integer> search(Collection<? extends T> collection, T object) {
		List<Integer> result = new ArrayList<>();
		int index = 0;
		for (T obj : collection) {
			if (obj.equals(object)) {
				result.add(index);
			}
			index++;
		}
		return result;
	}

	/**
	 * Contains element.
	 *
	 * @param <T> the generic type
	 * @param source the source
	 * @param target the target
	 * @return 		true if source contains at least on element of target
	 */
	public static <T> boolean containsElement(Collection<T> source, Collection<T> target) {
		for (T s: source) {
			if (target.contains(s)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Removes the duplicates.
	 *
	 * @param <T> the generic type
	 * @param l the l
	 * @return a duplicate-free list
	 */
	public static <T> Collection<T> removeDuplicates(Collection<T> l) {
		return new LinkedHashSet<>(l);
	}

	/**
	 * Extract.
	 *
	 * @param <T> the generic type
	 * @param l the l
	 * @param p a list of positions
	 * @return a list of T made of all the items in l appearing at positions in p
	 */
	public static <T> List<T> extract(List<T> l, List<Integer> p) {
		Preconditions.checkArgument(l != null);
		Preconditions.checkArgument(p != null);
		List<T> result = new ArrayList<>(p.size());
		for (Integer position: p) {
			Preconditions.checkState(position >= 0 && position < l.size());
			result.add(l.get(position));
		}
		return result;
	}

	/**
	 * Typed to terms.
	 *
	 * @param typed the typed
	 * @return List<Term>
	 */
	public static List<Term> typedToTerms(Collection<? extends Typed> typed) {
		List<Term> result = new ArrayList<>(typed.size());
		for (Typed o : typed) {
			result.add(typedToTerm(o));
		}
		return result;
	}

	/**
	 * Typed to term.
	 *
	 * @param typed the typed
	 * @return Term
	 */
	public static Term typedToTerm(Typed typed) {
		if (typed instanceof TypedConstant) {
			return (TypedConstant) typed;
		}
		return new Variable(String.valueOf(typed));
	}

	/**
	 * To typed constants.
	 *
	 * @param typed the typed
	 * @return List<TypedConstant<?>>
	 */
	public static List<TypedConstant<?>> toTypedConstants(List<Typed> typed) {
		List<TypedConstant<?>> result = new ArrayList<>();
		for (Typed t: typed) {
			if (t instanceof TypedConstant) {
				result.add((TypedConstant) t);
			} else {
				result.add(new TypedConstant(Types.cast(t.getType(), String.valueOf(t))));
			}
		}
		return result;
	}

	/**
	 * Converts a list of Typed to a list of VariableTerm.
	 *
	 * @param typed Collection<? extends Typed>
	 * @return List<Variable>
	 */
	public static List<Variable> typedToVariable(Collection<? extends Typed> typed) {
		List<Variable> result = new ArrayList<>();
		for (Typed a : typed) {
			result.add(new Variable(a.toString()));
		}
		return result;
	}

	/**
	 * Gets the variables.
	 *
	 * @param atoms the atoms
	 * @return the variables of the input atoms
	 */
	public static List<Variable> getVariables(Collection<? extends Atom> atoms) {
		Set<Variable> result = new LinkedHashSet<>();
		for (Atom atom: atoms) {
			for (Term term:atom.getTerms()) {
				if (term instanceof Variable) {
					result.add((Variable) term);
				}
			}
		}
		return Lists.newArrayList(result);
	}

	/**
	 * Gets the constants.
	 *
	 * @param atoms the atoms
	 * @return the constants of the input atoms
	 */
	public static Collection<Constant> getConstants(Collection<? extends Atom> atoms) {
		Collection<Constant> result = new LinkedHashSet<>();
		for (Atom atom:atoms) {
			for (Term term:atom.getTerms()) {
				if (term instanceof Constant && ((Constant) term).isSkolem()) {
					result.add((Constant) term);
				}
			}
		}
		return result;
	}

	/**
	 * Gets the constants.
	 *
	 * @param atom the atom
	 * @return the constants of the input atom
	 */
	public static Set<Constant> getConstants(Atom atom) {
		Set<Constant> result = new LinkedHashSet<>();
		for (Term term:atom.getTerms()) {
			if (!term.isVariable()) {
				result.add((Constant) term);
			}
		}
		return result;
	}
	
	/**
	 * Gets the non schema constants.
	 *
	 * @param atom the atom
	 * @return the non schema constants
	 */
	public static Set<Constant> getNonSchemaConstants(Atom atom) {
		Set<Constant> result = new LinkedHashSet<>();
		for (Term term:atom.getTerms()) {
			if (!(term instanceof TypedConstant)) {
				result.add((Constant) term);
			}
		}
		return result;
	}

	/**
	 * Gets the terms.
	 *
	 * @param atoms Iterable<PredicateFormula>
	 * @return the terms of the input atom
	 */
	public static Collection<Term> getTerms(Iterable<Atom> atoms) {
		Set<Term> result = new LinkedHashSet<>();
		for (Atom atom:atoms) {
			for (Term term:atom.getTerms()) {
				result.add(term);
			}
		}
		return result;
	}

	/**
	 * Generates a list of terms matching the attributes of the input relation.
	 *
	 * @param r Relation
	 * @return List<Term>
	 */
	public static List<Term> generateVariables(Relation r) {
		List<Term> result = new ArrayList<>();
		for (int i = 0, l = r.getArity(); i < l; i++) {
			result.add(Variable.getFreshVariable());
		}
		return result;
	}

	/**
	 * Generates a list of attribute whose name are the name as those of term in
	 * the given predicate, and types match with the predicate attribute types.
	 * @param terms List<? extends Term>
	 * @param type TupleType
	 * @return List<Attribute>
	 */
	public static List<Attribute> termsToAttributes(List<? extends Term> terms, TupleType type) {
		Preconditions.checkArgument(terms.size() == type.size());
		List<Attribute> result = new ArrayList<>();
		int i = 0;
		for (Term t : terms) {
			result.add(new Attribute(type.getType(i++), t.toString()));
		}
		return result;
	}

	/**
	 * Converts a list of Term to a list of Typed.
	 *
	 * @param terms List<? extends Term>
	 * @param type TupleType
	 * @return List<Typed>
	 */
	public static List<Typed> termsToTyped(List<? extends Term> terms, TupleType type) {
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
		if (t.isVariable() || t.isSkolem()) {
			return new Attribute(type, String.valueOf(t));
		} else if (t instanceof TypedConstant) {
			return (TypedConstant) t;
		} else {
			throw new IllegalStateException("Unknown typed object: " + t);
		}
	}

	/**
	 * Generates a list of terms matching the attributes of the input relation.
	 *
	 * @param q ConjunctiveQuery
	 * @return List<Attribute>
	 */
	public static List<Attribute> termsToAttributes(ConjunctiveQuery q) {
		List<Attribute> result = new ArrayList<>();
		for (Term t : q.getFree()) {
			if (t instanceof Variable) {
				boolean found = false;
				for (Atom p : q.getBody()) {
					Predicate s = p.getPredicate();
					if (s instanceof Relation) {
						Relation r = (Relation) s;
						int i = 0;
						for (Term v : p.getTerms()) {
							if (v.equals(t)) {
								result.add(new Attribute(r.getAttribute(i).getType(), t.toString()));
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
			} else {
				result.add(new Attribute(String.class, t.toString()));
			}
		}
		assert result.size() == q.getFree().size() : "Could not infer type of projected term in the query";
		return result;
	}

	/**
	 * Generates a list of terms matching the list of input attributes.
	 *
	 * @param attributes the attributes
	 * @return List<Attribute>
	 */
	public static List<Attribute> canonicalAttributes(List<Attribute> attributes) {
		List<Attribute> result = new ArrayList<>();
		for (int index = 0, l = attributes.size(); index < l; ++index) {
			result.add(new Attribute(attributes.get(index).getClass(), "x" + index));
		}
		return result;
	}

	/**
	 * To strings.
	 *
	 * @param atoms the atoms
	 * @return the string representations of the input atoms
	 */
	public static Collection<String> toStrings(Collection<? extends Atom> atoms) {
		Set<String> strings = new LinkedHashSet<>();
		for(Atom atom: atoms) {
			strings.add(atom.toString());
		}
		return strings;
	}

	/**
	 * Gets the tuple type.
	 *
	 * @param q the q
	 * @return the tuple type of the input query
	 */
	public static TupleType getTupleType(Query<?> q) {
		List<Term> headTerms = q.getHead().getTerms();
		Type[] result = new Class<?>[headTerms.size()];
		boolean assigned = false;
		for (int i = 0, l = result.length; i < l; i++) {
			assigned = false;
			Term t = headTerms.get(i);
			if (t instanceof TypedConstant) {
				result[i] = ((TypedConstant) t).getType();
				continue;
			}
			for (Atom f: q.getBody().getAtoms()) {
				Predicate s = f.getPredicate();
				if (s instanceof Relation) {
					List<Integer> pos = f.getTermPositions(t);
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


	/**
	 * Mean dist.
	 *
	 * @param random Random
	 * @param mean double
	 * @param min double
	 * @param max double
	 * @return double
	 */
	public static double meanDist(Random random, double mean, double min, double max) {
		if (random.nextBoolean()) {
			return min + random.nextDouble() * (mean - min);
		}
		return mean + random.nextDouble() * (max - mean);
	}

	/**
	 * Retain.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param map Map<K,V>
	 * @param keys Collection<K>
	 * @return Map<K,V>
	 */
	public static <K,V> Map<K,V> retain(Map<K,V> map, Collection<K> keys) {
		Map<K,V> ret = new HashMap<>();
		for(K key: keys) {
			ret.put(key, map.get(key));
		}
		return ret;
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
	 * Connected components2.
	 *
	 * @param <T> the generic type
	 * @param clusters the clusters
	 * @return 		a partition of the given clusters, such that all predicates in the
	 *      each component are connected, and no predicates part of distinct
	 *      component are connected.
	 */
	public static <T> List<Set<T>> connectedComponents2(List<Set<T>> clusters) {
		List<Set<T>> result = new LinkedList<>();
		if (clusters.isEmpty()) {
			return result;
		}
		Set<T> first = clusters.get(0);
		if (clusters.size() > 1) {
			List<Set<T>> rest = connectedComponents2(clusters.subList(1, clusters.size()));
			for (Set<T> s : rest) {
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
	 * Format the given value so as to call the proper type conversion function.
	 *
	 * @param <T> the generic type
	 * @param c TypedConstant<T>
	 * @return a string representation a call to the given target type
	 * conversion function onto the given value;
	 */
	public static <T> String format(TypedConstant<T> c) {
		return format(c, c.getType());
	}

	/**
	 * Format the given value so as to call the proper type conversion function.
	 *
	 * @param <T> the generic type
	 * @param o Object
	 * @param target Class<T>
	 * @return a string representation a call to the given target type
	 * conversion function onto the given value;
	 */
	public static <T> String format(Object o, Type target) {
		if (target instanceof Class && Number.class.isAssignableFrom((Class) target)) {
			return String.valueOf(o);
		}
		return "'" + o + "'";
	}
	
	/**
	 * Asserts enabled.
	 */
	public static void assertsEnabled()
	{
		boolean assertsEnabled = false;
		assert assertsEnabled = true; // Intentional side effect!!!
		if (!assertsEnabled)
			throw new RuntimeException("Assertions must be enabled in the VM");

	}

}
