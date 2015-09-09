package uk.ac.ox.cs.pdq.planner.dag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.plan.DAGPlan;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

/**
 * This structure keeps, for all combinations of constants,
 * the collection of plans that features these constants as output, sorted by
 * increasing cost.
 *
 * @author Julien Leblay
 */
public class PlanCostIndex {
	protected Map<Set<? extends Term>, SortedMultiset<DAGPlan>> planRegistry = new LinkedHashMap<>();

	/**
	 * @param plan DAGPlan
	 */
	public synchronized void update(DAGPlan plan) {
		Preconditions.checkArgument(plan != null);
		if (!plan.getCost().isUpperBound()) {
			Set<? extends Term> outputs = Sets.newHashSet(plan.getOutput());
			for (Set<? extends Term> subSet: Sets.powerSet(outputs)) {
				if (!subSet.isEmpty()) {
					SortedMultiset<DAGPlan> q = this.planRegistry.get(subSet);
					if (q == null) {
						this.planRegistry.put(subSet, (q = initialize()));
					}
					q.remove(plan, q.size());
					q.add(plan);
				}
			}
		}
	}

	/**
	 * @param inputs Set<? extends Term>
	 * @return DAGPlan
	 */
	public DAGPlan get(Set<? extends Term> inputs) {
		SortedMultiset<DAGPlan> result = this.getAll(inputs);
		if (!result.isEmpty()) {
			return result.firstEntry().getElement();
		}
		return null;
	}

	/**
	 * @param inputs Set<? extends Term>
	 * @return SortedMultiset<DAGPlan>
	 */
	public SortedMultiset<DAGPlan> getAll(Set<? extends Term> inputs) {
		SortedMultiset<DAGPlan> result = this.planRegistry.get(inputs);
		if (result == null) {
			return initialize();
		}
		return result;
	}

	/**
	 * @return SortedMultiset<DAGPlan>
	 */
	private static SortedMultiset<DAGPlan> initialize() {
		return TreeMultiset.<DAGPlan>create();
	}
}
