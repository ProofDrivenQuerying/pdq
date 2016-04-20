package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.ParallelEGDChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseListState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Utility;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * The Class ReasonerUtility.
 *
 * @author Efthymia Tsamoura
 */
public class ReasonerUtility {

	/** The log. */
	protected static Logger log = Logger.getLogger(ReasonerUtility.class);

	/**
	 * Checks if is key.
	 *
	 * @param table the table
	 * @param candidateKeys the candidate keys
	 * @param constraints the constraints
	 * @param egdChaser the egd chaser
	 * @param detector the detector
	 * @return 		true if the input set of attributes is a key of the input table
	 */
	public boolean isKey(Table table, List<Attribute> candidateKeys, Collection<? extends Constraint<?,?>> constraints, ParallelEGDChaser egdChaser, DatabaseHomomorphismManager detector) {
		//Create the set of EGDs that correspond to the given table and keys
		EGD egd = EGD.getEGDs(new Predicate(table.getName(),table.getHeader().size()), (List<Attribute>) table.getHeader(), candidateKeys);
		
		Query<?> lquery = new ConjunctiveQuery(new Atom(new Predicate("Q", egd.getFree().size()), egd.getFree()), egd.getLeft());
		
		Query<?> rquery = new ConjunctiveQuery(new Atom(new Predicate("Q", egd.getRight().getTerms().size()), egd.getRight().getTerms()), 
				Conjunction.of(egd.getRight().getAtoms()));
		
		//Creates a chase state that consists of the canonical database of the input query.
		ListState state = new DatabaseChaseListState(lquery, detector);
		return egdChaser.entails(state, lquery.getFreeToCanonical(), rquery, constraints);
	}
	
	
	/**
	 * Checks if is open trigger.
	 *
	 * @param match the match
	 * @param s the s
	 * @return 		true if the constraint kept in the input match has been already fired with the input homomorphism
	 */
	public boolean isOpenTrigger(Match match, ChaseState s) {
		Map<Variable, Constant> mapping = match.getMapping();
		Constraint constraint = (Constraint) match.getQuery();
		Constraint grounded = constraint.fire(mapping, true);
		return !s.getFiringGraph().isFired(constraint, grounded.getLeft().getAtoms());
	}
	
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
	public Collection<? extends Constraint<?,?>> findRelevant(Query<?> query, Collection<? extends Constraint<?,?>> dependencies) {
		Collection<Constraint<?,?>> relevantDependencies = Sets.newLinkedHashSet();
		Collection<Predicate> relevantPredicates = Sets.newLinkedHashSet();
		for(Atom atom:query.getBody().getAtoms()) {
			relevantPredicates.add(atom.getPredicate());
		}
		Collection<? extends Constraint<?,?>> dependenciesCopy = Sets.newLinkedHashSet(dependencies);
		boolean change = false;
		do {
			Iterator<? extends Constraint<?,?>> iterator = dependenciesCopy.iterator();
			while(iterator.hasNext()) {
				Constraint<?,?> dependency = iterator.next();
				for(Atom headAtom:dependency.getHead().getAtoms()) {
					if(relevantPredicates.contains(headAtom.getPredicate())) {
						for(Atom atom:dependency.getBody().getAtoms()) {
							relevantPredicates.add(atom.getPredicate());
						}
						//Remove from the dependency all the irrelevant atoms
						Constraint<?, ?> dep = dependency.clone();
						Iterator<Atom> it = null;
						it = dep.getHead().getAtoms().iterator();
						while(it.hasNext()) {
							if(!relevantPredicates.contains(it.next().getPredicate())) {
								it.remove();
							}
						}
						
						it = dep.getBody().getAtoms().iterator();
						while(it.hasNext()) {
							if(!relevantPredicates.contains(it.next().getPredicate())) {
								it.remove();
							}
						}
						relevantDependencies.add(dep);
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
	
}
