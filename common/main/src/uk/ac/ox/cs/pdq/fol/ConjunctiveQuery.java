package uk.ac.ox.cs.pdq.fol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.util.CanonicalNameGenerator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * TOCOMMENT find a pretty way to write formulas in javadoc
 * A conjunctive query (CQ) is a first order formula of the form \exists x_1, \ldots, x_n \Wedge A_i,
 * where A_i are atoms with arguments that are either variables or constants.

 * @author Efthymia Tsamoura
 *
 */
public class ConjunctiveQuery extends QuantifiedFormula {

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
	
	/**
	 * Builds a conjunctive query given the input head variables and body.
	 * The query is grounded using the input mapping of variables to constants.
	 *  
	 * @param head
	 * 		The query's head
	 * @param formula
	 * 		The query's body
	 * @param grounding
	 * 		Mapping of query variables to constants  
	 */
	public ConjunctiveQuery(List<Variable> variables, Formula child, Map<Variable, Constant> canonicalSubstitution) {
		super(LogicalSymbols.EXISTENTIAL, variables, child);
		//Check that the body is a conjunction of positive atoms
		Preconditions.checkArgument(isConjunctionOfAtoms(child));
		this.canonicalSubstitution = canonicalSubstitution;
		this.canonicalSubstitutionOfFreeVariables = Maps.newHashMap(canonicalSubstitution);
		for(Variable variable:this.getBoundVariables()) {
			this.canonicalSubstitutionOfFreeVariables.remove(variable);
		}
	}
	
	public ConjunctiveQuery(List<Variable> variables, Formula child) {
		this(variables, child, generateSubstitutionToCanonicalVariables(child));
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
	
	
	public List<Atom> getCanonicalDatabase(Map<Variable, Constant> mapping) {
		Set<Atom> atoms = Sets.newHashSet();
		for(Atom atom:atoms) {
			atoms.add(atom.ground(mapping));
		}
		return Lists.newArrayList(atoms);
	}

	/**
	 * Checks if the query is boolean boolean.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Query#isBoolean()
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
	
	public boolean contains(Predicate s) {
		for (Atom atom: this.getAtoms()) {
			if (atom.getPredicate().equals(s)) {
				return true;
			}
		}
		return false;
	}
}
