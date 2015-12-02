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
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.collect.Lists;


/**
 * 	Proof configurations or configurations are associated with
	(i) a collection of facts using initial chase constants called the output
	facts OF, which will always implicitly include the initial chase
	facts, (ii) a subset of the initial chase constants, called the input
	chase constants IC. IC will represent hypotheses that the proof uses
	about which values are accessible. We can derive from the output
	facts the collection of output chase constants OC of the configuration:
	those that are mentioned in the facts OF. A configuration
	with input constants IC and output facts OF represents a proof of
	OF using the rules of AcSch, starting from the hypothesis that each
	c \in IC is accessible.
	The (output) facts are all stored inside the state member field.
 * 
 * 
 * @author Efthymia Tsamoura
 *
 * @param <P>
 * 		type of configuration plans. Plans depending on the type of proof configuration can be either DAG or sequential.
 */
public abstract class ChaseConfiguration<P extends Plan> implements Configuration<P> {

	/** The configuration's chase state. Keeps the output facts of this configuration */
	protected final AccessibleChaseState state;

	/** The plan that corresponds to this configuration */
	protected P plan;

	/** Input constants */
	protected final Collection<Constant> input;

	/** Output constants */
	protected final Collection<Constant> output;

	/** Proper output constants */
	protected final Collection<Constant> properOutput;
	
	/**
	 * 
	 * @param state
	 * 		The state of this configuration.
	 * @param input
	 * 		The input constants
	 * @param output
	 * 		The output constants
	 */
	public ChaseConfiguration(
			AccessibleChaseState state,
			Collection<Constant> input,
			Collection<Constant> output
			) {
		this.state = state;
		this.input = input;
		this.output = output;
		this.properOutput = getProperOutput(input, output);
	}

	private static Collection<Constant> getProperOutput(Collection<Constant> input, Collection<Constant> output) {
		Collection<Constant> properOutput;
		if(input != null && output != null) {
			properOutput = Lists.newArrayList(output);
			properOutput.removeAll(input);
		}
		else {
			properOutput = null;
		}
		return properOutput;
	}
	
	public AccessibleChaseState getState() {
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
	 * @return the configuration's input constants
	 */
	public Collection<Constant> getInput() {
		return this.input;
	}

	/**
	 * @return the configuration's output facts
	 */
	public Collection<Constant> getOutput() {
		return this.output;
	}

	/**
	 * @return the configuration's proper output facts
	 */
	public Collection<Constant> getProperOutput() {
		return this.properOutput;
	}

	/**
	 * @return the configuration's output facts
	 */
	public Collection<Predicate> getOutputFacts() {
		return this.state.getFacts();
	}
	
	/**
	 * Finds all consequences this.configuration using the input dependencies and the chase algorithm as a proof system.
	 * 
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

	/**
	 * @return true if the configuration filters 
	 */
	public Boolean isFilter() {
		return this.properOutput.isEmpty();
	}

	/**
	 * @return true if the configuration has no input constants
	 */
	public Boolean isClosed() {
		return this.input.isEmpty();
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
	public abstract ChaseConfiguration<P> clone();

}
