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
import uk.ac.ox.cs.pdq.reasoning.utility.DefaultRestrictedChaseDependencyAssessor;
import uk.ac.ox.cs.pdq.reasoning.utility.ReasonerUtility;
import uk.ac.ox.cs.pdq.reasoning.utility.RestrictedChaseDependencyAssessor;

import com.google.common.base.Preconditions;


/**
 * (From modern dependency theory notes)
 * Runs the chase algorithm applying only active triggers. 
 * Consider an instance I, a set Base of values, and a TGD
 * \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
 * a trigger for \delta in I is a homomorphism h of \sigma into I. A trigger is active if it
 * does not extend to a homomorphism h0 into I. Informally, a trigger is a tuple \vec{c}
 * satisfying \sigma, and it is active if there is no witness \vec{y} that makes \tau holds.
 * A chase step appends to I additional facts that were produced during grounding \delta. 
 * The output of the chase step is a new instance in which h is no longer an active trigger.
 * 
 * The facts that are generated during chasing are stored in a list.
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
	 * @param instance
	 * @param target
	 * @param dependencies
	 */
	@Override
	public <S extends ChaseState> void reasonUntilTermination(S instance,  Query<?> target, Collection<? extends Constraint> dependencies) {
		Preconditions.checkArgument(instance instanceof ListState);
		RestrictedChaseDependencyAssessor accessor = new DefaultRestrictedChaseDependencyAssessor(dependencies);
		boolean appliedStep = false;
		do {
			appliedStep = false;
			Collection<? extends Constraint> d = accessor.getDependencies(instance);
			List<Match> matches = instance.getMaches(d);
			for (Match match: matches) {
				if(new ReasonerUtility().isActiveTrigger(match, instance)){
					//A single chase step
					instance.chaseStep(match);
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
		if(!instance.isFailed()) {
			HomomorphismConstraint[] c = {
					HomomorphismConstraint.createTopKConstraint(1),
					HomomorphismConstraint.createMapConstraint(free)};

			return !instance.getMatches(target,c).isEmpty(); 
		}
		return false;
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
