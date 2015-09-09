package uk.ac.ox.cs.pdq.planner.reasoning.chase.state;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.ExtendedBag;
import uk.ac.ox.cs.pdq.reasoning.chase.BagsTree;

/**
 * Interface for merging trees of bags
 *
 * @author Efthymia Tsamoura
 */
public interface MergeTrees {
	/**
	 * @param tree1 The first input tree
	 * @param tree2 The second input tree
	 * @return the merged tree
	 */
	Pair<BagsTree,ExtendedBag> merge(BagsTree tree1, BagsTree tree2);
}
