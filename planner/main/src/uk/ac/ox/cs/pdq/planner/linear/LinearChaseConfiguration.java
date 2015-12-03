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

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.reasoning.Match;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Type of configurations met in the linear world.
 * Each linear chase configuration is associated with a set of facts a rule firing and a set of generated facts p – 
 * the ones produced by the last rule firing.
 * The (output) facts are all stored inside the state member field.
 *
 * @author Efthymia Tsamoura
 */
public class LinearChaseConfiguration extends ChaseConfiguration<LeftDeepPlan> implements LinearConfiguration {

	/** The parent linear configuration*/
	private final LinearChaseConfiguration parent;

	/** The (un)exposed candidate facts */
	private List<Candidate> candidates;

	/** The candidate facts exposed in this configuration */
	private final Set<Candidate> exposedCandidates;

	/** Random engine. Used when selecting candidate facts to expose*/
	protected final Random random;

	/**
	 * 
	 * @param parent
	 * 		The parent linear configuration
	 * @param exposedCandidates
	 * 		The candidate facts exposed in this configuration
	 * @param random
	 */
	public LinearChaseConfiguration(
			LinearChaseConfiguration parent,
			Set<Candidate> exposedCandidates,
			Random random) {		
		super(parent.getState().clone(),
				Collections.EMPTY_SET,
				LinearUtility.getOutputConstants(exposedCandidates)
				);
		Preconditions.checkNotNull(parent);
		Preconditions.checkNotNull(exposedCandidates);
		Preconditions.checkArgument(!exposedCandidates.isEmpty());
		Preconditions.checkNotNull(this.getInput());
		Preconditions.checkNotNull(this.getOutput());
		this.random = random;
		this.parent = parent;
		this.exposedCandidates = exposedCandidates;
		List<Match> matches = new ArrayList<>();
		for (Candidate candidate:exposedCandidates) {
			matches.add(candidate.getMatch());
		}
		this.chaseStep(matches);
		LeftDeepPlan plan = LeftDeepPlanGenerator.createLeftDeepPlan(this, this.parent.getPlan());
		this.setPlan(plan);
	}

	/**
	 * Creates a linear chase configuration using the input accessible chase state that has no parent configuration.
	 * Used when creating the root of the linear plan tree.
	 * @param state
	 * @param random
	 */
	public LinearChaseConfiguration(
			AccessibleChaseState state,
			Random random) {
		super(state,
				null,
				null
				);
		this.random = random;
		this.parent = null;
		this.exposedCandidates = null;
		this.setPlan(null);
	}

	/**
	 * 
	 * @param candidate
	 * @return
	 * 		true of the input candidate has been already exposed 
	 */
	public boolean isExposed(Candidate candidate) {
		Predicate infAcc = candidate.getInferredAccessibleFact();
		return this.getState().getFacts().contains(infAcc);
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
	public void detectCandidates(AccessibleSchema accessibleSchema) throws PlannerException {
		List<Candidate> result = new ArrayList<>();
		Map<AccessibilityAxiom, List<Match>> nonFiredAxioms =
				this.getState().getUnexposedFacts(accessibleSchema);
		for (AccessibilityAxiom axiom:nonFiredAxioms.keySet()) {
			for (Match matching:nonFiredAxioms.get(axiom)) {
				Predicate fact = axiom.getGuard().ground(matching.getMapping());
				result.add(new Candidate(accessibleSchema, axiom, fact, matching));
			}
		}
		this.candidates = result;
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
						&& candidate.getAccessMethod().equals(current.getAccessMethod())) {
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
		if(this.parent != null) {
			return new LinearChaseConfiguration(
					this.parent,
					this.exposedCandidates,
					this.random);
		}
		return new LinearChaseConfiguration(
				this.getState().clone(),
				this.random);
	}

	@Override
	public int compareTo(Configuration<LeftDeepPlan> o) {
		return this.getPlan().compareTo(o.getPlan());
	}
}
