package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;
import uk.ac.ox.cs.pdq.reasoning.utility.ReasonerUtility;

import com.google.common.base.Preconditions;


/**
 * The restricted chase algorithm.
 * According to this, a dependency is fired unless it is already satisfied.
 * The facts that are being generated after each chase step are kept in a list.
 *
 * @author Efthymia Tsamoura
 *
 */
public class RestrictedChaser extends Chaser {


	/**
	 * Constructor for RestrictedChaser.
	 * @param statistics
	 */
	public RestrictedChaser(
			StatisticsCollector statistics) {
		super(statistics);
	}

	/**
	 * Chases the input state until termination
	 * @param s
	 * @param target
	 * @param dependencies
	 */
	@Override
	public <S extends ChaseState> void reasonUntilTermination(S s,  Query<?> target, Collection<? extends Constraint> dependencies) {
		Preconditions.checkArgument(s instanceof ListState);
		boolean appliedStep = false;
		do {
			appliedStep = false;
			List<Match> matches = s.getMaches(dependencies);
			for (Match match: matches) {
				if(new ReasonerUtility().isActiveTrigger(match, s)){
					//A single chase step
					s.chaseStep(match);
					appliedStep = true;
				}
			}
		} while (appliedStep);
	}

	/**
	 * 
	 * @param instance
	 * @param free
	 * 		Mapping of query's free variables to constants
	 * @param target
	 * @param constraints
	 * @return
	 * 		true if the input instance with the given set of free variables and constraints implies the target query.
	 * 		
	 */
	@Override
	public <S extends ChaseState> boolean entails(S instance, Map<Variable, Constant> free, Query<?> target,
			Collection<? extends Constraint> constraints) {
		this.reasonUntilTermination(instance, target, constraints);
		HomomorphismConstraint[] c = {
				HomomorphismConstraint.topK(1),
				HomomorphismConstraint.satisfies(free)};
		return !instance.getMatches(target,c).isEmpty();
	}

	/**
	 * 
	 * @param source
	 * @param target
	 * @param constraints
	 * @return
	 * 		true if the source query entails the target query
	 */
	@Override
	public <S extends ChaseState> boolean entails(Query<?> source, Query<?> target,
			Collection<? extends Constraint> constraints) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RestrictedChaser clone() {
		return new RestrictedChaser(this.statistics);
	}
}
