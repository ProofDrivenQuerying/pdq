// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear;

import java.util.Collection;
import java.util.Set;

import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

/**
 * Configurations represent a state in the search space for a plan. They include derived facts and a corresponding  query plan.
 * Linear configurations are mapped to left-deep plans.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface LinearConfiguration extends Configuration {

	/**
	 *
	 * @return the candidates to expose of this configuration
	 */
	Collection<Candidate> getCandidates();

	/**
	 *
	 * @return a candidate of this configuration
	 */
	Candidate chooseCandidate();

	/**
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
	 *
	 * @param candidate the candidate
	 * @return a list of candidates sharing the same chase constants in their input positions with this configuration
	 */
	Set<Candidate> getSimilarCandidates(Candidate candidate);

}
