package uk.ac.ox.cs.pdq.fol;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.TypedConstant;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * TOCOMMENT agree on a way to write formulas in javadoc
 * A dependency of the form \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
 * where \sigma and \tau are conjunctions of atoms.
 *
 * @author Efthymia Tsamoura
 */
public class EGD extends Dependency {

	/**  The dependency's universally quantified variables. */
	protected List<Variable> universal;

	/**  The dependency's constants. */
	protected Collection<TypedConstant<?>> constants = new LinkedHashSet<>();

	public EGD(LogicalSymbols operator, List<Variable> variables, Implication implication) {
		super(operator, variables, implication);
		Preconditions.checkArgument(isConjunctionOfAtoms(implication.getChildren().get(0)));
		Preconditions.checkArgument(isConjunctionOfEqualities(implication.getChildren().get(1)));
	}

	public EGD(Formula body, Formula head) {
		super(body,head);
		Preconditions.checkArgument(isConjunctionOfAtoms(body));
		Preconditions.checkArgument(isConjunctionOfEqualities(head));
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

	private static boolean isConjunctionOfEqualities(Formula formula) {
		if(formula instanceof Conjunction) {
			return isConjunctionOfEqualities(formula.getChildren().get(0)) && isConjunctionOfEqualities(formula.getChildren().get(1));
		}
		if(formula instanceof Atom) {
			if(((Atom)formula).isEquality()) {
				return true;
			}
		}
		return false;
	}

	public Implication fire(Map<Variable, Constant> mapping) {
		List<Formula> bodyAtoms = Lists.newArrayList();
		for(Atom atom:this.body.getAtoms()) {
			atom.ground(mapping);
			bodyAtoms.add(atom);
		}
		List<Formula> headAtoms = Lists.newArrayList();
		for(Atom atom:this.body.getAtoms()) {
			atom.ground(mapping);
			headAtoms.add(atom);
		}
		Formula bodyConjunction = Conjunction.of(bodyAtoms);
		Formula headConjunction = Conjunction.of(headAtoms);
		return Implication.of(bodyConjunction, headConjunction);
	}

	/**
	 * Gets the universally quantified variables.
	 *
	 * @return List<Variable>
	 */
	public List<Variable> getUniversal() {
		if(this.universal == null) {
			this.universal = this.variables;
		}
		return this.universal;
	}

	@Override
	public String toString() {
		String f = "";
		String b = "";

		if(!this.universal.isEmpty()) {
			f = this.universal.toString();
		}

		return f + this.body + LogicalSymbols.IMPLIES + b + this.head;
	}
}
