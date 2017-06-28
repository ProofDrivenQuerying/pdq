package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;

/**
 * A formula that contains no logical connectives.
 * An atomic formula is a formula of the form P (t_1, \ldots, t_n) for P a predicate, and the t_i terms.)
 *
 * @author Efthymia Tsamoura
 */
public class Atom extends Formula {
	private static final long serialVersionUID = 8284527612446931534L;

	/**
	 * The predicate of this atom.
	 */
	private final Predicate predicate;

	/**  The terms of this atom. */
	private final Term[] terms;

	/**   Cashed string representation of the atom. */
	protected String toString = null;
	
	/**  Cashed list of free variables. */
	private Variable[] freeVariables = null;

	
	private Atom(Predicate predicate, Term[] terms) {
		Assert.assertTrue("Predicate and terms list cannot be null. (predicate: " + predicate + ", terms:" + terms + ")", predicate != null && terms != null);
		this.predicate = predicate;
		this.terms = terms.clone();
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
		for(Integer i: positions) {
			t.add(this.terms[i]);
		}
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
		return new Atom[]{this};
	}
	
	/**
	 * Checks if is fact.
	 * TOCOMMENT: I GUESS THIS SHOULD be isGround()
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

	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder builder = new StringBuilder();
			builder.append(this.predicate.getName());
            if (this.terms.length > 0) {
                builder.append('(');
                for (int index = 0; index < this.terms.length; index++) {
                    if (index > 0)
                        builder.append(',');
                    builder.append(this.terms[index]).toString();
                }
                builder.append(')');
            }
		}
		return this.toString;
	}

	@Override
	public int getId() {
		return this.hashCode();
	}
	
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static final InterningManager<Atom> s_interningManager = new InterningManager<Atom>() {
        protected boolean equal(Atom object1, Atom object2) {
            if (!object1.predicate.equals(object2.predicate) || object1.terms.length != object2.terms.length)
                return false;
            for (int index = object1.terms.length - 1; index >= 0; --index)
                if (!object1.terms[index].equals(object2.terms[index]))
                    return false;
            return true;
        }

        protected int getHashCode(Atom object) {
            int hashCode = object.predicate.hashCode();
            for (int index = object.terms.length - 1; index >= 0; --index)
                hashCode = hashCode * 7 + object.terms[index].hashCode();
            return hashCode;
        }
    };

    public static Atom create(Predicate predicate, Term... arguments) {
        return s_interningManager.intern(new Atom(predicate, arguments));
    }
}
