package uk.ac.ox.cs.pdq.fol;

import org.junit.Assert;

/**
 * TOCOMMENT agree on a way to write formulas in javadoc
 * A dependency of the form \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
 * where \sigma and \tau are conjunctions of atoms.
 *
 * @author Efthymia Tsamoura
 */
public class EGD extends Dependency {
	private static final long serialVersionUID = 7220579236533295677L;
	/**  The dependency's universally quantified variables. */
	protected Variable[] universal;
	
	protected EGD(Formula body, Formula head) {
		super(body, head);
		Assert.assertTrue(isConjunctionOfAtoms(body));
		Assert.assertTrue(isConjunctionOfEqualities(head));
	}

	private static boolean isConjunctionOfAtoms(Formula formula) {
		if(formula instanceof Conjunction) 
			return isConjunctionOfAtoms(formula.getChildren()[0]) && isConjunctionOfAtoms(formula.getChildren()[1]);
		if(formula instanceof Atom) 
			return true;
		return false;
	}

	private static boolean isConjunctionOfEqualities(Formula formula) {
		if(formula instanceof Conjunction) 
			return isConjunctionOfEqualities(formula.getChildren()[0]) && isConjunctionOfEqualities(formula.getChildren()[1]);
		if(formula instanceof Atom) {
			if(((Atom)formula).isEquality()) 
				return true;
		}
		return false;
	}

	/**
	 * Gets the universally quantified variables.
	 *
	 * @return List<Variable>
	 */
	public Variable[] getUniversal() {
		if(this.universal == null) 
			this.universal = this.variables;
		return this.universal.clone();
	}

	@Override
	public String toString() {
		String f = "";
		if(this.getUniversal().length > 0) 
			f = this.getUniversal().toString();
		return f + this.body + LogicalSymbols.IMPLIES + this.head;
	}
	
    public static EGD create(Formula head, Formula body) {
        return Cache.egd.intern(new EGD(head, body));
    }
}
