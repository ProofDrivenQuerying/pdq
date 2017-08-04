package uk.ac.ox.cs.pdq.fol;

import java.io.Serializable;

/**
 * A disjunction of literals
 *
 * @author Efthymia Tsamoura
 */
public class Clause implements Serializable{
	private static final long serialVersionUID = -4433034581780675663L;

	/**   Cashed string representation of the literal. */
	private String toString = null;
	
	protected final Literal[] literals;
	
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
	
    public static Clause create(Literal... literals) {
        return Cache.clause.intern(new Clause(literals));
    }

}
