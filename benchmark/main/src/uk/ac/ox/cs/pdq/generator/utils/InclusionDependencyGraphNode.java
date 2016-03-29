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
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Graph representation of foreign key dependencies.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class InclusionDependencyGraphNode {

	/** The node. */
	Relation node;

	/** The neighbors. */
	SortedSet<InclusionDependencyGraphNode> neighbors;

	/** The back neighbors. */
	SortedSet<InclusionDependencyGraphNode> backNeighbors;

	/**
	 * Instantiates a new inclusion dependency graph node.
	 *
	 * @param n the n
	 */
	public InclusionDependencyGraphNode(Relation n) {
		this.node = n;
		this.neighbors = new TreeSet<>(new NodeComparator());
		this.backNeighbors = new TreeSet<>(new NodeComparator());
		
	}
	
	/**
	 * Gets the relation.
	 *
	 * @return the relation
	 */
	public Relation getRelation() {
		return this.node;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
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

	/**
	 * Adds the neighbor.
	 *
	 * @param neighbor the neighbor
	 */
	public void addNeighbor(InclusionDependencyGraphNode neighbor) {
		if (!this.neighbors.contains(neighbor)) {
			this.neighbors.add(neighbor);
		}
	}

	/**
	 * Adds the back neighbor.
	 *
	 * @param neighbor the neighbor
	 */
	public void addBackNeighbor(InclusionDependencyGraphNode neighbor) {
		if (!this.backNeighbors.contains(neighbor)) {
			this.backNeighbors.add(neighbor);
		}
	}

	/**
	 * Randomly traverse the dependency graph and return the path used.
	 *
	 * @param random the random
	 * @param maxDepth the max depth
	 * @param prevPred the prev pred
	 * @param prevRel the prev rel
	 * @return a random path in the dependency graph.
	 */
	public List<Atom> traverseRandom(
			Random random, int maxDepth,
			Atom prevPred,
			Relation prevRel) {
		List<Atom> result = new LinkedList<>();
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
			Atom p = new Atom(this.node, terms);
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

	/**
	 * Traverse backwards.
	 *
	 * @param maxDistance the max distance
	 * @return the list
	 */
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


	/**
	 * Traverse backwards.
	 *
	 * @param maxDistance the max distance
	 * @param currentDistance the current distance
	 * @return the sets the
	 */
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
	
	
	/**
	 * The Class NodeComparator.
	 */
	private static class NodeComparator implements Comparator<InclusionDependencyGraphNode> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
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