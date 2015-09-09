package uk.ac.ox.cs.pdq.endpoint.util;

import java.io.Serializable;
import java.util.concurrent.Future;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.base.Preconditions;

public class PlanningSession implements Serializable {
	
	/** */
	private static final long serialVersionUID = 2907015742722726690L;

	private final Schema schema;
	private final Query<?> query;
	private final Future<Plan> future;
	private final BufferedProgressLogger logger;

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

	public Schema getSchema() {
		return this.schema;
	}

	public Query<?> getQuery() {
		return this.query;
	}

	public Future<Plan> getFuture() {
		return this.future;
	}

	public BufferedProgressLogger getLogger() {
		return this.logger;
	}
}
