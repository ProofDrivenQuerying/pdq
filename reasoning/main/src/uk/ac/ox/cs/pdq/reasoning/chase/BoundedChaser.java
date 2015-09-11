package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.utility.ReasonerUtility;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;


/**
 * A bounded chase algorithm. This implementation
 * finds the maximum number of chase rounds k during chasing the input schema
 * and then performs only k rounds of chasing during chasing the accessible schema.
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
	 * 
	 * @param statistics
	 * @param k
	 * 		Factor of number of rounds to chase before stopping.
	 * @param fullInitialize
	 * 		If true, the initialisation phase is not bounded by k.
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
	 * 
	 * @param state
	 * @param target
	 * @param constraints
	 */
	public void initialize(
			ChaseState state, 
			Query<?> target, 
			Collection<? extends Constraint> constraints) {
		synchronized (this.k) {
			int oldK = this.k.get();
			if (this.fullInitialization) {
				this.k.set(Integer.MAX_VALUE);
			}
			this.reasonUntilTermination(state, target, constraints);
			this.k.set(Math.min(oldK, this.lastRound));
			this.k.freeze();
		}
	}

	/**
	 * Chases the input state until termination
	 * @param s
	 * @param target
	 * @param dependencies
	 */
	@Override
	public <S extends ChaseState> void reasonUntilTermination(S s,  Query<?> target, Collection<? extends Constraint> dependencies) {
		Preconditions.checkArgument(s instanceof ListState);
		int rounds = 0;
		boolean appliedStep = true;
		while (rounds < this.k.get() && appliedStep) {
			appliedStep = false;
			List<Match> matches = s.getMaches(dependencies);
			for (Match match: matches) {
				if(new ReasonerUtility().isActiveTrigger(match, s)){
					s.chaseStep(match);
					appliedStep = true;
				}
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

		/** If true, any future modification of the supplied value will throw an exception */
		private boolean frozen = false;

		/** The supplied integer */
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
		 * @param k Integer
		 */
		public void set(Integer k) {
			Preconditions.checkState(!this.frozen, "Cannot set a frozen KSupplier");
			Preconditions.checkArgument(k != null);
			this.k = k;
		}

		/**
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
