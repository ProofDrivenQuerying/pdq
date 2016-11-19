package uk.ac.ox.cs.pdq.generator.reverse;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

// TODO: Auto-generated Javadoc
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
	
	/** The reasoning params. */
	private final ReasoningParameters reasoningParams;
	
	/** The database params. */
	private final DatabaseParameters dbParams;

	/**
	 * Instantiates a new unanswerable query selector.
	 *
	 * @param p the p
	 * @param c the c
	 * @param r the r
	 * @param s the s
	 */
	public UnanswerableQuerySelector(PlannerParameters p, CostParameters c, ReasoningParameters r, DatabaseParameters d, Schema s) {
		this.planParams = (PlannerParameters) p.clone();
		this.costParams = (CostParameters) c.clone();
		this.planParams.setTimeout(10000);
		this.reasoningParams = (ReasoningParameters) r.clone();

		this.dbParams = (DatabaseParameters) r.clone();
		this.schema = s;
	}
	
	/**
	 * {@inheritDoc}
	 * @throws SQLException 
	 * @see uk.ac.ox.cs.pdq.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean accept(ConjunctiveQuery q) {
		try {
			try {
				return new ExplorationSetUp(this.planParams, this.costParams, this.reasoningParams, this.dbParams, this.schema).search(q,true) != null;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		} catch (PlannerException e) {
			log.error(e);
			return false;
		}
	}

}
