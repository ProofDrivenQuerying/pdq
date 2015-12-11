package uk.ac.ox.cs.pdq.generator.reverse;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

/**
 * A QuerySelector that accepts only conjunctive query that are not unanswerable
 * without considering schema dependencies.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 *
 */
public class UnanswerableQuerySelector implements QuerySelector {

	/** Logger. */
	private static Logger log = Logger.getLogger(ReverseQueryGenerator.class);

	private final PlannerParameters planParams;
	private final CostParameters costParams;
	private final ReasoningParameters reasoningParams;
	private final Schema schema;
	
	public UnanswerableQuerySelector(PlannerParameters p, CostParameters c, ReasoningParameters r, Schema s) {
		this.planParams = (PlannerParameters) p.clone();
		this.costParams = (CostParameters) c.clone();
		this.reasoningParams = (ReasoningParameters) r.clone();
		this.planParams.setTimeout(10000);
		this.schema = s;
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.formula.Query)
	 */
	@Override
	public boolean accept(Query<?> q) {
		try {
			return new Planner(this.planParams, this.costParams, this.reasoningParams, this.schema).search(q,true) != null;
		} catch (PlannerException e) {
			log.error(e);
			return false;
		}
	}

}
