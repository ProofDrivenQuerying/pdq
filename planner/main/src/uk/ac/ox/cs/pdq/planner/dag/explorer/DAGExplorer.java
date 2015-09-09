package uk.ac.ox.cs.pdq.planner.dag.explorer;

import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.explorer.Explorer;

import com.google.common.eventbus.EventBus;

/**
 * Abstract DAG explorer
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public abstract class DAGExplorer extends Explorer<DAGPlan> {

	/** The minimum cost configuration */
	protected DAGChaseConfiguration bestConfiguration = null;

	/**
	 * Constructor for DAGExplorer.
	 * @param eventBus EventBus
	 * @param collectStats boolean
	 */
	public DAGExplorer(EventBus eventBus, boolean collectStats) {
		super(eventBus, collectStats);
	}

	/**
	 * @return true if the planner terminates
	 */
	@Override
	protected boolean terminates() {
		return false;
	}

	/**
	 * Updates the minimum cost configuration/plan
	 * @param configuration
	 * @return true if the best configuration/plan is updated
	 */
	public boolean setBestPlan(DAGChaseConfiguration configuration) {
		if(this.bestConfiguration != null && configuration != null &&
				this.bestConfiguration.getPlan().getCost().lessOrEquals(configuration.getPlan().getCost())) {
			return false;
		}
		this.bestConfiguration = configuration;
		this.bestConfiguration.addProjection();
		this.bestPlan = this.bestConfiguration.getPlan();
		this.eventBus.post(this);
		this.eventBus.post(this.getBestPlan());
		log.trace("\t+ BEST CONFIGURATION	" + configuration + "\t" + configuration.getPlan().getCost());
		return true;
	}

	@Override
	public DAGPlan getBestPlan() {
		if (this.bestConfiguration == null) {
			return null;
		}
		return this.bestConfiguration.getPlan();
	}

	public DAGChaseConfiguration getBestConfiguration() {
		return this.bestConfiguration;
	}
}
