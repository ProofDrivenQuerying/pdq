package uk.ac.ox.cs.pdq.util;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

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
	public static Term[] typedToTerms(Attribute[] typed) {
		Term[] result = new Term[typed.length];
		for (int index = 0; index < typed.length; ++index) {
			result[index] = Variable.create(String.valueOf(typed[index].getName()));
		}
		return result;
	}

	/**
	 * Gets the variables.
	 *
	 * @param formulas the atoms
	 * @return the variables of the input atoms
	 */
	public static List<Variable> getVariables(Formula[] formulas) {
		Set<Variable> result = new LinkedHashSet<>();
		for (Formula formula: formulas) {
			for(Atom atom:formula.getAtoms()) 
				result.addAll(Arrays.asList(atom.getVariables()));
		}
		return new ArrayList<>(result);
	}
	
	/** Same as above but works with lists
	 * @param formulas
	 * @return
	 */
	public static List<Variable> getVariables(List<Atom> formulas) {
		return getVariables(formulas.toArray(new Formula[formulas.size()]));
	}

	public static Collection<Constant> getTypedConstants(Collection<Atom> atoms) {
		Collection<Constant> result = new LinkedHashSet<>();
		for (Atom atom:atoms) {
			for (Term term:atom.getTerms()) {
				if (term instanceof Constant && ((Constant) term).isUntypedConstant()) 
					result.add((Constant) term);
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
				if (term.isUntypedConstant()) 
					result.add((Constant) term);
			}
		}
		return result;
	}

	public static Collection<Term> getTerms(Atom[] atoms) {
		Set<Term> result = new LinkedHashSet<>();
		for (Atom atom:atoms) {
			for (Term term:atom.getTerms()) 
				result.add(term);
		}
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
			variables.addAll(getVariables(((Conjunction)formula).getChildren()[0]));
			variables.addAll(getVariables(((Conjunction)formula).getChildren()[1]));
		}
		else if(formula instanceof Disjunction) {
			variables.addAll(getVariables(((Disjunction)formula).getChildren()[0]));
			variables.addAll(getVariables(((Disjunction)formula).getChildren()[1]));
		}
		else if(formula instanceof Negation) {
			variables.addAll(getVariables(((Negation)formula).getChildren()[0]));
		}
		else if(formula instanceof Atom) {
			variables.addAll(Arrays.asList(((Atom)formula).getVariables()));
		}
		else if(formula instanceof Implication) {
			variables.addAll(getVariables(((Implication)formula).getChildren()[0]));
			variables.addAll(getVariables(((Implication)formula).getChildren()[0]));
		}
		else if(formula instanceof QuantifiedFormula) {
			variables.addAll(getVariables(((QuantifiedFormula)formula).getChildren()[0]));
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
	public static EGD getEGD(Predicate predicate, Attribute[] attributes, Attribute[] keys) {
		Term[] leftTerms = Utility.typedToTerms(attributes);
		Term[] copiedTerms = leftTerms.clone();
		//Keeps the terms that should be equal
		Map<Term,Term> tobeEqual = com.google.common.collect.Maps.newHashMap();
		int i = 0;
		for(Attribute typed:attributes) {
			if(!Arrays.asList(keys).contains(typed)) {
				// the ? is a naming convention, could be anything.
				Term term = Variable.create(String.valueOf("?" + typed));
				copiedTerms[i] = term;
				tobeEqual.put(leftTerms[i], term);
			}
			i++;
		}
		Predicate equality = Predicate.create("equality", 2, true);
		//Create the constant equality predicates
		int index = 0;
		Atom[] equalities = new Atom[tobeEqual.entrySet().size()];
		for(java.util.Map.Entry<Term, Term> pair:tobeEqual.entrySet()) 
			equalities[index++] = Atom.create(equality, pair.getKey(), pair.getValue());
		Atom body[] = new Atom[]{Atom.create(Predicate.create(predicate.getName(), leftTerms.length), leftTerms), 
						Atom.create(Predicate.create(predicate.getName(), copiedTerms.length), copiedTerms)};
		return EGD.create(body, equalities,true);
	}

	/**
	 * Constructs an EGD for the given relation and key attibutes.
	 *
	 * @param relation the relation
	 * @param keys the key attirbutes
	 * @return the EGD representing the primary key
	 */
	public static EGD getEGD(Relation relation, Attribute[] keys) {
		return getEGD(Predicate.create(relation.getName(), relation.getArity()), relation.getAttributes(), keys);
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
	public static List<Constant> getTypedAndUntypedConstants(Atom atom, Integer[] positions) {
		List<Constant> result = new ArrayList<>();
		for(Integer i: positions) {
			if(i < atom.getNumberOfTerms() && !atom.getTerm(i).isVariable()) 
				result.add((Constant) atom.getTerm(i));
			else 
				throw new java.lang.IllegalArgumentException();
		}
		return result;
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
	 * Simple name.
	 *
	 * @param type the type
	 * @return the shortest know name of the given type.
	 */
	public static String simpleName(Type type) {
		if (type instanceof Class) {
			return ((Class<?>) type).getSimpleName();
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

	/**
	 * @param fact An input fact
	 * @return The list of attributes coming from this fact
	 */
	public static Attribute[] getAttributes(Atom fact, Schema schema) {
		
		return schema.getRelation(fact.getPredicate().getName()).getAttributes();
	}

//	/**
//	 * Creates a new foreign key object.
//	 *
//	 * @param dependency LinearGuarded
//	 */
//	public static ForeignKey createForeignKey(LinearGuarded dependency) {
//		ForeignKey foreignKey = new ForeignKey();
//		Atom left = dependency.getBodyAtom(0);
//		Atom right = dependency.getHeadAtom(0);
//		Relation leftRel = (Relation) left.getPredicate();
//		Relation rightRel = (Relation) right.getPredicate();
//		foreignKey.setForeignRelation(rightRel);
//		foreignKey.setForeignRelationName(rightRel.getName());
//		for (Variable v:CollectionUtils.intersection(Arrays.asList(left.getVariables()), Arrays.asList(right.getVariables()))) {
//			foreignKey.addReference(new Reference(leftRel.getAttribute(Arrays.asList(left.getTerms()).indexOf(v)), rightRel.getAttribute(Arrays.asList(right.getTerms()).indexOf(v))));
//		}
//		return foreignKey;
//	}
	
	/**
	 * Gets the term positions.
	 *
	 * @param term Term
	 * @return List<Integer>
	 */
	public static List<Integer> getTermPositions(Atom atom, Term term) {
		return Utility.search(atom.getTerms(), term);
	}

	/**
	 * Gets only the terms at the specified input positions.
	 *
	 * @param positions List<Integer>
	 * @return the Set<Term> at the given positions.
	 */
	public static Set<Term> getTerms(Atom atom, Integer[] positions) {
		Set<Term> t = new LinkedHashSet<>();
		for(Integer index: positions) 
			t.add(atom.getTerm(index));
		return t;
	}
}
