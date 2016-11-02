package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.DatabaseEqualityRelation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;


/**
 * TOCOMMENT how does this differ from Equality in common.fol?
 * An equality.
 *
 * @author Efthymia Tsamoura
 */
public final class DatabaseEquality extends Atom {
	
	/**
	 * Constructor for Equality.
	 * @param terms Collection<? extends Term>
	 */
	public DatabaseEquality(Collection<? extends Term> terms) {
		super(DatabaseEqualityRelation.relation, terms);
		Preconditions.checkArgument(terms.size()==3, "Illegal equality terms");
	}
	
	/**
	 * Instantiates a new equality.
	 *
	 * @param term Term[]
	 */
	public DatabaseEquality(Term... term) {
		super(DatabaseEqualityRelation.relation, term);
		Preconditions.checkArgument(term.length==3, "Illegal equality terms");
	}
	
	/**
	 * Ground.
	 *
	 * @param mapping Map<Variable,Term>
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
	 */
	@Override
	public DatabaseEquality ground(Map<Variable, Constant> mapping) {
		throw new java.lang.UnsupportedOperationException();
	}
}
