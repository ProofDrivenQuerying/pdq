package uk.ac.ox.cs.pdq.reasoning;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.ReasoningTypes;
import uk.ac.ox.cs.pdq.reasoning.chase.BoundedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.BoundedChaser.KSupplier;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.KTerminationChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * Creates reasoners based on the input arguments
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class ReasonerFactory {

	protected static Logger log = Logger.getLogger(ReasonerFactory.class);

	/** The event bus that will be shared by all instances create by this factory */
	private final EventBus eventBus;

	/** If true, statistics are collected while the reasoner is used. */
	private final boolean collectStatistics;

	/** Type of reasoner */
	private final ReasoningTypes type;

	/** K for the KTermination chase */
	private final Integer terminationK;

	/** KSupplier to be shared across all BoundedChasers created by this factory */
	private KSupplier kSupplier = null;

	/** true, if the reasoner initialisation shall be unrestricted. */
	private final Boolean fullInitialization;


	/**
	 *
	 * @param eventBus
	 * @param collectStats
	 * @param params
	 */
	public ReasonerFactory(
			EventBus eventBus,
			boolean collectStats,
			ReasoningParameters params) {
		this(eventBus, collectStats,
				params.getReasoningType(), 
				params.getTerminationK(),
				params.getFullInitialization());
	}

	/**
	 *
	 * @param eventBus
	 * @param collectStats
	 * @param type
	 * @param k
	 * @param fullInitialization
	 */
	protected ReasonerFactory(
			EventBus eventBus,
			boolean collectStats,
			ReasoningTypes type,
			Integer k,
			Boolean fullInitialization) {
		Preconditions.checkNotNull(eventBus);
		this.eventBus = eventBus;
		this.collectStatistics = collectStats;
		this.type = type;
		this.terminationK = k;
		this.fullInitialization = fullInitialization;
	}

	/**
	 * @return
	 * 		a fresh instance of reasoner. Currently, all reasoners
	 *      implement the Chaser interface.
	 */
	public Chaser getInstance() {
//		BlockingDetector blockingDetector = null;
//		if (this.type == ReasoningTypes.BLOCKING_CHASE) {
//			blockingDetector = new BlockingDetector();
//		} 
//		else  if (this.schema.isCyclic() && !this.schema.containsViews()) {
//			log.warn("Cycles detected in input schema. Forcing reasoning type to " + ReasoningTypes.BLOCKING_CHASE);
//			this.type = ReasoningTypes.BLOCKING_CHASE;
//			blockingDetector = new BlockingDetector();
//		}
		switch (this.type) {
		case RESTRICTED_CHASE:
			return new RestrictedChaser(
					this.collectStatistics == true ? new StatisticsCollector(this.collectStatistics, this.eventBus) : null);
		case KTERMINATION_CHASE:
			return new KTerminationChaser(
					this.collectStatistics == true ? new StatisticsCollector(this.collectStatistics, this.eventBus) : null,
					this.terminationK);
		case BOUNDED_CHASE:
			if (this.kSupplier == null) {
				this.kSupplier = new KSupplier(this.terminationK);
			}
			return new BoundedChaser(
					this.collectStatistics == true ? new StatisticsCollector(this.collectStatistics, this.eventBus) : null,
					this.kSupplier,
					this.fullInitialization);
//		case BLOCKING_CHASE:
//			return new BlockingChaser(
//					this.collectStatistics == true ? new StatisticsCollector(this.collectStatistics, this.eventBus) : null,
//					blockingDetector,
//					1);
		default:
			return null;
		}
	}

	/**
	 * @return the event bus associated with this factory
	 */
	public EventBus getEventBus() {
		return this.eventBus;
	}
	
	public ReasoningTypes getType() {
		return this.type;
	}
	
	public boolean getCollectStatistics() {
		return this.collectStatistics;
	}
}
