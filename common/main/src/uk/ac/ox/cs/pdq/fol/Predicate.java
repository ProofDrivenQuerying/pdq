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
 * A formula that contains no logical connectives.
 * An atomic formula is a formula of the form P (t_1, \ldots, t_n) for P a predicate, and the t_i terms.)
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Predicate extends AbstractFormula implements Formula {

	/**
	 * The signature of the predicate of this atom.
	 * A signature bridges the predicate with the real-word relation it comes from.
	 * If it does not correspond to a real-word relation, the signature is a dummy one.
	 */
	private final Signature signature;

	/** Signature's name */
	private final String name;

	/** Signature's arity */
	private final int arity;

	/** The terms of this atom */
	private final List<Term> terms;

	/**  Cashed string representation of the atom */
	private final String toString;

	private Integer hash;

	/**
	 * Constructor for Atomic formulae
	 * @param signature Signature
	 * @param terms Collection<? extends Term>
	 */
	public Predicate(Signature signature, Collection<? extends Term> terms) {
		super();
		Preconditions.checkArgument(signature != null && terms != null,
				"Signature and terms list cannot be null. (signature: " + signature + ", terms:" + terms + ")");
		Preconditions.checkArgument(signature.getArity() == terms.size(),
				"Predicate signature does not match terms lists " + signature.getName()
				+ "(" + signature.getArity() + ") <> " + terms);

		this.signature = signature;
		this.name = signature.getName();
		this.arity = signature.getArity();
		this.terms = ImmutableList.copyOf(terms);
		this.toString = this.signature.getName() +
				(this.signature.arity > 0 ? "(" + Joiner.on(",").join(this.terms) + ")" : "");

	}
	
	/**
	 * @param signature Signature
	 * @param term Term[]
	 */
	public Predicate(Signature signature, Term... term) {
		this(signature, Lists.newArrayList(term));
	}

	/**
	 * @return true, if the atom acts as an equality
	 */
	public boolean isEquality() {
		return this.signature.isEquality();
	}

	/**
	 * @return the atom's signature
	 */
	public Signature getSignature() {
		return this.signature;
	}


	/**
	 * @return the atom's predicate
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the atom's arity
	 */
	public int getTermsCount() {
		return this.arity;
	}

	/**
	 * @param n int
	 * @return the atom's n-th term
	 */
	public Term getTerm(int n) {
		return this.terms.get(n);
	}

	/**
	 * @return the list of terms
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getTerms()
	 */
	@Override
	public List<Term> getTerms() {
		return this.terms;
	}


	/**
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

	/**
	 * @return List<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getPredicates()
	 */
	@Override
	public List<Predicate> getPredicates() {
		return ImmutableList.of(this);

	}

	/**
	 * @param mapping Map<Variable,Term>
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
	 */
	@Override
	public Predicate ground(Map<Variable, Constant> mapping) {
		List<Term> nterms = new ArrayList<>();
		for (Term term: this.terms) {
			if (term.isVariable() && mapping.containsKey(term)) {
				nterms.add(mapping.get(term));
			} else {
				nterms.add(term);
			}
		}
		return new Predicate(this.signature, nterms);
	}

	/**
	 * @param term Term
	 * @return List<Integer>
	 */
	public List<Integer> getTermPositions(Term term) {
		return Utility.search(this.terms, term);
	}

	/**
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
				&& this.signature.equals(((Predicate) o).signature)
				&& this.terms.equals(((Predicate) o).terms);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.terms, this.signature);
		}
		return this.hash;
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.toString;
	}

	/**
	 * @return int
	 */
	@Override
	public int getId() {
		return this.hashCode();
	}

	/**
	 * @param signature Signature
	 * @param tuple Tuple
	 * @return PredicateFormula
	 */
	public static Predicate makeFact(Signature signature, Tuple tuple) {
		TypedConstant[] terms = new TypedConstant[tuple.size()];
		for (int i = 0, l = tuple.size(); i < l; i++) {
			terms[i++] = new TypedConstant<>(tuple.getValue(i));
		}
		return new Predicate(signature, terms);
	}
	/**
	 * @return a generic formula builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 */
	public static class Builder implements uk.ac.ox.cs.pdq.builder.Builder<Predicate> {

		private Collection<Term> terms = new LinkedList<>();
		private Signature signature = null;

		/**
		 * @param signature Signature
		 * @return Builder
		 */
		public Builder setSignature(Signature signature) {
			assert signature != null;
			this.signature = signature;
			return this;
		}

		/**
		 * @param t Term
		 * @return Builder
		 */
		public Builder addTerm(Term t) {
			assert t != null;
			this.terms.add(t);
			return this;
		}

		/**
		 * @return the number of terms added so far.
		 */
		public int getTermCount() {
			return this.terms.size();
		}

		/**
		 * @return PredicateFormula
		 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
		 */
		@Override
		public Predicate build() {
			assert this.signature != null;
			assert this.terms.size() == this.signature.getArity();
			return new Predicate(this.signature, this.terms);
		}

	}

	/**
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
	public static Predicate TAUTOLOGY = new Predicate(new Signature(LogicalSymbols.TOP.toString(), 0)) ;

	/**
	 * Static reference to the contradiction formula.
	 *
	 * @author Julien Leblay
	 */
	public static Predicate CONTRADICTION = new Predicate(new Signature(LogicalSymbols.BOTTOM.toString(), 0));
}
