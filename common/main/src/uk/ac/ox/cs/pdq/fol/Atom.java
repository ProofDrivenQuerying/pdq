package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * TOCOMMENT find a pretty way to write formulas in javadoc
 * A formula that contains no logical connectives.
 * An atomic formula is a formula of the form P (t_1, \ldots, t_n) for P a predicate, and the t_i terms.)
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Atom extends AbstractFormula implements Formula {

	/**
	 * The predicate of this atom.
	 * A predicate bridges the atom with the real-word relation it comes from.
	 * If it does not correspond to a real-word relation, the predicate is a dummy one.
	 */
	private final Predicate predicate;

	/**  Predicate's name. */
	private final String name;

	/**  Predicate's arity. */
	private final int arity;

	/**  The terms of this atom. */
	private final List<Term> terms;

	/**   Cashed string representation of the atom. */
	private final String toString;

	private Integer hash;

	/**
	 * Constructor for Atomic formulae.
	 *
	 * @param predicate Predicate
	 * @param terms Collection<? extends Term>
	 */
	public Atom(Predicate predicate, Collection<? extends Term> terms) {
		super();
		Preconditions.checkArgument(predicate != null && terms != null,
				"Predicate and terms list cannot be null. (predicate: " + predicate + ", terms:" + terms + ")");
		Preconditions.checkArgument(predicate.getArity() == terms.size(),
				"Atom predicate does not match terms lists " + predicate.getName()
				+ "(" + predicate.getArity() + ") <> " + terms);

		this.predicate = predicate;
		this.name = predicate.getName();
		this.arity = predicate.getArity();
		this.terms = ImmutableList.copyOf(terms);
		this.toString = this.predicate.getName() +
				(this.predicate.arity > 0 ? "(" + Joiner.on(",").join(this.terms) + ")" : "");

	}
	
	/**
	 * Instantiates a new atom.
	 *
	 * @param predicate Predicate
	 * @param term Term[]
	 */
	public Atom(Predicate predicate, Term... term) {
		this(predicate, Lists.newArrayList(term));
	}

	/**
	 * Checks if this is an equality atom.
	 *
	 * @return true, if the atom acts as an equality
	 */
	public boolean isEquality() {
		return (this instanceof Equality);
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
	 * TOCOMMMENT I guess this is the predicate name of the atom's predicate.
	 * However both Atom and Predicate have a name field. Are these the same? Are they kept in sync? Maybe one should go.
	 *
	 * @return the atom's 
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the atom's terms count.
	 *
	 * @return the atom's arity
	 */
	public int getTermsCount() {
		return this.arity;
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
	 * Gets the constants lying at the input positions.
	 *
	 * @throws IllegalArgumentException if there is a non-constant at one of the input positions
	 * @param positions List<Integer>
	 * @return the List<Constant> at the given positions.
	 */
	public List<Constant> getConstants(List<Integer> positions) {
		List<Constant> result = new ArrayList<>();
		for(Integer i: positions) {
			if(i < this.terms.size() && !this.terms.get(i).isVariable()) {
				result.add((Constant) this.terms.get(i));
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
		}
		return result;
	}

	/**
	 * Gets the variables.
	 *
	 * @return List<Variable>
	 */
	public List<Variable> getVariables() {
		List<Variable> result = new ArrayList<>();
		for (Term term: this.terms) {
			if(term instanceof Variable) {
				result.add((Variable) term);
			}
		}
		return result;
	}

	/**
	 * Gets the constants.
	 *
	 * @return Collection<Constant>
	 */
	public Collection<Constant> getConstants() {
		Set<Constant> result = new LinkedHashSet<>();
		for (Term term: this.terms) {
			if (!term.isVariable()) {
				result.add((Constant) term);
			}
		}
		return result;
	}

	/**
	 * Gets the schema constants.
	 *
	 * @return Collection<TypedConstant<?>>
	 */
	public Collection<TypedConstant<?>> getSchemaConstants() {
		Set<TypedConstant<?>> result = new LinkedHashSet<>();
		for (Term term: this.terms) {
			if (term instanceof TypedConstant) {
				result.add((TypedConstant) term);
			}
		}
		return result;
	}
	
	public List<TypedConstant<?>> getSchemaConstantsList() {
		List<TypedConstant<?>> result = Lists.newArrayList();
		for (Term term: this.terms) {
			if (term instanceof TypedConstant) {
				result.add((TypedConstant) term);
			}
		}
		return result;
	}

	/**
	 * Gets the predicates.
	 *
	 * @return List<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getAtoms()
	 */
	@Override
	public List<Atom> getAtoms() {
		return ImmutableList.of(this);

	}

	/**
	 * Ground.
	 *
	 * @param mapping Map<Variable,Term>
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
	 */
	@Override
	public Atom ground(Map<Variable, Constant> mapping) {
		List<Term> nterms = new ArrayList<>();
		for (Term term: this.terms) {
			if (term.isVariable() && mapping.containsKey(term)) {
				nterms.add(mapping.get(term));
			} else {
				nterms.add(term);
			}
		}
		return new Atom(this.predicate, nterms);
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

	/**
	 * Make fact.
	 *
	 * @param predicate Predicate
	 * @param tuple Tuple
	 * @return PredicateFormula
	 */
	public static Atom makeFact(Predicate predicate, Tuple tuple) {
		TypedConstant[] terms = new TypedConstant[tuple.size()];
		for (int i = 0, l = tuple.size(); i < l; i++) {
			terms[i++] = new TypedConstant<>(tuple.getValue(i));
		}
		return new Atom(predicate, terms);
	}
	
	/**
	 * Builder.
	 *
	 * @return a generic formula builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * The Class Builder.
	 */
	public static class Builder implements uk.ac.ox.cs.pdq.builder.Builder<Atom> {

		/** The terms. */
		private Collection<Term> terms = new LinkedList<>();
		
		/** The predicate. */
		private Predicate predicate = null;

		/**
		 * Sets the predicate.
		 *
		 * @param predicate Predicate
		 * @return Builder
		 */
		public Builder setSignature(Predicate predicate) {
			assert predicate != null;
			this.predicate = predicate;
			return this;
		}

		/**
		 * Adds the term.
		 *
		 * @param t Term
		 * @return Builder
		 */
		public Builder addTerm(Term t) {
			assert t != null;
			this.terms.add(t);
			return this;
		}

		/**
		 * Gets the term count.
		 *
		 * @return the number of terms added so far.
		 */
		public int getTermCount() {
			return this.terms.size();
		}

		/**
		 * Builds the.
		 *
		 * @return PredicateFormula
		 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
		 */
		@Override
		public Atom build() {
			assert this.predicate != null;
			assert this.terms.size() == this.predicate.getArity();
			return new Atom(this.predicate, this.terms);
		}

	}

	/**
	 * Gets the children.
	 *
	 * @return Collection<Formula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getSubFormulas()
	 */
	@Override
	public Collection<Formula> getChildren() {
		return ImmutableList.of();
	}

	/**
	 * Static reference to the tautology formula.
	 *
	 * @author Julien Leblay
	 */
	public static Atom TAUTOLOGY = new Atom(new Predicate(LogicalSymbols.TOP.toString(), 0)) ;

	/**
	 * Static reference to the contradiction formula.
	 *
	 * @author Julien Leblay
	 */
	public static Atom CONTRADICTION = new Atom(new Predicate(LogicalSymbols.BOTTOM.toString(), 0));
}
