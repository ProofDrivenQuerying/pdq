package uk.ac.ox.cs.pdq.planner.linear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import uk.ac.ox.cs.pdq.algebra.Operators;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.reasoning.Proof;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

/**
 * A linear configuration with state a ChaseState object
 *
 * @author Efthymia Tsamoura
 */
public class LinearChaseConfiguration extends ChaseConfiguration<LeftDeepPlan> implements LinearConfiguration {

	private final EventBus eventBus;

	private final boolean collectStats;

	/** The parent linear configuration*/
	private final LinearChaseConfiguration parent;

	/** The (un)exposed candidates */
	private final List<Candidate> candidates;

	/** The exposed candidates */
	private final Set<Candidate> exposedCandidates;

	/** Random engine. Used when selecting candidate facts to expose*/
	protected final Random random;
	
	private final CostEstimator<LeftDeepPlan> costEstimator;

	/**
	 *
	 * @param eventBus
	 * @param collectStats
	 * @param schema
	 * @param accessibleSchema
	 * @param chaser
	 * 		Chases the input state
	 * @param costEstimator
	 * 		Estimates a plan's cost
	 * @param parent
	 * 		The parent linear configuration
	 * @param exposedCandidates
	 * 		The exposed candidates
	 * @param flow
	 * @param random
	 * @throws PlannerException
	 */
	public LinearChaseConfiguration(EventBus eventBus,
			boolean collectStats,
			AccessibleSchema accessibleSchema,
			Query<?> query,
			Chaser chaser,
			Dominance[] dominance,
			SuccessDominance successDominance,
			CostEstimator<LeftDeepPlan> costEstimator,
			LinearChaseConfiguration parent,
			Set<Candidate> exposedCandidates,
			Random random) throws PlannerException {		
		super(accessibleSchema, 
				query, 
				chaser, 
				parent.getState().clone(),
				createState(parent.getProofState(), exposedCandidates),
				Collections.EMPTY_SET,
				LinearUtility.getOutputConstants(exposedCandidates),
				dominance,
				successDominance);
		Preconditions.checkNotNull(parent);
		Preconditions.checkNotNull(exposedCandidates);
		Preconditions.checkArgument(!exposedCandidates.isEmpty());
		Preconditions.checkNotNull(costEstimator);
		Preconditions.checkNotNull(this.getInput());
		Preconditions.checkNotNull(this.getOutput());

		this.eventBus = eventBus;
		this.collectStats = collectStats;
		this.random = random;
		this.parent = parent;
		this.exposedCandidates = exposedCandidates;
		this.costEstimator = costEstimator;
		List<Match> matches = new ArrayList<>();
		for (Candidate candidate:exposedCandidates) {
			matches.add(candidate.getMatch());
		}
		this.chaseStep(matches);
		this.candidates = this.detectCandidates();
		LeftDeepPlan plan = this.createPlan(this.parent.getPlan());
		this.setPlan(plan);
	}
	
	/**
	 *
	 * @param eventBus
	 * @param collectStats
	 * @param schema
	 * @param accessibleSchema
	 * @param chaser
	 * 		Chases the input state
	 * @param costEstimator
	 * 		Estimates a plan's cost
	 * @param state
	 * @param flow
	 * @param random
	 * @throws PlannerException
	 */
	public LinearChaseConfiguration(EventBus eventBus,
			boolean collectStats,
			AccessibleSchema accessibleSchema,
			Query<?> query,
			Chaser chaser,
			AccessibleChaseState state,
			Dominance[] dominance,
			SuccessDominance successDominance,
			CostEstimator<LeftDeepPlan> costEstimator,
			Random random) throws PlannerException {
		super(accessibleSchema, 
				query,
				chaser, 
				state,
				null,
				null,
				null,
				dominance,
				successDominance);
		Preconditions.checkNotNull(costEstimator);
		
		this.eventBus = eventBus;
		this.collectStats = collectStats;
		this.random = random;
		this.parent = null;
		this.exposedCandidates = null;
		this.candidates = this.detectCandidates();
		this.costEstimator = costEstimator;
		this.setPlan(null);
	}


	/**
	 * @param candidates Set<Candidate>
	 * @see uk.ac.ox.cs.pdq.linear.configuration.LinearConfiguration#removeCandidates(Set<Candidate>)
	 */
	@Override
	public void removeCandidates(Set<Candidate> candidates) {
		this.candidates.removeAll(candidates);
	}

	/**
	 * Detects the configuration's candidate facts
	 * @return List<Candidate>
	 * @throws PlannerException
	 */
	public List<Candidate> detectCandidates() throws PlannerException {
		return this.detectCandidates(null);
	}

	/**
	 * Detects the configuration's candidate facts
	 * @param reachableRelations List<Relation>
	 * @return List<Candidate>
	 * @throws PlannerException
	 */
	public List<Candidate> detectCandidates(List<Relation> reachableRelations) throws PlannerException {
		List<Candidate> result = new ArrayList<>();
		Map<AccessibilityAxiom, List<Match>> nonFiredAxioms =
				this.getState().getUnexposedFacts(this.getAccessibleSchema());
		for (AccessibilityAxiom axiom:nonFiredAxioms.keySet()) {
			for (Match matching:nonFiredAxioms.get(axiom)) {
				Predicate fact = axiom.getGuard().ground(matching.getMapping());
				result.add(new Candidate(this.getAccessibleSchema(), axiom, fact, matching));
			}
		}
		return result;
	}

