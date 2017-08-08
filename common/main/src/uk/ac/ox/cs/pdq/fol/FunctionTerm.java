package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

/**
 * 
 * @author Efthtymia Tsamoura
 *
 */
public class FunctionTerm extends Term{
	private static final long serialVersionUID = 4513191157046218083L;

	private final Function function;
	private final Term[] terms;
	protected String toString = null;
	protected Variable[] variables;

	private FunctionTerm(Function function, Term... terms) {
		Assert.assertNotNull(terms);
		Assert.assertTrue(function.getArity() == terms.length);
		this.function = function;
		this.terms = terms.clone();
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
	public Term[] getTerms() {
		return this.terms.clone();
	}

	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder builder = new StringBuilder();
			builder.append(this.function.getName());
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

	public Variable[] getVariables() {
		if(this.variables == null) {
			List<Variable> variables = new ArrayList<>();
			for (Term term: this.terms) {
				if(term instanceof Variable) 
					variables.add((Variable) term);
				else if(term instanceof FunctionTerm) 
					variables.addAll(Arrays.asList(((FunctionTerm) term).getVariables()));
			}
			this.variables = variables.toArray(new Variable[variables.size()]);
		}
		return this.variables.clone();
	}
}
