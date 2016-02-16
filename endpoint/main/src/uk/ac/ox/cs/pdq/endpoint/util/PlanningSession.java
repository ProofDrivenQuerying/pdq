package uk.ac.ox.cs.pdq.endpoint.util;

import java.io.Serializable;
import java.util.concurrent.Future;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * The Class PlanningSession.
 */
public class PlanningSession implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2907015742722726690L;

	/** The schema. */
	private final Schema schema;
	
	/** The query. */
	private final Query<?> query;
	
	/** The future. */
	private final Future<Plan> future;
	
	/** The logger. */
	private final BufferedProgressLogger logger;

	/**
	 * Instantiates a new planning session.
	 *
	 * @param schema the schema
	 * @param query the query
	 * @param future the future
	 * @param logger the logger
	 */
	public PlanningSession(Schema schema, Query<?> query, Future<Plan> future, BufferedProgressLogger logger) {
		super();
		Preconditions.checkArgument(schema != null);
		Preconditions.checkArgument(query != null);
		Preconditions.checkArgument(future != null);
		Preconditions.checkArgument(logger != null);
		this.schema = schema;
		this.query = query;
		this.future = future;
		this.logger = logger;
	}

	/**
	 * Gets the schema.
	 *
	 * @return the schema
	 */
	public Schema getSchema() {
		return this.schema;
	}

	/**
	 * Gets the query.
	 *
	 * @return the query
	 */
	public Query<?> getQuery() {
		return this.query;
	}

	/**
	 * Gets the future.
	 *
	 * @return the future
	 */
	public Future<Plan> getFuture() {
		return this.future;
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public BufferedProgressLogger getLogger() {
		return this.logger;
	}
}
