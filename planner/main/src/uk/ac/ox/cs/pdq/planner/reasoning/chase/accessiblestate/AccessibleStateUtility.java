package uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;

// TODO: Auto-generated Javadoc
/**
 * The Class Utility.
 *
 * @author Efthymia Tsamoura
 */
public class AccessibleStateUtility {
	
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

	/**
	 * TOCOMMENT: FIX THIS
	 *
	 * @param facts the facts
	 * @return the collection
	 */
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
			if (!(fact.getPredicate().equals(AccessibleSchema.accessibleRelation)) && 
					!(fact.getPredicate().getName().startsWith(AccessibleSchema.inferredAccessiblePrefix))) {
				atomsMap.put(fact.getPredicate(), fact);
			}
		}
		return atomsMap;
	}

	/**
 *TOCOMMENT: WHAT ARE THESE!!!!
	 *
	 * @param facts the facts
	 * @return the multimap
	 */
	public static Multimap<Term,Atom> getAllTermsAppearingInAccessibleFacts(Collection<Atom> facts) {
		Multimap<Term,Atom> accessibleTerms = LinkedHashMultimap.create();
		for(Atom fact:facts) {
			if (fact.getPredicate().equals(AccessibleSchema.accessibleRelation)) 
				accessibleTerms.put(fact.getTerm(0), fact);
		}
		return accessibleTerms;
	}

}
