package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

import com.google.common.base.Preconditions;


// TODO: Auto-generated Javadoc
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
	public <S extends ChaseState> void reasonUntilTermination(S instance, Collection<? extends Constraint> dependencies) {
		Preconditions.checkArgument(instance instanceof ListState);
		int rounds = 0;
		boolean appliedStep = true;
		while (rounds < this.k && appliedStep) {
			appliedStep = false;
			List<Match> matches = instance.getMatches(dependencies, HomomorphismProperty.createActiveTriggerProperty());
			if(!matches.isEmpty()) {
				appliedStep = true;
			}
			++rounds;
		}
	}

}
