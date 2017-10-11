package uk.ac.ox.cs.pdq.runtime.query;

import uk.ac.ox.cs.pdq.datasources.memory.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;


/**
 * This factory create query evaluators that may be specific to relation 
 * databases, web services, etc or a combination of those.
 * 
 * The decision on which type of evaluator to instantiate is taken by the 
 * factor based on information given in the input schema.
 * 
 * @author Julien Leblay
 */
public class QueryEvaluatorFactory {

	/**
	 * New evaluator.
	 *
	 * @param schema the schema
	 * @param query Query
	 * @return QueryEvaluator
	 * @throws EvaluationException the evaluation exception
	 */
	public static QueryEvaluator newEvaluator(Schema schema, ConjunctiveQuery query) throws EvaluationException {
		QueryEvaluator result = null;
		for (Atom p: query.getAtoms()) {
			Relation r = (Relation) p.getPredicate();
			if (r instanceof InMemoryTableWrapper) 
				return new InMemoryQueryEvaluator(query);
		}
		return result;
	}
}
