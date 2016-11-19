package uk.ac.ox.cs.pdq.planner.linear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.util.IndexedDirectedGraph;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * The Class LinearUtility.
 *
 * @author Efthymia Tsamoura
 */
public class LinearUtility {

	/**
	 * Gets the input constants.
	 *
	 * @param exposed the exposed
	 * @return 		the input constants of the input candidate facts
	 */
	public static Collection<Constant> getInputConstants(Set<Candidate> exposed) {
		Preconditions.checkNotNull(exposed);
		Preconditions.checkArgument(exposed.size() > 0);
		Candidate candidate = exposed.iterator().next();
		return PlannerUtility.getInputConstants(candidate.getRule(), Sets.newHashSet(candidate.getFact()));
	}
	
	/**
	 * Gets the output constants.
	 *
	 * @param candidates the candidates
	 * @return 		the output constants of the input candidate facts
	 */
	public static Collection<Constant> getOutputConstants(Set<Candidate> candidates) {
		Set<Atom> facts = new HashSet<>();
		for(Candidate candidate:candidates) {
			facts.add(candidate.getFact());
		}
		return Utility.getTypedConstants(facts);
	}
	
	/**
	 * Creates the path.
	 *
	 * @param <T> the generic type
	 * @param tree 		the input node tree
	 * @param ids the ids
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
