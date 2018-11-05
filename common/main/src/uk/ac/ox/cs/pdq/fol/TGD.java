package uk.ac.ox.cs.pdq.fol;

import java.util.Arrays;

/**
 *
 * @author Efthymia Tsamoura, Mark Ridler
 */
public class TGD extends Dependency {

	private static final long serialVersionUID = 2745278271063580698L;
	
	protected TGD(Atom[] body, Atom[] head) {
		super(body,head);
	}
	
	@Override
	public String toString() {
		return this.body.toString() + LogicalSymbols.IMPLIES + this.head.toString();
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
	
    public static TGD create(Atom[] body, Atom[] head) {
        return Cache.tgd.retrieve(new TGD(body, head));
    }
}
