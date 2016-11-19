package uk.ac.ox.cs.pdq.generator.reverse;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

// TODO: Auto-generated Javadoc
/**
 * A QuerySelector that accepts conjunctive query where that do not look like
 * already observed queries.
 * 
 * @author Julien Leblay
 *
 */
public class DiversityQuerySelector implements QuerySelector {

	/** TODO: The signatures. */
	private Set<SortedSet<FactSignature>> signatures = new LinkedHashSet<>();
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.fol.Query)
	 */
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.formula.Query)
	 */
	@Override
	public boolean accept(ConjunctiveQuery q) {
		SortedSet<FactSignature> signature = FactSignature.make(q.getAtoms());
		if (this.signatures.contains(signature)) {
			return false;
		}
		this.signatures.add(signature);
		return true;
	}
}
