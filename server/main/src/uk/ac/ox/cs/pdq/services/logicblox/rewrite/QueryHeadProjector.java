package uk.ac.ox.cs.pdq.services.logicblox.rewrite;

 import java.util.HashSet;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.util.Utility;

import com.beust.jcommander.internal.Lists;
import com.logicblox.common.Sets;

/**
 * Only keeps head terms that appear in a given list.
 * 
 * @author Julien Leblay
 */
public class QueryHeadProjector implements Rewriter<ConjunctiveQuery, ConjunctiveQuery>{

	/** The terms. */
	private final Set<Term> terms;
	
	/**
	 * Instantiates a new query head projector.
	 *
	 * @param query the query
	 */
	public QueryHeadProjector(ConjunctiveQuery query) {
		this.terms = new HashSet<>();
		for (Term t: query.getFree()) {
			// ??? How could it be that a free term of a query is not variable???
			if (t.isVariable()) {
				this.terms.add(query.getGroundingsProjectionOnFreeVars().get(t));
			} else {
				this.terms.add(t);
			}
		}
	}
	
	/**
	 * Rewrite.
	 *
	 * @param input T
	 * @return a copy of the input query where head terms have been reduced to
	 * those of the list given at construction time..
	 * @throws RewriterException the rewriter exception
	 */
	@Override
	public ConjunctiveQuery rewrite(ConjunctiveQuery input) throws RewriterException {
		return new ConjunctiveQuery(
				input.getHead().getName(),
				Lists.newArrayList(
						Sets.intersect(
								this.terms,
								new HashSet<>(input.getFree()))),
				input.getBody());
	}
}
