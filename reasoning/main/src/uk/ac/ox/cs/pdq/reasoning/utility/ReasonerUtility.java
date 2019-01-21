package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * The Class ReasonerUtility.
 *
 * @author Efthymia Tsamoura
 */
public class ReasonerUtility {

	/** The log. */
	protected static Logger log = Logger.getLogger(ReasonerUtility.class);
	
	/**
	 * 
	 * @param query
	 * @param dependencies
	 * @return
	 * 		the dependencies that are relevant to the input query.
	 * 		When chasing, one needs to maintain only the relevant relations and only fire the relevant dependencies.
	 * 
	 *  	The algorithm works as follows:
	 *  	Let RelevantRelations := relations in Q //initialize
			While (RelevantRelations changes) do
			{
				Let TGDs' := all tau in Sigma such that some relation R in RelevantRElations is in the head)
				//TGDs that can change some relevant relation
				Let Rels' := all relations in the body of a TGD in TGDs'
				RelevantRelations += Rels' 
			}
			Let RelevantTGDs:= all TGDs such that some relation in the head are in Rels
	 */
	public Collection<? extends Dependency> findRelevant(ConjunctiveQuery query, Collection<? extends Dependency> dependencies) {
		Collection<Dependency> relevantDependencies = Sets.newLinkedHashSet();
		Collection<Predicate> relevantPredicates = Sets.newLinkedHashSet();
		for(Atom atom:query.getAtoms()) {
			relevantPredicates.add(atom.getPredicate());
		}
		Collection<? extends Dependency> dependenciesCopy = Sets.newLinkedHashSet(dependencies);
		boolean change = false;
		do {
			Iterator<? extends Dependency> iterator = dependenciesCopy.iterator();
			while(iterator.hasNext()) {
				Dependency dependency = iterator.next();
				for(Atom headAtom:dependency.getHead().getAtoms()) {
					if(relevantPredicates.contains(headAtom.getPredicate())) {
						for(int bodyAtomIndex = 0; bodyAtomIndex < dependency.getNumberOfBodyAtoms(); ++bodyAtomIndex) 
							relevantPredicates.add(dependency.getBodyAtom(bodyAtomIndex).getPredicate());
						//Remove from the dependency all the irrelevant atoms
						List<Atom> relevantHeadAtoms = new ArrayList<>();
						for(int headAtomIndex = 0; headAtomIndex < dependency.getNumberOfHeadAtoms(); ++headAtomIndex) {
							if(relevantPredicates.contains(dependency.getHeadAtom(headAtomIndex).getPredicate())) 
								relevantHeadAtoms.add(dependency.getHeadAtom(headAtomIndex));
						}
						
						List<Atom> relevantBodyAtoms = new ArrayList<>();
						for(int bodyAtomIndex = 0; bodyAtomIndex < dependency.getNumberOfBodyAtoms(); ++bodyAtomIndex) {
							if(relevantPredicates.contains(dependency.getBodyAtom(bodyAtomIndex).getPredicate())) 
								relevantBodyAtoms.add(dependency.getBodyAtom(bodyAtomIndex));
						}
						
						Dependency relevantDependency = Dependency.create(relevantBodyAtoms.toArray(new Atom[relevantBodyAtoms.size()]), 
								relevantHeadAtoms.toArray(new Atom[relevantHeadAtoms.size()]));
						
						relevantDependencies.add(relevantDependency);
						iterator.remove();
						change = true;
						break;
					}
				}
			}
			change = false;
		}while(change);
		return null;
	}
	
	/**
	 * Extracts all constants from the terms of the given facts.
	 * 
	 * @param facts
	 * @return a map of each constant to the atom and the position inside this atom
	 *         where it appears. An exception is thrown when there is an equality in
	 *         the input
	 */
	public static Multimap<Constant, Atom> createdConstantsMap(Collection<Atom> facts) {
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		for (Atom fact : facts) {
			Preconditions.checkArgument(!fact.isEquality());
			for (Term term : fact.getTerms())
				constantsToAtoms.put((Constant) term, fact);
		}
		return constantsToAtoms;
	}

	/**
	 * 
	 * @param dependencies
	 * @return
	 * 		true if the input set of dependencies contains EGDs
	 */
	public static boolean checkEGDs(Dependency[] dependencies) {
		for(Dependency dependency:dependencies) {
			if(dependency instanceof EGD) {
				return true;
			}
		}
		return false;
	}
	
}
