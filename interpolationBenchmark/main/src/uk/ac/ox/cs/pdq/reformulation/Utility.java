package uk.ac.ox.cs.pdq.reformulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.beust.jcommander.internal.Lists;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class Utility {

	private static <T extends Object> void getSubsets(List<T> superSet, int k, int idx, Set<T> currentSubset, List<Set<T>> subsets) {
		if (currentSubset.size() == k) {
			subsets.add(new HashSet<>(currentSubset));
			return;
		}
		if (idx == superSet.size()) {
			return;
		}
		else {
			T x = superSet.get(idx);
			currentSubset.add(x);
			getSubsets(superSet, k, idx+1, currentSubset, subsets);
			currentSubset.remove(x);
			getSubsets(superSet, k, idx+1, currentSubset, subsets);
		}
	}

	/**
	 * 
	 * @param superSet
	 * @param k
	 * @return all subsets of the input list of elements of size k
	 */
	public static <T extends Object> List<Set<T>> getSubsets(List<T> superSet, int k) {
		List<Set<T>> res = new ArrayList<>();
		getSubsets(superSet, k, 0, new HashSet<T>(), res);
		return res;
	}

	public static List<Integer> findIndexes(String input, String character){	 
		List<Integer> indices = Lists.newArrayList();
		int index = input.indexOf(character);
		while(index >= 0) {
			indices.add(index);
			index = input.indexOf(character, index+1);
		}
		return indices;
	}


}
