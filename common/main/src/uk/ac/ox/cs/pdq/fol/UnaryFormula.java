package uk.ac.ox.cs.pdq.fol;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * A unary formula.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @param <T> the generic type
 */
public abstract class UnaryFormula<T extends Formula> extends AbstractFormula {

	/**  
	 * TOCOMMENT having both child and children needs synchronizing.
	 * 
	 * The subformula. */
	protected final T child;
	
	/** The children. */
	protected final Collection<T> children;


	/**  The unary operator. */
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
	 * Gets the top-level symbol.
	 *
	 * @return the operator of this formula
	 */
	public LogicalSymbols getSymbol() {
		return this.operator;
	}

	@Override
	public List<Atom> getAtoms() {
		return this.child.getAtoms();
	}

	@Override
	public List<Term> getTerms() {
		return this.child.getTerms();
	}

	/**
	 * TOCOMMENT see comment in the fields above
	 * 
	 * Gets the child.
	 *
	 * @return T
	 */
	public T getChild() {
		return this.child;
	}

	/**
	 * Gets the children subformulas.
	 *
	 * @return Collection<T>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getSubFormulas()
	 */
	@Override
	public Collection<T> getChildren() {
		return this.children;
	}

	@Override
	public String toString() {
		return this.operator + "(" + this.child + ")";
	}
}
