package uk.ac.ox.cs.pdq.fol;

import static uk.ac.ox.cs.pdq.fol.LogicalSymbols.EXISTENTIAL;
import static uk.ac.ox.cs.pdq.fol.LogicalSymbols.UNIVERSAL;

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
 * 
 * @author Efthymia Tsamoura
 *
 */
public class QuantifiedFormula extends Formula {

	protected final Formula child;

	/**  The unary operator. */
	protected final LogicalSymbols operator;

	/**  The quantified variables. */
	protected final List<Variable> variables;

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
	 * Instantiates a new quantified formula.
	 *
	 * @param operator 		Input quantifier operator
	 * @param variables 		Input quantified variables
	 * @param child 		Input child
	 */
	public QuantifiedFormula(LogicalSymbols operator, List<Variable> variables, Formula child) {
		Preconditions.checkArgument(operator == UNIVERSAL || operator == EXISTENTIAL);
		Preconditions.checkArgument(child != null);
		Preconditions.checkArgument(variables != null);
		Preconditions.checkArgument(Utility.getVariables(child).containsAll(variables));
		this.child = child;
		this.operator = operator;
		this.variables = ImmutableList.copyOf(variables);
	}

	/**
	 * Checks if is universal.
	 *
	 * @return boolean
	 */
	public boolean isUniversal() {
		return this.operator == LogicalSymbols.UNIVERSAL;
	}


	/**
	 * Checks if is existential.
	 *
	 * @return boolean
	 */
	public boolean isExistential() {
		return this.operator == LogicalSymbols.EXISTENTIAL;
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
				&& this.operator.equals(((QuantifiedFormula) o).operator)
				&& this.variables.equals(((QuantifiedFormula) o).variables)
				&& this.child.equals(((QuantifiedFormula) o).child);
	}


	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.operator, this.variables, this.child);
		}
		return this.hash;
	}


	@Override
	public java.lang.String toString() {
		if(this.toString == null) {
			this.toString = "";
			String op = this.operator.equals(LogicalSymbols.UNIVERSAL) ? "forall" : "exists";
			this.toString += "(" + op + "[" + Joiner.on(",").join(this.variables) + "]" + this.child.toString() + ")";
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
		if(this.freeVariables == null) {
			List<Variable> variables = Lists.newArrayList(this.child.getFreeVariables());
			variables.removeAll(this.variables);
			this.freeVariables = variables;
		}
		return this.freeVariables;
	}

	@Override
	public List<Variable> getBoundVariables() {
		if(this.boundVariables == null) {
			Set<Variable> variables = Sets.newLinkedHashSet(this.child.getBoundVariables());
			variables.addAll(this.variables);
			this.boundVariables = Lists.newArrayList(variables);
		}
		return this.boundVariables;
	}

	public static QuantifiedFormula of(LogicalSymbols operator, List<Variable> variables, Formula child) {
		return new QuantifiedFormula(operator, variables, child);
	}

	public LogicalSymbols getOperator() {
		return this.operator;
	}
	
	public List<Variable> getTopLevelQuantifiedVariables() {
		return this.variables;
	}
}
