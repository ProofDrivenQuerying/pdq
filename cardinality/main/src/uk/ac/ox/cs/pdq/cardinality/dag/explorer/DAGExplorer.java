/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.dag.explorer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.cardinality.CardinalityException;
import uk.ac.ox.cs.pdq.cardinality.CardinalityParameters;
import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cardinality.explorer.Explorer;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Abstract DAG explorer.
 *
 * @author Efthymia Tsamoura
 */
public abstract class DAGExplorer extends Explorer<DAGPlan> {

	/** The query. */
	protected final ConjunctiveQuery query;

	/** The schema. */
	protected final Schema schema;

	/** The chaser. */
	protected final Chaser chaser;

	/** The detector. */
	protected final HomomorphismDetector detector;

	/** The cardinality estimator. */
	protected final CardinalityEstimator cardinalityEstimator;

	/**  The minimum cost configuration. */
	protected DAGAnnotatedPlan bestConfiguration = null;

	/** The parameters. */
	protected final CardinalityParameters parameters; 

	/**
	 * Instantiates a new DAG explorer.
	 *
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param parameters the parameters
	 * @param query the query
	 * @param schema the schema
	 * @param chaser the chaser
	 * @param detector the detector
	 * @param cardinalityEstimator the cardinality estimator
	 */
	public DAGExplorer(EventBus eventBus, 
			boolean collectStats, 
			CardinalityParameters parameters,
			ConjunctiveQuery query, 
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
	 * Updates the minimum cost configuration/plan.
	 *
	 * @param configuration the configuration
	 * @return true if the best configuration/plan is updated
	 */
	public boolean setBestPlan(DAGAnnotatedPlan configuration) {	
		//Removed at 04/02/2015
		if(this.bestConfiguration != null && configuration != null &&
				this.bestConfiguration.getSize().compareTo(configuration.getSize()) <= 0 
				&& this.bestConfiguration.getAdjustedQuality() <= configuration.getAdjustedQuality()
				) {
			return false;
		}
		this.bestConfiguration = configuration;
		this.eventBus.post(this);
		log.trace("\tBEST CONFIGURATION:	" + configuration + "\t\t" + 
				"Size: " + configuration.getSize() + "\t" +
				"SizeOf: " + configuration.getSizeOf() + "\t" + 
				"SizeOfProjection: "+configuration.getSizeOfProjection() + "\t" +  
				"Quality: "+configuration.getQuality() +  "\t" + 
				"Adjusted quality: "+configuration.getAdjustedQuality() );
		return true;
		
//		if(this.bestConfiguration == null && configuration != null) {
//			this.bestConfiguration = configuration;
//		}
//		else if(this.bestConfiguration != null && 
//				configuration != null &&
//				configuration.getCoverage() >= this.bestConfiguration.getCoverage() &&
//				configuration.getSize().compareTo(this.bestConfiguration.getSize()) < 0) {
//			this.bestConfiguration = configuration;
//		}
//		else if(this.bestConfiguration != null && 
//				configuration != null &&
//				configuration.getCoverage() == this.bestConfiguration.getCoverage() &&
//				configuration.getAdjustedQuality() <= this.bestConfiguration.getAdjustedQuality() &&
//				configuration.getSize().compareTo(this.bestConfiguration.getSize()) < 0) {
//			this.bestConfiguration = configuration;
//		}
//		this.eventBus.post(this);
//		log.trace("\tBEST CONFIGURATION:	" + configuration + "\t\t" + 
//				"Size: " + configuration.getSize() + "\t" +
//				"SizeOf: " + configuration.getSizeOf() + "\t" + 
//				"SizeOfProjection: "+configuration.getSizeOfProjection() + "\t" +  
//				"Quality: "+configuration.getQuality() +  "\t" + 
//				"Adjusted quality: "+configuration.getAdjustedQuality() + "\t" + 
//				"Coverage: " + configuration.getCoverage());
//		return true;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.explorer.Explorer#getBestPlan()
	 */
	@Override
	public DAGPlan getBestPlan() {
		if (this.bestConfiguration == null) {
			return null;
		}
		return this.bestPlan;
	}

	/**
	 * Gets the best configuration.
	 *
	 * @return the best configuration
	 */
	public DAGAnnotatedPlan getBestConfiguration() {
		return this.bestConfiguration;
	}

	/**
	 * Terminates.
	 *
	 * @return true if the planner terminates
	 */
	@Override
	protected boolean terminates() {
		return false;
	}

	/**
	 * Creates the initial configurations.
	 *
	 * @return a list of ApplyRule configurations based on the facts derived after chasing the input schema with the canonical database of the query
	 * @throws CardinalityException the planner exception
	 */
	protected List<DAGAnnotatedPlan> createInitialConfigurations() throws CardinalityException {
		ChaseState state = null;
		state = new DatabaseChaseListState(this.query, (DatabaseHomomorphismManager) this.detector);
		this.chaser.reasonUntilTermination(state, 
				CollectionUtils.union(this.schema.getDependencies(), this.schema.getKeyDependencies()));

		List<DAGAnnotatedPlan> collection = new ArrayList<>();
		for(Atom fact:state.getFacts()) {
			ChaseState newState = new DatabaseChaseListState((DatabaseHomomorphismManager) this.detector, Sets.newHashSet(fact));
			this.chaser.reasonUntilTermination(newState, this.schema.getDependencies());
			UnaryAnnotatedPlan unary =  new UnaryAnnotatedPlan(newState,fact);
			collection.add(unary);
		}
		return collection;
	}

}
