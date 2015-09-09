package uk.ac.ox.cs.pdq.reasoning.chase;

import static uk.ac.ox.cs.pdq.reasoning.logging.performance.ReasoningStatKeys.MILLI_BLOCKING_CHECK;
import static uk.ac.ox.cs.pdq.reasoning.logging.performance.ReasoningStatKeys.MILLI_UPDATE_QUERY_DEPENDENCIES;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.Bag.BagStatus;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TreeState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;

import com.google.common.base.Preconditions;

/**
 * Blocking chase algorithm.
 * The facts that are being generated after each chase step are organised
 * into a tree of bags.
 *
 * @author Efthymia Tsamoura
 *
 */
public class BlockingChaser extends Chaser {

	/** Detects which bags are blocked */
	protected final BlockingDetector blockingDetector;

	/** Defines how often will we perform blocking detection */
	protected final Integer blockingInterval;

	/**
	 * 
	 * @param statistics
	 * @param blockingDetector
	 * 		Detects which bags are blocked
	 * @param blockingInterval
	 * 		 Defines how often will we perform blocking detection
	 */
	public BlockingChaser(
			StatisticsCollector statistics,
			BlockingDetector blockingDetector,
			Integer blockingInterval) {
		super(statistics);
		Preconditions.checkNotNull(blockingDetector);
		Preconditions.checkNotNull(blockingInterval);
		this.blockingDetector = blockingDetector;
		this.blockingInterval = blockingInterval;
	}

	/**
	 * @param state Input state
	 * @return true if all bags are blocked
	 */
	protected boolean terminates(TreeState state) {
		for (Bag b: state.getTree().vertexSet()) {
			if (b.getType() == BagStatus.NONBLOCKED) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Performs blocking detection.
	 * It is called every this.blockingInterval steps
	 * @param state S
	 */
	protected void blockingCheck(TreeState state) {
		if(this.statistics != null){this.statistics.start(MILLI_BLOCKING_CHECK);}
		this.blockingDetector.doBlocking(state.getTree().vertexSet());
		if(this.statistics != null){this.statistics.stop(MILLI_BLOCKING_CHECK);}
	}

	/**
	 * @param state S
	 * @param dependencies Collection<? extends Constraint>
	 * @return if the chase succeeds
	 */
	public boolean reason(TreeState state, Collection<? extends Constraint> dependencies) {
		state.updateTree();
		int rounds = 0;
		//True if at the end of the internal for loop at least one dependency has been fired
		boolean appliedStep = true;
		while (appliedStep && !this.terminates(state)) {
			appliedStep = false;
			//Find for each input dependency, the matches of its left-hand side to the facts of the input state
			List<Match> matches = state.getMaches(dependencies);
			for (Match match: matches) {
				if(!state.isSatisfied(match)){
					state.chaseStep(match);
					appliedStep = true;
					++rounds;
					if (rounds % this.blockingInterval == 0) {
						this.statistics.start(MILLI_UPDATE_QUERY_DEPENDENCIES);
						state.updateTree();
						this.statistics.stop(MILLI_UPDATE_QUERY_DEPENDENCIES);
						this.blockingCheck(state);
					}
				}
			}
		}
		return true;
	}

	@Override
	public <S extends ChaseState> void reasonUntilTermination(S s,  Query<?> target, Collection<? extends Constraint> dependencies) {
		Preconditions.checkArgument(s instanceof TreeState);
		this.reason((TreeState)s, dependencies);
	}

	@Override
	public <S extends ChaseState> boolean entails(S instance, Map<Variable, Constant> free, Query<?> target,
			Collection<? extends Constraint> constraints) {
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
}
