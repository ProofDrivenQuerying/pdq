package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.io.jaxb.adapters.QueryAdapter;

/**
 * Same as a normal CQ but it can add inequality condition by specifying the
 * variable pairs that has to be different.
 * 
 * @author Gabor
 *
 */
@XmlJavaTypeAdapter(QueryAdapter.class)
public class ConjunctiveQueryWithInequality extends ConjunctiveQuery {
	private static final long serialVersionUID = 1L;
	/**
	 * List of variable pairs that has to be different.
	 */
	private List<Pair<Variable, Variable>> inequalities = new ArrayList<>();

	/**
	 * Builds a query given a set of free variables and an atom. The query is
	 * grounded using the input mapping of variables to constants.
	 */
	private ConjunctiveQueryWithInequality(Variable[] freeVariables, Atom[] children, List<Pair<Variable, Variable>> inequalities) {
		super(freeVariables, children);
		this.inequalities = inequalities;
	}

	@Override
	public java.lang.String toString() {
		return super.toString() + " Inequalities: " + inequalities;
	}

	public List<Pair<Variable, Variable>> getInequalities() {
		return this.inequalities;
	}
	
	public static ConjunctiveQueryWithInequality create(Variable[] freeVariables, Atom[] children, List<Pair<Variable, Variable>> inequalities) {
		return Cache.conjunctiveQueryWithInequality.retrieve(new ConjunctiveQueryWithInequality(freeVariables, children, inequalities));
	}

}
