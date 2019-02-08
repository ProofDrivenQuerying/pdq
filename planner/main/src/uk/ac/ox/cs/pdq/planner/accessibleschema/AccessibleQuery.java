package uk.ac.ox.cs.pdq.planner.accessibleschema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Change a conjunctive query Q to its "accessible version" infaccQ
 * @author gabor
 *
 */
public class AccessibleQuery extends ConjunctiveQuery {
	private static final long serialVersionUID = 1L;
	private static Map<ConjunctiveQuery, AccessibleQuery> cache = new HashMap<>();
	
	private AccessibleQuery(Variable[] freeVariables, Atom[] children) {
		super(freeVariables, children);
	}
	
	/**
	 * Change a conjunctive query Q to its "accessible version" infaccQ
	 *
	 * 
	 * @param query the query
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#createAccessibleQuery(AccessibleSchema)
	 */
	public static AccessibleQuery createAccessibleQuery(ConjunctiveQuery query) {
		if (cache.containsKey(query))
			return cache.get(query);
		Atom[] atoms = getAccessibleAtoms(query);
		AccessibleQuery acq = new AccessibleQuery(query.getFreeVariables(), atoms);
		cache.put(query, acq);
		return acq;
	}
	
	private static Atom[] getAccessibleAtoms(ConjunctiveQuery query) {
		Atom[] atoms = new Atom[query.getNumberOfAtoms()];
		for (int atomIndex = 0; atomIndex < query.getNumberOfAtoms(); ++atomIndex) {
			Atom queryAtom = query.getAtom(atomIndex);
			Predicate predicate = null;
				predicate = Predicate.create(AccessibleSchema.inferredAccessiblePrefix + queryAtom.getPredicate().getName(), queryAtom.getPredicate().getArity());
			atoms[atomIndex] = Atom.create(predicate, queryAtom.getTerms());
		}
		return atoms;
	}
	
	public static Map<Variable, Constant> generateCanonicalMappingForQuery(ConjunctiveQuery query) {
		Map<Variable, Constant> canonicalMapping = new LinkedHashMap<>();
		List<Variable> freeVariables = Arrays.asList(query.getFreeVariables());
		for (Atom atom : query.getBody().getAtoms()) {
			for (Term t : atom.getTerms()) {
				if (t.isVariable()) {
					Constant c = canonicalMapping.get(t);
					if (c == null && !freeVariables.contains(t)) {
						c = UntypedConstant.create("v_" + ((Variable)t).getSymbol() + query.getId());
						canonicalMapping.put((Variable) t, c);
					} else if (c==null) {
						// c is a free variable we want to preserve its name in the new constant.
						c = UntypedConstant.create("fv_" + ((Variable)t).getSymbol() + query.getId());
						canonicalMapping.put((Variable) t, c);
					}
				}
			}
		}
		return canonicalMapping;
	}

	/**
	 * Group by binding.
	 *
	 * @param axioms 		Input accessibility axioms
	 * @param atomsMap 		Maps each schema signature (relation) to its chase facts
	 * @return 		pairs of accessibility axioms to chase facts.
	 */
	public static List<Pair<AccessibilityAxiom, Collection<Atom>>> groupFactsByAccessMethods(
			AccessibilityAxiom[] axioms, 
			Multimap<Predicate, Atom> atomsMap) {
		List<Pair<AccessibilityAxiom, Collection<Atom>>> ret = new ArrayList<>();
		for(AccessibilityAxiom axiom: axioms) {
			//For each axiom, we get the relevant facts
			//and group them based on the constants of their input positions
			Collection<Atom> facts = null;
			for (Predicate r :atomsMap.keySet()) {
				if (r.getName().equals(axiom.getBaseRelation().getName())) 
					facts = atomsMap.get(r);
			}
			if (facts!=null) {
				Multimap<Collection<Term>, Atom> groupsOfFacts = LinkedHashMultimap.create();
				for(Atom fact: facts)  {
					groupsOfFacts.put(uk.ac.ox.cs.pdq.util.Utility.getTerms(fact,axiom.getAccessMethod().getInputs()), fact);
				}
				Iterator<Collection<Term>> keyIterator = groupsOfFacts.keySet().iterator();
				while(keyIterator.hasNext()) {
					Collection<Atom> collection = Sets.newLinkedHashSet(groupsOfFacts.get(keyIterator.next()));
					ret.add(Pair.of(axiom, collection));
				}
			}
		}
		return ret;
	}

	public static Collection<Atom> getInferredAccessibleAtoms(Collection<Atom> facts) {
		Collection<Atom> inferred = new LinkedHashSet<>();
		for(Atom fact:facts) {
			if (fact.getPredicate().getName().startsWith(AccessibleSchema.inferredAccessiblePrefix)) 
				inferred.add(fact);
		}
		return inferred;
	}

	/**
	 *
	 * @param facts the facts
	 * @return the multimap
	 */
	public static Multimap<Predicate, Atom> createAtomsMap(Collection<Atom> facts) {
		Multimap<Predicate, Atom> atomsMap = LinkedHashMultimap.create();
		for(Atom fact:facts) {
			if (!(fact.getPredicate().getName().equals(AccessibleSchema.accessibleRelation.getName())) && 
					!(fact.getPredicate().getName().startsWith(AccessibleSchema.inferredAccessiblePrefix))) {
				atomsMap.put(fact.getPredicate(), fact);
			}
		}
		return atomsMap;
	}

	/**
	 * All Terms sorted out from the input facts where the fact is from the Accessible table. Simply put these are the accessible terms.
	 *
	 * @param facts the facts
	 * @return the multimap
	 */
	public static Multimap<Term,Atom> getAllTermsAppearingInAccessibleFacts(Collection<Atom> facts) {
		Multimap<Term,Atom> accessibleTerms = LinkedHashMultimap.create();
		for(Atom fact:facts) {
			if (fact.getPredicate().getName().equals(AccessibleSchema.accessibleRelation.getName())) 
				accessibleTerms.put(fact.getTerm(0), fact);
		}
		return accessibleTerms;
	}

}
