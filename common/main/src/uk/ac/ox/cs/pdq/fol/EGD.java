package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;

import org.junit.Assert;

/**
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class EGD extends Dependency {
	private static final long serialVersionUID = 7220579236533295677L;
	/**  The dependency's universally quantified variables. */
	protected Variable[] universal;
	
	/**
	 * Flag to indicate if this EGD was created from a functional dependency.
	 */
	private boolean isFromFunctionalDependency;
	
	protected EGD(Atom[] body, Atom[] head, boolean isFromFunctionalDependency) {
		super(body, head);
		this.isFromFunctionalDependency = isFromFunctionalDependency;
		Assert.assertTrue(isConjunctionOfNonEqualities(body));
		Assert.assertTrue(!isConjunctionOfNonEqualities(head));
		if (isFromFunctionalDependency) {
			Assert.assertEquals(2,body.length);
			Assert.assertEquals(body[0].getPredicate(),body[1].getPredicate());
		}
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
        return Cache.egd.retrieve(new EGD(head, body,false));
    }
    public static EGD create(Atom[] head, Atom[] body, boolean isFromFunctionalDependency) {
        return Cache.egd.retrieve(new EGD(head, body,isFromFunctionalDependency));
    }

	public boolean isFromFunctionalDependency() {
		return isFromFunctionalDependency;
	}
}
