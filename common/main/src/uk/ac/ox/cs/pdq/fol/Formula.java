package uk.ac.ox.cs.pdq.fol;

import java.io.Serializable;

/**
 * Top-level FO formula.
 *
 * @author Efthymia Tsamoura
 */
public abstract class Formula implements Serializable{
	private static final long serialVersionUID = -398980058943314856L;

	public abstract int getId();
	
	public abstract <T extends Formula> T[] getChildren();

	public abstract Atom[] getAtoms();

	public abstract Term[] getTerms();
	
	public abstract Variable[] getFreeVariables();
	
	public abstract Variable[] getBoundVariables();
}
