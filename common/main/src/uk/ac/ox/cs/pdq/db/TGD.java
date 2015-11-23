package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Skolem.Generator;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A tuple generating dependency
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class TGD
		extends Implication<Conjunction<Predicate>, Conjunction<Predicate>>
		implements Constraint<Conjunction<Predicate>, Conjunction<Predicate>> {

	/** The dependency's universally quantified variables */
	protected final List<Variable> universal;

	/** The dependency's existentially quantified variables */
	protected final List<Variable> existential;

	/** The dependency's constants */
	protected final Collection<TypedConstant<?>> constants = new LinkedHashSet<>();

	/**
	 * @param left The left-hand side conjunction of the dependency
	 * @param right The right-hand side conjunction of the dependency
	 */
	public TGD(Conjunction<Predicate> left, Conjunction<Predicate> right) {
		super(left, right);
		this.universal = Utility.getVariables(left.getPredicates());
		this.existential = Utility.getVariables(right.getPredicates());
		this.existential.removeAll(this.universal);

		for (Term term:right.getTerms()) {
			if (!term.isVariable() && !term.isSkolem()) {
				this.constants.add(((TypedConstant) term));
			}
		}
	}

	/**
	 * @return the inverse dependency
	 */
	public TGD invert() {
		return new TGD(this.right, this.left);
	}

	/**
	 * @return List<Variable>
	 */
	public List<Variable> getUniversal() {
		return this.universal;
	}

	/**
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Evaluatable#getFree()
	 */
	@Override
	public List<Term> getFree() {
		return Lists.<Term>newArrayList(this.universal);
	}

	/**
	 * @return List<Variable>
	 */
	public List<Variable> getExistential() {
		return this.existential;
	}

	/**
	 * @return L
	 * @see uk.ac.ox.cs.pdq.db.Constraint#getLeft()
	 */
	@Override
	public Conjunction<Predicate> getLeft() {
		return this.left;
	}

	/**
	 * @return R
	 * @see uk.ac.ox.cs.pdq.db.Constraint#getRight()
	 */
	@Override
	public Conjunction<Predicate> getRight() {
		return this.right;
	}

	/**
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getTerms()
	 */
	@Override
	public List<Term> getTerms() {
		List<Term> terms = new ArrayList<>();
		terms.addAll(this.left.getTerms());
		terms.addAll(this.right.getTerms());
		return terms;
	}


	/**
	 * @return Collection<TypedConstant<?>>
	 * @see uk.ac.ox.cs.pdq.db.Constraint#getSchemaConstants()
	 */
	@Override
	public Collection<TypedConstant<?>> getSchemaConstants() {
		return this.constants;
	}

	/**
	 * @param mapping Map<Variable,Term>
	 * @param canonicalNames boolean
	 * @return TGD<L,R>
	 * @see uk.ac.ox.cs.pdq.ics.IC#fire(Map<Variable,Term>, boolean)
	 */
	@Override
	public TGD fire(Map<Variable, Constant> mapping, boolean canonicalNames) {
		return this.ground(mapping, canonicalNames);
	}

	/**
	 * @param mapping
	 * @return
	 * 		If canonicalNames is TRUE returns a copy of the input mapping
	 * 		augmented such that Skolem constants are produced for
	 *      the existentially quantified variables
	 */
	public Map<Variable, Constant> skolemizeMapping(Map<Variable, Constant> mapping) {
		String namesOfUniversalVariables = "";
		Map<Variable, Constant> result = new LinkedHashMap<>(mapping);
		for (Variable variable: this.universal) {
			Variable variableTerm = variable;
			Preconditions.checkState(result.get(variableTerm) != null);
			namesOfUniversalVariables += variable.getName() + result.get(variableTerm);
		}
		for(Variable variable:this.existential) {
			if (!result.containsKey(variable)) {
				result.put(variable,
						new Skolem(
								Generator.getName("TGD" + this.id,
										namesOfUniversalVariables,
										variable.getName()))
						);
			}
		}
		return result;
	}

	/**
	 * @param mapping
	 * @param canonicalNames
	 * 		True if we assign Skolem constants to the existentially quantified variables
	 * @return
	 * 		the grounded dependency using the input mapping.
	 *      If canonicalNames is TRUE then skolem constants are produced for
	 *      the existentially quantified variables
	 */
	public TGD ground(Map<Variable, Constant> mapping, boolean canonicalNames) {
		return canonicalNames == true ? this.ground(this.skolemizeMapping(mapping)): this.ground(mapping);
	}

	/**
	 * @param mapping Map<Variable,Term>
	 * @return TGD<L,R>
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
	 */
	@Override
	public TGD ground(Map<Variable, Constant> mapping) {
		return new TGD(
				this.left.ground(mapping),
				this.right.ground(mapping));
	}

	/**
	 * @return Set<Variable>
	 * @see uk.ac.ox.cs.pdq.db.Constraint#getBothSideVariables()
	 */
	@Override
	public Set<Variable> getBothSideVariables() {
		Set<Variable> variables = Sets.newHashSet(Utility.getVariables(this.left.getPredicates()));
		variables.retainAll(Utility.getVariables(this.right.getPredicates()));
		return variables;
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
				&& this.left.equals(((TGD) o).left)
				&& this.right.equals(((TGD) o).right)
				&& this.universal.equals(((TGD) o).universal)
				&& this.existential.equals(((TGD) o).existential);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.universal, this.existential, this.left, this.right);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		String f = "";
		String b = "";

		if(!this.universal.isEmpty()) {
			f = this.universal.toString();
		}

		if(!this.existential.isEmpty()) {
			b = this.existential.toString();
		}
		return f + this.left + LogicalSymbols.IMPLIES + b + this.right;
	}
}
