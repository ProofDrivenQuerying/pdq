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
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Runs the parallel EGD chase.
 * TODO add description
 *
 * @author Efthymia Tsamoura
 *
 */
public class EGDChaser extends Chaser {


	/**
	 * Constructor for EGDChaser.
	 * @param statistics StatisticsCollector
	 */
	public EGDChaser(
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
			appliedOddStep = false;
			appliedEvenStep = false;
			//Find all active triggers
			List<Match> matches = step % 2 == 0 ? s.getMaches(tgds):s.getMaches(egds);
			List<Match> activeTriggers = Lists.newArrayList();
			for(Match match:matches) {
				if(!s.isSatisfied(match)){
					activeTriggers.add(match);
				}
			}
			boolean succeeds = s.chaseStep(activeTriggers);
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
		} while (!(appliedOddStep == false && appliedEvenStep == false && step % 2 == 0));
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
					HomomorphismConstraint.topK(1),
					HomomorphismConstraint.satisfies(free)};

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

}
