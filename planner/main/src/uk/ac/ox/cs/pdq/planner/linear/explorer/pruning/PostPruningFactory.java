package uk.ac.ox.cs.pdq.planner.linear.explorer.pruning;

import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PostPruningTypes;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.base.Preconditions;

/**
 * Creates plan post-pruning objects
 *
 * @author Efthymia Tsamoura
 *
 */
public class PostPruningFactory {

	private final PostPruningTypes type;
	private final NodeFactory nodeFactory;
	private final AccessibleSchema accessibleSchema;
	private final Chaser chaser;
	private final Query<?> query;

	/**
	 * 
	 * @param type
	 * @param nodeFactory
	 * @param chaser
	 * @param query
	 * @param accessibleSchema
	 */
	public PostPruningFactory(PostPruningTypes type, NodeFactory nodeFactory, Chaser chaser, Query<?> query,
			AccessibleSchema accessibleSchema) {
		Preconditions.checkNotNull(nodeFactory);
		Preconditions.checkNotNull(chaser);
		Preconditions.checkNotNull(query);
		Preconditions.checkNotNull(accessibleSchema);
		this.type = type;
		this.nodeFactory = nodeFactory;
		this.accessibleSchema = accessibleSchema;
		this.chaser = chaser;
		this.query = query;
	}

	/**
	 * 
	 * @return
	 */
	public PostPruning getInstance() {
		if(this.type == null) {
			return null;
		}
		switch(this.type) {
		case REMOVE_ACCESSES:
			return new PostPruningRemoveFollowUps(this.nodeFactory, this.accessibleSchema, this.chaser, this.query);
		default:
			return null;
		}
	}
}
