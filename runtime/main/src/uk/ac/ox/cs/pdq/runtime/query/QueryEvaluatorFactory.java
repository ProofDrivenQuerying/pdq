package uk.ac.ox.cs.pdq.runtime.query;

import java.sql.SQLException;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.wrappers.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
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
	 * @param schema
	 * @param query Query
	 * @return QueryEvaluator
	 * @throws EvaluationException
	 */
	public static QueryEvaluator newEvaluator(Schema schema, Query<?> query) throws EvaluationException {
//		if (!checkConsistency(schema)) {
//			throw new EvaluationException("Currently query evaluation for heterogeneous sources is not supported.");
//		}
		try {
			QueryEvaluator result = null;
			for (Predicate p: ((ConjunctiveQuery) query).getBody()) {
				Relation r = (Relation) p.getSignature();
			    if (r instanceof InMemoryTableWrapper) {
			    	return new InMemoryQueryEvaluator(query);
			    } else {
					return SQLQueryEvaluator.newEvaluator(query);
				}
			}
			return result;
		} catch (SQLException e) {
			throw new EvaluationException(e.getMessage(), e);
		}
	}

	/**
	 * @param schema
	 * @return true if all the relation in the given schema are of the same
	 * most-specific type.
	 */
	@Deprecated
	private static boolean checkConsistency(Schema schema) {
		Class<?> cl = null;
		for (Relation r: schema.getRelations()) {
			if (cl == null) {
				cl = r.getClass();
			} else if (!cl.equals(r.getClass())) {
				return false;
			}
		}
		return true;
	}

}
