package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TriggerProperty;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance.LimitTofacts;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;


// TODO: Auto-generated Javadoc
/**
 * Runs the chase algorithm for a given set of rounds. 
 * First, it finds the maximum number of chase rounds k during chasing the input schema
 * and then it performs only k rounds of chasing during chasing the accessible schema.
 *
 * @author Efthymia Tsamoura
 *
 */
public class BoundedChaser extends RestrictedChaser {

	/** If true, the initialisation phase is not bounded by k. */
	private final boolean fullInitialization;

	/** Factor of number of rounds to chase before stopping. */
	private final KSupplier k;

	/** The number of rounds performed during the last call of chase. */
	private Integer lastRound;

	/**
	 * Instantiates a new bounded chaser.
	 *
	 * @param statistics the statistics
	 * @param k 		Factor of number of rounds to chase before stopping.
	 * @param fullInitialize 		If true, the initialisation phase is not bounded by k.
	 */
	public BoundedChaser(
			StatisticsCollector statistics,
			KSupplier k, 
			boolean fullInitialize) {
		super(statistics);
		Preconditions.checkArgument(k != null);
		this.k = k;
		this.fullInitialization = fullInitialize;
	}


	/**
	 * Initialize.
	 *
	 * @param instance the instance
	 * @param constraints the constraints
	 */
	public void initialize(
			ChaseInstance instance, 
			Collection<? extends Dependency> constraints) {
		synchronized (this.k) {
			int oldK = this.k.get();
			if (this.fullInitialization) {
				this.k.set(Integer.MAX_VALUE);
			}
			this.reasonUntilTermination(instance, constraints);
			this.k.set(Math.min(oldK, this.lastRound));
			this.k.freeze();
		}
	}

	/**
	 * Chases the input state until termination.
	 *
	 * @param <S> the generic type
	 * @param intance the intance
	 * @param dependencies the dependencies
	 */
	@Override
	public <S extends ChaseInstance> void reasonUntilTermination(S instance,  Collection<? extends Dependency> dependencies) {
		Preconditions.checkArgument(instance instanceof ChaseInstance);
		int rounds = 0;
		boolean appliedStep = true;
		while (rounds < this.k.get() && appliedStep) {
			appliedStep = false;
			List<Match> matches = instance.getTriggers(dependencies, TriggerProperty.ACTIVE, LimitTofacts.THIS);
			if(!matches.isEmpty()) {
				appliedStep = true;
			}
			if(appliedStep) {
				++rounds;
			}
		}
		this.lastRound = rounds;
	}


	/**
	 * Supplies the actual termination number to the bounded chase.
	 * This supplier allows delaying the determination of K, until after the
	 * Initialisation phase, and share the K value across distinct instances
	 * of BoundedChaser.
	 *
	 * @author Julien Leblay
	 */
	public static class KSupplier implements Supplier<Integer> {

		/**  If true, any future modification of the supplied value will throw an exception. */
		private boolean frozen = false;

		/**  The supplied integer. */
		private Integer k = null;

		/**
		 * Constructor for KSupplier.
		 * @param k int
		 */
		public KSupplier(int k) {
			Preconditions.checkArgument(k >= 0);
			this.k = k;
		}

		/**
		 * Forbids any further use of set().
		 */
		public void freeze() {
			this.frozen = true;
		}

		/**
		 * Sets the.
		 *
		 * @param k Integer
		 */
		public void set(Integer k) {
			Preconditions.checkState(!this.frozen, "Cannot set a frozen KSupplier");
			Preconditions.checkArgument(k != null);
			this.k = k;
		}

		/**
		 * Gets the.
		 *
		 * @return Integer
		 * @see com.google.common.base.Supplier#get()
		 */
		@Override
		public Integer get() {
			Preconditions.checkNotNull(this.k);
			return this.k;
		}
	}
}
