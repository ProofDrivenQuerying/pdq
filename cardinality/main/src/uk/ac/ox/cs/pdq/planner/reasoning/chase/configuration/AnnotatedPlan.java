/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

// TODO: Auto-generated Javadoc
/**
 * A configuration which uses the chase as a proof system .
 *
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public abstract class AnnotatedPlan<P extends Plan> implements Configuration<P> {

	/** The configuration's state. This can be either a tree of bags or a list of facts */
	protected final ChaseState state;

	/**  The plan that corresponds to this configuration. */
	protected P plan;

	/**  Output constants. */
	protected final Collection<Constant> output;

	/**
	 * Instantiates a new annotated plan.
	 *
	 * @param state 		The state of this configuration.
	 * @param output 		The output constants
	 */
	public AnnotatedPlan(
			ChaseState state,
			Collection<Constant> output
			) {
		this.state = state;
		this.output = output;
	}
	
	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public ChaseState getState() {
		return this.state;
	}

	/**
	 * Gets the plan.
	 *
	 * @return P
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#getPlan()
	 */
	@Override
	public P getPlan() {
		return this.plan;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.Configuration#setPlan(uk.ac.ox.cs.pdq.plan.Plan)
	 */
	@Override
	public void setPlan(P plan) {
		this.plan = plan;
	}

	/**
	 * Gets the output.
	 *
	 * @return the configuration's output facts
	 */
	public Collection<Constant> getOutput() {
		return this.output;
	}

	/**
	 * Gets the output facts.
	 *
	 * @return the configuration's output facts
	 */
	public Collection<Atom> getOutputFacts() {
		return this.state.getFacts();
	}
	
	/**
	 * Closes this.configuration using the input dependencies
	 *
	 * @param chaser the chaser
	 * @param query the query
	 * @param dependencies the dependencies
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	public void reasonUntilTermination(Chaser chaser, Query<?> query, Collection<? extends Constraint> dependencies) throws PlannerException, LimitReachedException {
		chaser.reasonUntilTermination(this.state, dependencies);
	}

	/**
	 * Fires the dependencies in the input matches.
	 *
	 * @param matches the matches
	 */
	public void chaseStep(List<Match> matches) {
		this.state.chaseStep(matches);
	}

	/**
	 * Matches query.
	 *
	 * @param query the query
	 * @return 		the list of query matches
	 * @throws PlannerException the planner exception
	 */
	public List<Match> matchesQuery(Query<?> query) throws PlannerException {
		return this.state.getMatches(query);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.Configuration#isSuccessful(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean isSuccessful(Query<?> query) {
		try {
			return !this.matchesQuery(query).isEmpty();
		} catch (PlannerException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Clone.
	 *
	 * @return ChaseConfiguration<S,P>
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#clone()
	 */
	@Override
	public abstract AnnotatedPlan<P> clone();

}
