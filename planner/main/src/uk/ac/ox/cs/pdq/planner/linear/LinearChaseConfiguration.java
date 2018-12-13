package uk.ac.ox.cs.pdq.planner.linear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.Utility;

/**
 * Type of configurations met in the linear world.
 * Each linear chase configuration is associated with a set of facts, 
 * a rule firing, and a set of generated facts --
 * the ones produced by the last rule firing.
 * The (output) facts are all stored inside the state member field.
 *
 * @author Efthymia Tsamoura
 */
public class LinearChaseConfiguration extends ChaseConfiguration implements LinearConfiguration {

	/**  The (un)exposed candidate facts. */
	private List<Candidate> candidates;
	
	/**  The candidate facts exposed in this configuration. */

	private final Set<Candidate> newlyExposedCandidates;
	
	/**  An accessibility axiom. */
	private final AccessibilityAxiom rule;
	
	/** The facts of this configuration. These must share the same constants for the input positions of the accessibility axiom */
	private final Set<Atom> facts;

	/**
	 * Instantiates a new linear chase configuration.
	 *
	 * @param parent 		The parent linear configuration
	 * @param candidatesToExpose 		The candidate facts exposed in this configuration
	 */
	public LinearChaseConfiguration(LinearChaseConfiguration parent, Set<Candidate> candidatesToExpose) {		
		super(parent.getState().clone(), new LinkedHashSet<Constant>(), LinearUtility.getOutputConstants(candidatesToExpose));
		Assert.assertNotNull(parent);
		Assert.assertTrue(candidatesToExpose!= null && !candidatesToExpose.isEmpty());
		Assert.assertTrue(this.getInput() != null && this.getInput().isEmpty());
		Assert.assertNotNull(this.getOutput());
		this.rule = candidatesToExpose.iterator().next().getRule();
		this.facts = new LinkedHashSet<>();
		this.newlyExposedCandidates = candidatesToExpose;
		for (Candidate candidate:candidatesToExpose) {
			Assert.assertTrue(this.rule.equals(candidate.getRule()));
			this.facts.add(candidate.getFact());
		}
		List<Match> matches = new ArrayList<>();
		for (Candidate candidate:candidatesToExpose) 
			matches.add(candidate.getMatch());
		this.chaseStep(matches);
		
		RelationalTerm op1 = PlanCreationUtility.createSingleAccessPlan(this.rule.getBaseRelation(), this.rule.getAccessMethod(), this.facts);
		if(parent.getPlan() != null)
			this.plan = PlanCreationUtility.createJoinPlan(parent.getPlan(),op1);
		else 
			this.plan = op1;
	}

	/**
	 * Creates a linear chase configuration using the input accessible chase state that has no parent configuration.
	 * Used when creating the root of the linear plan tree.
	 *
	 * @param state the state; i.e. the set of facts
	 */
	public LinearChaseConfiguration(AccessibleChaseInstance state) {
		super(state, null, null);
		this.rule = null;
		this.facts = null;
		this.plan = null;
		this.newlyExposedCandidates = null;
	}

	/**
	 * Checks if is exposed.
	 *
	 * @param candidate the candidate
	 * @return 		true if the input candidate has been already exposed
	 */
	public boolean isExposed(Candidate candidate) {
		return this.getState().getFacts().contains(candidate.getInferredAccessibleFact());
	}
	
	/**
	 * Removes the candidates.
	 *
	 * @param candidates Set<Candidate>
	 * @see uk.ac.ox.cs.pdq.linear.configuration.LinearConfiguration#removeCandidates(Set<Candidate>)
	 */
	@Override
	public void removeCandidates(Set<Candidate> candidates) {
		this.candidates.removeAll(candidates);
	}

	/**
	 * Detects the configuration's candidate facts.
	 *
	 * @param accessibleSchema the accessible schema
	 * @return List<Candidate>
	 * @throws PlannerException the planner exception
	 */
	public void detectCandidates(AccessibleSchema accessibleSchema) {
		List<Candidate> result = new ArrayList<>();
		Map<AccessibilityAxiom, List<Match>> nonFiredAxioms = this.getState().getUnexposedFacts(accessibleSchema);
		for (AccessibilityAxiom axiom:nonFiredAxioms.keySet()) {
			for (Match match:nonFiredAxioms.get(axiom)) {
				Atom fact = (Atom) Utility.applySubstitution(axiom.getGuard(), match.getMapping());
				result.add(new Candidate(axiom, fact, match));
			}
		}
		this.candidates = result;
	}

	/**
	 * Gets the similar candidates.
	 *
	 * @param candidate Input candidate fact
	 * @return a list of candidates sharing the same constants in their input positions with this configuration * @see uk.ac.ox.cs.pdq.linear.configuration.LinearConfiguration#getSimilarCandidates(Candidate)
	 */
	@Override
	public Set<Candidate> getSimilarCandidates(Candidate candidate) {
		Set<Candidate> similarCandidates = new HashSet<>();
		Iterator<Candidate> iterator = this.candidates.iterator();
		while(iterator.hasNext()) {
			Candidate current = iterator.next();
			if(this.isExposed(candidate)) 
				iterator.remove();
			else 
				if (candidate.getRelation().equals(current.getRelation())
						&& candidate.getAccessMethod().equals(current.getAccessMethod())) {
					Collection<Constant> terms1 = candidate.getInput();
					Collection<Constant> terms2 = current.getInput();
					if (terms1 == null && terms2 == null) {
						similarCandidates.add(current);
					} else if (terms1 != null && terms2 != null) {
						Set<Constant> set1 = new LinkedHashSet<>(terms1);
						Set<Constant> set2 = new LinkedHashSet<>(terms2);
						if (set1.equals(set2)) 
							similarCandidates.add(current);
					}
				}
		}
		return similarCandidates;
	}


	/**
	 * Gets the candidates.
	 *
	 * @return List<Candidate>
	 * @see uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration#getCandidates()
	 */
	@Override
	public List<Candidate> getCandidates() {
		return this.candidates;
	}

	/**
	 * Checks for candidates.
	 *
	 * @return Boolean
	 * @see uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration#hasCandidates()
	 */
	@Override
	public Boolean hasCandidates() {
		return !this.candidates.isEmpty();
	}

	/**
	 *
	 * @return Candidate
	 * @see uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration#chooseCandidate()
	 */
	@Override
	public Candidate chooseCandidate() {
		while(!this.candidates.isEmpty()) {
			int selection = this.candidates.size()-1;
			Candidate candidate = this.candidates.get(selection);
			if(!this.isExposed(candidate)) 
				return candidate;
			else 
				this.candidates.remove(selection);
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.Configuration#compareTo(uk.ac.ox.cs.pdq.planner.reasoning.Configuration)
	 */
	@Override
	public int compareTo(Configuration o) {
		return this.getCost().compareTo(o.getCost());
	}

	public AccessibilityAxiom getRule() {
		return this.rule;
	}

	public Set<Atom> getFacts() {
		return this.facts;
	}

	public Set<Candidate> getExposedCandidates() {
		return this.newlyExposedCandidates;
	}
}
