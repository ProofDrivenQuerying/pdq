package uk.ac.ox.cs.pdq.planner.linear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.util.IndexedDirectedGraph;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class LinearUtility {

	public static Collection<Constant> getInputConstants(Set<Candidate> exposedCandidates) {
		Preconditions.checkNotNull(exposedCandidates);
		Preconditions.checkArgument(exposedCandidates.size() > 0);
		Candidate candidate = exposedCandidates.iterator().next();
		return PlannerUtility.getInputConstants(candidate.getRule(), Sets.newHashSet(candidate.getFact()));
	}
	
	public static Collection<Constant> getOutputConstants(Set<Candidate> candidates) {
		Set<Predicate> facts = new HashSet<>();
		for(Candidate candidate:candidates) {
			facts.add(candidate.getFact());
		}
		return Utility.getConstants(facts);
	}
	
	public static <T extends SearchNode> List<T> createPath(IndexedDirectedGraph<T> nodesSet, List<Integer> path){
		Preconditions.checkArgument(path != null && !path.isEmpty());
		List<T> nodes = new ArrayList<>();
		for (Integer n: path) {
			T node = nodesSet.getVertex(n);
			Preconditions.checkNotNull(node);
			nodes.add(node);
		}
		return nodes;
	}
}
