package uk.ac.ox.cs.pdq.fol;

import static uk.ac.ox.cs.pdq.fol.LogicalSymbols.EXISTENTIAL;
import static uk.ac.ox.cs.pdq.fol.LogicalSymbols.UNIVERSAL;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * A quantified formula
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public abstract class QuantifiedFormula<T extends Formula> extends UnaryFormula<T> {

	/** The quantified variables*/
	protected final List<Variable> variables;

	/**
	 *
	 * @param operator
	 * 		Input quantifier operator
	 * @param variables
	 * 		Input quantified variables
	 * @param child
	 * 		Input child
	 * 		Throws illegal argument exception when the input operator is not a quantifier one
	 */
	protected QuantifiedFormula(LogicalSymbols operator, List<Variable> variables, T child) {
		super(operator, child);
		assert(operator == UNIVERSAL || operator == EXISTENTIAL);
		assert(child.getTerms().containsAll(variables));
		this.variables = variables;
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
				&& this.operator.equals(((QuantifiedFormula<?>) o).operator)
				&& this.variables.equals(((QuantifiedFormula<?>) o).variables)
				&& this.child.equals(((QuantifiedFormula<?>) o).child);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.operator, this.variables, this.child);
	}

	/**
	 * @return java.lang.String
	 */
	@Override
	public java.lang.String toString() {
		return this.operator.toString() + " " + Joiner.on(",").join(this.variables) + " "
				+ this.child.toString();

	}

	/**
	 * @return List<Variable>
	 */
	public List<Variable> getVariables() {
		return this.variables;
	}

	/**
	 * @return boolean
	 */
	public boolean isUniversal() {
		return this.operator == LogicalSymbols.UNIVERSAL;
	}


	/**
	 * @return boolean
	 */
	public boolean isExistential() {
		return this.operator == LogicalSymbols.EXISTENTIAL;
	}

	/**
	 * Constructor for UniversallyQuantifiedFormula.
	 * @param variables List<Variable>
	 * @param child T
	 */
	public static <T extends Formula> UniversallyQuantifiedFormula<T> forAll(List<Variable> variables, T child) {
		return new UniversallyQuantifiedFormula<>(variables, child);
	}

	/**
	 * Constructor for ExistentiallyQuantifiedFormula.
	 * @param variables List<Variable>
	 * @param child T
	 */
	public static <T extends Formula> ExistentiallyQuantifiedFormula<T> thereExists(List<Variable> variables, T child) {
		return new ExistentiallyQuantifiedFormula<>(variables, child);
	}

	/**
	 */
	public static final class UniversallyQuantifiedFormula<T extends Formula> extends QuantifiedFormula<T>{

		/**
		 * Constructor for UniversallyQuantifiedFormula.
		 * @param variables List<Variable>
		 * @param child T
		 */
		UniversallyQuantifiedFormula(List<Variable> variables, T child) {
			super(UNIVERSAL, variables, child);
		}

		/**
		 * @param mapping Map<Variable,Term>
		 * @return Formula
		 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
		 */
		@Override
		public UniversallyQuantifiedFormula ground(Map<Variable, Constant> mapping) {
			Preconditions.checkArgument(Collections.disjoint(this.variables, mapping.keySet()), "Illegal grounding for quantifier formula " + this);
			return new UniversallyQuantifiedFormula<>(
					Lists.newArrayList(this.variables),
					this.child.ground(mapping));
		}
	}

	/**
	 */
	public static final class ExistentiallyQuantifiedFormula<T extends Formula> extends QuantifiedFormula<T>{

		/**
		 * Constructor for ExistentiallyQuantifiedFormula.
		 * @param variables List<Variable>
		 * @param child T
		 */
		ExistentiallyQuantifiedFormula(List<Variable> variables, T child) {
			super(EXISTENTIAL, variables, child);
		}

		/**
		 * @param mapping Map<Variable,Term>
		 * @return Formula
		 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
		 */
		@Override
		public ExistentiallyQuantifiedFormula ground(Map<Variable, Constant> mapping) {
			Preconditions.checkArgument(Collections.disjoint(this.variables, mapping.keySet()), "Illegal grounding for quantifier formula " + this);
			return new ExistentiallyQuantifiedFormula<>(
					Lists.newArrayList(this.variables),
					this.child.ground(mapping));
		}
	}
}
