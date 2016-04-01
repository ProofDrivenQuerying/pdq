package uk.ac.ox.cs.pdq.db;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;

// TODO: Auto-generated Javadoc
/**
 * Convenience class, referring to a predicate whose relation symbol is a
 * database relation.
 *
 * @author Julien Leblay
 */
public class DatabasePredicate extends Atom {

	/**
	 * Constructor for DatabasePredicate.
	 * @param signature Relation
	 * @param terms Collection<? extends Term>
	 */
	public DatabasePredicate(Relation signature, Collection<? extends Term> terms) {
		super(signature, terms);
	}

	/**
	 * Constructor for DatabasePredicate.
	 * @param signature Relation
	 * @param terms Term[]
	 */
	public DatabasePredicate(Relation signature, Term... terms) {
		super(signature, terms);
	}

	/**
	 * Gets the signature.
	 *
	 * @return Relation
	 * @see uk.ac.ox.cs.pdq.fol.AtomicFormula#getPredicate()
	 */
	@Override
	public Relation getPredicate() {
		return (Relation) super.getPredicate();
	}
}
