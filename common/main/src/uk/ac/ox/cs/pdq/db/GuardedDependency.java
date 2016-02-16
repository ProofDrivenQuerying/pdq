package uk.ac.ox.cs.pdq.db;

import uk.ac.ox.cs.pdq.fol.Predicate;

// TODO: Auto-generated Javadoc
/**
 * A guarded dependency.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface GuardedDependency {

	/**
	 * Gets the guard.
	 *
	 * @return the guard of this dependency
	 */
	Predicate getGuard();
}
