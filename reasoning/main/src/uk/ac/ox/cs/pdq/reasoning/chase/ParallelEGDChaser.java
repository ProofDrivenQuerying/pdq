package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseListState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.reasoning.utility.DefaultParallelEGDChaseDependencyAssessor;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;
import uk.ac.ox.cs.pdq.reasoning.utility.ParallelEGDChaseDependencyAssessor;
import uk.ac.ox.cs.pdq.reasoning.utility.ParallelEGDChaseDependencyAssessor.EGDROUND;
import uk.ac.ox.cs.pdq.reasoning.utility.ReasonerUtility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


// TODO: Auto-generated Javadoc
/**
 * Runs EGD chase using parallel chase steps.
 * (From modern dependency theory notes)
 * 
 * A trigger for and EGD \delta = \sigma --> x_i = x_j in I is again a homomorphism h in
	\sigma into I. A trigger is active if it does not extend to a homomorphism h0 into I.
	Given trigger h
	for \delta in I, a chase pre-step marks the pair h(x_i) and h(x_j) as equal. Formally,
	it appends the pair h(x_i), h(x_j) to a set of pairs MarkedEqual.
	An EGD parallel chase step on instance I for a set of constraints C is performed
	as follows.
	i. A chase pre-step is performed for every constraint \delta in C and every active
	trigger h in I.
	ii. The resulting set of marked pairs is closed under reflexivity and transitivity
	to get an equivalence relation.
	iii. If we try to equate two different schema constants, then the chase fails. 
	The facts that are generated during chasing are stored in a list.
	
 * @author Efthymia Tsamoura
 *
 */
public class ParallelEGDChaser extends Chaser {


	/**
	 * Constructor for EGDChaser.
	 * @param statistics StatisticsCollector
	 */
	public ParallelEGDChaser(
			StatisticsCollector statistics) {
		super(statistics);
	}

	/**
	 * Chases the input state until termination.
	 * The EGDs and the TGDs are applied in rounds, i.e., during even round we apply parallel EGD chase steps,
	 * while during odd rounds we apply parallel TGD chase steps.  
	 *
	 * @param <S> the generic type
	 * @param instance the instance
	 * @param target the target
	 * @param dependencies the dependencies
	 */
	@Override
	public <S extends ChaseState> void reasonUntilTermination(S instance,  Collection<? extends Constraint> dependencies) {
		Preconditions.checkArgument(instance instanceof ListState);
		ParallelEGDChaseDependencyAssessor accessor = new DefaultParallelEGDChaseDependencyAssessor(dependencies);
		
		Collection<TGD> tgds = Sets.newHashSet();
		Collection<EGD> egds = Sets.newHashSet();
		for(Constraint constraint:dependencies) {
			if(constraint instanceof EGD) {
				egds.add((EGD) constraint);
			}
			else if(constraint instanceof TGD) {
				tgds.add((TGD) constraint);
			}
			else {
				throw new java.lang.IllegalArgumentException("Unsupported constraint type");
			}
		}

		int step = 0;
		//True if at the end of the internal for loop at least one dependency has been fired
		boolean appliedOddStep = false;
		boolean appliedEvenStep = false;
		do {
			++step;
			//Find all active triggers
			Collection<? extends Constraint> d = step % 2 == 0 ? accessor.getDependencies(instance, EGDROUND.TGD):accessor.getDependencies(instance, EGDROUND.EGD);
			List<Match> activeTriggers = instance.getMatches(d, HomomorphismProperty.createActiveTriggerProperty());
			boolean succeeds = instance.chaseStep(activeTriggers);
			if(!succeeds) {
				break;
			}
			if(succeeds && !activeTriggers.isEmpty()) {
				if(step % 2 == 0) {
					appliedEvenStep = true;
				}
				else {
					appliedOddStep = true;
				}
			}
			
			if(activeTriggers.isEmpty()) {
				if(step % 2 == 0) {
					appliedEvenStep = false;
				}
				else {
					appliedOddStep = false;
				}
			}
			
		} while (!(appliedOddStep == false && appliedEvenStep == false && step > 1));
	}


	/**
	 * Entails.
	 *
	 * @param <S> the generic type
	 * @param instance the instance
	 * @param free 		Mapping of query's free variables to constants
	 * @param target the target
	 * @param constraints the constraints
	 * @return 		true if the input instance with the given set of free variables and constraints implies the target query.
	 */
	@Override
	public <S extends ChaseState> boolean entails(S instance, Map<Variable, Constant> free, Query<?> target,
			Collection<? extends Constraint<?,?>> constraints) {
		Collection<? extends Constraint<?, ?>> relevantDependencies = new ReasonerUtility().findRelevant(target, constraints);
		this.reasonUntilTermination(instance, relevantDependencies);
		if(!instance.isFailed()) {
			HomomorphismProperty[] c = {
					HomomorphismProperty.createTopKProperty(1),
					HomomorphismProperty.createMapProperty(free)};

			return !instance.getMatches(target,c).isEmpty(); 
		}
		return false;
	}

	/**
	 * Entails.
	 *
	 * @param <S> the generic type
	 * @param source the source
	 * @param target the target
	 * @param constraints the constraints
	 * @return 		true if the source query entails the target query
	 */
	@Override
	public boolean entails(Query<?> source, Query<?> target,
			Collection<? extends Constraint<?,?>> constraints, HomomorphismDetector detector) {
		Collection<? extends Constraint<?, ?>> relevantDependencies = new ReasonerUtility().findRelevant(target, constraints);
		DatabaseChaseListState instance = new DatabaseChaseListState(source, (DatabaseHomomorphismManager)detector);
		this.reasonUntilTermination(instance, relevantDependencies);
		if(!instance.isFailed()) {
			HomomorphismProperty[] c = {
					HomomorphismProperty.createTopKProperty(1)};
			return !instance.getMatches(target,c).isEmpty(); 
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.Chaser#clone()
	 */
	@Override
	public ParallelEGDChaser clone() {
		return new ParallelEGDChaser(this.statistics);
	}

}
