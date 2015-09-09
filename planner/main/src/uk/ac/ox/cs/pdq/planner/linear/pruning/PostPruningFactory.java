package uk.ac.ox.cs.pdq.planner.linear.pruning;

import uk.ac.ox.cs.pdq.planner.PlannerParameters.PostPruningTypes;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;

/**
 * Creates plan post-pruning objects
 *
 * @author Efthymia Tsamoura
 *
 * @param <S>
 * @param <N>
 */
public class PostPruningFactory {

	private final PostPruningTypes type;
	private final NodeFactory nodeFactory;
	private final AccessibleSchema accessibleSchema;

	/**
	 * Constructor for PlanPostPruningFactory.
	 * @param type PostPruningTypes
	 * @param nodeFactory NodeFactory<S,N>
	 * @param accessibleSchema AccessibleSchema
	 */
	public PostPruningFactory(PostPruningTypes type, NodeFactory nodeFactory, AccessibleSchema accessibleSchema) {
		this.type = type;
		this.nodeFactory = nodeFactory;
		this.accessibleSchema = accessibleSchema;
	}

	/**
	 * @return PlanPostPruning<S,N>
	 */
	public PostPruning getInstance() {
		if(this.type == null) {
			return null;
		}
		switch(this.type) {
		case REMOVE_ACCESSES:
			return new PostPruningRemoveFollowUps(this.nodeFactory, this.accessibleSchema);
		default:
			return null;
		}
	}
}
