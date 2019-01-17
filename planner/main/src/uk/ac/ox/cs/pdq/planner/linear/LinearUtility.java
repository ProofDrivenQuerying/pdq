package uk.ac.ox.cs.pdq.planner.linear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.plantree.IndexedDirectedGraph;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * The Class LinearUtility.
 *
 * @author Efthymia Tsamoura
 */
public class LinearUtility {

	
	
	/**
	 *
	 * @param candidates a set of Candidates (facts whose input constants are accessible)
	 * @return 		the output constants of the input candidate facts
	 */
	public static Collection<Constant> getOutputConstants(Set<Candidate> candidates) {
		Set<Atom> facts = new HashSet<>();
		for(Candidate candidate:candidates) {
			facts.add(candidate.getFact());
		}
		return Utility.getUntypedConstants(facts);
	}
	
	/**
	 *
	 * @param <T> the generic type
	 * @param tree 		the input node tree
	 * @param ids a set of numbers that should be ids of nodes in the tree
	 * @return 		a sequence of nodes having the input node ids
	 */
	public static <T extends SearchNode> List<T> createPath(IndexedDirectedGraph<T> tree, List<Integer> ids){
		Preconditions.checkArgument(ids != null && !ids.isEmpty());
		List<T> nodes = new ArrayList<>();
		for (Integer n: ids) {
			T node = tree.getVertex(n);
			Preconditions.checkNotNull(node);
			nodes.add(node);
		}
		return nodes;
	}
}
