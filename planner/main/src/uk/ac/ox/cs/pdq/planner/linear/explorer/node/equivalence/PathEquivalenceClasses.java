package uk.ac.ox.cs.pdq.planner.linear.explorer.node.equivalence;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

/**
 * Collections of paths that have equivalent configurations.
 * Each node of the path is saturated using the chase and is associated with a unique configuration.
 * A chase sequence v is  equivalent to another sequence v' if there is a bijection h from the configuration of v
 * to the configuration of v' that preserves any constants of the input schema S_0,
 * the original relations of S_0 and the relations of the form InferredAccR.
 *
 * @author Efthymia Tsamoura
 *
 */
public class PathEquivalenceClasses {

	/** Maps each node to the equivalence class it belongs. This implementation keeps only the final node of each path*/
	private final BiMap<SearchNode,PathEquivalenceClass> entries;

	public PathEquivalenceClasses() {
		this.entries = HashBiMap.create();
	}

	/**
	 * Adds the child to the equivalence class of the parent
	 * @param parent
	 * @param child
	 * @return the parent's equivalence class
	 */
	public PathEquivalenceClass addEntry(SearchNode parent, SearchNode child) {
		PathEquivalenceClass equivalenceClass = this.entries.get(parent);
		Preconditions.checkNotNull(equivalenceClass);
		equivalenceClass.addPath(child.getPathFromRoot());
		return equivalenceClass;
	}

	/**
	 * Creates a new class given the input node
	 * @param parent
	 */
	public void addEntry(SearchNode parent) {
		this.entries.put(parent, new PathEquivalenceClass(parent));
	}

	/**
	 * Adds the path to the given equivalence class
	 * @param path
	 * @param equivalenceClass
	 */
	public void addEntry(List<Integer> path, PathEquivalenceClass equivalenceClass) {
		Preconditions.checkArgument(this.entries.containsValue(equivalenceClass));
		equivalenceClass.addPath(path);
	}

	/**
	 * @param path
	 * @return the paths (and their associated equivalence classes) that have the input path as prefix
	 */
	public List<Entry<PathEquivalenceClass,List<Integer>>> isPrefixOf(List<Integer> path) {
		List<Entry<PathEquivalenceClass,List<Integer>>> target = new ArrayList<>();
		for(PathEquivalenceClass equivalenceClass:this.entries.values()) {
			for(List<Integer> existingPath:equivalenceClass.getPaths()) {
				if(Collections.indexOfSubList(existingPath, path) == 0) {
					target.add(new AbstractMap.SimpleEntry<PathEquivalenceClass,List<Integer>>(equivalenceClass, existingPath));
				}
			}
		}
		return target;
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.entries.toString();
	}

	/**
	 * A collection of equivalent paths
	 *
	 * @author Efthymia Tsamoura
	 */
	public static class PathEquivalenceClass {
		/** The representative node of this class*/
		private final SearchNode representativeNode;
		/** The representative path of this class*/
		private final List<Integer> representativePath;
		/** Set of equivalent paths*/
		private final Set<List<Integer>> paths;

		/**
		 * Creates a new class setting the input node as representative
		 * @param representativeNode
		 */
		public PathEquivalenceClass(SearchNode representativeNode) {
			this.representativeNode = representativeNode;
			this.representativePath = representativeNode.getPathFromRoot();
			this.paths = Sets.newHashSet();
			this.paths.add(this.representativePath);
		}

		/**
		 * @return SearchNode
		 */
		public SearchNode getRepresentativeNode() {
			return this.representativeNode;
		}

		/**
		 * @return List<Integer>
		 */
		public List<Integer> getRepresentativePath() {
			return this.representativePath;
		}

		/**
		 * @return Set<List<Integer>>
		 */
		public Set<List<Integer>> getPaths() {
			return this.paths;
		}

		/**
		 * @param otherPath List<Integer>
		 */
		public void addPath(List<Integer> otherPath) {
			this.paths.add(otherPath);
		}

		/**
		 * @param o Object
		 * @return boolean
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null) {
				return false;
			}
			return this.getClass().isInstance(o)
					&& this.representativeNode.equals(((PathEquivalenceClass) o).representativeNode)
					&& this.representativePath.equals(((PathEquivalenceClass) o).representativePath)
					&& this.paths.equals(((PathEquivalenceClass) o).paths);
		}

		/**
		 * @return String
		 */
		@Override
		public String toString() {
			return this.representativeNode + "\n"
					+ this.representativePath + "\n" +
					Joiner.on("\n").join(this.paths);
		}
	}
}