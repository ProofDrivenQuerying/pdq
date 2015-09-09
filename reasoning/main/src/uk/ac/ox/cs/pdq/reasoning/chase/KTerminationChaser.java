package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;

import com.google.common.base.Preconditions;


/**
 * A non-blocking chase algorithm which runs for k chase steps.
 * The difference between this algorithm and the restricted chase one is
 * that this algorithm performs k chase rounds prior to stopping.
 *
 *
 * @author Efthymia Tsamoura
 *
 */
public class KTerminationChaser extends RestrictedChaser {

	/** Factor of number of rounds to chase for before stop the chase. */
	private final Integer k;


	/**
	 * 
	 * @param statistics
	 * @param k
	 * 		Factor of number of rounds to chase for before stop the chase.
	 */
	public KTerminationChaser(
			StatisticsCollector statistics,
			int k) {
		super(statistics);
		Preconditions.checkArgument(k >= 0);
		this.k = k;
	}

	/**
	 * @param state S
	 * @param dependencies Collection<? extends Constraint>
	 */
	@Override
	public <S extends ChaseState> void reasonUntilTermination(S s, Query<?> target, Collection<? extends Constraint> dependencies) {
		Preconditions.checkArgument(s instanceof ListState);
		int rounds = 0;
		boolean appliedStep = true;
		while (rounds < this.k && appliedStep) {
			appliedStep = false;
			List<Match> matches = s.getMaches(dependencies);
			for (Match match: matches) {
				if(!s.isSatisfied(match)){
					s.chaseStep(match);
					appliedStep = true;
				}
			}
			++rounds;
		}
	}

}
