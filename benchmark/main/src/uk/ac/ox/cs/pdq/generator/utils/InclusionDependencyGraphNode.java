package uk.ac.ox.cs.pdq.generator.utils;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

/**
 * Graph representation of foreign key dependencies.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class InclusionDependencyGraphNode {

	Relation node;

	SortedSet<InclusionDependencyGraphNode> neighbors;

	SortedSet<InclusionDependencyGraphNode> backNeighbors;

	public InclusionDependencyGraphNode(Relation n) {
		this.node = n;
		this.neighbors = new TreeSet<>(new NodeComparator());
		this.backNeighbors = new TreeSet<>(new NodeComparator());
		
	}
	
	public Relation getRelation() {
		return this.node;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.node.getName());
		String sep = " --> ";
		for (InclusionDependencyGraphNode n : this.neighbors) {
			result.append(sep).append(n.node.getName());
			sep = ", ";
		}
		return result.toString();
	}

	public void addNeighbor(InclusionDependencyGraphNode neighbor) {
		if (!this.neighbors.contains(neighbor)) {
			this.neighbors.add(neighbor);
		}
	}

	public void addBackNeighbor(InclusionDependencyGraphNode neighbor) {
		if (!this.backNeighbors.contains(neighbor)) {
			this.backNeighbors.add(neighbor);
		}
	}

	/**
	 * Randomly traverse the dependency graph and return the path used.
	 * 
	 * @param random
	 * @param maxDepth
	 * @return a random path in the dependency graph.
	 */
	public List<Predicate> traverseRandom(
			Random random, int maxDepth,
			Predicate prevPred,
			Relation prevRel) {
		List<Predicate> result = new LinkedList<>();
		if (maxDepth > 0) {
			List<Term> terms = Utility.generateVariables(this.node);
			if (prevPred != null) {
				for (ForeignKey fk : prevRel.getForeignKeys()) {
					if (this.node.equals(fk.getForeignRelation())) {
						for (Reference ref : fk.getReferences()) {
							int remotePos = this.node.getAttributeIndex(ref.getForeignAttributeName());
							int localPos = prevRel.getAttributeIndex(ref.getLocalAttributeName());
							terms.set(remotePos, prevPred.getTerm(localPos));
						}
					}
				}
			}
			Predicate p = new Predicate(this.node, terms);
			result.add(p);
			if (!this.neighbors.isEmpty()) {
				InclusionDependencyGraphNode next = Lists.newArrayList(this.neighbors).get(random.nextInt(this.neighbors.size()));
				// Avoid cylcle of size 2
				if (!next.getRelation().equals(prevRel)) {
					result.addAll(next.traverseRandom(random, maxDepth - 1, p, this.node));
				}
			}
		}
		return result;
	}

	public List<InclusionDependencyGraphNode> traverseBackwards(int maxDistance) {
		Set<InclusionDependencyGraphNode> ret = new LinkedHashSet<>();
		if(maxDistance >= 1) {
			for(InclusionDependencyGraphNode backNeighbor:this.backNeighbors) {
				ret.add(backNeighbor);
				ret.addAll(backNeighbor.traverseBackwards(maxDistance, 2));
			}
		}
		return Lists.newArrayList(ret);
	}


	private Set<InclusionDependencyGraphNode> traverseBackwards(int maxDistance, int currentDistance) {
		Set<InclusionDependencyGraphNode> ret = new LinkedHashSet<>();
		if(maxDistance >= currentDistance) {
			for(InclusionDependencyGraphNode backNeighbor:this.backNeighbors) {
				ret.add(backNeighbor);
				ret.addAll(backNeighbor.traverseBackwards(maxDistance, ++currentDistance));
			}
		}
		return ret;
	}
	
	
	private static class NodeComparator implements Comparator<InclusionDependencyGraphNode> {

		@Override
		public int compare(InclusionDependencyGraphNode o1, InclusionDependencyGraphNode o2) {
			if (!o1.node.equals(o2)) {
				return o1.node.getName().compareTo(o2.node.getName());
			}
			List<ForeignKey> fk1 = o1.node.getForeignKeys();
			List<ForeignKey> fk2 = o2.node.getForeignKeys();
			if (fk1 != null) {
				if (fk2 != null) {
					if (fk1.size() == fk2.size()) {
						for (int i = 0, l = fk1.size(); i < l; i++) {
							String name1 = fk1.get(i).getName();
							String name2 = fk2.get(i).getName();
							if (!name1.equals(name2)) {
								return name1.compareTo(name2);
							}
						}
					}
					return fk1.size() - fk2.size();
				}
				return -1;
			}
			return fk2 == null ? 0 : 1;
		}
		
	}
}