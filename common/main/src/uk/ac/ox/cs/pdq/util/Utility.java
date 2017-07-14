package uk.ac.ox.cs.pdq.util;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DataType;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QNames;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

// TODO: Auto-generated Javadoc
/**
 * Provide utility function, that don't fit anywhere else.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @author George Konstantinidis
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
	 * Search.
	 *
	 * @param <T> the generic type
	 * @param collection the collection
	 * @param object the object
	 * @return 		the positions where the input object appears in collection.
	 * 		If object does not appear in source, then an empty list is returned
	 */
	public static <T> List<Integer> search(T[] collection, T object) {
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
	public static List<TypedConstant> toTypedConstants(List<Typed> typed) {
		List<TypedConstant> result = new ArrayList<>();
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
	 * Gets the variables.
	 *
	 * @param formulas the atoms
	 * @return the variables of the input atoms
	 */
	public static List<Variable> getVariables(Collection<? extends Formula> formulas) {
		Set<Variable> result = new LinkedHashSet<>();
		for (Formula formula: formulas) {
			for(Atom atom:formula.getAtoms()) {
				result.addAll(atom.getVariables());
			}
		}
		return Lists.newArrayList(result);
	}

	public static Collection<Constant> getTypedConstants(Collection<Atom> atoms) {
		Collection<Constant> result = new LinkedHashSet<>();
		for (Atom atom:atoms) {
			for (Term term:atom.getTerms()) {
				if (term instanceof Constant && ((Constant) term).isUntypedConstant()) {
					result.add((Constant) term);
				}
			}
		}
		return result;
	}


	public static Set<Constant> getTypedAndUntypedConstants(Atom atom) {
		Set<Constant> result = new LinkedHashSet<>();
		for (Term term:atom.getTerms()) {
			if (!term.isVariable()) {
				result.add((Constant) term);
			}
		}
		return result;
	}

	public static Set<Constant> getUntypedConstants(Atom atom) {
		Set<Constant> result = new LinkedHashSet<>();
		for (Term term:atom.getTerms()) {
			if (term.isUntypedConstant()) {
				result.add((Constant) term);
			}
		}
		return result;
	}

	public static Set<Constant> getUntypedConstants(Collection<Atom> atoms) {
		Set<Constant> result = new LinkedHashSet<>();
		for(Atom atom:atoms) {
			for (Term term:atom.getTerms()) {
				if (term.isUntypedConstant()) {
					result.add((Constant) term);
				}
			}
		}
		return result;
	}

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
	public static Term[] createVariables(Relation r) {
		Term[] result = new Term[r.getArity()];
		for (int i = 0, l = r.getArity(); i < l; i++) 
			result[i] = Variable.getFreshVariable();
		return result;
	}

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
	 * Generates a list of attribute whose name are the name as those of term in
	 * the given predicate, and types match with the predicate attribute types.
	 * @param variables List<? extends Term>
	 * @param type TupleType
	 * @return List<Attribute>
	 */
	public static List<Attribute> variablesToAttributes(List<Variable> variables, TupleType type) {
		Preconditions.checkArgument(variables.size() == type.size());
		List<Attribute> result = new ArrayList<>();
		int i = 0;
		for (Term t : variables) {
			result.add(new Attribute(type.getType(i++), t.toString()));
		}
		return result;
	}

	/**
	 * Converts a list of Term to a list of Typed.
	 *
	 * @param variables List<? extends Term>
	 * @param type TupleType
	 * @return List<Typed>
	 */
	public static List<Typed> variablesToTyped(List<Variable> variables, TupleType type) {
		Preconditions.checkArgument(variables.size() == type.size());
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
		}
		assert result.size() == q.getFreeVariables().size() : "Could not infer type of projected term in the query";
		return result;
	}

	/**
	 * Gets the tuple type.
	 *
	 * @param q the q
	 * @return the tuple type of the input query
	 */
	public static TupleType getTupleType(ConjunctiveQuery q) {
		Type[] result = new Class<?>[q.getFreeVariables().size()];
		boolean assigned = false;
		for (int i = 0, l = result.length; i < l; i++) {
			assigned = false;
			Variable t = q.getFreeVariables().get(i);
			for (Atom f: q.getAtoms()) {
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
	 * Format the given value so as to call the proper type conversion function.
	 *
	 * @param <T> the generic type
	 * @param c TypedConstant<T>
	 * @return a string representation a call to the given target type
	 * conversion function onto the given value;
	 */
	public static <T> String format(TypedConstant c) {
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
		if (target instanceof Class && Number.class.isAssignableFrom((Class<?>) target)) {
			return String.valueOf(o);
		}
		return "'" + o + "'";
	}

	/**
	 * Asserts enabled.
	 */
	public static void assertsEnabled() {
		boolean assertsEnabled = false;
		assert assertsEnabled = true; // Intentional side effect!!!
		if (!assertsEnabled)
			throw new RuntimeException("Assertions must be enabled in the VM");

	}

	public static List<Variable> getVariables(Formula formula) {
		List<Variable> variables = Lists.newArrayList();
		if(formula instanceof Conjunction) {
			variables.addAll(getVariables(((Conjunction)formula).getChildren().get(0)));
			variables.addAll(getVariables(((Conjunction)formula).getChildren().get(1)));
		}
		else if(formula instanceof Disjunction) {
			variables.addAll(getVariables(((Disjunction)formula).getChildren().get(0)));
			variables.addAll(getVariables(((Disjunction)formula).getChildren().get(1)));
		}
		else if(formula instanceof Negation) {
			variables.addAll(getVariables(((Negation)formula).getChildren().get(0)));
		}
		else if(formula instanceof Atom) {
			variables.addAll(((Atom)formula).getVariables());
		}
		else if(formula instanceof Implication) {
			variables.addAll(getVariables(((Implication)formula).getChildren().get(0)));
			variables.addAll(getVariables(((Implication)formula).getChildren().get(1)));
		}
		else if(formula instanceof QuantifiedFormula) {
			variables.addAll(getVariables(((QuantifiedFormula)formula).getChildren().get(0)));
		}
		return variables;
	}

	/**
	 * Let R be a relation of arity n and x_k be its key.
	 * The EGD that captures the EGD dependency is given by
	 * R(x_1,...,x_k,...x_n) ^ R(x_1',...,x_k,...x_n') --> \Wedge_{i \neq k} x_i=x_i'
	 *
	 * @param predicate the signature
	 * @param attributes the attributes
	 * @param keys the keys
	 * @return 		a collection of EGDs for the input relation and keys
	 */
	public static EGD getEGDs(Predicate predicate, Attribute[] attributes, Attribute[] keys) {
		List<Term> leftTerms = Utility.typedToTerms(attributes);
		List<Term> copiedTerms = Lists.newArrayList(leftTerms);
		//Keeps the terms that should be equal
		Map<Term,Term> tobeEqual = com.google.common.collect.Maps.newHashMap();
		int i = 0;
		for(Attribute typed:attributes) {
			if(!keys.contains(typed)) {
				Term term = new Variable(String.valueOf("?" + typed));//TOCOMMENT why are we using a "?" here?
				copiedTerms.set(i, term);
				tobeEqual.put(leftTerms.get(i), term);
			}
			i++;
		}
		Predicate equality = new Predicate(QNames.EQUALITY.toString(), 2, true);
		//Create the constant equality predicates
		List<Formula> equalities = Lists.newArrayList();
		for(java.util.Map.Entry<Term, Term> pair:tobeEqual.entrySet()) {
			equalities.add(new Atom(equality, pair.getKey(), pair.getValue()));
		}
		Formula body =
				Conjunction.of(new Atom(new Predicate(predicate.getName(), leftTerms.size()), leftTerms), 
						new Atom(new Predicate(predicate.getName(), copiedTerms.size()), copiedTerms));
		return new EGD(body, Conjunction.of(equalities));
	}

	/**
	 * TOCOMMENT why plural in the name of the method?
	 * Constructs an EGD for the given relation and key attibutes.
	 *
	 * @param relation the relation
	 * @param keys the key attirbutes
	 * @return the EGD representing the primary key
	 */
	public static EGD getEGDs(Relation relation, Attribute[] keys) {
		return getEGDs(new Predicate(relation.getName(), relation.getArity()), relation.getAttributes(), keys);
	}

	public static List<TypedConstant> getTypedConstants(Formula formula) {
		List<TypedConstant> typedConstants = Lists.newArrayList();
		for(Atom atom:formula.getAtoms()) {
			for(Term term:atom.getTerms()) {
				if(term instanceof TypedConstant) {
					typedConstants.add((TypedConstant)term);
				}
			}
		}
		return typedConstants;
	}

	/**
	 * Gets the constants lying at the input positions.
	 *
	 * @throws IllegalArgumentException if there is a non-constant at one of the input positions
	 * @param positions List<Integer>
	 * @return the List<Constant> at the given positions.
	 */
	public static List<Constant> getTypedAndUntypedConstants(Atom atom, List<Integer> positions) {
		List<Constant> result = new ArrayList<>();
		for(Integer i: positions) {
			if(i < atom.getTerms().size() && !atom.getTerms().get(i).isVariable()) {
				result.add((Constant) atom.getTerms().get(i));
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
		}
		return result;
	}

	/**
	 * TOCOMMENT the next 3 methods are discussed in #42
	 * 
	 * Generate canonical mapping.
	 *
	 * @param body the body
	 * @return 		a mapping of variables of the input conjunction to constants. 
	 * 		A fresh constant is created for each variable of the conjunction. 
	 * 		This method is invoked by the conjunctive query constructor when the constructor is called with empty input canonical mapping.
	 */
	public static Map<Variable, Constant> generateCanonicalMapping(ConjunctiveQuery query) {
		Map<Variable, Constant> canonicalMapping = new LinkedHashMap<>();
		for (Atom p: query.getAtoms()) {
			for (Term t: p.getTerms()) {
				if (t.isVariable()) {
					Constant c = canonicalMapping.get(t);
					if (c == null) {
						c = new UntypedConstant(CanonicalNameGenerator.getName());
						canonicalMapping.put((Variable) t, c);
					}
				}
			}
		}
		return canonicalMapping;
	}

	/**
	 * Make fact.
	 *
	 * @param predicate Predicate
	 * @param tuple Tuple
	 * @return PredicateFormula
	 */
	public static Atom makeFact(Predicate predicate, Tuple tuple) {
		TypedConstant<?>[] terms = new TypedConstant[tuple.size()];
		for (int i = 0, l = tuple.size(); i < l; i++) {
			terms[i++] = new TypedConstant<>(tuple.getValue(i));
		}
		return new Atom(predicate, terms);
	}
	/**
	 * Clusters the input atoms based on their signature
	 * @param atoms
	 * @return
	 */
	public static Map<Predicate, List<Atom>> clusterAtomsWithSamePredicateName(Collection<? extends Atom> atoms) {
		//Cluster the input facts based on their predicate
		Map<Predicate, List<Atom>> clusters = Maps.newHashMap();
		for (Atom atom:atoms) {
			if(clusters.containsKey(atom.getPredicate())) {
				clusters.get(atom.getPredicate()).add(atom);
			}
			else {
				ArrayList<Atom> new_list  = new ArrayList<Atom>();
				new_list.add(atom);
				clusters.put(atom.getPredicate(), new_list);
			}
		}
		return clusters;
	}

	/**
	 * @return an atom with predicate the input relation.
	 */
	public static Atom createAtom(Relation relation) {
		List<Term> variableTerms = new ArrayList<>();
		for (Attribute attribute : relation.getAttributes()) {
			variableTerms.add(new Variable(attribute.getName()));
		}
		return new Atom(relation, variableTerms);
	}

	public static Atom createAtomsWithoutExtraAttribute(Relation relation) {
		List<Term> variableTerms = new ArrayList<>();
		for (Attribute attribute : relation.getAttributes().subList(0, relation.getAttributes().size()-1)) {
			variableTerms.add(new Variable(attribute.getName()));
		}
		return new Atom(relation, variableTerms);
	}


	/**
	 * Gets the term positions.
	 *
	 * @param term Term
	 * @return List<Integer>
	 */
	public List<Integer> getTermPositions(Term term) {
		return Utility.search(this.terms, term);
	}

	/**
	 * Helper printing method.
	 *
	 * @return String
	 */
	private String makeString() {
		StringBuilder result = new StringBuilder();
		result.append(this.name).append('[').append(this.arity).append(']');
		return result.toString().intern();
	}


	/**
	 * Gets the term positions.
	 *
	 * @param term Term
	 * @return List<Integer>
	 */
	public List<Integer> getTermPositions(Term term) {
		return Utility.search(this.terms, term);
	}

	/**
	 * Simple name.
	 *
	 * @param type the type
	 * @return the shortest know name of the given type.
	 */
	public static String simpleName(Type type) {
		if (type instanceof Class) {
			return ((Class<?>) type).getSimpleName();
		}
		if (type instanceof DataType) {
			return ((DataType) type).getName();
		}
		return type.toString();

	}

	/**
	 * Canonical name.
	 *
	 * @param type the type
	 * @return the canonical name of the given type. By default, toString() is
	 * used. If the type is a conventional class, then getCanonicalName is used,
	 * if it is a DataType, then getName() is used.
	 */
	public static String canonicalName(Type type) {
		if (type instanceof Class) {
			return ((Class<?>) type).getCanonicalName();
		}
		if (type instanceof DataType) {
			return ((DataType) type).getName();
		}
		return type.toString();

	}

	/**
	 * Equals.
	 *
	 * @param o1 the o1
	 * @param o2 the o2
	 * @return if o1 and o2 are the same type
	 */
	public static boolean equals(Type o1, Type o2) {
		if (o1 == null) {
			return o2 == null;
		}
		return o1.equals(o2);
	}

	/**
	 * Checks if is numeric.
	 *
	 * @param type the type
	 * @return true if the given is numeric, false otherwise
	 */
	public static boolean isNumeric(Type type) {
		if (type instanceof Class) {
			return Number.class.isAssignableFrom((Class<?>) type);
		}
		return false;

	}

	/**
	 * Attempts to cast the given string to given class. If the class is not
	 * supported, the return object is the same as s.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param o the o
	 * @return a representation of s cast to the given class.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Type type, Object o) {
		if (o == null) {
			return null;
		}
		try {
			if (type instanceof Class) {
				String s = String.valueOf(o);
				Class<?> cl = (Class<?>) type;
				if (Integer.class.equals(cl)) {
					return (T) (s.isEmpty() ? null : cl.cast(Integer.valueOf(s.trim())));
				} else if (Long.class.equals(cl)) {
					return (T) (s.isEmpty() ? null : cl.cast(Long.valueOf(s.trim())));
				} else if (Double.class.equals(cl)) {
					return (T) (s.isEmpty() ? null : cl.cast(Double.valueOf(s.trim())));
				} else if (BigDecimal.class.equals(cl)) {
					return (T) (s.isEmpty() ? null : cl.cast(BigDecimal.valueOf(Double.valueOf(s.trim()))));
				} else if (Boolean.class.equals(cl)) {
					return (T) (s.isEmpty() ? null : cl.cast(Boolean.valueOf(s.trim())));
				} else if (String.class.equals(cl)) {
					return (T) String.valueOf(o);
				} else if (Date.class.equals(cl)) {
					java.util.Date d = (java.util.Date) new SimpleDateFormat("yyyy-MM-dd").parseObject(s.trim());
					return (T) cl.cast(new Date(d.getTime()));
				}
			} else {
				throw new ClassCastException(o + " could not be cast to " + type);
			}
		} catch (NumberFormatException e) {
			log.error(e);
		} catch (ParseException e) {
			log.error(e);
		}
		throw new ClassCastException(o + " could not be cast to " + type);
	}

	public static AccessMethod getAccessMethod(Relation relation, String acceessMethodName) {

	}

	/**
	 * Gets the type of the attributes of the relation.
	 *
	 * @return the relation's type
	 */
	public static TupleType getType(Relation relation) {
		return null;
	}

	/**
	 * @param fact An input fact
	 * @return The list of attributes coming from this fact
	 */
	public static Attribute[] getAttributes(Atom fact) {
		Assert.assertTrue(fact.getPredicate() instanceof Relation);
		return ((Relation) fact.getPredicate()).getAttributes();
	}
	
	/**
	 * Creates a new foreign key object.
	 *
	 * @param dep LinearGuarded
	 */
	public static ForeignKey createForeignKey(LinearGuarded dep) {
		ForeignKey foreignKey = new ForeignKey();
		Atom left = dep.getBody().getAtoms()[0];
		Atom right = dep.getHead().getAtoms()[0];
		Relation leftRel = (Relation) left.getPredicate();
		Relation rightRel = (Relation) right.getPredicate();
		foreignKey.setForeignRelation(rightRel);
		foreignKey.setForeignRelationName(rightRel.getName());
		for (Variable v:CollectionUtils.intersection(left.getVariables(), right.getVariables())) {
			foreignKey.addReference(new Reference(leftRel.getAttribute(left.getTerms().indexOf(v)), rightRel.getAttribute(right.getTerms().indexOf(v))));
		}
	}
	
	

}
