package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * TOCOMMENT we have both n-ary formulas and just formulas? I suggest we merge these.
 * A n-ary formula.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @param <T> Type of input formulas
 */
public abstract class NaryFormula<T extends Formula> extends AbstractFormula implements Iterable<T> {

	/**  The formula's top-level operator. */
	protected final LogicalSymbols operator;

	/**  The subformulae. */
	protected final ImmutableList<T> children;

	/**
	 * Instantiates a new nary formula.
	 *
	 * @param operator 		Input head operator
	 * @param childen 		Input subformulas
	 */
	public NaryFormula(LogicalSymbols operator, Collection<T> childen) {
		super();
		this.operator = operator;
		this.children = ImmutableList.copyOf(childen);
	}

	/**
	 * Gets the n subformulas of this n-ary formula.
	 *
	 * @return Collection<T>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getSubFormulas()
	 */
	@Override
	public Collection<T> getChildren() {
		return this.children;
	}

	@Override
	public List<Term> getTerms() {
		List<Term> terms = new ArrayList<>();
		for (Formula atomics:this.children) {
			terms.addAll(atomics.getTerms());
		}
		return terms;
	}

	@Override
	public List<Atom> getAtoms() {
		List<Atom> result = new ArrayList<>();
		for (Formula item: this.children) {
			result.addAll(item.getAtoms());
		}
		return result;
	}

	/**
	 * Two n-ary formulas are equal if their top-level operator and all subformulas are equal (using their corresponding equals method).
	 *
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

	@Override
	public int hashCode() {
		return Objects.hash(this.operator, this.children);
	}

	@Override
	public String toString() {
		return Joiner.on(this.operator.toString()).join(this.children);
	}


	public int size() {
		return this.children.size();
	}

	/**
	 * Checks if is empty.
	 *
	 * @return boolean
	 */
	public boolean isEmpty() {
		return this.children.isEmpty();
	}

	/**
	 * Gets the top-level operator of this formula.
	 *
	 * @return LogicalSymbols
	 */
	public LogicalSymbols getSymbol() {
		return this.operator;
	}

	/**
	 * Iterates over all subformulas of the top-level operator of this formula.
	 *
	 * @return Iterator<T>
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return this.children.iterator();
	}
}