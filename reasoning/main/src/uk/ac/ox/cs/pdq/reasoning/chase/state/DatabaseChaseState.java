package uk.ac.ox.cs.pdq.reasoning.chase.state;

import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManager;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 *
 * A collection of facts produced during chasing.
 * It also keeps a graph of the rule firings that took place during chasing.
 * This implementation keeps the facts produced during chasing in a database.
 * Homomorphisms are detected using the DBMS the stores the chase facts. 
 *
 * @author Efthymia Tsamoura
 *
 */
public abstract class DatabaseChaseState implements ChaseState {

	/**  Queries and updates the database of facts *. */
	protected HomomorphismManager manager;

	/**
	 * Constructor for DatabaseChaseState.
	 * @param manager DBHomomorphismManager
	 */
	public DatabaseChaseState(
			HomomorphismManager manager) {
		Preconditions.checkNotNull(manager);
		this.manager = manager;
	}

	/**
	 * Gets the manager.
	 *
	 * @return DBHomomorphismManager
	 */
	public HomomorphismManager getManager() {
		return this.manager;
	}

	/**
	 * Sets the manager.
	 *
	 * @param manager DBHomomorphismManager
	 */
	public void setManager(HomomorphismManager manager) {
		this.manager = manager;
	}

	/**
	 * Clone.
	 *
	 * @return DatabaseChaseState
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#clone()
	 */
	@Override
	public abstract DatabaseChaseState clone();
}
