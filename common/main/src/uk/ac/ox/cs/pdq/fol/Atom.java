package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.AtomAdapter;

/**
 * A formula that contains no logical connectives.
 * An atomic formula is a formula of the form P (t_1, \ldots, t_n) for P a predicate, and the t_i terms.)
 *
 * @author Efthymia Tsamoura
 */
@XmlJavaTypeAdapter(AtomAdapter.class)
public class Atom extends Formula {
	private static final long serialVersionUID = 8284527612446931534L;

	/**
	 * The predicate of this atom.
	 */
	protected final Predicate predicate;

	/**  The terms of this atom. */
	protected final Term[] terms;

	/**   Cashed string representation of the atom. */
	protected String toString = null;
	
	/**  Cashed list of free variables. */
	private Variable[] freeVariables = null;

	
	private Atom(Predicate predicate, Term[] terms) {
		Preconditions.checkArgument(predicate != null && terms != null,"Predicate and terms list cannot be null. (predicate: " + predicate + ", terms:" + terms + ")");
		Preconditions.checkArgument(predicate.getArity() == terms.length,"Predicate arity not equal to number of terms");
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
	 * @param termIndex int
	 * @return the atom's n-th term
	 */
	public Term getTerm(int termIndex) {
		return this.terms[termIndex];
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
	 * @return Boolean
	 */
	public Boolean isGround() {
		for(Term term:this.terms) {
			if(term instanceof Variable) {
				return false;
			}
		}
		return true;
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
            this.toString = builder.toString();
		}
		return this.toString;
	}

	@Override
	public int getId() {
		return this.hashCode();
	}
	
    public static Atom create(Predicate predicate, Term... arguments) {
        return Cache.atom.retrieve(new Atom(predicate, arguments));
    }
    
    public static Atom create(Relation predicate, Term... arguments) {
        return Cache.atom.retrieve(new Atom(Predicate.create(predicate.getName(),predicate.getArity(),predicate.isEquality()) , arguments));
    }
    
    /** 
     * xml parsing will fail if we return the same reference
     */
    public static Atom createFromXml(Predicate predicate, Term... arguments) {
        return new Atom(predicate, arguments);
    }
    
	/**
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
	public Formula getChild(int childIndex) {
		return null;
	}

	@Override
	public int getNumberOfChildren() {
		return 0;
	}
	
	public int getNumberOfTerms() {
		return this.terms.length;
	}
}
