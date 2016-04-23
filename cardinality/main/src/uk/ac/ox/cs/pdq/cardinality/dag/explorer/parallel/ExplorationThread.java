/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.dag.explorer.parallel;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.dag.equivalence.DAGAnnotatedPlanClasses;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityUtility;
import uk.ac.ox.cs.pdq.cardinality.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Iterates over the input collection of configurations to identify the minimum-cost one.
 *
 * @author Efthymia Tsamoura
 */
public class ExplorationThread implements Callable<DAGAnnotatedPlan> {
	
	private static int MAX_COVERAGE = 5;

	/** The log. */
	protected static Logger log = Logger.getLogger(ExplorationThread.class);
	
	/** The query. */
	private final ConjunctiveQuery query;
	
	/**  Performs success dominance checks. */
	private final Dominance qualityDominance;
	
	/** The dominance. */
	private final Dominance[] dominance;

	/**  Detects homomorphisms. */
	private final HomomorphismDetector detector;

	/**  Input configurations. */
	private final Queue<DAGAnnotatedPlan> input;

	/**  Classes of structurally equivalent configurations. */
	private final DAGAnnotatedPlanClasses classes;

	/**  The minimum cost closed and successful configuration found so far. */
	private DAGAnnotatedPlan best = null;
	
	//Remove at 04/02/2015
//	private boolean bestQueryMatch = false;

	/**  The output non-dominated and not successful configurations. */
	private final Set<DAGAnnotatedPlan> output;

	/**  The output non-dominated and successful (and not closed) configurations. */
	private final Set<DAGAnnotatedPlan> successful;
	
	/** The cardinality estimator. */
	private final CardinalityEstimator cardinalityEstimator;

	/**
	 * Instantiates a new exploration thread.
	 *
	 * @param query the query
	 * @param input 		Input configurations
	 * @param classes 		Classes of structurally equivalent configurations
	 * @param best 		The minimum cost closed and successful configuration found so far
	 * @param detector 		Detects homomorphisms
	 * @param cardinalityEstimator the cardinality estimator
	 * @param qualityDominance 		Performs success dominance checks
	 * @param dominance the dominance
	 * @param output 		The output non-dominated and not successful configurations
	 * @param successfulConfigurations 		The output non-dominated and successful (and not closed) configurations
	 */
	public ExplorationThread(
			ConjunctiveQuery query,
			Queue<DAGAnnotatedPlan> input,
			DAGAnnotatedPlanClasses classes,
			DAGAnnotatedPlan best,
			HomomorphismDetector detector,
			CardinalityEstimator cardinalityEstimator,
			Dominance qualityDominance,
			Dominance[] dominance,
			Set<DAGAnnotatedPlan> output,
			Set<DAGAnnotatedPlan> successfulConfigurations
			) {	
		Preconditions.checkNotNull(query);
		Preconditions.checkNotNull(detector);
	
		this.best = best == null ? null : best.clone();
		this.query = query;
		this.detector = detector;
		this.input = input;
		this.classes = classes;
		this.output = output;
		this.successful = successfulConfigurations;
		this.qualityDominance = qualityDominance;
		this.dominance = dominance;
		this.cardinalityEstimator = cardinalityEstimator;
	}

