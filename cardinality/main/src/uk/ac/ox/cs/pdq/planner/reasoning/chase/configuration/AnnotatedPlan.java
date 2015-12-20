package uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

/**
 * A configuration which uses the chase as a proof system 
 * @author Efthymia Tsamoura
 *
 */
public abstract class AnnotatedPlan<P extends Plan> implements Configuration<P> {

	/** The configuration's state. This can be either a tree of bags or a list of facts */
	protected final ChaseState state;

	/** The plan that corresponds to this configuration */
	protected P plan;

	/** Output constants */
	protected final Collection<Constant> output;

	/**
	 * 
	 * @param state
	 * 		The state of this configuration.
	 * @param output
	 * 		The output constants
	 */
	public AnnotatedPlan(
			ChaseState state,
			Collection<Constant> output
			) {
		this.state = state;
		this.output = output;
	}
	
	public ChaseState getState() {
		return this.state;
	}

	/**
	 * @return P
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#getPlan()
	 */
	@Override
	public P getPlan() {
		return this.plan;
	}

	@Override
	public void setPlan(P plan) {
		this.plan = plan;
	}

	/**
	 * @return the configuration's output facts
	 */
	public Collection<Constant> getOutput() {
		return this.output;
	}

	/**
	 * @return the configuration's output facts
	 */
	public Collection<Predicate> getOutputFacts() {
		return this.state.getFacts();
	}
	
	/**
	 * Closes this.configuration using the input dependencies
	 * @throws PlannerException
	 * @throws LimitReachedException
	 */
	public void reasonUntilTermination(Chaser chaser, Query<?> query, Collection<? extends Constraint> dependencies) throws PlannerException, LimitReachedException {
		chaser.reasonUntilTermination(this.state, query, dependencies);
	}

	/**
	 * Fires the dependencies in the input matches
	 * @param matches
	 */
	public void chaseStep(List<Match> matches) {
		this.state.chaseStep(matches);
	}

	/**
	 * 
	 * @param query
	 * @return
	 * 		the list of query matches
	 * @throws PlannerException
	 */
	public List<Match> matchesQuery(Query<?> query) throws PlannerException {
		return this.state.getMatches(query);
	}
	
	@Override
	public boolean isSuccessful(Query<?> query) {
		try {
			return !this.matchesQuery(query).isEmpty();
		} catch (PlannerException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @return ChaseConfiguration<S,P>
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#clone()
	 */
	@Override
	public abstract AnnotatedPlan<P> clone();

}
