package uk.ac.ox.cs.pdq.planner.dag.equivalence;

import java.util.Collection;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;

/**
 * Class of equivalent configurations.
 *  A mapping h from the chase constants of one configuration conf to the chase constants of another configuration conf'
 *  is fact-preserving if it preserves inferred accessible output facts in going from conf to conf' and if the h image of every input
 *  constant of conf is an input constant of conf'. Configurations conf, conf' are fact-equivalent if there is a bijective fact-preserving mapping h between them.
 * @author Efthymia Tsamoura
 *
 */
public abstract class DAGEquivalenceClass {

	/**  The representative of this class. */
	protected DAGChaseConfiguration representative;

	/**  The minimum depth configuration of this class. */
	protected Integer minHeight;

	/**
	 * Adds the entry.
	 *
	 * @param configuration Adds the input configuration to the class
	 */
	public abstract void addEntry(DAGChaseConfiguration configuration);

	/**
	 * Removes the input configuration from the class.
	 *
	 * @param configuration the configuration
	 */
	public abstract void removeEntry(DAGChaseConfiguration configuration);

	/**
	 * Gets all the configurations in the class.
	 *
	 * @return the configurations of the class
	 */
	public abstract Collection<DAGChaseConfiguration> getAll();

	/**
	 * Removes all input configurations.
	 *
	 * @param configurations the configurations
	 */
	public abstract void removeAll(Collection<DAGChaseConfiguration> configurations);

	/**
	 * Dominated by.
	 *
	 * @param dominance the dominance
	 * @param configuration the configuration
	 * @return the class configurations that are dominated by the input configuration
	 */
	public abstract Collection<DAGChaseConfiguration> dominatedBy(Dominance[] dominance, DAGChaseConfiguration configuration);


	/**
	 * Structurally equivalent to.
	 *
	 * @param configuration the configuration
	 * @return true if the configuration is structurally equivalent to the configurations of this class
	 */
	public abstract boolean structurallyEquivalentTo(DAGChaseConfiguration configuration);

	/**
	 * Dominate.
	 *
	 * @param dominance the dominance
	 * @param configuration the configuration
	 * @return the configurations that dominate the input configuration
	 */
	public abstract DAGChaseConfiguration dominate(Dominance[] dominance, DAGChaseConfiguration configuration);

	/**
	 * Checks if is sleeping.
	 *
	 * @return true if the class is sleeping. A class is sleeping if its minimum cost closed configuration has cost > the best plan found so far.
	 */
	public abstract boolean isSleeping();

	/**
	 * Checks if is empty.
	 *
	 * @return true if the class is empty
	 */
	public abstract boolean isEmpty();

	/**
	 * Size.
	 *
	 * @return the size of the class
	 */
	public abstract int size();

	/**
	 * Gets the representative.
	 *
	 * @return DAGChaseConfiguration
	 */
	public DAGChaseConfiguration getRepresentative() {
		return this.representative;
	}

	/**
	 * Gets the min height.
	 *
	 * @return Integer
	 */
	public Integer getMinHeight() {
		return this.minHeight;
	}
}