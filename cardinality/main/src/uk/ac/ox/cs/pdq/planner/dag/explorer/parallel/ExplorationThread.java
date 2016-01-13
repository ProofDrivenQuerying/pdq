package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.cardinality.CardinalityEstimator;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGAnnotatedPlanClasses;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;

/**
 * Iterates over the input collection of configurations to identify the minimum-cost one
 *
 * @author Efthymia Tsamoura
 *
 */
public class ExplorationThread implements Callable<DAGAnnotatedPlan> {

	protected static Logger log = Logger.getLogger(ExplorationThread.class);
	
	private final Query<?> query;
	
	/** Performs success dominance checks*/
	private final Dominance qualityDominance;
	
	private final Dominance[] dominance;

	/** Detects homomorphisms*/
	private final HomomorphismDetector detector;

	/** Input configurations*/
	private final Queue<DAGAnnotatedPlan> input;

	/** Classes of structurally equivalent configurations */
	private final DAGAnnotatedPlanClasses classes;

	/** The minimum cost closed and successful configuration found so far */
	private DAGAnnotatedPlan best = null;
	
	private boolean bestQueryMatch = false;

	/** The output non-dominated and not successful configurations */
	private final Set<DAGAnnotatedPlan> output;

	/** The output non-dominated and successful (and not closed) configurations */
	private final Set<DAGAnnotatedPlan> successful;
	
	private final CardinalityEstimator cardinalityEstimator;

	/**
	 *
	 * @param input
	 * 		Input configurations
	 * @param classes
	 * 		Classes of structurally equivalent configurations
	 * @param best
	 * 		The minimum cost closed and successful configuration found so far
	 * @param detector
	 * 		Detects homomorphisms
	 * @param qualityDominance
	 * 		Performs success dominance checks
	 * @param output
	 * 		The output non-dominated and not successful configurations
	 * @param successfulConfigurations
	 * 		The output non-dominated and successful (and not closed) configurations
	 */
	public ExplorationThread(
			Query<?> query,
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
	 * @return DAGAnnotatedPlan
	 * @throws Exception
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public DAGAnnotatedPlan call() throws Exception {
		DAGAnnotatedPlan configuration;
		//Poll the next configuration
		while((configuration = this.input.poll()) != null) {
			//log.trace(configuration + "\t" + "Size: " + configuration.getSize() + "\t" +  "Quality: " + configuration.getQuality() + "\t" + "Adjusted quality: " + configuration.getAdjustedQuality());
			if(configuration.getState() instanceof DatabaseChaseState) {
				((DatabaseChaseState)configuration.getState()).setManager((DBHomomorphismManager) this.detector);
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
				
				//Assess its potential
				if (this.best == null || 
						configuration.getAdjustedQuality() <= this.best.getAdjustedQuality()
						//configuration.getSize().compareTo(this.best.getSize()) < 0
						) 
						
						
				{
					//Find the configurations dominated by the current one and remove them
					Collection<DAGAnnotatedPlan> dominated = this.classes.dominatedBy(this.dominance, configuration);
					if(!dominated.isEmpty()) {
						this.output.removeAll(dominated);
						this.classes.removeAll(dominated);
						this.successful.removeAll(dominated);
					}
//					//Check for query match
//					boolean matchesQuery = configuration.isSuccessful(this.query);
//					//Estimate the configuration's adjusted quality 
//					double adjustedQuality = this.cardinalityEstimator.adjustedQualityOf(configuration, this.query, matchesQuery);
//					configuration.setAdjustedQuality(adjustedQuality);
//					if (matchesQuery || configuration.getOutput().containsAll(this.query.getFree2Canonical().values())) {
//						this.setBestConfiguration(configuration);
//					}
					//Update the best configuration
					if(!matchesQuery) {
						this.classes.addEntry(configuration);
						this.output.add(configuration);
					}
					if(matchesQuery) {
						log.trace("QUERY MATCH: " + configuration + "\t\t" + 
								"Size: " + configuration.getSize() + "\t" + "Cardinality: " + configuration.getCardinality() + "\t" +  
								"Quality: " + configuration.getQuality() + "\t" + 
								"Adjusted quality: " + configuration.getAdjustedQuality());
						this.successful.add(configuration);
						this.setBestConfiguration(configuration, matchesQuery);
					}
					if(!matchesQuery && configuration.getOutput().containsAll(this.query.getFree2Canonical().values())) {
						log.trace("CONTAINMENT: " + configuration + "\t\t" + 
								"Size: " + configuration.getSize() + "\t" + "Cardinality: " + configuration.getCardinality() + "\t" +  
								"Quality: " + configuration.getQuality() + "\t" + 
								"Adjusted quality: " + configuration.getAdjustedQuality());
						this.setBestConfiguration(configuration, matchesQuery);
					}
				}
			}
		}
		return this.best;
	}

	/**
	 * @param configuration DAGAnnotatedPlan
	 */
	private void setBestConfiguration(DAGAnnotatedPlan configuration, boolean matchesQuery) {
		if(this.best != null && configuration != null &&
				//this.best.isSuccessful(this.query) && 
				this.bestQueryMatch && 
				this.best.getSize().compareTo(configuration.getSize()) < 0 &&
				this.best.getAdjustedQuality() <= configuration.getAdjustedQuality()
				) {
			return;
		}
		this.best = configuration;
		this.bestQueryMatch = matchesQuery;
		
	}
}
