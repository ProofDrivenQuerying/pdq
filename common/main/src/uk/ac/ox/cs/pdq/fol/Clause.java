package uk.ac.ox.cs.pdq.fol;

import java.io.Serializable;

import uk.ac.ox.cs.pdq.InterningManager;

/**
 * A disjunction of literals
 *
 * @author Efthymia Tsamoura
 */
public class Clause implements Serializable{
	private static final long serialVersionUID = -4433034581780675663L;

	/**   Cashed string representation of the literal. */
	private String toString = null;
	
	private final Literal[] literals;
	
	private Clause(Literal... literals) {
		this.literals = literals.clone();
	}
	
	public Literal[] getLiterals() {
		return this.literals.clone();
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
			int i = 0;
			for(Literal literal:this.literals) {
				this.toString += literal.toString();
				if(i < this.literals.length - 1) {
					this.toString += " | ";
				}
				++i;
			}
		}
		return this.toString;
	}
	
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static final InterningManager<Clause> s_interningManager = new InterningManager<Clause>() {
        protected boolean equal(Clause object1, Clause object2) {
            if (object1.literals.length != object2.literals.length)
                return false;
            for (int index = object1.literals.length - 1; index >= 0; --index)
                if (!object1.literals[index].equals(object2.literals[index]))
                    return false;
            return true;
        }

        protected int getHashCode(Clause object) {
            int hashCode = 0;
            for (int index = object.literals.length - 1; index >= 0; --index)
                hashCode = hashCode * 7 + object.literals[index].hashCode();
            return hashCode;
        }
    };

    public static Clause create(Literal... literals) {
        return s_interningManager.intern(new Clause(literals));
    }

}