	/**
	 * @param candidate Input candidate fact
	 * @return a list of candidates sharing the same constants in their input positions with this configuration * @see uk.ac.ox.cs.pdq.linear.configuration.LinearConfiguration#getSimilarCandidates(Candidate)
	 */
	@Override
	public Set<Candidate> getSimilarCandidates(Candidate candidate) {
		Set<Candidate> similarCandidates = new HashSet<>();
		Iterator<Candidate> iterator = this.candidates.iterator();
		while(iterator.hasNext()) {
			Candidate current = iterator.next();
			if(this.isExposed(candidate)) {
				iterator.remove();
			}
			else 
			if (candidate.getRelation().equals(current.getRelation())
					&& candidate.getBinding().equals(current.getBinding())) {
				Collection<Constant> terms1 = candidate.getInput();
				Collection<Constant> terms2 = current.getInput();
				if (terms1 == null && terms2 == null) {
					similarCandidates.add(current);
				} else if (terms1 != null && terms2 != null) {
					Set<Constant> set1 = Sets.newLinkedHashSet(terms1);
					Set<Constant> set2 = Sets.newLinkedHashSet(terms2);
					if (set1.equals(set2)) {
						similarCandidates.add(current);
					}
				}
			}
		}
		return similarCandidates;
	}


	/**
	 * @return List<Candidate>
	 * @see uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration#getCandidates()
	 */
	@Override
	public List<Candidate> getCandidates() {
		return this.candidates;
	}

	/**
	 * @return Collection<Candidate>
	 * @see uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration#getExposedCandidates()
	 */
	@Override
	public Collection<Candidate> getExposedCandidates() {
		return this.exposedCandidates;
	}


	/**
	 * @return Boolean
	 * @see uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration#hasCandidates()
	 */
	@Override
	public Boolean hasCandidates() {
		return !this.candidates.isEmpty();
	}

	/**
	 * @return Candidate
	 * @see uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration#chooseCandidate()
	 */
	@Override
	public Candidate chooseCandidate() {
		while(!this.candidates.isEmpty()) {
			int selection = this.random.nextInt(this.candidates.size());
			Candidate candidate = this.candidates.get(selection);
			if(!this.isExposed(candidate)) {
				return candidate;
			}
			else {
				this.candidates.remove(selection);
			}
		}
		return null;
	}
	
	@Override
	public void addProjection() {
		Projection project = Operators.createFinalProjection(this.getQuery(), this.getPlan().getOperator());
		LeftDeepPlan plan = this.getPlan().projectLast(project);
		plan.setCost(this.getPlan().getCost());
		this.setPlan(plan);
	}
	
	private static List<Proof.ProofState> createState(List<Proof.ProofState> parentState, Set<Candidate> exposedCandidates) {
		AccessibilityAxiom axiom = null;
		Set<Map<Variable, Constant>> matches = Sets.newLinkedHashSet();
		for(Candidate candidate:exposedCandidates) {
			axiom = candidate.getRule();
			matches.add(candidate.getMatch().getMapping());
		}
		List<Proof.ProofState> s = Lists.newArrayList();
		if(parentState != null) {
			s.addAll(parentState);
		}
		s.add(Proof.ProofState.createState(axiom, matches));
		return s;
	}

	/**
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.getState().equals(((LinearChaseConfiguration) o).getState());
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.getState());
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return Joiner.on("\n").join(this.exposedCandidates);
	}
	
	/**
	 * @return LinearChaseConfiguration
	 * @see uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration#clone()
	 */
	@Override
	public LinearChaseConfiguration clone() {
		try {
			if(this.parent != null) {
				return new LinearChaseConfiguration(this.eventBus,
						this.collectStats,
						this.getAccessibleSchema(),
						this.getQuery(),
						this.getChaser(),
						this.getDominanceDetectors(),
						this.getSuccessDominanceDetector(),
						this.getCostEstimator(),
						this.parent,
						this.exposedCandidates,
						this.random);
			}
			return new LinearChaseConfiguration(this.eventBus,
					this.collectStats,
					this.getAccessibleSchema(),
					this.getQuery(),
					this.getChaser(),
					this.getState().clone(),
					this.getDominanceDetectors(),
					this.getSuccessDominanceDetector(),
					this.getCostEstimator(),
					this.random);
		} catch (PlannerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param parentPlan LeftDeepPlan
	 * @see uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration#createPlan(LeftDeepPlan)
	 */
	@Override
	public LeftDeepPlan createPlan(LeftDeepPlan parentPlan) {
		LeftDeepPlan plan = LinearPlanGenerator.createLinearPlan(this, parentPlan);
		this.getCostEstimator().cost(plan);
		return plan;
	}
	
	public boolean isExposed(Candidate candidate) {
		Predicate infAcc = candidate.getInferredAccessibleFact();
		return this.getState().getFacts().contains(infAcc);
	}

	public CostEstimator<LeftDeepPlan> getCostEstimator() {
		return this.costEstimator;
	}

	@Override
	public int compareTo(Configuration<LeftDeepPlan> o) {
		return this.getPlan().compareTo(o.getPlan());
	}
}
