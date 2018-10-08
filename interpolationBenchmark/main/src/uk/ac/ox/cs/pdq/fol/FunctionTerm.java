package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class FunctionTerm implements Term{

	private final Function function;
	private final List<Term> terms;
	private Integer hash = null;
	protected String toString = null;
	protected List<Variable> variables;
	
	public FunctionTerm(Function function, List<Term> terms) {
		Preconditions.checkArgument(terms != null);
		Preconditions.checkArgument(function.getArity() == terms.size());
		this.function = function;
		this.terms = ImmutableList.copyOf(terms);
	}
	
	@Override
	public boolean isVariable() {
		return !(this.function.getArity() == 0);
	}

	@Override
	public boolean isUntypedConstant() {
		return false;
	}

	@Override
	public Term clone() {
		return new FunctionTerm(this.function, this.terms);
	}

	/**
	 * @return the function
	 */
	public Function getFunction() {
		return this.function;
	}

	/**
	 * @return the terms
	 */
	public List<Term> getTerms() {
		return this.terms;
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
				&& this.function.equals(((FunctionTerm) o).function)
				&& this.terms.equals(((FunctionTerm) o).terms);
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.function, this.terms);
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
			this.toString = this.function.getName() + (this.function.arity > 0 ? "(" + Joiner.on(",").join(this.terms) + ")" : "");
		}
		return this.toString;
	}
	
	public List<Variable> getVariables() {
		if(this.variables == null) {
			this.variables = new ArrayList<>();
			for (Term term: this.terms) {
				if(term instanceof Variable) {
					this.variables.add((Variable) term);
				}
				else if(term instanceof FunctionTerm) {
					this.variables.addAll(((FunctionTerm) term).getVariables());
				}
			}
		}
		return this.variables;
	}
}
