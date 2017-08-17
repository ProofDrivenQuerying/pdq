package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;

import org.junit.Assert;

/**
 *
 * @author Efthymia Tsamoura
 */
public class EGD extends Dependency {
	private static final long serialVersionUID = 7220579236533295677L;
	/**  The dependency's universally quantified variables. */
	protected Variable[] universal;
	
	protected EGD(Atom[] body, Atom[] head) {
		super(body, head);
		Assert.assertTrue(isConjunctionOfNonEqualities(body));
		Assert.assertTrue(!isConjunctionOfNonEqualities(head));
	}

	private static boolean isConjunctionOfNonEqualities(Atom[] atoms) {
		for(Atom atom:atoms) {
			if(atom.isEquality())
				return false;
		}
		return true;
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
			f = Arrays.asList(this.getUniversal()).toString();
		
		return f + this.body + LogicalSymbols.IMPLIES + this.head;
	}
	
	public boolean isLinear() {
		return this.body.getAtoms().length == 1;
	}
	
    public static EGD create(Atom[] head, Atom[] body) {
        return Cache.egd.retrieve(new EGD(head, body));
    }
}
