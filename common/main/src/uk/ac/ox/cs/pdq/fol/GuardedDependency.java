package uk.ac.ox.cs.pdq.fol;

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
	Atom getGuard();
}