	/**
	 * Call.
	 *
	 * @return DAGAnnotatedPlan
	 * @throws Exception the exception
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public DAGAnnotatedPlan call() throws Exception {
		DAGAnnotatedPlan configuration;
		//Poll the next configuration
		while((configuration = this.input.poll()) != null) {
//			log.trace(configuration + "\t\t" + 
//					"Size: " + configuration.getSize() + "\t" +
//					"SizeOf: " + configuration.getSizeOf() + "\t" + 
//					"SizeOfProjection: " + configuration.getSizeOfProjection() + "\t" +  
//					"Quality: " + configuration.getQuality() + "\t" + 
//					"Adjusted quality: " + configuration.getAdjustedQuality());
			if(configuration.getState() instanceof DatabaseChaseState) {
				((DatabaseChaseState)configuration.getState()).setManager((DatabaseHomomorphismManager) this.detector);
			}
			//If the configuration is not dominated
			DAGAnnotatedPlan dominator = this.classes.dominate(this.dominance, configuration);
			if (dominator != null
					//ExplorerUtils.isDominated(this.dag, binConfig) != null || ExplorerUtils.isDominated(binConfigs, binConfig) != null
					) {
			} else {
				
				//Check for query match
				boolean matchesQuery = configuration.isSuccessful(this.query);
				//Estimate the configuration's adjusted quality 
				double adjustedQuality = this.cardinalityEstimator.adjustedQualityOf(configuration, this.query, matchesQuery);
				configuration.setAdjustedQuality(adjustedQuality);
				
				//Removed at 04/02/2015
//				//Assess its potential
				if (this.best == null || 
						configuration.getAdjustedQuality() <= this.best.getAdjustedQuality() 
//						configuration.getIndependenceAssumptions() <= 2
				) 
//				if(!(this.best != null && 
//						this.best.getCoverage() >= MAX_COVERAGE && 
//						this.best.getAdjustedQuality() < configuration.getAdjustedQuality()))
				{
					//Find the configurations dominated by the current one and remove them
					Collection<DAGAnnotatedPlan> dominated = this.classes.dominatedBy(this.dominance, configuration);
					if(!dominated.isEmpty()) {
						this.output.removeAll(dominated);
						this.classes.removeAll(dominated);
						this.successful.removeAll(dominated);
					}
					//Update the best configuration
					if(!matchesQuery) {
						this.classes.addEntry(configuration);
						this.output.add(configuration);
					}
					if(matchesQuery) {
						this.successful.add(configuration);
						//Estimate the coverage of the input annotated plan
						int coverage = CardinalityUtility.coverage(configuration, this.query, this.detector);
						configuration.setCoverage(coverage);
						this.setBestConfiguration(configuration);
						log.trace("QUERY MATCH: " + configuration + "\t\t" + 
								"Size: " + configuration.getSize() + "\t" +
								"SizeOf: " + configuration.getSizeOf() + "\t" + 
								"SizeOfProjection: " + configuration.getSizeOfProjection() + "\t" +  
								"Quality: " + configuration.getQuality() + "\t" + 
								"Adjusted quality: " + configuration.getAdjustedQuality()+ "\t" +  
								"Coverage: " + configuration.getCoverage());
					}
					if(!matchesQuery && configuration.getOutput().containsAll(this.query.getGroundingsProjectionOnFreeVars().values())) {
						//Estimate the coverage of the input annotated plan
						int coverage = CardinalityUtility.coverage(configuration, this.query, this.detector);
						configuration.setCoverage(coverage);
						this.setBestConfiguration(configuration);
						log.trace("CONTAINMENT: " + configuration + "\t\t" + 
								"Size: " + configuration.getSize() + "\t" +
								"SizeOf: " + configuration.getSizeOf() + "\t" + 
								"SizeOfProjection: " + configuration.getSizeOfProjection() + "\t" +  
								"Quality: " + configuration.getQuality() + "\t" + 
								"Adjusted quality: " + configuration.getAdjustedQuality() + "\t" + 
								"Coverage: " + configuration.getCoverage());
					}
				}
			}
		}
		return this.best;
	}

	/**
	 * @param configuration DAGAnnotatedPlan
	 */
	//Removed at 04/02/2015
	private void setBestConfiguration(DAGAnnotatedPlan configuration) {
		if(this.best != null && 
				configuration != null &&
				this.best.getSize().compareTo(configuration.getSize()) <= 0 
				&& this.best.getAdjustedQuality() <= configuration.getAdjustedQuality()
				) {
			return;
		}
		this.best = configuration;
	}
	
//	private void setBestConfiguration(DAGAnnotatedPlan configuration) {
//		if(this.best == null && configuration != null) {
//			this.best = configuration;
//		}
//		else if(this.best != null && 
//				configuration != null &&
//				configuration.getCoverage() >= this.best.getCoverage() &&
//				configuration.getSize().compareTo(this.best.getSize()) < 0) {
//			this.best = configuration;
//		}
//		else if(this.best != null && 
//				configuration != null &&
//				configuration.getCoverage() == this.best.getCoverage() &&
//				configuration.getAdjustedQuality() <= this.best.getAdjustedQuality() &&
//				configuration.getSize().compareTo(this.best.getSize()) < 0) {
//			this.best = configuration;
//		}
//	}
}
