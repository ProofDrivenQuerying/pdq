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
import uk.ac.ox.cs.pdq.reasoning.utility.DefaultRestrictedDependencyAssessor;
import uk.ac.ox.cs.pdq.reasoning.utility.ReasonerUtility;
import uk.ac.ox.cs.pdq.reasoning.utility.RestrictedDependencyAssessor;

import com.google.common.base.Preconditions;


/**
 * @TODO delete this class. It is actually the same with RestrictedChaser
 *
 * @author Efthymia Tsamoura
 *
 */
public class SequentialEGDChaser extends Chaser {
	
	/**
	 * Constructor for EGDChaser.
	 * @param statistics StatisticsCollector
	 */
	public SequentialEGDChaser(
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
		RestrictedDependencyAssessor accessor = new DefaultRestrictedDependencyAssessor(dependencies);
		boolean appliedStep = false;
		do {
			appliedStep = false;
			Collection<? extends Constraint> d = accessor.getDependencies(s);
			List<Match> matches = s.getMaches(d);
			
			int i = 0;
			for (Match match: matches) {
				if(new ReasonerUtility().isActiveTrigger(match, s)){
					//A single chase step
					s.chaseStep(match);
					appliedStep = true;
				}
				i++;
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
	public SequentialEGDChaser clone() {
		return new SequentialEGDChaser(this.statistics);
	}

}
