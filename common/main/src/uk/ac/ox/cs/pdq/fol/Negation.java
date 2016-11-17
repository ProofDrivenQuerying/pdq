package uk.ac.ox.cs.pdq.fol;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public final class Negation extends Formula {

	protected final Formula child;

	/**  The unary operator. */
	protected final LogicalSymbols operator = LogicalSymbols.NEGATION;

	/**  Cashed string representation of the atom. */
	private String toString = null;

	/** The hash. */
	private Integer hash;

	/**  Cashed list of atoms. */
	private List<Atom> atoms = null;

	/**  Cashed list of terms. */
	private List<Term> terms = null;

	/**  Cashed list of free variables. */
	private List<Variable> freeVariables = null;

	/**  Cashed list of bound variables. */
	private List<Variable> boundVariables = null;

	/**
	 * Constructor for Negation.
	 * @param sf T
	 */
	public Negation(Formula child) {
		Preconditions.checkArgument(child != null);
		this.child = child;
	}

	/**
	 * Convenience constructor for Negation.
	 *
	 * @param <T> the generic type
	 * @param f T
	 * @return Negation<T>
	 */
	public static Negation of(Formula f) {
		Preconditions.checkArgument(f != null);
		return new Negation(f);
	}

	@Override
	public List<Formula> getChildren() {
		return ImmutableList.of(this.child);
	}

	@Override
	public List<Atom> getAtoms() {
		if(this.atoms == null) {
			this.atoms = this.child.getAtoms();
		}
		return this.atoms;
	}

	@Override
	public List<Term> getTerms() {
		if(this.terms == null) {
			this.terms = this.child.getTerms();
		}
		return this.terms;
	}

	@Override
	public List<Variable> getFreeVariables() {
		if(this.freeVariables == null) {
			this.freeVariables = this.child.getFreeVariables();
		}
		return this.freeVariables;
	}

	@Override
	public List<Variable> getBoundVariables() {
		if(this.boundVariables == null) {
			this.boundVariables = this.child.getBoundVariables();
		}
		return this.boundVariables;
	}

	/**
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
				&& this.child.equals(((Negation) o).child);
	}


	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.operator, this.child);
		}
		return this.hash;
	}
	
	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			this.toString = "";
			this.toString += "(" + "~" + this.child.toString() + ")";
		}
		return this.toString;
	}

	@Override
	public int getId() {
		return this.hashCode();
	}
}