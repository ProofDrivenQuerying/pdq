package uk.ac.ox.cs.pdq.fol;

import java.util.List;

import com.google.common.base.Preconditions;

/**
 * TOCOMMENT agree on a way to write formulas in javadoc
 * A dependency of the form \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
 * where \sigma and \tau are conjunctions of atoms.
 *
 * @author Efthymia Tsamoura
 */
public class TGD extends Dependency {
	
	public TGD(LogicalSymbols operator, List<Variable> variables, Implication implication) {
		super(operator, variables, implication);
		Preconditions.checkArgument(isConjunctionOfAtoms(implication.getChildren().get(0)));
		Preconditions.checkArgument(isConjunctionOfAtoms(implication.getChildren().get(1)));
	}

	public TGD(Formula body, Formula head) {
		super(body,head);
		Preconditions.checkArgument(isConjunctionOfAtoms(body));
		Preconditions.checkArgument(isConjunctionOfAtoms(head));
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
		return f + this.body + LogicalSymbols.IMPLIES + b + this.head;
	}
	
	public boolean isLinear() {
		return this.body.getAtoms().size() == 1;
	}
	
	public boolean isGuarded() {
		List<Atom> atoms = this.body.getAtoms();
		Atom guard = null;
		for(Atom atom:atoms) {
			if(guard == null || atom.getPredicate().getArity() >  guard.getPredicate().getArity()) {
				guard = atom;
			}
		}	
		for(Atom atom:atoms) {
			if(!guard.getVariables().containsAll(atom.getVariables())) {
				return false;
			}
		}
		return true;
	}
}
