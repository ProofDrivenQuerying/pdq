package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.List;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TriggerProperty;
import uk.ac.ox.cs.pdq.reasoning.utility.ReasonerUtility;


/**
 * Runs the chase for k chase rounds.
 * The difference with the restricted chase algorithm is
 * that this implementation performs k chase rounds prior to stopping.
 * 
 * @author Efthymia Tsamoura
 *
 */
public class KTerminationChaser extends RestrictedChaser {

	/** Factor of number of rounds to chase for before stop the chase. */
	private final Integer k;


	/**
	 * Instantiates a new k termination chaser.
	 *
	 * @param statistics the statistics
	 * @param k 		Factor of number of rounds to chase for before stop the chase.
	 */
	public KTerminationChaser(
			StatisticsCollector statistics,
			int k) {
		super(statistics);
		Preconditions.checkArgument(k >= 0);
		this.k = k;
	}

	/**
	 * Reason until termination.
	 *
	 * @param <S> the generic type
	 * @param instance the instance
	 * @param dependencies Collection<? extends Constraint>
	 */
	@Override
	public <S extends ChaseInstance> void reasonUntilTermination(S instance, Dependency[] dependencies) {
		Preconditions.checkArgument(instance instanceof ChaseInstance);
		Preconditions.checkArgument(!ReasonerUtility.checkEGDs(dependencies), "KTerminationChaser is not allowed with EGDs");
		int rounds = 0;
		boolean appliedStep = true;
		while (rounds < this.k && appliedStep) {
			appliedStep = false;
			List<Match> matches = instance.getTriggers(dependencies, TriggerProperty.ACTIVE);
			if(!matches.isEmpty()) {
				instance.chaseStep(matches);
				appliedStep = true;
			}
			++rounds;
		}
	}

}
