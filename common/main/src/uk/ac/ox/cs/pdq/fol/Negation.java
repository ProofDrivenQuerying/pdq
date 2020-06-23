// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.fol;

import org.junit.Assert;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public final class Negation extends Formula {

	private static final long serialVersionUID = 2571574465306118274L;

	protected final Formula child;

	/**  The unary operator. */
	protected final LogicalSymbols operator = LogicalSymbols.NEGATION;

	/**  Cached string representation of the atom. */
	private String toString = null;

	/**  Cached list of atoms. */
	private Atom[] atoms;

	/**  Cached list of terms. */
	private Term[] terms;

	/**  Cached list of free variables. */
	private Variable[] freeVariables;

	/**  Cached list of bound variables. */
	private Variable[] boundVariables;

	/**
	 * Constructor for Negation.
	 * @param sf T
	 */
	private Negation(Formula child) {
		Assert.assertNotNull(child);
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
		Assert.assertNotNull(f);
		return Negation.create(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Formula[] getChildren() {
		return new Formula[]{this.child};
	}

	@Override
	public Atom[] getAtoms() {
		if(this.atoms == null) 
			this.atoms = this.child.getAtoms();
		return this.atoms.clone();
	}

	@Override
	public Term[] getTerms() {
		if(this.terms == null) 
			this.terms = this.child.getTerms();
		return this.terms.clone();
	}

	@Override
	public Variable[] getFreeVariables() {
		if(this.freeVariables == null) 
			this.freeVariables = this.child.getFreeVariables();
		return this.freeVariables.clone();
	}

	@Override
	public Variable[] getBoundVariables() {
		if(this.boundVariables == null) 
			this.boundVariables = this.child.getBoundVariables();
		return this.boundVariables.clone();
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
	
    public static Negation create(Formula child) {
        return Cache.negation.retrieve(new Negation(child));
    }
    
	@Override
	public Formula getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}

	@Override
	public int getNumberOfChildren() {
		return 1;
	}
}
