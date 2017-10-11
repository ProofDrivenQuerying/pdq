package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TriggerProperty;
import uk.ac.ox.cs.pdq.reasoning.utility.DefaultParallelEGDChaseDependencyAssessor;
import uk.ac.ox.cs.pdq.reasoning.utility.ParallelEGDChaseDependencyAssessor;
import uk.ac.ox.cs.pdq.reasoning.utility.ParallelEGDChaseDependencyAssessor.EGDROUND;


/**
 * Runs EGD and TGD chase using parallel chase steps.
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
	public <S extends ChaseInstance> void reasonUntilTermination(S instance,  Dependency[] dependencies) {
		Preconditions.checkArgument(instance instanceof ChaseInstance);
		ParallelEGDChaseDependencyAssessor accessor = new DefaultParallelEGDChaseDependencyAssessor(dependencies);

		Collection<TGD> tgds = Sets.newHashSet();
		Collection<EGD> egds = Sets.newHashSet();
		for(Dependency constraint:dependencies) {
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
			Dependency[] d = step % 2 == 0 ? accessor.getDependencies(instance, EGDROUND.TGD):accessor.getDependencies(instance, EGDROUND.EGD);
			List<Match> activeTriggers = instance.getTriggers(d, TriggerProperty.ACTIVE);
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

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.Chaser#clone()
	 */
	@Override
	public ParallelEGDChaser clone() {
		return new ParallelEGDChaser(this.statistics);
	}

}
