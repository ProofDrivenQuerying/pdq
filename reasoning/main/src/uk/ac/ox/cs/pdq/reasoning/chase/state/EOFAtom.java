package uk.ac.ox.cs.pdq.reasoning.chase.state;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * A formula that contains no logical connectives.
 * An atomic formula is a formula of the form P (t_1, \ldots, t_n) for P a predicate, and the t_i terms.)
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class EOFAtom extends Atom {

	
	/**
	 * Constructor for Atomic formulae.
	 *
	 * @param predicate Predicate
	 * @param terms Collection<? extends Term>
	 */
	public EOFAtom() {
		super(new Predicate("EOF", 0), Lists.<Term>newArrayList());

	}

	/**
	 * Checks if is equality.
	 *
	 * @return true, if the atom acts as an equality
	 */
	public boolean isEquality() {
		return false;
	}


	/**
	 * Checks if is fact.
	 *
	 * @return Boolean
	 */
	public Boolean isFact() {
		return false;
	}
}
