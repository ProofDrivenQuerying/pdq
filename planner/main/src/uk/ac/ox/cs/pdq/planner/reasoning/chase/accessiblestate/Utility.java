package uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.planner.util.FiringGraph;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

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
	 * @param signatureToSchemaFact 		Maps each schema signature (relation) to its chase facts
	 * @return 		pairs of accessibility axioms to chase facts.
	 */
	public List<Pair<AccessibilityAxiom, Collection<Atom>>> groupByBinding(
			Collection<AccessibilityAxiom> axioms, 
			Multimap<Predicate, Atom> signatureToSchemaFact) {
		List<Pair<AccessibilityAxiom, Collection<Atom>>> ret = new ArrayList<>();
		for(AccessibilityAxiom axiom: axioms) {
			//For each axiom, we get the relevant facts
			//and group them based on the constants of their input positions
			Collection<Atom> facts = signatureToSchemaFact.get(axiom.getBaseRelation());
			Multimap<Collection<Term>, Atom> groupsOfFacts = LinkedHashMultimap.create();
			for(Atom fact: facts) {
				groupsOfFacts.put(fact.getTerms(axiom.getAccessMethod().getZeroBasedInputs()), fact);
			}
			Iterator<Collection<Term>> keyIterator = groupsOfFacts.keySet().iterator();
			while(keyIterator.hasNext()) {
				Collection<Atom> collection = Sets.newLinkedHashSet(groupsOfFacts.get(keyIterator.next()));
				ret.add(Pair.of(axiom, collection));
			}
		}
		return ret;
	}

	//TODO Update the firing history of the accessed and accessible facts
	/**
	 * Generate facts.
	 *
	 * @param schema the schema
	 * @param axiom the axiom
	 * @param facts the facts
	 * @param inferred the inferred
	 * @param derivedInferred the derived inferred
	 * @param graph the graph
	 * @return 		the corresponding accessed, accessible and inferred accessible facts for each fact in the input collection
	 */
	public Collection<Atom> generateFacts(AccessibleSchema schema, 
			AccessibilityAxiom axiom, 
			Collection<Atom> facts,
			Collection<String> inferred,
			Collection<Atom> derivedInferred,
			FiringGraph graph
			) {
		Collection<Atom> createdFacts = new LinkedHashSet<>();
		for(Atom fact:facts) {			
			Atom accessedFact = new Atom((Relation) fact.getPredicate(), fact.getTerms());
			createdFacts.add(accessedFact);
			
			Atom infAccFact = new Atom(schema.getInferredAccessibleRelation((Relation) fact.getPredicate()), fact.getTerms());
			createdFacts.add(infAccFact);
			inferred.add(infAccFact.toString());
			derivedInferred.add(infAccFact);
			graph.put(axiom, accessedFact, infAccFact);
			
			for(Term term:fact.getTerms()) {
				createdFacts.add(new Atom(schema.getAccessibleRelation(), term));
			}
		}
		return createdFacts;
	}

	/**
	 * Infer inferred.
	 *
	 * @param facts the facts
	 * @return the collection
	 */
	public static Collection<String> inferInferred(Collection<Atom> facts) {
		Collection<String> inferred = new LinkedHashSet<>();
		for(Atom fact:facts) {
			if (fact.getPredicate() instanceof InferredAccessibleRelation) {
				inferred.add(fact.toString());
			}
		}
		return inferred;
	}
	
	/**
	 * Infer derived inferred.
	 *
	 * @return the collection
	 */
	public static Collection<Atom> inferDerivedInferred() {
		return new LinkedHashSet<>();
	}

	/**
	 * Infer signature groups.
	 *
	 * @param facts the facts
	 * @return the multimap
	 */
	public static Multimap<Predicate, Atom> inferSignatureGroups(Collection<Atom> facts) {
		Multimap<Predicate, Atom> signatureGroups = LinkedHashMultimap.create();
		for(Atom fact:facts) {
			if (!(fact.getPredicate() instanceof AccessibleRelation) && 
					!(fact.getPredicate() instanceof InferredAccessibleRelation)) {
				signatureGroups.put(fact.getPredicate(), fact);
			}
		}
		return signatureGroups;
	}

	/**
	 * Infer accessible terms.
	 *
	 * @param facts the facts
	 * @return the multimap
	 */
	public static Multimap<Term,Atom> inferAccessibleTerms(Collection<Atom> facts) {
		Multimap<Term,Atom> accessibleTerms = LinkedHashMultimap.create();
		for(Atom fact:facts) {
			if (fact.getPredicate() instanceof AccessibleRelation) {
				accessibleTerms.put(fact.getTerm(0), fact);
			}
		}
		return accessibleTerms;
	}
	
	/**
	 * Infer terms.
	 *
	 * @param facts the facts
	 * @return the collection
	 */
	public static Collection<Term> inferTerms(Collection<Atom> facts) {
		Collection<Term> terms = new LinkedHashSet<>();
		for(Atom fact:facts) {
			terms.addAll(fact.getTerms());
		}
		return terms;
	}

}
