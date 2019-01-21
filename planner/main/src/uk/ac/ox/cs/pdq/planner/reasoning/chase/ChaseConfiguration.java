package uk.ac.ox.cs.pdq.planner.reasoning.chase;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.ChaseConstantGenerator;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Proof configurations or configurations are associated with (i) a collection
 * of facts using initial chase constants called the output facts OF, which will
 * always implicitly include the initial chase facts, (ii) a subset of the
 * initial chase constants, called the input chase constants IC. IC will
 * represent hypotheses that the proof uses about which values are accessible.
 * We can derive from the output facts the collection of output chase constants
 * OC of the configuration: those that are mentioned in the facts OF. A
 * configuration with input constants IC and output facts OF represents a proof
 * of OF using the rules of AcSch, starting from the hypothesis that each c \in
 * IC is accessible. The (output) facts are all stored inside the state member
 * field.
 * 
 * 
 * @author Efthymia Tsamoura
 *
 * @param <P>
 *            type of configuration plans. Plans depending on the type of proof
 *            configuration can be either DAG or sequential.
 */
public abstract class ChaseConfiguration implements Configuration {
	private final Integer id;

	/**
	 * The configuration's chase state. Keeps the output facts of this configuration
	 */
	protected final AccessibleChaseInstance state;

	/** The plan that corresponds to this configuration. */
	protected RelationalTerm plan;

	protected Cost cost;

	/** Input constants. */
	private final Collection<Constant> input;

	/** Output constants. */
	protected final Collection<Constant> output;

	/**
	 * "Proper" output constants, where proper means it does not contain constants
	 * that are inputs.
	 */
	protected final Collection<Constant> properOutput;

	/**
	 * Instantiates a new chase configuration.
	 *
	 * @param state
	 *            The chase state of this configuration.
	 * @param input
	 *            The input constants
	 * @param output
	 *            The output constants
	 */
	public ChaseConfiguration(AccessibleChaseInstance state, Collection<Constant> input, Collection<Constant> output) {
		this.state = state;
		this.input = input;
		this.output = output;
		this.properOutput = getProperOutput(input, output);
		this.id = GlobalCounterProvider.getNext("ChaseConfigurationID");
	}

	/**
	 * All output expect the input constants.
	 * 
	 * @param input
	 *            the input
	 * @param output
	 *            the output
	 * @return the proper output
	 */
	private static Collection<Constant> getProperOutput(Collection<Constant> input, Collection<Constant> output) {
		Collection<Constant> properOutput;
		if (input != null && output != null) {
			properOutput = Lists.newArrayList(output);
			properOutput.removeAll(input);
		} else
			properOutput = null;
		return properOutput;
	}

	/**
	 *
	 * @return the chase state of this configuration.
	 */
	public AccessibleChaseInstance getState() {
		return this.state;
	}

	/**
	 *
	 * @return P
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#getPlan()
	 */
	@Override
	public RelationalTerm getPlan() {
		return this.plan;
	}

	@Override
	public Cost getCost() {
		return this.cost;
	}

	@Override
	public void setCost(Cost cost) {
		this.cost = cost;
	}

	/**
	 *
	 * @return the configuration's input constants
	 */
	@Override
	public Collection<Constant> getInput() {
		return this.input;
	}

	/**
	 *
	 * @return the configuration's output facts
	 */
	@Override
	public Collection<Constant> getOutput() {
		return this.output;
	}

	/**
	 * @return the configuration's output facts that does not contain any input
	 *         constants.
	 */
	public Collection<Constant> getProperOutput() {
		return this.properOutput;
	}

	/**
	 *
	 * @return the configuration's output facts
	 */
	public Collection<Atom> getOutputFacts() {
		return this.state.getFacts();
	}

	/**
	 * Finds all consequences of this.configuration using the input dependencies and
	 * the chase algorithm as a proof system.
	 *
	 * @param chaser
	 *            the chaser
	 * @param query
	 *            the query
	 * @param dependencies
	 *            the dependencies
	 * @throws PlannerException
	 *             the planner exception
	 * @throws LimitReachedException
	 *             the limit reached exception
	 */
	public void reasonUntilTermination(Chaser chaser, Dependency[] dependencies) throws PlannerException, LimitReachedException {
		chaser.reasonUntilTermination(this.state, dependencies);
	}

	/**
	 * Applies the input triggers.
	 *
	 * @param matches
	 *            the matches
	 */
	public void chaseStep(List<Match> matches) {
		this.state.chaseStep(matches);
	}

	/**
	 * Conjunctive query match definition) If Q′ is a conjunctive query and v is a
	 * chase configuration having elements for each free variable of Q′, then a
	 * homomorphism of Q′ into v mapping each free variable into the corresponding
	 * element is called a match for Q′ in v.
	 *
	 * @param query
	 *            An input query
	 * @return the list of query matches
	 * @throws PlannerException
	 *             the planner exception
	 */
	public List<Match> matchesQuery(ConjunctiveQuery query) throws PlannerException {
		return this.state.getMatches(query, ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().get(query));
	}

	/**
	 * Generate canonical mapping.
	 *
	 * @param formula
	 *            the body
	 * @return a mapping of variables of the input conjunction to constants. A fresh
	 *         constant is created for each variable of the conjunction. This method
	 *         is invoked by the conjunctive query constructor when the constructor
	 *         is called with empty input canonical mapping.
	 */
	public static Map<Variable, Constant> generateSubstitutionToCanonicalVariables(Formula formula) {
		Map<Variable, Constant> canonicalMapping = new LinkedHashMap<>();
		for (Atom atom : formula.getAtoms()) {
			for (Term t : atom.getTerms()) {
				if (t.isVariable()) {
					Constant c = canonicalMapping.get(t);
					if (c == null) {
						c = UntypedConstant.create(ChaseConstantGenerator.getName());
						canonicalMapping.put((Variable) t, c);
					}
				}
			}
		}
		return canonicalMapping;
	}

	/**
	 * Checks if is filter.
	 *
	 * @return true if the configuration is a filter
	 */
	public Boolean isFilter() {
		return this.properOutput.isEmpty();
	}

	/**
	 *
	 * @return true if the configuration has no input constants
	 */
	public Boolean isClosed() {
		return this.input.isEmpty();
	}

	/**
	 *
	 * @param query
	 *            the query
	 * @return true if the configuration matches the input query. (Conjunctive query
	 *         match definition) If Q′ is a conjunctive query and v is a chase
	 *         configuration having elements for each free variable of Q′, then a
	 *         homomorphism of Q′ into v mapping each free variable into the
	 *         corresponding element is called a match for Q′ in v.
	 */
	@Override
	public boolean isSuccessful(ConjunctiveQuery query) {
		try {
			return !this.matchesQuery(query).isEmpty();
		} catch (PlannerException e) {
			throw new IllegalStateException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (this.plan == null && ((ChaseConfiguration) o).plan == null)
			return true;
		if (this.plan == null && ((ChaseConfiguration) o).plan != null)
			return false;
		return this.getClass().isInstance(o) && this.plan.equals(((ChaseConfiguration) o).plan);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.plan);
	}

	public Integer getId() {
		return this.id;
	}

}
