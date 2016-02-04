package uk.ac.ox.cs.pdq.generator.reverse;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
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

	/** The plan params. */
	private final PlannerParameters planParams;
	
	/** The cost params. */
	private final CostParameters costParams;
	
	/** The schema. */
	private final Schema schema;
	private final ReasoningParameters reasoningParams;

	/**
	 * Instantiates a new unanswerable query selector.
	 *
	 * @param p the p
	 * @param c the c
	 * @param r the r
	 * @param s the s
	 */
	public UnanswerableQuerySelector(PlannerParameters p, CostParameters c, ReasoningParameters r, Schema s) {
		this.planParams = (PlannerParameters) p.clone();
		this.costParams = (CostParameters) c.clone();
		this.planParams.setTimeout(10000);
		this.reasoningParams = (ReasoningParameters) r.clone();
		this.schema = s;
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean accept(Query<?> q) {
		try {
			return new ExplorationSetUp(this.planParams, this.costParams, this.reasoningParams, this.schema).search(q,true) != null;
		} catch (PlannerException e) {
			log.error(e);
			return false;
		}
	}

}
