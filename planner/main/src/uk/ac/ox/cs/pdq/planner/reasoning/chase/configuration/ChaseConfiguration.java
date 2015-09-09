package uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.reasoning.Proof;
import uk.ac.ox.cs.pdq.planner.reasoning.Proof.ProofState;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.equivalence.FactEquivalence;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.equivalence.FastFactEquivalence;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.equivalence.FastStructuralEquivalence;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.equivalence.StructuralEquivalence;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.Costable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * A configuration which uses the chase as a proof system 
 * @author Efthymia Tsamoura
 *
 */
public abstract class ChaseConfiguration<P extends Plan> implements Configuration<P> {

	/** The accessible schema */
	protected final AccessibleSchema accessibleSchema;

	protected final Query<?> query;

	/** The configuration's state. This can be either a tree of bags or a list of facts */
	protected final AccessibleChaseState state;

	/** Chases the state */
	protected final Chaser chaser;

	/** The plan that corresponds to this configuration */
	protected P plan;

	/** The proof that corresponds to this configuration */
	protected Proof proof;

	protected final List<Proof.ProofState> proofState;

	/** Input constants */
	protected final Collection<Constant> input;

	/** Output constants */
	protected final Collection<Constant> output;

	/** Proper output constants */
	protected final Collection<Constant> properOutput;

	/** Perform dominance checks */
	protected Dominance[] dominance;

	/** Perform success dominance checks */
	protected SuccessDominance successDominance;
	
	/** Performs structural equivalence checks */
	private final StructuralEquivalence structuralEquivalence = new FastStructuralEquivalence();
	
	/** Performs structural equivalence checks */
	private final FactEquivalence factEquivalence = new FastFactEquivalence();

	/**
	 * Creates a configuration which encloses the input state object
	 * @param accessibleSchema
	 * @param query
	 * 		The input query
	 * @param chaser
	 * 		Chase reasoner
	 * @param state
	 * 		The state of this configuration.
	 * @param proofState
	 * 		The proof state of this configuration
	 * @param flow
	 * 		The control flow of the configuration's plan
	 * @param input
	 * 		The input constants
	 * @param output
	 * 		The output constants
	 * @param dominance
	 * 		Perform dominance checks
	 * @param successDominance
	 * 		Performs success dominance checks
	 */
	public ChaseConfiguration(
			AccessibleSchema accessibleSchema,
			Query<?> query,
			Chaser chaser,
			AccessibleChaseState state,
			List<Proof.ProofState> proofState,
			Collection<Constant> input,
			Collection<Constant> output,
			Dominance[] dominance,
			SuccessDominance successDominance) {
		Preconditions.checkNotNull(accessibleSchema);
		Preconditions.checkNotNull(query);
		Preconditions.checkNotNull(chaser);
		Preconditions.checkNotNull(successDominance);
		Preconditions.checkNotNull(dominance);

		this.accessibleSchema = accessibleSchema;
		this.query = query;
		this.chaser = chaser;
		this.state = state;
		this.proofState = proofState;
		this.input = input;
		this.output = output;
		this.properOutput = getProperOutput(input, output);
		this.dominance = dominance;
		this.successDominance = successDominance;
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

	public AccessibleSchema getAccessibleSchema() {
		return this.accessibleSchema;
	}


	public AccessibleChaseState getState() {
		return this.state;
	}


	public Chaser getChaser() {
		return this.chaser;
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

	public Proof getProof() {
		return this.proof;
	}
	
	public void setProof(Proof proof) {
		this.proof = proof;
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
	 * @return the configuration's inferred accessible facts
	 */
	public Collection<String> getInferred() {
		return this.state.getInferred();
	}

	/**
	 * @return the inferred accessible facts that were derived during chasing the state of this configuration
	 */
	public Collection<Predicate> getDerivedInferred() {
		return this.getState().getDerivedInferred();
	}

	/**
	 * @return the firings that have driven to the derivation of each fact in the state of this configuration
	 */
	public Map<Predicate, Pair<Constraint, Collection<Predicate>>> getFactProvenance() {
		return this.state.getProvenance();
	}


	/**
	 * Closes this.configuration using the input dependencies
	 * @throws PlannerException
	 * @throws LimitReachedException
	 */
	public void close() throws PlannerException, LimitReachedException {
		this.chaser.reasonUntilTermination(this.state, this.query, this.accessibleSchema.getInferredAccessibilityAxioms());
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

	public Dominance[] getDominanceDetectors() {
		return this.dominance;
	}


	public SuccessDominance getSuccessDominanceDetector() {
		return this.successDominance;
	}

	public void setDominance(Dominance[] dominance) {
		Preconditions.checkNotNull(dominance);
		this.dominance = dominance;
	}

	public void setSuccessDominance(SuccessDominance successDominance) {
		Preconditions.checkNotNull(successDominance);
		this.successDominance = successDominance;
	}

	/**
	 * @param configuration 
	 * @return true if this configuration is dominated by the input one
	 */
	public boolean isDominatedBy(ChaseConfiguration configuration) {
		for(Dominance detector:this.dominance) {
			if(detector.isDominated(this, configuration)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @param configuration
	 * @return true if this.configuration is equivalent to the input one
	 */
	public boolean isEquivalentTo(ChaseConfiguration configuration) {
		return this.factEquivalence.isEquivalent(this, (ChaseConfiguration<P>) configuration);
	}

	/**
	 *
	 * @param configuration
	 * @return true if this.configuration is equivalent to the input one
	 */
	public boolean isStructurallyEquivalentTo(ChaseConfiguration configuration) {
		return this.structuralEquivalence.isEquivalent(this, (ChaseConfiguration<P>) configuration);
	}

	public Query<?> getQuery() {
		return this.query;
	}

	public List<ProofState> getProofState() {
		return this.proofState;
	}

	@Override
	public boolean isSuccessful() {
		try {
			return !this.matchesQuery(this.getQuery()).isEmpty();
		} catch (PlannerException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Creates a proof for this configuration given the query match
	 * @param parentProof
	 */
	public Proof createProof(Map<Variable, Constant> match) {
		return Proof.createProof(this.getProofState(), match);
	}

	/**
	 * Adds the final projection to the configuration's plan 
	 */
	public abstract void addProjection();

	/**
	 * @return ChaseConfiguration<S,P>
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#clone()
	 */
	@Override
	public abstract ChaseConfiguration<P> clone();

	public abstract <C extends Costable> CostEstimator<C> getCostEstimator();

}
