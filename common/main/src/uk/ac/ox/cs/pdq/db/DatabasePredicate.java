package uk.ac.ox.cs.pdq.db;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * Convenience class, referring to a predicate whose relation symbol is a
 * database relation.
 *
 * @author Julien Leblay
 */
public class DatabasePredicate extends Predicate {

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
	 * @return Relation
	 * @see uk.ac.ox.cs.pdq.fol.AtomicFormula#getSignature()
	 */
	@Override
	public Relation getSignature() {
		return (Relation) super.getSignature();
	}
}
