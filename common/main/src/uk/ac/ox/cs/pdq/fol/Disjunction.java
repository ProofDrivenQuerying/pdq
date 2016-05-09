package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;


/**
 * A disjunction of formulas.
 *
 * @author Julien Leblay
 * @param <T> the generic type that extends formula
 */
public final class Disjunction<T extends Formula> extends NaryFormula<T> {

	/**
	 * Constructor for Disjunction.
	 * @param subFormulas Collection<T>
	 */
	private Disjunction(Collection<T> subFormulas) {
		super(LogicalSymbols.OR, subFormulas);
	}

	/**
	 * Constructor for Disjunction.
	 * @param subFormulas T[]
	 */
	private Disjunction(T... subFormulas) {
		super(LogicalSymbols.OR, Lists.newArrayList(subFormulas));
	}

	/**
	 * Convenience constructor for Disjunction.
	 *
	 * @param <T> the generic type
	 * @param subFormulas T[]
	 * @return Disjunction<T>
	 */
	public static <T extends Formula> Disjunction<T> of(T... subFormulas) {
		return new Disjunction<>(subFormulas);
	}

	/**
	 * Convenience constructor for Disjunction.
	 *
	 * @param <T> the generic type
	 * @param subFormulas Collection<T>
	 * @return Disjunction<T>
	 */
	public static <T extends Formula> Disjunction<T> of(Collection<T> subFormulas) {
		return new Disjunction<>(subFormulas);
	}


	@Override
	public Formula ground(Map<Variable, Constant> mapping) {
		List<T> result = new ArrayList<>(this.children.size());
		for (T p: this.children) {
			result.add((T) p.ground(mapping));
		}
		return Disjunction.of(result);
	}

	/**
	 * Construct a disjunction builder.
	 *
	 * @return a generic formula builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A simple builder for disjunctions.
	 *
	 * @author Julien Leblay
	 */
	public static class Builder implements uk.ac.ox.cs.pdq.builder.Builder<Disjunction<?>> {

		/** 
		 * TOCOMMENT ??
		 * The current. */
		private LinkedList<Formula> current = new LinkedList<>();

		/**
		 * Constructs a disjunction builder.
		 *
		 * @param disjuncts Formula[]
		 * @return Builder
		 */
		public Builder or(Formula... disjuncts) {
			return this.or(Lists.newArrayList(disjuncts));
		}

		/**
		 * Constructs a disjunction builder.
		 *
		 * @param disjuncts List<Formula>
		 * @return Builder
		 */
		public Builder or(List<Formula> disjuncts) {
			for (Formula f: disjuncts) {
				this.current.add(f);
			}
			return this;
		}

		/**
		 * Builds the disjunction.
		 *
		 * @return Disjunction<?>
		 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
		 */
		@Override
		public Disjunction<?> build() {
			assert this.current != null;
			return Disjunction.of(this.current);
		}
	}
}
