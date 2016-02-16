package uk.ac.ox.cs.pdq.planner.linear;

import java.util.Collection;
import java.util.Set;

import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

// TODO: Auto-generated Javadoc
/**
 * Configurations represent derivation of implicit information using constraints and have a direct correspondence with a query plan.
 * Linear configurations are mapped to left-deep plans.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface LinearConfiguration extends Configuration<LeftDeepPlan> {

	/**
	 * Gets the candidates.
	 *
	 * @return the candidates to expose of this configuration
	 */
	Collection<Candidate> getCandidates();

	/**
	 * Gets the exposed candidates.
	 *
	 * @return the exposed candidates of this configuration
	 */
	Collection<Candidate> getExposedCandidates();

	/**
	 * Choose candidate.
	 *
	 * @return a randomly chosen candidate of this configuration
	 */
	Candidate chooseCandidate();

	/**
	 * Checks for candidates.
	 *
	 * @return true if the configuration has candidates
	 */
	Boolean hasCandidates();

	/**
	 * Remove the given candidates.
	 *
	 * @param candidates Set<Candidate>
	 */
	void removeCandidates(Set<Candidate> candidates);

	/**
	 * Gets the similar candidates.
	 *
	 * @param candidate the candidate
	 * @return a list of candidates sharing the same chase constants in their input positions with this configuration
	 */
	Set<Candidate> getSimilarCandidates(Candidate candidate);
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.Configuration#clone()
	 */
	@Override
	LinearConfiguration clone(); 

}
