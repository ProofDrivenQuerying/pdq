package uk.ac.ox.cs.pdq.fol;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.CanonicalNameGenerator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * TOCOMMENT agree on a way to write formulas in javadoc
 * A dependency of the form \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
 * where \sigma and \tau are conjunctions of atoms.
 *
 * @author Efthymia Tsamoura
 */
public class TGD extends Dependency {

	/**  The dependency's constants. */
	protected Collection<TypedConstant<?>> constants = new LinkedHashSet<>();

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

	/**
	 * Fire.
	 *
	 * @param mapping Map<Variable,Term>
	 * @param skolemize boolean
	 * @return TGD<L,R>
	 * @see uk.ac.ox.cs.pdq.ics.IC#fire(Map<Variable,Term>, boolean)
	 */
	public Implication fire(Map<Variable, Constant> mapping, boolean skolemize) {
		Map<Variable, Constant> skolemizedMapping = mapping;
		if(skolemize) {
			skolemizedMapping = this.skolemizeMapping(mapping);
		}
		List<Formula> bodyAtoms = Lists.newArrayList();
		for(Atom atom:this.body.getAtoms()) {
			atom.ground(skolemizedMapping);
			bodyAtoms.add(atom);
		}
		List<Formula> headAtoms = Lists.newArrayList();
		for(Atom atom:this.body.getAtoms()) {
			atom.ground(skolemizedMapping);
			headAtoms.add(atom);
		}
		Formula bodyConjunction = Conjunction.of(bodyAtoms);
		Formula headConjunction = Conjunction.of(headAtoms);
		return Implication.of(bodyConjunction, headConjunction);
	}

	/**
	 * TOCOMMENT there is no "canonicalNames" mentioned in the comment says here.
	 * Skolemize mapping.
	 *
	 * @param mapping the mapping
	 * @return 		If canonicalNames is TRUE returns a copy of the input mapping
	 * 		augmented such that Skolem constants are produced for
	 *      the existentially quantified variables
	 */
	public Map<Variable, Constant> skolemizeMapping(Map<Variable, Constant> mapping) {
		String namesOfUniversalVariables = "";
		Map<Variable, Constant> result = new LinkedHashMap<>(mapping);
		for (Variable variable: this.universal) {
			Variable variableTerm = variable;
			Preconditions.checkState(result.get(variableTerm) != null);
			namesOfUniversalVariables += variable.getSymbol() + result.get(variableTerm);
		}
		for(Variable variable:this.existential) {
			if (!result.containsKey(variable)) {
				result.put(variable,
						new UntypedConstant(
								CanonicalNameGenerator.getName("TGD" + this.getId(),
										namesOfUniversalVariables,
										variable.getSymbol()))
						);
			}
		}
		return result;
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
