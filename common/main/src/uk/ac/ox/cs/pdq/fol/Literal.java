package uk.ac.ox.cs.pdq.fol;

import java.util.Collection;

import com.google.common.base.Preconditions;


/**
 * A positive or a negative atom
 *
 * @author Efthymia Tsamoura
 */
public class Literal extends Atom{

	protected final LogicalSymbols operator;
	
	/**
	 * Constructor for Atomic formula.
	 *
	 * @param predicate Predicate
	 * @param terms Collection<? extends Term>
	 */
	public Literal(Predicate predicate, Collection<? extends Term> terms) {
		super(predicate, terms);
		this.operator = null;
	}
	
	/**
	 * Instantiates a new atom.
	 *
	 * @param predicate Predicate
	 * @param term Term[]
	 */
	public Literal(Predicate predicate, Term... terms) {
		super(predicate, terms);
		this.operator = null;
	}
	
	/**
	 * Constructor for Atomic formula.
	 *
	 * @param predicate Predicate
	 * @param terms Collection<? extends Term>
	 */
	public Literal(LogicalSymbols operator, Predicate predicate, Collection<? extends Term> terms) {
		super(predicate, terms);
		Preconditions.checkArgument(operator == LogicalSymbols.NEGATION);
		this.operator = operator;
	}
	
	/**
	 * Instantiates a new atom.
	 *
	 * @param predicate Predicate
	 * @param term Term[]
	 */
	public Literal(LogicalSymbols operator, Predicate predicate, Term... terms) {
		super(predicate, terms);
		Preconditions.checkArgument(operator == LogicalSymbols.NEGATION);
		this.operator = operator;
	}
	
	public boolean isPositive() {
		return this.operator == null;
	}
	
	@Override
	public String toString() {
		if(this.toString == null) {
			this.toString = this.isPositive() == true ? super.toString() : "~" + super.toString();
		}
		return this.toString;
	}
	
}
