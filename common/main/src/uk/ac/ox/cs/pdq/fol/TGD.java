package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;

/**
 * TOCOMMENT agree on a way to write formulas in javadoc
 * A dependency of the form \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
 * where \sigma and \tau are conjunctions of atoms.
 *
 * @author Efthymia Tsamoura
 */
public class TGD extends Dependency {

	private static final long serialVersionUID = 2745278271063580698L;

	protected TGD(Formula body, Formula head) {
		super(body, head);
		Assert.assertTrue(isConjunctionOfAtoms(body));
		Assert.assertTrue(head instanceof QuantifiedFormula || isConjunctionOfAtoms(head));
	}
	
	private static boolean isConjunctionOfAtoms(Formula formula) {
		if(formula instanceof Conjunction) 
			return isConjunctionOfAtoms(formula.getChildren()[0]) && isConjunctionOfAtoms(formula.getChildren()[1]);
		if(formula instanceof Atom) 
			return true;
		return false;
	}

	@Override
	public String toString() {
		String f = "";
		String b = "";
		
		if(this.getUniversal().length > 0) 
			f = this.getUniversal().toString();
		
		if(this.getExistential().length > 0) 
			b = this.getExistential().toString();
		
		return f + this.body + LogicalSymbols.IMPLIES + b + this.head;
	}
	
	public boolean isLinear() {
		return this.body.getAtoms().length == 1;
	}
	
	public boolean isGuarded() {
		Atom[] atoms = this.body.getAtoms();
		Atom guard = null;
		for(Atom atom:atoms) {
			if(guard == null || atom.getPredicate().getArity() >  guard.getPredicate().getArity()) 
				guard = atom;
		}	
		for(Atom atom:atoms) {
			if(!Arrays.asList(guard.getVariables()).containsAll(Arrays.asList(atom.getVariables()))) 
				return false;
		}
		return true;
	}
	
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    protected static final InterningManager<TGD> s_interningManager = new InterningManager<TGD>() {
        protected boolean equal(TGD object1, TGD object2) {
            if (!object1.head.equals(object2.head) || !object1.body.equals(object2.body) || object1.variables.length != object2.variables.length) 
                return false;
            for (int index = object1.variables.length - 1; index >= 0; --index)
                if (!object1.variables[index].equals(object2.variables[index]))
                    return false;
            return true;
        }
        
        protected int getHashCode(TGD object) {
            int hashCode = object.head.hashCode() + object.body.hashCode() * 7;
            for (int index = object.variables.length - 1; index >= 0; --index)
                hashCode = hashCode * 8 + object.variables[index].hashCode();
            return hashCode;
        }
    };
    
    public static TGD create(Formula head, Formula body) {
        return s_interningManager.intern(new TGD(head, body));
    }
}
