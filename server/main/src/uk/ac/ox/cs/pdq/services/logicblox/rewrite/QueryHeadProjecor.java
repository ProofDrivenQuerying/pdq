package uk.ac.ox.cs.pdq.services.logicblox.rewrite;

 import java.util.HashSet;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;

import com.beust.jcommander.internal.Lists;
import com.logicblox.common.Sets;

/**
 * Only keeps head terms that appear in a given list.
 * 
 * @author Julien Leblay
 */
public class QueryHeadProjecor implements Rewriter<ConjunctiveQuery, ConjunctiveQuery>{

	private final Set<Term> terms;
	
	public QueryHeadProjecor(Query<?> query) {
		this.terms = new HashSet<>();
		for (Term t: query.getFree()) {
			if (t.isVariable()) {
				this.terms.add(query.getFree2Canonical().get(t));
			} else {
				this.terms.add(t);
			}
		}
	}
	
	/**
	 * @param input T
	 * @return a copy of the input query where head terms have been reduced to
	 * those of the list given at construction time..
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
