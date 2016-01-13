package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.cardinality.CardinalityEstimator;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.explorer.Explorer;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

/**
 * Abstract DAG explorer
 * @author Efthymia Tsamoura
 *
 */
public abstract class DAGExplorer extends Explorer<DAGPlan> {

	protected final Query<?> query;

	protected final Schema schema;

	protected final Chaser chaser;

	protected final HomomorphismDetector detector;

	protected final CardinalityEstimator cardinalityEstimator;

	/** The minimum cost configuration */
	protected DAGAnnotatedPlan bestConfiguration = null;

	protected final PlannerParameters parameters; 

	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param parameters
	 * @param query
	 * @param schema
	 * @param chaser
	 * @param detector
	 * @param cardinalityEstimator
	 */
	public DAGExplorer(EventBus eventBus, 
			boolean collectStats, 
			PlannerParameters parameters,
			Query<?> query, 
			Schema schema, 
			Chaser chaser, 
			HomomorphismDetector detector,
			CardinalityEstimator cardinalityEstimator) {
		super(eventBus, collectStats);
		Preconditions.checkArgument(parameters != null);
		Preconditions.checkArgument(query != null);
		Preconditions.checkArgument(schema != null);
		Preconditions.checkArgument(chaser != null);
		Preconditions.checkArgument(detector != null);
		Preconditions.checkArgument(cardinalityEstimator != null);
		this.parameters = parameters;
		this.query = query;
		this.schema = schema;
		this.chaser = chaser;
		this.detector = detector;
		this.cardinalityEstimator = cardinalityEstimator;
	}

	/**
	 * Updates the minimum cost configuration/plan
	 * @param configuration
	 * @return true if the best configuration/plan is updated
	 * @throws PlannerException 
	 */
	public boolean setBestPlan(DAGAnnotatedPlan configuration) {
		if(this.bestConfiguration != null && configuration != null &&
				this.bestConfiguration.isSuccessful(this.query) && 
				this.bestConfiguration.getSize().compareTo(configuration.getSize()) < 0 &&
				this.bestConfiguration.getAdjustedQuality() <= configuration.getAdjustedQuality()
				) {
			return false;
		}
		this.bestConfiguration = configuration;
		this.eventBus.post(this);
		log.trace("\tBEST CONFIGURATION:	" + configuration + "\t\t" + "Size: " + configuration.getSize() + "\t" + 
		"Cardinality: "+configuration.getCardinality() + "\t" +  "Quality: "+configuration.getQuality()
				+  "\t" + "Adjusted quality: "+configuration.getAdjustedQuality() );
		return true;
	}

	@Override
	public DAGPlan getBestPlan() {
		if (this.bestConfiguration == null) {
			return null;
		}
		return this.bestPlan;
	}

	public DAGAnnotatedPlan getBestConfiguration() {
		return this.bestConfiguration;
	}

	/**
	 * @return true if the planner terminates
	 */
	@Override
	protected boolean terminates() {
		return false;
	}

	/**
	 * @return a list of ApplyRule configurations based on the facts derived after chasing the input schema with the canonical database of the query
	 * @throws PlannerException
	 */
	protected List<DAGAnnotatedPlan> createInitialConfigurations() throws PlannerException {
		ChaseState state = null;
		state = new DatabaseListState(this.query, (DBHomomorphismManager) this.detector);
		this.chaser.reasonUntilTermination(state, this.query, 
				CollectionUtils.union(this.schema.getDependencies(), this.schema.getKeyDependencies()));

		List<DAGAnnotatedPlan> collection = new ArrayList<>();
		for(Predicate fact:state.getFacts()) {
			ChaseState newState = new DatabaseListState((DBHomomorphismManager) this.detector, Sets.newHashSet(fact));
			this.chaser.reasonUntilTermination(newState, this.query, this.schema.getDependencies());
			UnaryAnnotatedPlan unary =  new UnaryAnnotatedPlan(newState,fact);
			collection.add(unary);
		}
		return collection;
	}

}
