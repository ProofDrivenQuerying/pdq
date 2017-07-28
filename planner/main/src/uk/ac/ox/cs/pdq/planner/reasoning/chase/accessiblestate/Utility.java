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
public class Utility {
	
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
			Collection<Atom> facts = atomsMap.get(axiom.getBaseRelation());
			Multimap<Collection<Term>, Atom> groupsOfFacts = LinkedHashMultimap.create();
			for(Atom fact: facts) 
				groupsOfFacts.put(uk.ac.ox.cs.pdq.util.Utility.getTerms(fact,axiom.getAccessMethod().getZeroBasedInputPositions()), fact);
			Iterator<Collection<Term>> keyIterator = groupsOfFacts.keySet().iterator();
			while(keyIterator.hasNext()) {
				Collection<Atom> collection = Sets.newLinkedHashSet(groupsOfFacts.get(keyIterator.next()));
				ret.add(Pair.of(axiom, collection));
			}
		}
		return ret;
	}

//	//TODO Update the firing history of the accessed and accessible facts
//	/**
//	 * Generate facts.
//	 *
//	 * @param schema the schema
//	 * @param axiom the axiom
//	 * @param facts the facts
//	 * @param inferred the inferred
//	 * @param derivedInferred the derived inferred
//	 * @param graph the graph
//	 * @return 		the corresponding accessed, accessible and inferred accessible facts for each fact in the input collection
//	 */
//	public static Collection<Atom> getFactsProducedAfterFiringAccessibilityAxiom(
//			AccessibilityAxiom axiom, 
//			Collection<Atom> facts,
//			Collection<Atom> inferred,
//			Collection<Atom> derivedInferred,
//			FiringGraph graph
//			) {
//		Collection<Atom> createdFacts = new LinkedHashSet<>();
//		for(Atom fact:facts) {			
//			Atom accessedFact = Atom.create(fact.getPredicate(), fact.getTerms());
//			createdFacts.add(accessedFact);
//			Atom inferredAccessibleFact = Atom.create(Predicate.create(AccessibleSchema.inferredAccessiblePrefix + fact.getPredicate().getName(), fact.getNumberOfTerms()), fact.getTerms());
//			createdFacts.add(inferredAccessibleFact);
//			inferred.add(inferredAccessibleFact);
//			derivedInferred.add(inferredAccessibleFact);
//			if(graph != null) 
//				graph.put(axiom, accessedFact, inferredAccessibleFact);
//			
//			for(Term term:fact.getTerms()) 
//				createdFacts.add(Atom.create(AccessibleSchema.accessibleRelation, term));
//		}
//		return createdFacts;
//	}

	/**
	 * Infer inferred.
	 *
	 * @param facts the facts
	 * @return the collection
	 */
	public static Collection<Atom> getInferredAtoms(Collection<Atom> facts) {
		Collection<Atom> inferred = new LinkedHashSet<>();
		for(Atom fact:facts) {
			if (fact.getPredicate().getName().startsWith(AccessibleSchema.inferredAccessiblePrefix)) 
				inferred.add(fact);
		}
		return inferred;
	}
	
	/**
	 * Infer signature groups.
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
	 * Infer accessible terms.
	 *
	 * @param facts the facts
	 * @return the multimap
	 */
	public static Multimap<Term,Atom> getAccessibleTerms(Collection<Atom> facts) {
		Multimap<Term,Atom> accessibleTerms = LinkedHashMultimap.create();
		for(Atom fact:facts) {
			if (fact.getPredicate().equals(AccessibleSchema.accessibleRelation)) 
				accessibleTerms.put(fact.getTerm(0), fact);
		}
		return accessibleTerms;
	}
	
//	/**
//	 * Infer terms.
//	 *
//	 * @param facts the facts
//	 * @return the collection
//	 */
//	public static Collection<Term> getTerms(Collection<Atom> facts) {
//		Collection<Term> terms = new LinkedHashSet<>();
//		for(Atom fact:facts) {
//			terms.addAll(fact.getTerms());
//		}
//		return terms;
//	}

}
