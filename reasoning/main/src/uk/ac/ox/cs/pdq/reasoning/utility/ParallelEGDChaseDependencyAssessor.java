package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;

// TODO: Auto-generated Javadoc
/**
 * Finds the dependencies that should be considered during chasing.
 * The returned dependencies are most likely to lead to triggers that are not already satisfied.  
 *    
 * @author Efthymia Tsamoura
 *
 */

public interface ParallelEGDChaseDependencyAssessor {

	
	/**
	 * The Enum EGDROUND.
	 */
	public static enum EGDROUND{/** The egd. */
EGD, /** The tgd. */
 TGD};
	
	/**
	 * Gets the dependencies.
	 *
	 * @param state 		A collection of chase facts
	 * @param round the round
	 * @return 		the dependencies that are most likely to be fired in the next chase round.
	 */
	Collection<? extends Dependency> getDependencies(ChaseState state, EGDROUND round);
	
}
