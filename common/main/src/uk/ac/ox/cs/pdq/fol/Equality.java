package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.io.xml.QNames;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * An equality.
 *
 * @author Efthymia Tsamoura
 */
public final class Equality extends Atom {

	/**
	 * Constructor for Equality.
	 * @param terms Collection<? extends Term>
	 */
	public Equality(Collection<? extends Term> terms) {
		super(new Predicate(QNames.EQUALITY.toString(), 2), terms);
		Preconditions.checkArgument(terms.size()==2, "Illegal equality terms");
	}
	
	/**
	 * Instantiates a new equality.
	 *
	 * @param term Term[]
	 */
	public Equality(Term... term) {
		super(new Predicate(QNames.EQUALITY.toString(), 2), term);
		Preconditions.checkArgument(term.length==2, "Illegal equality terms");
	}
	
	/**
	 * Ground.
	 *
	 * @param mapping Map<Variable,Term>
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
	 */
	@Override
	public Equality ground(Map<Variable, Constant> mapping) {
		List<Term> nterms = new ArrayList<>();
		for (Term term: this.getTerms()) {
			if (term.isVariable() && mapping.containsKey(term)) {
				nterms.add(mapping.get(term));
			} else {
				nterms.add(term);
			}
		}
		return new Equality(nterms);
	}
}
