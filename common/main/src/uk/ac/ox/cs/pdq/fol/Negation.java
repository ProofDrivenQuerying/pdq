package uk.ac.ox.cs.pdq.fol;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public final class Negation extends Formula {

	private static final long serialVersionUID = 2571574465306118274L;

	protected final Formula child;

	/**  The unary operator. */
	protected final LogicalSymbols operator = LogicalSymbols.NEGATION;

	/**  Cashed string representation of the atom. */
	private String toString = null;

	/**  Cashed list of atoms. */
	private Atom[] atoms;

	/**  Cashed list of terms. */
	private Term[] terms;

	/**  Cashed list of free variables. */
	private Variable[] freeVariables;

	/**  Cashed list of bound variables. */
	private Variable[] boundVariables;

	/**
	 * Constructor for Negation.
	 * @param sf T
	 */
	public Negation(Formula child) {
		Assert.assertNotNull(child);
		this.child = child;
	}

	/**
	 * Convenience constructor for Negation.
	 *
	 * @param <T> the generic type
	 * @param f T
	 * @return Negation<T>
	 */
	public static Negation of(Formula f) {
		Assert.assertNotNull(f);
		return new Negation(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Formula[] getChildren() {
		return new Formula[]{this.child};
	}

	@Override
	public Atom[] getAtoms() {
		if(this.atoms == null) 
			this.atoms = this.child.getAtoms();
		return this.atoms.clone();
	}

	@Override
	public Term[] getTerms() {
		if(this.terms == null) 
			this.terms = this.child.getTerms();
		return this.terms.clone();
	}

	@Override
	public Variable[] getFreeVariables() {
		if(this.freeVariables == null) 
			this.freeVariables = this.child.getFreeVariables();
		return this.freeVariables.clone();
	}

	@Override
	public Variable[] getBoundVariables() {
		if(this.boundVariables == null) 
			this.boundVariables = this.child.getBoundVariables();
		return this.boundVariables.clone();
	}
	
	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			this.toString = "";
			this.toString += "(" + "~" + this.child.toString() + ")";
		}
		return this.toString;
	}

	@Override
	public int getId() {
		return this.hashCode();
	}
	
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static final InterningManager<Negation> s_interningManager = new InterningManager<Negation>() {
        protected boolean equal(Negation object1, Negation object2) {
            if (!object1.child.equals(object2.child) || !object1.operator.equals(object2.operator))
                return false;
            return true;
        }
        
        protected int getHashCode(Negation object) {
            int hashCode = object.child.hashCode() + object.operator.hashCode() * 7;
            return hashCode;
        }
    };

    public static Negation create(Formula child) {
        return s_interningManager.intern(new Negation(child));
    }
    
	@Override
	public Formula getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}

	@Override
	public int getNumberOfChildlen() {
		return 1;
	}
}