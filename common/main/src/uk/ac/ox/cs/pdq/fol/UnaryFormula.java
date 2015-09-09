package uk.ac.ox.cs.pdq.fol;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;


/**
 * A unary formula
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public abstract class UnaryFormula<T extends Formula> extends AbstractFormula {

	/** The subformula */
	protected final T child;
	protected final Collection<T> children;


	/** The unary operator*/
	protected final LogicalSymbols operator;

	/**
	 * Constructor for UnaryFormula.
	 * @param operator LogicalSymbols
	 * @param subFormula T
	 */
	public UnaryFormula(LogicalSymbols operator, T subFormula) {
		super();
		Preconditions.checkArgument(subFormula != null);
		this.child = subFormula;
		this.children = ImmutableList.of(this.child);
		this.operator = operator;
	}

	/**
	 * @return the operator of this formula
	 */
	public LogicalSymbols getSymbol() {
		return this.operator;
	}

	/**
	 * @return List<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getPredicates()
	 */
	@Override
	public List<Predicate> getPredicates() {
		return this.child.getPredicates();
	}

	/**
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getTerms()
	 */
	@Override
	public List<Term> getTerms() {
		return this.child.getTerms();
	}

	/**
	 * @return T
	 */
	public T getChild() {
		return this.child;
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
	 * @return String
	 */
	@Override
	public String toString() {
		return this.operator + "(" + this.child + ")";
	}
}
