package uk.ac.ox.cs.pdq.planner.linear.explorer.pruning;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PostPruningTypes;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

/**
 * Creates plan post-pruning objects.
 *
 * @author Efthymia Tsamoura
 */
public class PostPruningFactory {

	/** The type. */
	private final PostPruningTypes type;
	
	/** The accessible schema. */
	private final AccessibleSchema accessibleSchema;
	
	/** The chaser. */
	private final Chaser chaser;
	
	/** The query. */
	private final ConjunctiveQuery query;

	/**
	 * Instantiates a new post pruning factory.
	 *
	 * @param type the type
	 * @param nodeFactory the node factory
	 * @param chaser the chaser
	 * @param query the query
	 * @param accessibleSchema the accessible schema
	 */
	public PostPruningFactory(PostPruningTypes type, Chaser chaser, ConjunctiveQuery query,
			AccessibleSchema accessibleSchema) {
		Preconditions.checkNotNull(chaser);
		Preconditions.checkNotNull(query);
		Preconditions.checkNotNull(accessibleSchema);
		this.type = type;
		this.accessibleSchema = accessibleSchema;
		this.chaser = chaser;
		this.query = query;
	}

	/**
	 * Gets the single instance of PostPruningFactory.
	 *
	 * @return single instance of PostPruningFactory
	 */
	public PostPruning getInstance() {
		if(this.type == null) {
			return null;
		}
		switch(this.type) {
		case REMOVE_ACCESSES:
			return new PostPruningRemoveFollowUps(this.accessibleSchema, this.chaser, this.query);
		default:
			return null;
		}
	}
}
