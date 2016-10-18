package uk.ac.ox.cs.pdq.reasoning;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.ReasoningTypes;
import uk.ac.ox.cs.pdq.reasoning.chase.BoundedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.BoundedChaser.KSupplier;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.ParallelEGDChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.KTerminationChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Creates reasoners based on the input arguments
 * 
 * 		The following reasoning algorithms are supported:
		-Restricted chase: Runs the chase algorithm applying only active triggers. 
 		Consider an instance I, a set Base of values, and a TGD
		\delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
		a trigger for \delta in I is a homomorphism h of \sigma into I. A trigger is active if it
		does not extend to a homomorphism h0 into I. Informally, a trigger is a tuple \vec{c}
		satisfying \sigma, and it is active if there is no witness \vec{y} that makes \tau holds.
		A chase step appends to I additional facts that were produced during grounding \delta. 
		The output of the chase step is a new instance in which h is no longer an active trigger.
		The facts that are generated during chasing are stored in a list.
	
		-Parallel EGD chase: Runs EGD chase using parallel chase steps.
	 	(From modern dependency theory notes)
 	 	A trigger for and EGD \delta = \sigma --> x_i = x_j in I is again a homomorphism h in
	 	\sigma into I. A trigger is active if it does not extend to a homomorphism h0 into I.
	 	Given trigger h
	 	for \delta in I, a chase pre-step marks the pair h(x_i) and h(x_j) as equal. Formally,
	 	it appends the pair h(x_i), h(x_j) to a set of pairs MarkedEqual.
	 	An EGD parallel chase step on instance I for a set of constraints C is performed
	 	as follows.
	 	i. A chase pre-step is performed for every constraint \delta in C and every active
		trigger h in I.
	 	ii. The resulting set of marked pairs is closed under reflexivity and transitivity
	 	to get an equivalence relation.
	 	iii. If we try to equate two different schema constants, then the chase fails. 
	 	The facts that are generated during chasing are stored in a list.
	 
	 	-Bounded chase and KTermination chase: Run the chase for k rounds.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class ReasonerFactory {

	/** The log. */
	protected static Logger log = Logger.getLogger(ReasonerFactory.class);

	/**  The event bus that will be shared by all instances create by this factory. */
	private final EventBus eventBus;

	/** If true, statistics are collected while the reasoner is used. */
	private final boolean collectStatistics;

	/**  Type of reasoner. */
	private final ReasoningTypes type;

	/**  K for the KTermination chase. */
	private final Integer terminationK;

	/**  KSupplier to be shared across all BoundedChasers created by this factory. */
	private KSupplier kSupplier = null;

	/** true, if the reasoner initialisation shall be unrestricted. */
	private final Boolean fullInitialization;


	/**
	 * Instantiates a new reasoner factory.
	 *
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param params the params
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
	 * Instantiates a new reasoner factory.
	 *
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param type the type
	 * @param k the k
	 * @param fullInitialization the full initialization
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
	 * Gets the single instance of ReasonerFactory.
	 *
	 * @return 		a fresh instance of reasoner. Currently, all reasoners
	 *      implement the Chaser interface.
	 */
	public Chaser getInstance() {
		switch (this.type) {
		case RESTRICTED_CHASE:
			return new RestrictedChaser(
					this.collectStatistics == true ? new StatisticsCollector(this.collectStatistics, this.eventBus) : null);
		case PARALLEL_EGD_CHASE:
			return new ParallelEGDChaser(
					this.collectStatistics == true ? new StatisticsCollector(this.collectStatistics, this.eventBus) : null);
			
		case SEQUENTIAL_EGD_CHASE:
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
		default:
			return null;
		}
	}

	/**
	 * Gets the event bus.
	 *
	 * @return the event bus associated with this factory
	 */
	public EventBus getEventBus() {
		return this.eventBus;
	}
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public ReasoningTypes getType() {
		return this.type;
	}
	
	/**
	 * Gets the collect statistics.
	 *
	 * @return the collect statistics
	 */
	public boolean getCollectStatistics() {
		return this.collectStatistics;
	}
}
