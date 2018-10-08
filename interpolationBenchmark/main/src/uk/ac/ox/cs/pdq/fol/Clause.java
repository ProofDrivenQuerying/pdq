package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * A disjunction of literals
 *
 * @author Efthymia Tsamoura
 */
public class Clause {
	
	/**   Cashed string representation of the literal. */
	private String toString = null;

	private Integer hash = null;
	
	private final Set<Literal> literals;
	
	public Clause(Set<Literal> literals) {
		this.literals = ImmutableSet.copyOf(literals);
	}
	
	public Clause(Literal... literals) {
		this.literals = ImmutableSet.copyOf(literals);
	}
	
	public Set<Literal> getLiterals() {
		return this.literals;
	}
	
	/**
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.literals.equals(((Clause) o).literals);
	}


	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.literals);
		}
		return this.hash;
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
				if(i < this.literals.size() - 1) {
					this.toString += " | ";
				}
				++i;
			}
		}
		return this.toString;
	}

}
