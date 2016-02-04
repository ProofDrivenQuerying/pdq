package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * A conjunctive formula
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 * @param <T>
 */
public final class Conjunction<T extends Formula> extends NaryFormula<T> {

	/**
	 * Constructor for Conjunction.
	 * @param children Collection<T>
	 */
	private Conjunction(Collection<T> children) {
		super(LogicalSymbols.AND, children);
	}

	/**
	 * Constructor for Conjunction.
	 * @param children T[]
	 */
	private Conjunction(T... children) {
		super(LogicalSymbols.AND, Lists.newArrayList(children));
	}

	/**
	 * @param children T[]
	 * @return Conjunction<T>
	 */
	public static <T extends Formula> Conjunction<T> of(T... children) {
		return new Conjunction<>(children);
	}

	/**
	 * @param children Collection<T>
	 * @return Conjunction<T>
	 */
	public static <T extends Formula> Conjunction<T> of(Collection<T> children) {
		return new Conjunction<>(children);
	}

	/**
	 * @param mapping Map<Variable,Term>
	 * @return Formula
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
	 */
	@Override
	public Conjunction<T> ground(Map<Variable, Constant> mapping) {
		List<T> result = new ArrayList<>(this.children.size());
		for (T p: this.children) {
			result.add((T) p.ground(mapping));
		}
		return Conjunction.of(result);
	}

	/**
	 * @return a generic formula builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A simple builder for conjunctions.
	 *
	 * @author Julien Leblay
	 */
	public static class Builder implements uk.ac.ox.cs.pdq.builder.Builder<Conjunction<?>> {

		private LinkedList<Formula> current = new LinkedList<>();

		/**
		 * @param conjuncts Formula[]
		 * @return Builder
		 */
		public Builder and(Formula... conjuncts) {
			return this.and(Lists.newArrayList(conjuncts));
		}

		/**
		 * @param conjuncts List<Formula>
		 * @return Builder
		 */
		public Builder and(List<Formula> conjuncts) {
			for (Formula f: conjuncts) {
				this.current.add(f);
			}
			return this;
		}

		/**
		 * @return Conjunction<?>
		 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
		 */
		@Override
		public Conjunction<?> build() {
			assert this.current != null;
			return Conjunction.of(this.current);
		}
	}
}
