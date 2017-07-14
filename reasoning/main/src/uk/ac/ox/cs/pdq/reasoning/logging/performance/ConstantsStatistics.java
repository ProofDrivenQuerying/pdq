package uk.ac.ox.cs.pdq.reasoning.logging.performance;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.logging.StatisticsLogger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

// TODO: Auto-generated Javadoc
/**
 * Keeps a set of constant observed so far and report on its cardinality.
 *
 * @author Julien Leblay
 */
public class ConstantsStatistics extends StatisticsLogger implements EventHandler {

	/**  The set of constants observed so far. */
	private Set<Constant> constants = new LinkedHashSet<>();

	/**
	 * Adds the given constants to the set of constants observed so far.
	 * @param c Set<Constant>
	 */
	public void addConstants(Set<Constant> c) {
		this.constants.addAll(c);
	}

	/**
	 * Gets the constants number.
	 *
	 * @return the number of constants observed so far.
	 */
	public int getConstantsNumber() {
		return this.constants.size();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.logging.performance.StatisticsLogger#makeHeader()
	 */
	@Override
	public String makeHeader() {
		StringBuilder result = new StringBuilder();
		result.append(ReasoningStatKeys.CONSTANTS).append(FIELD_SEPARATOR);
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.logging.performance.StatisticsLogger#makeLine()
	 */
	@Override
	public String makeLine() {
		StringBuilder result = new StringBuilder();
		result.append(this.constants.size()).append(FIELD_SEPARATOR);
		return result.toString();
	}

	/**
	 * Event-triggered, catches an increment i, contained some new constants,
	 * and adds them to the set.
	 *
	 * @param i the i
	 */
	@Subscribe
	public void process(Increment i) {
		this.constants.addAll(i.subset);
	}

	/**
	 * A ConstantStatistic increment is set of constant generally post through
	 * an event bus, carry a set of new constants to add to the existing ones.
	 *
	 * @author Julien Leblay
	 *
	 */
	public static class Increment {
		/** The set of new constant to add. */
		private final Collection<Constant> subset = new LinkedHashSet<>();
		
		/**
		 * Default constructor.
		 *
		 * @param subset the subset
		 */
		public Increment(Collection<Term> subset) {
			Preconditions.checkArgument(subset != null);
			for (Term t: subset) {
				if (t instanceof Constant) {
					this.subset.add((Constant) t);
				}
			}
		}
	}
}
