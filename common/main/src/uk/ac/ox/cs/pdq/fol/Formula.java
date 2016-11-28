package uk.ac.ox.cs.pdq.fol;

import java.util.List;


/**
 * Top-level FO formula.
 *
 * @author Efthymia Tsamoura
 */
public abstract class Formula {

	public abstract int getId();
	
	public abstract <T extends Formula> List<T> getChildren();

	public abstract List<Atom> getAtoms();

	public abstract List<Term> getTerms();
	
	public abstract List<Variable> getFreeVariables();
	
	public abstract List<Variable> getBoundVariables();
}
