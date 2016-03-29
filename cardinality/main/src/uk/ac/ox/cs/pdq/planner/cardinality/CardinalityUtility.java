/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.cardinality;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


// TODO: Auto-generated Javadoc
/**
 * The Class CardinalityUtility.
 *
 * @author Efthymia Tsamoura
 */
public class CardinalityUtility {

	/** The log. */
	protected static Logger log = Logger.getLogger(CardinalityUtility.class);
	

	/**
	 * Determines whether the attributes corresponding to
	 * constants in keys form a key in the plan of AnnPlan. We decide this by adding
	 * to the fact annotations in AnnPlan a copy of all fact annotations containing some
	 * constant in keys, with any constant c in keys being renamed to a fresh copy c′. We then
	 * chase with the constraints and see if for every c_i \in keys′, c_i = c′ i is derived.
	 *
	 * @param keys 		Input candidate keys
	 * @param configuration the configuration
	 * @param egd 		Runs the EGD chasing algorithm 
	 * @param detector 		Detects homomorphisms during chasing
	 * @param dependencies 		Dependencies to consider during chasing
	 * @return 		true if the input collection of constants is a key for the input annotated plan
	 */
	public static boolean isKey(Collection<Constant> keys, DAGAnnotatedPlan configuration, Chaser egd, HomomorphismDetector detector, Collection<? extends Constraint> dependencies) {
		Preconditions.checkNotNull(keys);
		Preconditions.checkArgument(!keys.isEmpty());

		//Resort to base tables for unary annotated plans
		if(configuration instanceof UnaryAnnotatedPlan) {
			List<Integer> keyPositions = Lists.newArrayList();
			Relation relation = ((UnaryAnnotatedPlan)configuration).getRelation();
			for(Constant key:keys) {
				keyPositions.addAll(Utility.search(((UnaryAnnotatedPlan) configuration).getFact().getTerms(), key));
			}
			if(!relation.getKeyPositions().isEmpty() && keyPositions.containsAll(relation.getKeyPositions())) {
				return true;
			}
		}	

		//Map to each constant in keys a new name
		Map<Constant,Skolem> map = Maps.newHashMap();

		//Create a copy of the facts in the input configuration with the renamed constants
		Collection<Atom> copiedFacts = Sets.newHashSet();
		for(Atom fact:configuration.getOutputFacts()) {
			List<Term> copiedTerms = Lists.newArrayList();
			for(Term originalTerm:fact.getTerms()) {
				if(originalTerm instanceof Skolem && !keys.contains(originalTerm) && map.get(originalTerm) == null) {
					map.put((Constant) originalTerm, new Skolem("?" + ((Skolem) originalTerm).getName()));
				}

				if(map.get(originalTerm) != null) {
					copiedTerms.add(map.get(originalTerm));
				}
				else {
					copiedTerms.add(originalTerm);
				}
			}
			copiedFacts.add(new Atom(fact.getSignature(), copiedTerms));
		}

		DatabaseListState state = new DatabaseListState((DBHomomorphismManager)detector, Sets.newLinkedHashSet(CollectionUtils.union(configuration.getOutputFacts(), copiedFacts)));
		egd.reasonUntilTermination(state, null, dependencies);

		//Clear the database from the copied facts
		((DBHomomorphismManager)detector).deleteFacts(
				Sets.newLinkedHashSet(CollectionUtils.union(copiedFacts, CollectionUtils.removeAll(state.getFacts(), configuration.getOutputFacts()))));
		
		if(state.isFailed()) {
			return false;
		}
		else {
			EqualConstantsClasses classes = state.getConstantClasses();
			for(Entry<Constant, Skolem> entry:map.entrySet()) {
				if(classes.getClass(entry.getKey()) == null ||
					classes.getClass(entry.getValue()) == null ||
					!classes.getClass(entry.getKey()).equals(classes.getClass(entry.getValue()))) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Determines whether there is an “inclusion dependency of left into
	 * right on constants”. It holds if there is a mapping of the non-schema constants
	 * of right to the non-schema constants of left that is the identity on constants
	 * and which preserves all fact annotations of right.
	 *
	 * @param constants the constants
	 * @param source the source
	 * @param target the target
	 * @param detector 		Detects homomorphisms
	 * @return 		true if there is an inclusion dependency of left into right on constants
	 */
	public static boolean hasID(Collection<Constant> constants, DAGAnnotatedPlan source, DAGAnnotatedPlan target, HomomorphismDetector detector) {
		Preconditions.checkNotNull(constants);
		Preconditions.checkArgument(!constants.isEmpty());

		//Create a query from the left's configuration explicit facts
		Map<Constant,Variable> queryMap = Maps.newHashMap();
		int index = 0;
		for(Term term:target.getExportedConstants()) {
			if(term instanceof Skolem && !queryMap.containsKey(term)) {
				queryMap.put((Constant) term, new Variable("x"+index));
			}
			++index;
		}
		
		List<Atom> queryAtoms = Lists.newArrayList();
		for(UnaryAnnotatedPlan unary:target.getUnaryAnnotatedPlans()) {
			Atom fact = unary.getFact();
			List<Term> newTerms = Lists.newArrayList();
			for(Term term:fact.getTerms()) {
				if(term instanceof Skolem) {
					newTerms.add(queryMap.get(term));
				}
				else {
					newTerms.add(term);
				}
			}
			queryAtoms.add(new Atom(fact.getSignature(), newTerms));
		}
		
		
		Map<Variable, Constant> mapConstraint = Maps.newHashMap();
		List<Term> headTerms = Lists.newArrayList();
		for(Constant constant:constants) {
			if(queryMap.get(constant) == null) {
				log.trace("The constant " + constant + " is not a representative of its class. Skip this case."
						+ " " + source + " " + target + " " + constants);
				return false;
			}
			headTerms.add(queryMap.get(constant));
			mapConstraint.put(queryMap.get(constant), constant);
		}
		Atom head = new Atom(new Predicate("Q", constants.size()), headTerms);
		Query<?> query = new ConjunctiveQuery(head, Conjunction.of(queryAtoms));

		//Create homomorphism constraints that preserve the input constants
		if(source.getState() instanceof DatabaseChaseState) {
			((DatabaseChaseState)source.getState()).setManager((DBHomomorphismManager) detector);
		}
		
		HomomorphismConstraint[] c = new HomomorphismConstraint[2];
		c[0] = HomomorphismConstraint.createMapConstraint(mapConstraint);
		c[1] = HomomorphismConstraint.createTopKConstraint(1);
		boolean hasID = !source.getState().getMatches(query, c).isEmpty();
		
		log.trace("hasID " + source + "-->" + target + " = " + hasID);
		return hasID;
	}

}
