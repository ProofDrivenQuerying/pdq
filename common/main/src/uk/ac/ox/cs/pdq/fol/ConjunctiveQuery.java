package uk.ac.ox.cs.pdq.fol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.util.CanonicalNameGenerator;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

//TODO fix the comments
/**
 * TOCOMMENT find a pretty way to write formulas in javadoc
 * A conjunctive query (CQ) is a first order formula of the form \exists x_1, \ldots, x_n \Wedge A_i,
 * where A_i are atoms with arguments that are either variables or constants.

 * @author Efthymia Tsamoura
 *
 */
public class ConjunctiveQuery extends Formula {

	protected final Formula child;
	
	/** The hash. */
	private Integer hash;

	/**  Cashed list of atoms. */
	private List<Atom> atoms = null;

	/**  Cashed list of terms. */
	private List<Term> terms = null;
	
	/**  Cashed string representation of the atom. */
	private String toString = null;

	/** 
	 * TOCOMMENT we should get rid of this when we fix #42
	 * 
	 * The grounding. */
	protected Map<Variable, Constant> canonicalSubstitution;
	
	/**  
	 * TOCOMMENT we should get rid of this when we fix #42, together with the grounding field a few lines below, they are very confusing.
	 * 
	 * Map of query's free variables to chase constants. */
	protected Map<Variable, Constant> canonicalSubstitutionOfFreeVariables;
	
	/**  Cashed list of free variables. */
	private List<Variable> freeVariables = null;

	/**  Cashed list of bound variables. */
	private List<Variable> boundVariables = null;

	
	/**
	 * Builds a query given a set of free variables and its conjunction.
	 * The query is grounded using the input mapping of variables to constants.
	 */
	public ConjunctiveQuery(List<Variable> freeVariables, Conjunction child, Map<Variable, Constant> canonicalSubstitution) {
		//Check that the body is a conjunction of positive atoms
		Preconditions.checkArgument(isConjunctionOfAtoms(child));
		Preconditions.checkArgument(child.getFreeVariables().containsAll(freeVariables));
		this.child = child;
		this.freeVariables = ImmutableList.copyOf(freeVariables);
		this.boundVariables = ImmutableList.copyOf(CollectionUtils.removeAll(child.getFreeVariables(), freeVariables));
		this.canonicalSubstitution = canonicalSubstitution;
		this.canonicalSubstitutionOfFreeVariables = Maps.newHashMap(canonicalSubstitution);
		for(Variable variable:this.getBoundVariables()) {
			this.canonicalSubstitutionOfFreeVariables.remove(variable);
		}
	}
	
	/**
	 * Builds a query given a set of free variables and an atom.
	 * The query is grounded using the input mapping of variables to constants.
	 */
	public ConjunctiveQuery(List<Variable> freeVariables, Atom child, Map<Variable, Constant> canonicalSubstitution) {
		//Check that the body is a conjunction of positive atoms
		Preconditions.checkArgument(isConjunctionOfAtoms(child));
		Preconditions.checkArgument(child.getFreeVariables().containsAll(freeVariables));
		this.child = child;
		this.freeVariables = ImmutableList.copyOf(freeVariables);
		this.boundVariables = ImmutableList.copyOf(CollectionUtils.removeAll(child.getFreeVariables(), freeVariables));
		this.canonicalSubstitution = canonicalSubstitution;
		this.canonicalSubstitutionOfFreeVariables = Maps.newHashMap(canonicalSubstitution);
		for(Variable variable:this.getBoundVariables()) {
			this.canonicalSubstitutionOfFreeVariables.remove(variable);
		}
	}
	
	/**
	 * Builds a query given a set of free variables and its conjunction.
	 */
	public ConjunctiveQuery(List<Variable> freeVariables, Conjunction child) {
		this(freeVariables, child, generateSubstitutionToCanonicalVariables(child));
	}
	
	/**
	 * Builds a query given a set of free variables and an atom.
	 */
	public ConjunctiveQuery(List<Variable> freeVariables, Atom child) {
		this(freeVariables, child, generateSubstitutionToCanonicalVariables(child));
	}
	
	private static boolean isConjunctionOfAtoms(Formula formula) {
		if(formula instanceof Conjunction) {
			return isConjunctionOfAtoms(formula.getChildren().get(0)) && isConjunctionOfAtoms(formula.getChildren().get(1));
		}
		if(formula instanceof Atom) {
			return true;
		}
		return false;
	}
	/**
	 * TOCOMMENT the next 3 methods are discussed in #42
	 * 
	 * Generate canonical mapping.
	 *
	 * @param formula the body
	 * @return 		a mapping of variables of the input conjunction to constants. 
	 * 		A fresh constant is created for each variable of the conjunction. 
	 * 		This method is invoked by the conjunctive query constructor when the constructor is called with empty input canonical mapping.
	 */
	public static Map<Variable, Constant> generateSubstitutionToCanonicalVariables(Formula formula) {
		Map<Variable, Constant> canonicalMapping = new LinkedHashMap<>();
			for (Atom atom: formula.getAtoms()) {
				for (Term t: atom.getTerms()) {
					if (t.isVariable()) {
						Constant c = canonicalMapping.get(t);
						if (c == null) {
							c = new UntypedConstant(CanonicalNameGenerator.getName());
							canonicalMapping.put((Variable) t, c);
						}
					}
				}
			}
		return canonicalMapping;
	}
	
	/**
	 * Checks if the query is boolean boolean.
	 *
	 */
	public boolean isBoolean() {
		return this.getFreeVariables().isEmpty();
	}
	
	/**
	 * Gets the mapping of the free query variables to canonical constants.
	 *
	 * @return a map of query's free variables to its canonical constants.
	 * Given a CQ Q, the canonical database of Q is the instance which has for each atom R(\vec{v}) 
	 * in Q a corresponding fact for relation R with \vec{v} as a tuple. The canonical constants are the constants of the canonical database of Q
	 */
	public Map<Variable, Constant> getSubstitutionOfFreeVariablesToCanonicalConstants() {
		return this.canonicalSubstitutionOfFreeVariables;
	}
	
	public Map<Variable, Constant> getSubstitutionToCanonicalConstants() {
		return this.canonicalSubstitution;
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
				&& this.freeVariables.equals(((ConjunctiveQuery) o).freeVariables)
				&& this.child.equals(((ConjunctiveQuery) o).child);
	}


	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.freeVariables, this.child);
		}
		return this.hash;
	}


	@Override
	public java.lang.String toString() {
		if(this.toString == null) {
			this.toString = "";
			String op = this.boundVariables.isEmpty() ? "" : "exists";
			this.toString += "(" + op + "[" + Joiner.on(",").join(this.boundVariables) + "]" + this.child.toString() + ")";
		}
		return this.toString;
	}

	@Override
	public int getId() {
		return this.hashCode();
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
		return this.freeVariables;
	}

	@Override
	public List<Variable> getBoundVariables() {
		return this.boundVariables;
	}
}
