package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;

/**
 * Finds the dependencies that should be considered during chasing.
 * The returned dependencies are most likely to lead to triggers that are not already satisfied.  
 *    
 * @author Efthymia Tsamoura
 *
 */

public interface ParallelEGDDependencyAssessor {

	
	public static enum EGDROUND{EGD, TGD};
	
	Collection<? extends Constraint> getDependencies(ChaseState state, EGDROUND round);
	
}
