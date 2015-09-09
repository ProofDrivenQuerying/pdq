package uk.ac.ox.cs.pdq.db;

import uk.ac.ox.cs.pdq.fol.Predicate;

/**
 * A guarded dependency
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface GuardedDependency {

	/**
	 * @return the guard of this dependency
	 */
	Predicate getGuard();
}
