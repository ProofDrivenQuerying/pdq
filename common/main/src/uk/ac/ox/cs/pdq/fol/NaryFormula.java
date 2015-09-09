package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * A n-ary formula
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 * @param <T> Type of input formulas
 */
public abstract class NaryFormula<T extends Formula> extends AbstractFormula implements Iterable<T> {

	/** The formula's head operator*/
	protected final LogicalSymbols operator;

	/** The subformulae */
	protected final ImmutableList<T> children;

	/**
	 *
	 * @param operator
	 * 		Input head operator
	 * @param childen
	 * 		Input subformulas
	 */
	public NaryFormula(LogicalSymbols operator, Collection<T> childen) {
		super();
		this.operator = operator;
		this.children = ImmutableList.copyOf(childen);
	}

	/**
	 * @return Collection<T>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getSubFormulas()
	 */
	@Override
	public Collection<T> getChildren() {
		return this.children;
	}

	/**
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getTerms()
	 */
	@Override
	public List<Term> getTerms() {
		List<Term> terms = new ArrayList<>();
		for (Formula atomics:this.children) {
			terms.addAll(atomics.getTerms());
		}
		return terms;
	}

	/**
	 * @return List<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getPredicates()
	 */
	@Override
	public List<Predicate> getPredicates() {
		List<Predicate> result = new ArrayList<>();
		for (Formula item: this.children) {
			result.addAll(item.getPredicates());
		}
		return result;
	}

	/**
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.operator.equals(((NaryFormula<T>) o).operator)
				&& this.children.equals(((NaryFormula<T>) o).children);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.operator, this.children);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return Joiner.on(this.operator.toString()).join(this.children);
	}

	/**
	 * @return int
	 */
	public int size() {
		return this.children.size();
	}

	/**
	 * @return boolean
	 */
	public boolean isEmpty() {
		return this.children.isEmpty();
	}

	/**
	 * @return LogicalSymbols
	 */
	public LogicalSymbols getSymbol() {
		return this.operator;
	}

	/**
	 * @return Iterator<T>
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return this.children.iterator();
	}
}