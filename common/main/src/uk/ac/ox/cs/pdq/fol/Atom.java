package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * TOCOMMENT find a pretty way to write formulas in javadoc
 * A formula that contains no logical connectives.
 * An atomic formula is a formula of the form P (t_1, \ldots, t_n) for P a predicate, and the t_i terms.)
 *
 * @author Efthymia Tsamoura
 */
public class Atom extends Formula {

	/**
	 * The predicate of this atom.
	 */
	private final Predicate predicate;

	/**  The terms of this atom. */
	private final List<Term> terms;

	/**   Cashed string representation of the atom. */
	protected String toString = null;

	private Integer hash = null;
	
	/**  Cashed list of free variables. */
	private List<Variable> freeVariables = null;

	/**
	 * Constructor for Atomic formula.
	 *
	 * @param predicate Predicate
	 * @param terms Collection<? extends Term>
	 */
	public Atom(Predicate predicate, Collection<? extends Term> terms) {
		Preconditions.checkArgument(predicate != null && terms != null,
				"Predicate and terms list cannot be null. (predicate: " + predicate + ", terms:" + terms + ")");
//      The following precondition is not true anymore since for chasing purposes we might append an atom with extra attributes
//		Preconditions.checkArgument(predicate.getArity() == terms.size(),
//				"Atom predicate does not match terms lists " + predicate.getName()
//				+ "(" + predicate.getArity() + ") <> " + terms);
		this.predicate = predicate;
		this.terms = ImmutableList.copyOf(terms);
	}
	
	/**
	 * Instantiates a new atom.
	 *
	 * @param predicate Predicate
	 * @param term Term[]
	 */
	public Atom(Predicate predicate, Term... terms) {
		this(predicate, Lists.newArrayList(terms));
	}

	/**
	 * Checks if this is an equality atom.
	 *
	 * @return true, if the atom acts as an equality
	 */
	public boolean isEquality() {
		return this.predicate.isEquality();
	}

	/**
	 * Gets the atom's predicate.
	 *
	 * @return the atom's predicate
	 */
	public Predicate getPredicate() {
		return this.predicate;
	}
	
	/**
	 * Gets the term at the input position.
	 *
	 * @param n int
	 * @return the atom's n-th term
	 */
	public Term getTerm(int n) {
		return this.terms.get(n);
	}

	/**
	 * Gets the terms of this atom.
	 *
	 * @return the list of terms
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getTerms()
	 */
	@Override
	public List<Term> getTerms() {
		return this.terms;
	}


	/**
	 * Gets only the terms at the specified input positions.
	 *
	 * @param positions List<Integer>
	 * @return the Set<Term> at the given positions.
	 */
	public Set<Term> getTerms(List<Integer> positions) {
		Set<Term> t = new LinkedHashSet<>();
		for(Integer i: positions) {
			t.add(this.terms.get(i));
		}
		return t;
	}

	/**
	 * Gets the variables.
	 *
	 * @return List<Variable>
	 */
	public List<Variable> getVariables() {
		return this.getFreeVariables();
	}

	/**
	 * Gets the predicates.
	 *
	 * @return List<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getAtoms()
	 */
	@Override
	public List<Atom> getAtoms() {
		return Lists.newArrayList(this);
	}

	/**
	 * Gets the term positions.
	 *
	 * @param term Term
	 * @return List<Integer>
	 */
	public List<Integer> getTermPositions(Term term) {
		return Utility.search(this.terms, term);
	}

	/**
	 * Checks if is fact.
	 *
	 * @return Boolean
	 */
	public Boolean isFact() {
		for(Term term:this.terms) {
			if(term instanceof Variable) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the children.
	 *
	 * @return Collection<Formula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getSubFormulas()
	 */
	@Override
	public List<Formula> getChildren() {
		return ImmutableList.of();
	}

	@Override
	public List<Variable> getFreeVariables() {
		if(this.freeVariables == null) {
			this.freeVariables = new ArrayList<>();
			Set<Variable> freeVariablesSet = Sets.newHashSet();
			for (Term term: this.terms) {
				if(term instanceof Variable) {
					freeVariablesSet.add((Variable) term);
				}
				else if(term instanceof FunctionTerm) {
					freeVariablesSet.addAll(((FunctionTerm) term).getVariables());
				}
			}
			this.freeVariables.addAll(freeVariablesSet);
		}
		return this.freeVariables;
	}

	@Override
	public List<Variable> getBoundVariables() {
		return ImmutableList.of();
	}
	
	/**
	 * Equals.
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
				&& this.predicate.equals(((Atom) o).predicate)
				&& this.terms.equals(((Atom) o).terms);
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.terms, this.predicate);
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
			this.toString = this.predicate.getName() + (this.predicate.arity > 0 ? "(" + Joiner.on(",").join(this.terms) + ")" : "");
		}
		return this.toString;
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
}
