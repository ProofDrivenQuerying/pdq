package uk.ac.ox.cs.pdq.planner.reasoning.chase.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class Utility {
	
	/**
	 *
	 * @param axioms
	 * 		Input accessibility axioms
	 * @param signatureToSchemaFact
	 * 		Maps each schema signature (relation) to its chase facts
	 * @return
	 * 		pairs of accessibility axioms to chase facts.
	 */
	public List<Pair<AccessibilityAxiom, Collection<Predicate>>> groupByBinding(
			Collection<AccessibilityAxiom> axioms, 
			Multimap<Signature, Predicate> signatureToSchemaFact) {
		List<Pair<AccessibilityAxiom, Collection<Predicate>>> ret = new ArrayList<>();
		for(AccessibilityAxiom axiom: axioms) {
			//For each axiom, we get the relevant facts
			//and group them based on the constants of their input positions
			Collection<Predicate> facts = signatureToSchemaFact.get(axiom.getBaseRelation());
			Multimap<Collection<Term>, Predicate> groupsOfFacts = LinkedHashMultimap.create();
			for(Predicate fact: facts) {
				groupsOfFacts.put(fact.getTerms(axiom.getAccessMethod().getZeroBasedInputs()), fact);
			}
			Iterator<Collection<Term>> keyIterator = groupsOfFacts.keySet().iterator();
			while(keyIterator.hasNext()) {
				Collection<Predicate> collection = Sets.newLinkedHashSet(groupsOfFacts.get(keyIterator.next()));
				ret.add(Pair.of(axiom, collection));
			}
		}
		return ret;
	}

	//TODO Update the firing history of the accessed and accessible facts
	/**
	 * @param schema
	 * @param axiom
	 * @param facts
	 * @return
	 * 		the corresponding accessed, accessible and inferred accessible facts for each fact in the input collection
	 */
	public Collection<Predicate> generateFacts(AccessibleSchema schema, 
			AccessibilityAxiom axiom, 
			Collection<Predicate> facts,
			Collection<String> inferred,
			Collection<Predicate> derivedInferred,
			FiringGraph graph
			) {
		Collection<Predicate> createdFacts = new LinkedHashSet<>();
		for(Predicate fact:facts) {			
			Predicate accessedFact = new Predicate((Relation) fact.getSignature(), fact.getTerms());
			createdFacts.add(accessedFact);
			
			Predicate infAccFact = new Predicate(schema.getInferredAccessibleRelation((Relation) fact.getSignature()), fact.getTerms());
			createdFacts.add(infAccFact);
			inferred.add(infAccFact.toString());
			derivedInferred.add(infAccFact);
			graph.put(axiom, accessedFact, infAccFact);
			
			for(Term term:fact.getTerms()) {
				createdFacts.add(new Predicate(schema.getAccessibleRelation(), term));
			}
		}
		return createdFacts;
	}

	public static Collection<String> inferInferred(Collection<Predicate> facts) {
		Collection<String> inferred = new LinkedHashSet<>();
		for(Predicate fact:facts) {
			if (fact.getSignature() instanceof InferredAccessibleRelation) {
				inferred.add(fact.toString());
			}
		}
		return inferred;
	}
	
	public static Collection<Predicate> inferDerivedInferred() {
		return new LinkedHashSet<>();
	}

	public static Multimap<Signature, Predicate> inferSignatureGroups(Collection<Predicate> facts) {
		Multimap<Signature, Predicate> signatureGroups = LinkedHashMultimap.create();
		for(Predicate fact:facts) {
			if (!(fact.getSignature() instanceof AccessibleRelation) && 
					!(fact.getSignature() instanceof InferredAccessibleRelation)) {
				signatureGroups.put(fact.getSignature(), fact);
			}
		}
		return signatureGroups;
	}

	public static Multimap<Term,Predicate> inferAccessibleTerms(Collection<Predicate> facts) {
		Multimap<Term,Predicate> accessibleTerms = LinkedHashMultimap.create();
		for(Predicate fact:facts) {
			if (fact.getSignature() instanceof AccessibleRelation) {
				accessibleTerms.put(fact.getTerm(0), fact);
			}
		}
		return accessibleTerms;
	}
	
	public static Collection<Term> inferTerms(Collection<Predicate> facts) {
		Collection<Term> terms = new LinkedHashSet<>();
		for(Predicate fact:facts) {
			terms.addAll(fact.getTerms());
		}
		return terms;
	}

}
