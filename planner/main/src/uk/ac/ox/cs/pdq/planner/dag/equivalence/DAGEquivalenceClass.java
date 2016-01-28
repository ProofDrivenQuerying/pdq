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

	/** The representative of this class */
	protected DAGChaseConfiguration representative;

	/** The minimum depth configuration of this class */
	protected Integer minHeight;

	/**
	 * @param configuration Adds the input configuration to the class
	 */
	public abstract void addEntry(DAGChaseConfiguration configuration);

	/**
	 * Removes the input configuration from the class
	 * @param configuration
	 */
	public abstract void removeEntry(DAGChaseConfiguration configuration);

	/**
	 * @return the configurations of the class */
	public abstract Collection<DAGChaseConfiguration> getAll();

	/**
	 * Removes all input configurations
	 * @param configurations
	 */
	public abstract void removeAll(Collection<DAGChaseConfiguration> configurations);

	/**
	 * @param configuration
	 * @return the class configurations that are dominated by the input configuration
	 */
	public abstract Collection<DAGChaseConfiguration> dominatedBy(Dominance[] dominance, DAGChaseConfiguration configuration);


	/**
	 * @param configuration
	 * @return true if the configuration is structurally equivalent to the configurations of this class
	 */
	public abstract boolean structurallyEquivalentTo(DAGChaseConfiguration configuration);

	/**
	 * @param configuration
	 * @return the configurations that dominate the input configuration
	 */
	public abstract DAGChaseConfiguration dominate(Dominance[] dominance, DAGChaseConfiguration configuration);

	/**
	 *
	 * @return true if the class is sleeping. A class is sleeping if its minimum cost closed configuration has cost > the best plan found so far.
	 */
	public abstract boolean isSleeping();

	/**
	 * @return true if the class is empty
	 */
	public abstract boolean isEmpty();

	/**
	 * @return the size of the class
	 */
	public abstract int size();

	/**
	 * @return DAGChaseConfiguration
	 */
	public DAGChaseConfiguration getRepresentative() {
		return this.representative;
	}

	/**
	 * @return Integer
	 */
	public Integer getMinHeight() {
		return this.minHeight;
	}
}