// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

/**
 * A positive or a negative atom
 *
 * @author Efthymia Tsamoura
 */
public class Literal extends Formula{

	private static final long serialVersionUID = -8815583036643488336L;

	protected final LogicalSymbols operator;
	
	/**
	 * The predicate of this atom.
	 */
	protected final Predicate predicate;

	/**  The terms of this atom. */
	protected final Term[] terms;

	/**   Cached string representation of the atom. */
	protected String toString = null;
	
	/**  Cached list of free variables. */
	private Variable[] freeVariables;
	
	private final Atom atom;

	private Literal(Predicate predicate, Term... terms) {
		this(null, predicate, terms);
	}
	
	private Literal(LogicalSymbols operator, Predicate predicate, Term... terms) {
		Assert.assertTrue("Predicate and terms list cannot be null. (predicate: " + predicate + ", terms:" + terms + ")", predicate != null && terms != null);
		Assert.assertTrue("Atom predicate does not match terms lists " + predicate.getName() + "(" + predicate.getArity() + ") <> " + terms, 
				predicate.getArity() == terms.length);
		this.predicate = predicate;
		this.terms = terms.clone();
		this.operator = operator;
		this.atom = Atom.create(this.predicate, this.terms);
	}
	
	public boolean isPositive() {
		return this.operator == null;
	}
	
	@Override
	public String toString() {
		if(this.toString == null) 
			this.toString = this.isPositive() == true ? super.toString() : "~" + super.toString();
		return this.toString;
	}
	
	/**
	 * Gets the term at the input position.
	 *
	 * @param n int
	 * @return the atom's n-th term
	 */
	public Term getTerm(int n) {
		return this.terms[n];
	}

	/**
	 * Gets the terms of this atom.
	 *
	 * @return the list of terms
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getTerms()
	 */
	@Override
	public Term[] getTerms() {
		return this.terms.clone();
	}


	/**
	 * Gets only the terms at the specified input positions.
	 *
	 * @param positions List<Integer>
	 * @return the Set<Term> at the given positions.
	 */
	public Set<Term> getTerms(List<Integer> positions) {
		Set<Term> t = new LinkedHashSet<>();
		for(Integer i: positions) 
			t.add(this.terms[i]);
		return t;
	}

	/**
	 * Gets the variables.
	 *
	 * @return List<Variable>
	 */
	public Variable[] getVariables() {
		return this.getFreeVariables();
	}

	/**
	 * Gets the predicates.
	 *
	 * @return List<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getAtoms()
	 */
	@Override
	public Atom[] getAtoms() {
		return new Atom[]{this.atom};
	}

	/**
	 * Checks if is fact.
	 *
	 * @return Boolean
	 */
	public Boolean isFact() {
		for(Term term:this.terms) {
			if(term instanceof Variable) 
				return false;
		}
		return true;
	}

	/**
	 * Gets the children.
	 *
	 * @return Collection<Formula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getSubFormulas()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Formula[] getChildren() {
		return new Formula[]{};
	}

	@Override
	public Variable[] getFreeVariables() {
		if(this.freeVariables == null) {
			Set<Variable> freeVariablesSet = new LinkedHashSet<>();
			for (Term term: this.terms) {
				if(term instanceof Variable) 
					freeVariablesSet.add((Variable) term);
				else if(term instanceof FunctionTerm) 
					freeVariablesSet.addAll(Arrays.asList(((FunctionTerm) term).getVariables()));
			}
			this.freeVariables = freeVariablesSet.toArray(new Variable[freeVariablesSet.size()]);
		}
		return this.freeVariables.clone();
	}

	@Override
	public Variable[] getBoundVariables() {
		return new Variable[]{};
	}

	/**
	 * Gets the id.
	 *
	 * @return int
	 */
	@Override
	public int getId() {
		return this.hashCode();
	}
	
    public static Literal create(Predicate predicate, Term... arguments) {
        return Cache.literal.retrieve(new Literal(predicate, arguments));
    }
    
    public static Literal create(LogicalSymbols operator, Predicate predicate, Term... arguments) {
        return Cache.literal.retrieve(new Literal(operator, predicate, arguments));
    }
    
	@Override
	public Formula getChild(int childIndex) {
		return null;
	}

	@Override
	public int getNumberOfChildren() {
		return 0;
	}
	
}
