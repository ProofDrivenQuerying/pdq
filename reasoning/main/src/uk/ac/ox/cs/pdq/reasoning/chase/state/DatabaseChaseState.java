package uk.ac.ox.cs.pdq.reasoning.chase.state;

import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;

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
	protected DBHomomorphismManager manager;

	/**
	 * Constructor for DatabaseChaseState.
	 * @param manager DBHomomorphismManager
	 */
	public DatabaseChaseState(
			DBHomomorphismManager manager) {
		Preconditions.checkNotNull(manager);
		this.manager = manager;
	}

	/**
	 * Gets the manager.
	 *
	 * @return DBHomomorphismManager
	 */
	public DBHomomorphismManager getManager() {
		return this.manager;
	}

	/**
	 * Sets the manager.
	 *
	 * @param manager DBHomomorphismManager
	 */
	public void setManager(DBHomomorphismManager manager) {
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
