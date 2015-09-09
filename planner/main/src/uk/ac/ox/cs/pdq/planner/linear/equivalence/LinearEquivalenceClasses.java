package uk.ac.ox.cs.pdq.planner.linear.equivalence;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

/**
 * A collection of equivalence classes.
 *
 * @author Efthymia Tsamoura
 *
 * @param <S>
 */
public class LinearEquivalenceClasses {

	/** Maps each node to the equivalence class it belongs to*/
	private final BiMap<SearchNode,EquivalenceClass> entries;

	public LinearEquivalenceClasses() {
		this.entries = HashBiMap.create();
	}

	/**
	 * Adds the child to the equivalence class of the parent
	 * @param parent
	 * @param child
	 * @return the parent's equivalence class
	 */
	public EquivalenceClass addEntry(SearchNode parent, SearchNode child) {
		EquivalenceClass equivalenceClass = this.entries.get(parent);
		Preconditions.checkNotNull(equivalenceClass);
		equivalenceClass.addPath(child.getPathFromRoot());
		return equivalenceClass;
	}

	/**
	 * Creates a new class given the input node
	 * @param parent
	 */
	public void addEntry(SearchNode parent) {
		this.entries.put(parent, new EquivalenceClass(parent));
	}

	/**
	 * Adds the path to the given equivalence class
	 * @param path
	 * @param equivalenceClass
	 */
	public void addEntry(List<Integer> path, EquivalenceClass equivalenceClass) {
		Preconditions.checkArgument(this.entries.containsValue(equivalenceClass));
		equivalenceClass.addPath(path);
	}

	/**
	 * @param path
	 * @return the paths (and their associated equivalence classes) that have the input path as prefix
	 */
	public List<Entry<EquivalenceClass,List<Integer>>> isPrefixOf(List<Integer> path) {
		List<Entry<EquivalenceClass,List<Integer>>> target = new ArrayList<>();
		for(EquivalenceClass equivalenceClass:this.entries.values()) {
			for(List<Integer> existingPath:equivalenceClass.getPaths()) {
				if(Collections.indexOfSubList(existingPath, path) == 0) {
					target.add(new AbstractMap.SimpleEntry<EquivalenceClass,List<Integer>>(equivalenceClass, existingPath));
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
	 * Class of structurally equivalent configurations
	 *
	 * @author Efthymia Tsamoura
	 */
	public static class EquivalenceClass {
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
		public EquivalenceClass(SearchNode representativeNode) {
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
					&& this.representativeNode.equals(((EquivalenceClass) o).representativeNode)
					&& this.representativePath.equals(((EquivalenceClass) o).representativePath)
					&& this.paths.equals(((EquivalenceClass) o).paths);
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